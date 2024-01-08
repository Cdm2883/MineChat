package vip.cdms.minechat.protocol.plugin.builtin.service;

import android.app.Activity;
import android.app.Application;
import android.content.SharedPreferences;

import java.util.Arrays;

import vip.cdms.mcoreui.util.MCTextParser;
import vip.cdms.mcoreui.util.StringUtils;
import vip.cdms.mcoreui.view.dialog.DialogBuilder;
import vip.cdms.mcoreui.view.show.Toast;
import vip.cdms.minechat.protocol.script.NodeNative;
import vip.cdms.minechat.protocol.plugin.JLanguage;
import vip.cdms.minechat.protocol.plugin.PluginMain;
import vip.cdms.minechat.protocol.plugin.ServiceExtension;
import vip.cdms.minechat.protocol.util.ExceptionHandler;

@PluginMain(defaultEnabled = false, defaultPriority = 10)
public class NodejsService extends ServiceExtension {
    private static boolean running = false;
    JLanguage lang = new JLanguage();
    public NodejsService(Application application, SharedPreferences sharedPreferences, ExceptionHandler exceptionHandler) {
        super(application, sharedPreferences, exceptionHandler);
        lang.set(application);
        setPluginIcon("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");
        setPluginTitle(lang.add("Node.js Runtime", "Node.js 运行时"));
        setPluginSummary(lang.add("[built-in] Give software the ability to run node.js programs", "[内置] 给软件提供运行node.js程序的能力"));
    }

//    @Override
//    public void openPluginSetting() {
//    }

    static boolean initialize = false;
    @Override
    public void onStart() {
        super.onStart();
        try {
            if (!initialize) {
                NodeNative.loadLibrary();
                NodeNative.initialize();
                initialize = true;
                running = true;
                new Toast()
                        .setTitle(MCTextParser.easyFormat("&2[&aNode.js&2]"))
                        .setMessage(lang.add("Node.js runtime initialization succeeded", "Node.js 运行时初始化成功") + "!")
                        .show(getActivity().getWindow().getDecorView());
                return;
            }
            running = false;
            throw new RuntimeException(lang.add("You can't initialize the Node .js again in this process!", "您无法在这个进程再次初始化Node.js!").toString());
        } catch (Throwable e) {
            running = false;
            e.printStackTrace();

            String message;
            if (e instanceof UnsatisfiedLinkError)
                message = String.format(
                        lang.add(
                                """
                                Oops! Node.js service failed to start!
                                
                                This error may occur because:
                                1. You do not have node's runtime installed %s
                                2. The runtime you installed does not meet the ABI of your device
                                """,
                                """
                                糟糕! Node.js 服务无法启动!
                                
                                出现此错误的原因可能是:
                                1. 你没有安装node的运行库 %s
                                2. 你安装的运行库不符合您设备的ABI
                                """
                        ).toString(),
                        "(" + StringUtils.sliceEnd(Arrays.toString(NodeNative.NECESSARY_LIBRARIES).substring(1), 1) + ")"
                ) + "\n" + e.getMessage();
            else message = e.getMessage() + "\n\n" + ExceptionHandler.printStackTrace(e);

            new DialogBuilder(getActivity())
                    .setTitle(lang.add("Node.js Services", "Node.js 服务启动失败"))
                    .setContent(message, false)
                    .show();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (initialize && running) NodeNative.shutdown();
        running = false;
    }

    @Override
    public void onActivityStart(Activity activity) {
        super.onActivityStart(activity);
        lang.set(activity);
    }

    public static boolean isRunning() {
        return running;
    }
}
