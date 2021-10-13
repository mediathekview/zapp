package de.christinecoenen.code.zapp.app.mediathek.ui.detail

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import de.christinecoenen.code.zapp.R
import de.christinecoenen.code.zapp.app.mediathek.controller.downloads.IDownloadController
import de.christinecoenen.code.zapp.app.mediathek.controller.downloads.exceptions.NoNetworkException
import de.christinecoenen.code.zapp.app.mediathek.controller.downloads.exceptions.WrongNetworkConditionException
import de.christinecoenen.code.zapp.app.mediathek.ui.detail.dialogs.ConfirmFileDeletionDialog
import de.christinecoenen.code.zapp.app.mediathek.ui.detail.dialogs.SelectQualityDialog
import de.christinecoenen.code.zapp.app.mediathek.ui.detail.player.MediathekPlayerActivity
import de.christinecoenen.code.zapp.app.settings.ui.SettingsActivity
import de.christinecoenen.code.zapp.databinding.FragmentMediathekDetailBinding
import de.christinecoenen.code.zapp.models.shows.DownloadStatus
import de.christinecoenen.code.zapp.models.shows.MediathekShow
import de.christinecoenen.code.zapp.models.shows.PersistedMediathekShow
import de.christinecoenen.code.zapp.models.shows.Quality
import de.christinecoenen.code.zapp.repositories.MediathekRepository
import de.christinecoenen.code.zapp.utils.system.ImageHelper.loadThumbnailAsync
import de.christinecoenen.code.zapp.utils.system.IntentHelper.openUrl
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import org.koin.android.ext.android.inject
import timber.log.Timber

class MediathekDetailFragment : Fragment() {

	companion object {

		private const val ARG_SHOW = "ARG_SHOW"

		fun getInstance(show: MediathekShow): MediathekDetailFragment {
			return MediathekDetailFragment().apply {
				arguments = Bundle().apply {
					putSerializable(ARG_SHOW, show)
				}
			}
		}
	}

	private var _binding: FragmentMediathekDetailBinding? = null
	private val binding: FragmentMediathekDetailBinding get() = _binding!!

	private val downloadController: IDownloadController by inject()
	private val mediathekRepository: MediathekRepository by inject()

	private var startDownloadJob: Job? = null
	private var persistedMediathekShow: PersistedMediathekShow? = null
	private var argumentsMediathekShow: MediathekShow? = null
	private var downloadStatus = DownloadStatus.NONE

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		argumentsMediathekShow = requireArguments().getSerializable(ARG_SHOW) as MediathekShow

		lifecycleScope.launchWhenCreated {
			val persistedShow = mediathekRepository
				.persistOrUpdateShow(argumentsMediathekShow!!)
				.first()

			onShowLoaded(persistedShow)
		}
	}

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		_binding = FragmentMediathekDetailBinding.inflate(inflater, container, false)

		binding.play.setOnClickListener { onPlayClick() }
		binding.buttons.download.setOnClickListener { onDownloadClick() }
		binding.buttons.share.setOnClickListener { onShareClick() }
		binding.buttons.website.setOnClickListener { onWebsiteClick() }

		lifecycleScope.launchWhenCreated {
			mediathekRepository
				.getPlaybackPositionPercent(argumentsMediathekShow!!.apiId)
				.collect(::updatePlaybackPosition)
		}

		return binding.root
	}

	override fun onResume() {
		super.onResume()

		downloadController.deleteDownloadsWithDeletedFiles()
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

		lifecycleScope.launchWhenCreated {
			downloadController
				.getDownloadStatus(show.apiId)
				.collect(::onDownloadStatusChanged)
		}

		lifecycleScope.launchWhenCreated {
			downloadController
				.getDownloadProgress(show.apiId)
				.collect(::onDownloadProgressChanged)
		}
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
		startActivity(MediathekPlayerActivity.getStartIntent(context, persistedMediathekShow!!.id))
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
		val dialog = ConfirmFileDeletionDialog()

		setFragmentResultListener(ConfirmFileDeletionDialog.REQUEST_KEY_CONFIRMED) { _, _ ->
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
		binding.texts.thumbnail.visibility = View.GONE

		when (status) {
			DownloadStatus.NONE, DownloadStatus.CANCELLED, DownloadStatus.DELETED, DownloadStatus.PAUSED, DownloadStatus.REMOVED -> {
				binding.buttons.downloadProgress.visibility = View.GONE
				binding.buttons.download.setText(R.string.fragment_mediathek_download)
				binding.buttons.download.setIconResource(R.drawable.ic_baseline_save_alt_24)
			}
			DownloadStatus.ADDED, DownloadStatus.QUEUED -> {
				binding.buttons.downloadProgress.visibility = View.VISIBLE
				binding.buttons.downloadProgress.isIndeterminate = true
				binding.buttons.download.setText(R.string.fragment_mediathek_download_running)
				binding.buttons.download.setIconResource(R.drawable.ic_stop_white_24dp)
			}
			DownloadStatus.DOWNLOADING -> {
				binding.buttons.downloadProgress.visibility = View.VISIBLE
				binding.buttons.downloadProgress.isIndeterminate = false
				binding.buttons.download.setText(R.string.fragment_mediathek_download_running)
				binding.buttons.download.setIconResource(R.drawable.ic_stop_white_24dp)
			}
			DownloadStatus.COMPLETED -> {
				binding.buttons.downloadProgress.visibility = View.GONE
				binding.buttons.download.setText(R.string.fragment_mediathek_download_delete)
				binding.buttons.download.setIconResource(R.drawable.ic_baseline_delete_outline_24)
				updateVideoThumbnail()
			}
			DownloadStatus.FAILED -> {
				binding.buttons.downloadProgress.visibility = View.GONE
				binding.buttons.download.setText(R.string.fragment_mediathek_download_retry)
				binding.buttons.download.setIconResource(R.drawable.ic_warning_white_24dp)
			}
		}
	}

	private fun updateVideoThumbnail() {
		lifecycleScope.launchWhenCreated {

			// reload show for up to date file path and then update thumbnail
			mediathekRepository
				.getPersistedShow(persistedMediathekShow!!.id)
				.map { it.downloadedVideoPath }
				.filterNotNull()
				.distinctUntilChanged()
				.map { loadThumbnailAsync(binding.root.context, it) }
				.catch { e -> Timber.e(e) }
				.collectLatest {
					binding.texts.thumbnail.setImageBitmap(it)
					binding.texts.thumbnail.visibility = View.VISIBLE
				}
		}
	}

	private fun share(quality: Quality) {
		val videoIntent = Intent(Intent.ACTION_VIEW).apply {
			val url = persistedMediathekShow!!.mediathekShow.getVideoUrl(quality)
			setDataAndType(Uri.parse(url), "video/*")
		}

		startActivity(Intent.createChooser(videoIntent, getString(R.string.action_share)))
	}

	private fun download(downloadQuality: Quality) {
		startDownloadJob?.cancel()

		startDownloadJob = lifecycleScope.launchWhenCreated {

			try {
				downloadController.startDownload(persistedMediathekShow!!, downloadQuality)
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
						startActivity(SettingsActivity.getStartIntent(requireContext()))
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
