package de.christinecoenen.code.zapp.tv2.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import de.christinecoenen.code.zapp.tv2.theme.AppTheme

class MainActivity : ComponentActivity() {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		setContent {
			AppTheme {
				Column(
					modifier = Modifier
                        .fillMaxSize()
                        .fillMaxWidth()
				) {

					TopNavigation(
						modifier = Modifier.align(Alignment.CenterHorizontally)
					)

					// TODO: change when top navigation changed
					Box(
						modifier = Modifier.fillMaxSize()
					) {
						Text(
							text = "Hello World!",
							color = MaterialTheme.colorScheme.onSurface,
							modifier = Modifier.fillMaxSize()
						)
					}
				}
			}
		}
	}

}
