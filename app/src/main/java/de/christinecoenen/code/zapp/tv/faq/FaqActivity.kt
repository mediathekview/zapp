package de.christinecoenen.code.zapp.tv.faq

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import de.christinecoenen.code.zapp.R
import de.christinecoenen.code.zapp.databinding.TvActivityFaqBinding
import de.christinecoenen.code.zapp.utils.io.IoUtils.readAllText
import de.christinecoenen.code.zapp.utils.system.IStartableActivity
import ru.noties.markwon.Markwon

class FaqActivity : Activity() {

	companion object : IStartableActivity {
		override fun getStartIntent(context: Context?): Intent =
			Intent(context, FaqActivity::class.java)
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		val binding = TvActivityFaqBinding.inflate(layoutInflater)

		setContentView(binding.root)

		val markdown = resources.readAllText(R.raw.faq)
		Markwon.setMarkdown(binding.txtFaq, markdown)
	}
}
