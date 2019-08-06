package co.chatsdk.core.rigs;

import android.graphics.Bitmap;

import java.io.File;

import co.chatsdk.core.utils.FileUtils;

public class FileUploadable extends Uploadable {

    public File file;

    public FileUploadable(File file, String name, String mimeType) {
        this(file, name, mimeType, null);
    }

    public FileUploadable(File file, String name, String mimeType, Compressor compressor) {
        super(name, mimeType, compressor);
        this.file = file;
    }

    public byte [] getBytes () {
        return FileUtils.fileToBytes(file);
    }

}
