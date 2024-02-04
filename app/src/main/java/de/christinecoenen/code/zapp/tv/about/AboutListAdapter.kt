package de.christinecoenen.code.zapp.tv.about

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import de.christinecoenen.code.zapp.R
import de.christinecoenen.code.zapp.databinding.TvAboutItemBinding
import de.christinecoenen.code.zapp.tv.changelog.ChangelogActivity
import de.christinecoenen.code.zapp.tv.faq.FaqActivity
import de.christinecoenen.code.zapp.tv.settings.SettingsActivity

class AboutListAdapter(
	private val listener: AboutItemListener
) : RecyclerView.Adapter<AboutViewViewHolder>() {

	private val aboutItems = listOf(
		AboutItem(
			R.string.activity_settings_title,
			R.drawable.ic_outline_settings_24,
			SettingsActivity
		),
		AboutItem(
			R.string.changelog_title,
			R.drawable.ic_sharp_format_list_bulleted_24,
			ChangelogActivity
		),
		AboutItem(
			R.string.faq_title,
			R.drawable.ic_baseline_help_outline_24,
			FaqActivity
		),
	)

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AboutViewViewHolder {
		val layoutInflater = LayoutInflater.from(parent.context)
		val binding = TvAboutItemBinding.inflate(layoutInflater, parent, false)
		return AboutViewViewHolder(binding, listener)
	}

	override fun onBindViewHolder(holder: AboutViewViewHolder, position: Int) {
		holder.bind(aboutItems[position])
	}

	override fun getItemCount() = aboutItems.size
}
