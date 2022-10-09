package sdk.chat.core.types;

/**
 * Created by benjaminsmiley-andrews on 08/05/2017.
 */

public class Progress {

    public long transferredBytes;
    public long totalBytes;
    public Throwable error;

    public Progress() {
    }

    public Progress(Throwable error) {
        this.error = error;
    }

    public Progress(long transferredBytes, long totalBytes) {
        this.transferredBytes = transferredBytes;
        this.totalBytes = totalBytes;
    }

    public float asPercentage() {
        return asFraction() * 100f;
    }

    public float asFraction() {
        if (totalBytes == 0) {
            return 0;
        }
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
