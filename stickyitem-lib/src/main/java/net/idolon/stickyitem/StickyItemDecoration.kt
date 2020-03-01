package net.idolon.stickyitem

import android.graphics.Canvas
import android.view.View
import androidx.annotation.Px
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.*
import androidx.recyclerview.widget.StaggeredGridLayoutManager


class StickyItemDecoration(
    private val stickyItemContainer: StickyItemContainer,
    private val isStickyItemPredicate: (position: Int) -> Boolean
) : ItemDecoration() {

    private var firstVisiblePosition = 0
    private var stickyItemPosition = 0
    private var adapter: Adapter<*>? = null
    private var enableStickyItem = true

    @Suppress("unused")
    fun enableStickyItem(value: Boolean) {
        this.enableStickyItem = value
        if (!value) {
            stickyItemContainer.visibility = View.INVISIBLE
        }
    }

    override fun onDraw(canvas: Canvas, recyclerView: RecyclerView, state: State) {
        super.onDraw(canvas, recyclerView, state)

        checkCache(recyclerView)

        // If the RecyclerView is not set for checkCache, the adapter property is null
        if (adapter == null) return

        recyclerView.calculateStickyItemPosition()

        if (enableStickyItem && firstVisiblePosition >= stickyItemPosition && stickyItemPosition != NO_POSITION) {

            when (recyclerView.orientation) {
                VERTICAL -> {

                    stickyItemContainer.onDataChange(stickyItemPosition)

                    val stickyItemContainerBottom = stickyItemContainer.bottom

                    val nextStickyItemPosition = recyclerView.findNextVisibleStickyItemPosition()
                    val nextStickyItemView = recyclerView.findViewByPosition(nextStickyItemPosition)

                    @Px val offset =
                        if (nextStickyItemView != null &&
                            nextStickyItemView.top > 0 && nextStickyItemView.top < stickyItemContainerBottom
                        ) {
                            nextStickyItemView.top - stickyItemContainerBottom
                        } else {
                            0
                        }

                    stickyItemContainer.offsetItemViewVertically(offset)
                    stickyItemContainer.visibility = View.VISIBLE
                }

                HORIZONTAL -> {

                    stickyItemContainer.onDataChange(stickyItemPosition)

                    val stickyItemContainerRight = stickyItemContainer.right

                    val nextStickyItemPosition = recyclerView.findNextVisibleStickyItemPosition()
                    val nextStickyItemView = recyclerView.findViewByPosition(nextStickyItemPosition)

                    @Px val offset =
                        if (nextStickyItemView != null &&
                            nextStickyItemView.left > 0 && nextStickyItemView.left < stickyItemContainerRight
                        ) {
                            nextStickyItemView.left - stickyItemContainerRight
                        } else {
                            0
                        }

                    stickyItemContainer.offsetItemViewHorizontally(offset)
                    stickyItemContainer.visibility = View.VISIBLE
                }
            }

        } else {
            stickyItemContainer.reset()
            stickyItemContainer.visibility = View.INVISIBLE
        }
    }

    private fun RecyclerView.findNextVisibleStickyItemPosition(): Int =
        (findFirstVisiblePosition()..findLastVisiblePosition()).find(::isStickyItemPosition)
            ?: NO_POSITION

    private fun RecyclerView.calculateStickyItemPosition() {

        // Get the first visible item position
        firstVisiblePosition = findFirstVisiblePosition()

        // Get the position of the sticky item

        val position = findStickyItemPosition(fromPosition = firstVisiblePosition)
        if (position >= 0) {
            // The sticky item position is valid
            stickyItemPosition = position
        }
    }

    /**
     * Decrement from the specified position to find out the position of a sticky item
     *
     * @return sticky item position or [NO_POSITION] if no sticky item was found
     */
    private fun findStickyItemPosition(fromPosition: Int) =
        (fromPosition downTo 0).find(::isStickyItemPosition) ?: NO_POSITION

    /**
     * Check if item at position is a sticky one by applying [isStickyItemPredicate]
     */
    private fun isStickyItemPosition(position: Int) = isStickyItemPredicate(position)

    /**
     * Locate the first visible item
     */
    private fun RecyclerView.findFirstVisiblePosition(): Int {
        return when (val layoutManager = this.layoutManager) {
            is GridLayoutManager -> layoutManager.findFirstVisibleItemPosition()
            is LinearLayoutManager -> layoutManager.findFirstVisibleItemPosition()
            is StaggeredGridLayoutManager -> layoutManager.findFirstVisibleItemPositions(null).min()
                ?: NO_POSITION
            else -> NO_POSITION
        }
    }

    /**
     * Locate the last visible item
     */
    private fun RecyclerView.findLastVisiblePosition(): Int {
        return when (val layoutManager = this.layoutManager) {
            is GridLayoutManager -> layoutManager.findLastVisibleItemPosition()
            is LinearLayoutManager -> layoutManager.findLastVisibleItemPosition()
            is StaggeredGridLayoutManager -> layoutManager.findLastVisibleItemPositions(null).max()
                ?: NO_POSITION
            else -> NO_POSITION
        }
    }

    private fun RecyclerView.findViewByPosition(position: Int): View? =
        layoutManager?.findViewByPosition(position)

    /**
     * Check if cached adapter value is still the actual one
     */
    private fun checkCache(recyclerView: RecyclerView) {
        val adapter = recyclerView.adapter
        if (this.adapter !== adapter) {
            this.adapter = adapter

            // The adapter is null or a different one, empty the cache
            stickyItemPosition = NO_POSITION

            adapter?.registerAdapterDataObserver(object : AdapterDataObserver() {
                override fun onChanged() = reset()

                override fun onItemRangeChanged(positionStart: Int, itemCount: Int) = reset()

                override fun onItemRangeChanged(positionStart: Int, itemCount: Int, payload: Any?) =
                    reset()

                override fun onItemRangeInserted(positionStart: Int, itemCount: Int) = reset()

                override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) = reset()

                override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) =
                    reset()
            })
        }
    }

    private fun reset() = stickyItemContainer.reset()

}

private val RecyclerView.orientation
    get() = when (val layoutManager = layoutManager) {
        is LinearLayoutManager -> layoutManager.orientation
        is StaggeredGridLayoutManager -> layoutManager.orientation
        else -> VERTICAL
    }