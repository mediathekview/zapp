package de.christinecoenen.code.zapp.views;


import android.content.Context;
import android.databinding.ViewDataBinding;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import butterknife.BindInt;
import butterknife.ButterKnife;
import de.christinecoenen.code.programguide.ProgramGuideRequest;
import de.christinecoenen.code.programguide.model.Show;
import de.christinecoenen.code.zapp.R;
import de.christinecoenen.code.zapp.model.ChannelModel;

public abstract class ProgramInfoViewBase extends LinearLayout {

	private static final String TAG = ProgramInfoViewBase.class.getSimpleName();


	@BindInt(R.integer.view_program_info_update_show_info_interval_seconds)
	protected int updateShowInfoIntervalSeconds;

	@BindInt(R.integer.view_program_info_update_show_time_interval_seconds)
	protected int updateShowTimeIntervalSeconds;

	private ProgramGuideRequest currentShowInfoRequest;
	private Show currentShow = null;
	private ChannelModel currentChannel;
	private ViewDataBinding binding;

	private final Handler handler = new Handler();
	private Timer timer;

	private final ProgramGuideRequest.Listener programGuideListener = new ProgramGuideRequest.Listener() {
		@Override
		public void onRequestError() {
			logMessage("could not load show info");

			ProgramInfoViewBase.this.currentShow = null;
			applyShow(currentShow);
			setIsLoading(false);
		}

		@Override
		public void onRequestSuccess(Show currentShow) {
			logMessage("show info loaded: " + currentShow);

			ProgramInfoViewBase.this.currentShow = currentShow;
			applyShow(currentShow);
			setIsLoading(false);
		}
	};

	public ProgramInfoViewBase(Context context, AttributeSet attrs) {
		super(context, attrs);

		LayoutInflater inflater = (LayoutInflater) context
			.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		binding = inflate(inflater);

		setOrientation(LinearLayout.VERTICAL);
		setGravity(Gravity.CENTER_VERTICAL);

		ButterKnife.bind(this, this);
	}

	public ProgramInfoViewBase(Context context) {
		this(context, null);
	}

	public void setChannel(ChannelModel channel) {
		if (channel == currentChannel) {
			return;
		}

		currentShow = null;
		currentChannel = channel;

		applyShow(null);

		chancelProgramGuideLoading();
		loadProgramGuide();
	}

	public void pause() {
		timer.cancel();
		timer = null;
	}

	public void resume() {
		if (timer != null) {
			return;
		}

		timer = new Timer();
		timer.scheduleAtFixedRate(new UpdateShowInfoTask(), 0,
			TimeUnit.SECONDS.toMillis(updateShowInfoIntervalSeconds));
		timer.scheduleAtFixedRate(new UpdateShowTimeTask(), 0,
			TimeUnit.SECONDS.toMillis(updateShowTimeIntervalSeconds));
	}

	protected abstract ViewDataBinding inflate(LayoutInflater inflater);

	protected abstract void applyShow(Show show);

	protected abstract void setIsLoading(boolean isLoading);

	private void updateShowInfo() {
		if (currentShow == null ||
			currentShow.getEndTime() == null ||
			currentShow.getEndTime().isBeforeNow()) {
			reloadProgramGuide();
		}
	}

	private void displayTime() {
		binding.invalidateAll();
	}

	private void reloadProgramGuide() {
		if (currentChannel == null) {
			return;
		}

		logMessage("reloadProgramGuide");
		chancelProgramGuideLoading();
		loadProgramGuide();
	}

	private void loadProgramGuide() {
		setIsLoading(true);

		currentShowInfoRequest = new ProgramGuideRequest(getContext())
			.setChannelId(currentChannel.getId())
			.setListener(programGuideListener)
			.execute();
	}

	private void chancelProgramGuideLoading() {
		setIsLoading(false);
		if (currentShowInfoRequest != null) {
			currentShowInfoRequest.cancel();
		}
	}

	private void logMessage(String message) {
		String channelId = null;
		if (currentChannel != null) {
			channelId = currentChannel.getId();
		}
		Log.d(TAG, channelId + " - " + message);
	}

	private class UpdateShowTimeTask extends TimerTask {
		@Override
		public void run() {
			handler.post(new Runnable() {
				@Override
				public void run() {
					displayTime();
				}
			});
		}
	}

	private class UpdateShowInfoTask extends TimerTask {
		@Override
		public void run() {
			handler.post(new Runnable() {
				@Override
				public void run() {
					logMessage("UpdateShowInfoTask");
					updateShowInfo();
				}
			});
		}
	}
}
