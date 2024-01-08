package vip.cdms.minechat.protocol.util;

import android.content.Context;
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
import java.util.function.Consumer;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import vip.cdms.minechat.protocol.app.Storage;
import vip.cdms.minechat.protocol.util.OkHttpJsonCallback;
import vip.cdms.minechat.protocol.util.StorageUtils;

// todo delete
public class MojangAPI {
    public static void getAvatar(Context context, String username, Consumer<Bitmap> callback) {
        callback.accept(null);
//        File cache = new File(Storage.CACHE_DIR, username + ".png");
//        try {
//            if (cache.exists()) {
//                Bitmap bitmap = BitmapFactory.decodeStream(new FileInputStream(cache));
//                callback.accept(bitmap);
//                return;
//            }
//
//            OkHttpJsonCallback.OnFailure onFailure = e -> callback.accept(null);
//            OkHttpClient client = new OkHttpClient().newBuilder().build();
//            Request profilesRequest = new Request.Builder()
//                    .url("https://api.mojang.com/users/profiles/minecraft/" + username).get()
//                    .build();
//            client.newCall(profilesRequest).enqueue(new OkHttpJsonCallback<JsonObject>()
//                    .setOnFailure(onFailure).setOnResponse(body -> {
//                        if (body.has("errorMessage")) {
//                            callback.accept(null);
//                            return;
//                        }
//                        String id = body.get("id").getAsString();
//                        // 感谢 crafatar.com 提供的API服务
//                        Glide.with(context).asBitmap().load("https://crafatar.com/avatars/" + id).into(new SimpleTarget<Bitmap>() {
//                            @Override
//                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
//                                try {
//                                    callback.accept(resource);
//                                    FileOutputStream out = new FileOutputStream(cache);
//                                    resource.compress(Bitmap.CompressFormat.PNG, 100, out);
//                                    out.flush();
//                                    out.close();
//                                } catch (Exception e) {
//                                    e.printStackTrace();
//                                    callback.accept(null);
//                                }
//                            }
//                        });
//                    }));
//        } catch (Exception e) {
//            e.printStackTrace();
//            callback.accept(null);
//        }
    }
}