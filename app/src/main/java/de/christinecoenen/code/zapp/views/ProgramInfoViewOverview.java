package de.christinecoenen.code.zapp.views;


import android.content.Context;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.util.AttributeSet;
import android.view.LayoutInflater;

import de.christinecoenen.code.programguide.model.Show;
import de.christinecoenen.code.zapp.R;
import de.christinecoenen.code.zapp.databinding.ViewProgramInfoOverviewBinding;

public class ProgramInfoViewOverview extends ProgramInfoViewBase {

	private ViewProgramInfoOverviewBinding binding;

	public ProgramInfoViewOverview(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ProgramInfoViewOverview(Context context) {
		this(context, null);
	}

	@Override
	protected ViewDataBinding inflate(LayoutInflater inflater) {
		binding = DataBindingUtil.inflate(inflater, R.layout.view_program_info_overview, this, true);
		return binding;
	}

	@Override
	protected void applyShow(Show show) {
		binding.setShow(show);
	}

	@Override
	protected void setIsLoading(boolean isLoading) {
		binding.setIsLoading(isLoading);
	}
}
