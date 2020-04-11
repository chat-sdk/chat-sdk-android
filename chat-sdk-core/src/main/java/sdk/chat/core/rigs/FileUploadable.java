package sdk.chat.core.rigs;

import java.io.File;

import sdk.chat.core.storage.FileManager;

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
        return FileManager.fileToBytes(file);
    }

}
