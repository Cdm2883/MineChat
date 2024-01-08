package vip.cdms.mcoreui.view.dialog;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import android.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;

import vip.cdms.mcoreui.R;
import vip.cdms.mcoreui.util.MathUtils;
import vip.cdms.mcoreui.util.SoundPlayer;
import vip.cdms.mcoreui.util.ViewUtils;
import vip.cdms.mcoreui.view.button.IconButton;
import vip.cdms.mcoreui.view.button.TextButton;
import vip.cdms.mcoreui.view.show.TextView;

/**
 * ORE UI风格弹窗构造器
 * @author Cdm2883
 */
public class DialogBuilder {
    private final Context context;
    private final AlertDialog.Builder alertDialogBuilder;
    private final int dp;

    private final LinearLayout layout;
    private final ConstraintLayout constraintLayout;
    private final TextView title;
    private final View closeButton;
    private final IconButton cancelButton;
    private final LinearLayout center;
    private final ScrollView scrollView;
    private final LinearLayout content;
    private final TextView text;
    private final LinearLayout buttons;

    public DialogBuilder(Context context) {
        this.context = context;
        alertDialogBuilder = new AlertDialog.Builder(context);
        dp = MathUtils.dp2px(context, 1);

        layout = (LinearLayout) LinearLayout.inflate(context, R.layout.layout_dialog, null);
        constraintLayout = (ConstraintLayout) layout.getChildAt(0);
        title = layout.findViewById(R.id.dialog_title);
        closeButton = layout.findViewById(R.id.dialog_button_close);
        cancelButton = layout.findViewById(R.id.dialog_button_cancel);
        center = layout.findViewById(R.id.dialog_center);
        scrollView = layout.findViewById(R.id.dialog_scroll_view);
        content = layout.findViewById(R.id.dialog_content);
        text = layout.findViewById(R.id.dialog_text);
        buttons = layout.findViewById(R.id.dialog_buttons);

        ViewUtils.setOreUIVerticalScrollBar(scrollView);
    }

    public DialogBuilder setTitle(CharSequence title) {
        this.title.setText(title);
        return this;
    }


    public DialogBuilder setCenter(View view) {
        center.removeAllViews();
        center.addView(view);
        return this;
    }

    public DialogBuilder setContent(CharSequence content) {
        return setContent(content, true);
    }
    public DialogBuilder setContent(CharSequence content, boolean center) {
        text.setText(content);
        text.setGravity(center ? Gravity.CENTER : Gravity.LEFT);
        return this;
    }
    public DialogBuilder setContent(View view) {
        scrollView.removeAllViews();
        scrollView.addView(view);
        return this;
    }
    public DialogBuilder addContent(View view) {
        content.removeView(text);
        content.addView(view);
        return this;
    }

    public DialogBuilder addAction(CharSequence text) {
        return addAction(text, TextButton.Style.COMMON);
    }
    public DialogBuilder addAction(CharSequence text, View.OnClickListener listener) {
        return addAction(text, TextButton.Style.COMMON, false, listener);
    }
    public DialogBuilder addAction(CharSequence text, TextButton.Style style) {
        return addAction(text, style, false, null);
    }
    public DialogBuilder addAction(CharSequence text, TextButton.Style style, boolean bold) {
        return addAction(text, style, bold, null);
    }
    public DialogBuilder addAction(CharSequence text, TextButton.Style style, View.OnClickListener listener) {
        return addAction(text, style, false, listener);
    }
    public DialogBuilder addAction(CharSequence text, TextButton.Style style, boolean bold, View.OnClickListener listener) {
        TextButton button = new TextButton(context);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                48 * dp
        );
        if (buttons.getChildCount() > 0) layoutParams.topMargin = 4 * dp;
        button.setLayoutParams(layoutParams);

        button.setOnClickListener(listener);
        button.setText(text);
        button.setStyle(style);
        button.setBold(bold);
        button.post(() -> {
            if (button.getLineCount() > 1) {
                layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                MotionEvent downEvent = MotionEvent.obtain(0, 0, MotionEvent.ACTION_DOWN, 0, 0, 0);
                button.onTouchEvent(downEvent);
                button.refresh();
                downEvent.recycle();
            }
        });
        return addAction(button);
    }
    public DialogBuilder addAction(View view) {
        if (view == null) return this;
        buttons.addView(view);
        return this;
    }
    public DialogBuilder setAction(View view) {
        buttons.removeAllViews();
        return addAction(view);
    }

    public DialogBuilder setCancelable(boolean cancelable) {
        cancelButton.setVisibility(cancelable ? View.VISIBLE : View.GONE);
        return this;
    }

    public void close() {
        closeButton.callOnClick();
    }
    public void cancel() {
        cancelButton.callOnClick();
    }

    private Runnable onCancelListener;
    public DialogBuilder setOnCancelListener(Runnable onCancelListener) {
        this.onCancelListener = onCancelListener;
        return this;
    }


    @SuppressLint("ClickableViewAccessibility")
    public AlertDialog create() {
        if (context instanceof Activity activity && activity.isFinishing()) return null;

        constraintLayout.post(() -> {
            int height = constraintLayout.getHeight();
            int maxHeight = context.getResources().getDisplayMetrics().heightPixels / 2;
            if (height > maxHeight)
                constraintLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, maxHeight));
//            else
//                constraintLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

            layout.setAlpha(1);
//            TimeUtils.setTimeout(() -> layout.post(() -> layout.setAlpha(1)), 250);
        });

        AlertDialog alertDialog = alertDialogBuilder
                .setCancelable(false)
                .setView(layout)
                .create();
        layout.setAlpha(0);

        closeButton.setOnClickListener(v -> alertDialog.cancel());
        cancelButton.setOnClickListener(v -> {
            alertDialog.cancel();
            if (onCancelListener != null) onCancelListener.run();
        });
        ViewUtils.forEach(layout, view -> {
            if (view instanceof SoundPlayer.ClickSound clickSound)
                view.setOnTouchListener((v, event) -> {
                    if (event.getAction() == MotionEvent.ACTION_DOWN)
                        SoundPlayer.play(context, clickSound.getClickSoundRawResId());
                    return false;
                });
        });
        if (buttons.getChildCount() == 0)
            buttons.setVisibility(View.GONE);
        else
            ViewUtils.forEach(buttons, view ->
                    ViewUtils.addOnClickListener(view, v ->
                            alertDialog.cancel()));

        Window window = alertDialog.getWindow();
        assert window != null;
        window.setBackgroundDrawableResource(android.R.color.transparent);
        window.setLayout(
                Math.min(context.getResources().getDisplayMetrics().widthPixels - 32 * dp, 1024),
                WindowManager.LayoutParams.WRAP_CONTENT
        );
        return alertDialog;
    }

    @SuppressLint("ClickableViewAccessibility")
    public AlertDialog show() {
        AlertDialog alertDialog = create();
        if (alertDialog != null) alertDialog.show();
        return alertDialog;
    }
}
