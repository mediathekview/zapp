package de.christinecoenen.code.zapp.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.christinecoenen.code.zapp.R;
import de.christinecoenen.code.zapp.model.ChannelModel;

public class StreamPageFragment extends Fragment {

	@SuppressWarnings("unused")
	private static final String TAG = StreamPageFragment.class.getSimpleName();
	private static final String ARGUMENT_CHANNEL_MODEL = "ARGUMENT_CHANNEL_MODEL";

	@BindView(R.id.image_channel_logo) ImageView logoView;

	protected ChannelModel channel;

	public static StreamPageFragment newInstance(ChannelModel channelModel) {
		StreamPageFragment fragment = new StreamPageFragment();
		Bundle args = new Bundle();
		args.putSerializable(ARGUMENT_CHANNEL_MODEL, channelModel);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_channel_detail, container, false);
		ButterKnife.bind(this, rootView);

		Bundle args = getArguments();
		channel = (ChannelModel) args.getSerializable(ARGUMENT_CHANNEL_MODEL);
		logoView.setImageResource(channel.getDrawableId());

		return rootView;
	}

	public void onHide() {
		logoView.setVisibility(View.VISIBLE);
	}

	public void onVideoStart() {
		fadeOutLogo();
	}

	private void fadeOutLogo() {
		if (logoView.getVisibility() == View.VISIBLE) {
			Animation fadeOutAnimation = AnimationUtils.
					loadAnimation(getContext(), android.R.anim.fade_out);
			fadeOutAnimation.setAnimationListener(new Animation.AnimationListener() {
				@Override
				public void onAnimationStart(Animation animation) {}

				@Override
				public void onAnimationEnd(Animation animation) {
					logoView.setVisibility(View.GONE);
				}

				@Override
				public void onAnimationRepeat(Animation animation) {}
			});

			logoView.startAnimation(fadeOutAnimation);
		}
	}
}
