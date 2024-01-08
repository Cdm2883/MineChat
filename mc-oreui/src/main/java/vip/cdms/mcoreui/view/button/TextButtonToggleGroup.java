package vip.cdms.mcoreui.view.button;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

import vip.cdms.mcoreui.R;
import vip.cdms.mcoreui.util.MathUtils;
import vip.cdms.mcoreui.util.ViewUtils;

public class TextButtonToggleGroup extends LinearLayout {
    private final int dp;
    private final boolean isMini;
    private int position;
    private TextButton.Style style;

    public TextButtonToggleGroup(Context context) {
        this(context, null);
    }
    public TextButtonToggleGroup(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }
    public TextButtonToggleGroup(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }
    public TextButtonToggleGroup(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        dp = MathUtils.dp2px(context, 1);
        setOrientation(HORIZONTAL);
        setBaselineAligned(false);  // 居然遇到了和之前写的一个OreUI的Web组件库一样的问题 (

        TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.TextButtonToggleGroup);
        isMini = attributes.getBoolean(R.styleable.TextButtonToggleGroup_mini, false);
        position = attributes.getInt(R.styleable.TextButtonToggleGroup_defaultPosition, 0);
        attributes.recycle();

        @SuppressLint("CustomViewStyleable") TypedArray attributes1 = context.obtainStyledAttributes(attrs, R.styleable.TextButton);
        switch (attributes1.getInt(R.styleable.TextButton_style, 0)) {
            case 0 -> style = TextButton.Style.COMMON;
            case 1 -> style = TextButton.Style.GRAY;
            case 2 -> style = TextButton.Style.GREEN;
//            case 3 -> style = Style.PINK;
            case 4 -> style = TextButton.Style.PURPLE;
            case 5 -> style = TextButton.Style.RED;
        }
        attributes1.recycle();

        post(this::init);
//        setEnabled(false);
    }

    public void init() { post(() -> {
        ViewGroup.LayoutParams layoutParams = getLayoutParams();
        layoutParams.height = isMini ? 48 * dp : 60 * dp;
        setLayoutParams(layoutParams);

        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            TextButton button = (TextButton) getChildAt(i);
            button.setStyle(style);

            LayoutParams childLayoutParams = (LayoutParams) button.getLayoutParams();
            childLayoutParams.weight = 1;
            if (childCount > 1 && i > 0) childLayoutParams.leftMargin = -2 * dp;
            button.setLayoutParams(childLayoutParams);
        }

        refresh();
    }); }

    public void refresh() {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            TextButton button = (TextButton) getChildAt(i);
            button.setChecked(position == i);
            button.setEnabled(isEnabled());
            int finalI = i;
            button.setOnClickListener(v -> check(finalI));
        }
    }

    public void check(int position) {
        if (this.position == position) return;
        this.position = position;
        for (var listener : onButtonCheckedListeners)
            listener.onButtonChecked(this, position);
        refresh();
    }
    public int getChecked() {
        return position;
    }

    public interface OnButtonCheckedListener {
        void onButtonChecked(TextButtonToggleGroup group, int position);
    }
    private final ArrayList<OnButtonCheckedListener> onButtonCheckedListeners = new ArrayList<>();
    public void addOnButtonCheckedListener(@NonNull OnButtonCheckedListener listener) {
        onButtonCheckedListeners.add(listener);
    }
    public void removeOnButtonCheckedListener(@NonNull OnButtonCheckedListener listener) {
        onButtonCheckedListeners.remove(listener);
    }
}
