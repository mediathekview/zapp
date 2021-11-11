package de.christinecoenen.code.zapp.tv

import android.graphics.drawable.InsetDrawable
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.content.res.AppCompatResources
import androidx.leanback.widget.ImageCardView
import androidx.leanback.widget.Presenter
import de.christinecoenen.code.zapp.models.channels.ChannelModel
import kotlin.math.max
import kotlin.math.roundToInt

class ChannelCardPresenter : Presenter() {

	override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
		return ViewHolder(ImageCardView(parent.context))
	}

	override fun onBindViewHolder(viewHolder: ViewHolder, item: Any) {
		val view = viewHolder.view as ImageCardView
		view.setMainImageDimensions(200, 100)
		view.setMainImageScaleType(ImageView.ScaleType.FIT_CENTER)
		view.setMainImageAdjustViewBounds(true)

		val channel = item as ChannelModel
		val logoDrawable = AppCompatResources.getDrawable(view.context, channel.drawableId)!!
		val inset = (max(logoDrawable.intrinsicHeight, logoDrawable.intrinsicWidth) * 0.25).roundToInt()
		view.titleText = channel.name
		view.mainImage = InsetDrawable(logoDrawable, inset)
	}

	override fun onUnbindViewHolder(viewHolder: ViewHolder) {}
}
