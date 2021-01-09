package de.christinecoenen.code.zapp.app.mediathek.ui.detail

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import de.christinecoenen.code.zapp.R
import de.christinecoenen.code.zapp.app.ZappApplicationBase
import de.christinecoenen.code.zapp.app.mediathek.controller.downloads.DownloadController
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
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import timber.log.Timber

class MediathekDetailFragment : Fragment(), ConfirmFileDeletionDialog.Listener, SelectQualityDialog.Listener {

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

	private val createDisposables = CompositeDisposable()
	private val createViewDisposables = CompositeDisposable()
	private var startDownloadDisposable: Disposable = CompositeDisposable()
	private var mediathekRepository: MediathekRepository? = null
	private var persistedMediathekShow: PersistedMediathekShow? = null
	private var argumentsMediathekShow: MediathekShow? = null
	private var downloadController: DownloadController? = null
	private var downloadStatus = DownloadStatus.NONE

	override fun onAttach(context: Context) {
		super.onAttach(context)

		val app = context.applicationContext as ZappApplicationBase

		downloadController = app.downloadController
		mediathekRepository = app.mediathekRepository
	}

	override fun onDetach() {
		super.onDetach()
		downloadController = null
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		argumentsMediathekShow = requireArguments().getSerializable(ARG_SHOW) as MediathekShow

		val persistShowDisposable = mediathekRepository!!
			.persistOrUpdateShow(argumentsMediathekShow!!)
			.firstElement()
			.observeOn(AndroidSchedulers.mainThread())
			.subscribe(::onShowLoaded, Timber::e)

		createDisposables.add(persistShowDisposable)
	}

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
		_binding = FragmentMediathekDetailBinding.inflate(inflater, container, false)

		binding.play.setOnClickListener(::onPlayClick)
		binding.buttons.download.setOnClickListener(::onDownloadClick)
		binding.buttons.share.setOnClickListener(::onShareClick)
		binding.buttons.website.setOnClickListener(::onWebsiteClick)

		val viewingProgressDisposable = mediathekRepository!!
			.getPlaybackPositionPercent(argumentsMediathekShow!!.apiId)
			.observeOn(AndroidSchedulers.mainThread())
			.subscribe(::updatePlaybackPosition, Timber::e)

		createViewDisposables.add(viewingProgressDisposable)

		return binding.root
	}

	override fun onResume() {
		super.onResume()

		downloadController!!.deleteDownloadsWithDeletedFiles()
	}

	override fun onDestroyView() {
		super.onDestroyView()

		createViewDisposables.clear()
	}

	override fun onDestroy() {
		super.onDestroy()

		createDisposables.clear()
	}

	override fun onConfirmDeleteDialogOkClicked() {
		downloadController!!.deleteDownload(persistedMediathekShow!!.id)
	}

	override fun onDownloadQualitySelected(quality: Quality) {
		download(quality)
	}

	override fun onShareQualitySelected(quality: Quality) {
		share(quality)
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

		downloadController!!
			.getDownloadStatus(show.apiId)
			.observeOn(AndroidSchedulers.mainThread())
			.subscribe(::onDownloadStatusChanged)
			.also(createViewDisposables::add)

		downloadController!!
			.getDownloadProgress(show.apiId)
			.observeOn(AndroidSchedulers.mainThread())
			.subscribe(::onDownloadProgressChanged)
			.also(createViewDisposables::add)
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

	private fun onPlayClick(view: View) {
		startActivity(MediathekPlayerActivity.getStartIntent(context, persistedMediathekShow!!.id))
	}

	private fun onDownloadClick(view: View) {
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
				downloadController!!.stopDownload(persistedMediathekShow!!.id)
			DownloadStatus.COMPLETED ->
				showConfirmDeleteDialog()
		}
	}

	private fun onShareClick(view: View) {
		showSelectQualityDialog(SelectQualityDialog.Mode.SHARE)
	}

	private fun onWebsiteClick(view: View) {
		openUrl(requireContext(), persistedMediathekShow!!.mediathekShow.websiteUrl)
	}

	private fun showConfirmDeleteDialog() {
		val dialog = ConfirmFileDeletionDialog()
		dialog.setTargetFragment(this, 0)
		dialog.show(parentFragmentManager, null)
	}

	private fun showSelectQualityDialog(mode: SelectQualityDialog.Mode) {
		val dialog = SelectQualityDialog.newInstance(persistedMediathekShow!!.mediathekShow, mode)
		dialog.setTargetFragment(this, 0)
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
		// reload show for up to date file path and then update thumbnail
		mediathekRepository!!
			.getPersistedShow(persistedMediathekShow!!.id)
			.firstOrError()
			.flatMap { loadThumbnailAsync(requireContext(), it.downloadedVideoPath) }
			.observeOn(AndroidSchedulers.mainThread())
			.subscribe({ thumbnail ->
				binding.texts.thumbnail.setImageBitmap(thumbnail)
				binding.texts.thumbnail.visibility = View.VISIBLE
			}, Timber::e)
			.also(createViewDisposables::add)
	}

	private fun share(quality: Quality) {
		val videoIntent = Intent(Intent.ACTION_VIEW).apply {
			val url = persistedMediathekShow!!.mediathekShow.getVideoUrl(quality)
			setDataAndType(Uri.parse(url), "video/*")
		}

		startActivity(Intent.createChooser(videoIntent, getString(R.string.action_share)))
	}

	private fun download(downloadQuality: Quality) {
		if (!startDownloadDisposable.isDisposed) {
			startDownloadDisposable.dispose()
		}

		startDownloadDisposable = downloadController!!
			.startDownload(persistedMediathekShow!!, downloadQuality)
			.observeOn(AndroidSchedulers.mainThread())
			.subscribe({}, ::onStartDownloadException)
			.also(createViewDisposables::add)
	}

	private fun onStartDownloadException(throwable: Throwable) {
		when (throwable) {
			is WrongNetworkConditionException -> {
				Snackbar
					.make(requireView(), R.string.error_mediathek_download_over_unmetered_network_only, Snackbar.LENGTH_LONG)
					.setAction(R.string.activity_settings_title) {
						startActivity(SettingsActivity.getStartIntent(requireContext()))
					}
					.show()
			}
			is NoNetworkException -> {
				Snackbar
					.make(requireView(), R.string.error_mediathek_download_no_network, Snackbar.LENGTH_LONG)
					.show()
			}
			else -> {
				Snackbar
					.make(requireView(), R.string.error_mediathek_generic_start_download_error, Snackbar.LENGTH_LONG)
					.show()
				Timber.e(throwable)
			}
		}
	}
}
