package vip.cdms.mcoreui.view.show;

import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import vip.cdms.mcoreui.R;
import vip.cdms.mcoreui.util.MCFontWrapper;
import vip.cdms.mcoreui.util.MCTextParser;
import vip.cdms.mcoreui.util.MathUtils;
import vip.cdms.mcoreui.util.ResourcesUtils;
import vip.cdms.mcoreui.util.SoundPlayer;
import vip.cdms.mcoreui.util.ViewUtils;

/**
 * ORE UI风格土司通知
 * @author Cdm2883
 */
public class Toast {
    private String title;
    private String message;

    public Toast setTitle(String title) {
        this.title = title;
        return this;
    }

    public Toast setMessage(String message) {
        this.message = message;
        return this;
    }

    public void show(View view) {
        Context context = view.getContext();
        int dp = MathUtils.dp2px(context, 1);

        Snackbar snackbar = Snackbar.make(
                view,
                MCFontWrapper.WRAPPER.wrap(title + MCTextParser.SS + "r\n" + message),
                Snackbar.LENGTH_SHORT
        ).setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_FADE);

        View snackbarView = snackbar.getView();
        snackbarView.setBackground(ResourcesUtils.getPixelDrawable(context, R.drawable.toast_background));
        ViewUtils.forEach((ViewGroup) snackbarView, view1 -> {
            if (!(view1 instanceof TextView textView)) return;
            textView.setSingleLine(false);
        });

        FrameLayout.LayoutParams snackbarViewParams = (FrameLayout.LayoutParams) snackbarView.getLayoutParams();
        snackbarViewParams.gravity = Gravity.TOP;
        snackbarViewParams.setMargins(
                snackbarViewParams.leftMargin + 8 * dp,
                snackbarViewParams.topMargin + 60 * dp,
                snackbarViewParams.rightMargin + 8 * dp,
                snackbarViewParams.bottomMargin
        );
        snackbarView.setLayoutParams(snackbarViewParams);

        snackbar.show();
        SoundPlayer.play(context, R.raw.toast);
    }
    public void show(Activity activity) {
        show(activity.getWindow().getDecorView());
    }
}
