package sdk.chat.ui.recycler

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.TextView
import com.google.android.material.switchmaterial.SwitchMaterial
import sdk.chat.ui.ChatSDKUI
import sdk.chat.ui.R
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
    var checked: Boolean = starting.get()

    fun click() {
        onClick.run(value)
    }
}

class NavigationViewModel(val title: String, val onClick: Runnable): SmartViewModel() {
    var clicked = false

    fun click() {
        if (!clicked) {
            clicked = true
            onClick.run()
        }
    }
}
class DividerViewModel(): SmartViewModel()

interface ButtonRunnable {
    fun run(value: Activity)
}

class ButtonViewModel(val title: String, val color: Int, val onClick: ButtonRunnable): SmartViewModel() {

    fun click(activity: Activity) {
        onClick.run(activity)
    }
}

interface ToggleRunnable {
    fun run(value: Boolean)
}

interface StartingValue {
    fun get(): Boolean
}

class ToggleViewModel(val title: String, var enabled: StartingValue, val onChange: ToggleRunnable): SmartViewModel() {

    fun change(value: Boolean) {
        onChange.run(value)
    }
}

open class RadioViewHolder(parentView: ViewGroup) :
        SmartViewHolder<RadioViewModel>(parentView, R.layout.recycler_view_holder_radio) {

    open var textView:TextView  = itemView.findViewById(R.id.textView)
    open var radioButton: RadioButton = itemView.findViewById(R.id.radioButton)

    override fun bind(item: RadioViewModel) {
        textView.text = item.title
        radioButton.isChecked = item.starting.get()
    }
}

open class SectionViewHolder(parentView: ViewGroup) :
        SmartViewHolder<SectionViewModel>(parentView, R.layout.recycler_view_holder_section) {

    open var textView:TextView  = itemView.findViewById(R.id.textView)
    open var topBorder: View = itemView.findViewById(R.id.topBorder)
    open var bottomBorder: View = itemView.findViewById(R.id.bottomBorder)

    override fun bind(item: SectionViewModel) {
        if (item.paddingTop != null) {
            itemView.setPadding(itemView.paddingLeft, item.paddingTop, itemView.paddingRight, itemView.paddingBottom)
            itemView.requestLayout();
        }
        textView.text = item.title.uppercase(Locale.ROOT)

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

open class NavigationViewHolder(parentView: ViewGroup) :
        SmartViewHolder<NavigationViewModel>(parentView, R.layout.recycler_view_holder_navigation) {

    open var textView:TextView  = itemView.findViewById(R.id.textView)
    open var imageView:ImageView  = itemView.findViewById(R.id.imageView)

    init {
        imageView.setImageDrawable(ChatSDKUI.icons().get(ChatSDKUI.icons().arrowRight, R.color.gray_very_light))
    }

//    init(par) {
//        imageView
//    }

    override fun bind(item: NavigationViewModel) {
        textView.text = item.title
    }
}

open class ButtonViewHolder(parentView: ViewGroup) :
        SmartViewHolder<ButtonViewModel>(parentView, R.layout.recycler_view_holder_button) {

    open var textView:TextView  = itemView.findViewById(R.id.textView)

    override fun bind(item: ButtonViewModel) {
        textView.text = item.title
        textView.setTextColor(item.color)
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

    open var textView:TextView  = itemView.findViewById(R.id.textView)
    open var switchMaterial:SwitchMaterial  = itemView.findViewById(R.id.switchMaterial)

    init {
        itemView.isEnabled = false
        switchMaterial.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { _, isChecked ->
            // Run after animation
            model?.change(isChecked)
        })
    }

    override fun bind(item: ToggleViewModel) {
        model = item
        textView.text = item.title
        switchMaterial.isChecked = item.enabled.get()
    }

}