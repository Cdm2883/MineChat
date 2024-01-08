package vip.cdms.minechat.protocol.dataexchange.client;

import android.view.View;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import vip.cdms.mcoreui.view.dialog.FormBuilder;
import vip.cdms.minechat.protocol.dataexchange.ui.PlayerListController;
import vip.cdms.minechat.protocol.dataexchange.ui.ScoreboardController;
import vip.cdms.minechat.protocol.dataexchange.ui.TitleController;

/**
 * 为了防止IDE自动补全的干扰, 应运而生的 (
 */
public interface SimpleClientRepeaterHelper {
    SimpleClientRepeaterHelper connectingProgress(CharSequence content);
    default SimpleClientRepeaterHelper connectingProgress() {
        return connectingProgress("");
    }
    SimpleClientRepeaterHelper connected();
    void close(CharSequence reason);
    void error(Throwable throwable);
    void reconnect(String host, int port);

    SimpleClientRepeaterHelper print(CharSequence message);
    SimpleClientRepeaterHelper justPrint(CharSequence message);

    SimpleClientRepeaterHelper requestModalForm(FormBuilder<?, ?> formBuilder);

    TitleController getTitleController();
    ScoreboardController getScoreboardController();
    PlayerListController getPlayerListController();

    SimpleClientRepeaterHelper setOnMoreClickListener(View.OnClickListener listener);
    SimpleClientRepeaterHelper setOnMessageListener(Consumer<String> onMessageListener);
    SimpleClientRepeaterHelper setOnCommandListener(Consumer<String> onCommandListener);
    SimpleClientRepeaterHelper setOnCloseListener(Runnable onCloseListener);
    SimpleClientRepeaterHelper setOnPacketSendListener(BiConsumer<String, Object> onPacketSendListener);
    SimpleClientRepeaterHelper setOnEventAddListener(BiConsumer<String, Consumer<Object>> onEventAddListener);
}
