package sdk.chat.message.audio.keyboard

import android.content.Context
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.Rect
import android.media.MediaMetadataRetriever
import android.media.MediaRecorder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import cafe.adriel.androidaudiorecorder.Util
import com.github.piasy.rxandroidaudio.AudioRecorder
import io.reactivex.functions.Action
import io.reactivex.functions.Consumer
import sdk.chat.core.session.ChatSDK
import sdk.chat.core.ui.AbstractKeyboardOverlayFragment
import sdk.chat.core.ui.Sendable
import sdk.chat.core.utils.PermissionRequestHandler
import sdk.chat.message.audio.AudioMessageModule
import sdk.chat.message.audio.R
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit

class RecordAudioKeyboardOverlayFragment(): AbstractKeyboardOverlayFragment(), TouchAwareConstraintLayout.TouchListener {

    enum class RecordButtonMode {
        normal,
        selected,
        permission
    }

    open lateinit var rootView: View
    open lateinit var lockImageView: ImageView
    open lateinit var recordImageView: ImageView
    open lateinit var sendButton: Button
    open lateinit var cancelButton: Button
    open lateinit var timeTextView: TextView
    open lateinit var touchAwareLayout: TouchAwareConstraintLayout
    open lateinit var micImageView: ImageView
    open lateinit var infoTextView: TextView


    val audioRecorder = AudioRecorder.getInstance()
    var timer: Timer? = null
    var isRecording = false
    var permissionGranted = false

    var isLocked = false
    var recordMode: RecordButtonMode = RecordButtonMode.permission
    var audioFile: File? = null

    override fun setViewSize(width: Int?, height: Int?, context: Context) {

    }

    fun getLayout(): Int {
        return R.layout.fragment_audio_record
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        rootView = inflater.inflate(getLayout(), container, false)

        touchAwareLayout = rootView.findViewById(R.id.touchAwareLayout)
        lockImageView = rootView.findViewById(R.id.lockImage)
        recordImageView = rootView.findViewById(R.id.recordImage)
        sendButton = rootView.findViewById(R.id.sendButton)
        cancelButton = rootView.findViewById(R.id.cancelButton)
        micImageView = rootView.findViewById(R.id.micImage)
        infoTextView = rootView.findViewById(R.id.infoTextView)
        timeTextView = rootView.findViewById(R.id.timeTextView)

        cancelButton.setOnClickListener(View.OnClickListener {
            reset()
        })
        sendButton.setOnClickListener(View.OnClickListener {
            send()
        })

        touchAwareLayout.listener = this

        context?.let {
            val red = ContextCompat.getColor(it, R.color.red)
            val filter = PorterDuffColorFilter(red, PorterDuff.Mode.MULTIPLY)
            micImageView.drawable.colorFilter = filter
        }

//        updateRecordButtonForPermissions()
        reset()

        return rootView
    }

    fun lock() {
        setLockButtonSelected(true)
        sendButton.visibility = View.VISIBLE
        cancelButton.visibility = View.VISIBLE
    }

    fun newAudioFile(): File {
        val fm = ChatSDK.shared().fileManager()
        return fm.newDatedFile(fm.audioStorage(), "voice_message", "wav")
    }

    fun startRecording() {

        setRecordButtonMode(RecordButtonMode.selected)
        audioFile = newAudioFile();

        timeTextView.text = Util.formatSeconds(0)
        audioRecorder.prepareRecord(
            MediaRecorder.AudioSource.MIC,
            MediaRecorder.OutputFormat.MPEG_4,
            MediaRecorder.AudioEncoder.AAC,
            audioFile)

        audioRecorder.setOnErrorListener {
            reset()
        }

        if (audioRecorder.startRecord()) {
            isRecording = true
            startTimer()
            timeTextView.visibility = View.VISIBLE
            micImageView.visibility = View.VISIBLE
        } else {
            reset()
        }
    }

    fun send() {
        val duration = audioRecorder.progress().toLong()
        stopRecording()
        // Get the audio

        val theAudioFile = audioFile

        if (duration > AudioMessageModule.config().minimumAudioRecordingLength) {
            keyboardOverlayHandler.get()?.send(Sendable { activity, thread ->
                ChatSDK.audioMessage().sendMessage(activity, theAudioFile, "audio/wav", duration, thread)
            })
        }

        reset()
    }

    fun audioDuration(): Long? {
        val mmr = MediaMetadataRetriever()
        mmr.setDataSource(audioFile?.absolutePath)
        val durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        val millSecond = durationStr?.toLong()
        if (millSecond != null) {
            return TimeUnit.MILLISECONDS.toSeconds(millSecond)
        }
        return null
    }

    fun stopRecording() {
        isRecording = false
        audioRecorder.stopRecord()
        stopTimer()
        recordImageView.setImageResource(R.drawable.mic_button)
    }

    private fun startTimer() {
        stopTimer()
        timer = Timer()
        timer?.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                updateTimer()
            }
        }, 0, 1000)
    }

    fun reset() {
        stopTimer()
        updateRecordButtonForPermissions()
//        if (audioPermissionGranted()) {
//            setRecordButtonMode(RecordButtonMode.normal)
//        } else {
//            setRecordButtonMode(RecordButtonMode.permission)
//        }
        setLockButtonSelected(false)
        isRecording = false
        timeTextView.text = Util.formatSeconds(0)
        sendButton.visibility = View.INVISIBLE
        cancelButton.visibility = View.INVISIBLE
        timeTextView.visibility = View.INVISIBLE
        micImageView.visibility = View.INVISIBLE

        audioFile = null

    }

    private fun stopTimer() {
        timer?.let {
            it.cancel()
            it.purge()
            timer = null
        }
        if (timer != null) {
        }
    }

    private fun updateTimer() {
        rootView.post {
            if (isRecording) {
                timeTextView.text = Util.formatSeconds(audioRecorder.progress())
            } else  {
                timeTextView.text = Util.formatSeconds(0)
            }
        }
    }

    fun audioPermissionGranted(): Boolean {
        if (!permissionGranted) {
            permissionGranted = PermissionRequestHandler.recordAudioGranted()
        }
        return permissionGranted
    }

    fun requestAudioPermission() {
        keyboardOverlayHandler.get()?.activity.let {
            PermissionRequestHandler.requestRecordAudio(it).doOnComplete(Action {
                updateRecordButtonForPermissions()
            }).doOnError(Consumer {
                updateRecordButtonForPermissions()
            }).subscribe()
        }
    }

    fun updateRecordButtonForPermissions() {

        recordImageView.visibility = View.VISIBLE
        infoTextView.visibility = View.VISIBLE

        if (audioPermissionGranted()) {
            permissionGranted = true
            setRecordButtonMode(RecordButtonMode.normal)
            infoTextView.text = ""
        } else {
            permissionGranted = false
            setRecordButtonMode(RecordButtonMode.permission)
            infoTextView.text = resources.getString(R.string.permission_denied)
        }
    }

    fun isInside(x: Float, y: Float, view: View): Boolean {
        val rect = Rect()
        view.getHitRect(rect)
        return rect.contains(x.toInt(), y.toInt())
    }

    override fun touchDown(x: Float, y: Float) {
        if (isInside(x, y, recordImageView) && !isRecording) {
            if (!audioPermissionGranted()) {
                requestAudioPermission()
            } else {
                startRecording()
            }
        }
    }

    override fun touchUp(x: Float, y: Float) {
        val recordOver = isInside(x, y, recordImageView)

        if (!isLocked ) {
            if (audioPermissionGranted()) {
                if (recordOver) {
                    send()
                } else {
                    reset()
                }
            }
        }
    }

    override fun touchCancelled() {
        reset()
    }

    fun inLock(x: Float, y: Float): Boolean {
        return isInside(x, y, lockImageView)
    }

    fun inRecord(x: Float, y: Float): Boolean {
        return isInside(x, y, recordImageView)
    }

    override fun touchMoved(x: Float, y: Float) {
        if (!isLocked && isRecording && isInside(x, y, lockImageView)) {
            lock()
        }
    }

    fun setRecordButtonMode(mode: RecordButtonMode) {
        rootView.post {
            if (mode == RecordButtonMode.permission) {
                recordImageView.setImageResource(R.drawable.mic_permission)
            }
            if (mode == RecordButtonMode.normal) {
                recordImageView.setImageResource(R.drawable.mic_button)
            }
            if (mode == RecordButtonMode.selected) {
                recordImageView.setImageResource(R.drawable.mic_button_red)
            }
            recordMode = mode
        }
    }

    fun setLockButtonSelected(selected: Boolean) {
        lockImageView.context?.let {

            val color = ContextCompat.getColor(it, if (selected) R.color.red else R.color.gray_1)
            val filter = PorterDuffColorFilter(color, PorterDuff.Mode.MULTIPLY)
            lockImageView.drawable.colorFilter = filter
            lockImageView.setImageResource(R.drawable.slide_to_lock_white)

//            if (selected) {
//                lockImageView.setImageResource(R.drawable.slide_to_lock)
//            } else {
//                lockImageView.setImageResource(R.drawable.slide_to_lock_white)
//            }
            isLocked = selected
        }
    }


}