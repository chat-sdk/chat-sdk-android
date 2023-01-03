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

//    public static FileUploadable fromJson(String json) {
//        if (json == null) {
//            return null;
//        }
//        Gson gson = new Gson();
//        HashMap<String, String> map = gson.fromJson(json, HashMap.class);
//        if (map == null) {
//            return null;
//        }
//        String name = map.get(nameKey);
//        String mimeType = map.get(mimeTypeKey);
//
//        String path = map.get(pathKey);
//        File file = new File(path);
//        if (file.exists() && name != null && mimeType != null) {
//            return new FileUploadable(file, name, mimeType);
//        }
//        return null;
//    }
}
