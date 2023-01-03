package sdk.chat.core.types;

import java.util.HashMap;
import java.util.Map;

import sdk.chat.core.storage.TransferStatus;

/**
 * Created by benjaminsmiley-andrews on 08/05/2017.
 */

public class FileUploadResult {

    public String name;
    public String mimeType;
    public String url;
    public Progress progress = new Progress();
    public Map<String, String> meta = new HashMap<>();
    public TransferStatus status;

    public TransferStatus getStatus() {
        if (status != null) {
            return status;
        }
        return TransferStatus.None;
    }

    public float progressAsPercentage() {
        if (progress != null) {
            return progress.asPercentage();
        }
        return 0;
    }

    public boolean urlValid() {
        return url != null && !url.isEmpty();
    }

}
