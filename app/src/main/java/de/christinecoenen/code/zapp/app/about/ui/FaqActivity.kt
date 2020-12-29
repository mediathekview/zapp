package de.christinecoenen.code.zapp.app.about.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import de.christinecoenen.code.zapp.R
import de.christinecoenen.code.zapp.databinding.ActivityFaqBinding
import org.apache.commons.io.IOUtils
import ru.noties.markwon.Markwon
import timber.log.Timber
import java.io.IOException
import java.nio.charset.StandardCharsets

class FaqActivity : AppCompatActivity() {

	companion object {
		fun getStartIntent(context: Context?): Intent = Intent(context, FaqActivity::class.java)
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		val binding = ActivityFaqBinding.inflate(layoutInflater)

		setContentView(binding.root)

		try {
			resources.openRawResource(R.raw.faq).use { inputStream ->
				val markdown = IOUtils.toString(inputStream, StandardCharsets.UTF_8)
				Markwon.setMarkdown(binding.txtFaq, markdown)
			}
		} catch (e: IOException) {
			Timber.e(e)
		}
	}
}
