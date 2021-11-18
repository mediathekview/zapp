package de.christinecoenen.code.zapp.tv.changelog

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import de.christinecoenen.code.zapp.R
import de.christinecoenen.code.zapp.databinding.TvActivityChangelogBinding
import org.apache.commons.io.IOUtils
import ru.noties.markwon.Markwon
import timber.log.Timber
import java.io.IOException
import java.nio.charset.StandardCharsets

class ChangelogActivity : Activity() {

	companion object {
		fun getStartIntent(context: Context?): Intent =
			Intent(context, ChangelogActivity::class.java)
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		val binding = TvActivityChangelogBinding.inflate(layoutInflater)

		setContentView(binding.root)

		try {
			resources.openRawResource(R.raw.changelog).use { inputStream ->
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
