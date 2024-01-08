package vip.cdms.minechat.protocol.dataexchange.client;

import java.util.function.Consumer;

import vip.cdms.minechat.protocol.dataexchange.ui.PlayerListController;
import vip.cdms.minechat.protocol.dataexchange.ui.ScoreboardController;
import vip.cdms.minechat.protocol.dataexchange.ui.TitleController;

public interface ExtensionClient {
    ExtensionClient loading(CharSequence progress);
    default ExtensionClient loading() {
        return loading("");
    }
    ExtensionClient loaded();

    void close(CharSequence reason);
    void error(Throwable throwable);

    ExtensionClient print(CharSequence message);
    default ExtensionClient print(CharSequence... messages) {
        if (messages.length == 0) return print("");
        ExtensionClient client = null;
        for (CharSequence message : messages)
            client = print(message);
        return client;
    }
    ExtensionClient justPrint(CharSequence message);

    TitleController getTitleController();
    ScoreboardController getScoreboardController();
    PlayerListController getPlayerListController();

    void sendMessage(String message);
    void executeCommand(String command);

    void sendPacket(String name, Object obj);
    void addOnEvent(String name, Consumer<Object> callback);
}
