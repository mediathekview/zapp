package de.christinecoenen.code.zapp;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import org.fourthline.cling.android.AndroidUpnpService;
import org.fourthline.cling.android.AndroidUpnpServiceImpl;
import org.fourthline.cling.android.FixedAndroidLogHandler;
import org.fourthline.cling.controlpoint.ActionCallback;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.types.UDAServiceType;
import org.fourthline.cling.registry.Registry;
import org.fourthline.cling.registry.RegistryListener;
import org.fourthline.cling.support.avtransport.callback.Play;
import org.fourthline.cling.support.avtransport.callback.SetAVTransportURI;

import java.util.logging.Level;
import java.util.logging.Logger;

public class UpnpTest extends AppCompatActivity {

	private RegistryListener registryListener = new RegistryListener() {
		@Override
		public void remoteDeviceDiscoveryStarted(Registry registry, RemoteDevice device) {
			Log.d("test", "discovery started");
		}

		@Override
		public void remoteDeviceDiscoveryFailed(Registry registry, RemoteDevice device, Exception ex) {
			Log.d("test", "discovery failed: " + ex.getMessage());
		}

		@Override
		public void remoteDeviceAdded(Registry registry, final RemoteDevice device) {
			String videoUrl = "http://dwstream5-lh.akamaihd.net/i/dwstream5_live@124540/master.m3u8";
			final Service renderingControlService = device.findService(new UDAServiceType("RenderingControl"));
			final Service avTransportService = device.findService(new UDAServiceType("AVTransport"));
			if (renderingControlService != null && avTransportService != null) {
				Log.d("test", "added renderer: " + device.getDetails().getFriendlyName());

				ActionCallback setAVTransportUriAction =
					new SetAVTransportURI(avTransportService, videoUrl, "<DIDL-Lite xmlns='urn:schemas-upnp-org:metadata-1-0/DIDL-Lite/' xmlns:dc='http://purl.org/dc/elements/1.1/' xmlns:upnp='urn:schemas-upnp-org:metadata-1-0/upnp/' xmlns:dlna='urn:schemas-dlna-org:metadata-1-0/'><item id='sample' parentID='0' restricted='0'><dc:title>"+"Test"+"</dc:title><dc:creator>Mohit</dc:creator><upnp:genre>No Genre</upnp:genre><res protocolInfo='http-get:*:video/mpeg:DLNA.ORG_FLAGS=01700000000000000000000000000000;DLNA.ORG_CI=0;DLNA.ORG_OP=01'>"+videoUrl+"</res><upnp:class>object.item.videoItem</upnp:class></item></DIDL-Lite>") {
						@Override
						public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
							Log.e("test", "Action SetAVTransportUri failed on " + device.toString() + ": " + defaultMsg);
						}

						@Override
						public void success(ActionInvocation invocation) {
							super.success(invocation);
							Log.e("test", "success: " + invocation.toString());

							ActionCallback playAction = new Play(avTransportService) {
								@Override
								public void success(ActionInvocation invocation) {
									super.success(invocation);
									Log.e("test", "success: " + invocation.toString());
								}

								@Override
								public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
									Log.e("test", "Action SetAVTransportUri failed on " + device.toString() + ": " + defaultMsg);
								}
							};
							upnpService.getControlPoint().execute(playAction);
						}
					};
				upnpService.getControlPoint().execute(setAVTransportUriAction);
			}
		}

		@Override
		public void remoteDeviceUpdated(Registry registry, RemoteDevice device) {
			//Log.d("test", "updated: " + device.toString());
		}

		@Override
		public void remoteDeviceRemoved(Registry registry, RemoteDevice device) {

		}

		@Override
		public void localDeviceAdded(Registry registry, LocalDevice device) {

		}

		@Override
		public void localDeviceRemoved(Registry registry, LocalDevice device) {

		}

		@Override public void beforeShutdown(Registry registry) {

		}

		@Override
		public void afterShutdown() {

		}
	};

	private AndroidUpnpService upnpService;

	private ServiceConnection serviceConnection = new ServiceConnection() {

		public void onServiceConnected(ComponentName className, IBinder service) {
			Log.d("test", "onServiceConnected");
			upnpService = (AndroidUpnpService) service;

			// Get ready for future device advertisements
			upnpService.getRegistry().addListener(registryListener);

			// Now add all devices to the list we already know about
			for (Device device : upnpService.getRegistry().getDevices()) {
				//registryListener.deviceAdded(device);
				Log.d("test", "added: " + device.toString());
			}

			// Search asynchronously for all devices, they will respond soon
			upnpService.getControlPoint().search();
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

		// Fix the logging integration between java.util.logging and Android internal logging
		org.seamless.util.logging.LoggingUtil.resetRootHandler(
			new FixedAndroidLogHandler()
		);
		// Now you can enable logging as needed for various categories of Cling:
		Logger.getLogger("org.fourthline.cling").setLevel(Level.INFO);

		// This will start the UPnP service if it wasn't already started
		getApplicationContext().bindService(
			new Intent(this, AndroidUpnpServiceImpl.class),
			serviceConnection,
			Context.BIND_AUTO_CREATE
		);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (upnpService != null) {
			upnpService.getRegistry().removeListener(registryListener);
		}
		// This will stop the UPnP service if nobody else is bound to it
		getApplicationContext().unbindService(serviceConnection);
	}
}
