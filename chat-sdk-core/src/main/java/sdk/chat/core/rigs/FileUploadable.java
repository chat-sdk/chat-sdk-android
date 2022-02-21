package sdk.chat.core.rigs;

import java.io.File;

import sdk.chat.core.storage.FileManager;

public class FileUploadable extends Uploadable {

    public File file;

    public FileUploadable(File file, String name, String mimeType, String messageKey) {
        this(file, name, mimeType, messageKey, null);
    }

    public FileUploadable(File file, String name, String mimeType, String messageKey, Compressor compressor) {
        super(name, mimeType, messageKey, compressor);
        this.file = file;
    }

    public byte [] getBytes () {
        return FileManager.fileToBytes(file);
    }

}
