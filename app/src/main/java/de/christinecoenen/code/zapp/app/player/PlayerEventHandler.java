package de.christinecoenen.code.zapp.app.player;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.RendererCapabilities;
import com.google.android.exoplayer2.analytics.AnalyticsListener;
import com.google.android.exoplayer2.metadata.Metadata;
import com.google.android.exoplayer2.source.MediaSourceEventListener;
import com.google.android.exoplayer2.source.TrackGroup;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.MappingTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.upstream.HttpDataSource;

import java.io.IOException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import de.christinecoenen.code.zapp.R;
import io.reactivex.subjects.BehaviorSubject;
import timber.log.Timber;

/**
 * Transforms player events from exo player into RXJava observables.
 */
class PlayerEventHandler implements AnalyticsListener {

	private final BehaviorSubject<Boolean> isBufferingSource = BehaviorSubject.create();
	private final BehaviorSubject<Boolean> isIdleSource = BehaviorSubject.create();
	private final BehaviorSubject<Integer> errorResourceIdSource = BehaviorSubject.create();
	private final BehaviorSubject<Boolean> shouldHoldWakelockSource = BehaviorSubject.create();
	private final DefaultTrackSelector trackSelector;
	private static final String TAG = "PlayerEventHandler";

	PlayerEventHandler(@NonNull DefaultTrackSelector trackSelector) {
		this.trackSelector = trackSelector;
	}

	BehaviorSubject<Boolean> isBuffering() {
		return isBufferingSource;
	}

	BehaviorSubject<Boolean> isIdle() {
		return isIdleSource;
	}

	BehaviorSubject<Integer> getErrorResourceId() {
		return errorResourceIdSource;
	}

	/**
	 * @return emits true when the player is playing or buffering and false
	 * if it is idle, paused or stopped
	 */
	BehaviorSubject<Boolean> getShouldHoldWakelock() {
		return shouldHoldWakelockSource;
	}

	@Override
	public void onPlayerStateChanged(EventTime eventTime, boolean playWhenReady, int playbackState) {
		boolean isBuffering = playbackState == Player.STATE_BUFFERING;
		isBufferingSource.onNext(isBuffering);

		boolean isReady = playbackState == Player.STATE_IDLE;
		isIdleSource.onNext(isReady);

		boolean shouldHoldWakelock = playWhenReady &&
			(playbackState == Player.STATE_BUFFERING || playbackState == Player.STATE_READY);
		shouldHoldWakelockSource.onNext(shouldHoldWakelock);
	}

	@Override
	public void onTracksChanged(
		EventTime eventTime, TrackGroupArray ignored, TrackSelectionArray trackSelections) {
		MappingTrackSelector.MappedTrackInfo mappedTrackInfo =
			trackSelector.getCurrentMappedTrackInfo();
		if (mappedTrackInfo == null) {
			Timber.tag(TAG).d("Tracks []");
			return;
		}
		Timber.tag(TAG).d("Tracks [");
		// Log tracks associated to renderers.
		int rendererCount = mappedTrackInfo.getRendererCount();
		for (int rendererIndex = 0; rendererIndex < rendererCount; rendererIndex++) {
			TrackGroupArray rendererTrackGroups = mappedTrackInfo.getTrackGroups(rendererIndex);
			TrackSelection trackSelection = trackSelections.get(rendererIndex);
			if (rendererTrackGroups.length > 0) {
				Timber.tag(TAG).d("  Renderer:" + rendererIndex + " [");
				for (int groupIndex = 0; groupIndex < rendererTrackGroups.length; groupIndex++) {
					TrackGroup trackGroup = rendererTrackGroups.get(groupIndex);
					String adaptiveSupport =
						getAdaptiveSupportString(
							trackGroup.length,
							mappedTrackInfo.getAdaptiveSupport(rendererIndex, groupIndex, false));
					Timber.tag(TAG).d("    Group:" + groupIndex + ", adaptive_supported=" + adaptiveSupport + " [");
					for (int trackIndex = 0; trackIndex < trackGroup.length; trackIndex++) {
						String status = getTrackStatusString(trackSelection, trackGroup, trackIndex);
						String formatSupport =
							getFormatSupportString(
								mappedTrackInfo.getTrackSupport(rendererIndex, groupIndex, trackIndex));
						Timber.tag(TAG).d(
							"      "
								+ status
								+ " Track:"
								+ trackIndex
								+ ", "
								+ Format.toLogString(trackGroup.getFormat(trackIndex))
								+ ", supported="
								+ formatSupport);
					}
					Timber.tag(TAG).d("    ]");
				}
				// Log metadata for at most one of the tracks selected for the renderer.
				if (trackSelection != null) {
					for (int selectionIndex = 0; selectionIndex < trackSelection.length(); selectionIndex++) {
						Metadata metadata = trackSelection.getFormat(selectionIndex).metadata;
						if (metadata != null) {
							Timber.tag(TAG).d("    Metadata [");
							printMetadata(metadata);
							Timber.tag(TAG).d("    ]");
							break;
						}
					}
				}
				Timber.tag(TAG).d("  ]");
			}
		}
		// Log tracks not associated with a renderer.
		TrackGroupArray unassociatedTrackGroups = mappedTrackInfo.getUnmappedTrackGroups();
		if (unassociatedTrackGroups.length > 0) {
			Timber.tag(TAG).d("  Renderer:None [");
			for (int groupIndex = 0; groupIndex < unassociatedTrackGroups.length; groupIndex++) {
				Timber.tag(TAG).d("    Group:" + groupIndex + " [");
				TrackGroup trackGroup = unassociatedTrackGroups.get(groupIndex);
				for (int trackIndex = 0; trackIndex < trackGroup.length; trackIndex++) {
					String status = getTrackStatusString(false);
					String formatSupport =
						getFormatSupportString(RendererCapabilities.FORMAT_UNSUPPORTED_TYPE);
					Timber.tag(TAG).d(
						"      "
							+ status
							+ " Track:"
							+ trackIndex
							+ ", "
							+ Format.toLogString(trackGroup.getFormat(trackIndex))
							+ ", supported="
							+ formatSupport);
				}
				Timber.tag(TAG).d("    ]");
			}
			Timber.tag(TAG).d("  ]");
		}
		Timber.tag(TAG).d("]");
	}

	private static String getAdaptiveSupportString(int trackCount, int adaptiveSupport) {
		if (trackCount < 2) {
			return "N/A";
		}
		switch (adaptiveSupport) {
			case RendererCapabilities.ADAPTIVE_SEAMLESS:
				return "YES";
			case RendererCapabilities.ADAPTIVE_NOT_SEAMLESS:
				return "YES_NOT_SEAMLESS";
			case RendererCapabilities.ADAPTIVE_NOT_SUPPORTED:
				return "NO";
			default:
				return "?";
		}
	}

	// Suppressing reference equality warning because the track group stored in the track selection
	// must point to the exact track group object to be considered part of it.
	@SuppressWarnings("ReferenceEquality")
	private static String getTrackStatusString(
		@Nullable TrackSelection selection, TrackGroup group, int trackIndex) {
		return getTrackStatusString(selection != null && selection.getTrackGroup() == group
			&& selection.indexOf(trackIndex) != C.INDEX_UNSET);
	}

	private static String getFormatSupportString(int formatSupport) {
		switch (formatSupport) {
			case RendererCapabilities.FORMAT_HANDLED:
				return "YES";
			case RendererCapabilities.FORMAT_EXCEEDS_CAPABILITIES:
				return "NO_EXCEEDS_CAPABILITIES";
			case RendererCapabilities.FORMAT_UNSUPPORTED_DRM:
				return "NO_UNSUPPORTED_DRM";
			case RendererCapabilities.FORMAT_UNSUPPORTED_SUBTYPE:
				return "NO_UNSUPPORTED_TYPE";
			case RendererCapabilities.FORMAT_UNSUPPORTED_TYPE:
				return "NO";
			default:
				return "?";
		}
	}

	private static String getTrackStatusString(boolean enabled) {
		return enabled ? "[X]" : "[ ]";
	}

	private void printMetadata(Metadata metadata) {
		for (int i = 0; i < metadata.length(); i++) {
			Timber.tag(TAG).d("%s%s", "      ", metadata.get(i));
		}
	}

	@Override
	public void onPlayerError(EventTime eventTime, ExoPlaybackException error) {
		int errorMessageResourceId = R.string.error_stream_unknown;

		switch (error.type) {
			case ExoPlaybackException.TYPE_SOURCE:
				Timber.e(error, "exo player error TYPE_SOURCE");
				errorMessageResourceId = R.string.error_stream_io;
				break;
			case ExoPlaybackException.TYPE_RENDERER:
				Timber.e(error, "exo player error TYPE_RENDERER");
				errorMessageResourceId = R.string.error_stream_unsupported;
				break;
			case ExoPlaybackException.TYPE_UNEXPECTED:
				Timber.e(error, "exo player error TYPE_UNEXPECTED");
				break;
		}

		errorResourceIdSource.onNext(errorMessageResourceId);
	}

	@Override
	public void onLoadError(EventTime eventTime, MediaSourceEventListener.LoadEventInfo loadEventInfo, MediaSourceEventListener.MediaLoadData mediaLoadData, IOException error, boolean wasCanceled) {
		if (wasCanceled) {
			return;
		}

		Timber.e(error, "exo player onLoadError");

		if (error instanceof HttpDataSource.HttpDataSourceException) {
			errorResourceIdSource.onNext(R.string.error_stream_io);
		} else {
			errorResourceIdSource.onNext(R.string.error_stream_unknown);
		}
	}
}
