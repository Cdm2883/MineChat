package vip.cdms.mcoreui.view.show;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

import java.lang.reflect.Field;
import java.util.ResourceBundle;

import vip.cdms.mcoreui.R;
import vip.cdms.mcoreui.util.PixelFont;
import vip.cdms.mcoreui.util.ResourcesUtils;

public class TextView extends AppCompatTextView {
    public TextView(@NonNull Context context) {
        this(context, null);
    }
    public TextView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, android.R.attr.textViewStyle);
    }
    public TextView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray attributes = context.obtainStyledAttributes(attrs, new int[]{ android.R.attr.textColor });
        int color = attributes.getColor(0, 0xffffffff);
        setTextColor(color);
        attributes.recycle();

//        setHighlightColor(0xff589342);
//        setLinkTextColor(0xff589342);
        setLinkTextColor(ResourcesUtils.getColor(context, R.color.text_select_handle_color));
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        if (text instanceof SpannableStringBuilder || text instanceof SpannableString) super.setText(text, type);
        else super.setText(new PixelFont(text), type);
    }

    private int color;
    @Override
    public void setTextColor(int color) {
        super.setTextColor(color);
        this.color = color;
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (enabled) setTextColor(color);
        else super.setTextColor(0xffbfc0c2);
    }

    @Override
    public boolean isFocused() {
        return getEllipsize() == TextUtils.TruncateAt.MARQUEE || super.isFocused();
    }
}
