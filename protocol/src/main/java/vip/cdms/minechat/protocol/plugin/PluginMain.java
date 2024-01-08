package vip.cdms.minechat.protocol.plugin;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记插件入口以及对插件默认信息进行配置
 * @author Cdm2883
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface PluginMain {
    /** 插件默认打开状态 */
    boolean defaultEnabled() default true;
    /** 插件默认优先级 */
    int defaultPriority() default 0;
}