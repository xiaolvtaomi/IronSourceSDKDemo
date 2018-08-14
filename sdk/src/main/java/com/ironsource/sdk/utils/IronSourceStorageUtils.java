package com.ironsource.sdk.utils;

import android.content.Context;

import com.ironsource.environment.DeviceStatus;
import com.ironsource.sdk.data.SSAEnums;
import com.ironsource.sdk.data.SSAEnums.ProductType;
import com.ironsource.sdk.data.SSAFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class IronSourceStorageUtils {
    private static final String SSA_DIRECTORY_NAME = "supersonicads";

    public static String initializeCacheDirectory(Context context) {
        createRootDirectory(context);
        String result = refreshRootDirectory(context);
        return result;
    }


    private static String refreshRootDirectory(Context context) {
        String storedVerison = IronSourceSharedPrefHelper.getSupersonicPrefHelper().getCurrentSDKVersion();

        String sdkVer = DeviceProperties.getSupersonicSdkVersion();
        String cacheDirectoryPath;
        if (!storedVerison.equalsIgnoreCase(sdkVer)) {

            IronSourceSharedPrefHelper.getSupersonicPrefHelper().setCurrentSDKVersion(sdkVer);


            File cacheDir = DeviceStatus.getExternalCacheDir(context);
            if (cacheDir != null) {
                deleteAllFiles(cacheDir.getAbsolutePath() + File.separator + "supersonicads" + File.separator);
            }

            deleteAllFiles(DeviceStatus.getInternalCacheDirPath(context) + File.separator + "supersonicads" + File.separator);


            cacheDirectoryPath = createRootDirectory(context);
        } else {
            cacheDirectoryPath = getDiskCacheDir(context, "supersonicads").getPath();
        }

        return cacheDirectoryPath;
    }


    private static void deleteAllFiles(String path) {
        File root = new File(path);
        File[] files = root.listFiles();

        if (files == null) {
            return;
        }
        for (File file : files) {
            if (file.isDirectory()) {
                deleteAllFiles(file.getAbsolutePath());
                file.delete();
            } else {
                file.delete();
            }
        }
    }


    private static File getDiskCacheDir(Context context, String cacheDirName) {
        if (SDKUtils.isExternalStorageAvailable()) {
            File externalCacheDir = DeviceStatus.getExternalCacheDir(context);

            if (externalCacheDir != null) {
                String cachePath = externalCacheDir.getPath();
                return new File(cachePath + File.separator + cacheDirName);
            }
            String cachePath = DeviceStatus.getInternalCacheDirPath(context);
            return new File(cachePath + File.separator + cacheDirName);
        }


        String cachePath = DeviceStatus.getInternalCacheDirPath(context);
        return new File(cachePath + File.separator + cacheDirName);
    }


    private static String createRootDirectory(Context context) {
        File rootDirectory = getDiskCacheDir(context, "supersonicads");

        if (!rootDirectory.exists()) {
            rootDirectory.mkdir();
        }

        return rootDirectory.getPath();
    }


    public static String makeDir(String cacheRootDirectory, String directory) {
        File dir = new File(cacheRootDirectory, directory);

        if (!dir.exists()) {
            boolean isCreated = dir.mkdirs();
            if (!isCreated) {
                return null;
            }
        }
        return dir.getPath();
    }


    public static synchronized boolean deleteFile(String rootCacheDir, String filePath, String fileName) {
        File dir = new File(rootCacheDir, filePath);

        if (!dir.exists()) {
            return false;
        }

        File[] files = dir.listFiles();

        if (files == null) {
            return false;
        }

        for (File entry : files) {
            if ((entry.isFile()) &&
                    (entry.getName().equalsIgnoreCase(fileName))) {
                return entry.delete();
            }
        }


        return false;
    }


    public static synchronized boolean isFileCached(String rootDirPath, SSAFile ssaFile) {
        File dir = new File(rootDirPath, ssaFile.getPath());

        if (dir.listFiles() != null) {
            for (File entry : dir.listFiles()) {
                if ((entry.isFile()) &&
                        (entry.getName().equalsIgnoreCase(SDKUtils.getFileName(ssaFile.getFile())))) {
                    return true;
                }
            }
        }


        return false;
    }


    public static boolean isPathExist(String cachRootPath, String path) {
        File file = new File(cachRootPath, path);

        return file.exists();
    }


    public static synchronized boolean deleteFolder(String cacheRootDir, String path) {
        File folder = new File(cacheRootDir, path);
        return (deleteFolderContentRecursive(folder)) && (folder.delete());
    }


    private static boolean deleteFolderContentRecursive(File dir) {
        File[] files = dir.listFiles();
        boolean success = true;
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    success &= deleteFolderContentRecursive(file);
                }
                if (!file.delete()) {
                    success = false;
                }
            }
        }

        return success;
    }


    public static String getCachedFilesMap(String cacheRootPath, String path) {
        JSONObject jsnObj = buildFilesMap(cacheRootPath, path);
        try {
            jsnObj.put("path", path);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsnObj.toString();
    }


    private static JSONObject buildFilesMap(String cacheRootPath, String path) {
        File dir = new File(cacheRootPath, path);
        JSONObject jsnObj = new JSONObject();
        File[] fileList = dir.listFiles();

        if (fileList != null) {
            for (File entry : fileList) {
                try {
                    Object obj = looping(entry);

                    if ((obj instanceof JSONArray)) {
                        jsnObj.put("files", looping(entry));
                    } else if ((obj instanceof JSONObject)) {
                        jsnObj.put(entry.getName(), looping(entry));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    new IronSourceAsyncHttpRequestTask().execute(new String[]{"https://www.supersonicads.com/mobile/sdk5/log?method=" + e.getStackTrace()[0].getMethodName()});
                }
            }
        }

        return jsnObj;
    }


    private static Object looping(File file) {
        JSONObject arr = new JSONObject();
        JSONArray tempArr = new JSONArray();
        try {
            if (file.isFile()) {
                tempArr.put(file.getName());
                return tempArr;
            }

            for (File fileEntry : file.listFiles()) {
                if (fileEntry.isDirectory()) {
                    arr.put(fileEntry.getName(), looping(fileEntry));
                } else {
                    tempArr.put(fileEntry.getName());
                    arr.put("files", tempArr);
                }
            }


            if (file.isDirectory()) {
                String lastUpdate = IronSourceSharedPrefHelper.getSupersonicPrefHelper().getCampaignLastUpdate(file.getName());
                if (lastUpdate != null) {
                    arr.put("lastUpdateTime", lastUpdate);
                }
            }


            String product = file.getName().toLowerCase();
            SSAEnums.ProductType type = null;
            if (product.startsWith(SSAEnums.ProductType.RewardedVideo.toString().toLowerCase())) {
                type = SSAEnums.ProductType.RewardedVideo;
            } else if (product.startsWith(SSAEnums.ProductType.OfferWall.toString().toLowerCase())) {
                type = SSAEnums.ProductType.OfferWall;
            } else if (product.startsWith(SSAEnums.ProductType.Interstitial.toString().toLowerCase())) {
                type = SSAEnums.ProductType.Interstitial;
            }

            if (type != null) {
                arr.put(SDKUtils.encodeString("applicationUserId"),
                        SDKUtils.encodeString(
                                IronSourceSharedPrefHelper.getSupersonicPrefHelper().getUniqueId(type)));


                arr.put(SDKUtils.encodeString("applicationKey"),
                        SDKUtils.encodeString(
                                IronSourceSharedPrefHelper.getSupersonicPrefHelper().getApplicationKey(type)));
            }

        } catch (JSONException e) {
            e.printStackTrace();
            new IronSourceAsyncHttpRequestTask().execute(new String[]{"https://www.supersonicads.com/mobile/sdk5/log?method=" + e.getStackTrace()[0].getMethodName()});
        }
        return arr;
    }


    public static boolean renameFile(String fromName, String toName)
            throws Exception {
        File srcFile = new File(fromName);
        File destFile = new File(toName);
        return srcFile.renameTo(destFile);
    }


    public static int saveFile(byte[] data, String destFileName)
            throws Exception {
        int totalBytesRead = 0;
        File tmpOutputFile = new File(destFileName);
        FileOutputStream fos = new FileOutputStream(tmpOutputFile);
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        try {
            byte[] buffer = new byte[102400];
            int bytesRead;
            while ((bytesRead = bais.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
                totalBytesRead += bytesRead;
            }
        } finally {
            fos.close();
            bais.close();
        }

        return totalBytesRead;
    }
}


/* Location:              /Users/lml/Downloads/mediationsdk-6.7.10.jar!/com/ironsource/sdk/utils/IronSourceStorageUtils.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */