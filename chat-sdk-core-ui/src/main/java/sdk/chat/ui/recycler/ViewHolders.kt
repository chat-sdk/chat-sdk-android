package sdk.chat.ui.recycler

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import kotlinx.android.synthetic.main.recycler_view_holder_navigation.view.*
import kotlinx.android.synthetic.main.recycler_view_holder_radio.view.*
import kotlinx.android.synthetic.main.recycler_view_holder_section.view.*
import kotlinx.android.synthetic.main.recycler_view_holder_section.view.textView
import kotlinx.android.synthetic.main.recycler_view_holder_toggle.view.*
import sdk.chat.ui.R
import sdk.chat.ui.icons.Icons
import smartadapter.viewholder.SmartViewHolder
import java.util.*

open class SmartViewModel() {

}

open class SectionViewModel(val title: String, val paddingTop: Int? = null): SmartViewModel() {
    var hideTopBorder = false
    var hideBottomBorder = false

    open fun hideBorders(top: Boolean? = false, bottom: Boolean? = false): SectionViewModel {
        if (top != null) {
            hideTopBorder = top
        }
        if (bottom != null) {
            hideBottomBorder = bottom
        }
        return this
    }

}

interface RadioRunnable {
    fun run(value: String)
}

class RadioViewModel(val group: String, val title: String, val value: String, var starting: StartingValue, val onClick: RadioRunnable): SmartViewModel() {
    var delay: Long = 200
    var checked: Boolean = starting.get()
    open fun setDelay(delay: Long): RadioViewModel {
        this.delay = delay
        return this
    }

    fun click() {
        Handler(Looper.getMainLooper()).postDelayed({
            onClick.run(value)
        }, delay)
    }
}

class NavigationViewModel(val title: String, val onClick: Runnable): SmartViewModel() {
    var delay: Long = 0
    var clicked = false
    open fun setDelay(delay: Long): NavigationViewModel {
        this.delay = delay
        return this
    }

    fun click() {
        if (!clicked) {
            clicked = true
            Handler(Looper.getMainLooper()).postDelayed({
                onClick.run()
            }, delay)
        }
    }
}
class DividerViewModel(): SmartViewModel()

interface ButtonRunnable {
    fun run(value: Activity)
}

class ButtonViewModel(val title: String, val color: Int, val onClick: ButtonRunnable): SmartViewModel() {
    var delay: Long = 500
    open fun setDelay(delay: Long): ButtonViewModel {
        this.delay = delay
        return this
    }

    fun click(activity: Activity) {
        Handler(Looper.getMainLooper()).postDelayed({
            onClick.run(activity)
        }, delay)
    }
}

interface ToggleRunnable {
    fun run(value: Boolean)
}

interface StartingValue {
    fun get(): Boolean
}

class ToggleViewModel(val title: String, var enabled: StartingValue, val onChange: ToggleRunnable): SmartViewModel() {
    var delay: Long = 500
    open fun setDelay(delay: Long): ToggleViewModel {
        this.delay = delay
        return this
    }

    fun change(value: Boolean) {
        Handler(Looper.getMainLooper()).postDelayed({
            onChange.run(value)
        }, delay)
    }
}

open class RadioViewHolder(parentView: ViewGroup) :
        SmartViewHolder<RadioViewModel>(parentView, R.layout.recycler_view_holder_radio) {

    override fun bind(item: RadioViewModel) {
        with(itemView) {
            textView.text = item.title
            radioButton.isChecked = item.starting.get()
        }
    }
}

open class SectionViewHolder(parentView: ViewGroup) :
        SmartViewHolder<SectionViewModel>(parentView, R.layout.recycler_view_holder_section) {

    override fun bind(item: SectionViewModel) {
        with(itemView) {
            if (item.paddingTop != null) {
                itemView.setPadding(itemView.paddingLeft, item.paddingTop, itemView.paddingRight, itemView.paddingBottom)
                itemView.requestLayout();
            }
            textView.text = item.title.toUpperCase(Locale.ROOT)

            if (item.hideTopBorder) {
                topBorder.visibility = View.INVISIBLE
            } else {
                topBorder.visibility = View.VISIBLE
            }

            if (item.hideBottomBorder) {
                bottomBorder.visibility = View.INVISIBLE
            } else {
                bottomBorder.visibility = View.VISIBLE
            }
        }
    }
}

open class NavigationViewHolder(parentView: ViewGroup) :
        SmartViewHolder<NavigationViewModel>(parentView, R.layout.recycler_view_holder_navigation) {

    init {
        with(itemView) {
            imageView.setImageDrawable(Icons.get(Icons.choose().arrowRight, R.color.gray_very_light))
        }
    }

//    init(par) {
//        imageView
//    }

    override fun bind(item: NavigationViewModel) {
        with(itemView) {
            textView.text = item.title
        }
    }
}

open class ButtonViewHolder(parentView: ViewGroup) :
        SmartViewHolder<ButtonViewModel>(parentView, R.layout.recycler_view_holder_button) {

    override fun bind(item: ButtonViewModel) {
        with(itemView) {
            textView.text = item.title
            textView.setTextColor(item.color)
        }
    }

}

open class DividerViewHolder(parentView: ViewGroup) :
        SmartViewHolder<DividerViewModel>(parentView, R.layout.recycler_view_holder_divider) {

    override fun bind(item: DividerViewModel) {}
}

open class ToggleViewHolder(parentView: ViewGroup) :
        SmartViewHolder<ToggleViewModel>(parentView, R.layout.recycler_view_holder_toggle) {

    protected var model: ToggleViewModel? = null
//    protected var checked = false

    init {
        with(itemView) {
            itemView.isEnabled = false
            switchMaterial.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { _, isChecked ->
                // Run after animation
                model?.change(isChecked)

//                if (isChecked != checked) {
//                }
            })
        }
    }

    override fun bind(item: ToggleViewModel) {
        with(itemView) {
            model = item
            textView.text = item.title
            switchMaterial.isChecked = item.enabled.get()
        }
    }

}