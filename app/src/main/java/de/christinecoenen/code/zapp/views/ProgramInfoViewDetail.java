package de.christinecoenen.code.zapp.views;


import android.content.Context;
import android.util.AttributeSet;

import de.christinecoenen.code.zapp.R;

public class ProgramInfoViewDetail extends ProgramInfoViewBase {

	public ProgramInfoViewDetail(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ProgramInfoViewDetail(Context context) {
		super(context);
	}

	@Override
	protected int getViewId() {
		return R.layout.view_program_info_detail;
	}
}
