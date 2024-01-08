package vip.cdms.minechat.activity.chat.controller;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.HashMap;
import java.util.function.Consumer;

import vip.cdms.mcoreui.util.MCFontWrapper;
import vip.cdms.mcoreui.view.show.ImageView;
import vip.cdms.mcoreui.view.show.TextView;
import vip.cdms.minechat.R;
import vip.cdms.minechat.activity.chat.ChatActivity;
import vip.cdms.minechat.activity.chat.ChatActivityConnection;
import vip.cdms.minechat.databinding.ActivityChatBinding;
import vip.cdms.minechat.protocol.dataexchange.ui.PlayerListController;

public class PlayerListControllerImpl extends ChatActivityConnection implements PlayerListController {
    ActivityChatBinding binding;
    int dp;
    HashMap<String, View> playerListId2View;
    public PlayerListControllerImpl(ChatActivity activity) {
        super(activity);
        binding = getBinding();
        dp = activity.dp;
        playerListId2View = activity.playerListId2View;
    }

    @Override
    public void setTitle(CharSequence title) {
        runOnUiThread(() ->
                binding.pauseMenuTitle.setText(title instanceof String ? MCFontWrapper.WRAPPER.wrap(title) : title));
    }

    @Override
    public void add(String id, String username, Consumer<Consumer<Drawable>> avatar) {
        if (playerListId2View.get(id) != null) return;

        ViewGroup playerItem = (ViewGroup) ViewGroup.inflate(getActivity(), R.layout.layout_list_player, null);

        LinearLayout.LayoutParams layoutParams;
        if (binding.pauseMenuPlayers.getChildCount() > 0) {
            layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.topMargin = -2 * dp;
        } else {
            layoutParams = null;
        }

        TextView num = playerItem.findViewById(R.id.num);
        ImageView icon = playerItem.findViewById(android.R.id.icon);
        TextView title = playerItem.findViewById(android.R.id.title);

        new Thread(() -> avatar.accept(drawable -> {
            if (drawable == null) return;
            runOnUiThread(() -> icon.setImageDrawable(drawable));
        })).start();
        runOnUiThread(() -> {
            if (layoutParams != null) playerItem.setLayoutParams(layoutParams);
            num.setText(String.valueOf(binding.pauseMenuPlayers.getChildCount() + 1));
            title.setText(MCFontWrapper.WRAPPER.wrap(username));
            binding.pauseMenuPlayers.addView(playerItem);
        });
        playerListId2View.put(id, playerItem);
    }
    @Override
    public void remove(String id) {
        View view = playerListId2View.get(id);
        if (view == null) return;
        runOnUiThread(() -> binding.pauseMenuPlayers.removeView(view));
        playerListId2View.remove(id);
    }
}
