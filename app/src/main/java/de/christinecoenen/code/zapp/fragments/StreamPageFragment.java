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

	public static final String ARG_CHANNEL_MODEL = "channel_model";

	@SuppressWarnings("unused")
	private static final String TAG = StreamPageFragment.class.getSimpleName();


	@BindView(R.id.channel_logo) ImageView logoView;

	protected ChannelModel channel;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_stream_page, container, false);
		ButterKnife.bind(this, rootView);

		Bundle args = getArguments();
		channel = (ChannelModel) args.getSerializable(ARG_CHANNEL_MODEL);
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
