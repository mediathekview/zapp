package de.christinecoenen.code.zapp.app.player

import android.content.Context
import android.graphics.Bitmap
import androidx.core.graphics.drawable.toBitmap
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import java.io.ByteArrayOutputStream
import java.io.IOException

object VideoInfoArtworkExtensions {

	fun VideoInfo.getArtworkByteArray(context: Context): ByteArray {
		val vectorDrawable = VectorDrawableCompat.create(
			context.resources,
			artworkVectorDrawableResId,
			context.theme
		)
		val bitmap = vectorDrawable?.toBitmap(
			vectorDrawable.intrinsicWidth,
			vectorDrawable.intrinsicHeight
		) ?: throw IOException("Could not convert artwork to byte array.")

		return convertToByteArray(bitmap)
	}

	@Throws(IOException::class)
	private fun convertToByteArray(bitmap: Bitmap): ByteArray {
		ByteArrayOutputStream().use { stream ->
			bitmap.compress(Bitmap.CompressFormat.PNG, 0, stream)
			return stream.toByteArray()
		}
	}
}
