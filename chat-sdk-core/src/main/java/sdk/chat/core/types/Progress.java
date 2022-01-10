package sdk.chat.core.types;

/**
 * Created by benjaminsmiley-andrews on 08/05/2017.
 */

public class Progress {

    public long transferredBytes;
    public long totalBytes;

    public float asFraction() {
        return (float) transferredBytes / (float) totalBytes;
    }

    public float getTotalBytes() {
        return totalBytes;
    }

    public void set(long totalBytes, long transferredBytes) {
        this.transferredBytes = transferredBytes;
        this.totalBytes = totalBytes;
    }

    public Progress add(Progress result) {
        transferredBytes += result.transferredBytes;
        totalBytes += result.totalBytes;
        return this;
    }

}
