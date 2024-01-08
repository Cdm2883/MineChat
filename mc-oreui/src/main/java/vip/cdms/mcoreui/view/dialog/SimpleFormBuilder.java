package vip.cdms.mcoreui.view.dialog;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.LinearLayout;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Consumer;

import vip.cdms.mcoreui.R;
import vip.cdms.mcoreui.util.FontWrapper;
import vip.cdms.mcoreui.util.MathUtils;
import vip.cdms.mcoreui.view.button.TextButton;
import vip.cdms.mcoreui.view.show.ImageView;
import vip.cdms.mcoreui.view.show.TextView;

/**
 * ORE UI风格简单(按钮)表单构造器
 * @author Cdm2883
 */
public class SimpleFormBuilder implements FormBuilder<SimpleFormBuilder, JsonPrimitive>  {
    public interface ActionHolder extends FormBuilder.Holder {
        void setOnActiveListener(View.OnClickListener listener);
    }
    public interface ButtonImageLoader {
        ButtonImageLoader NON = new ButtonImageLoader() {
            public void loadUrl(String url, ImageView imageView) {}
            public void loadPath(String path, ImageView imageView) {}
        };
        void loadUrl(String url, ImageView imageView);
        void loadPath(String path, ImageView imageView);
    }
    public interface MixImageLoader extends ButtonImageLoader {
        void load(boolean loadUrl, String load, ImageView imageView);
        default void loadUrl(String url, ImageView imageView) {
            load(true, url, imageView);
        }
        default void loadPath(String path, ImageView imageView) {
            load(false, path, imageView);
        }
    }

    public SimpleFormBuilder() {}
    public SimpleFormBuilder loadFormJson(JsonObject json) {
        if (!json.get("type").getAsString().equals("form"))
            throw new RuntimeException(new IllegalArgumentException("Need type form, given " + json.get("type").getAsString()));

        setTitle(json.get("title").getAsString());
        setContent(json.has("content") ? json.get("content").getAsString() : null);
        for (var jsonElement : json.get("buttons").getAsJsonArray()) {
            if (!(jsonElement instanceof JsonObject element))
                throw new RuntimeException(new IllegalArgumentException(jsonElement.toString()));
            String text = element.get("text").getAsString();
            JsonObject image = element.has("image") ? element.get("image").getAsJsonObject() : null;
            if (image == null) addButton(text);
            else addButton(text, image.get("data").getAsString(), "url".equals(image.get("type").getAsString()));
        }
        return this;
    }

    private ButtonImageLoader imageLoader = ButtonImageLoader.NON;
    public SimpleFormBuilder setImageLoader(ButtonImageLoader imageLoader) {
        this.imageLoader = imageLoader;
        return this;
    }
    public SimpleFormBuilder setDefaultImageLoader(ButtonImageLoader imageLoader) {
        return getImageLoader() == ButtonImageLoader.NON ? this : setImageLoader(imageLoader);
    }
    public ButtonImageLoader getImageLoader() {
        return imageLoader;
    }

    private FontWrapper fontWrapper = FontWrapper.NON;
    @Override
    public SimpleFormBuilder setFontWrapper(FontWrapper fontWrapper) {
        this.fontWrapper = fontWrapper;
        return this;
    }

    private CharSequence title = "";
    @Override
    public SimpleFormBuilder setTitle(CharSequence title) {
        this.title = title;
        return this;
    }

    private CharSequence content = null;
    public SimpleFormBuilder setContent(CharSequence content) {
        this.content = content;
        return this;
    }

    private final ArrayList<ActionHolder> elements = new ArrayList<>();
    public SimpleFormBuilder addElement(ActionHolder element) {
        elements.add(element);
        return this;
    }

    private final HashMap<Integer, View.OnClickListener> actions = new HashMap<>();
    public SimpleFormBuilder action(View.OnClickListener action) {
        actions.put(elements.size() - 1, action);
        return this;
    }

    public SimpleFormBuilder addButton(CharSequence text) {
        return addElement(new ActionHolder() {
            View.OnClickListener onActiveListener;
            @Override
            public void setOnActiveListener(View.OnClickListener listener) {
                onActiveListener = listener;
            }
            @Override
            public View buildView(Context context) {
                TextButton textButton = new TextButton(context);
                textButton.setOnClickListener(onActiveListener);
                textButton.setText(fontWrapper.wrap(text));
                return textButton;
            }
        });
    }
    public SimpleFormBuilder addButton(CharSequence text, Consumer<ImageView> image) {
        return addElement(new ActionHolder() {
            View.OnClickListener onActiveListener;
            @Override
            public void setOnActiveListener(View.OnClickListener listener) {
                onActiveListener = listener;
            }
            @Override
            public View buildView(Context context) {
                ConstraintLayout layout = (ConstraintLayout) ConstraintLayout.inflate(context, R.layout.layout_image_button, null);

                ImageView imageView = layout.findViewById(R.id.image_view);
                image.accept(imageView);

                TextButton textButton = layout.findViewById(R.id.button);
                textButton.setOnClickListener(onActiveListener);
                textButton.setText(fontWrapper.wrap(text));

                return layout;
            }
        });
    }
    public SimpleFormBuilder addButton(CharSequence text, String image, Boolean isUrl) {
        return addButton(text, imageView -> {
            if (imageLoader != null) {
                if (isUrl) imageLoader.loadUrl(image, imageView);
                else imageLoader.loadPath(image, imageView);
            }
        });
    }
    public SimpleFormBuilder addButton(CharSequence text, Drawable image) {
        return addButton(text, imageView -> imageView.setImageDrawable(image));
    }

    private Consumer<FormCallback<JsonPrimitive>> callback = callback -> {};
    @Override
    public SimpleFormBuilder setCallback(Consumer<FormCallback<JsonPrimitive>> callback) {
        this.callback = callback;
        return this;
    }

    @Override
    public DialogBuilder build(Context context) {
        final int dp = MathUtils.dp2px(context, 1);
        final boolean hasContent = !(content == null || content.length() == 0);

        DialogBuilder dialogBuilder = new DialogBuilder(context)
                .setTitle(fontWrapper.wrap(title))
                .setOnCancelListener(() -> callback.accept(new FormCallback<>("busy")));
        if (hasContent) {
            TextView contentView = new TextView(context);
            contentView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            contentView.setText(fontWrapper.wrap(content));
            dialogBuilder.addContent(contentView);
        }
        for (int i = 0; i < elements.size(); i++) {
            var element = elements.get(i);
            int finalI = i;

            element.setOnActiveListener(v -> {
                View.OnClickListener action = actions.get(finalI);
                if (action != null) action.onClick(v);

                callback.accept(new FormCallback<>(new JsonPrimitive(finalI)));
                dialogBuilder.close();
            });

            View view = element.buildView(context);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            if (hasContent || i != 0) layoutParams.topMargin = 8 * dp;
            view.setLayoutParams(layoutParams);
            dialogBuilder.addContent(view);
        }
        return dialogBuilder;
    }
}
