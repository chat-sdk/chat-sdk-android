package smartadapter.extension

import android.util.TypedValue
import android.view.View
import androidx.core.content.ContextCompat

fun View.setBackgroundAttribute(attribute: Int) {
    setBackgroundDrawable(with(TypedValue()) {
        context.theme.resolveAttribute(attribute, this, true)
        ContextCompat.getDrawable(context, resourceId)
    })
}
