package de.christinecoenen.code.zapp.app.mediathek.ui.list.adapter

import android.view.View
import de.christinecoenen.code.zapp.models.shows.MediathekShow

interface MediathekShowListItemListener {

	fun onShowClicked(show: MediathekShow)

	fun onShowLongClicked(show: MediathekShow, view: View) {}

	fun onShowFocusChanged(show: MediathekShow, focused: Boolean) {}
}
