package de.christinecoenen.code.zapp.tv2.live

import android.content.Context
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.ui.compose.PlayerSurface
import androidx.media3.ui.compose.SURFACE_TYPE_TEXTURE_VIEW
import de.christinecoenen.code.zapp.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

val fadeDuration = 750.milliseconds
val changeDelay = 10.seconds

fun initializeExoPlayer(context: Context): ExoPlayer {
	return ExoPlayer.Builder(context)
		.setTrackSelector(
			DefaultTrackSelector(context)
				.apply {
					setParameters(
						parameters
							.buildUpon()
							.setRendererDisabled(C.TRACK_TYPE_VIDEO, true)
							.setRendererDisabled(C.TRACK_TYPE_TEXT, true)
							.setRendererDisabled(C.TRACK_TYPE_AUDIO, true)
							.build()
					)
				}
		)
		.build()
}

@Composable
fun StreamPreviewImage(
	modifier: Modifier = Modifier,
	streamUrl: String
) {
	val context = LocalContext.current
	val coroutineScope = rememberCoroutineScope()

	var isVisible by remember { mutableStateOf(false) }

	val alpha by animateFloatAsState(
		targetValue = if (isVisible) 1f else 0f,
		animationSpec = tween(
			durationMillis = fadeDuration.inWholeMilliseconds.toInt(),
			easing = FastOutSlowInEasing,
		)
	)

	val player = remember {
		initializeExoPlayer(context).apply {
			addListener(object : Player.Listener {
				override fun onRenderedFirstFrame() {
					isVisible = true
				}
			})
		}
	}

	LifecycleResumeEffect(streamUrl) {
		val job = coroutineScope.launch {
			while (isActive) {
				isVisible = false
				delay(fadeDuration)

				player.apply {
					stop()
					removeMediaItem(0)
					setMediaItem(MediaItem.fromUri(streamUrl))
					prepare()
					Timber.d("reload video preview")
				}

				delay(changeDelay)
			}
		}

		onPauseOrDispose { job.cancel() }
	}

	DisposableEffect(Unit) {
		onDispose {
			player.release()
		}
	}

	Box(
		modifier = modifier.aspectRatio(16 / 9f),
	) {
		PlayerSurface(
			player = player,
			surfaceType = SURFACE_TYPE_TEXTURE_VIEW,
			modifier = Modifier.alpha(alpha),
		)
		Image(
			painter = painterResource(R.drawable.scrim),
			contentScale = ContentScale.FillBounds,
			contentDescription = null,
			modifier = Modifier.fillMaxSize()
		)
	}
}
