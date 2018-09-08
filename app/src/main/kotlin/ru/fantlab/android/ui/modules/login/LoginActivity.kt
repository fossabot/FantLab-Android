package ru.fantlab.android.ui.modules.login

import android.os.Bundle
import android.support.annotation.StringRes
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.TextInputLayout
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import butterknife.BindView
import butterknife.OnEditorAction
import ru.fantlab.android.R
import ru.fantlab.android.helper.*
import ru.fantlab.android.ui.base.BaseActivity

class LoginActivity : BaseActivity<LoginMvp.View, LoginPresenter>(), LoginMvp.View {

	@BindView(R.id.username) lateinit var username: TextInputLayout
	@BindView(R.id.password) lateinit var password: TextInputLayout
	@BindView(R.id.login) lateinit var login: FloatingActionButton
	@BindView(R.id.proceedWithoutLogin) lateinit var proceedWithoutLogin: Button
	@BindView(R.id.progress) lateinit var progress: ProgressBar

	override fun onCreate(savedInstanceState: Bundle?) {
		setTheme(R.style.LoginTheme)
		super.onCreate(savedInstanceState)
		login.setOnClickListener {
			doLogin()
			proceedWithoutLogin.isEnabled = false
		}
		proceedWithoutLogin.setOnClickListener {
			presenter.proceedWithoutLogin()
		}
	}

	override fun isTransparent(): Boolean = true

	override fun providePresenter(): LoginPresenter = LoginPresenter()

	override fun layout(): Int = R.layout.login_form_layout

	override fun canBack(): Boolean = false

	@OnEditorAction(R.id.passwordEditText)
	fun onSendPassword(): Boolean {
		doLogin()
		return true
	}

	override fun onEmptyUserName(isEmpty: Boolean) {
		username.error = if (isEmpty) getString(R.string.required_field) else null
	}

	override fun onEmptyPassword(isEmpty: Boolean) {
		password.error = if (isEmpty) getString(R.string.required_field) else null
	}

	override fun onSuccessfullyLoggedIn() {
		hideProgress()
		onRestartApp()
	}

	override fun hideProgress() {
		progress.visibility = View.GONE
		login.show()
		proceedWithoutLogin.isEnabled = true
	}

	override fun showErrorMessage(msgRes: String) {
		hideProgress()
		super.showErrorMessage(msgRes)
	}

	override fun showMessage(@StringRes titleRes: Int, @StringRes msgRes: Int) {
		hideProgress()
		super.showMessage(titleRes, msgRes)
	}

	override fun showMessage(titleRes: String, msgRes: String) {
		hideProgress()
		super.showMessage(titleRes, msgRes)
	}

	override fun showProgress(@StringRes resId: Int, cancelable: Boolean) {
		login.hide()
		AppHelper.hideKeyboard(login)
		AnimHelper.animateVisibility(progress, true)
	}

	override fun showSignInFailed() {
		showMessage(R.string.error, R.string.failed_login)
	}

	override fun showUserBlocked(endDate: String) {
		val date = endDate.parseFullDate(false)!!.getTimeAgo().toString().toLowerCase()
		val message = getString(R.string.user_blocked, date)
		showMessage(getString(R.string.error), message)
	}

	override fun showUserBlockedForever() {
		showMessage(R.string.error, R.string.user_blocked_forever)
	}

	override fun validateAuth(): Boolean = true

	private fun doLogin() {
		if (progress.visibility == View.GONE) {
			presenter.login(InputHelper.toString(username), InputHelper.toString(password))
		}
	}
}