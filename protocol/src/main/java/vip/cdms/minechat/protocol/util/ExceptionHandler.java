package vip.cdms.minechat.protocol.util;

import android.app.Activity;
import android.content.Context;
import android.view.View;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.function.Consumer;

import vip.cdms.mcoreui.view.dialog.DialogBuilder;
import vip.cdms.mcoreui.util.MathUtils;
import vip.cdms.mcoreui.view.dialog.FormBuilder;
import vip.cdms.mcoreui.view.show.TextView;

public class ExceptionHandler {
    public static void printStackTrace(Context context, Throwable e, Consumer<DialogBuilder> dialogBuilderConsumer) {
        printStackTrace(context, "Oops!", e, dialogBuilderConsumer);
    }
    public static void printStackTrace(Context context, Throwable e) {
        printStackTrace(context, e, null);
    }
    public static void printStackTrace(Context context, CharSequence title, Throwable e, Consumer<DialogBuilder> dialogBuilderConsumer) {
        e.printStackTrace();
        printStackTrace(context, title, e.getMessage(), printStackTrace(e), dialogBuilderConsumer);
    }
    public static void printStackTrace(Context context, CharSequence title, Throwable e) {
        printStackTrace(context, title, e, null);
    }
    public static void printStackTrace(Context context, CharSequence title, CharSequence em, CharSequence e, Consumer<DialogBuilder> dialogBuilderConsumer) {
        if (context == null) return;
        Runnable show = () -> {
            TextView textView = new TextView(context);
            textView.setPadding(0, 0, 0, MathUtils.dp2px(context, 16));
            textView.setTextSize(18);
            if (em == null) textView.setVisibility(View.GONE);
            else textView.setText(em);
            TextView textView1 = new TextView(context);
            textView1.setTextSize(6);
            textView1.setText(e);
            DialogBuilder dialogBuilder = new DialogBuilder(context)
                    .setTitle(title)
                    .addContent(textView)
                    .addContent(textView1);
            dialogBuilder.show();
            if (dialogBuilderConsumer != null)
                dialogBuilderConsumer.accept(dialogBuilder);
        };
        if (context instanceof Activity activity) activity.runOnUiThread(show);
        else show.run();
    }
    public static void printStackTrace(Context context, CharSequence title, CharSequence em, CharSequence e) {
        printStackTrace(context, title, em, e, null);
    }
    public static String printStackTrace(Throwable e) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        e.printStackTrace(new PrintStream(outputStream));
        return outputStream.toString();
    }

    public interface AutoTryCatch {
        void run() throws Throwable;
    }
    public static void tryCatch(Context context, AutoTryCatch tryCatch) {
        tryCatch(tryCatch, e -> {
            if (context == null) throw new RuntimeException(e);
            else printStackTrace(context, e);
        });
    }
    public static void tryCatch(AutoTryCatch tryCatch, Handler handler) {
        try {
            tryCatch.run();
        } catch (Throwable e) {
            if (handler == null) throw new RuntimeException(e);
            else handler.getCaught(e);
        }
    }


    public interface AutoTryCatchResult<T> {
        T get() throws Throwable;
    }
    public static <T> T ror(AutoTryCatchResult<T> getter, AutoTryCatchResult<T> defaultGetter) {
        try {
            T value = getter.get();
            return value == null ? defaultGetter.get() : value;
        } catch (Throwable e) {
            try {
                return defaultGetter.get();
            } catch (Throwable ex) {
                throw new RuntimeException(ex);
            }
        }
    }
    public static <T> T ror(AutoTryCatchResult<T> getter, T defaultValue) {
        return ror(getter, (AutoTryCatchResult<T>) () -> defaultValue);
    }
    public static <T> T ror(T value, T defaultValue) {
        return ror(() -> value, defaultValue);
    }

    public interface Processor<T> {
        T processing(T target) throws Throwable;
    }
    public static <T> T processing(T target, Processor<T> processor) {
        return ror(() -> processor.processing(target), target);
    }



    public interface Handler {
        void getCaught(Throwable e);
    }
    private Handler uncaughtHandler;
    public ExceptionHandler() {}
    public ExceptionHandler(Handler handler) {
        this.uncaughtHandler = handler;
    }
    public ExceptionHandler setUncaughtHandler(Handler uncaughtHandler) {
        this.uncaughtHandler = uncaughtHandler;
        return this;
    }

    private record AExceptionHandler(Class<? extends Throwable> e, Handler handler, boolean disposable) {}
    private final ArrayList<AExceptionHandler> handlers = new ArrayList<>();
    public ExceptionHandler hand(Class<? extends Throwable> e, Handler handler) {
        handlers.add(new AExceptionHandler(e, handler, false));
        return this;
    }
    public ExceptionHandler handDisposable(Class<? extends Throwable> e, Handler handler) {
        handlers.add(new AExceptionHandler(e, handler, true));
        return this;
    }

    public ExceptionHandler throwException(Throwable e) {
        e.printStackTrace();
        if (uncaughtHandler == null && handlers.isEmpty()) throw new RuntimeException(e);
        int handTime = 0;
        for (int i = 0; i < handlers.size(); i++) {
            AExceptionHandler aExceptionHandler = handlers.get(i);
            if (aExceptionHandler == null || !aExceptionHandler.e.isInstance(e)) continue;
            aExceptionHandler.handler.getCaught(e);
            handTime++;
        }
        if (handTime == 0 && uncaughtHandler != null) uncaughtHandler.getCaught(e);
        if (handTime == 0 && uncaughtHandler == null) throw new RuntimeException(e);
        return this;
    }
    public ExceptionHandler tryCatch(AutoTryCatch tryCatch) {
        try {
            tryCatch.run();
        } catch (Throwable e) {
            throwException(e);
        }
        for (int i = 0; i < handlers.size(); i++) {
            AExceptionHandler aExceptionHandler = handlers.get(i);
            if (aExceptionHandler != null && aExceptionHandler.disposable) handlers.set(i, null);
        }
        return this;
    }
}
