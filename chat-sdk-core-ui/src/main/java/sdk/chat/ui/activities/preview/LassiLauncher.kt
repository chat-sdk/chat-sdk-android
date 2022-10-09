package sdk.chat.ui.activities.preview

import android.app.Activity
import android.content.Intent
import com.lassi.domain.media.LassiOption
import com.lassi.domain.media.MediaType
import com.lassi.presentation.builder.Lassi
import sdk.chat.core.dao.Keys
import sdk.chat.core.utils.Device
import sdk.chat.ui.R

open class LassiLauncher {

//    private val mPermissionSettingResult =
//        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
//            requestPermissionForDocument()
//        }
    companion object {
        open fun launchImagePicker(activity: Activity): Intent {

                val spanCount = if (Device.isPortrait()) 3 else 5

                val intent = Lassi(activity)
                    .with(LassiOption.CAMERA_AND_GALLERY)
                    .setMaxCount(8)
                    .setGridSize(spanCount)
                    .setPlaceHolder(R.drawable.ic_image_placeholder)
                    .setErrorDrawable(R.drawable.ic_image_placeholder)
                    .setSelectionDrawable(R.drawable.ic_checked_media)
                    .setStatusBarColor(R.color.colorPrimaryDark)
                    .setToolbarColor(R.color.colorPrimary)
                    .setToolbarResourceColor(R.color.white)
                    .setProgressBarColor(R.color.colorAccent)
        //                .setCropType(CropImageView.CropShape.OVAL)
        //                .setCropAspectRatio(1, 1)
                    .setCompressionRation(10)
                    .setMinFileSize(0)
                    .setMaxFileSize(10000)
        //                .enableActualCircleCrop()
                    .setSupportedFileTypes("jpg", "jpeg", "png", "webp", "gif")
        //                .enableFlip()
        //                .enableRotate()
                    .build()

//                intent.putExtra(Keys.IntentKeyPreviewActivityType, Keys.IntentKeyPreviewActivityTypeImage)
                return intent

        }

    open fun launchVideoPicker(activity: Activity): Intent {

        val spanCount = if (Device.isPortrait()) 3 else 5

        val intent = Lassi(activity)
            .with(LassiOption.CAMERA_AND_GALLERY)
            .setMaxCount(spanCount)
            .setGridSize(3)
            .setMaxCount(8)
            .setMinTime(1)
            .setMaxTime(150)
            .setMinFileSize(0)
            .setMaxFileSize(40000)
            .setMediaType(MediaType.VIDEO)
            .setStatusBarColor(R.color.colorPrimaryDark)
            .setToolbarColor(R.color.colorPrimary)
            .setToolbarResourceColor(android.R.color.white)
            .setProgressBarColor(R.color.colorAccent)
            .setSelectionDrawable(R.drawable.ic_checked_media)
            .setSupportedFileTypes("mp4", "mkv", "webm", "avi", "flv", "3gp")
            .build()

//        intent.putExtra(Keys.IntentKeyPreviewActivityType, Keys.IntentKeyPreviewActivityTypeVideo)
        return intent
    }
}

//    open fun requestPermissionForDocument() {
//        when {
//            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
//                if (Environment.isExternalStorageManager()) {
//
//                } else {
//                    try {
//                        val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
//                        intent.addCategory("android.intent.category.DEFAULT")
//                        intent.data = Uri.parse(
//                            String.format("package:%s", applicationContext?.packageName)
//                        )
//                        mPermissionSettingResult.launch(intent)
//                    } catch (e: Exception) {
//                        val intent = Intent()
//                        intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
//                        mPermissionSettingResult.launch(intent)
//                    }
//                }
//            }
//            else -> {
//            }
//        }
//    }

}