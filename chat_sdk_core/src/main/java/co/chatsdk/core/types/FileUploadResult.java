package co.chatsdk.core.types;
/**
 * Created by benjaminsmiley-andrews on 08/05/2017.
 */

public class FileUploadResult {

    public String name;
    public String mimeType;
    public String url;
    public Progress progress = new Progress();

    public boolean isComplete () {
        return url != null && !url.isEmpty();
    }

}
