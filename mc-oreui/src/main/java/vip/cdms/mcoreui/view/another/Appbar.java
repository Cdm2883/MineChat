package vip.cdms.mcoreui.view.another;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewParent;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.drawerlayout.widget.DrawerLayout;

import java.util.function.Consumer;

import vip.cdms.mcoreui.R;
import vip.cdms.mcoreui.util.IconFont;
import vip.cdms.mcoreui.util.MathUtils;
import vip.cdms.mcoreui.util.PixelFont;
import vip.cdms.mcoreui.util.ResourcesUtils;
import vip.cdms.mcoreui.view.button.IconButton;

/**
 * ORE UI风格应用栏
 * @author Cdm2883
 */
public class Appbar extends LinearLayout {
    private static final int maxButtonHeightDp = 50;

    private final IconButton buttonLeft;
    private final IconButton buttonRight;


    private final TextView titleView;

    public Appbar(@NonNull Context context) {
        this(context, null);
    }
    public Appbar(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }
    public Appbar(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(R.layout.layout_appbar, this, true);

//        setElevation(0);
        setBackground(ResourcesUtils.getPixelDrawable(getContext(), R.drawable.appbar));

        buttonLeft = findViewById(R.id.button_left);
        buttonRight = findViewById(R.id.button_right);

        Consumer<View> buttonSize = view -> {
            int maxHeight = MathUtils.dp2px(getContext(), maxButtonHeightDp);
            int height = view.getHeight();
            int difference = height - maxHeight;
            if (difference > 0) {
                ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) view.getLayoutParams();
                layoutParams.setMargins(
                        difference,
                        difference / 2,
                        difference,
                        difference / 2
                );
                view.setLayoutParams(layoutParams);
            }
        };
        buttonLeft.post(() -> buttonSize.accept(buttonLeft));
        buttonRight.post(() -> buttonSize.accept(buttonRight));

        titleView = findViewById(R.id.title);

        @SuppressLint("CustomViewStyleable") TypedArray attributes = context.obtainStyledAttributes(attrs, com.google.android.material.R.styleable.Toolbar);
        setTitle(attributes.getString(com.google.android.material.R.styleable.Toolbar_title));

        Drawable navigationIcon = attributes.getDrawable(com.google.android.material.R.styleable.Toolbar_navigationIcon);
        if (navigationIcon != null) {
            buttonLeft.setImageDrawable(navigationIcon);
            buttonLeft.setVisibility(VISIBLE);
        }
        Drawable collapseIcon = attributes.getDrawable(com.google.android.material.R.styleable.Toolbar_collapseIcon);
        if (collapseIcon != null) {
            buttonRight.setImageDrawable(collapseIcon);
            buttonRight.setVisibility(VISIBLE);
        }

        attributes.recycle();
        setLeftButtonOnClickListener(null);
    }

    public void setTitle(CharSequence charSequence) {
        if (charSequence == null || charSequence.length() == 0) {
            Drawable background = ResourcesUtils.getPixelDrawable(getContext(), R.drawable.appbar);
            background.setAlpha(0);
            setBackground(background);
            return;
        }
        titleView.setText(
                IconFont.WRAPPER.wrap(
                        new PixelFont(charSequence, true)));
    }

    public void setLeftIcon(Drawable drawable) {
        if (drawable == null) return;
        buttonLeft.setImageDrawable(drawable.mutate());
    }
    public void setLeftIconColor(@ColorInt int color) {
        buttonLeft.setImageTintList(ColorStateList.valueOf(color));
    }

    public void setRightIcon(Drawable drawable) {
        if (drawable == null) return;
        buttonRight.setImageDrawable(drawable.mutate());
    }
    public void setRightIconColor(@ColorInt int color) {
        buttonRight.setImageTintList(ColorStateList.valueOf(color));
    }

    public IconButton getLeftButton() {
        return buttonLeft;
    }
    public IconButton getRightButton() {
        return buttonRight;
    }

    public void setLeftButtonOnClickListener(OnClickListener onClickListener) {
        buttonLeft.setOnClickListener(v -> {
            if (onClickListener != null) onClickListener.onClick(v);

            View parent = this;
            DrawerLayout drawerLayout = null;
            do {
                ViewParent mParent = parent.getParent();
                if (!(mParent instanceof View)) break;
                parent = (View) mParent;
                if (parent instanceof DrawerLayout layout) drawerLayout = layout;
            } while (drawerLayout == null);

            if (drawerLayout != null) {
                if (drawerLayout.isOpen())
                    drawerLayout.close();
                else
                    drawerLayout.open();
            }

        });
    }

    public void setRightButtonOnClickListener(OnClickListener onClickListener) {
        buttonRight.setOnClickListener(onClickListener);
    }
}
