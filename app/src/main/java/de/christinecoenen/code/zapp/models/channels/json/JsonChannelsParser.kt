package de.christinecoenen.code.zapp.models.channels.json

import android.content.Context
import androidx.core.graphics.toColorInt
import com.google.gson.Gson
import de.christinecoenen.code.zapp.models.channels.ChannelModel
import java.util.*

/**
 * Helper class to convert a json string to a list of
 * channel models.
 */
internal class JsonChannelsParser(private val context: Context) {

	/**
	 * This methods is blocking.
	 * @param json  a json file with model data
	 * @return      a list of fully parsed ChannelModels
	 * or empty list if the provided string is null
	 * See "R.raw.channels" for channel format'
	 */
	fun parse(json: String): List<ChannelModel> {
		return getChannelList(json)
	}

	private fun getChannelList(json: String): List<ChannelModel> {
		val gson = Gson()
		val jsonModels = gson.fromJson(json, Array<JsonChannelModel>::class.java) ?: arrayOf()
		return mapJsonModels(jsonModels)
	}

	private fun mapJsonModels(jsonModels: Array<JsonChannelModel>): List<ChannelModel> {
		return jsonModels.map(::mapJsonModel)
	}

	private fun mapJsonModel(jsonModel: JsonChannelModel): ChannelModel {
		val logoResourceId = context.resources.getIdentifier(jsonModel.logoName, "drawable", context.packageName)

		return ChannelModel(
			id = jsonModel.id,
			name = jsonModel.name,
			subtitle = jsonModel.subtitle,
			streamUrl = jsonModel.streamUrl,
			drawableId = logoResourceId,
			color = jsonModel.color.toColorInt(),
			isEnabled = true
		)
	}
}
