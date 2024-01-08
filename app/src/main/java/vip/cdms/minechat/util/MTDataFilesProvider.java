package vip.cdms.minechat.util;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsProvider;
import android.system.ErrnoException;
import android.system.Os;
import android.system.StructStat;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

/**
 * 为了实现访问data和支持MT管理器提供的功能,
 * 遂使用Procyon对MT管理器的“注入文件提供器”功能中对软件注入的字节码进行了反编译,
 * 并对反编译结果进行了修正, 精简, 美化和修改.
 * 虽然反编译非常不道德, 但是真的不想再写一遍了qwq
 */
// 未来还将用于适配nodejs (用户可以更加方便的管理nodejs文件), Android不允许外部存储空间存在软链接
public class MTDataFilesProvider extends DocumentsProvider {
    public static final String[] ROOT_COLUMNS = new String[] { "root_id", "mime_types", "flags", "icon", "title", "summary", "document_id" };
    public static final String[] DOCUMENT_COLUMNS = new String[] { "document_id", "mime_type", "_display_name", "last_modified", "flags", "_size", "mt_extras" };
    public String packageName;
    public File appFilesParentDir;
    public File appDataDir;
    public File appObbDir;

    public MTDataFilesProvider() {
        super();
    }

    public static boolean deleteFile(File file) {
        RETURN: {
            if (!file.isDirectory()) break RETURN;

            boolean isSymbolicLink;
            while (true) {
                try {
                    isSymbolicLink = (Os.lstat(file.getPath()).st_mode & 0xF000) == 0xA000;
                } catch (ErrnoException ex) {
                    ex.printStackTrace();
                    continue;
                }
                break;
            }
            if (isSymbolicLink) break RETURN;

            File[] listFiles = file.listFiles();
            if (listFiles == null) break RETURN;

            for (File listFile : listFiles) {
                if (!deleteFile(listFile))
                    return false;
            }
        }
        return file.delete();
    }

    public static String getMimeType(File file) {
        if (file.isDirectory()) return "vnd.android.document/directory";
        String name = file.getName();
        int lastIndex = name.lastIndexOf('.');
        if (lastIndex >= 0) {
            String mimeTypeFromExtension = MimeTypeMap.getSingleton().getMimeTypeFromExtension(name.substring(lastIndex + 1).toLowerCase());
            if (mimeTypeFromExtension != null) return mimeTypeFromExtension;
        }
        return "application/octet-stream";
    }

    @Override
    public void attachInfo(Context context, ProviderInfo providerInfo) {
        super.attachInfo(context, providerInfo);

        packageName = context.getPackageName();
        appFilesParentDir = context.getFilesDir().getParentFile();

        File externalStorageDir = Environment.getExternalStorageDirectory();
        appDataDir = new File(externalStorageDir, "Android/data/" + packageName);
        appObbDir = new File(externalStorageDir, "Android/obb/" + packageName);
    }


    public File findFile(String path, boolean checkExistence) throws FileNotFoundException {
        if (!path.startsWith(packageName))
            throw new FileNotFoundException(path.concat(" not found"));

        String remainingPath;
        String cleanedPath = remainingPath = path.substring(packageName.length());
        if (cleanedPath.startsWith("/"))
            remainingPath = cleanedPath.substring(1);

        if (remainingPath.isEmpty()) return null;

        int index = remainingPath.indexOf('/');
        String subPath;
        String restOfPath;
        if (index == -1) {
            subPath = "";
            restOfPath = remainingPath;
        } else {
            restOfPath = remainingPath.substring(0, index);
            subPath = remainingPath.substring(index + 1);
        }

        File targetFile = null;
        if (restOfPath.equalsIgnoreCase("data")) {
            targetFile = new File(appFilesParentDir, subPath);
        } else if (restOfPath.equalsIgnoreCase("android_data")) {
            targetFile = new File(appDataDir, subPath);
        } else if (restOfPath.equalsIgnoreCase("android_obb")) {
            targetFile = new File(appObbDir, subPath);
        }

        if (targetFile != null) {
            if (checkExistence) {
                try {
                    Os.lstat(targetFile.getPath());
                } catch (Exception ex) {
                    throw new FileNotFoundException(path.concat(" not found"));
                }
            }
            return targetFile;
        }
        throw new FileNotFoundException(path.concat(" not found"));
    }

    @Override
    public Bundle call(String method, String arg, Bundle extras) {
        Bundle resultBundle = super.call(method, arg, extras);
        if (resultBundle != null) return resultBundle;

        // 适配MT管理器的自定义方法
        if (!method.startsWith("mt:")) return null;

        Bundle responseBundle = new Bundle();
        try {
            List<String> pathSegments = ((Uri) Objects.requireNonNull(extras.getParcelable("uri"))).getPathSegments();
            String targetFileName = pathSegments.get(pathSegments.size() >= 4 ? 3 : 1);

            int hashCode = method.hashCode();
            int actionType = switch (hashCode) {
                case 0x6621b52e  -> method.equals("mt:setLastModified") ? 0 : -1;
                case -1645162251 -> method.equals("mt:setPermissions")  ? 1 : -1;
                case 0x0cc82212  -> method.equals("mt:createSymlink")   ? 2 : -1;
                default          -> -1;
            };

            File targetFile;
            switch (actionType) {
                case 0 -> {  // mt:setLastModified
                    targetFile = findFile(targetFileName, true);
                    if (targetFile == null) break;
                    responseBundle.putBoolean("result", targetFile.setLastModified(extras.getLong("time")));
                }
                case 1 -> {  // mt:setPermissions
                    targetFile = findFile(targetFileName, true);
                    if (targetFile == null) break;
                    int permissions = extras.getInt("permissions");
                    try {
                        Os.chmod(targetFile.getPath(), permissions);
                        responseBundle.putBoolean("result", true);
                    } catch (ErrnoException ex) {
                        responseBundle.putBoolean("result", false);
                        responseBundle.putString("message", ex.getMessage());
                    }
                }
                case 2 -> {  // mt:createSymlink
                    targetFile = findFile(targetFileName, false);
                    if (targetFile == null) break;
                    String symlinkPath = extras.getString("path");
                    try {
                        Os.symlink(symlinkPath, targetFile.getPath());
                        responseBundle.putBoolean("result", true);
                    } catch (ErrnoException ex) {
                        responseBundle.putBoolean("result", false);
                        responseBundle.putString("message", ex.getMessage());
                    }
                }
                default -> {
                    responseBundle.putBoolean("result", false);
                    responseBundle.putString("message", "Unsupported method: " + method);
                }
            }
        } catch (Exception e) {
            responseBundle.putBoolean("result", false);
            responseBundle.putString("message", e.toString());
        }
        return responseBundle;
    }

    @Override
    public String createDocument(String parentDocumentId, String mimeType, String displayName) throws FileNotFoundException {
        File parentDir = findFile(parentDocumentId, true);
        if (parentDir != null) {
            File file = new File(parentDir, displayName);
            StringBuilder sb;
            for (int n = 2; file.exists(); file = new File(parentDir, sb.toString()), ++n) {
                sb = new StringBuilder()
                        .append(displayName).append(" (").append(n).append(")");
            }

            try {
                if (
                        "vnd.android.document/directory".equals(mimeType) ?
                                file.mkdir()
                                :
                                file.createNewFile()
                ) {
                    StringBuilder sb2 = new StringBuilder();
                    if (parentDocumentId.endsWith("/")) {
                        sb2.append(parentDocumentId)
                                .append(file.getName());
                    } else {
                        sb2.append(parentDocumentId)
                                .append("/")
                                .append(file.getName());
                    }
                    mimeType = sb2.toString();
                    return mimeType;
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        throw new FileNotFoundException("Failed to create document in " + parentDocumentId + " with name " + displayName);
    }

    public void buildCursor(MatrixCursor matrixCursor, String documentId, File file) throws FileNotFoundException {
        File targetFile = file;
        if (file == null) targetFile = findFile(documentId, true);

        if (targetFile == null) {
            matrixCursor.newRow()
                    .add("document_id", packageName)
                    .add("_display_name", packageName)
                    .add("_size", 0L)
                    .add("mime_type", "vnd.android.document/directory")
                    .add("last_modified", 0)
                    .add("flags", 0);
            return;
        }

        int flags = targetFile.canWrite() ? 8 : 0;
        int finalFlags = flags;
        if (Objects.requireNonNull(targetFile.getParentFile()).canWrite())
            finalFlags = (flags | 0x4 | 0x40);

        String path = targetFile.getPath();
        String name;
        int extrasType = 0;
        if (path.equals(appFilesParentDir.getPath())) {
            name = "data";
        } else if (path.equals(appDataDir.getPath())) {
            name = "android_data";
        } else if (path.equals(appObbDir.getPath())) {
            name = "android_obb";
        } else {
            name = targetFile.getName();
            extrasType = 1;
        }
        
        MatrixCursor.RowBuilder row = matrixCursor.newRow()
                .add("document_id", documentId)
                .add("_display_name", name)
                .add("_size", targetFile.length())
                .add("mime_type", getMimeType(targetFile))
                .add("last_modified", targetFile.lastModified())
                .add("flags", finalFlags)
                .add("mt_path", targetFile.getAbsolutePath());
        
        if (extrasType == 0) return;
        try {
            StringBuilder extrasBuilder = new StringBuilder();
            StructStat lstat = Os.lstat(path);
            extrasBuilder.append(lstat.st_mode)
                    .append("|")
                    .append(lstat.st_uid)
                    .append("|")
                    .append(lstat.st_gid);
            if ((lstat.st_mode & 0xF000) == 0xA000)
                extrasBuilder.append("|")
                        .append(Os.readlink(path));
            row.add("mt_extras", extrasBuilder.toString());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void deleteDocument(String documentId) throws FileNotFoundException {
        File targetFile = findFile(documentId, true);
        if (targetFile != null && deleteFile(targetFile)) return;
        throw new FileNotFoundException("Failed to delete document ".concat(documentId));
    }

    @Override
    public String getDocumentType(String documentId) throws FileNotFoundException {
        File targetFile = findFile(documentId, true);
        return targetFile == null ? "vnd.android.document/directory" : getMimeType(targetFile);
    }

    @Override
    public boolean isChildDocument(String parentDocumentId, String documentId) {
        return documentId.startsWith(parentDocumentId);
    }

    @Override
    public String moveDocument(String sourceDocumentId, String sourceParentDocumentId, String targetParentDocumentId) throws FileNotFoundException {
        File sourceFile = findFile(sourceDocumentId, true);
        File targetParentDir = findFile(targetParentDocumentId, true);
        if (sourceFile != null && targetParentDir != null) {
            File targetFile = new File(targetParentDir, sourceFile.getName());
            if (!targetFile.exists() && sourceFile.renameTo(targetFile))
                return targetParentDocumentId.endsWith("/") ? 
                        targetParentDocumentId + targetFile.getName() 
                        : 
                        targetParentDocumentId + "/" + targetFile.getName();
        }
        throw new FileNotFoundException("Filed to move document " + sourceDocumentId + " to " + targetParentDocumentId);
    }

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public ParcelFileDescriptor openDocument(String documentId, String mode, CancellationSignal signal) throws FileNotFoundException {
        File targetFile = this.findFile(documentId, false);
        if (targetFile != null)
            return ParcelFileDescriptor.open(targetFile, ParcelFileDescriptor.parseMode(mode));
        throw new FileNotFoundException(documentId.concat(" not found"));
    }

    @Override
    public Cursor queryChildDocuments(String parentDocumentId, String[] projection, String sortOrder) throws FileNotFoundException {
        String parentPath = parentDocumentId.endsWith("/") ?
                parentDocumentId.substring(0, parentDocumentId.length() - 1)
                :
                parentDocumentId;

        if (projection == null) projection = DOCUMENT_COLUMNS;

        MatrixCursor matrixCursor = new MatrixCursor(projection);
        File parentFile = findFile(parentPath, true);
        if (parentFile == null) {
            buildCursor(matrixCursor, parentPath.concat("/data"), appFilesParentDir);
            if (appDataDir.exists())
                buildCursor(matrixCursor, parentPath.concat("/android_data"), appDataDir);
            if (appObbDir.exists())
                buildCursor(matrixCursor, parentPath.concat("/android_obb"), appObbDir);
        } else {
            File[] childFiles = parentFile.listFiles();
            if (childFiles != null) {
                for (File childFile : childFiles)
                    buildCursor(matrixCursor, parentPath + "/" + childFile.getName(), childFile);
            }
        }
        return matrixCursor;
    }

    @Override
    public Cursor queryDocument(String documentId, String[] projection) throws FileNotFoundException {
        if (projection == null) projection = DOCUMENT_COLUMNS;
        MatrixCursor matrixCursor = new MatrixCursor(projection);
        buildCursor(matrixCursor, documentId, null);
        return matrixCursor;
    }

    @Override
    public Cursor queryRoots(String[] projection) {
        ApplicationInfo applicationInfo = Objects.requireNonNull(getContext()).getApplicationInfo();
        String string = applicationInfo.loadLabel(getContext().getPackageManager()).toString();
        if (projection == null) projection = ROOT_COLUMNS;
        MatrixCursor matrixCursor = new MatrixCursor(projection);
        matrixCursor.newRow()
                .add("root_id", packageName)
                .add("document_id", packageName)
                .add("summary", packageName)
                .add("flags", 17)
                .add("title", string)
                .add("mime_types", "*/*")
                .add("icon", applicationInfo.icon);
        return matrixCursor;
    }

    @Override
    public void removeDocument(String documentId, String parentDocumentId) throws FileNotFoundException {
        deleteDocument(documentId);
    }

    @Override
    public String renameDocument(String documentId, String displayName) throws FileNotFoundException {
        File sourceFile = findFile(documentId, true);
        if (sourceFile != null && sourceFile.renameTo(new File(sourceFile.getParentFile(), displayName))) {
            int lastIndex = documentId.lastIndexOf('/', documentId.length() - 2);
            return documentId.substring(0, lastIndex) + "/" + displayName;
        }
        throw new FileNotFoundException("Failed to rename document " + documentId + " to " + displayName);
    }
}

