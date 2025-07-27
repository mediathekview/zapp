package de.christinecoenen.code.zapp.tv2.changelog

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.ui.res.stringResource
import de.christinecoenen.code.zapp.R
import de.christinecoenen.code.zapp.tv2.about.MarkdownScreen
import de.christinecoenen.code.zapp.utils.system.IStartableActivity

class ChangelogActivity : ComponentActivity() {

	companion object : IStartableActivity {
		override fun getStartIntent(context: Context?): Intent =
			Intent(context, ChangelogActivity::class.java)
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		setContent {
			MarkdownScreen(
				title = stringResource(R.string.changelog_title),
				markdownResId = R.raw.changelog,
			)
		}
	}
}
