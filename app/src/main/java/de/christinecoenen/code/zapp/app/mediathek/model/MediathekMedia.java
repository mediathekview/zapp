package de.christinecoenen.code.zapp.app.mediathek.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

import de.christinecoenen.code.zapp.R;

public class MediathekMedia implements Serializable {

	public enum Type {
		@SerializedName("video")
		VIDEO,

		@SerializedName("audio")
		AUDIO,

		@SerializedName("subtitle")
		SUBTITLE
	}

	public enum Quality {
		@SerializedName("0")
		ULTRA_LOW(R.string.fragment_mediathek_qualities_ultra_low),
		@SerializedName("1")
		VERY_LOW(R.string.fragment_mediathek_qualities_very_low),
		@SerializedName("2")
		LOW(R.string.fragment_mediathek_qualities_low),
		@SerializedName("3")
		MEDIUM(R.string.fragment_mediathek_qualities_medium),
		@SerializedName("4")
		HIGH(R.string.fragment_mediathek_qualities_high),
		@SerializedName("5")
		VERY_HIGH(R.string.fragment_mediathek_qualities_very_high);

		private final int titleResId;

		Quality(int titleResId) {
			this.titleResId = titleResId;
		}

		public int getTitleResId() {
			return titleResId;
		}
	}

	private Type type;
	private String url;
	private long size;
	private Quality quality;

	public Type getType() {
		return type;
	}

	public String getUrl() {
		return url;
	}

	public long getSize() {
		return size;
	}

	public Quality getQuality() {
		return quality;
	}

	public boolean isSubtitle() {
		return type == Type.SUBTITLE;
	}

	@Override
	public String toString() {
		return "MediathekMedia{" +
			"type=" + type +
			", url='" + url + '\'' +
			", size=" + size +
			", quality=" + quality +
			'}';
	}
}
