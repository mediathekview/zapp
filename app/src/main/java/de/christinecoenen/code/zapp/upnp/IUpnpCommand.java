package de.christinecoenen.code.zapp.upnp;


interface IUpnpCommand {

	void execute();

	void setListener(Listener listener);

	interface Listener {
		void onCommandSuccess();
		void onCommandFailure(String reason);
	}
}
