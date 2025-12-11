package de.christinecoenen.code.zapp.tv2.about

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.mikepenz.markdown.compose.LazyMarkdownSuccess
import com.mikepenz.markdown.compose.components.MarkdownComponent
import com.mikepenz.markdown.compose.components.markdownComponents
import com.mikepenz.markdown.compose.elements.MarkdownHeader
import com.mikepenz.markdown.m3.Markdown
import com.mikepenz.markdown.m3.markdownColor
import com.mikepenz.markdown.model.rememberMarkdownState
import de.christinecoenen.code.zapp.R
import de.christinecoenen.code.zapp.tv2.theme.AppTheme
import de.christinecoenen.code.zapp.tv2.theme.TvScreenPreview
import de.christinecoenen.code.zapp.utils.io.IoUtils.readAllText

@TvScreenPreview
@Composable
fun MarkdownScreen(
	title: String = "Changelog",
	markdownResId: Int = R.raw.changelog,
) {
	val resources = LocalResources.current
	val markdown = remember { resources.readAllText(markdownResId) }
	val markdownState = rememberMarkdownState(markdown)

	val headlineUnderlineStyle = MaterialTheme.typography.headlineMedium
		.merge(textDecoration = TextDecoration.Underline)

	val focusableHeadline: MarkdownComponent = {
		val interactionSource = remember { MutableInteractionSource() }
		val isFocused by interactionSource.collectIsFocusedAsState()

		Box(
			modifier = Modifier
				.focusable(interactionSource = interactionSource)
				.padding(top = 16.dp, bottom = 8.dp)
		) {
			MarkdownHeader(
				it.content,
				it.node,
				if (isFocused) headlineUnderlineStyle else MaterialTheme.typography.headlineMedium
			)
		}
	}

	AppTheme {
		Column(
			modifier = Modifier
				.background(MaterialTheme.colorScheme.surface)
				.fillMaxSize()
				.padding(horizontal = 130.dp)
		) {
			Spacer(Modifier.height(32.dp))

			Text(
				text = title,
				style = MaterialTheme.typography.headlineLarge,
				color = MaterialTheme.colorScheme.onSurface,
			)

			Spacer(Modifier.height(16.dp))

			Markdown(
				markdownState,
				success = { state, components, modifier ->
					LazyMarkdownSuccess(
						state,
						components,
						modifier,
						contentPadding = PaddingValues(bottom = 24.dp)
					)
				},
				colors = markdownColor(
					text = MaterialTheme.colorScheme.onSurface
				),
				components = markdownComponents(
					heading2 = focusableHeadline,
				)
			)
		}
	}
}
