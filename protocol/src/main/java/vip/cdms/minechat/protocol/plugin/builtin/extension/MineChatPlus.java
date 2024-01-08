package vip.cdms.minechat.protocol.plugin.builtin.extension;

import android.app.Activity;
import android.content.SharedPreferences;
import android.view.View;

import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.RecyclerView;

import java.sql.Ref;

import vip.cdms.mcoreui.util.CacheUtils;
import vip.cdms.mcoreui.util.MCTextParser;
import vip.cdms.mcoreui.util.ReflectionUtils;
import vip.cdms.mcoreui.view.show.Toast;
import vip.cdms.minechat.protocol.dataexchange.client.ExtensionClient;
import vip.cdms.minechat.protocol.plugin.JLanguage;
import vip.cdms.minechat.protocol.plugin.ProtocolExtension;
import vip.cdms.minechat.protocol.plugin.ProtocolProvider;
import vip.cdms.minechat.protocol.util.ExceptionHandler;
import vip.cdms.minechat.protocol.util.StringCommandHelper;

public class MineChatPlus extends ProtocolExtension {
    JLanguage lang = new JLanguage();
    public MineChatPlus(Activity activity, SharedPreferences sharedPreferences, ExceptionHandler exceptionHandler) {
        super(activity, sharedPreferences, exceptionHandler);
        lang.set(activity);
        setPluginIcon("iVBORw0KGgoAAAANSUhEUgAAAAoAAAAKCAMAAAC67D+PAAAAAXNSR0IArs4c6QAAAAxQTFRFHh4fEw0L/6RARDoweD4CLQAAACtJREFUCJljYGaEAmYGRkYmJghmAJNgNpAJFQcyQSywDAoTSQGSNoRhCCsAHoIAjecddCcAAAAASUVORK5CYII=");
        setPluginTitle(lang.add("MineChat+", "应用功能扩展"));
        setPluginSummary(lang.add("[built-in] Utility MineChat utils", "[内置] 实用软件小工具"));
    }

//    @Override
//    public void openPluginSetting() {
//    }

    static class CommandHelper extends StringCommandHelper.ListenerHelper {
        @OnRegex(regex = "^(minechat|app|mct) ?(help|\\?)?$")
        public void help(JLanguage lang, ExtensionClient client) {
            client.print(MCTextParser.easyFormat(lang.add("&2--- &aMineChat+ &2---", "&2--- &a应用功能扩展 &2---").toString()));
            help(client, "/mct [help|?]");
            help(client, "/mct <clear|cls> [keep:num]");
        }
        public void help(ExtensionClient client, String text) {
            client.print(text.replaceAll("([\\[\\]<>|:])", MCTextParser.easyFormat("&7&m$1&r")));
        }

        @OnRegex(regex = "^(?:minechat|app|mct)? ?(?:clear|cls) ?(\\d+)?$")
        public void clear(
                Activity activity,
                ExceptionHandler exceptionHandler,
                Integer count
        ) throws Throwable {
            new Thread(() -> exceptionHandler.tryCatch(() -> {
                Object aServer = ReflectionUtils.get("aServer", activity);
                String serverId = ReflectionUtils.get("id", aServer);
                Object chatHistoryDao = ReflectionUtils.get("chatHistoryDao", activity);
                if (count != null) ReflectionUtils.invoke("keep", chatHistoryDao, serverId, count + 1);
                else ReflectionUtils.invoke("clear", chatHistoryDao, serverId);

                RecyclerView.Adapter<RecyclerView.ViewHolder> adapter = ReflectionUtils.get("adapter", activity);
                if (count != null) ReflectionUtils.invoke("keep", adapter, count);
                else ReflectionUtils.invoke("clear", adapter);
            })).start();
        }
    }
    CommandHelper commandHelper;
    StringCommandHelper.ListenerHost commandHost;
    @Override
    public void onConnected(ProtocolProvider protocolProvider, ExtensionClient client) {
        super.onConnected(protocolProvider, client);
        commandHelper = new CommandHelper();
        commandHost = StringCommandHelper.easyTransfer(commandHelper, getExceptionHandler(), getActivity(), client, lang);
    }
    @Override
    public boolean onCommand(String command) {
        return commandHost.onCommand(command);
    }

    @Override
    public void onExtensionClick(View view) {
        getClient().print();
        commandHelper.help(lang, getClient());
    }
}
