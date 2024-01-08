package vip.cdms.minechat.protocol.util;

import androidx.annotation.NonNull;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class OkHttpJsonCallback<T extends JsonElement> implements Callback {
    public interface OnFailure {
        void onFailure(Exception e);
    }
    public interface OnResponse<T extends JsonElement> {
        void onResponse(T body) throws Exception;
    }
    private OnFailure onFailure = e -> {};
    private OnResponse<T> onResponse = body -> {};
    @Override
    public void onFailure(@NonNull Call call, @NonNull IOException e) {
        onFailure.onFailure(e);
    }
    /** @noinspection unchecked*/
    @Override
    public void onResponse(@NonNull Call call, @NonNull Response response) {
        try {
            String result = Objects.requireNonNull(response.body()).string();
            onResponse.onResponse((T) JsonParser.parseString(result));
        } catch (Exception e) {
            onFailure.onFailure(e);
        }
    }

    public OkHttpJsonCallback<T> setOnFailure(OnFailure onFailure) {
        this.onFailure = onFailure;
        return this;
    }
    public OkHttpJsonCallback<T> setOnResponse(OnResponse<T> onResponse) {
        this.onResponse = onResponse;
        return this;
    }
}