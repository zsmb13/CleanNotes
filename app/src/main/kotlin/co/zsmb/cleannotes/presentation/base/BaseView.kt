package co.zsmb.cleannotes.presentation.base

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import co.zsmb.cleannotes.di.base.ActivityComponent

abstract class BaseView<PR : Terminable, AC : ActivityComponent<PR>> : AppCompatActivity(), NavigableView {

    protected lateinit var activityComponent: AC

    protected lateinit var presenter: PR

    protected abstract fun createComponent(): AC

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initActivityComponent()
        initPresenter()
    }

    private fun initActivityComponent() {
        val oldComponent = lastCustomNonConfigurationInstance

        if (oldComponent == null) {
            activityComponent = createComponent()
        }
        else {
            // The object stored can only be put there by our own
            // onRetainCustomNonConfigurationInstance method, and
            // is always of type C or is null
            @Suppress("UNCHECKED_CAST")
            activityComponent = oldComponent as AC
        }
    }

    final override fun onRetainCustomNonConfigurationInstance() = activityComponent

    private fun initPresenter() {
        presenter = activityComponent.createPresenter()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!isChangingConfigurations) {
            presenter.onTerminate()
        }
    }

}