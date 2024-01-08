package vip.cdms.minechat.protocol.plugin;

import android.app.Activity;
import android.app.Application;
import android.app.Service;
import android.content.SharedPreferences;

import vip.cdms.minechat.protocol.util.ExceptionHandler;

/**
 * 软件服务插件 (由软件控制的单例)
 * @author Cdm2883
 */
public abstract class ServiceExtension extends Plugin {
    private final Application application;
    public ServiceExtension(Application application, SharedPreferences sharedPreferences, ExceptionHandler exceptionHandler) {
        super(null, sharedPreferences, exceptionHandler);
        this.application = application;
    }
    protected Application getApplication() {
        return application;
    }

    public void onStart() {
    }
    public void onDestroy() {
    }
    public void onActivityStart(Activity activity) {
        setActivity(activity);
    }
}
