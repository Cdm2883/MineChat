package vip.cdms.mcoreui.view.dialog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Consumer;
import java.util.function.Function;

import vip.cdms.mcoreui.R;
import vip.cdms.mcoreui.util.FontWrapper;
import vip.cdms.mcoreui.util.MathUtils;
import vip.cdms.mcoreui.util.SoundPlayer;
import vip.cdms.mcoreui.util.ViewUtils;
import vip.cdms.mcoreui.view.button.TextButton;
import vip.cdms.mcoreui.view.input.Dropdown;
import vip.cdms.mcoreui.view.input.EditText;
import vip.cdms.mcoreui.view.input.Slider;
import vip.cdms.mcoreui.view.input.Switch;
import vip.cdms.mcoreui.view.show.TextView;

/**
 * ORE UI风格自定义表单构造器
 * @author Cdm2883
 */
public class CustomFormBuilder implements FormBuilder<CustomFormBuilder, JsonArray> {
    public interface InputHolder extends FormBuilder.Holder {
        void setOnValueChangeListener(Consumer<JsonElement> listener);
        @NonNull
        JsonElement getValue();
    }
    public interface SimpleTextWatcher extends TextWatcher {
        default void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        void onTextChanged(CharSequence s, int start, int before, int count);
        default void afterTextChanged(Editable s) {}
    }

    public CustomFormBuilder() {}
    @Override
    public CustomFormBuilder loadFormJson(JsonObject json) {
        if (!json.get("type").getAsString().equals("custom_form"))
            throw new RuntimeException(new IllegalArgumentException("Need type custom_form, given " + json.get("type").getAsString()));

        Function<JsonElement, Integer> jsonIntegerOrNull = jsonElement -> jsonElement == null ? null : jsonElement.getAsInt();
        Function<JsonElement, Boolean> jsonBooleanOrNull = jsonElement -> jsonElement == null ? null : jsonElement.getAsBoolean();
        Function<JsonElement, String> jsonStringOrNull = jsonElement -> jsonElement == null ? null : jsonElement.getAsString();
        Function<JsonArray, CharSequence[]> jsonArray2CharSequences = jsonArray -> {
            ArrayList<CharSequence> result = new ArrayList<>();
            for (var jsonElement : jsonArray)
                result.add(jsonElement.getAsString());
            return result.toArray(new CharSequence[0]);
        };

        setTitle(json.get("title").getAsString());
        for (var jsonElement : json.get("content").getAsJsonArray()) {
            if (!(jsonElement instanceof JsonObject element))
                throw new RuntimeException(new IllegalArgumentException(jsonElement.toString()));
            switch (element.get("type").getAsString()) {
                case "label" -> addLabel(
                        element.get("text").getAsString()
                );
                case "input" -> addInput(
                        element.get("text").getAsString(),
                        jsonStringOrNull.apply(element.get("placeholder")),
                        jsonStringOrNull.apply(element.get("default"))
                );
                case "toggle" -> addSwitch(
                        element.get("text").getAsString(),
                        jsonBooleanOrNull.apply(element.get("default"))
                );
                case "dropdown" -> addDropdown(
                        element.get("text").getAsString(),
                        jsonArray2CharSequences.apply(element.get("options").getAsJsonArray()),
                        jsonIntegerOrNull.apply(element.get("default"))
                );
                case "slider" -> addSlider(
                        element.get("text").getAsString(),
                        element.get("min").getAsInt(),
                        element.get("max").getAsInt(),
                        jsonIntegerOrNull.apply(element.get("step")),
                        jsonIntegerOrNull.apply(element.get("default"))
                );
                case "step_slider" -> addStepSlider(
                        element.get("text").getAsString(),
                        jsonArray2CharSequences.apply(element.get("steps").getAsJsonArray()),
                        jsonIntegerOrNull.apply(element.get("default"))
                );
            }
        }
        return this;
    }

    private FontWrapper fontWrapper = FontWrapper.NON;
    @Override
    public CustomFormBuilder setFontWrapper(FontWrapper fontWrapper) {
        this.fontWrapper = fontWrapper;
        return this;
    }

    private CharSequence title = "";
    @Override
    public CustomFormBuilder setTitle(CharSequence title) {
        this.title = title;
        return this;
    }

    private final ArrayList<InputHolder> elements = new ArrayList<>();
    public ArrayList<InputHolder> getElements() {
        return elements;
    }
    public CustomFormBuilder addElement(InputHolder element) {
        elements.add(element);
        return this;
    }

    private final HashMap<Integer, Consumer<JsonElement>> inputs = new HashMap<>();
    /** @noinspection unchecked*/
    public <J extends JsonElement> CustomFormBuilder input(Consumer<J> callback) {
        inputs.put(elements.size() - 1, (Consumer<JsonElement>) callback);
        return this;
    }
    // 更方便使用
    public <J extends JsonElement> CustomFormBuilder input(Class<?> ignored, Consumer<J> callback) {
        return input(callback);
    }

    public CustomFormBuilder addLabel(CharSequence text) {
        return addElement(new InputHolder() {
            @Override
            public void setOnValueChangeListener(Consumer<JsonElement> listener) {}
            @NonNull
            @Override
            public JsonElement getValue() {
                return JsonNull.INSTANCE;
            }
            @Override
            public View buildView(Context context) {
                TextView textView = new TextView(context);
                textView.setTextSize(18);
                textView.setText(fontWrapper.wrap(text));
                return textView;
            }
        });
    }
    public CustomFormBuilder addInput(CharSequence title, CharSequence placeholder, String defaultValue) {
        if (defaultValue == null) defaultValue = "";
        String finalDefaultValue = defaultValue;
        return addElement(new InputHolder() {
            Consumer<JsonElement> onValueChangeListener;
            @Override
            public void setOnValueChangeListener(Consumer<JsonElement> listener) {
                onValueChangeListener = listener;
            }
            JsonElement value = new JsonPrimitive(finalDefaultValue);
            @NonNull
            @Override
            public JsonElement getValue() {
                return value;
            }
            @Override
            public View buildView(Context context) {
                LinearLayout root = new LinearLayout(context);
                root.setOrientation(LinearLayout.VERTICAL);

                TextView titleView = new TextView(context);
                titleView.setTextSize(18);
                titleView.setText(fontWrapper.wrap(title));
                root.addView(titleView);

                EditText inputView = new EditText(context);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                layoutParams.topMargin = MathUtils.dp2px(context, 4);
                inputView.setLayoutParams(layoutParams);
                inputView.addTextChangedListener((SimpleTextWatcher) (s, start, before, count) -> {
                    value = new JsonPrimitive(s.toString());
                    if (onValueChangeListener != null) onValueChangeListener.accept(value);
                });
                if (placeholder != null) inputView.setHint(fontWrapper.wrap(placeholder));
                inputView.setText(finalDefaultValue);
                root.addView(inputView);

                return root;
            }
        });
    }
    public CustomFormBuilder addSwitch(CharSequence title, Boolean defaultValue) {
        if (defaultValue == null) defaultValue = false;
        boolean finalDefaultValue = defaultValue;
        return addElement(new InputHolder() {
            Consumer<JsonElement> onValueChangeListener;
            @Override
            public void setOnValueChangeListener(Consumer<JsonElement> listener) {
                onValueChangeListener = listener;
            }
            JsonElement value = new JsonPrimitive(finalDefaultValue);
            @NonNull
            @Override
            public JsonElement getValue() {
                return value;
            }
            @Override
            public View buildView(Context context) {
                LinearLayout root = new LinearLayout(context);
                root.setOrientation(LinearLayout.HORIZONTAL);

                Switch switchView = new Switch(context);
                switchView.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    value = new JsonPrimitive(isChecked);
                    if (onValueChangeListener != null) onValueChangeListener.accept(value);
                });
                switchView.setChecked(finalDefaultValue);
                root.addView(switchView);

                TextView titleView = new TextView(context);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                layoutParams.leftMargin = MathUtils.dp2px(context, 8);
                layoutParams.topMargin = MathUtils.dp2px(context, 3);
                titleView.setLayoutParams(layoutParams);
                titleView.setTextSize(18);
                titleView.setText(fontWrapper.wrap(title));
                root.addView(titleView);

                return root;
            }
        });
    }
    public CustomFormBuilder addDropdown(CharSequence title, CharSequence[] items, Integer defaultValue) {
        if (defaultValue == null) defaultValue = 0;
        int finalDefaultValue = defaultValue;
        return addElement(new InputHolder() {
            Consumer<JsonElement> onValueChangeListener;
            @Override
            public void setOnValueChangeListener(Consumer<JsonElement> listener) {
                onValueChangeListener = listener;
            }
            JsonElement value = new JsonPrimitive(finalDefaultValue);
            @NonNull
            @Override
            public JsonElement getValue() {
                return value;
            }
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public View buildView(Context context) {
                LinearLayout root = new LinearLayout(context);
                root.setOrientation(LinearLayout.VERTICAL);

                TextView titleView = new TextView(context);
                titleView.setTextSize(18);
                titleView.setText(fontWrapper.wrap(title));
                root.addView(titleView);

                Dropdown dropdownView = new Dropdown(context);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                layoutParams.topMargin = MathUtils.dp2px(context, 4);
                dropdownView.setLayoutParams(layoutParams);
                dropdownView.setOnItemSelectedListener((view, position, selectNothing) -> {
                    value = new JsonPrimitive(position);
                    if (onValueChangeListener != null) onValueChangeListener.accept(value);
                });
                dropdownView.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) ->
                        ViewUtils.forEach(dropdownView, view ->
                                view.setOnTouchListener((v1, event) -> {
                    if (event.getAction() == MotionEvent.ACTION_DOWN)
                        SoundPlayer.playClickSound(context);
                    return false;
                })));
                for (int i = 0; i < items.length; i++) items[i] = fontWrapper.wrap(items[i]);
                dropdownView.setItemList(items);
                dropdownView.select(finalDefaultValue);
                root.addView(dropdownView);

                return root;
            }
        });
    }
    public CustomFormBuilder addSlider(CharSequence title, int min, int max, Integer step, Integer defaultValue) {
        if (step == null) step = 1;
        int finalStep = step;
        if (defaultValue == null) defaultValue = min;
        int finalDefaultValue = defaultValue;
        return addElement(new InputHolder() {
            Consumer<JsonElement> onValueChangeListener;
            @Override
            public void setOnValueChangeListener(Consumer<JsonElement> listener) {
                onValueChangeListener = listener;
            }
            JsonElement value = new JsonPrimitive(finalDefaultValue);
            @NonNull
            @Override
            public JsonElement getValue() {
                return value;
            }
            @SuppressLint("SetTextI18n")
            @Override
            public View buildView(Context context) {
                LinearLayout root = new LinearLayout(context);
                root.setOrientation(LinearLayout.VERTICAL);

                TextView titleView = new TextView(context);
                titleView.setTextSize(18);
                titleView.setText(fontWrapper.wrap(title + ": " + finalDefaultValue));
                root.addView(titleView);

                Slider sliderView = new Slider(context);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                layoutParams.topMargin = MathUtils.dp2px(context, 4);
                sliderView.setLayoutParams(layoutParams);
                sliderView.addOnChangeListener((slider, value1) -> {
                    titleView.setText(fontWrapper.wrap(title + ": " + value1)); // ???为什么冒号也要i18n

                    value = new JsonPrimitive(value1);
                    if (onValueChangeListener != null) onValueChangeListener.accept(value);
                });
                sliderView.setMin(min);
                sliderView.setMax(max);
                sliderView.setStep(finalStep);
                sliderView.post(() -> sliderView.setValue(finalDefaultValue));
                root.addView(sliderView);

                return root;
            }
        });
    }
    public CustomFormBuilder addStepSlider(CharSequence title, CharSequence[] items, Integer defaultValue) {
        if (defaultValue == null) defaultValue = 0;
        int finalDefaultValue = defaultValue;
        return addElement(new InputHolder() {
            Consumer<JsonElement> onValueChangeListener;
            @Override
            public void setOnValueChangeListener(Consumer<JsonElement> listener) {
                onValueChangeListener = listener;
            }
            JsonElement value = new JsonPrimitive(finalDefaultValue);
            @NonNull
            @Override
            public JsonElement getValue() {
                return value;
            }
            @SuppressLint("SetTextI18n")
            @Override
            public View buildView(Context context) {
                LinearLayout root = new LinearLayout(context);
                root.setOrientation(LinearLayout.VERTICAL);

                TextView titleView = new TextView(context);
                titleView.setTextSize(18);
                titleView.setText(fontWrapper.wrap(title + ": " + items[finalDefaultValue]));
                root.addView(titleView);

                Slider sliderView = new Slider(context);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                layoutParams.topMargin = MathUtils.dp2px(context, 4);
                sliderView.setLayoutParams(layoutParams);
                sliderView.addOnChangeListener((slider, value1) -> {
                    titleView.setText(fontWrapper.wrap(title + ": " + items[value1]));

                    value = new JsonPrimitive(value1);
                    if (onValueChangeListener != null) onValueChangeListener.accept(value);
                });
                sliderView.setMin(0);
                sliderView.setMax(items.length - 1);
                sliderView.setStep(1);
                sliderView.setValue(finalDefaultValue);
                root.addView(sliderView);

                return root;
            }
        });
    }

    private Consumer<FormCallback<JsonArray>> callback = callback -> {};
    @Override
    public CustomFormBuilder setCallback(Consumer<FormCallback<JsonArray>> callback) {
        this.callback = callback;
        return this;
    }

    private CharSequence submitText = null;
    private TextButton.Style submitStyle = null;
    public CustomFormBuilder setSubmit(CharSequence text, TextButton.Style style) {
        submitText = text;
        submitStyle = style;
        return this;
    }

    @Override
    public DialogBuilder build(Context context) {
        JsonArray formData = new JsonArray();
        final int dp = MathUtils.dp2px(context, 1);

        DialogBuilder dialogBuilder = new DialogBuilder(context)
                .setTitle(fontWrapper.wrap(title))
                .setOnCancelListener(() -> callback.accept(new FormCallback<>("busy")))
                .addAction(
                        fontWrapper.wrap(submitText != null ? submitText : context.getResources().getString(R.string.form_custom_submit)),
                        submitStyle,
                        v -> {
                            for (int i = 0; i < elements.size(); i++) {
                                Consumer<JsonElement> input = inputs.get(i);
                                if (input == null) continue;
                                input.accept(formData.get(i));
                            }
                            callback.accept(new FormCallback<>(formData));
                        }
                );
        for (int i = 0; i < elements.size(); i++) {
            var element = elements.get(i);
            int finalI = i;

            formData.add(element.getValue());
            element.setOnValueChangeListener(jsonElement -> formData.set(finalI, jsonElement));

            View view = element.buildView(context);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            if (i != 0) layoutParams.topMargin = 8 * dp;
            view.setLayoutParams(layoutParams);
            dialogBuilder.addContent(view);
        }
        return dialogBuilder;
    }
}
