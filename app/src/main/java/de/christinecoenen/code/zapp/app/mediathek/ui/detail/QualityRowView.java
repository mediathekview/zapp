package de.christinecoenen.code.zapp.app.mediathek.ui.detail;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.christinecoenen.code.zapp.R;
import de.christinecoenen.code.zapp.app.mediathek.model.MediathekMedia;


public class QualityRowView extends LinearLayout {

	@BindView(R.id.txt_title)
	protected TextView titleText;

	private MediathekMedia media;
	private Listener listener;

	public QualityRowView(Context context, AttributeSet attrs) {
		super(context, attrs);

		setOrientation(LinearLayout.VERTICAL);

		LayoutInflater inflater = (LayoutInflater) context
			.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		//noinspection ConstantConditions
		inflater.inflate(R.layout.fragment_mediathek_detail_qualities_row, this, true);

		ButterKnife.bind(this, this);
	}

	public QualityRowView(Context context) {
		this(context, null);
	}

	public void setMedia(MediathekMedia media) {
		this.media = media;

		titleText.setText(media.getQuality().getTitleResId());
		// TODO: display media size
	}

	public Listener getListener() {
		return listener;
	}

	public void setListener(Listener listener) {
		this.listener = listener;
	}

	@OnClick(R.id.btn_share)
	protected void onShareLowClick() {
		if (listener != null) {
			listener.onShareClick(media);
		}
	}

	@OnClick(R.id.btn_download)
	protected void onDownloadLowClick() {
		if (listener != null) {
			listener.onDownloadClick(media);
		}
	}

	public interface Listener {
		void onDownloadClick(MediathekMedia media);

		void onShareClick(MediathekMedia media);
	}
}
