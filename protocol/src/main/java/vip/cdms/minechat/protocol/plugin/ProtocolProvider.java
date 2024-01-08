package vip.cdms.minechat.protocol.plugin;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.Serializable;
import java.util.Map;
import java.util.function.Consumer;

import vip.cdms.minechat.protocol.dataexchange.client.Client;
import vip.cdms.minechat.protocol.util.ExceptionHandler;

/**
 * 协议提供器
 * @author Cdm2883
 */
public abstract class ProtocolProvider extends Plugin {
    public record ServerConfig(String host, int port, JsonObject others) implements Serializable {
        public ServerConfig(String host, int port) {
            this(host, port, new JsonObject());
        }
        public JsonObject toJson() {
            JsonObject json = new JsonObject();
            json.addProperty("host", host);
            if (port != -1) json.addProperty("port", port);
            for (Map.Entry<String, JsonElement> entry : others.entrySet())
                json.add(entry.getKey(), entry.getValue());
            return json;
        }
        public static ServerConfig formJson(JsonObject json) {
            JsonObject others = json.deepCopy();
            others.remove("host");
            others.remove("port");
            return new ServerConfig(
                    json.has("host") ? json.get("host").getAsString() : null,
                    json.has("port") ? json.get("port").getAsInt() : -1,
                    others
            );
        }
        public JsonElement get(String memberName) {
            return others.get(memberName);
        }
        public void add(String property, JsonElement value) {
            others.add(property, value);
        }
        public void add(String property, String value) {
            others.addProperty(property, value);
        }
        public void add(String property, Number value) {
            others.addProperty(property, value);
        }
        public void add(String property, Boolean value) {
            others.addProperty(property, value);
        }
        public void add(String property, Character value) {
            others.addProperty(property, value);
        }

        public ServerConfig host(String host) {
            if (this.host != null && this.host.equals(host)) return this;
            return new ServerConfig(host, port, others);
        }
        public ServerConfig port(int port) {
            if (this.port == port) return this;
            return new ServerConfig(host, port, others);
        }

        public String portString() {
            return String.valueOf(port);
        }
    }
    public abstract static class MotdCallback {
        public abstract void icon(Drawable drawable);
        public abstract void title(CharSequence charSequence);
        public abstract void summary(CharSequence charSequence);
        public abstract void topText(CharSequence charSequence);
        public abstract void topIcon(Drawable drawable);
        public static MotdCallback make(
                Consumer<Drawable> icon,
                Consumer<CharSequence> title,
                Consumer<CharSequence> summary,
                Consumer<CharSequence> topText,
                Consumer<Drawable> topIcon
        ) {
            return new MotdCallback() {
                @Override
                public void icon(Drawable drawable) {
                    icon.accept(drawable);
                }
                @Override
                public void title(CharSequence charSequence) {
                    title.accept(charSequence);
                }
                @Override
                public void summary(CharSequence charSequence) {
                    summary.accept(charSequence);
                }
                @Override
                public void topText(CharSequence charSequence) {
                    topText.accept(charSequence);
                }
                @Override
                public void topIcon(Drawable drawable) {
                    topIcon.accept(drawable);
                }
            };
        }
    }

    public ProtocolProvider(Activity activity, SharedPreferences sharedPreferences, ExceptionHandler exceptionHandler) {
        super(activity, sharedPreferences, exceptionHandler);
    }

    public abstract void createOrEdit(@Nullable ServerConfig config, Consumer<ServerConfig> configCallback);
    public abstract void motd(ServerConfig config, MotdCallback motdCallback);
    public abstract void connect(ServerConfig config, Consumer<Client<?>> clientCallback) throws Exception;
}
