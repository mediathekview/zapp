package de.christinecoenen.code.zapp.upnp;


import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.types.UDAServiceType;

public class RendererDevice {

	private Device device;

	private final Service avTransportService;

	RendererDevice(Device device) {
		this.device = device;
		this.avTransportService = device.findService(new UDAServiceType("AVTransport"));
	}

	Service getAvTransportService() {
		return avTransportService;
	}

	@Override
	public boolean equals(Object obj) {
		return this.device.equals(device);
	}

	@Override
	public String toString() {
		return device.getDetails().getFriendlyName();
	}
}
