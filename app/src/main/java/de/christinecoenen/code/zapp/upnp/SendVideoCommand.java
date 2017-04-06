package de.christinecoenen.code.zapp.upnp;


import android.util.Log;

import org.fourthline.cling.UpnpService;
import org.fourthline.cling.controlpoint.ActionCallback;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.support.avtransport.callback.Play;
import org.fourthline.cling.support.avtransport.callback.SetAVTransportURI;

class SendVideoCommand implements IUpnpCommand {

	private static final String TAG = SendVideoCommand.class.getSimpleName();
	private static final String METADATA = "<DIDL-Lite xmlns='urn:schemas-upnp-org:metadata-1-0/DIDL-Lite/' xmlns:dc='http://purl.org/dc/elements/1.1/' xmlns:upnp='urn:schemas-upnp-org:metadata-1-0/upnp/' xmlns:dlna='urn:schemas-dlna-org:metadata-1-0/'><item id='sample' parentID='0' restricted='0'><dc:title>%s</dc:title><dc:creator>Mohit</dc:creator><upnp:genre>No Genre</upnp:genre><res protocolInfo='http-get:*:video/mpeg:DLNA.ORG_FLAGS=01700000000000000000000000000000;DLNA.ORG_CI=0;DLNA.ORG_OP=01'>%s</res><upnp:class>object.item.videoItem</upnp:class></item></DIDL-Lite>";

	private UpnpService upnpService;
	private RendererDevice device;
	private String videoUrl;
	private Listener listener;

	SendVideoCommand(UpnpService upnpService, RendererDevice device, String videoUrl, String videoTitle) {
		this.upnpService = upnpService;
		this.device = device;
		this.videoUrl = videoUrl;
	}

	@Override
	public void execute() {
		String metadata = String.format(METADATA, "Test", videoUrl);
		ActionCallback setAVTransportUriAction = new SetAVTransportURI(device.getAvTransportService(), videoUrl, metadata) {
			@Override
			public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
				Log.e(TAG, "Action SetAVTransportUri failed on " + device.toString() + ": " + defaultMsg);
				if (listener != null) {
					listener.onCommandFailure(defaultMsg);
				}
			}

			@Override
			public void success(ActionInvocation invocation) {
				super.success(invocation);
				Log.e(TAG, "success: " + invocation.toString());

				ActionCallback playAction = new Play(device.getAvTransportService()) {
					@Override
					public void success(ActionInvocation invocation) {
						super.success(invocation);
						Log.e(TAG, "success: " + invocation.toString());
						if (listener != null) {
							listener.onCommandSuccess();
						}
					}

					@Override
					public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
						Log.e(TAG, "Action SetAVTransportUri failed on " + device.toString() + ": " + defaultMsg);
						if (listener != null) {
							listener.onCommandFailure(defaultMsg);
						}
					}
				};
				upnpService.getControlPoint().execute(playAction);
			}
		};
		upnpService.getControlPoint().execute(setAVTransportUriAction);
	}

	@Override
	public void setListener(Listener listener) {
		this.listener = listener;
	}
}
