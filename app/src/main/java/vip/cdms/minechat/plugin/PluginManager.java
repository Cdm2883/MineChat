package vip.cdms.minechat.plugin;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.JsonObject;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import dalvik.system.DexClassLoader;
import dalvik.system.DexFile;
import vip.cdms.mcoreui.util.ReflectionUtils;
import vip.cdms.minechat.protocol.app.Storage;
import vip.cdms.minechat.protocol.plugin.Plugin;
import vip.cdms.minechat.protocol.plugin.PluginMain;
import vip.cdms.minechat.protocol.plugin.ServiceExtension;
import vip.cdms.minechat.protocol.plugin.ServicePool;
import vip.cdms.minechat.protocol.plugin.builtin.extension.ImageSender;
import vip.cdms.minechat.protocol.plugin.builtin.extension.MineChatPlus;
import vip.cdms.minechat.protocol.plugin.builtin.protocol.CloudburstMC_Protocol;
import vip.cdms.minechat.protocol.plugin.builtin.protocol.DebugConsole;
import vip.cdms.minechat.protocol.plugin.builtin.protocol.GeyserMC_MCProtocolLib;
import vip.cdms.minechat.protocol.plugin.builtin.protocol.ProtocolServerPlugin;
import vip.cdms.minechat.protocol.plugin.builtin.service.NodejsService;
import vip.cdms.minechat.protocol.plugin.builtin.service.TermuxStarter;
import vip.cdms.minechat.protocol.util.ExceptionHandler;
import vip.cdms.minechat.protocol.util.StorageUtils;

public class PluginManager {
    public static class PluginConfig implements Comparable<PluginConfig> {
        private boolean enabled;
        private int priority;
        private Class<? extends Plugin> clazz;

        public boolean isEnabled() {
            return enabled;
        }
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;

            if (!pluginConfigs.has("enabled"))
                pluginConfigs.add("enabled", new JsonObject());
            JsonObject enables = pluginConfigs.get("enabled").getAsJsonObject();
            enables.addProperty(getClazzName(), enabled);
            StorageUtils.write(Storage.FILES_DIR, "plugins", pluginConfigs);
        }

        public int getPriority() {
            return priority;
        }
        public void setPriority(int priority) {
            this.priority = priority;

            if (!pluginConfigs.has("priority"))
                pluginConfigs.add("priority", new JsonObject());
            JsonObject priorities = pluginConfigs.get("priority").getAsJsonObject();
            priorities.addProperty(getClazzName(), priority);
            StorageUtils.write(Storage.FILES_DIR, "plugins", pluginConfigs);
        }

        public Class<? extends Plugin> getClazz() {
            return clazz;
        }
        public String getClazzName() {
            return getClazz().getName();
        }
        public boolean isAssignableFrom(Class<? extends Plugin> clazz) {
            return clazz.isAssignableFrom(getClazz());
        }
        public Plugin createInstance(Context context, ExceptionHandler exceptionHandler) {
            return PluginManager.createInstance(context, getClazz(), exceptionHandler);
        }
        public Plugin createInstance(Context context) {
            return PluginManager.createInstance(context, getClazz());
        }
        /** @noinspection unchecked*/
        public <P extends Plugin> P createInstance(Class<P> clazz, Context context, ExceptionHandler exceptionHandler) {
            return isAssignableFrom(clazz) ? (P) createInstance(context, exceptionHandler) : null;
        }
        /** @noinspection unchecked*/
        public <P extends Plugin> P createInstance(Class<? extends Plugin> clazz, Context context) {
            return isAssignableFrom(clazz) ? (P) createInstance(context) : null;
        }

        @Override
        public int hashCode() {
            return getClazz().getName().hashCode();
        }
        @Override
        public int compareTo(PluginConfig o) {
            return o.getPriority() - getPriority();
        }
    }
    public static final ArrayList<PluginConfig> plugins = new ArrayList<>();
    private static JsonObject pluginConfigs;

    public static ArrayList<PluginConfig> getPlugins() {
        Collections.sort(plugins);
        return plugins;
    }
    public static ArrayList<PluginConfig> getPlugins(Class<? extends Plugin> clazz) {
        ArrayList<PluginConfig> plugins = new ArrayList<>();
        for (PluginConfig plugin : getPlugins())
            if (plugin.isAssignableFrom(clazz))
                plugins.add(plugin);
        return plugins;
    }
    public static PluginConfig getPlugin(Class<? extends Plugin> clazz) {
        for (PluginConfig plugin : getPlugins())
            if (plugin.getClazzName().equals(clazz.getName()))
                return plugin;
        return null;
    }

    public static SharedPreferences getSharedPreferences(Context context, Class<?> clazz) {
        return context.getSharedPreferences(clazz.getName(), Context.MODE_PRIVATE);
    }

    /** @noinspection unchecked*/
    public static <P extends Plugin> P createInstance(Context context, Class<P> clazz, ExceptionHandler exceptionHandler) {
        boolean isServiceExtension = ServiceExtension.class.isAssignableFrom(clazz);
        if (isServiceExtension) {
            ServiceExtension serviceExtension = ServicePool.getInstance(clazz.getName());
            if (serviceExtension != null) return (P) serviceExtension;
        }

        try {
            Constructor<P> constructor = clazz.getDeclaredConstructor(isServiceExtension ? Application.class : Activity.class, SharedPreferences.class, ExceptionHandler.class);
            constructor.setAccessible(true);
            P plugin = constructor.newInstance(
                    isServiceExtension ?
                            (context instanceof Activity activity ? activity.getApplication() : context)
                            :
                            context,
                    getSharedPreferences(context, clazz),
                    exceptionHandler
            );
            if (isServiceExtension) ServicePool.putInstance((ServiceExtension) plugin);
            return plugin;
        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException |
                 InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
    public static <P extends Plugin> P createInstance(Context context, Class<P> clazz) {
        return createInstance(context, clazz, new ExceptionHandler(e ->
                ExceptionHandler.printStackTrace(context, clazz.getSimpleName(), e)));
    }

    public static void register(Class<? extends Plugin> clazz) {
        boolean defaultEnabled = true;
        int defaultPriority = 0;
        if (clazz.isAnnotationPresent(PluginMain.class)) {
            PluginMain pluginMain = clazz.getAnnotation(PluginMain.class);
            assert pluginMain != null;
            defaultEnabled = pluginMain.defaultEnabled();
            defaultPriority = pluginMain.defaultPriority();
        }

        String name = clazz.getName();
        PluginConfig config = new PluginConfig();
        config.enabled = ExceptionHandler.ror(() ->
                pluginConfigs.get("enabled").getAsJsonObject().get(name).getAsBoolean(), defaultEnabled);
        config.priority = ExceptionHandler.ror(() ->
                pluginConfigs.get("priority").getAsJsonObject().get(name).getAsInt(), defaultPriority);
        config.clazz = clazz;
        plugins.add(config);
    }
    /** @noinspection unchecked*/
    public static void register(String clazz) throws ClassNotFoundException {
        register((Class<? extends Plugin>) Class.forName(clazz));
    }

    /**
     * 添加路径到so库搜索路径, 修改自Tinker
     * @param classLoader 要补丁的ClassLoader
     * @param folder 要添加的路径
     * @see <a href="https://github.com/Tencent/tinker/blob/836ca8bba1053307a3152ccb5bf60bff7ce170f9/tinker-android/tinker-android-lib/src/main/java/com/tencent/tinker/lib/library/TinkerLoadLibrary.java#L275">TinkerLoadLibrary.java#L275</a>
     */
    private static void installNativeLibraryPath(ClassLoader classLoader, File folder) throws Throwable {
        ReflectionUtils.lockCache();

        Object dexPathList;
        try {
            dexPathList = ReflectionUtils.get("dexPathList", classLoader);
        } catch (NoSuchFieldException e) {
            dexPathList = ReflectionUtils.get("pathList", classLoader);
        }

        List<File> origLibDirs = ReflectionUtils.get("nativeLibraryDirectories", dexPathList);
        if (origLibDirs == null) origLibDirs = new ArrayList<>(2);

        final Iterator<File> libDirIt = origLibDirs.iterator();
        while (libDirIt.hasNext()) {
            final File libDir = libDirIt.next();
            if (folder.equals(libDir)) {
                libDirIt.remove();
                break;
            }
        }
        origLibDirs.add(0, folder);

        List<File> origSystemLibDirs = ReflectionUtils.get("systemNativeLibraryDirectories", dexPathList);
        if (origSystemLibDirs == null) origSystemLibDirs = new ArrayList<>(2);

        final List<File> newLibDirs = new ArrayList<>(origLibDirs.size() + origSystemLibDirs.size() + 1);
        newLibDirs.addAll(origLibDirs);
        newLibDirs.addAll(origSystemLibDirs);

        Object[] elements;
        try {
            elements = (Object[]) ReflectionUtils.invoke("makePathElements", ReflectionUtils.D_U_P_T, dexPathList, List.class, newLibDirs);
        } catch (Throwable throwable) {
            final ArrayList<IOException> suppressedExceptions = new ArrayList<>();
            elements = (Object[]) ReflectionUtils.invoke("makePathElements", ReflectionUtils.D_U_P_T, dexPathList,
                    List.class, newLibDirs,
                    File.class, null,
                    List.class, suppressedExceptions);
        }
        ReflectionUtils.set("nativeLibraryPathElements", dexPathList, elements);

        ReflectionUtils.unlockCache();
    }

    /** @noinspection unchecked*/
    public static void load(ClassLoader classLoader, File file, File appPluginDir) throws IOException, ClassNotFoundException {
        String dexPath = file.getPath();
        File cache = null;
        DexFile dexFile = null;
        StorageUtils.delete(new File(appPluginDir, "oat"));
        try {
            cache = File.createTempFile("dex", "", Storage.CACHE_DIR);
            DexClassLoader dexClassLoader = new DexClassLoader(
                    dexPath,
                    cache.getPath(),
                    null,
                    classLoader
            );

            dexFile = new DexFile(file);
            for (Enumeration<String> classNames = dexFile.entries(); classNames.hasMoreElements(); ) {
                String className = classNames.nextElement();
                Class<?> clazz = dexClassLoader.loadClass(className);
                if (!clazz.isAnnotationPresent(PluginMain.class)) continue;
                register((Class<? extends Plugin>) clazz);
//            break;
            }
        } finally {
            if (dexFile != null) dexFile.close();
            if (cache != null) StorageUtils.delete(cache);
            StorageUtils.delete(new File(appPluginDir, "oat"));
        }
    }

    private static void searchDexPluginAndRegister(ClassLoader classLoader, File dir) throws IOException, ClassNotFoundException {
        for (File file : Objects.requireNonNull(dir.listFiles()))
            load(classLoader, file, dir);
    }

    public static void init(Context context) throws Throwable {
        plugins.clear();
        pluginConfigs = Objects.requireNonNullElse(
                StorageUtils.read(Storage.FILES_DIR, "plugins", JsonObject.class),
                new JsonObject()
        );

        // 内置插件
        register(ProtocolServerPlugin.class);
        register(CloudburstMC_Protocol.class);
        register(GeyserMC_MCProtocolLib.class);
        register(DebugConsole.class);

        register(ImageSender.class);
        register(MineChatPlus.class);

        register(NodejsService.class);
        register(TermuxStarter.Plugin.class);

        // so
        File appSoDir = new File(Storage.FILES_DIR, "lib");
        if (!appSoDir.exists() && !appSoDir.mkdirs()) throw new RuntimeException("Could not create " + appSoDir);
        installNativeLibraryPath(context.getClassLoader(), appSoDir);

        // dex
        File appPluginDir = new File(Storage.FILES_DIR, "plugin");
        if (!appPluginDir.exists() && !appPluginDir.mkdirs()) throw new RuntimeException("Could not create " + appPluginDir);
        searchDexPluginAndRegister(context.getClassLoader(), appPluginDir);
    }
}
