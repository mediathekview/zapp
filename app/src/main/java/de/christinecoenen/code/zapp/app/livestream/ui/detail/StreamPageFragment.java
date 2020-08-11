package de.christinecoenen.code.zapp.app.livestream.ui.detail;


import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import de.christinecoenen.code.zapp.databinding.FragmentStreamPageBinding;
import de.christinecoenen.code.zapp.model.ChannelModel;
import timber.log.Timber;

public class StreamPageFragment extends Fragment {

	private static final String ARGUMENT_CHANNEL_MODEL = "ARGUMENT_CHANNEL_MODEL";

	private View rootView;
	private TextView errorText;
	private Listener listener;

	static StreamPageFragment newInstance(ChannelModel channelModel) {
		StreamPageFragment fragment = new StreamPageFragment();
		Bundle args = new Bundle();
		args.putSerializable(ARGUMENT_CHANNEL_MODEL, channelModel);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		FragmentStreamPageBinding binding = FragmentStreamPageBinding.inflate(inflater, container, false);

		rootView = binding.getRoot();
		errorText = binding.textError;

		Bundle args = requireArguments();
		ChannelModel channel = (ChannelModel) args.getSerializable(ARGUMENT_CHANNEL_MODEL);

		if (channel != null) {
			ImageView logoView = binding.imageChannelLogo;
			logoView.setImageResource(channel.getDrawableId());
			logoView.setContentDescription(channel.getName());
			errorText.setBackgroundColor(channel.getColor());

			errorText.setOnClickListener(view -> onErrorViewClick());

			if (channel.getSubtitle() != null) {
				TextView subtitleText = binding.textChannelSubtitle;
				subtitleText.setText(channel.getSubtitle());
			}
		} else {
			Timber.w("channel argument is null");
		}

		return rootView;
	}

	@Override
	public void onAttach(@NonNull Context context) {
		super.onAttach(context);

		if (context instanceof Listener) {
			listener = (Listener) context;
		} else {
			throw new RuntimeException("Activity must implement StreamPageFragment.Listener.");
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		listener = null;
	}

	@Override
	public void onStop() {
		super.onStop();
		// don't use onPause to support multiwindow feature
		rootView.setVisibility(View.VISIBLE);
		errorText.setVisibility(View.GONE);
	}

	void onHide() {
		rootView.setVisibility(View.VISIBLE);
		errorText.setVisibility(View.GONE);
	}

	void onVideoStart() {
		fadeOutLogo();
	}

	void onVideoError(String message) {
		rootView.setVisibility(View.VISIBLE);
		errorText.setVisibility(View.VISIBLE);
		errorText.setText(message);
	}

	private void onErrorViewClick() {
		listener.onErrorViewClicked();
		onHide();
	}

	private void fadeOutLogo() {
		if (rootView.getVisibility() == View.VISIBLE) {
			Animation fadeOutAnimation = AnimationUtils.
				loadAnimation(getContext(), android.R.anim.fade_out);
			fadeOutAnimation.setAnimationListener(new Animation.AnimationListener() {
				@Override
				public void onAnimationStart(Animation animation) {
				}

				@Override
				public void onAnimationEnd(Animation animation) {
					rootView.setVisibility(View.GONE);
				}

				@Override
				public void onAnimationRepeat(Animation animation) {
				}
			});

			rootView.startAnimation(fadeOutAnimation);
		}
	}

	public interface Listener {
		void onErrorViewClicked();
	}
}
