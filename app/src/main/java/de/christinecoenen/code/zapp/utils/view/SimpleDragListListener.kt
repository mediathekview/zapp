package de.christinecoenen.code.zapp.utils.view

import com.woxthebox.draglistview.DragListView.DragListListener

open class SimpleDragListListener : DragListListener {

	override fun onItemDragStarted(position: Int) {}

	override fun onItemDragging(itemPosition: Int, x: Float, y: Float) {}

	override fun onItemDragEnded(fromPosition: Int, toPosition: Int) {}

}
