package de.christinecoenen.code.zapp.app.mediathek.ui.detail

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import de.christinecoenen.code.zapp.R
import de.christinecoenen.code.zapp.models.shows.MediathekShow

class MediathekDetailActivity : AppCompatActivity() {

	companion object {

		private const val EXTRA_SHOW = "de.christinecoenen.code.zapp.EXTRA_SHOW"

		@JvmStatic
		fun getStartIntent(context: Context?, show: MediathekShow?): Intent {
			return Intent(context, MediathekDetailActivity::class.java).apply {
				action = Intent.ACTION_VIEW
				putExtra(EXTRA_SHOW, show)
			}
		}

	}

	private lateinit var show: MediathekShow

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		setContentView(R.layout.activity_mediathek_detail)
		supportActionBar?.setDisplayHomeAsUpEnabled(true)

		show = intent.extras!!.getSerializable(EXTRA_SHOW) as MediathekShow

		if (savedInstanceState == null) {
			supportFragmentManager
				.beginTransaction()
				.add(R.id.container, MediathekDetailFragment.getInstance(show), "MediathekDetailFragment")
				.commit()
		}
	}

	override fun onCreateOptionsMenu(menu: Menu): Boolean {
		menuInflater.inflate(R.menu.activity_mediathek_detail, menu)
		return true
	}

	override fun onOptionsItemSelected(item: MenuItem): Boolean {
		return when (item.itemId) {
			R.id.menu_share -> {
				startActivity(Intent.createChooser(show.shareIntentPlain, getString(R.string.action_share)))
				true
			}
			android.R.id.home -> {
				finish()
				true
			}
			else -> super.onOptionsItemSelected(item)
		}
	}
}
