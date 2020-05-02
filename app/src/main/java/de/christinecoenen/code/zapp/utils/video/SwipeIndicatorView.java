package de.christinecoenen.code.zapp.utils.video;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import de.christinecoenen.code.zapp.R;
import de.christinecoenen.code.zapp.databinding.ViewSwipeIndicatorBinding;

public class SwipeIndicatorView extends FrameLayout {

	private FrameLayout indicator;
	private ImageView icon;

	public SwipeIndicatorView(@NonNull Context context) {
		super(context);
		init(context);
	}

	public SwipeIndicatorView(@NonNull Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public SwipeIndicatorView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context);
	}

	public void setIconResId(int resId) {
		icon.setImageResource(resId);
	}

	public void setValue(float value) {
		indicator.getLayoutParams().height = (int) (value * getHeight());
		indicator.requestLayout();

		setVisibility(VISIBLE);
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();

		LayoutParams params = (LayoutParams) getLayoutParams();
		params.height = LayoutParams.MATCH_PARENT;
		params.width = 300;
		setLayoutParams(params);
	}

	private void init(Context context) {
		View view = inflate(context, R.layout.view_swipe_indicator, this);

		ViewSwipeIndicatorBinding binding = ViewSwipeIndicatorBinding.bind(view);
		indicator = binding.indicator;
		icon = binding.icon;

		setVisibility(GONE);
	}
}
