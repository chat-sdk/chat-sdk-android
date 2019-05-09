package co.chatsdk.core.rigs;

import java.io.File;

public class FileItem {

    public File file;
    public String name;
    public String mimeType;
    public boolean compress;

    public FileItem (File file, String name, String mimeType) {
        this(file, name, mimeType, false);
    }
    public FileItem (File file, String name, String mimeType, boolean compress) {
        this.file = file;
        this.name = name;
        this.mimeType = mimeType;
        this.compress = compress;
    }
}
