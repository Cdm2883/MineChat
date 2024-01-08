package vip.cdms.minechat.protocol.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 正则命令帮手
 * @author Cdm2883
 */
public class StringCommandHelper {
    /**
     * 命令接收器
     */
    public interface ListenerHost {
        /**
         * 接收命令
         * @param command 命令
         * @return 是否消耗
         */
        boolean onCommand(String command);
    }

    /**
     * 命令帮手, 命令接收器接收的命令会处理后交给它处理
     */
    public static abstract class ListenerHelper {
        /**
         * 修饰在方法上, 表示这个方法是一个命令节点
         * 一条命令可进入多个命令节点
         */
        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        public @interface OnRegex {
            String regex();
            int flags() default Pattern.CASE_INSENSITIVE;
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.PARAMETER)
        public @interface RegGroup {
            int value() default 0;
        }

        /** 监听状态 */
        public enum ListeningStatus {
            /** 监听状态: 停止监听并消耗该命令 */STOPPED,
            /** 监听状态: 继续监听并消耗该命令 (默认) */CONTINUE,
            /** 监听状态: 继续监听, 不消耗该命令 */NO_CONSUME
        }

        /**
         * 处理未捕获的异常
         * @param throwable 未捕获的异常
         */
        public void handleUncaughtException(Throwable throwable) {
            throw new RuntimeException(throwable);
        }

        /** 处理结果 */
        public record Preprocesses(Object obj) {}
        /** 处理结果: 拦截, 不消耗该命令 */
        public static Preprocesses INTERCEPT = new Preprocesses(null);
        /** 处理结果: 继续 */
        public static Preprocesses CONTINUE = new Preprocesses(null);
        /**
         * 对所有的被显式监听的命令进行预处理
         * @param command 命令
         * @return 处理结果
         */
        public Preprocesses preprocessing(String command) {
            return CONTINUE;
        }

        /**
         * 接收其他没有被显式监听的命令
         * @param command 命令
         * @return 是否消耗
         */
        public boolean onAnotherCommand(String command) {
            return false; // 不消耗
        }
    }

    private static class ACommandNode {
        record ARegGroup(int group, Class<?> clazz) {}
        record AExtendValue(int index) {}
        Method method;
        Pattern pattern;
        final ArrayList<Object> params = new ArrayList<>();
    }
    /**
     * 将命令接收器接收的命令处理后转接给命令帮手处理
     * @param helper 命令帮手
     * @param originalHost 命令接收器
     * @param extendValues 其他可用值
     * @return 被代理后的命令接收器
     * @noinspection unchecked, DataFlowIssue
     */
    public static <H/* extends ListenerHost*/> H transfer/*transfur (bushi*/(ListenerHelper helper, H originalHost, Object... extendValues) {
        ArrayList<ACommandNode> commands = new ArrayList<>();

        Method[] methods = helper.getClass().getMethods();
        for (Method method : methods) {
            if (!method.isAnnotationPresent(ListenerHelper.OnRegex.class)) continue;
            ListenerHelper.OnRegex onRegex = method.getAnnotation(ListenerHelper.OnRegex.class);
            assert onRegex != null;
            method.setAccessible(true);  // 提高性能

            ACommandNode command = new ACommandNode();
            command.method = method;
            command.pattern = Pattern.compile(onRegex.regex(), onRegex.flags());

            Parameter[] parameters = method.getParameters();
            Class<?>[] parameterTypes = method.getParameterTypes();
            ArrayList<Integer> groupUsedIndex = new ArrayList<>();  // 记录使用过的捕获组
            OUT:
            for (int i = 0; i < parameters.length; i++) {
                Parameter parameter = parameters[i];
                Class<?> parameterType = parameterTypes[i];

                if (parameter.isAnnotationPresent(ListenerHelper.RegGroup.class)) {
                    int groupIndex = parameter.getAnnotation(ListenerHelper.RegGroup.class).value();
                    groupUsedIndex.add(groupIndex);
                    command.params.add(new ACommandNode.ARegGroup(groupIndex, parameterType));
                    continue;
                }

                if (parameterType.getName().equals(ListenerHelper.Preprocesses.class.getName())) {
                    command.params.add(parameterType);
                    continue;
                }

                for (int j = 0; j < extendValues.length; j++) {
                    Object extendValue = extendValues[j];
                    if (
                            extendValue == null
                            || !parameterType.isAssignableFrom(extendValue.getClass())
                    )
                        continue;
                    command.params.add(new ACommandNode.AExtendValue(j));
                    continue OUT;
                }

                int groupIndex = -1;
                for (int j = 1; groupIndex == -1; j++) {
                    if (groupUsedIndex.contains(j)) continue;
                    groupUsedIndex.add(j);
                    groupIndex = j;
                }
                command.params.add(new ACommandNode.ARegGroup(groupIndex, parameterType));
            }

            commands.add(command);
        }

        Function<String, Boolean> onCommand = command -> {
            boolean hasRunned = false;
            boolean consumed = false;
            ArrayList<ACommandNode> stoppedNodes = new ArrayList<>();
            for (ACommandNode node : commands) {
                Matcher matcher = node.pattern.matcher(command);
                if (!matcher.find()) continue;
                hasRunned = true;

                ListenerHelper.Preprocesses preprocesses = helper.preprocessing(command);
                if (preprocesses == ListenerHelper.INTERCEPT) continue;

                Object[] args = new Object[node.params.size()];
                for (int i = 0; i < node.params.size(); i++) {
                    Object param = node.params.get(i);

                    if (param instanceof ACommandNode.ARegGroup aRegGroup) {
                        String group = matcher.group(aRegGroup.group);
                        if (group == null) {
                            args[i] = null;
                            continue;
                        }
                        args[i] = switch (aRegGroup.clazz.getName()) {
                            case "java.lang.String"    -> group;
                            case "java.lang.Boolean"   -> group.equals("1") || (!group.equals("0") && Boolean.parseBoolean(group));
                            case "java.lang.Byte"      -> Byte.parseByte(group);
                            case "java.lang.Short"     -> Short.parseShort(group);
                            case "java.lang.Integer"   -> Integer.parseInt(group);
                            case "java.lang.Long"      -> Long.parseLong(group);
                            case "java.lang.Float"     -> Float.parseFloat(group);
                            case "java.lang.Double"    -> Double.parseDouble(group);
                            case "java.lang.Character" -> group.charAt(0);
                            default -> null;
                        };
                        continue;
                    }

                    if (param instanceof ACommandNode.AExtendValue aExtendValue) {
                        args[i] = extendValues[aExtendValue.index];
                        continue;
                    }

                    if (param instanceof Class<?> clazz
                            && clazz.getName().equals(ListenerHelper.Preprocesses.class.getName())) {
                        args[i] = preprocesses;
                        continue;
                    }

                    args[i] = null;
                }

                try {
                    Object invoked = node.method.invoke(helper, args);
                    ListenerHelper.ListeningStatus listeningStatus = ExceptionHandler.ror(() ->
                            invoked == null ? ListenerHelper.ListeningStatus.CONTINUE : (ListenerHelper.ListeningStatus) invoked,
                            ListenerHelper.ListeningStatus.CONTINUE);
                    switch (listeningStatus) {
                        case STOPPED:
                            stoppedNodes.add(node);
                        case CONTINUE:
                            consumed = true;
                            break;
                        case NO_CONSUME:
                    }
                } catch (InvocationTargetException e) {
                    e.getTargetException().printStackTrace();
                    helper.handleUncaughtException(e.getTargetException());
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                    helper.handleUncaughtException(throwable);
                }
            }
            if (!stoppedNodes.isEmpty()) commands.removeAll(stoppedNodes);

            return hasRunned ? consumed : helper.onAnotherCommand(command);
        };

        return (H) Proxy.newProxyInstance(
                originalHost.getClass().getClassLoader(),
                originalHost.getClass().getInterfaces(),
                (proxy, method, args) -> (  // boolean onCommand(String string);
                        method.getName().equals("onCommand")
                                && args.length == 1
                                && args[0] instanceof String command
                ) ? onCommand.apply(command) : method.invoke(originalHost, args)
        );
    }
    public static ListenerHost easyTransfer(ListenerHelper helper, Object... args) {
        return transfer(helper, string -> false, args);
    }
}
