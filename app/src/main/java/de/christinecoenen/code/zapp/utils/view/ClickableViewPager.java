package de.christinecoenen.code.zapp.utils.view;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;


public class ClickableViewPager extends ViewPager {

	private View.OnClickListener listener;
	private GestureDetector gestureDetector;

	public ClickableViewPager(Context context) {
		super(context);
		init();
	}

	public ClickableViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	private void init() {
		gestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
			@Override
			public boolean onSingleTapConfirmed(MotionEvent e) {
				if (listener != null) {
					listener.onClick(null);
				}
				return true;
			}
		});
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		gestureDetector.onTouchEvent(event);
		return super.onTouchEvent(event);
	}

	@Override
	public void setOnClickListener(View.OnClickListener listener) {
		super.setOnClickListener(listener);
		this.listener = listener;
	}
}
