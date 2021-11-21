package de.christinecoenen.code.zapp.utils.io

import android.content.res.Resources
import androidx.annotation.RawRes
import java.io.BufferedReader
import java.io.InputStream
import java.io.OutputStream

object IoUtils {

	fun Resources.readAllText(@RawRes resourceId: Int): String {
		this.openRawResource(resourceId).use { inputStream ->
			return inputStream.readAllText()
		}
	}

	fun InputStream.readAllText(): String {
		return this.bufferedReader().use(BufferedReader::readText)
	}

	fun OutputStream.writeAllText(string: String) {
		this.bufferedWriter().use { writer ->
			writer.write(string)
		}
	}
}
