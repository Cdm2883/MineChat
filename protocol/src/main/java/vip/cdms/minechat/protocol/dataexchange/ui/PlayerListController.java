package vip.cdms.minechat.protocol.dataexchange.ui;

import android.graphics.drawable.Drawable;

import java.util.function.Consumer;

public interface PlayerListController {
    void setTitle(CharSequence title);

    void add(String id, String username, Consumer<Consumer<Drawable>> avatar);

    void remove(String id);
}
