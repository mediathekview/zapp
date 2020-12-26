package de.christinecoenen.code.zapp.app.about.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import de.christinecoenen.code.zapp.R
import de.christinecoenen.code.zapp.databinding.ActivityChangelogBinding
import org.apache.commons.io.IOUtils
import ru.noties.markwon.Markwon
import timber.log.Timber
import java.io.IOException
import java.nio.charset.StandardCharsets

class ChangelogActivity : AppCompatActivity() {

	companion object {
		fun getStartIntent(context: Context?): Intent = Intent(context, ChangelogActivity::class.java)
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		val binding = ActivityChangelogBinding.inflate(layoutInflater)

		setContentView(binding.root)

		try {
			resources.openRawResource(R.raw.changelog).use { inputStream ->
				val markdown = IOUtils.toString(inputStream, StandardCharsets.UTF_8)
				Markwon.setMarkdown(binding.txtChangelog, markdown)
			}
		} catch (e: IOException) {
			Timber.e(e)
		}
	}
}
