package de.christinecoenen.code.zapp.utils.system;

import android.view.Menu;
import android.view.MenuItem;

public class MenuHelper {

	public static void uncheckItems(Menu menu) {
		if (menu == null) {
			return;
		}

		for (int i = 0; i < menu.size(); i++) {
			MenuItem item = menu.getItem(i);
			item.setChecked(false);
			uncheckItems(item.getSubMenu());
		}
	}

}
