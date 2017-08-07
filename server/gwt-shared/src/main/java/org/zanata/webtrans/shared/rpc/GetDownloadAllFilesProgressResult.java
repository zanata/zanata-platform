package org.zanata.webtrans.shared.rpc;

public class GetDownloadAllFilesProgressResult implements DispatchResult {

    private static final long serialVersionUID = 1L;

    private long currentProgress;
    private long maxProgress;
    private String downloadId;

    @SuppressWarnings("unused")
    private GetDownloadAllFilesProgressResult() {
    }

    public GetDownloadAllFilesProgressResult(long currentProgress,
            long maxProgress, String downloadId) {
        this.maxProgress = maxProgress;
        this.currentProgress = currentProgress;
        this.downloadId = downloadId;
    }

    public long getCurrentProgress() {
        return currentProgress;
    }

    public long getMaxProgress() {
        return maxProgress;
    }

    public String getDownloadId() {
        return downloadId;
    }

    public boolean isDone() {
        return currentProgress == maxProgress;
    }

}
