package ru.fantlab.android.ui.modules.search.awards

import android.support.v4.widget.SwipeRefreshLayout
import ru.fantlab.android.data.dao.SearchAwardModel
import ru.fantlab.android.provider.rest.loadmore.OnLoadMore
import ru.fantlab.android.ui.base.mvp.BaseMvp
import ru.fantlab.android.ui.widgets.recyclerview.BaseViewHolder

interface SearchAwardsMvp {

	interface View : BaseMvp.View,
			SwipeRefreshLayout.OnRefreshListener,
			android.view.View.OnClickListener {

		fun onNotifyAdapter(items: List<SearchAwardModel>?, page: Int)

		fun onSetTabCount(count: Int)

		fun onSetSearchQuery(query: String)

		fun onQueueSearch(query: String)

		fun getLoadMore(): OnLoadMore<String>

		fun onItemClicked(item: SearchAwardModel)
	}

	interface Presenter : BaseMvp.Presenter,
			BaseViewHolder.OnItemClickListener<SearchAwardModel>,
			BaseMvp.PaginationListener<String> {

		fun getAwards() : ArrayList<SearchAwardModel>
	}
}