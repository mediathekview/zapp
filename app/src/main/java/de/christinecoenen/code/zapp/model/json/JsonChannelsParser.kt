package de.christinecoenen.code.zapp.model.json


import android.content.Context
import android.graphics.Color
import com.google.gson.Gson
import de.christinecoenen.code.zapp.model.ChannelModel

/**
 * Helper class to convert a json string to a list of
 * channel models.
 */
internal class JsonChannelsParser(private val context: Context) {

	/**
	 * This methods is blocking.
	 * @param json  a json file with model data
	 * @return      a list of fully parsed ChannelModels
	 * or null if the provided string is null
	 * See "R.raw.channels" for channel format'
	 */
	fun parse(json: String): List<ChannelModel> {
		return getChannelList(json)
	}

	private fun getChannelList(json: String): List<ChannelModel> {
		val jsonModels = gson.fromJson(json, Array<JsonChannelModel>::class.java)
		return jsonModels.map(this::mapJsonModel)
	}

	private fun mapJsonModel(jsonModel: JsonChannelModel): ChannelModel {
		val logoResourceId = context.resources.getIdentifier(jsonModel.logoName, "drawable", context.packageName)

		return ChannelModel(
			id = jsonModel.id,
			name = jsonModel.name,
			subtitle = jsonModel.subtitle,
			streamUrl = jsonModel.streamUrl,
			drawableId = logoResourceId,
			color = Color.parseColor(jsonModel.color)
		)
	}

	companion object {
		private val gson = Gson()
	}
}
