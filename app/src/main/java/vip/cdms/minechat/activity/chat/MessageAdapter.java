package vip.cdms.minechat.activity.chat;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.text.Spannable;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ImageSpan;
import android.text.util.Linkify;
import android.view.Gravity;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.function.Predicate;

import vip.cdms.mcoreui.util.FontWrapper;
import vip.cdms.mcoreui.util.MCFontWrapper;
import vip.cdms.mcoreui.util.MathUtils;
import vip.cdms.mcoreui.util.ResourcesUtils;
import vip.cdms.mcoreui.util.TimeUtils;
import vip.cdms.mcoreui.view.show.TextView;
import vip.cdms.minechat.R;
import vip.cdms.minechat.view.SlideListAdapter;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public record AMessage(boolean isMe, CharSequence content, Drawable icon) {}
    static final float DEFAULT_TEXT_SIZE = 18;
    LinearLayoutManager linearLayoutManager;
    static class MessageList extends ArrayList<AMessage> {}
    MessageList messages = new MessageList();
    float textSize = DEFAULT_TEXT_SIZE;
    final boolean hasBackground;
    public FontWrapper fontWrapper = MCFontWrapper.WRAPPER;

    public MessageAdapter(LinearLayoutManager linearLayoutManager) {
        this(linearLayoutManager, false);
    }
    public MessageAdapter(LinearLayoutManager linearLayoutManager, boolean hasBackground) {
        this.linearLayoutManager = linearLayoutManager;
        this.hasBackground = hasBackground;
    }

    public MessageAdapter addMessage(int delay, AMessage message) {
        messages.add(message);
        notifyItemInserted(this.messages.size() - 1);
        linearLayoutManager.scrollToPositionWithOffset(getItemCount() - 1, Integer.MIN_VALUE);
        if (delay != -1) TimeUtils.setTimeout(() -> post(() -> {
            int index = messages.indexOf(message);
            this.messages.remove(index);
            notifyItemRemoved(index);
        }), delay);
        return this;
    }
    public MessageAdapter addMessage(int delay, boolean isMe, CharSequence message) {
        return addMessage(delay, new AMessage(isMe, message instanceof String ? fontWrapper.wrap(message) : message, null));
    }
    public MessageAdapter addMessage(boolean isMe, CharSequence message) {
        return addMessage(-1, isMe, message);
    }
    public MessageAdapter addMessage(CharSequence message) {
        return addMessage(false, message);
    }
    public MessageAdapter clear() {
        return post(() -> {
            int beforeSize = messages.size();
            messages.clear();
            notifyItemRangeRemoved(0, beforeSize);
        });
    }
    public MessageAdapter keep(int count) {
        if (count == 0) return clear();
        return post(() -> {
            int beforeSize = messages.size();
            int i = 0;
            Iterator<AMessage> it = messages.iterator();
            while (it.hasNext()) {
                it.next();
                if (i < beforeSize - count - 1) it.remove();
                i++;
            }
            notifyItemRangeRemoved(0, beforeSize - count);
        });
    }
    public MessageAdapter post(Runnable runnable) {
        new Handler(Looper.getMainLooper()).post(runnable);
        return this;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();

        TextView textView = new TextView(context);
        textView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        if (hasBackground)
            textView.setBackgroundColor(0x55000000);

        return new SlideListAdapter.Holder(textView);
    }
    Drawable meIcon;
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Context context = holder.itemView.getContext();
        int dp = MathUtils.dp2px(context, 1);
        AMessage message = messages.get(position);

        TextView textView = (TextView) holder.itemView;
        textView.setTextIsSelectable(true);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        textView.setTextSize(textSize);

        CharSequence content = message.content();
        int iconSize = (int) (textSize * dp);
        if (content instanceof Spanned spanned)
            for (ImageSpan span : spanned.getSpans(0, spanned.length(), ImageSpan.class)) {
                Drawable drawable = span.getDrawable();
                drawable.setBounds(0, 0, iconSize, iconSize);
                drawable.invalidateSelf();
            }
        textView.setText(content);

        if (message.isMe || message.icon != null) {
            textView.setPadding(32 * dp, 32 * dp, 8 * dp, 0);
            textView.setGravity(Gravity.END);

            float shadowDistance = textSize * dp * 0.074f;
            textView.setShadowLayer(0.01f, shadowDistance, shadowDistance, 0x50ffffff);

            textView.setCompoundDrawablePadding(8 * dp);
            if (meIcon == null) meIcon = ResourcesUtils.getPixelDrawable(textView.getContext(), R.drawable.icon_command_send);
            Drawable rightIcon = message.isMe ? meIcon : message.icon;
            iconSize = (int) (textSize * dp * 0.85);
            rightIcon.setBounds(0, 0, iconSize, iconSize);
            textView.setCompoundDrawables(null, null, rightIcon, null);
        } else {
            textView.setPadding(8 * dp, 0, 8 * dp, 0);
            textView.setGravity(Gravity.START);
            textView.setShadowLayer(0, 0, 0, 0);
            textView.setCompoundDrawablePadding(0);
            textView.setCompoundDrawables(null, null, null, null);
        }
    }
    @Override
    synchronized public int getItemCount() {
        return messages.size();
    }

    @Override
    public int getItemViewType(int position) {
        return messages.get(position).isMe() ? 0 : 1;
    }
}
