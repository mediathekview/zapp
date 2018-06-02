package de.christinecoenen.code.zapp.model

/**
 * A sorted channel list. Channel order will be
 * persisted across application starts.
 */
interface ISortableChannelList : IChannelList {

    /**
     * Reloads the current channel order from disk.
     * Us this eg. in onResume when another activity
     * might have modified the channel order.
     */
    fun reloadChannelOrder()

    /**
     * Writes the current channel order to disk.
     */
    fun persistChannelOrder()

}
