package de.christinecoenen.code.zapp.views;


import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import butterknife.BindInt;
import butterknife.BindView;
import butterknife.ButterKnife;
import de.christinecoenen.code.programguide.ProgramGuideRequest;
import de.christinecoenen.code.programguide.model.Show;
import de.christinecoenen.code.zapp.R;
import de.christinecoenen.code.zapp.model.ChannelModel;

public class ProgrammInfoView extends LinearLayout {

	private static final String TAG = ProgrammInfoView.class.getSimpleName();

	protected @BindView(R.id.text_show_title) TextView showTitleView;
	protected @BindView(R.id.text_show_subtitle) TextView showSubtitleView;

	protected @BindInt(R.integer.view_program_info_update_show_info_interval_seconds) int updateShowInfoIntervalSeconds;

	private ProgramGuideRequest currentShowInfoRequest;
	private Show currentShow = null;
	private ChannelModel currentChannel;

	private final Handler handler = new Handler();
	private Timer updateShowInfoTimer;

	private final TimerTask updateShowInfoTimerTask = new TimerTask() {
		@Override
		public void run() {
			handler.post(new Runnable() {
				@Override
				public void run() {
					updateShowInfo();
				}
			});
		}
	};

	private final ProgramGuideRequest.Listener programGuideListener = new ProgramGuideRequest.Listener() {
		@Override
		public void onRequestError() {
			Log.w(TAG, "could not load show info");
			showTitleView.setText(R.string.activity_channel_detail_info_error);
			showSubtitleView.setText("");
		}

		@Override
		public void onRequestSuccess(Show currentShow) {
			Log.w(TAG, "show info loaded: " + currentShow);
			showTitleView.setText(currentShow.getTitle());
			showSubtitleView.setText(currentShow.getSubtitle());
			ProgrammInfoView.this.currentShow = currentShow;
		}
	};

	public ProgrammInfoView(Context context, AttributeSet attrs) {
		super(context, attrs);

		setOrientation(LinearLayout.VERTICAL);
		setGravity(Gravity.CENTER_VERTICAL);

		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.view_programm_info, this, true);

		ButterKnife.bind(this, this);
	}

	public ProgrammInfoView(Context context) {
		this(context, null);
	}

	public void setChannel(ChannelModel channel) {
		currentShow = null;
		currentChannel = channel;

		showTitleView.setText("");
		showSubtitleView.setText("");

		chancelProgramGuideLoading();
		loadProgramGuide();
	}

	public void pause() {
		updateShowInfoTimer.cancel();
	}

	public void resume() {
		updateShowInfoTimer = new Timer();
		updateShowInfoTimer.scheduleAtFixedRate(updateShowInfoTimerTask, 0,
				TimeUnit.SECONDS.toMillis(updateShowInfoIntervalSeconds));
	}

	public void updateShowInfo() {
		if (currentShow != null) {
			if (currentShow.getEndTime() == null || currentShow.getEndTime().isBeforeNow()) {
				reloadProgramGuide();
			}
		}
	}

	private void reloadProgramGuide() {
		Log.d(TAG, "reloadProgramGuide");
		chancelProgramGuideLoading();
		loadProgramGuide();
	}

	private void loadProgramGuide() {
		currentShowInfoRequest = new ProgramGuideRequest(getContext())
				.setChannelId(currentChannel.getId())
				.setListener(programGuideListener)
				.execute();
	}

	private void chancelProgramGuideLoading() {
		if (currentShowInfoRequest != null) {
			currentShowInfoRequest.cancel();
		}
	}
}
