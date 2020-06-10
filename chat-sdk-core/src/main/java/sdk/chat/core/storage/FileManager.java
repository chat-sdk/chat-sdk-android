package sdk.chat.core.storage;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;

import androidx.annotation.Nullable;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.utils.CurrentLocale;

import static android.os.Environment.isExternalStorageRemovable;

public class FileManager {

    public static String images = "images";
    public static String videos = "videos";
    public static String audio = "audio";

    protected Context context;

    public FileManager(Context context) {
        this.context = context;
    }

    public File storage() {
        File dir;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) ||
                !isExternalStorageRemovable()) {
            dir = context.getExternalFilesDir("");
        } else {
            dir = context.getFilesDir();
        }
        return subdir(dir, ChatSDK.config().storageDirectory);
    }

    public File cache() {
        File dir;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) ||
                !isExternalStorageRemovable()) {
            dir = context.getExternalCacheDir();
        } else {
            dir = context.getCacheDir();
        }
        return subdir(dir, ChatSDK.config().storageDirectory);
    }

    public File subdir(File parent, String name) {
        File directory = new File(parent, name);
        if (directory.exists() || directory.mkdir()) {
            return directory;
        }
        return null;
    }

    public File imageCache() {
        return subdir(cache(), images);
    }

    public File imageStorage() {
        return subdir(storage(), images);
    }

    public File videoCache() {
        return subdir(cache(), videos);
    }

    public File videoStorage() {
        return subdir(storage(), videos);
    }

    public File audioCache() {
        return subdir(cache(), audio);
    }

    public File audioStorage() {
        return subdir(storage(), audio);
    }

    public String date() {
        DateFormat format = new SimpleDateFormat("yy_mm_dd_hh_ss_SSS", CurrentLocale.get(context));
        return format.format(new Date());
    }

    public void addFileToGallery(File file) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri contentUri = Uri.fromFile(file);
        mediaScanIntent.setData(contentUri);
        context.sendBroadcast(mediaScanIntent);
    }

    public File newFile(File parent, String name) {
        return newFile(parent, name, null);
    }

    public File newFile(File parent, String name, @Nullable String ext) {
        return new File(parent, name + (ext == null ? "" : "." + ext));
    }

    public File newDatedFile(File parent, String name) {
        return newDatedFile(parent, name, null);
    }

    public File newDatedFile(File parent, String name, @Nullable String ext) {
        return new File(parent, name + date() + (ext == null ? "" : "." + ext));
    }

    public static byte [] fileToBytes (File file) {
        int size = (int) file.length();
        byte[] bytes = new byte[size];
        try {
            BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
            buf.read(bytes, 0, bytes.length);
            buf.close();
        } catch (IOException e) {
            ChatSDK.events().onError(e);
        }
        return bytes;
    }
}

