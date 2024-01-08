package vip.cdms.mcoreui.view.input;

import static vip.cdms.mcoreui.util.MCTextParser.SS;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import vip.cdms.mcoreui.R;
import vip.cdms.mcoreui.util.IconFont;
import vip.cdms.mcoreui.util.MCFontWrapper;
import vip.cdms.mcoreui.util.MCTextParser;
import vip.cdms.mcoreui.util.MathUtils;
import vip.cdms.mcoreui.util.PixelFont;
import vip.cdms.mcoreui.util.ResourcesUtils;
import vip.cdms.mcoreui.util.ViewUtils;
import vip.cdms.mcoreui.view.button.TextButton;
import vip.cdms.mcoreui.view.dialog.DialogBuilder;

/**
 * ORE UI风格编辑框
 * @author Cdm2883
 */
public class EditText extends AppCompatEditText implements TextWatcher {
    private int dp;
    private String text = "";
    public EditText(@NonNull Context context) {
        this(context, null);
    }
    public EditText(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, androidx.appcompat.R.attr.editTextStyle);
    }
    public EditText(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        dp = MathUtils.dp2px(context, 1);
        setTextColor(0xffffffff);
        setTextSize(18);
        setBackground(ResourcesUtils.getPixelDrawable(context, R.drawable.input_text_background));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            setTextCursorDrawable(new ColorDrawable(ResourcesUtils.getColor(context, R.color.text_select_handle_color)));
        }
        addTextChangedListener(this);

        setHintTextColor(0xffb0b1b4);

        setCustomInsertionActionModeCallback(new ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                menu.add(Menu.NONE, 114514, Menu.NONE, SS);
                if (!IconFont.TABLE.isEmpty())
                    menu.add(Menu.NONE, 1919810, Menu.NONE, IconFont.WRAPPER.wrap(
                            String.valueOf(IconFont.TABLE.entrySet().iterator().next().getKey())));
                return true;
            }
            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }
            @SuppressLint("SetTextI18n")
            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == 114514) {
                    int start = getSelectionStart();
                    String text = String.valueOf(getText());
                    setText(text.substring(0,start) + SS + text.substring(start));
                    setSelection(Math.min(start + 1, Objects.requireNonNull(getText()).length()));
                    return true;
                } else if (itemId == 1919810) {
                    DialogBuilder dialogBuilder = new DialogBuilder(context);
                    RecyclerView recyclerView = new RecyclerView(context);
                    recyclerView.setClipToPadding(false);
                    recyclerView.setPadding(8 * dp, 8 * dp, 8 * dp, 8 * dp);
                    recyclerView.setLayoutManager(new GridLayoutManager(context, 4));
                    ViewUtils.setOreUIVerticalScrollBar(recyclerView);
                    Adapter adapter = new Adapter(character -> {
                        int start = getSelectionStart();
                        String text = String.valueOf(getText());
                        setText(text.substring(0,start) + character + text.substring(start));
                        setSelection(Math.min(start + 1, Objects.requireNonNull(getText()).length()));
                        dialogBuilder.cancel();
                    });
                    recyclerView.setAdapter(adapter);
                    adapter.entries.addAll(IconFont.TABLE.entrySet());
                    adapter.notifyItemRangeInserted(0, adapter.entries.size());
                    dialogBuilder.setCenter(recyclerView);
                    dialogBuilder.show();
                    return true;
                }
                return false;
            }
            @Override
            public void onDestroyActionMode(ActionMode mode) {

            }
        });
    }
    private class Adapter extends RecyclerView.Adapter<Adapter.Holder> {
        final Consumer<Character> callback;
        Adapter(Consumer<Character> callback) {
            this.callback = callback;
        }
        final List<Map.Entry<Character, Bitmap>> entries = new ArrayList<>();
        class Holder extends RecyclerView.ViewHolder {
            public Holder(@NonNull View itemView) {
                super(itemView);
            }
        }
        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            TextButton button = new TextButton(getContext());
            button.setLayoutParams(new GridLayoutManager.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            return new Holder(button);
        }
        @Override
        public void onBindViewHolder(@NonNull Holder holder, int position) {
            Map.Entry<Character, Bitmap> entry = entries.get(position);
            char character = entry.getKey();
            Bitmap icon = entry.getValue();

            SpannableStringBuilder sp = new SpannableStringBuilder("0");
            sp.setSpan(new ImageSpan(IconFont.make(icon)), 0, sp.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);

            TextButton button = (TextButton) holder.itemView;
            GridLayoutManager.LayoutParams layoutParams = (GridLayoutManager.LayoutParams) button.getLayoutParams();
            layoutParams.setMargins(2 * dp, 2 * dp, 2 * dp, 2 * dp);
            button.setLayoutParams(layoutParams);
            button.setOnClickListener(v -> callback.accept(character));
            button.setText(sp);
        }
        @Override
        public int getItemCount() {
            return entries.size();
        }
    };

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        setBackground(ResourcesUtils.getPixelDrawable(getContext(), enabled ? R.drawable.input_text_background : R.drawable.input_text_disabled));
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }
    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }
    @Override
    public void afterTextChanged(Editable s) {
        int start = getSelectionStart();
        int end = getSelectionEnd();
        text = String.valueOf(getText());

        CharSequence texted = keepText(text);
        removeTextChangedListener(this);
        setText(texted);
        setSelection(start, end);
        addTextChangedListener(this);
        if (getHint() != null) {
            CharSequence hinted = MCFontWrapper.WRAPPER.wrap(String.valueOf(getHint()));
            setHint(hinted);
        }
    }

    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect);

        removeTextChangedListener(this);

        if (!focused) {
            setText(MCFontWrapper.WRAPPER.wrap(String.valueOf(text)));
            return;
        }
        setText(keepText(text));

        addTextChangedListener(this);
    }

    public static CharSequence keepText(String text) {
        return IconFont.WRAPPER.wrap(
                PixelFont.WRAPPER.wrap(
                        MCTextParser.text2CharSequence(text, true)));
    }
}
