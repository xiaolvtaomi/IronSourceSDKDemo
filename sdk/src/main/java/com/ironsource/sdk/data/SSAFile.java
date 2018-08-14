package com.ironsource.sdk.data;

public class SSAFile extends SSAObj {
    private String FILE = "file";
    private String PATH = "path";
    private String LAST_UPDATE_TIME = "lastUpdateTime";
    private String mFile;
    private String mPath;
    private String mErrMsg;
    private String mLastUpdateTime;

    public SSAFile(String value) {
        super(value);

        if (containsKey(this.FILE)) {
            setFile(getString(this.FILE));
        }

        if (containsKey(this.PATH)) {
            setPath(getString(this.PATH));
        }

        if (containsKey(this.LAST_UPDATE_TIME)) {
            setLastUpdateTime(getString(this.LAST_UPDATE_TIME));
        }
    }

    public SSAFile(String file, String path) {
        setFile(file);
        setPath(path);
    }

    public String getFile() {
        return this.mFile;
    }

    private void setFile(String file) {
        this.mFile = file;
    }

    private void setPath(String path) {
        this.mPath = path;
    }

    public String getPath() {
        return this.mPath;
    }

    public void setErrMsg(String errMsg) {
        this.mErrMsg = errMsg;
    }

    public String getErrMsg() {
        return this.mErrMsg;
    }

    public void setLastUpdateTime(String lastUpdateTime) {
        this.mLastUpdateTime = lastUpdateTime;
    }

    public String getLastUpdateTime() {
        return this.mLastUpdateTime;
    }
}


/* Location:              /Users/lml/Downloads/mediationsdk-6.7.10.jar!/com/ironsource/sdk/data/SSAFile.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */