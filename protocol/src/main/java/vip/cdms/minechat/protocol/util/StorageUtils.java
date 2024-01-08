package vip.cdms.minechat.protocol.util;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.stream.JsonReader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Objects;

public class StorageUtils {
    public static void write(File file, JsonElement json) {
        if (file == null) return;
        try (FileWriter writer = new FileWriter(file)) {
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            new Gson().newBuilder()
                    .setPrettyPrinting()
                    .create()
                    .toJson(json, writer);
            writer.flush();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public static void write(File parent, String name, JsonElement json) {
        if (parent == null || name == null || name.isEmpty()) return;
        write(new File(parent, name + ".json"), json);
    }

    public static <T extends JsonElement> T read(File file, Class<T> clazz) {
        if (file == null) return null;
        try {
            if (!file.exists()) return null;
            JsonReader reader = new JsonReader(new FileReader(file));
            return new Gson().fromJson(reader, clazz);
        } catch (Exception e) {
            return null;
        }
    }
    public static <T extends JsonElement> T read(File parent, String name, Class<T> clazz) {
        if (parent == null || name == null || name.isEmpty()) return null;
        return read(new File(parent, name + ".json"), clazz);
    }

    public static boolean delete(File file) {
        if (file == null || !file.exists()) return false;
        if (file.isFile()) return file.delete();
        for (File child : Objects.requireNonNull(file.listFiles()))
            delete(child);
        return file.delete();
    }
}
