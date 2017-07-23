package de.christinecoenen.code.zapp.app.livestream.ui.detail;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.christinecoenen.code.zapp.R;
import de.christinecoenen.code.zapp.model.ChannelModel;

public class StreamPageFragment extends Fragment {

	private static final String TAG = StreamPageFragment.class.getSimpleName();
	private static final String ARGUMENT_CHANNEL_MODEL = "ARGUMENT_CHANNEL_MODEL";

	protected @BindView(R.id.image_channel_logo) ImageView logoView;
	protected @BindView(R.id.text_channel_subtitle) TextView subtitleText;
	protected @BindView(R.id.text_error) TextView errorText;

	private View rootView;

	public static StreamPageFragment newInstance(ChannelModel channelModel) {
		StreamPageFragment fragment = new StreamPageFragment();
		Bundle args = new Bundle();
		args.putSerializable(ARGUMENT_CHANNEL_MODEL, channelModel);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.fragment_stream_page, container, false);
		ButterKnife.bind(this, rootView);

		Bundle args = getArguments();
		ChannelModel channel = (ChannelModel) args.getSerializable(ARGUMENT_CHANNEL_MODEL);

		if (channel != null) {
			logoView.setImageResource(channel.getDrawableId());
			logoView.setContentDescription(channel.getName());
			errorText.setBackgroundColor(channel.getColor());

			if (channel.getSubtitle() != null) {
				subtitleText.setText(channel.getSubtitle());
			}
		} else {
			Log.w(TAG, "channel argument is null");
		}

		return rootView;
	}

	@Override
	public void onStop() {
		super.onStop();
		// don't use onPause to support multiwindow feature
		rootView.setVisibility(View.VISIBLE);
		errorText.setVisibility(View.GONE);
	}

	public void onHide() {
		rootView.setVisibility(View.VISIBLE);
		errorText.setVisibility(View.GONE);
	}

	public void onVideoStart() {
		fadeOutLogo();
	}

	public void onVideoError(String message) {
		rootView.setVisibility(View.VISIBLE);
		errorText.setVisibility(View.VISIBLE);
		errorText.setText(message);
	}

	private void fadeOutLogo() {
		if (rootView.getVisibility() == View.VISIBLE) {
			Animation fadeOutAnimation = AnimationUtils.
					loadAnimation(getContext(), android.R.anim.fade_out);
			fadeOutAnimation.setAnimationListener(new Animation.AnimationListener() {
				@Override
				public void onAnimationStart(Animation animation) {}

				@Override
				public void onAnimationEnd(Animation animation) {
					rootView.setVisibility(View.GONE);
				}

				@Override
				public void onAnimationRepeat(Animation animation) {}
			});

			rootView.startAnimation(fadeOutAnimation);
		}
	}
}
