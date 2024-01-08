package vip.cdms.minechat.protocol.dataexchange.client;

import android.view.View;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import vip.cdms.mcoreui.view.dialog.FormBuilder;
import vip.cdms.minechat.protocol.dataexchange.ui.PlayerListController;
import vip.cdms.minechat.protocol.dataexchange.ui.ScoreboardController;
import vip.cdms.minechat.protocol.dataexchange.ui.TitleController;

public interface Client<C extends Client<?>> {
    C setOnConnectingProgressListener(Consumer<CharSequence> listener);
    C setOnConnectedListener(Runnable listener);
    C setOnCloseListener(Consumer<CharSequence> listener);
    C setOnErrorListener(Consumer<Throwable> listener);
    C setOnReconnectListener(BiConsumer<String, Integer> listener);

    C setOnPrint(Consumer<CharSequence> callback);
    C setOnJustPrint(Consumer<CharSequence> callback);
    C setOnModalFormRequest(Consumer<FormBuilder<?, ?>> callback);
//    C setOnBossBar(...); boss_event

    C setTitleController(TitleController titleController);
    C setScoreboardController(ScoreboardController scoreboardController);
    C setPlayerListController(PlayerListController playerListController);

    void moreOnCLick(View view);

    void sendMessage(String message);
    void executeCommand(String command);

    void close();
    void sendPacket(String name, Object obj);
    void addOnEvent(String name, Consumer<Object> callback);
}
