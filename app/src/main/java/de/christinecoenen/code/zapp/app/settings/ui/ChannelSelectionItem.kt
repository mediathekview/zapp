package de.christinecoenen.code.zapp.app.settings.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import de.christinecoenen.code.zapp.R
import de.christinecoenen.code.zapp.models.channels.ChannelModel
import sh.calvin.reorderable.ReorderableCollectionItemScope

@Composable
fun ChannelSelectionItem(
	channel: ChannelModel,
	isDragging: Boolean = false,
	scope: ReorderableCollectionItemScope
) {
	val hapticFeedback = LocalHapticFeedback.current

	val enabled = remember { mutableStateOf(channel.isEnabled) }
	val hasSubtitle = !channel.subtitle.isNullOrBlank()

	val bgColor by animateColorAsState(
		if (isDragging) {
			MaterialTheme.colorScheme.surfaceContainerHigh
		} else {
			MaterialTheme.colorScheme.surfaceContainerLow
		},
	)

	Surface(
		color = bgColor,
		modifier = Modifier
            .aspectRatio(5 / 3f)
            .alpha(if (enabled.value) 1f else 0.3f)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(),
                role = Role.Checkbox
            ) {
                enabled.value = !enabled.value
                channel.isEnabled = enabled.value
            }
	) {
		Row {
			// handle
			Box(
				modifier = with(scope) {
					Modifier
                        .draggableHandle(
                            onDragStarted = {
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.GestureThresholdActivate)
                            },
                            onDragStopped = {
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.GestureEnd)
                            },
                        )
                        .fillMaxHeight()
                        .padding(2.dp)
                        .width(24.dp)
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest)
				},
			) {
				Icon(
					painterResource(R.drawable.ic_handle_white_24dp),
					contentDescription = "Reorder",
					modifier = Modifier
                        .fillMaxSize()
                        .padding(4.dp)
				)
			}

			// channel info
			Column(
				verticalArrangement = Arrangement.Center,
				horizontalAlignment = Alignment.CenterHorizontally,
				modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 16.dp)
			) {
				// logo
				Image(
					painter = painterResource(channel.drawableId),
					contentDescription = channel.name,
					modifier = Modifier
                        .weight(0.7f)
                        .padding(horizontal = 16.dp)
				)

				// subtitle
				if (hasSubtitle) {
					Spacer(Modifier.height(2.dp))

					Text(
						text = channel.subtitle!!,
						style = MaterialTheme.typography.labelSmall,
						overflow = TextOverflow.Ellipsis,
						modifier = Modifier
                            .weight(0.3f)
                            .padding(horizontal = 2.dp)
					)
				}
			}
		}
	}
}
