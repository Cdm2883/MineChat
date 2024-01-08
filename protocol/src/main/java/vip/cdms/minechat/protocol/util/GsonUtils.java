package vip.cdms.minechat.protocol.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class GsonUtils {
    public static final Gson GSON = new Gson();
    public static final Gson GSON_FILE = new GsonBuilder()
            .setPrettyPrinting()
            .create();
}
