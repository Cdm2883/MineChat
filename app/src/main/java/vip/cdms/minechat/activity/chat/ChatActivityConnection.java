package vip.cdms.minechat.activity.chat;

import vip.cdms.minechat.databinding.ActivityChatBinding;

public abstract class ChatActivityConnection {
    ChatActivity activity;
    ActivityChatBinding binding;
    public ChatActivityConnection(ChatActivity activity) {
        this.activity = activity;
        binding = activity.binding;
    }

    public ChatActivity getActivity() {
        return activity;
    }
    public ActivityChatBinding getBinding() {
        return binding;
    }

    public void runOnUiThread(Runnable runnable) {
        activity.runOnUiThread(runnable);
    }
}
