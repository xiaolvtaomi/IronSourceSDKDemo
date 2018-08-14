package com.ironsource.sdk.precache;

import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.ironsource.sdk.data.SSAFile;
import com.ironsource.sdk.utils.IronSourceSharedPrefHelper;
import com.ironsource.sdk.utils.IronSourceStorageUtils;
import com.ironsource.sdk.utils.Logger;
import com.ironsource.sdk.utils.SDKUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.concurrent.Callable;


public class DownloadManager {
    private static final String TAG = "DownloadManager";
    public static final String UTF8_CHARSET = "UTF-8";
    public static final int OPERATION_TIMEOUT = 5000;
    static final int MESSAGE_MALFORMED_URL_EXCEPTION = 1004;
    static final int MESSAGE_HTTP_NOT_FOUND = 1005;
    static final int MESSAGE_HTTP_EMPTY_RESPONSE = 1006;
    static final int MESSAGE_EMPTY_URL = 1007;
    static final int MESSAGE_SOCKET_TIMEOUT_EXCEPTION = 1008;
    static final int MESSAGE_IO_EXCEPTION = 1009;
    static final int MESSAGE_URI_SYNTAX_EXCEPTION = 1010;
    static final int MESSAGE_GENERAL_HTTP_ERROR_CODE = 1011;
    static final int MESSAGE_FILE_NOT_FOUND_EXCEPTION = 1018;
    static final int MESSAGE_OUT_OF_MEMORY_EXCEPTION = 1019;
    static final int MESSAGE_NUM_OF_BANNERS_TO_INIT_SUCCESS = 1012;
    static final int MESSAGE_NUM_OF_BANNERS_TO_CACHE = 1013;
    static final int MESSAGE_INIT_BC_FAIL = 1014;
    static final int MESSAGE_ZERO_CAMPAIGNS_TO_INIT_SUCCESS = 1015;
    static final int MESSAGE_FILE_DOWNLOAD_SUCCESS = 1016;
    static final int MESSAGE_FILE_DOWNLOAD_FAIL = 1017;
    static final int MESSAGE_TMP_FILE_RENAME_FAILED = 1020;
    public static final String CAMPAIGNS = "campaigns";
    public static final String GLOBAL_ASSETS = "globalAssets";
    public static final String SETTINGS = "settings";
    protected static final String MALFORMED_URL_EXCEPTION = "malformed url exception";
    protected static final String HTTP_NOT_FOUND = "http not found";
    protected static final String HTTP_EMPTY_RESPONSE = "http empty response";
    protected static final String URI_SYNTAX_EXCEPTION = "uri syntax exception";
    protected static final String HTTP_ERROR_CODE = "http error code";
    protected static final String SOCKET_TIMEOUT_EXCEPTION = "socket timeout exception";
    protected static final String IO_EXCEPTION = "io exception";
    protected static final String FILE_NOT_FOUND_EXCEPTION = "file not found exception";
    protected static final String OUT_OF_MEMORY_EXCEPTION = "out of memory exception";
    protected static final String HTTP_OK = "http ok";
    private static final String TEMP_DIR_FOR_FILES = "temp";
    private static final String TEMP_PREFIX_FOR_FILES = "tmp_";
    private DownloadHandler mDownloadHandler;
    public static final String FILE_ALREADY_EXIST = "file_already_exist";
    public static final String NO_DISK_SPACE = "no_disk_space";
    private static final String UNABLE_TO_CREATE_FOLDER = "unable_to_create_folder";
    public static final String STORAGE_UNAVAILABLE = "sotrage_unavailable";
    public static final String NO_NETWORK_CONNECTION = "no_network_connection";
    private static DownloadManager mDownloadManager;
    private Thread mMobileControllerThread;
    private String mCacheRootDirectory;

    private DownloadManager(String cacheRootDirectory) {
        this.mCacheRootDirectory = cacheRootDirectory;
        this.mDownloadHandler = getDownloadHandler();

        IronSourceStorageUtils.deleteFolder(this.mCacheRootDirectory, "temp");
        IronSourceStorageUtils.makeDir(this.mCacheRootDirectory, "temp");
    }

    public static synchronized DownloadManager getInstance(String cacheRootDirectory) {
        if (mDownloadManager == null) {
            mDownloadManager = new DownloadManager(cacheRootDirectory);
        }
        return mDownloadManager;
    }

    static class DownloadHandler extends Handler {
        OnPreCacheCompletion mListener;

        void setOnPreCacheCompletion(OnPreCacheCompletion listener) {
            if (null == listener) throw new IllegalArgumentException();
            this.mListener = listener;
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1016:
                    this.mListener.onFileDownloadSuccess((SSAFile) msg.obj);
                    break;
                case 1017:
                    this.mListener.onFileDownloadFail((SSAFile) msg.obj);
                    break;
            }

        }


        public void release() {
            this.mListener = null;
        }
    }

    DownloadHandler getDownloadHandler() {
        return new DownloadHandler();
    }


    public void setOnPreCacheCompletion(OnPreCacheCompletion listener) {
        this.mDownloadHandler.setOnPreCacheCompletion(listener);
    }

    public void release() {
        mDownloadManager = null;
        this.mDownloadHandler.release();
        this.mDownloadHandler = null;
    }

    public void downloadFile(SSAFile file) {
        Runnable worker = new SingleFileWorkerThread(file, this.mDownloadHandler, this.mCacheRootDirectory, getTempFilesDirectory());
        new Thread(worker).start();
    }

    public void downloadMobileControllerFile(SSAFile file) {
        Runnable mobileControllerWorker = new SingleFileWorkerThread(file, this.mDownloadHandler, this.mCacheRootDirectory, getTempFilesDirectory());
        this.mMobileControllerThread = new Thread(mobileControllerWorker);
        this.mMobileControllerThread.start();
    }


    public boolean isMobileControllerThreadLive() {
        return (this.mMobileControllerThread != null) && (this.mMobileControllerThread.isAlive());
    }

    public static abstract interface OnPreCacheCompletion {
        public abstract void onFileDownloadSuccess(SSAFile paramSSAFile);

        public abstract void onFileDownloadFail(SSAFile paramSSAFile);
    }

    static class SingleFileWorkerThread implements Runnable {
        private final String mTempFilesDirectory;
        private String mFile;
        private String mPath;
        private String mFileName;
        private long mConnectionRetries;
        private String mCacheRootDirectory;
        Handler mDownloadHandler;

        SingleFileWorkerThread(SSAFile file, Handler downloadHandler, String cacheRootDir, String tempFilesDirectory) {
            this.mFile = file.getFile();
            this.mPath = file.getPath();

            this.mFileName = guessFileName(this.mFile);

            this.mConnectionRetries = getConnectionRetries();
            this.mCacheRootDirectory = cacheRootDir;
            this.mDownloadHandler = downloadHandler;
            this.mTempFilesDirectory = tempFilesDirectory;
        }

        String guessFileName(String file) {
            return SDKUtils.getFileName(this.mFile);
        }

        FileWorkerThread getFileWorkerThread(String url, String directory, String fileName, long connectionRetries, String tmpFilesDirectory) {
            return new FileWorkerThread(url, directory, fileName, connectionRetries, tmpFilesDirectory);
        }


        Message getMessage() {
            return new Message();
        }

        String makeDir(String cacheRootDirectory, String directory) {
            return IronSourceStorageUtils.makeDir(cacheRootDirectory, directory);
        }


        public void run() {
            SSAFile ssaFile = new SSAFile(this.mFileName, this.mPath);

            Message msg = getMessage();
            msg.obj = ssaFile;
            String folderName = makeDir(this.mCacheRootDirectory, this.mPath);

            if (folderName == null) {
                msg.what = 1017;
                ssaFile.setErrMsg("unable_to_create_folder");
                this.mDownloadHandler.sendMessage(msg);
            } else {
                FileWorkerThread fileWorkerThread = getFileWorkerThread(this.mFile, folderName, ssaFile


                        .getFile(), this.mConnectionRetries, this.mTempFilesDirectory);


                Result results = fileWorkerThread.call();
                int code = results.responseCode;


                switch (code) {
                    case 404:
                    case 1004:
                    case 1005:
                    case 1006:
                    case 1008:
                    case 1009:
                    case 1010:
                    case 1011:
                    case 1018:
                    case 1019:
                        String errMsg = convertErrorCodeToMessage(code);

                        msg.what = 1017;
                        ssaFile.setErrMsg(errMsg);
                        this.mDownloadHandler.sendMessage(msg);
                        break;

                    case 200:
                        msg.what = 1016;
                        this.mDownloadHandler.sendMessage(msg);
                        break;
                }

            }
        }


        String convertErrorCodeToMessage(int errorCode) {
            String errMsg = "not defined message for " + errorCode;
            switch (errorCode) {
                case 1004:
                    errMsg = "malformed url exception";
                    break;
                case 404:
                case 1005:
                    errMsg = "http not found";
                    break;
                case 1006:
                    errMsg = "http empty response";
                    break;
                case 1010:
                    errMsg = "uri syntax exception";
                    break;
                case 1011:
                    errMsg = "http error code";
                    break;
                case 1018:
                    errMsg = "file not found exception";
                    break;
                case 1008:
                    errMsg = "socket timeout exception";
                    break;
                case 1009:
                    errMsg = "io exception";
                    break;
                case 1019:
                    errMsg = "out of memory exception";
            }

            return errMsg;
        }

        public long getConnectionRetries() {
            return Long.parseLong(IronSourceSharedPrefHelper.getSupersonicPrefHelper().getConnectionRetries());
        }
    }


    static class FileWorkerThread
            implements Callable<Result> {
        private String mFileUrl;


        private String mDirectory;


        private String mFileName;


        private long mConnectionRetries;


        private String mTmpFilesDirectory;


        public FileWorkerThread(String url, String directory, String fileName, long connectionRetries, String tmpFilesDirectory) {
            this.mFileUrl = url;
            this.mDirectory = directory;
            this.mFileName = fileName;
            this.mConnectionRetries = connectionRetries;
            this.mTmpFilesDirectory = tmpFilesDirectory;
        }

        int saveFile(byte[] data, String destFileName) throws Exception {
            return IronSourceStorageUtils.saveFile(data, destFileName);
        }

        boolean renameFile(String fromName, String toName) throws Exception {
            return IronSourceStorageUtils.renameFile(fromName, toName);
        }

        byte[] getBytes(InputStream in) throws IOException {
            return DownloadManager.getBytes(in);
        }


        public Result call() {
            Result results = null;

            if (this.mConnectionRetries == 0L) {
                this.mConnectionRetries = 1L;
            }


            for (int tryIndex = 0; tryIndex < this.mConnectionRetries; tryIndex++) {
                results = downloadContent(this.mFileUrl, tryIndex);
                int responseCode = results.responseCode;

                if ((responseCode != 1008) && (responseCode != 1009)) {
                    break;
                }
            }


            if ((null != results) && (null != results.body)) {
                String origFileName = this.mDirectory + File.separator + this.mFileName;
                String tmpFileName = this.mTmpFilesDirectory + File.separator + "tmp_" + this.mFileName;
                try {
                    int totalBytesRead = saveFile(results.body, tmpFileName);
                    if (totalBytesRead == 0) {
                        results.responseCode = 1006;
                    } else if (!renameFile(tmpFileName, origFileName)) {
                        results.responseCode = 1020;
                    }
                } catch (FileNotFoundException e) {
                    results.responseCode = 1018;
                } catch (Exception e) {
                    if (!TextUtils.isEmpty(e.getMessage())) {
                        Logger.i("DownloadManager", e.getMessage());
                    }
                    results.responseCode = 1009;
                } catch (Error err) {
                    if (!TextUtils.isEmpty(err.getMessage())) {
                        Logger.i("DownloadManager", err.getMessage());
                    }
                    results.responseCode = 1019;
                }
            }

            return results;
        }


        Result downloadContent(String url, int tryNumber) {
            Result results = new Result();

            HttpURLConnection connection = null;

            int responseCode = 0;

            if (TextUtils.isEmpty(url)) {
                results.url = url;
                results.responseCode = 1007;
                return results;
            }
            InputStream is = null;
            try {
                URL mUrl = new URL(url);


                mUrl.toURI();

                connection = (HttpURLConnection) mUrl.openConnection();
                connection.setRequestMethod("GET");

                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);

                connection.connect();
                responseCode = connection.getResponseCode();
                if ((responseCode < 200) || (responseCode >= 400)) {
                    responseCode = 1011;
                } else {
                    is = connection.getInputStream();
                    results.body = getBytes(is);
                }

                if (responseCode != 200) {
                    Logger.i("DownloadManager", " RESPONSE CODE: " + responseCode + " URL: " + url + " ATTEMPT: " + tryNumber);
                }


            } catch (MalformedURLException e) {
                responseCode = 1004;
            } catch (URISyntaxException e) {
                responseCode = 1010;
            } catch (SocketTimeoutException e) {
                responseCode = 1008;
            } catch (FileNotFoundException e) {
                responseCode = 1018;
            } catch (Exception e) {
                if (!TextUtils.isEmpty(e.getMessage())) {
                    Logger.i("DownloadManager", e.getMessage());
                }
                responseCode = 1009;
            } catch (Error err) {
                responseCode = 1019;
                if (!TextUtils.isEmpty(err.getMessage())) {
                    Logger.i("DownloadManager", err.getMessage());
                }
            } finally {
                try {
                    if (is != null) {
                        is.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (connection != null) {
                    connection.disconnect();
                }

                results.url = url;
                results.responseCode = responseCode;
            }

            return results;
        }
    }

    String getTempFilesDirectory() {
        return this.mCacheRootDirectory + File.separator + "temp";
    }

    static byte[] getBytes(InputStream in) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        byte[] data = new byte[' '];
        int bytesRead;
        while ((bytesRead = in.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, bytesRead);
        }
        buffer.flush();
        return buffer.toByteArray();
    }

    static class Result {
        public String url;
        int responseCode;
        byte[] body;
    }
}


/* Location:              /Users/lml/Downloads/mediationsdk-6.7.10.jar!/com/ironsource/sdk/precache/DownloadManager.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */