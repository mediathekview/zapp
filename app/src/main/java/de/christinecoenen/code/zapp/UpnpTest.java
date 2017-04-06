package de.christinecoenen.code.zapp;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ListViewCompat;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.christinecoenen.code.zapp.upnp.DeviceAdapter;
import de.christinecoenen.code.zapp.upnp.RendererDevice;
import de.christinecoenen.code.zapp.upnp.UpnpRendererService;

public class UpnpTest extends AppCompatActivity implements AdapterView.OnItemClickListener {

	private static final String videoUrl = "http://www.lehman.edu/faculty/hoffmann/itc/techteach/video/Video1.WMV";

	@BindView(R.id.list)
	protected ListViewCompat listView;

	private UpnpRendererService.Binder upnpService;
	private ArrayAdapter<RendererDevice> adapter;

	private ServiceConnection serviceConnection = new ServiceConnection() {

		public void onServiceConnected(ComponentName className, IBinder service) {
			Log.d("test", "onServiceConnected");
			upnpService = (UpnpRendererService.Binder) service;
			upnpService.search();
			adapter = new DeviceAdapter(UpnpTest.this, android.R.layout.simple_list_item_1, upnpService);
			listView.setAdapter(adapter);
		}

		public void onServiceDisconnected(ComponentName className) {
			Log.d("test", "onServiceDisconnected");
			upnpService = null;
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_upnp_test);
		ButterKnife.bind(this);

		listView.setOnItemClickListener(this);

		// This will start the UPnP service if it wasn't already started
		getApplicationContext().bindService(
			new Intent(this, UpnpRendererService.class),
			serviceConnection,
			Context.BIND_AUTO_CREATE
		);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		// This will stop the UPnP service if nobody else is bound to it
		getApplicationContext().unbindService(serviceConnection);
	}

	@Override
	public void onItemClick(AdapterView<?> adapterView, android.view.View view, int position, long id) {
		RendererDevice device = adapter.getItem(position);
		Toast.makeText(this, device.toString(), Toast.LENGTH_LONG).show();
		upnpService.sendToDevice(device, videoUrl, null);
	}
}
