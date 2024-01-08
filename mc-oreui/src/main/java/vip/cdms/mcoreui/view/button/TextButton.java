package vip.cdms.mcoreui.view.button;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.drawable.Drawable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.Checkable;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;

import vip.cdms.mcoreui.R;
import vip.cdms.mcoreui.util.ImageUtils;
import vip.cdms.mcoreui.util.MathUtils;
import vip.cdms.mcoreui.util.PixelDrawable;
import vip.cdms.mcoreui.util.PixelFont;
import vip.cdms.mcoreui.util.ResourcesUtils;
import vip.cdms.mcoreui.util.SoundPlayer;

/**
 * ORE UI风格文字
 * @author Cdm2883
 */
public class TextButton extends AppCompatButton implements Checkable, SoundPlayer.ClickSound {
    private final int dp;
    public TextButton(@NonNull Context context) {
        this(context, null);
    }
    public TextButton(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, androidx.appcompat.R.attr.buttonStyle);
    }
    public TextButton(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        dp = MathUtils.dp2px(context, 1);

        TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.TextButton);

        switch (attributes.getInt(R.styleable.TextButton_style, 0)) {
            case 0 -> setStyle(Style.COMMON);
            case 1 -> setStyle(Style.GRAY);
            case 2 -> setStyle(Style.GREEN);
//            case 3 -> setStyle(Style.PINK);
            case 4 -> setStyle(Style.PURPLE);
            case 5 -> setStyle(Style.RED);
        }
        setBold(attributes.getBoolean(R.styleable.TextButton_bold, false));
        setChecked(attributes.getBoolean(R.styleable.TextButton_checked, false));
        setEnabled(!attributes.getBoolean(R.styleable.TextButton_disabled, false));

        attributes.recycle();

        setAllCaps(false);
        refresh();

        if (attrs != null) post(() -> {
            Drawable[] compoundDrawables = getCompoundDrawables();
            setCompoundDrawables(
                    compoundDrawables[0],
                    compoundDrawables[1],
                    compoundDrawables[2],
                    compoundDrawables[3]
            );
        });
    }

    /** 重新绘制view */
    public void refresh() {
        lastTouchX = lastTouchY = -1;
        preClick = true;

        Style style = isEnabled() ? this.style : Style.DISABLE;

        if (bold) {
            setTextSize(20);
            setShadowLayer(0.01f, 6, 6, 0x50000000);
        } else {
            setTextSize(18);
            setShadowLayer(0, 0, 0, 0);
        }

//        setText(getText().toString());
        setBackground(ResourcesUtils.getPixelDrawable(getContext(), style.background));
        setTextColor(style.textColor);
    }
    @Override
    public void setBackground(Drawable background) {
        if (isChecked() && !isEnabled()) background = ResourcesUtils.getPixelDrawable(getContext(), Style.DISABLE.enabled);
        else if (isChecked()) background = ResourcesUtils.getPixelDrawable(getContext(), style.enabled);
        super.setBackground(background);
    }
    @Override
    public int getClickSoundRawResId() {
        return bold ? R.raw.button_click_pop : R.raw.button_click;
    }

    private float lastTouchX;
    private float lastTouchY;
    private boolean preClick;
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled()) return true;
        final float x = event.getX();
        final float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                setBackground(ResourcesUtils.getPixelDrawable(getContext(), style.pressed));
                break;
            case MotionEvent.ACTION_MOVE:
                if (
                        (lastTouchX != -1 && lastTouchY != -1)
                                && (
                                Math.abs(lastTouchX - x) > 3
                                        || Math.abs(lastTouchY - y) > 3
                        )
                ) {
                    preClick = false;
                    setBackground(ResourcesUtils.getPixelDrawable(getContext(), style.hovered));
                } else {
                    lastTouchX = x;
                    lastTouchY = y;
                }
                break;
            case MotionEvent.ACTION_UP:
                if (preClick) performClick();
                refresh();
            case MotionEvent.ACTION_CANCEL:
                refresh();
                break;
        }

        return true;
//        return super.onTouchEvent(event);
    }
    @Override
    public void setOnLongClickListener(@Nullable OnLongClickListener l) {
        throw new RuntimeException();
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        if (text == null) text = "null";
        if (text instanceof SpannableString || text instanceof SpannableStringBuilder) super.setText(text, type);
        else super.setText(new PixelFont(text, bold), type);
    }

    @Override
    public void setCompoundDrawables(@Nullable Drawable left, @Nullable Drawable top, @Nullable Drawable right, @Nullable Drawable bottom) {
        if (
                !(
                        right == null
                        || (right instanceof PixelDrawable pixelDrawable && pixelDrawable.isEmpty())
                )
        ) {
            setPaddingRelative(
                    20 * dp,
                    getPaddingTop(),
                    8 * dp,
                    getPaddingBottom()
            );
            setTextAlignment(TEXT_ALIGNMENT_TEXT_START);
        } else setTextAlignment(TEXT_ALIGNMENT_CENTER);

        super.setCompoundDrawables(
                left instanceof ImageUtils.RotatedDrawable ? left : ImageUtils.setBounds(PixelDrawable.instance(left)),
                top instanceof ImageUtils.RotatedDrawable ? top : ImageUtils.setBounds(PixelDrawable.instance(top)),
                right instanceof ImageUtils.RotatedDrawable ? right : ImageUtils.setBounds(PixelDrawable.instance(right)),
                bottom instanceof ImageUtils.RotatedDrawable ? bottom : ImageUtils.setBounds(PixelDrawable.instance(bottom))
        );
    }

    /**
     * 按钮样式
     * @param background 正常显示状态的背景
     * @param hovered 悬浮在按钮上显示的背景
     * @param pressed 按钮按下时的背景
     * @param enabled 按钮被选择时的背景
     * @param textColor 按钮上文字的颜色
     */
    public record Style(
            @DrawableRes int background,
            @DrawableRes int hovered,
            @DrawableRes int pressed,
            @DrawableRes int enabled,
            @ColorInt int textColor
    ) {
        public static final Style COMMON = new Style(R.drawable.button_text_common_background, R.drawable.button_text_common_hovered_background, R.drawable.button_text_common_pressed_background, R.drawable.button_text_common_enabled_background, 0xff202022);
        public static final Style GRAY = new Style(R.drawable.button_text_gray_background, R.drawable.button_text_gray_hovered_background, R.drawable.button_text_gray_pressed_background, R.drawable.button_text_gray_enabled_background, 0xffffffff);
        public static final Style GREEN = new Style(R.drawable.button_text_green_background, R.drawable.button_text_green_hovered_background, R.drawable.button_text_green_pressed_background, R.drawable.button_text_green_enabled_background, 0xffffffff);
        //            public static final Style PINK = new Style(-1, -1, -1, -1, -1);  // 之前版本有这个按钮的, 做的时候没找到, 就先不做了, 之后再补 (逃;
        public static final Style PURPLE = new Style(R.drawable.button_text_purple_background, R.drawable.button_text_purple_hovered_background, R.drawable.button_text_purple_pressed_background, R.drawable.button_text_purple_enabled_background, 0xffffffff);
        public static final Style RED = new Style(R.drawable.button_text_red_background, R.drawable.button_text_red_hovered_background, R.drawable.button_text_red_pressed_background, R.drawable.button_text_red_enabled_background, 0xffffffff);

        private static final Style DISABLE = new Style(R.drawable.button_text_disable, -1, -1, R.drawable.button_text_disable_enable, 0xff4a4b4c);
    }
    private Style style;
    public void setStyle(Style style) {
        if (this.style == style || style == null) return;
        this.style = style;
        refresh();
    }

    // 设置被选择样式 (enable)
    private boolean checked = false;
    @Override
    public void setChecked(boolean b) {
        checked = b;
        refresh();
    }
    @Override
    public boolean isChecked() {
        return checked;
    }
    @Override
    public void toggle() {
        setChecked(!isChecked());
    }

    // 设置禁用样式
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        refresh();
    }

    // 文字阴影 + 字号加大 + 气泡点击音效
    private boolean bold = false;
    public void setBold(boolean bold) {
        this.bold = bold;
        refresh();
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.setDrawFilter(
                new PaintFlagsDrawFilter(Paint.FILTER_BITMAP_FLAG, 0)
        );
        super.draw(canvas);
    }
}
