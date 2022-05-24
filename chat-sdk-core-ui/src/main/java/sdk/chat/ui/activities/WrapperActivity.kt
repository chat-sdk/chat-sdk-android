package sdk.chat.ui.activities

import android.os.Bundle
import androidx.fragment.app.Fragment
import sdk.chat.ui.R

open abstract class WrapperActivity<T: Fragment>: BaseActivity() {

    lateinit var fragment: T

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportFragmentManager.beginTransaction().add(R.id.fragmentWrapper, fragment).commit()
    }

    open abstract fun bindFragment()

    override fun getLayout(): Int {
        return R.layout.activity_wrapper
    }
}
