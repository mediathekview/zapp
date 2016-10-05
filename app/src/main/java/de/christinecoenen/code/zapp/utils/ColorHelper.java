package de.christinecoenen.code.zapp.utils;


import android.animation.ArgbEvaluator;
import android.graphics.Color;

public class ColorHelper {

	public static int interpolate(float percent, int color1, int color2) {
		return (int) new ArgbEvaluator().evaluate(percent, color1, color2);
	}

	@SuppressWarnings("SameParameterValue")
	public static int darker(int color, float amount) {
		float diff = 255 * amount;
		int r = (int) Math.max(0, (Color.red(color) - diff));
		int b = (int) Math.max(0, (Color.blue(color) - diff));
		int g = (int) Math.max(0, (Color.green(color) - diff));
		return Color.rgb(r, g, b);
	}
}
