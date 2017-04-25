package de.christinecoenen.code.zapp.upnp;


import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.types.UDAServiceType;

public class RendererDevice {

	private final Device device;

	private final Service avTransportService;

	RendererDevice(Device device) {
		this.device = device;
		this.avTransportService = device.findService(new UDAServiceType("AVTransport"));
	}

	Service getAvTransportService() {
		return avTransportService;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		RendererDevice that = (RendererDevice) o;

		return device.equals(that.device);
	}

	@Override
	public String toString() {
		return device.getDetails().getFriendlyName();
	}
}
