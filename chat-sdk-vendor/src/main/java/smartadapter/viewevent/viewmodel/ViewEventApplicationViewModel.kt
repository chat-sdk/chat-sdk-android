package smartadapter.viewevent.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import smartadapter.viewevent.listener.OnViewEventListener
import smartadapter.viewevent.model.ViewEvent

/**
 * Basic android view model that wraps an [OnViewEventListener].
 */
open class ViewEventApplicationViewModel<VE : ViewEvent, T : OnViewEventListener<VE>>(
    application: Application,
    val viewEventListener: T
) : AndroidViewModel(application) {

    init {
        viewEventListener.eventListener = {
            eventObserver.postValue(it)
        }
    }

    private val eventObserver by lazy(mode = LazyThreadSafetyMode.PUBLICATION) {
        MutableLiveData<VE>()
    }

    fun observe(
        lifecycle: LifecycleOwner,
        observer: Observer<VE>
    ): T {
        eventObserver.observe(lifecycle, observer)
        return viewEventListener
    }

    fun observe(
        lifecycle: LifecycleOwner,
        listener: (VE) -> Unit
    ): T {
        eventObserver.observe(lifecycle, Observer {
            listener.invoke(it)
        })
        return viewEventListener
    }

    fun removeListener(observer: Observer<ViewEvent>) =
        eventObserver.removeObserver(observer)

    fun removeObservers(lifecycleOwner: LifecycleOwner) =
        eventObserver.removeObservers(lifecycleOwner)
}