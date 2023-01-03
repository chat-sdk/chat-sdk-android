package sdk.chat.ui.activities.preview

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.viewpager2.widget.ViewPager2
import com.lassi.common.utils.KeyUtils
import com.lassi.data.media.MiMedia
import io.reactivex.Completable
import org.pmw.tinylog.Logger
import sdk.chat.core.dao.Keys
import sdk.chat.core.dao.Thread
import sdk.chat.core.session.ChatSDK
import sdk.chat.core.ui.ThemeProvider
import sdk.chat.ui.R
import sdk.chat.ui.activities.BaseActivity
import sdk.guru.common.DisposableMap
import java.io.File

open class ChatPreviewActivity: BaseActivity() {

    open var viewPager2: ViewPager2? = null
    open var adapter: PreviewPagerAdapter? = null

    open var addButton: ImageButton? = null
    open var sendButton: ImageButton? = null
    open var deleteButton: ImageButton? = null
    open var exitButton: ImageButton? = null
    open var positionText: TextView? = null

    open val dm = DisposableMap()
    open var threadEntityID: String? = null

    override fun getLayout(): Int {
        return R.layout.activity_chat_media_preview
    }

    open val receiveData = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { it ->
        if (it.resultCode == Activity.RESULT_OK) {
            it.data?.let {
                addFromIntent(it)
            }
        }
    }

    open fun addFromIntent(intent: Intent) {
        val selectedMedia = intent.getSerializableExtra(KeyUtils.SELECTED_MEDIA) as ArrayList<MiMedia>
        if (!selectedMedia.isNullOrEmpty()) {
            adapter?.let { ad ->
                ad.addMedia(selectedMedia)
                viewPager2?.currentItem = ad.itemCount - 1
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        updateThread(savedInstanceState)

        adapter = PreviewPagerAdapter(this)
        viewPager2 = findViewById(R.id.viewPager)
        addButton = findViewById(R.id.addButton)
        sendButton = findViewById(R.id.sendButton)
        deleteButton = findViewById(R.id.deleteButton)
        exitButton = findViewById(R.id.exitButton)
        positionText = findViewById(R.id.positionText)

        viewPager2?.adapter = adapter

        viewPager2?.registerOnPageChangeCallback(object: ViewPager2.OnPageChangeCallback() {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels)
                updatePage()
            }

            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
            }

            override fun onPageScrollStateChanged(state: Int) {
                super.onPageScrollStateChanged(state)
            }
        })

        exitButton?.setOnClickListener(View.OnClickListener {
            finish()
        })

        deleteButton?.setOnClickListener(View.OnClickListener {
            viewPager2?.let {
                adapter?.removeItemAtIndex(it.currentItem)
                updatePage()
            }
        })

        sendButton?.setOnClickListener(View.OnClickListener {
            adapter?.let { it ->

                val thread = ChatSDK.db().fetchEntityWithEntityID(threadEntityID, Thread::class.java)
                if (thread != null) {
                    for(item in it.media) {

                        var completables = ArrayList<Completable>()

                        item.path?.let { path ->
                            val file = File(path)
                            if (item.duration > 0) {
                                if (ChatSDK.videoMessage() != null && threadEntityID != null) {
                                    Logger.debug("Send video");
                                    completables.add(ChatSDK.videoMessage().sendMessageWithVideo(file, thread))
                                }
                            } else {
                                if (ChatSDK.imageMessage() != null && threadEntityID != null) {
                                    Logger.debug("ImageSend: Send image");
                                    completables.add(ChatSDK.imageMessage().sendMessageWithImage(file, thread))
                                }
                            }
                        }

                        Completable.merge(completables).subscribe(this)
                    }
                }
            }
            finish()
        })

        val provider = ChatSDK.feather().instance(
            ThemeProvider::class.java
        )
        provider?.applyTheme(sendButton, "chat-preview-send-button")

        addButton?.setOnClickListener { view: View? ->
            adapter?.let {
                if (!it.isVideo()) {
                    val intent = LassiLauncher.launchImagePicker(this)
                    receiveData.launch(intent)
                } else {
                    val intent = LassiLauncher.launchVideoPicker(this)
                    receiveData.launch(intent)
                }
            }
        }

        savedInstanceState?.let {
            val mediaArray = it.getParcelableArrayList<MiMedia>("media")
            adapter?.addMedia(mediaArray)
        }

        addFromIntent(intent)

    }

    open fun updateThread(bundle: Bundle?) {
        var bundle = bundle
        if (bundle == null) {
            bundle = intent.extras
        }
        if (bundle != null && bundle.containsKey(Keys.IntentKeyThreadEntityID)) {
            threadEntityID = bundle.getString(Keys.IntentKeyThreadEntityID)

        }
        if (threadEntityID == null) {
            finish()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        updateThread(intent.extras)
        addFromIntent(intent)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // Get the items
        adapter?.let {
            outState.putParcelableArrayList("media", it.media)
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        val mediaArray = savedInstanceState.getParcelableArrayList<MiMedia>("media")
        adapter?.addMedia(mediaArray)
    }

    open fun updatePage() {
        adapter?.let {
            val count = it.itemCount
            if (it.itemCount == 0) {
                positionText?.setText("")
            } else {
                val current = (viewPager2?.currentItem ?: 0) + 1
                positionText?.setText("$current/$count")
            }
            deleteButton?.visibility = if(count > 0) View.VISIBLE else View.INVISIBLE
            sendButton?.visibility = if(count > 0) View.VISIBLE else View.INVISIBLE
        }
    }

}