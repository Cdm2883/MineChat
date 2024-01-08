package vip.cdms.minechat.util;

import java.util.Objects;

public interface RunnableEx extends Runnable {
    default RunnableEx andThen(Runnable after) {
        Objects.requireNonNull(after);
        return () -> { run(); after.run(); };
    }
}
