package de.christinecoenen.code.zapp.utils.video;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.ImageView;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.christinecoenen.code.zapp.R;

public class SwipeIndicatorView extends FrameLayout {

	@BindView(R.id.indicator)
	protected FrameLayout indicator;

	@BindView(R.id.icon)
	protected ImageView icon;

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
		inflate(context, R.layout.view_swipe_indicator, this);
		ButterKnife.bind(this);

		setVisibility(GONE);
	}
}
