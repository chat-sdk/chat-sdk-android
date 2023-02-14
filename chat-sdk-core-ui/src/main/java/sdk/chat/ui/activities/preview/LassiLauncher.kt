package sdk.chat.ui.activities.preview

import android.app.Activity
import android.content.Intent
import com.lassi.domain.media.LassiOption
import com.lassi.domain.media.MediaType
import com.lassi.presentation.builder.Lassi
import com.lassi.presentation.cropper.CropImageView.CropShape
import sdk.chat.core.utils.Device
import sdk.chat.ui.R

open class LassiLauncher {

    companion object {

        open fun launchImagePicker(activity: Activity): Intent {
            return launchImagePicker(activity, 8, null, false)
        }

        open fun launchImagePicker(activity: Activity, maxCount: Int? = 8, cropType: CropShape? = null, circleCrop: Boolean? = false): Intent {

            val spanCount = if (Device.isPortrait()) 3 else 5

            val builder = Lassi(activity)
                .with(LassiOption.CAMERA_AND_GALLERY)
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
                .setSupportedFileTypes("jpg", "jpeg", "png", "webp", "gif");

            cropType?.let {
                builder.setCropType(it)
            }

            circleCrop?.let {
                if (it) {
                    builder.enableActualCircleCrop()
                    builder.setCropType(CropShape.OVAL)
                    builder.setCropAspectRatio(1, 1)
                }
            }

            maxCount?.let {
                builder.setMaxCount(it)
            }

            return builder.build()
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