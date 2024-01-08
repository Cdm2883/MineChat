package vip.cdms.minechat.protocol.plugin;

import android.app.Activity;
import android.content.SharedPreferences;
import android.view.View;

import vip.cdms.minechat.protocol.dataexchange.client.ExtensionClient;
import vip.cdms.minechat.protocol.util.ExceptionHandler;
import vip.cdms.minechat.protocol.util.StringCommandHelper;

/**
 * 协议功能扩展插件
 * @author Cdm2883
 */
public abstract class ProtocolExtension extends Plugin implements StringCommandHelper.ListenerHost {
    private ProtocolProvider protocolProvider;
    private ExtensionClient client;
    public ProtocolExtension(Activity activity, SharedPreferences sharedPreferences, ExceptionHandler exceptionHandler) {
        super(activity, sharedPreferences, exceptionHandler);
    }
    protected ProtocolProvider getProtocolProvider() {
        return protocolProvider;
    }
    protected ExtensionClient getClient() {
        return client;
    }

    public void onExtensionClick(View view) {}

    public void onConnected(ProtocolProvider protocolProvider, ExtensionClient client) {
        this.protocolProvider = protocolProvider;
        this.client = client;
    }
    public void onDisconnect() {}

    /**
     * 监听用户输入命令 (不带斜杠"/")
     * @param command 命令
     * @return 是否消耗
     */
    @Override
    public boolean onCommand(String command) {
        return false;
    }

    /**
     * 监听消息输出
     * @param message 消息
     * @return 修改后输出的消息
     */
    public CharSequence onPrint(CharSequence message) {
        return message;
    }
}
