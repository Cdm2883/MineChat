package vip.cdms.minechat.activity.chat;

import java.util.function.Consumer;

import vip.cdms.minechat.protocol.dataexchange.client.ExtensionClient;
import vip.cdms.minechat.protocol.dataexchange.ui.PlayerListController;
import vip.cdms.minechat.protocol.dataexchange.ui.ScoreboardController;
import vip.cdms.minechat.protocol.dataexchange.ui.TitleController;

public class ExtensionClientImpl extends ChatActivityConnection implements ExtensionClient {
    ChatActivity activity;
    public ExtensionClientImpl(ChatActivity activity) {
        super(activity);
        this.activity = activity;
    }

    @Override
    public ExtensionClient loading(CharSequence progress) {
        activity.loading(progress);
        return this;
    }
    @Override
    public ExtensionClient loaded() {
        activity.loaded();
        return this;
    }

    @Override
    public void close(CharSequence reason) {
        activity.close(reason);
    }
    @Override
    public void error(Throwable throwable) {
        activity.error(throwable);
    }

    @Override
    public ExtensionClient print(CharSequence message) {
        activity.print(message);
        return this;
    }
    @Override
    public ExtensionClient justPrint(CharSequence message) {
        activity.justPrint(message);
        return this;
    }

    @Override
    public TitleController getTitleController() {
        return activity.titleController;
    }
    @Override
    public ScoreboardController getScoreboardController() {
        return activity.scoreboardController;
    }
    @Override
    public PlayerListController getPlayerListController() {
        return activity.playerListController;
    }

    @Override
    public void sendMessage(String message) {
        activity.client.sendMessage(message);
    }
    @Override
    public void executeCommand(String command) {
        activity.client.executeCommand(command);
    }

    @Override
    public void sendPacket(String name, Object obj) {
        activity.client.sendPacket(name, obj);
    }
    @Override
    public void addOnEvent(String name, Consumer<Object> callback) {
        activity.client.addOnEvent(name, callback);
    }
}
