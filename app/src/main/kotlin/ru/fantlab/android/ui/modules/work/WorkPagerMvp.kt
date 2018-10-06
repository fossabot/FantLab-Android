package ru.fantlab.android.ui.modules.work

import ru.fantlab.android.ui.base.mvp.BaseMvp

interface WorkPagerMvp {

	interface View : BaseMvp.View {

		fun onSetBadge(tabIndex: Int, count: Int)

		fun onSetTitle(title: String)
	}

	interface Presenter : BaseMvp.Presenter
}