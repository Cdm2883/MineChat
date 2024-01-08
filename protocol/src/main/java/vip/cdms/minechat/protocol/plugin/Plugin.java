package vip.cdms.minechat.protocol.plugin;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Base64;

import vip.cdms.mcoreui.util.ImageUtils;
import vip.cdms.mcoreui.view.dialog.DialogBuilder;
import vip.cdms.minechat.protocol.util.ExceptionHandler;

public abstract class Plugin {
    private Activity activity;
    private final SharedPreferences sharedPreferences;
    private final ExceptionHandler exceptionHandler;
    public Plugin(Activity activity, SharedPreferences sharedPreferences, ExceptionHandler exceptionHandler) {
        this.activity = activity;
        this.sharedPreferences = sharedPreferences;
        this.exceptionHandler = exceptionHandler;
    }
    void setActivity(Activity activity) {
        this.activity = activity;
    }
    /** @return 插件运行时的Activity */
    protected Activity getActivity() {
        return activity;
    }
    protected void runOnUiThread(Runnable action) {
        activity.runOnUiThread(action);
    }

    /** @return 分配给插件的SharedPreferences */
    protected SharedPreferences getSharedPreferences() {
        return sharedPreferences;
    }

    protected ExceptionHandler getExceptionHandler() {
        return exceptionHandler;
    }
    protected void throwException(Throwable e) {
        exceptionHandler.throwException(e);
    }
    protected void tryCatch(ExceptionHandler.AutoTryCatch tryCatch) {
        exceptionHandler.tryCatch(tryCatch);
    }
    public static <T> T ror(ExceptionHandler.AutoTryCatchResult<T> getter, ExceptionHandler.AutoTryCatchResult<T> defaultGetter) {
        return ExceptionHandler.ror(getter, defaultGetter);
    }
    public static <T> T ror(ExceptionHandler.AutoTryCatchResult<T> getter, T defaultValue) {
        return ExceptionHandler.ror(getter, defaultValue);
    }
    public static <T> T ror(T value, T defaultValue) {
        return ExceptionHandler.ror(value, defaultValue);
    }

    private Drawable pluginIcon = null;
    /** 获取插件图标, 可以重写以获取动态的效果 */
    public Drawable getPluginIcon() {
        return pluginIcon;
    }
    public void setPluginIcon(Drawable drawable) {
        pluginIcon = drawable;
    }
    public void setPluginIcon(Bitmap bitmap) {
        setPluginIcon(ImageUtils.bitmapToDrawable(null, bitmap));
    }
    public void setPluginIcon(String base64) {
        if (base64 == null) return;
        byte[] bytes = Base64.decode(base64 , Base64.DEFAULT);
        Bitmap iconBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        setPluginIcon(iconBitmap);
    }

    private CharSequence pluginTitle;
    public CharSequence getPluginTitle() {
        return pluginTitle == null ? getClass().getSimpleName() : pluginTitle;
    }
    public void setPluginTitle(CharSequence pluginTitle) {
        this.pluginTitle = pluginTitle;
    }

    private CharSequence pluginSummary;
    public CharSequence getPluginSummary() {
        return pluginSummary == null ? getClass().getName() : pluginSummary;
    }
    public void setPluginSummary(CharSequence pluginSummary) {
        this.pluginSummary = pluginSummary;
    }

    public void openPluginSetting() {
        openPluginAbout();
    }
    public void openPluginAbout() {
        new DialogBuilder(getActivity())
                .setTitle(getPluginTitle())
                .setContent(getPluginSummary())
                .show();
    }
}
