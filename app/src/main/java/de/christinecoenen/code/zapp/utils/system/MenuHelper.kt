package de.christinecoenen.code.zapp.utils.system

import android.view.Menu
import androidx.core.view.forEach

object MenuHelper {

	fun uncheckItems(menu: Menu) {
		menu.forEach { item ->
			item.isChecked = false
			uncheckItems(item.subMenu)
		}
	}
}
