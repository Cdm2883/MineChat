package vip.cdms.mcoreui;

import android.app.Application;
import android.content.Context;

import java.lang.reflect.Method;

import me.weishu.reflection.Reflection;
import vip.cdms.mcoreui.util.PixelFont;
import vip.cdms.mcoreui.util.SoundPlayer;

/**
 * 用于完成一些初始化操作的Application
 * @author Cdm2883
 */
public class OreUIApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        SoundPlayer.init(this);
        PixelFont.init(this);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        Reflection.unseal(base);
    }

//    /**
//     * 获取当前应用的Application
//     * 先使用ActivityThread里获取Application的方法，如果没有获取到，
//     * 再使用AppGlobals里面的获取Application的方法
//     * @see <a href="https://blog.csdn.net/csdm_admin/article/details/108760450">CSDN</a>
//     */
//    public static Application getApplication(){
//        Application application = null;
//        try{
//            Class clazz = Class.forName("android.app.ActivityThread");
//            Method currentApplicationMethod = clazz.getDeclaredMethod("currentApplication");
//            currentApplicationMethod.setAccessible(true);
//            application = (Application) currentApplicationMethod.invoke(null);
//        } catch (Exception ignored) {}
//
//        if(application != null)
//            return application;
//
//        try{
//            Class clazz = Class.forName("android.app.AppGlobals");
//            Method currentApplicationMethod = clazz.getDeclaredMethod("getInitialApplication");
//            currentApplicationMethod.setAccessible(true);
//            application = (Application) currentApplicationMethod.invoke(null);
//        } catch (Exception ignored){}
//
//        return application;
//    }
}
