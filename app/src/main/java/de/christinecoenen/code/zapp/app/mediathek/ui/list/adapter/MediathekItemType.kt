package de.christinecoenen.code.zapp.app.mediathek.ui.list.adapter

/**
 * Type of mediathek list item that should be displayed.
 * The list item can be slightly adjusted, depending on the type.
 */
enum class MediathekItemType {
	/**
	 * General purpose.
	 * Items relevant to the user will be highlighted.
	 * No thumbnail will be loaded.
	 */
	Default,

	/**
	 * For shows in the downloads list.
	 * A thumbnail will be loaded.
	 */
	Download,

	/**
	 * For shows that the user has started watching.
	 */
	History,

	/**
	 * For shows that have been bookmarked by the user.
	 */
	Bookmark,
}
