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
		return Color.argb(Color.alpha(color), r, g, b);
	}

	@SuppressWarnings("SameParameterValue")
	public static int withAlpha(int color, int alpha) {
		int r = Color.red(color);
		int b = Color.blue(color);
		int g = Color.green(color);
		return Color.argb(alpha, r, g, b);
	}
}
