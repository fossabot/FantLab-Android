package ru.fantlab.android.ui.widgets.recyclerview.scroll

import android.annotation.SuppressLint
import android.content.Context
import android.support.design.widget.AppBarLayout
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import it.sephiroth.android.library.bottomnavigation.BottomNavigation
import ru.fantlab.android.R
import ru.fantlab.android.helper.ActivityHelper

class RecyclerViewFastScroller : FrameLayout {

	private lateinit var scrollerView: ImageView
	private val scrollTop: ImageButton? = null
	private var aHeight: Int = 0
	private var recyclerView: RecyclerView? = null
	private var layoutManager: RecyclerView.LayoutManager? = null
	private var appBarLayout: AppBarLayout? = null
	private var bottomNavigation: BottomNavigation? = null
	private var toggled: Boolean = false
	private var registeredObserver = false

	private val onScrollListener = object : RecyclerView.OnScrollListener() {
		override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
			if (scrollerView.isSelected) return
			val verticalScrollOffset = recyclerView.computeVerticalScrollOffset()
			val verticalScrollRange = recyclerView.computeVerticalScrollRange()
			val proportion = verticalScrollOffset.toFloat() / (verticalScrollRange.toFloat() - aHeight)
			setScrollerHeight(aHeight * proportion)
		}
	}

	private val observer = object : RecyclerView.AdapterDataObserver() {
		override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
			super.onItemRangeInserted(positionStart, itemCount)
			hideShow()
		}

		override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
			super.onItemRangeRemoved(positionStart, itemCount)
			hideShow()
		}

		override fun onChanged() {
			super.onChanged()
			hideShow()
		}
	}

	constructor(context: Context) : super(context) {
		init()
	}

	@JvmOverloads constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int = 0) : super(context, attrs, defStyleAttr) {
		init()
	}

	override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
		super.onSizeChanged(w, h, oldw, oldh)
		aHeight = h
	}

	@SuppressLint("ClickableViewAccessibility")
	override fun onTouchEvent(event: MotionEvent): Boolean {
		val action = event.action
		when (action) {
			MotionEvent.ACTION_DOWN -> {
				if (event.x < scrollerView.x - scrollerView.paddingStart) return false
				scrollerView.isSelected = true
				hideAppbar()
				val y = event.y
				setScrollerHeight(y)
				setRecyclerViewPosition(y)
				return true
			}
			MotionEvent.ACTION_MOVE -> {
				val y = event.y
				setScrollerHeight(y)
				setRecyclerViewPosition(y)
				return true
			}
			MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
				scrollerView.isSelected = false
				showAppbar()
				return true
			}
		}
		return super.onTouchEvent(event)
	}

	override fun onDetachedFromWindow() {
		recyclerView?.let {
			it.removeOnScrollListener(onScrollListener)
			safelyUnregisterObserver()
		}
		appBarLayout = null
		bottomNavigation = null
		super.onDetachedFromWindow()
	}

	private fun safelyUnregisterObserver() {
		try {// rare case
			recyclerView?.let {
				if (registeredObserver) {
					it.adapter?.unregisterAdapterDataObserver(observer)
				}
			}
		} catch (ignored: Exception) {
		}
	}

	protected fun init() {
		visibility = View.GONE
		clipChildren = false
		val inflater = LayoutInflater.from(context)
		inflater.inflate(R.layout.fastscroller_layout, this)
		scrollerView = findViewById(R.id.fast_scroller_handle)
		visibility = View.VISIBLE
		val activity = ActivityHelper.getActivity(context)
		activity?.let {
			appBarLayout = it.findViewById(R.id.appbar)
			bottomNavigation = it.findViewById(R.id.bottomNavigation)
		}
	}

	protected fun hideAppbar() {
		if (!toggled) {
			appBarLayout?.setExpanded(false, true)
			bottomNavigation?.setExpanded(false, true)
			toggled = true
		}
	}

	protected fun showAppbar() {
		if (toggled) {
			if (scrollerView.y == 0f) {
				appBarLayout?.setExpanded(true, true)
				bottomNavigation?.setExpanded(true, true)
				toggled = false
			}
		}
	}

	fun attachRecyclerView(recyclerView: RecyclerView) {
		if (this.recyclerView == null) {
			this.recyclerView = recyclerView
			this.layoutManager = recyclerView.layoutManager
			this.recyclerView?.addOnScrollListener(onScrollListener)
			if (recyclerView.adapter != null && !registeredObserver) {
				recyclerView.adapter.registerAdapterDataObserver(observer)
				registeredObserver = true
			}
			hideShow()
			initScrollHeight()
		}
	}

	private fun initScrollHeight() {
		recyclerView?.let {
			if (it.computeVerticalScrollOffset() == 0) {
				it.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
					override fun onPreDraw(): Boolean {
						it.viewTreeObserver.removeOnPreDrawListener(this)
						initHeight()
						return true
					}
				})
			} else {
				initHeight()
			}
		}
	}

	protected fun initHeight() {
		if (scrollerView.isSelected) return
		recyclerView?.let {
			val verticalScrollOffset = it.computeVerticalScrollOffset()
			val verticalScrollRange = computeVerticalScrollRange()
			val proportion = verticalScrollOffset.toFloat() / (verticalScrollRange.toFloat() - aHeight)
			setScrollerHeight(aHeight * proportion)
		}
	}

	private fun setRecyclerViewPosition(y: Float) {
		recyclerView?.let {
			val itemCount = it.adapter.itemCount
			val proportion: Float = when {
				scrollerView.y == 0f -> 0f
				scrollerView.y + scrollerView.height >= aHeight - TRACK_SNAP_RANGE -> 1f
				else -> y / aHeight.toFloat()
			}
			val targetPos = getValueInRange(itemCount - 1, (proportion * itemCount.toFloat()).toInt())
			when (layoutManager) {
				is StaggeredGridLayoutManager -> (layoutManager as StaggeredGridLayoutManager).scrollToPositionWithOffset(targetPos, 0)
				is GridLayoutManager -> (layoutManager as GridLayoutManager).scrollToPositionWithOffset(targetPos, 0)
				else -> (layoutManager as LinearLayoutManager).scrollToPositionWithOffset(targetPos, 0)
			}
		}
	}

	private fun setScrollerHeight(y: Float) {
		val handleHeight = scrollerView.height
		scrollerView.y = getValueInRange(aHeight - handleHeight, (y - handleHeight / 2).toInt()).toFloat()
	}

	protected fun hideShow() {
		recyclerView?.let {
			visibility = if (it.adapter != null) {
				if (it.adapter.itemCount > 10) View.VISIBLE else View.GONE
			} else {
				View.GONE
			}
			return@let
		}
		visibility = View.GONE
	}

	companion object {

		private val TRACK_SNAP_RANGE = 5

		private fun getValueInRange(max: Int, value: Int): Int {
			return Math.min(Math.max(0, value), max)
		}
	}
}