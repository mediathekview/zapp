package de.christinecoenen.code.zapp.utils.system

import android.content.Context
import android.content.Intent

interface IStartableActivity {

	fun getStartIntent(context: Context?): Intent

}
