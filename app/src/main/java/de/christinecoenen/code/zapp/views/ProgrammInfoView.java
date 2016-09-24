package de.christinecoenen.code.zapp.views;


import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

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

	private ProgramGuideRequest currentShowInfoRequest;


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
		chancelProgramGuideLoading();
		loadProgramGuide(channel.getId());
	}


	private void loadProgramGuide(String channelId) {
		currentShowInfoRequest = new ProgramGuideRequest(getContext())
				.setChannelId(channelId)
				.setListener(programGuideListener)
				.execute();
	}

	private void chancelProgramGuideLoading() {
		showTitleView.setText("");
		showSubtitleView.setText("");
		if (currentShowInfoRequest != null) {
			currentShowInfoRequest.cancel();
		}
	}
}
