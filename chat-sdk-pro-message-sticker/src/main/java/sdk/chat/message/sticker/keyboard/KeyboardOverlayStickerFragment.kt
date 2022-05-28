package sdk.chat.message.sticker.keyboard

import android.content.Context
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.functions.Consumer
import sdk.chat.core.session.ChatSDK
import sdk.chat.core.ui.AbstractKeyboardOverlayFragment
import sdk.chat.core.ui.Sendable
import sdk.chat.message.sticker.R
import sdk.chat.message.sticker.StickerPack
import sdk.chat.message.sticker.provider.StickerPackProvider
import sdk.chat.ui.keyboard.KeyboardOverlayOptionsFragment
import smartadapter.SmartRecyclerAdapter
import smartadapter.viewevent.listener.OnClickEventListener

open class KeyboardOverlayStickerFragment(): AbstractKeyboardOverlayFragment() {

    open lateinit var stickerRecyclerAdapter: SmartRecyclerAdapter
    open lateinit var stickerRecyclerView: RecyclerView

    open lateinit var packRecyclerAdapter: SmartRecyclerAdapter
    open lateinit var packRecyclerView: RecyclerView

    open lateinit var rootView: View
    open var optionExecutor: KeyboardOverlayOptionsFragment.OptionExecutor? = null

    open var stickerPacks: List<StickerPack>? = null

    open var width = 0
    open var height = 0

    open var packWidth = 50
    open var stickerSize = 0f
    open var currentPack: StickerPack? = null

    open var provider: StickerPackProvider = ChatSDK.feather().instance(StickerPackProvider::class.java)

    open fun getLayout(): Int {
        return R.layout.fragment_sticker_smart_recycler
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        rootView = inflater.inflate(getLayout(), container, false)

        stickerRecyclerView = rootView.findViewById(R.id.stickerRecyclerView)
        packRecyclerView = rootView.findViewById(R.id.packRecyclerView)

        stickerRecyclerAdapter = SmartRecyclerAdapter
            .items(stickers())
            .map(StickerModel::class, StickerViewHolder::class)
            .add(OnClickEventListener {
                var model = stickerRecyclerAdapter.getItem(it.position)
                if (model is StickerModel) {
                    keyboardOverlayHandler.get()?.send(Sendable { _, thread ->
                        ChatSDK.stickerMessage()
                            .sendMessageWithSticker(model.sticker.imageName, thread)
                    })
                }
            })
            .into(stickerRecyclerView)

        packRecyclerAdapter = SmartRecyclerAdapter
            .items(packs())
            .map(StickerPackModel::class, StickerPackViewHolder::class)
            .add(OnClickEventListener {
                var model = packRecyclerAdapter.getItem(it.position)
                if (model is StickerPackModel) {
                    currentPack = model.pack
                    reloadStickers()
                }
            })
            .into(packRecyclerView)

        packRecyclerView.layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
        stickerRecyclerView.layoutManager = GridLayoutManager(context, 2, RecyclerView.HORIZONTAL, false)

        val d = provider.packs?.subscribe(Consumer {
            stickerPacks = it
            currentPack = it[0]
            reload()
        })

        provider.preload()

        return rootView
    }

    override fun setViewSize(width: Int, height: Int, context: Context) {
        // Work out the sticker item size
        packWidth = packWidth(context)
//        packWidth = 70;
        stickerSize = (height - packWidth) / 2f

        if (::packRecyclerAdapter.isInitialized) {
            reload()
        }
    }

    open fun reload() {
        packRecyclerAdapter.setItems(packs())
        reloadStickers()
    }

    open fun reloadStickers() {
        stickerRecyclerAdapter.setItems(stickers())
    }

    open fun packs(): MutableList<Any> {

        var items = arrayListOf<Any>()

        stickerPacks?.let {
            for (pack in it) {
                items.add(StickerPackModel(pack, packWidth))
            }
        }

        return items
    }

    open fun stickers(): MutableList<Any> {
        var items = arrayListOf<Any>()

        currentPack?.let {
            for (sticker in it.stickers) {
                items.add(StickerModel(sticker, stickerSize.toInt()))
            }
        }
        return items
    }

    open fun packWidth(context: Context): Int {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 70f, context.resources.displayMetrics).toInt()
    }

}