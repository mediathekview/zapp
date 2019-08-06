package de.christinecoenen.code.zapp.model;

/**
 * A sorted channel list. Channel order will be
 * persisted across application starts.
 */
public interface ISortableChannelList extends IChannelList {

	void reload();

	/**
	 * Reloads the current channel order from disk.
	 * Us this eg. in onResume when another activity
	 * might have modified the channel order.
	 */
	void reloadChannelOrder();

	/**
	 * Writes the current channel order to disk.
	 */
	void persistChannelOrder();

}
