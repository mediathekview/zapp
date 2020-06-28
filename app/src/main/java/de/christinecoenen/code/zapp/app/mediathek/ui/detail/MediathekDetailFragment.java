package de.christinecoenen.code.zapp.app.mediathek.ui.detail;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;

import de.christinecoenen.code.zapp.R;
import de.christinecoenen.code.zapp.app.ZappApplicationBase;
import de.christinecoenen.code.zapp.app.mediathek.controller.downloads.DownloadController;
import de.christinecoenen.code.zapp.app.mediathek.controller.downloads.exceptions.DownloadException;
import de.christinecoenen.code.zapp.app.mediathek.controller.downloads.exceptions.WrongNetworkConditionException;
import de.christinecoenen.code.zapp.app.mediathek.model.DownloadStatus;
import de.christinecoenen.code.zapp.app.mediathek.model.MediathekShow;
import de.christinecoenen.code.zapp.app.mediathek.model.PersistedMediathekShow;
import de.christinecoenen.code.zapp.app.mediathek.model.Quality;
import de.christinecoenen.code.zapp.app.mediathek.repository.MediathekRepository;
import de.christinecoenen.code.zapp.app.settings.ui.SettingsActivity;
import de.christinecoenen.code.zapp.databinding.FragmentMediathekDetailBinding;
import de.christinecoenen.code.zapp.utils.system.ImageHelper;
import de.christinecoenen.code.zapp.utils.system.IntentHelper;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import timber.log.Timber;


public class MediathekDetailFragment extends Fragment implements ConfirmFileDeletionDialog.Listener,
	SelectQualityDialog.Listener {

	private static final String ARG_SHOW = "ARG_SHOW";


	private CompositeDisposable createDisposables = new CompositeDisposable();
	private CompositeDisposable createViewDisposables = new CompositeDisposable();
	private FragmentMediathekDetailBinding binding;
	private MediathekRepository mediathekRepository;
	private PersistedMediathekShow persistedMediathekShow;
	private DownloadController downloadController;
	private DownloadStatus downloadStatus = DownloadStatus.NONE;


	public MediathekDetailFragment() {
		// Required empty public constructor
	}

	static MediathekDetailFragment getInstance(MediathekShow show) {
		MediathekDetailFragment fragment = new MediathekDetailFragment();
		Bundle args = new Bundle();
		args.putSerializable(ARG_SHOW, show);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onAttach(@NonNull Context context) {
		super.onAttach(context);
		ZappApplicationBase app = (ZappApplicationBase) context.getApplicationContext();
		downloadController = app.getDownloadController();
		mediathekRepository = app.getMediathekRepository();
	}

	@Override
	public void onDetach() {
		super.onDetach();
		downloadController = null;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getArguments() != null) {
			MediathekShow show = (MediathekShow) getArguments().getSerializable(ARG_SHOW);

			Disposable persistShowDisposable = mediathekRepository
				.persistOrUpdateShow(show)
				.firstElement()
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(this::onShowLoaded, Timber::e);

			createDisposables.add(persistShowDisposable);
		}
	}

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		binding = FragmentMediathekDetailBinding.inflate(inflater, container, false);

		binding.play.setOnClickListener(this::onPlayClick);
		binding.buttons.download.setOnClickListener(this::onDownloadClick);
		binding.buttons.share.setOnClickListener(this::onShareClick);
		binding.buttons.website.setOnClickListener(this::onWebsiteClick);

		return binding.getRoot();
	}

	@Override
	public void onResume() {
		super.onResume();

		downloadController.deleteDownloadsWithDeletedFiles();
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		createViewDisposables.clear();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		createDisposables.clear();
	}

	@Override
	public void onConfirmDeleteDialogOkClicked() {
		downloadController.deleteDownload(persistedMediathekShow.getMediathekShow().getApiId());
	}

	@Override
	public void onDownloadQualitySelected(Quality quality) {
		download(quality);
	}

	@Override
	public void onShareQualitySelected(Quality quality) {
		share(quality);
	}

	private void onShowLoaded(PersistedMediathekShow persistedMediathekShow) {
		this.persistedMediathekShow = persistedMediathekShow;

		MediathekShow show = persistedMediathekShow.getMediathekShow();

		binding.texts.topic.setText(show.getTopic());
		binding.texts.title.setText(show.getTitle());
		binding.texts.description.setText(show.getDescription());

		binding.time.setText(show.getFormattedTimestamp());
		binding.channel.setText(show.getChannel());
		binding.duration.setText(show.getFormattedDuration());
		binding.subtitle.setVisibility(show.hasSubtitle() ? View.VISIBLE : View.GONE);

		binding.buttons.download.setEnabled(show.hasAnyDownloadQuality());

		createViewDisposables.add(downloadController
			.getDownloadStatus(show.getApiId())
			.observeOn(AndroidSchedulers.mainThread())
			.subscribe(this::onDownloadStatusChanged));

		createViewDisposables.add(downloadController
			.getDownloadProgress(show.getApiId())
			.observeOn(AndroidSchedulers.mainThread())
			.subscribe(this::onDownloadProgressChanged));
	}

	private void onDownloadStatusChanged(DownloadStatus downloadStatus) {
		this.downloadStatus = downloadStatus;
		adjustUiToDownloadStatus(downloadStatus);
	}

	private void onDownloadProgressChanged(Integer progress) {
		binding.buttons.downloadProgress.setProgress(progress);
	}

	private void onPlayClick(View view) {
		startActivity(MediathekPlayerActivity.getStartIntent(getContext(), persistedMediathekShow.getId()));
	}

	private void onDownloadClick(View view) {
		switch (downloadStatus) {
			case NONE:
			case CANCELLED:
			case DELETED:
			case PAUSED:
			case REMOVED:
			case FAILED:
				showSelectQualityDialog(SelectQualityDialog.Mode.DOWNLOAD);
				break;
			case ADDED:
			case QUEUED:
			case DOWNLOADING:
				downloadController.stopDownload(persistedMediathekShow.getMediathekShow().getApiId());
				break;
			case COMPLETED:
				showConfirmDeleteDialog();
				break;
		}
	}

	private void onShareClick(View view) {
		showSelectQualityDialog(SelectQualityDialog.Mode.SHARE);
	}

	private void onWebsiteClick(View view) {
		IntentHelper.openUrl(requireContext(), persistedMediathekShow.getMediathekShow().getWebsiteUrl());
	}

	private void showConfirmDeleteDialog() {
		DialogFragment newFragment = new ConfirmFileDeletionDialog();
		newFragment.setTargetFragment(this, 0);
		newFragment.show(getParentFragmentManager(), null);
	}

	private void showSelectQualityDialog(SelectQualityDialog.Mode mode) {
		DialogFragment newFragment = SelectQualityDialog.newInstance(persistedMediathekShow.getMediathekShow(), mode);
		newFragment.setTargetFragment(this, 0);
		newFragment.show(getParentFragmentManager(), null);
	}

	private void adjustUiToDownloadStatus(DownloadStatus status) {
		binding.texts.thumbnail.setVisibility(View.GONE);

		switch (status) {
			case NONE:
			case CANCELLED:
			case DELETED:
			case PAUSED:
			case REMOVED:
				binding.buttons.downloadProgress.setVisibility(View.GONE);
				binding.buttons.download.setText(R.string.fragment_mediathek_download);
				binding.buttons.download.setIconResource(R.drawable.ic_file_download_white_24dp);
				break;
			case ADDED:
			case QUEUED:
				binding.buttons.downloadProgress.setVisibility(View.VISIBLE);
				binding.buttons.downloadProgress.setIndeterminate(true);
				binding.buttons.download.setText(R.string.fragment_mediathek_download_running);
				binding.buttons.download.setIconResource(R.drawable.ic_stop_white_24dp);
				break;
			case DOWNLOADING:
				binding.buttons.downloadProgress.setVisibility(View.VISIBLE);
				binding.buttons.downloadProgress.setIndeterminate(false);
				binding.buttons.download.setText(R.string.fragment_mediathek_download_running);
				binding.buttons.download.setIconResource(R.drawable.ic_stop_white_24dp);
				break;
			case COMPLETED:
				binding.buttons.downloadProgress.setVisibility(View.GONE);
				binding.buttons.download.setText(R.string.fragment_mediathek_download_delete);
				binding.buttons.download.setIconResource(R.drawable.ic_delete_white_24dp);

				Disposable loadThumbnailDisposable = ImageHelper.loadThumbnailAsync(getContext(), persistedMediathekShow.getDownloadedVideoPath())
					.observeOn(AndroidSchedulers.mainThread())
					.subscribe(thumbnail -> {
						binding.texts.thumbnail.setImageBitmap(thumbnail);
						binding.texts.thumbnail.setVisibility(View.VISIBLE);
					}, Timber::e);

				createViewDisposables.add(loadThumbnailDisposable);

				break;
			case FAILED:
				binding.buttons.downloadProgress.setVisibility(View.GONE);
				binding.buttons.download.setText(R.string.fragment_mediathek_download_retry);
				binding.buttons.download.setIconResource(R.drawable.ic_warning_white_24dp);
				break;
		}
	}

	private void share(Quality quality) {
		Intent videoIntent = new Intent(Intent.ACTION_VIEW);
		String url = persistedMediathekShow.getMediathekShow().getVideoUrl(quality);
		videoIntent.setDataAndType(Uri.parse(url), "video/*");
		startActivity(Intent.createChooser(videoIntent, getString(R.string.action_share)));
	}

	private void download(Quality downloadQuality) {
		try {
			downloadController.startDownload(persistedMediathekShow, downloadQuality);
		} catch (WrongNetworkConditionException e) {
			Snackbar snackbar = Snackbar
				.make(requireView(), R.string.error_mediathek_download_over_wifi_only, Snackbar.LENGTH_LONG);
			snackbar.setAction(R.string.activity_settings_title, v -> startActivity(SettingsActivity.getStartIntent(getContext())));
			snackbar.show();
		} catch (DownloadException e) {
			Toast.makeText(getContext(), R.string.error_mediathek_no_download_manager, Toast.LENGTH_LONG).show();
		}
	}
}
