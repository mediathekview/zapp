package de.christinecoenen.code.zapp.app.about.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.window.core.layout.WindowSizeClass
import androidx.window.core.layout.WindowWidthSizeClass
import com.mikepenz.aboutlibraries.ui.compose.android.rememberLibraries
import com.mikepenz.aboutlibraries.ui.compose.m3.LibrariesContainer
import de.christinecoenen.code.zapp.BuildConfig
import de.christinecoenen.code.zapp.R
import de.christinecoenen.code.zapp.theme.AppTheme
import de.christinecoenen.code.zapp.theme.ThemePreviews
import de.christinecoenen.code.zapp.utils.system.IntentHelper

class AboutFragment : Fragment() {

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		return ComposeView(requireContext())
			.apply {
				setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
				setContent { MainScreen() }
			}
	}

	@Preview(widthDp = 400, heightDp = 800, showBackground = true)
	@Preview(widthDp = 800, heightDp = 400, showBackground = true)
	@ThemePreviews
	@Composable
	fun MainScreen() {
		AppTheme {
			val windowSizeClass: WindowSizeClass =
				currentWindowAdaptiveInfo().windowSizeClass
			val stacked =
				windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.COMPACT

			if (stacked) {
				Column(horizontalAlignment = Alignment.CenterHorizontally) {
					Header()
					Spacer(Modifier.height(16.dp))
					HorizontalDivider()
					LibrariesList()
				}
			} else {
				Row(verticalAlignment = Alignment.CenterVertically) {
					Header()
					LibrariesList()
				}
			}
		}
	}

	@OptIn(ExperimentalLayoutApi::class)
	@ThemePreviews
	@Composable
	fun LibrariesList() {
		val libraries by rememberLibraries(R.raw.aboutlibraries)

		LibrariesContainer(
			libraries = libraries,
			onLibraryClick = { library ->
				library.website?.let {
					IntentHelper.openUrl(requireContext(), it)
				}
			},
			modifier = Modifier.fillMaxHeight()
		)
	}

	@ThemePreviews
	@Composable
	fun Header() {
		AppTheme {
			Column(
				horizontalAlignment = Alignment.CenterHorizontally,
				modifier = Modifier
                    .widthIn(0.dp, 400.dp)
                    .padding(horizontal = 16.dp)
			) {
				// icon with app info
				Row(
					verticalAlignment = Alignment.CenterVertically,
				) {
					// icon
					Surface(
						color = MaterialTheme.colorScheme.tertiaryContainer,
						shape = CircleShape,
						shadowElevation = 4.dp,
						modifier = Modifier
                            .width(74.dp)
                            .aspectRatio(1f)
					) {
						// icon image
						Icon(
							painter = painterResource(R.drawable.ic_zapp_tv),
							contentDescription = null,
							tint = MaterialTheme.colorScheme.onTertiaryContainer,
							modifier = Modifier.padding(4.dp)
						)
					}
					Spacer(Modifier.width(16.dp))

					// app info
					Column {
						// app name
						Text(
							text = stringResource(R.string.app_name),
							style = MaterialTheme.typography.headlineSmall,
							color = MaterialTheme.colorScheme.onSurface,
						)

						// version info
						Text(
							text = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
							style = MaterialTheme.typography.bodyLarge,
							color = MaterialTheme.colorScheme.onSurface,
						)
					}
				}
				Spacer(Modifier.height(16.dp))

				// description
				Text(
					text = stringResource(R.string.about_summary),
					style = MaterialTheme.typography.bodyMedium,
					color = MaterialTheme.colorScheme.onSurface,
					textAlign = TextAlign.Center,
				)
				Spacer(Modifier.height(16.dp))

				// buttons
				HeaderButtonBar()
			}
		}
	}

	@ThemePreviews
	@Composable
	fun HeaderButtonBar() {
		AppTheme {
			FlowRow(
				horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
			) {
				Button({ openChangelog() }) {
					Text(stringResource(R.string.changelog_title))
				}
				Button({ openFaq() }) {
					Text(stringResource(R.string.faq_title))
				}
				Button({ sendFeedbackMail() }) {
					Text(stringResource(R.string.menu_feedback))
				}
			}
		}
	}

	private fun openChangelog() {
		val action = AboutFragmentDirections.actionAboutFragmentToChangelogFragment()
		findNavController().navigate(action)
	}

	private fun openFaq() {
		val action = AboutFragmentDirections.toFaqFragment()
		findNavController().navigate(action)
	}

	private fun sendFeedbackMail() {
		IntentHelper.sendMail(
			requireContext(),
			getString(R.string.support_mail),
			getString(R.string.about_feedback_mail_subject)
		)
	}
}
