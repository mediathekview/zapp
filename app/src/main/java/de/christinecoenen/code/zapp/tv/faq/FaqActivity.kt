package de.christinecoenen.code.zapp.tv.faq

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import de.christinecoenen.code.zapp.R
import de.christinecoenen.code.zapp.databinding.TvActivityFaqBinding
import de.christinecoenen.code.zapp.utils.system.IStartableActivity
import org.apache.commons.io.IOUtils
import ru.noties.markwon.Markwon
import timber.log.Timber
import java.io.IOException
import java.nio.charset.StandardCharsets

class FaqActivity : Activity() {

	companion object: IStartableActivity {
		override fun getStartIntent(context: Context?): Intent =
			Intent(context, FaqActivity::class.java)
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		val binding = TvActivityFaqBinding.inflate(layoutInflater)

		setContentView(binding.root)

		try {
			resources.openRawResource(R.raw.faq).use { inputStream ->
				val markdown = IOUtils.toString(inputStream, StandardCharsets.UTF_8)
				Markwon.setText(
					binding.txtChangelog,
					Markwon.markdown(this, markdown),
					null
				)
			}
		} catch (e: IOException) {
			Timber.e(e)
		}
	}
}
