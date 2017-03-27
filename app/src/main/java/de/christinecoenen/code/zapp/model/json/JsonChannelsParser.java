package de.christinecoenen.code.zapp.model.json;


import android.content.Context;
import android.graphics.Color;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import de.christinecoenen.code.zapp.model.ChannelModel;

/**
 * Helper class to convert a json string to a list of
 * channel models.
 */
class JsonChannelsParser {

	private final Context context;

	JsonChannelsParser(Context context) {
		this.context = context;
	}

	/**
	 * This methods is blocking.
	 * @param json  a json file with model data
	 * @return      a list of fully parsed ChannelModels
	 *              or null if the provided string is null
	 * See "R.raw.channels" for channel format'
     */
	List<ChannelModel> parse(String json) {
		return getChannelList(json);
	}


	private List<ChannelModel> getChannelList(String json) {
		Gson gson = new Gson();
		JsonChannelModel[] jsonModels = gson.fromJson(json, JsonChannelModel[].class);

		if (jsonModels == null) {
			jsonModels = new JsonChannelModel[0];
		}

		return mapJsonModels(jsonModels);
	}

	private List<ChannelModel> mapJsonModels(JsonChannelModel[] jsonModels) {
		List<ChannelModel> channels = new ArrayList<>(jsonModels.length);

		for (JsonChannelModel jsonModel : jsonModels) {
			channels.add(mapJsonModel(jsonModel));
		}

		return channels;
	}

	private ChannelModel mapJsonModel(JsonChannelModel jsonModel) {
		int logoResourceId = context.getResources().
				getIdentifier(jsonModel.logoName, "drawable", context.getPackageName());

		ChannelModel model = new ChannelModel();
		model.setId(jsonModel.id);
		model.setName(jsonModel.name);
		model.setSubtitle(jsonModel.subtitle);
		model.setStreamUrl(jsonModel.streamUrl);
		model.setDrawableId(logoResourceId);

		if (jsonModel.color != null) {
			model.setColor(Color.parseColor(jsonModel.color));
		}

		return model;
	}
}
