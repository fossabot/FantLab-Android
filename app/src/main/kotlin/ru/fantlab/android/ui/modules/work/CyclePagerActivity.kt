package ru.fantlab.android.ui.modules.work

import android.app.Application
import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.TabLayout
import android.support.v4.view.ViewPager
import android.view.Menu
import android.view.MenuItem
import butterknife.BindView
import butterknife.OnClick
import com.evernote.android.state.State
import ru.fantlab.android.R
import ru.fantlab.android.data.dao.FragmentPagerAdapterModel
import ru.fantlab.android.data.dao.TabsCountStateModel
import ru.fantlab.android.helper.ActivityHelper
import ru.fantlab.android.helper.BundleConstant
import ru.fantlab.android.helper.Bundler
import ru.fantlab.android.helper.ViewHelper
import ru.fantlab.android.provider.scheme.LinkParserHelper
import ru.fantlab.android.ui.adapter.FragmentsPagerAdapter
import ru.fantlab.android.ui.base.BaseActivity
import ru.fantlab.android.ui.base.BaseFragment
import ru.fantlab.android.ui.base.mvp.presenter.BasePresenter
import ru.fantlab.android.ui.modules.editor.EditorActivity
import ru.fantlab.android.ui.modules.work.responses.WorkResponsesFragment
import ru.fantlab.android.ui.widgets.ViewPagerView
import java.text.NumberFormat
import java.util.*


class CyclePagerActivity : BaseActivity<WorkPagerMvp.View, BasePresenter<WorkPagerMvp.View>>(),
		WorkPagerMvp.View {

	@BindView(R.id.tabs) lateinit var tabs: TabLayout
	@BindView(R.id.tabbedPager) lateinit var pager: ViewPagerView
	@BindView(R.id.fab) lateinit var fab: FloatingActionButton

	@State var index: Int = 0
	@State var workId: Int = 0
	@State var workName: String = ""
	@State var tabsCountSet = HashSet<TabsCountStateModel>()
	private val numberFormat = NumberFormat.getNumberInstance()
	private lateinit var toolbarMenu: Menu
	private var isError = false

	override fun layout(): Int = R.layout.tabbed_pager_layout

	override fun isTransparent(): Boolean = true

	override fun canBack(): Boolean = true

	override fun providePresenter(): BasePresenter<WorkPagerMvp.View> = BasePresenter()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		if (savedInstanceState == null) {
			workId = intent?.extras?.getInt(BundleConstant.EXTRA, -1) ?: -1
			workName = intent?.extras?.getString(BundleConstant.EXTRA_TWO) ?: ""
			index = intent?.extras?.getInt(BundleConstant.EXTRA_THREE, -1) ?: -1
		}
		if (workId == -1) {
			finish()
			return
		}
		setTaskName(workName)
		title = workName
		selectMenuItem(R.id.mainView, false)
		val adapter = FragmentsPagerAdapter(
				supportFragmentManager,
				FragmentPagerAdapterModel.buildForCycle(this, workId)
		)
		pager.adapter = adapter
		tabs.tabGravity = TabLayout.GRAVITY_FILL
		tabs.tabMode = TabLayout.MODE_SCROLLABLE
		tabs.setupWithViewPager(pager)
		if (savedInstanceState == null) {
			if (index != -1) {
				pager.currentItem = index
			}
		}
		tabs.addOnTabSelectedListener(object : TabLayout.ViewPagerOnTabSelectedListener(pager) {
			override fun onTabReselected(tab: TabLayout.Tab) {
				super.onTabReselected(tab)
				onScrollTop(tab.position)
			}
		})
		pager.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
			override fun onPageSelected(position: Int) {
				super.onPageSelected(position)
				hideShowFab(position)
				hideShowToolbar(position)
			}
		})
		if (savedInstanceState != null && !tabsCountSet.isEmpty()) {
			tabsCountSet.forEach { setupTab(count = it.count, index = it.tabIndex) }
		}
		hideShowFab(pager.currentItem)
	}

	override fun onCreateOptionsMenu(menu: Menu): Boolean {
		menuInflater.inflate(R.menu.work_menu, menu)
		toolbarMenu = menu
		return super.onCreateOptionsMenu(menu)
	}

	override fun onOptionsItemSelected(item: MenuItem): Boolean {
		when (item.itemId) {
			R.id.share -> {
				ActivityHelper.shareUrl(this, Uri.Builder().scheme(LinkParserHelper.PROTOCOL_HTTPS)
						.authority(LinkParserHelper.HOST_DEFAULT)
						.appendPath("work$workId")
						.toString())
				return true
			}
			R.id.sort -> {
				((pager.adapter as FragmentsPagerAdapter).getItem(2) as WorkResponsesFragment).showSortDialog()
			}
		}
		return super.onOptionsItemSelected(item)
	}

	override fun onScrollTop(index: Int) {
		if (pager.adapter == null) return
		val fragment = pager.adapter?.instantiateItem(pager, index) as? BaseFragment<*, *>
		if (fragment is BaseFragment) {
			fragment.onScrollTop(index)
		}
	}

	override fun onSetBadge(tabIndex: Int, count: Int) {
		tabsCountSet.add(TabsCountStateModel(count = count, tabIndex = tabIndex))
		setupTab(count, tabIndex)
	}

	override fun onSetTitle(title: String) {
		this.title = title
	}

	private fun hideShowFab(position: Int) {
		when (position) {
			2 -> {
				if (isLoggedIn()) {
					fab.setImageResource(R.drawable.ic_response)
					fab.show()
				}
			}
			else -> fab.hide()
		}
	}

	private fun hideShowToolbar(position: Int) {
		if (isError) {
			fab.hide()
			return
		}
		when (position) {
			0 -> {
				toolbarMenu.findItem(R.id.sort).isVisible = false
				toolbarMenu.findItem(R.id.share).isVisible = true
			}
			2 -> {
				toolbarMenu.findItem(R.id.share).isVisible = false
				toolbarMenu.findItem(R.id.sort).isVisible = true
			}
			else -> {
				toolbarMenu.findItem(R.id.share).isVisible = false
				toolbarMenu.findItem(R.id.sort).isVisible = false
			}
		}
	}

	override fun onError() {
		isError = true
		fab.hide()
	}

	@OnClick(R.id.fab)
	fun onFabClicked() {
		when (pager.currentItem) {
			2 -> {
				startActivity(Intent(this, EditorActivity::class.java)
						.putExtra(BundleConstant.EXTRA_TYPE, BundleConstant.EDITOR_NEW_RESPONSE)
						.putExtra(BundleConstant.ID, workId))
			}
		}
	}

	private fun setupTab(count: Int, index: Int) {
		val textView = ViewHelper.getTabTextView(tabs, index)
		when (index) {
			1 -> textView.text = String.format("%s(%s)", getString(R.string.content), numberFormat.format(count.toLong()))
			2 -> textView.text = String.format("%s(%s)", getString(R.string.responses), numberFormat.format(count.toLong()))
			3 -> textView.text = String.format("%s(%s)", getString(R.string.editions), numberFormat.format(count.toLong()))
		}
	}

	companion object {
		fun startActivity(context: Context, workId: Int, workName: String, index: Int = -1) {
			val intent = Intent(context, CyclePagerActivity::class.java)
			intent.putExtras(Bundler.start()
					.put(BundleConstant.EXTRA, workId)
					.put(BundleConstant.EXTRA_TWO, workName)
					.put(BundleConstant.EXTRA_THREE, index)
					.end())
			if (context is Service || context is Application) {
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
			}
			context.startActivity(intent)
		}
	}

	override fun onScrolled(isUp: Boolean) {
		if (isUp) {
			fab.hide();
		} else {
			hideShowFab(pager.currentItem)
		}
	}

	override fun onSetMarked(isMarked: Boolean, mark: Int) {
	}

	override fun onGetMark(): Int? = -1
}