package de.christinecoenen.code.zapp.tv.changelog

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import de.christinecoenen.code.zapp.R
import de.christinecoenen.code.zapp.databinding.TvActivityChangelogBinding
import de.christinecoenen.code.zapp.utils.io.IoUtils.readAllText
import de.christinecoenen.code.zapp.utils.system.IStartableActivity
import ru.noties.markwon.Markwon

class ChangelogActivity : Activity() {

	companion object : IStartableActivity {
		override fun getStartIntent(context: Context?): Intent =
			Intent(context, ChangelogActivity::class.java)
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		val binding = TvActivityChangelogBinding.inflate(layoutInflater)

		setContentView(binding.root)

		val markdown = resources.readAllText(R.raw.changelog)
		Markwon.setMarkdown(binding.txtChangelog, markdown)
	}
}
