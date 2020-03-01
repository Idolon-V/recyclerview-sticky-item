package net.idolon.stickyitem

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Px
import androidx.core.view.ViewCompat


class StickyItemContainer @JvmOverloads constructor(
        context: Context?,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : ViewGroup(context, attrs, defStyleAttr) {

    @Px private var offsetX = 0
    @Px private var offsetY = 0
    @Px private var lastOffsetX = Int.MIN_VALUE
    @Px private var lastOffsetY = Int.MIN_VALUE

    private var lastStickyItemPosition = Int.MIN_VALUE

    val itemView get() = requireNotNull(getChildAt(0))

    var onBindDataListener: OnBindDataListener? = null

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {

        require(childCount == 1) { "Only 1 child view can be added to the container" }

        val itemView = itemView

        // Measure child view and consider margins
        measureChildWithMargins(itemView, widthMeasureSpec, 0, heightMeasureSpec, 0)

        // Get layout parameters of child element
        val lp = itemView.layoutParams as MarginLayoutParams

        // Calculate child view width
        @Px var desiredWidth = itemView.measuredWidth + lp.leftMargin + lp.rightMargin

        // Calculate child view height
        @Px var desiredHeight = itemView.measuredHeight + lp.topMargin + lp.bottomMargin

        // Consider the inner margin of the parent container
        desiredWidth += paddingLeft + paddingRight
        desiredHeight += paddingTop + paddingBottom

        // Compare the recommended minimum and expected value and take the larger one
        desiredWidth = desiredWidth.coerceAtLeast(suggestedMinimumWidth)
        desiredHeight = desiredHeight.coerceAtLeast(suggestedMinimumHeight)

        // Set the final measurement
        setMeasuredDimension(
                View.resolveSize(desiredWidth, widthMeasureSpec),
                View.resolveSize(desiredHeight, heightMeasureSpec)
        )
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val child = itemView
        val lp = child.layoutParams as MarginLayoutParams
        @Px val left = lp.leftMargin + paddingLeft + offsetX
        @Px val right = left + child.measuredWidth
        @Px val top = lp.topMargin + paddingTop + offsetY
        @Px val bottom = top + child.measuredHeight
        child.layout(left, top, right, bottom)
    }

    override fun generateLayoutParams(layoutParams: LayoutParams) = MarginLayoutParams(layoutParams)

    override fun generateLayoutParams(attrs: AttributeSet) = MarginLayoutParams(context, attrs)

    override fun checkLayoutParams(layoutParams: LayoutParams) = layoutParams is MarginLayoutParams

    fun offsetItemViewVertically(@Px offset: Int) {
        if (lastOffsetY != offset) {
            offsetY = offset
            itemView.offsetVerticalPositionBy(offset = offsetY - lastOffsetY)
        }
        lastOffsetY = offsetY
    }

    fun offsetItemViewHorizontally(@Px offset: Int) {
        if (lastOffsetX != offset) {
            offsetX = offset
            itemView.offsetHorizontalPositionBy(offset = offsetX - lastOffsetX)
        }
        lastOffsetX = offsetX
    }

    private fun View.offsetVerticalPositionBy(@Px offset: Int) = ViewCompat.offsetTopAndBottom(this, offset)

    private fun View.offsetHorizontalPositionBy(@Px offset: Int) = ViewCompat.offsetLeftAndRight(this, offset)

    fun onDataChange(stickyItemPosition: Int) {
        if (onBindDataListener != null && lastStickyItemPosition != stickyItemPosition) {
            onBindDataListener?.invoke(stickyItemPosition)
        }
        lastStickyItemPosition = stickyItemPosition
    }

    fun reset() {
        lastStickyItemPosition = Int.MIN_VALUE
    }
}

typealias OnBindDataListener = (position: Int) -> Unit