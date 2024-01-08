package vip.cdms.mcoreui.view.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.view.View;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.function.Consumer;

import vip.cdms.mcoreui.util.FontWrapper;

/**
 * ORE UI风格表单构造器
 * @author Cdm2883
 */
public interface FormBuilder<F extends FormBuilder<?, ?>, V extends JsonElement> {
    /**
     * 一种在没有上下文占位view的解决方案.
     * 只需在创建holder时提供信息, 在需要时才使用上下文创建view.
     */
    interface Holder {
        View buildView(Context context);
    }

    record FormCallback<V extends JsonElement>(
            boolean isCancel,
            String cancelReason,
            V responseData
    ) {
        public FormCallback(String cancelReason) {
            this(true, cancelReason, null);
        }
        public FormCallback(V responseData) {
            this(false, null, responseData);
        }

        public JsonObject toJson(int formId) {
            JsonObject object = new JsonObject();
            object.add("form_id", new JsonPrimitive(formId));
            object.add("has_response_data", new JsonPrimitive(!isCancel));
            object.add("data", isCancel ? JsonNull.INSTANCE : new JsonPrimitive(new Gson().toJson(responseData)));
            object.add("has_cancel_reason", new JsonPrimitive(isCancel));
            object.add("cancel_reason", isCancel ? new JsonPrimitive(cancelReason) : JsonNull.INSTANCE);
            return object;
        }
    }

    interface Processor<F extends FormBuilder<?, ?>> {
        F processing(F builder);
    }

    static <V extends JsonElement> Consumer<FormCallback<V>> betterCallback(@Nullable Consumer<FormCallback<V>> onCancel, @Nullable Consumer<FormCallback<V>> onResponse) {
        return formCallback -> {
            if (formCallback.isCancel()) {
                if (onCancel != null) onCancel.accept(formCallback);
            } else {
                if (onResponse != null) onResponse.accept(formCallback);
            }
        };
    }

    F loadFormJson(JsonObject json);
    F setFontWrapper(FontWrapper fontWrapper);
    F setTitle(CharSequence title);
    /** @noinspection unchecked*/
    default F processing(Processor<F> processor) {
        return processor.processing((F) this);
    }
    F setCallback(Consumer<FormCallback<V>> callback);
    DialogBuilder build(Context context);
    default AlertDialog show(Context context) {
        return build(context).show();
    }

    static FormBuilder<?, ?> create(JsonObject json, SimpleFormBuilder.ButtonImageLoader imageLoader) {
        String title = json.get("type").getAsString();
        return switch (title) {
            case "modal"       -> new ModalFormBuilder().loadFormJson(json);
            case "form"        -> new SimpleFormBuilder().loadFormJson(json).setImageLoader(imageLoader);
            case "custom_form" -> new CustomFormBuilder().loadFormJson(json);
            default ->
                    throw new RuntimeException(new IllegalArgumentException("Wrong type, given " + title));
        };
    }
}
