package de.christinecoenen.code.zapp.upnp;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.support.v7.widget.ListViewCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ProgressBar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnItemClick;
import de.christinecoenen.code.zapp.R;
import de.christinecoenen.code.zapp.model.ChannelModel;


public class DeviceDialog extends AppCompatDialogFragment {

	private static final String TAG = DeviceDialog.class.getSimpleName();
	private static final String EXTRA_CHANNEL = DeviceDialog.class.getSimpleName();

	public static DeviceDialog newInstance(ChannelModel channel) {
		DeviceDialog dialog = new DeviceDialog();
		Bundle bundle = new Bundle();
		bundle.putSerializable(EXTRA_CHANNEL, channel);
		dialog.setArguments(bundle);
		return dialog;
	}


	@BindView(R.id.progress)
	protected ProgressBar progressBar;

	@BindView(R.id.list)
	protected ListViewCompat listView;

	private UpnpService.Binder upnpService;
	private ChannelModel channel;
	private DeviceAdapter adapter;

	private final ServiceConnection upnpServiceConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			upnpService = (UpnpService.Binder) service;
			adapter = new DeviceAdapter(getActivity(), android.R.layout.simple_list_item_1, upnpService);
			listView.setAdapter(adapter);
			stopLoading();
		}

		public void onServiceDisconnected(ComponentName className) {
			upnpService = null;
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		channel = (ChannelModel) getArguments().getSerializable(EXTRA_CHANNEL);
	}

	@Override
	public void onStart() {
		super.onStart();
		getActivity().getApplicationContext().bindService(
			new Intent(getActivity(), UpnpService.class),
			upnpServiceConnection,
			Context.BIND_AUTO_CREATE
		);

		startLoading();
	}

	@Override
	public void onStop() {
		super.onStop();
		getActivity().getApplicationContext().unbindService(upnpServiceConnection);
	}

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View view = inflater.inflate(R.layout.dialog_upnp_device, null);

		ButterKnife.bind(this, view);

		return new AlertDialog.Builder(getActivity())
			.setTitle(R.string.upnp_dlg_select_renderer)
			.setView(view)
			.create();
	}

	@OnItemClick(R.id.list)
	public void onListItemClicked(AdapterView<?> adapterView, View view, int position, long id) {
		RendererDevice device = adapter.getItem(position);
		final Listener listener = (Listener) getActivity();

		startLoading();

		upnpService.sendToDevice(device, channel.getStreamUrl(), channel.getName(), new IUpnpCommand.Listener() {
			@Override
			public void onCommandSuccess() {
				Log.d(TAG, "sendToDevice - onCommandSuccess listener");
				listener.onSendToDeviceSuccess();
				stopLoading();
			}

			@Override
			public void onCommandFailure(String reason) {
				Log.d(TAG, "sendToDevice - onCommandFailure listener");
				stopLoading();
				dismiss();
				listener.onSendToDeviceError(reason);
			}
		});
	}

	private void startLoading() {
		progressBar.setVisibility(View.VISIBLE);
		listView.setVisibility(View.INVISIBLE);
	}

	private void stopLoading() {
		progressBar.setVisibility(View.INVISIBLE);
		listView.setVisibility(View.VISIBLE);
	}

	public interface Listener {
		void onSendToDeviceSuccess();

		void onSendToDeviceError(String reason);
	}
}
