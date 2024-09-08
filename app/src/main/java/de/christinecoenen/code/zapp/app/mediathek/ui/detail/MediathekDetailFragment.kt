package de.christinecoenen.code.zapp.app.mediathek.ui.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.snackbar.Snackbar
import de.christinecoenen.code.zapp.R
import de.christinecoenen.code.zapp.app.mediathek.controller.downloads.IDownloadController
import de.christinecoenen.code.zapp.app.mediathek.controller.downloads.exceptions.NoNetworkException
import de.christinecoenen.code.zapp.app.mediathek.controller.downloads.exceptions.WrongNetworkConditionException
import de.christinecoenen.code.zapp.app.mediathek.ui.dialogs.ConfirmDeleteDownloadDialog
import de.christinecoenen.code.zapp.app.mediathek.ui.dialogs.SelectQualityDialog
import de.christinecoenen.code.zapp.app.mediathek.ui.helper.ShowMenuProvider
import de.christinecoenen.code.zapp.databinding.MediathekDetailFragmentBinding
import de.christinecoenen.code.zapp.models.shows.DownloadStatus
import de.christinecoenen.code.zapp.models.shows.PersistedMediathekShow
import de.christinecoenen.code.zapp.models.shows.Quality
import de.christinecoenen.code.zapp.repositories.MediathekRepository
import de.christinecoenen.code.zapp.utils.system.ImageHelper.loadThumbnailAsync
import de.christinecoenen.code.zapp.utils.system.IntentHelper.openUrl
import de.christinecoenen.code.zapp.utils.system.LifecycleOwnerHelper.launchOnCreated
import de.christinecoenen.code.zapp.utils.system.SystemUiHelper.applyBottomInsetAsPadding
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.transform
import org.koin.android.ext.android.inject
import timber.log.Timber
import kotlin.time.Duration.Companion.milliseconds

class MediathekDetailFragment : Fragment() {

	private val args: MediathekDetailFragmentArgs by navArgs()

	private var _binding: MediathekDetailFragmentBinding? = null
	private val binding: MediathekDetailFragmentBinding get() = _binding!!

	private val downloadController: IDownloadController by inject()
	private val mediathekRepository: MediathekRepository by inject()

	private var startDownloadJob: Job? = null
	private var persistedMediathekShow: PersistedMediathekShow? = null
	private var downloadStatus = DownloadStatus.NONE

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		_binding = MediathekDetailFragmentBinding.inflate(inflater, container, false)
		binding.root.isVisible = false

		viewLifecycleOwner.launchOnCreated {
			loadOrPersistShowFromArguments()
		}

		binding.root.applyBottomInsetAsPadding()

		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		binding.play.setOnClickListener { onPlayClick() }
		binding.buttons.download.setOnClickListener { onDownloadClick() }
		binding.buttons.share.setOnClickListener { onShareClick() }
		binding.buttons.website.setOnClickListener { onWebsiteClick() }
	}

	override fun onResume() {
		super.onResume()

		downloadController.deleteDownloadsWithDeletedFiles()
	}

	private suspend fun loadOrPersistShowFromArguments() {
		if (args.mediathekShow != null) {
			// persist show if passed to this fragment
			val persistedShow = mediathekRepository
				.persistOrUpdateShow(args.mediathekShow!!)
				.first()

			onShowLoaded(persistedShow)
		} else {
			// load existing persisted show from id
			val persistedShow = mediathekRepository
				.getPersistedShow(args.persistedShowId)
				.first()

			onShowLoaded(persistedShow)
		}
	}

	private fun onShowLoaded(persistedMediathekShow: PersistedMediathekShow) {
		this.persistedMediathekShow = persistedMediathekShow

		val show = persistedMediathekShow.mediathekShow
		binding.texts.topic.text = show.topic
		binding.texts.title.text = show.title
		binding.texts.description.text = show.description
		binding.time.text = show.formattedTimestamp
		binding.channel.text = show.channel
		binding.duration.text = show.formattedDuration
		binding.subtitle.isVisible = show.hasSubtitle
		binding.buttons.download.isEnabled = show.hasAnyDownloadQuality()

		binding.root.isVisible = true

		requireActivity().addMenuProvider(
			ShowMenuProvider(this, show),
			viewLifecycleOwner,
			Lifecycle.State.RESUMED
		)

		viewLifecycleOwner.launchOnCreated {
			downloadController
				.getDownloadStatus(persistedMediathekShow.id)
				.collect(::onDownloadStatusChanged)
		}

		viewLifecycleOwner.launchOnCreated {
			downloadController
				.getDownloadProgress(persistedMediathekShow.id)
				.collect(::onDownloadProgressChanged)
		}

		viewLifecycleOwner.launchOnCreated {
			mediathekRepository
				.getPlaybackPositionPercent(show.apiId)
				.collect(::updatePlaybackPosition)
		}

		updateVideoThumbnail()
	}

	private fun updatePlaybackPosition(viewingProgress: Float) {
		binding.viewingProgress.progress = (viewingProgress * binding.viewingProgress.max).toInt()
	}

	private fun onDownloadStatusChanged(downloadStatus: DownloadStatus) {
		this.downloadStatus = downloadStatus
		adjustUiToDownloadStatus(downloadStatus)
	}

	private fun onDownloadProgressChanged(progress: Int) {
		binding.buttons.downloadProgress.progress = progress
	}

	private fun onPlayClick() {
		val directions =
			MediathekDetailFragmentDirections.toMediathekPlayerActivity(persistedMediathekShow!!.id)
		findNavController().navigate(directions)
	}

	private fun onDownloadClick() {
		when (downloadStatus) {
			DownloadStatus.NONE,
			DownloadStatus.CANCELLED,
			DownloadStatus.DELETED,
			DownloadStatus.PAUSED,
			DownloadStatus.REMOVED,
			DownloadStatus.FAILED ->
				showSelectQualityDialog(SelectQualityDialog.Mode.DOWNLOAD)
			DownloadStatus.ADDED,
			DownloadStatus.QUEUED,
			DownloadStatus.DOWNLOADING ->
				downloadController.stopDownload(persistedMediathekShow!!.id)
			DownloadStatus.COMPLETED ->
				showConfirmDeleteDialog()
		}
	}

	private fun onShareClick() {
		showSelectQualityDialog(SelectQualityDialog.Mode.SHARE)
	}

	private fun onWebsiteClick() {
		openUrl(requireContext(), persistedMediathekShow!!.mediathekShow.websiteUrl)
	}

	private fun showConfirmDeleteDialog() {
		val dialog = ConfirmDeleteDownloadDialog()

		setFragmentResultListener(ConfirmDeleteDownloadDialog.REQUEST_KEY_CONFIRMED) { _, _ ->
			downloadController.deleteDownload(persistedMediathekShow!!.id)
		}

		dialog.show(parentFragmentManager, null)
	}

	private fun showSelectQualityDialog(mode: SelectQualityDialog.Mode) {
		val dialog = SelectQualityDialog.newInstance(persistedMediathekShow!!.mediathekShow, mode)

		setFragmentResultListener(SelectQualityDialog.REQUEST_KEY_SELECT_QUALITY) { _, bundle ->
			val quality = SelectQualityDialog.getSelectedQuality(bundle)
			when (mode) {
				SelectQualityDialog.Mode.DOWNLOAD -> download(quality)
				SelectQualityDialog.Mode.SHARE -> share(quality)
			}
		}

		dialog.show(parentFragmentManager, null)
	}

	private fun adjustUiToDownloadStatus(status: DownloadStatus) {
		when (status) {
			DownloadStatus.NONE, DownloadStatus.CANCELLED, DownloadStatus.DELETED, DownloadStatus.PAUSED, DownloadStatus.REMOVED -> {
				binding.buttons.downloadProgress.visibility = View.GONE
				binding.buttons.download.setText(R.string.fragment_mediathek_download)
				binding.buttons.download.setIconResource(R.drawable.ic_baseline_save_alt_24)
			}
			DownloadStatus.ADDED, DownloadStatus.QUEUED -> {
				binding.buttons.downloadProgress.visibility = View.VISIBLE
				binding.buttons.downloadProgress.isIndeterminate = true
				binding.buttons.download.setText(R.string.fragment_mediathek_download_queued)
				binding.buttons.download.setIconResource(R.drawable.ic_outline_stop_24)
			}
			DownloadStatus.DOWNLOADING -> {
				binding.buttons.downloadProgress.visibility = View.VISIBLE
				binding.buttons.downloadProgress.isIndeterminate = false
				binding.buttons.download.setText(R.string.fragment_mediathek_download_running)
				binding.buttons.download.setIconResource(R.drawable.ic_outline_stop_24)
			}
			DownloadStatus.COMPLETED -> {
				binding.buttons.downloadProgress.visibility = View.GONE
				binding.buttons.download.setText(R.string.fragment_mediathek_download_delete)
				binding.buttons.download.setIconResource(R.drawable.ic_baseline_delete_outline_24)
			}
			DownloadStatus.FAILED -> {
				binding.buttons.downloadProgress.visibility = View.GONE
				binding.buttons.download.setText(R.string.fragment_mediathek_download_retry)
				binding.buttons.download.setIconResource(R.drawable.ic_outline_warning_amber_24)
			}
		}
	}

	private fun updateVideoThumbnail() {
		viewLifecycleOwner.launchOnCreated {

			// reload show for up to date file path and then update thumbnail
			mediathekRepository
				.getCompletetlyDownloadedVideoPath(persistedMediathekShow!!.mediathekShow.apiId)
				.transform {
					emit(it)
					delay(500.milliseconds)
				}
				.map { if (it == null) null else loadThumbnailAsync(binding.root.context, it) }
				.catch { e -> Timber.e(e) }
				.collectLatest {
					binding.texts.thumbnail.setImageBitmap(it)
					binding.texts.thumbnail.isVisible = (it != null)
				}
		}
	}

	private fun share(quality: Quality) {
		persistedMediathekShow?.mediathekShow?.playExternally(requireContext(), quality)
	}

	private fun download(downloadQuality: Quality) {
		startDownloadJob?.cancel()

		startDownloadJob = viewLifecycleOwner.launchOnCreated {

			try {
				downloadController.startDownload(persistedMediathekShow!!.id, downloadQuality)
			} catch (e: Exception) {
				onStartDownloadException(e)
			}
		}
	}

	private fun onStartDownloadException(throwable: Throwable) {
		when (throwable) {
			is WrongNetworkConditionException -> {
				Snackbar
					.make(
						requireView(),
						R.string.error_mediathek_download_over_unmetered_network_only,
						Snackbar.LENGTH_LONG
					)
					.setAction(R.string.activity_settings_title) {
						val directions = MediathekDetailFragmentDirections.toSettingsFragment()
						findNavController().navigate(directions)
					}
					.show()
			}
			is NoNetworkException -> {
				Snackbar
					.make(
						requireView(),
						R.string.error_mediathek_download_no_network,
						Snackbar.LENGTH_LONG
					)
					.show()
			}
			else -> {
				Snackbar
					.make(
						requireView(),
						R.string.error_mediathek_generic_start_download_error,
						Snackbar.LENGTH_LONG
					)
					.show()
				Timber.e(throwable)
			}
		}
	}
}
