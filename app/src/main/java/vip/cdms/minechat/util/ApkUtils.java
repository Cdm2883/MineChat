package vip.cdms.minechat.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import vip.cdms.minechat.protocol.util.OkHttpJsonCallback;
import vip.cdms.minechat.protocol.util.StorageUtils;

public class ApkUtils {
    public static void extract(PackageInfo packageInfo, File out) throws IOException {
        File apk = new File(packageInfo.applicationInfo.sourceDir);
        Files.copy(apk.toPath(), out.toPath());
    }

    public static void unzip(File zip, File out, String folderName, String... keep) throws IOException {
        if (!out.exists() && !out.mkdirs()) throw new IOException("Unable to create out directory");

        ZipFile zipFile = new ZipFile(zip);
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            String entryPathName = entry.getName();
            if (!entryPathName.startsWith(folderName)) continue;

            if (
                    keep != null && keep.length > 0
                    && !Arrays.asList(keep).contains(entryPathName.split("\\.")[entryPathName.split("\\.").length - 1])
            ) continue;

            File entryFile = new File(out, entryPathName.replace(folderName, ""));
            if (entryFile.getParentFile() != null && !entryFile.getParentFile().exists() && !entryFile.getParentFile().mkdirs())
                throw new IOException("Unable to create out directory");

            InputStream inputStream = zipFile.getInputStream(entry);
            FileOutputStream outputStream = new FileOutputStream(entryFile);

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1)
                outputStream.write(buffer, 0, bytesRead);

            outputStream.close();
            inputStream.close();
        }
        zipFile.close();
    }
}
