package vip.cdms.mcoreui.view.input;

import android.content.Context;
import android.text.Html;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Arrays;

import vip.cdms.mcoreui.R;
import vip.cdms.mcoreui.util.ImageUtils;
import vip.cdms.mcoreui.util.MathUtils;
import vip.cdms.mcoreui.util.ResourcesUtils;
import vip.cdms.mcoreui.view.button.TextButton;

/**
 * ORE UI风格下拉选择
 * @author Cdm2883
 */
public class Dropdown extends LinearLayout {
    private final int dp;

    public Dropdown(Context context) {
        this(context, null);
    }
    public Dropdown(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }
    public Dropdown(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }
    public Dropdown(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setOrientation(VERTICAL);
        dp = MathUtils.dp2px(context, 1);
    }

    private final TextButton.Style BUTTON_STYLE = new TextButton.Style(R.drawable.button_text_common_background, R.drawable.button_text_common_hovered_background, R.drawable.button_text_common_hovered_background, -1, 0xff202022);
    private final TextButton.Style BUTTON_STYLE_TOP = new TextButton.Style(R.drawable.button_text_dropdown_top, R.drawable.button_text_dropdown_top_hovered, R.drawable.button_text_dropdown_top_pressed, -1, 0xffffffff);
    private final TextButton.Style BUTTON_STYLE_CENTER = new TextButton.Style(R.drawable.button_text_dropdown_center, R.drawable.button_text_dropdown_center_hovered, R.drawable.button_text_dropdown_center_pressed, -1, 0xffffffff);
    private final TextButton.Style BUTTON_STYLE_BOTTOM = new TextButton.Style(R.drawable.button_text_dropdown_bottom, R.drawable.button_text_dropdown_bottom_hovered, R.drawable.button_text_dropdown_bottom_pressed, -1, 0xffffffff);

    private ArrayList<CharSequence> items;
    public void setItemList(ArrayList<CharSequence> items) {
        this.items = items;
        collapse();
    }
    public void setItemList(CharSequence[] items) {
        setItemList(new ArrayList<>(Arrays.asList(items)));
    }


    private int selected = 0;
    public int getSelected() {
        if (items.isEmpty()) return -1;
        return selected;
    }
    public void select(int position) {
        if (position < 0 || position >= items.size()) return;
        selected = position;
        collapse();
    }

//    private int bottomMargin = -1;
    private boolean expanded;
    public void expand() {
        removeAllViews();

        for (int i = 0; i < items.size(); i++) {
            int finalI = i;
            CharSequence text = items.get(i);

            TextButton button = new TextButton(getContext());
            button.setLayoutParams(
                    new LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            55 * dp
                    )
            );
            if (i == 0) button.setStyle(BUTTON_STYLE_TOP);
            else if (i == items.size() - 1) button.setStyle(BUTTON_STYLE_BOTTOM);
            else button.setStyle(BUTTON_STYLE_CENTER);
            button.setCompoundDrawables(null, null,
                    ResourcesUtils.getDrawable(getContext(), i == selected ? R.drawable.icon_check : R.drawable.icon_empty),
            null);
            button.setText(text);
            button.setOnClickListener(v -> {
                if (mOnItemSelectedListener != null) mOnItemSelectedListener.onItemSelected(v, finalI, selected == finalI);
                if (mOnClickListener != null) mOnClickListener.onClick(v);
                selected = finalI;
                collapse();
            });
            addView(button);
        }

// todo 完善
//        ViewGroup.LayoutParams params = getLayoutParams();
//        if (params instanceof LinearLayout.LayoutParams layoutParams) {
//            bottomMargin = layoutParams.bottomMargin;
//            layoutParams.setMargins(
//                    layoutParams.leftMargin,
//                    layoutParams.topMargin,
//                    layoutParams.rightMargin,
//                    layoutParams.bottomMargin - 55 * (items.size() - 1) * dp + 5 * dp
//            );
//            setLayoutParams(layoutParams);
//            setElevation(1000);
//        }

        expanded = true;
    }
    public void collapse() {
        removeAllViews();

        TextButton button = new TextButton(getContext());
        button.setLayoutParams(new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        button.setStyle(BUTTON_STYLE);
        button.setCompoundDrawables(null, null, ImageUtils.rotateDrawable(
                ResourcesUtils.getPixelDrawable(getContext(), R.drawable.icon_back), 90 * 3
        ), null);
        if (items.isEmpty()) button.setText(Html.fromHtml("<font color=\"red\">----------<font/>", Html.FROM_HTML_MODE_LEGACY));
        else button.setText(items.get(selected));
        button.setOnClickListener(v -> {
            if (mOnClickListener != null) mOnClickListener.onClick(v);
            expand();
        });

        addView(button);

//        if (bottomMargin != -1) {
//            ViewGroup.LayoutParams params = getLayoutParams();
//            if (params instanceof LinearLayout.LayoutParams layoutParams) {
//                layoutParams.setMargins(
//                        layoutParams.leftMargin,
//                        layoutParams.topMargin,
//                        layoutParams.rightMargin,
//                        bottomMargin
//                );
//                setLayoutParams(layoutParams);
//                setElevation(0);
//            }
//        }


        expanded = false;
    }
    public boolean isExpanded() {
        return expanded;
    }

    private OnClickListener mOnClickListener = null;
    @Override
    public void setOnClickListener(@Nullable OnClickListener l) {
        mOnClickListener = l;
    }

    public interface OnItemSelectedListener {
        void onItemSelected(View view, int position, boolean selectNothing);
    }
    private OnItemSelectedListener mOnItemSelectedListener = null;
    public void setOnItemSelectedListener(OnItemSelectedListener listener) {
        mOnItemSelectedListener = listener;
    }
}
