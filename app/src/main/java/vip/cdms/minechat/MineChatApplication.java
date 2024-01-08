package vip.cdms.minechat;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Looper;
import android.os.SystemClock;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import vip.cdms.mcoreui.OreUIApplication;
import vip.cdms.minechat.data.chathistory.ChatHistoryDatabase;
import vip.cdms.minechat.protocol.ProtocolApplication;
import vip.cdms.minechat.protocol.util.ExceptionHandler;
import vip.cdms.minechat.protocol.util.StorageUtils;
import vip.cdms.minechat.util.BedrockResource;

public class MineChatApplication extends Application implements Thread.UncaughtExceptionHandler {
    private final Map<Class<? extends Application>, Application> moduleApplications = new HashMap<>(){{
        put(OreUIApplication.class, null);
        put(ProtocolApplication.class, null);
    }};

    @Override
    public void onCreate() {
        super.onCreate();
        ProtocolInitializer.init(this);
        ChatHistoryDatabase.init(this);
        BedrockResource.init();

        Thread.setDefaultUncaughtExceptionHandler(this);

        moduleApplications.forEach((clazz, application) -> {
            if (application != null) application.onCreate();
        });
    }

    /** @noinspection JavaReflectionMemberAccess*/
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);

        moduleApplications.forEach((clazz, _application) -> {
            try {
                Application application = getModuleApplicationInstance(clazz);
                @SuppressLint("DiscouragedPrivateApi") Method method = Application.class.getDeclaredMethod("attach", Context.class);
                method.setAccessible(true);
                method.invoke(application, getBaseContext());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
    private Application getModuleApplicationInstance(Class<? extends Application> clazz) throws Exception {
        if (moduleApplications.get(clazz) == null)
            moduleApplications.put(clazz, clazz.newInstance());
        return moduleApplications.get(clazz);
    }

    @Override
    public void uncaughtException(@NonNull Thread t, @NonNull Throwable e) {
        // todo 更完善的错误收集系统
        new Thread(() -> {
            Looper.prepare();
            SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = defaultSharedPreferences.edit();
            editor.putString("uncaught_exception", ExceptionHandler.printStackTrace(e));
            editor.apply();
            Toast.makeText(getApplicationContext(), "正在收集异常...\nCollecting exceptions...", Toast.LENGTH_LONG).show();
            Looper.loop();
        }).start();
        SystemClock.sleep(3000);
        android.os.Process.killProcess(android.os.Process.myPid());
    }
}
