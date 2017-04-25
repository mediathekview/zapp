package de.christinecoenen.code.zapp.upnp;

import android.content.Context;
import android.widget.ArrayAdapter;


public class DeviceAdapter extends ArrayAdapter<RendererDevice> implements UpnpService.Listener {

	public DeviceAdapter(Context context, int resource, UpnpService.Binder upnpRendererService) {
		super(context, resource, upnpRendererService.getDevices());
		upnpRendererService.addListener(this);
	}

	@Override
	public void onDeviceAdded(RendererDevice device) {
		notifyDataSetChanged();
	}

	@Override
	public void onDeviceRemoved(RendererDevice device) {
		notifyDataSetChanged();
	}
}
