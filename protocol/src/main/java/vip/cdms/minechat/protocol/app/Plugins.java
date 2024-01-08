package vip.cdms.minechat.protocol.app;

import vip.cdms.minechat.protocol.plugin.Plugin;

public abstract class Plugins {
    public static Plugins INSTANCE;

    public abstract boolean isEnabled(Class<? extends Plugin> clazz);
}
