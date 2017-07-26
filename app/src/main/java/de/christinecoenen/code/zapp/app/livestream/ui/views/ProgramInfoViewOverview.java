package de.christinecoenen.code.zapp.app.livestream.ui.views;


import android.content.Context;
import android.util.AttributeSet;

import de.christinecoenen.code.zapp.R;

public class ProgramInfoViewOverview extends ProgramInfoViewBase {

	public ProgramInfoViewOverview(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ProgramInfoViewOverview(Context context) {
		super(context);
	}

	@Override
	protected int getViewId() {
		return R.layout.view_program_info_overview;
	}
}
