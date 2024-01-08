package vip.cdms.mcoreui.view.dialog;

import android.content.Context;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.function.Consumer;

import vip.cdms.mcoreui.util.FontWrapper;

/**
 * ORE UI风格模式表单构造器
 * @author Cdm2883
 */
public class ModalFormBuilder implements FormBuilder<ModalFormBuilder, JsonPrimitive> {
    private FontWrapper fontWrapper = FontWrapper.NON;
    private CharSequence title = "";
    private CharSequence content;
    private CharSequence confirmButton;
    private CharSequence cancelButton;
    private Consumer<FormCallback<JsonPrimitive>> callback = callback -> {};

    public ModalFormBuilder() {}
    @Override
    public ModalFormBuilder loadFormJson(JsonObject json) {
        if (!json.get("type").getAsString().equals("modal"))
            throw new RuntimeException(new IllegalArgumentException("Need type modal, given " + json.get("type").getAsString()));
        setTitle(json.get("title").getAsString());
        setContent(json.get("content").getAsString());
        setConfirmButton(json.get("button1").getAsString());
        setCancelButton(json.get("button2").getAsString());
        return this;
    }

    @Override
    public ModalFormBuilder setFontWrapper(FontWrapper fontWrapper) {
        this.fontWrapper = fontWrapper;
        return this;
    }
    @Override
    public ModalFormBuilder setTitle(CharSequence title) {
        this.title = title;
        return this;
    }
    public ModalFormBuilder setContent(CharSequence content) {
        this.content = content;
        return this;
    }
    public ModalFormBuilder setConfirmButton(CharSequence confirmButton) {
        this.confirmButton = confirmButton;
        return this;
    }
    public ModalFormBuilder setCancelButton(CharSequence cancelButton) {
        this.cancelButton = cancelButton;
        return this;
    }
    @Override
    public ModalFormBuilder setCallback(Consumer<FormCallback<JsonPrimitive>> callback) {
        this.callback = callback;
        return this;
    }

    @Override
    public DialogBuilder build(Context context) {
        return new DialogBuilder(context)
                .setTitle(fontWrapper.wrap(title))
                .setContent(fontWrapper.wrap(content))
                .addAction(fontWrapper.wrap(confirmButton), v -> callback.accept(new FormCallback<>(new JsonPrimitive(true))))
                .addAction(fontWrapper.wrap(cancelButton), v -> callback.accept(new FormCallback<>(new JsonPrimitive(false))))
                .setCancelable(false);
    }
}
