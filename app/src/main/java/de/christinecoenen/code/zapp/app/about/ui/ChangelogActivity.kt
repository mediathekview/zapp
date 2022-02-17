package de.christinecoenen.code.zapp.app.about.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import de.christinecoenen.code.zapp.R
import de.christinecoenen.code.zapp.databinding.ActivityChangelogBinding
import de.christinecoenen.code.zapp.utils.io.IoUtils.readAllText
import ru.noties.markwon.Markwon

class ChangelogActivity : AppCompatActivity() {

	companion object {
		fun getStartIntent(context: Context?): Intent =
			Intent(context, ChangelogActivity::class.java)
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		val binding = ActivityChangelogBinding.inflate(layoutInflater)

		setContentView(binding.root)

		val markdown = resources.readAllText(R.raw.changelog)
		Markwon.setMarkdown(binding.txtChangelog, markdown)
	}
}
