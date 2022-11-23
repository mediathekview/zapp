package de.christinecoenen.code.zapp.tv.faq

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import de.christinecoenen.code.zapp.R
import de.christinecoenen.code.zapp.databinding.TvActivityFaqBinding
import de.christinecoenen.code.zapp.repositories.MediathekRepository
import de.christinecoenen.code.zapp.utils.io.IoUtils.readAllText
import de.christinecoenen.code.zapp.utils.system.IStartableActivity
import io.noties.markwon.Markwon
import org.koin.android.ext.android.inject

class FaqActivity : Activity() {

	companion object : IStartableActivity {
		override fun getStartIntent(context: Context?): Intent =
			Intent(context, FaqActivity::class.java)
	}

	private val markwon: Markwon by inject()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		val binding = TvActivityFaqBinding.inflate(layoutInflater)

		setContentView(binding.root)

		val markdown = resources.readAllText(R.raw.faq)
		markwon.setMarkdown(binding.txtFaq, markdown)
	}
}
