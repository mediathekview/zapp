package de.christinecoenen.code.zapp.tv2.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import de.christinecoenen.code.zapp.tv2.about.AboutScreen
import de.christinecoenen.code.zapp.tv2.live.LiveScreen
import de.christinecoenen.code.zapp.tv2.about.MediaCenterScreen
import de.christinecoenen.code.zapp.tv2.theme.AppTheme
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {

	private val topNavigationViewModel: TopNavigationViewModel by viewModel()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		setContent {
			AppTheme {
				val selectedTabIndex by topNavigationViewModel.selectedTab

				Column(
					modifier = Modifier
                        .fillMaxSize()
                        .fillMaxWidth()
				) {

					TopNavigation(
						modifier = Modifier.align(Alignment.CenterHorizontally)
					)

					Box(
						modifier = Modifier.fillMaxSize()
					) {
						when (selectedTabIndex) {
							0 -> LiveScreen()
							1 -> MediaCenterScreen()
							2 -> AboutScreen()
						}
					}
				}
			}
		}
	}

}
