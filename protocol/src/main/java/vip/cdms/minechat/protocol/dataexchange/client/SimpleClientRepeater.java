package vip.cdms.minechat.protocol.dataexchange.client;

import android.view.View;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import vip.cdms.mcoreui.view.dialog.FormBuilder;
import vip.cdms.minechat.protocol.dataexchange.ui.PlayerListController;
import vip.cdms.minechat.protocol.dataexchange.ui.ScoreboardController;
import vip.cdms.minechat.protocol.dataexchange.ui.TitleController;


 public class SimpleClientRepeater implements SimpleClientRepeaterHelper, Client<SimpleClientRepeater> {
    public SimpleClientRepeater() {
    }
    public SimpleClientRepeaterHelper helper() {
        return this;
    }

    private Consumer<CharSequence> onConnectingProgressListener;
    @Override
    public SimpleClientRepeater setOnConnectingProgressListener(Consumer<CharSequence> listener) {
        onConnectingProgressListener = listener;
        return this;
    }
    public SimpleClientRepeater connectingProgress(CharSequence content) {
        if (onConnectingProgressListener != null) onConnectingProgressListener.accept(content);
        return this;
    }

    private Runnable onConnectedListener;
    @Override
    public SimpleClientRepeater setOnConnectedListener(Runnable listener) {
        onConnectedListener = listener;
        return this;
    }
    public SimpleClientRepeater connected() {
        if (onConnectedListener != null) onConnectedListener.run();
        return this;
    }

    private Consumer<CharSequence> onCloseListener;
    @Override
    public SimpleClientRepeater setOnCloseListener(Consumer<CharSequence> listener) {
        onCloseListener = listener;
        return this;
    }
    public void close(CharSequence reason) {
        if (onCloseListener != null) onCloseListener.accept(reason);
    }

    private Consumer<Throwable> onErrorListener;
    @Override
    public SimpleClientRepeater setOnErrorListener(Consumer<Throwable> listener) {
        onErrorListener = listener;
        return this;
    }
    public void error(Throwable throwable) {
        if (onErrorListener != null) onErrorListener.accept(throwable);
    }

    private BiConsumer<String, Integer> onReconnectListener;
    @Override
    public SimpleClientRepeater setOnReconnectListener(BiConsumer<String, Integer> listener) {
        onReconnectListener = listener;
        return this;
    }
    public void reconnect(String host, int port) {
        if (onReconnectListener != null) onReconnectListener.accept(host, port);
    }

     private Consumer<CharSequence> onPrintListener;
    @Override
    public SimpleClientRepeater setOnPrint(Consumer<CharSequence> callback) {
        onPrintListener = callback;
        return this;
    }
    public SimpleClientRepeater print(CharSequence message) {
        if (onPrintListener != null) onPrintListener.accept(message);
        return this;
    }

    private Consumer<CharSequence> onJustPrintListener;
    @Override
    public SimpleClientRepeater setOnJustPrint(Consumer<CharSequence> callback) {
        onJustPrintListener = callback;
        return this;
    }
    public SimpleClientRepeater justPrint(CharSequence message) {
        if (onJustPrintListener != null) onJustPrintListener.accept(message);
        return this;
    }

    private Consumer<FormBuilder<?, ?>> onModalFormRequest;
    @Override
    public SimpleClientRepeater setOnModalFormRequest(Consumer<FormBuilder<?, ?>> callback) {
        onModalFormRequest = callback;
        return this;
    }
    public SimpleClientRepeater requestModalForm(FormBuilder<?, ?> formBuilder) {
        onModalFormRequest.accept(formBuilder);
        return this;
    }

    private TitleController titleController;
    @Override
    public TitleController getTitleController() {
        return titleController;
    }
    public SimpleClientRepeater setTitleController(TitleController titleController) {
        this.titleController = titleController;
        return this;
    }

    private ScoreboardController scoreboardController;
    @Override
    public ScoreboardController getScoreboardController() {
        return scoreboardController;
    }
    public SimpleClientRepeater setScoreboardController(ScoreboardController scoreboardController) {
        this.scoreboardController = scoreboardController;
        return this;
    }

    private PlayerListController playerListController;
    @Override
    public PlayerListController getPlayerListController() {
        return playerListController;
    }
    public SimpleClientRepeater setPlayerListController(PlayerListController playerListController) {
        this.playerListController = playerListController;
        return this;
    }
    //    === === === === === === === === === === === ===

    private View.OnClickListener onMoreClickListener;
    @Override
    public void moreOnCLick(View view) {
        if (onMoreClickListener != null) onMoreClickListener.onClick(view);
    }
    public SimpleClientRepeater setOnMoreClickListener(View.OnClickListener listener) {
        onMoreClickListener = listener;
        return this;
    }

    private Consumer<String> onMessageListener;
    @Override
    public void sendMessage(String message) {
        if (onMessageListener != null) onMessageListener.accept(message);
    }
    public SimpleClientRepeater setOnMessageListener(Consumer<String> onMessageListener) {
        this.onMessageListener = onMessageListener;
        return this;
    }

    private Consumer<String> onCommandListener;
    @Override
    public void executeCommand(String command) {
        if (onCommandListener != null) onCommandListener.accept(command);
    }
    public SimpleClientRepeater setOnCommandListener(Consumer<String> onCommandListener) {
        this.onCommandListener = onCommandListener;
        return this;
    }

    private Runnable onNeedCloseListener;
    @Override
    public void close() {
        if (onNeedCloseListener != null) onNeedCloseListener.run();
    }
    public SimpleClientRepeater setOnCloseListener(Runnable onCloseListener) {
        onNeedCloseListener = onCloseListener;
        return this;
    }

    private BiConsumer<String, Object> onPacketSendListener;
    @Override
    public void sendPacket(String name, Object obj) {
        if (onPacketSendListener != null) onPacketSendListener.accept(name, obj);
    }
    public SimpleClientRepeater setOnPacketSendListener(BiConsumer<String, Object> onPacketSendListener) {
        this.onPacketSendListener = onPacketSendListener;
        return this;
    }

    private BiConsumer<String, Consumer<Object>> onEventAddListener;
    @Override
    public void addOnEvent(String name, Consumer<Object> callback) {
        if (onEventAddListener != null) onEventAddListener.accept(name, callback);
    }
    public SimpleClientRepeater setOnEventAddListener(BiConsumer<String, Consumer<Object>> onEventAddListener) {
        this.onEventAddListener = onEventAddListener;
        return this;
    }
}