package vip.cdms.minechat.protocol.plugin.builtin.protocol;

import android.app.Activity;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;

import com.google.gson.JsonArray;

import java.io.File;
import java.util.function.Consumer;

import vip.cdms.mcoreui.util.MCFontWrapper;
import vip.cdms.mcoreui.util.MCTextParser;
import vip.cdms.mcoreui.view.dialog.CustomFormBuilder;
import vip.cdms.minechat.protocol.app.Storage;
import vip.cdms.minechat.protocol.dataexchange.client.Client;
import vip.cdms.minechat.protocol.dataexchange.client.SimpleClientRepeater;
import vip.cdms.minechat.protocol.dataexchange.client.SimpleClientRepeaterHelper;
import vip.cdms.minechat.protocol.dataexchange.ui.PlayerListController;
import vip.cdms.minechat.protocol.dataexchange.ui.ScoreboardController;
import vip.cdms.minechat.protocol.dataexchange.ui.TitleController;
import vip.cdms.minechat.protocol.script.NodeNative;
import vip.cdms.minechat.protocol.script.ScriptException;
import vip.cdms.minechat.protocol.plugin.JLanguage;
import vip.cdms.minechat.protocol.plugin.ProtocolProvider;
import vip.cdms.minechat.protocol.plugin.builtin.service.NodejsService;
import vip.cdms.minechat.protocol.util.ExceptionHandler;
import vip.cdms.minechat.protocol.util.StorageUtils;
import vip.cdms.minechat.protocol.util.StringCommandHelper;

public class DebugConsole extends ProtocolProvider {
    JLanguage lang = new JLanguage();
    public DebugConsole(Activity activity, SharedPreferences sharedPreferences, ExceptionHandler exceptionHandler) {
        super(activity, sharedPreferences, exceptionHandler);
        lang.set(activity);
        setPluginIcon("iVBORw0KGgoAAAANSUhEUgAAABgAAAAYBAMAAAASWSDLAAAAGFBMVEVNOl0eHh+zz+yfn9w0LEI4MEdGNlhENVdHYFy6AAAATUlEQVQY023QMRHAQAzEQFMwBVE4CqZgCqZg+qnyk8yfum0V+Sn+iNON1ozaoMUMagOYAQ8JygIstgFqb8S2VBsG0VBhUR6ZWVDnzosHbfIj/1T1lusAAAAASUVORK5CYII=");
        setPluginTitle(lang.add("Debug Console", "调试控制台"));
        setPluginSummary(lang.add("[built-in] Cannot connect any server and is used only for debugging", "[内置] 无法建立实际连接, 仅用于调试"));
    }

    @Override
    public void createOrEdit(@Nullable ServerConfig config, Consumer<ServerConfig> configCallback) {
        String title = config == null ? "" : config.get("title").getAsString();
        String summary = config == null ? "" : config.get("summary").getAsString();

        new CustomFormBuilder()
                .setTitle(config == null ? lang.add("Add a server", "添加控制台") : lang.add("Edit the server", "编辑控制台"))
                .addInput(lang.add("title", "显示名称"), "Debug Console", title)
                .addInput(lang.add("summary", "地址"), "Hello MineChat!", summary)
                .setCallback(callback -> tryCatch(() -> {
                    if (callback.isCancel()) return;
                    JsonArray response = callback.responseData();

                    String mTitle = response.get(0).getAsString();
                    if (mTitle.isEmpty()) mTitle = "Debug Console";
                    String mSummary = response.get(1).getAsString();
                    if (mSummary.isEmpty()) mSummary = "Hello MineChat!";

                    ServerConfig edited = new ServerConfig(null, -1);
                    edited.add("title", mTitle);
                    edited.add("summary", mSummary);
                    configCallback.accept(edited);
                }))
                .show(getActivity());
    }

    @Override
    public void motd(ServerConfig config, MotdCallback motdCallback) {
        motdCallback.icon(getPluginIcon());
        motdCallback.title(MCFontWrapper.WRAPPER.wrap(config.get("title").getAsString()));
        motdCallback.summary(MCFontWrapper.WRAPPER.wrap(config.get("summary").getAsString()));
        motdCallback.topText(MCFontWrapper.WRAPPER.wrap(
                MCTextParser.randomInsert(true, false, "||||||||")
        ));
    }

    static class CommandHelper extends StringCommandHelper.ListenerHelper {
        @OnRegex(regex = "^(?:title)? ?(title|subtitle|actionbar|popup) ?([\\w\\W]*)$")
        public void title(
                TitleController titleController,
                String position,
                String message
        ) {
            switch (position.toLowerCase()) {
                case "title" -> titleController.title(message);
                case "subtitle" -> titleController.subtitle(message);
                case "actionbar" -> titleController.actionbar(message);
                case "popup" -> titleController.popup(message);
            }
        }
        @OnRegex(regex = "^title ?times ?([0-9]+) ([0-9]+) ([0-9]+)$")
        public void title(
                TitleController titleController,
                int fadeIn,
                int stay,
                int fadeOut
        ) {
            titleController.setFadeInTime(fadeIn);
            titleController.setStayTime(stay);
            titleController.setFadeOutTime(fadeOut);
        }

        @OnRegex(regex = "^toast ?([^ ]+) ?(.+)?$")
        public void toast(
                DebugConsole debugConsole,
                TitleController titleController,
                String title,
                String message
        ) {
            titleController.toast(
                    message == null ? debugConsole.getPluginTitle().toString() : title,
                    message == null ? title : message
            );
        }

        @OnRegex(regex = "^node ?(.+)$")
        public void nodeMain(ExceptionHandler exceptionHandler, String path) {
            exceptionHandler.tryCatch(() ->
                    NodeNative.runMain(new File(Storage.FILES_DIR, path).getPath(), new String[0]));
        }
    }
    CommandHelper commandHelper;
    StringCommandHelper.ListenerHost commandHost;

    NodeNative nodejs;

    Thread loopThread;
    /** @noinspection BusyWait*/
    @Override
    public void connect(ServerConfig config, Consumer<Client<?>> clientCallback) throws Exception {
        SimpleClientRepeater simpleClient = new SimpleClientRepeater();
        final SimpleClientRepeaterHelper clientHelper = simpleClient.helper();
        clientHelper
                .setOnMoreClickListener(v -> {
                })
                .setOnMessageListener(message -> {
                    clientHelper.print("> " + message);

                    if (nodejs == null) return;
                    if (message.equalsIgnoreCase(".exit")) {
                        nodejs = null;
                        clientHelper.print("exited the REPL.");
                        clientHelper.print("");
                        return;
                    }

                    getExceptionHandler()
                            .handDisposable(ScriptException.class, e ->
                                    clientHelper.print(MCTextParser.SS + "c" + e.getMessage()))
                            .tryCatch(() ->
                                    clientHelper.print(MCTextParser.SS + "7" + nodejs.evalString(message)));
                })
                .setOnCommandListener(command -> {
                    if (nodejs != null || !command.equalsIgnoreCase("/node")) {
                        commandHost.onCommand(command.substring(1));
                        return;
                    }

                    if (!NodejsService.isRunning()) {
                        clientHelper.print(MCTextParser.easyFormat("&c[&4ERROR&c]&r Node.js Service is not running!"));
                        return;
                    }
                    tryCatch(() -> {
                        nodejs = NodeNative.create();
                        clientHelper.print("Welcome to Node.js v" + nodejs.evalString("process.versions.node") + ".");
                        clientHelper.print("Type \".exit\" to exit.");
                    });
                })
                .setOnCloseListener(() -> {
                    loopThread.interrupt();
                })
                .setOnPacketSendListener((name, params) -> {
                })
                .setOnEventAddListener((name, callback) -> {
                });
        clientCallback.accept(simpleClient);
        TitleController titleController = clientHelper.getTitleController();
        ScoreboardController scoreboardController = clientHelper.getScoreboardController();
        PlayerListController playerListController = clientHelper.getPlayerListController();

        commandHelper = new CommandHelper();
        commandHost = StringCommandHelper.easyTransfer(commandHelper,
                getExceptionHandler(), clientHelper, titleController, this);

        loopThread = new Thread(() -> {
            while (!loopThread.isInterrupted()) {
                playerListController.setTitle(
                        MCTextParser.randomInsert(true, true, "Debug Console")
                );
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        loopThread.start();

        clientHelper.connected();
    }
}
