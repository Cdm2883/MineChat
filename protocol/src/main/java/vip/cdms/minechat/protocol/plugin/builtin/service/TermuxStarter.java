package vip.cdms.minechat.protocol.plugin.builtin.service;

import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import vip.cdms.mcoreui.view.dialog.DialogBuilder;
import vip.cdms.minechat.protocol.R;
import vip.cdms.minechat.protocol.plugin.JLanguage;
import vip.cdms.minechat.protocol.plugin.PluginMain;
import vip.cdms.minechat.protocol.plugin.ServiceExtension;
import vip.cdms.minechat.protocol.plugin.ServicePool;
import vip.cdms.minechat.protocol.util.ExceptionHandler;


public class TermuxStarter extends IntentService {
    private static final String RUN_COMMAND_PERMISSION = "com.termux.permission.RUN_COMMAND";
    private static boolean enable = false;
    @PluginMain(defaultEnabled = false)
    public static class Plugin extends ServiceExtension {
        JLanguage lang = new JLanguage();
        public Plugin(Application application, SharedPreferences sharedPreferences, ExceptionHandler exceptionHandler) {
            super(application, sharedPreferences, exceptionHandler);
            lang.set(application);
            setPluginIcon("iVBORw0KGgoAAAANSUhEUgAAADAAAAAwCAYAAABXAvmHAAADrUlEQVRoBe2Yu0p7QRDG4107wU4LWxGfQbxV1j6AFlaCF1RUsLLwDpYiFuKbCGJlYeFbWKmFhbeR3/L/luH8k00wiUngBDazZ3Zn9vtmZvdcCoVCwVq85QQancE8A3kGqjxE8hJq7hLq7OwMACXb29uto6Mj6Oi3tbVFAtJTEozRqiyPSuzTJdTV1RWdeIA9PT1R70kIcDGdxmos0wRYTJEkC93d3QG4jz59yCGz+hqDjUFzfssTAJyAy9BnQzqi7iNfbI7m1lCmCQCiv7/flpaWbGtryzY3N21nZ8eGhoZC1LPEACYSZKOGQEv5ShMAAKVzfX1t39/f9vn5GdrNzU0Eyhz2hACzb3wp1ZlEeQJkAYAPDw/GDxL8dnd3ra+vL0QGkn9UMtlMpAmoHIjiyMiIPT8/B/Bk4+PjwyYmJv7bHzpy6xx5EUkTEAiIUBYLCwuBAH+QeHl5sYGBATkL0h+xsq+jTBNQWSgTkLi6urKvr69A5P393e7u7mIpaT6AtSfqCJ6ApQkwrpNGWejt7bXb29uYCTrb29vxXoDNH2YhTcBHVBsWImNjY2E/KBOQGB8fDyUkG8lKglTFnDQBHCvy9HU8IhcXF0MWIMHJ9PT0ZIODg4EENrQqgFVqW56AQGdBob+4uIilBIn7+/uwsMqu4QS4KakUfEQBDziAPj4+RhJvb2+2t7dXafRqMS+dAYEHLAR07c/60dFRe319jeXE/WFycrIW4CrxkSagSEOAvoBLoi9GYGpqqpLFazEnTQCgIiEpMsimLyGB1QbOkmj6TQwBwAs4UmRa4hjVpoVIy97I/JlOBlrqUUKnDWWjcmq5hzkBh8T8/Hy8abXE47QiD4nfvtDMzc3Z0dFRaIeHh1Genp4a7eTkxI6Pj21/f9/Ozs7s4OAgrOXXZv0SreRANGAj//aVEtvz8/P4Gqr0cbcmg2ro6fPj4XBmZiauXwK4xssTYB9U81J/eXkp3BGk3qvjwD/gup6enhbAcjJNgAhW+1lldnbW1tfXbXl52VZXV0N/ZWXFNjY2bG1tLUrG+XTDnOHhYfNfBRNZSBPAEBL+KJUu65Sa9XWLneZoDB1N10iOZnRInXqyq0CWJ4BjHOFcRNAJrAeQ1XPPEAj50XUp6f2VmuP0aQI+jT6i/p1XRJzTSE465nh7gZSOcfnxvmWfkGkCSqmkFsYhfS3KtcBoTONe7+cLlNdpnaw/zS0i0wSKGMSSaJKxnECjM5JnIM9AlYdBXkINLaEf1Q6bLm2oZqoAAAAASUVORK5CYII=");
            setPluginTitle(lang.add("Termux Starter", "Termux 启动器"));
            setPluginSummary(lang.add("[built-in] Give software the ability to interact with Termux", "[内置] 给软件提供与Termux交互的能力"));
        }

        @Override
        public void onStart() {
            super.onStart();
            enable = true;

            if (getActivity().checkSelfPermission(RUN_COMMAND_PERMISSION) != PackageManager.PERMISSION_GRANTED)
                new DialogBuilder(getActivity())
                        .setTitle(lang.add("Permission denied", "Termux 启动器权限不足"))
                        .setContent(lang.add("", ""), false)  // todo
                        .addAction(lang.add("", "前往授权"), v -> {
                            ActivityCompat.requestPermissions(getActivity(), new String[]{RUN_COMMAND_PERMISSION}, 0);
                        })
                        .show();
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            enable = false;
        }

        @Override
        public Activity getActivity() {
            return super.getActivity();
        }
    }

    public static class Command extends Intent {
        public Command() {
            setClassName("com.termux", "com.termux.app.RunCommandService");
            setAction("com.termux.RUN_COMMAND");
        }

        /**
         * 命令的绝对路径*
         * @param path 绝对路径
         */
        public Command setCommandPath(String path) {
            putExtra("com.termux.RUN_COMMAND_PATH", path);
            return this;
        }
        /**
         * 命令的可执行文件的参数（非 stdin 脚本）
         * @param arguments 参数
         */
        public Command setArguments(String... arguments) {
            putExtra("com.termux.RUN_COMMAND_ARGUMENTS", arguments);
            return this;
        }
        /**
         * 是否用逗号替换参数中的逗号替代字符
         * @param enable 默认false
         * @since Termux >= 0.115
         * @see <a href="https://github.com/termux/termux-app/commit/2aafcf84">Termux Commit 2aafcf84</a>
         */
        public Command setReplaceCommaAlternativeCharsInArguments(boolean enable) {
            putExtra("com.termux.RUN_COMMAND_REPLACE_COMMA_ALTERNATIVE_CHARS_IN_ARGUMENTS", enable);
            return this;
        }
        /**
         * 应替换的参数中的逗号替代字符为默认的替代逗号字符 (U+201A, &sbquo;, &#8218;, single low-9 quotation mark)
         * @since Termux >= 0.115
         */
        public Command setCommaAlternativeCharsInArguments(String character) {
            putExtra("com.termux.RUN_COMMAND_COMMA_ALTERNATIVE_CHARS_IN_ARGUMENTS", character);
            return this;
        }
        /**
         * 命令的 stdin
         * @since Termux >= 0.109
         */
        public Command setStdin(String content) {
            putExtra("com.termux.RUN_COMMAND_STDIN", content);
            return this;
        }
        /**
         * 命令的当前工作目录
         * @param workdir 默认为/data/data/com.termux/files/home
         */
        public Command setWorkdir(String workdir) {
            putExtra("com.termux.RUN_COMMAND_WORKDIR", workdir);
            return this;
        }
        /**
         * 在后台终端会话中运行命令
         * @param background 默认false即前台
         */
        @Deprecated
        public Command setBackground(boolean background) {
            putExtra("com.termux.RUN_COMMAND_BACKGROUND", background);
            return this;
        }
        /**
         * 后台命令的自定义日志级别, Termux 应用将使用该级别.
         * @since Termux >= 0.118
         * @see <a href="https://github.com/termux/termux-tasker#custom-log-level">自定义日志级别</a> <a href="https://github.com/termux/termux-app/commit/60f37bde">60f37bde</a>
         */
        public Command setBackgroundCustomLogLevel(String level) {
            putExtra("com.termux.RUN_COMMAND_BACKGROUND_CUSTOM_LOG_LEVEL", level);
            return this;
        }
        public enum SessionAction {
            /**
             * set the new session as
             * the current session and will start TERMUX_ACTIVITY if its not running to bring
             * the new session to foreground.
             */
            SWITCH_TO_NEW_SESSION_AND_OPEN_ACTIVITY,
            /**
             * keep any existing session
             * as the current session and will start TERMUX_ACTIVITY if its not running to
             * bring the existing session to foreground. The new session will be added to the left
             * sidebar in the sessions list.
             */
            KEEP_CURRENT_SESSION_AND_OPEN_ACTIVITY,
            /**
             * set the new session as
             * the current session but will not start TERMUX_ACTIVITY if its not running
             * and session(s) will be seen in Termux notification and can be clicked to bring new
             * session to foreground. If the TERMUX_ACTIVITY is already running, then this
             * will behave like KEEP_CURRENT_SESSION_AND_OPEN_ACTIVITY.
             */
            SWITCH_TO_NEW_SESSION_AND_DONT_OPEN_ACTIVITY,
            /**
             * keep any existing session
             * as the current session but will not start TERMUX_ACTIVITY if its not running
             * and session(s) will be seen in Termux notification and can be clicked to bring
             * existing session to foreground. If the TERMUX_ACTIVITY is already running,
             * then this will behave like KEEP_CURRENT_SESSION_AND_OPEN_ACTIVITY.
             */
            KEEP_CURRENT_SESSION_AND_DONT_OPEN_ACTIVITY,
        }
        /**
         * 前台命令的会话操作
         * @param action 默认为 SWITCH_TO_NEW_SESSION_AND_OPEN_ACTIVITY
         * @since Termux >= 0.109
         */
        public Command setSessionAction(SessionAction action) {
            putExtra("com.termux.RUN_COMMAND_SESSION_ACTION", action.ordinal());
            return this;
        }
        /**
         * 命令的标签
         * @since Termux >= 0.109
         */
        public Command setCommandLabel(String label) {
            putExtra("com.termux.RUN_COMMAND_COMMAND_LABEL", label);
            return this;
        }
        /**
         * 命令的描述 (理想情况下应该很短)
         * @since Termux >= 0.109
         */
        public Command setCommandDescription(String markdown) {
            putExtra("com.termux.RUN_COMMAND_COMMAND_DESCRIPTION", markdown);
            return this;
        }
        /**
         * 命令的帮助. 这可以添加有关命令的详细信息. 第三方应用可以为设置命令向用户提供更多信息. 理想情况下, 应提供一个链接, 链接到完整的详细信息.
         * @since Termux >= 0.109
         */
        public Command setCommandHelp(String markdown) {
            putExtra("com.termux.RUN_COMMAND_COMMAND_HELP", markdown);
            return this;
        }
        /**
         * 将命令的结果返回给调用者的挂起意图. 结果将发送到 TERMUX_SERVICE.EXTRA_PLUGIN_RESULT_BUNDLE bundle 中
         * @since Termux >= 0.109
         */
        public Command setPendingIntent(Parcelable parcelable) {
            putExtra("com.termux.RUN_COMMAND_PENDING_INTENT", parcelable);
            return this;
        }
        /**
         * 写入执行命令的结果的目录路径
         * @since Termux >= 0.115
         * @see <a href="https://github.com/termux/termux-app/commit/2aafcf84">2aafcf84</a>
         */
        public Command setResultDirectory(String path) {
            putExtra("com.termux.RUN_COMMAND_RESULT_DIRECTORY", path);
            return this;
        }
        /**
         * (setResultDirectory*&setResultSingleFile true) 是否应将结果写入进结果目录的单个文件, 否则将写进多个文件 (err, errmsg, stdout, stderr, exit_code)
         * @param enable 默认false
         * @since Termux >= 0.115
         * @see <a href="https://github.com/termux/termux-app/commit/2aafcf84?diff=unified#diff-dbd168dc286d63c515cbd3662c8e0e6dd8992bb6b00cfa96be101b27301a52e8R112">RunCommandService.java#2aafcf84-L112</a>
         */
        public Command setResultSingleFile(boolean enable) {
            putExtra("com.termux.RUN_COMMAND_RESULT_SINGLE_FILE", enable);
            return this;
        }
        /**
         * (setResultDirectory*) basename of the result file
         * @since Termux >= 0.115
         * @see <a href="https://github.com/termux/termux-app/commit/2aafcf84?diff=unified#diff-dbd168dc286d63c515cbd3662c8e0e6dd8992bb6b00cfa96be101b27301a52e8R113">RunCommandService.java#2aafcf84-L113</a>
         */
        public Command setResultFileBasename(String basename) {
            putExtra("com.termux.RUN_COMMAND_RESULT_FILE_BASENAME", basename);
            return this;
        }
        /**
         * (setResultDirectory*)
         * @since Termux >= 0.115
         * @see <a href="https://github.com/termux/termux-app/commit/2aafcf84?diff=unified#diff-dbd168dc286d63c515cbd3662c8e0e6dd8992bb6b00cfa96be101b27301a52e8R114">RunCommandService.java#2aafcf84-L114</a>
         */
        public Command setResultFileOutputFormat(String format) {
            putExtra("com.termux.RUN_COMMAND_RESULT_FILE_OUTPUT_FORMAT", format);
            return this;
        }
        /**
         * (setResultDirectory*)
         * @since Termux >= 0.115
         * @see <a href="https://github.com/termux/termux-app/commit/2aafcf84?diff=unified#diff-dbd168dc286d63c515cbd3662c8e0e6dd8992bb6b00cfa96be101b27301a52e8R115">RunCommandService.java#2aafcf84-L115</a>
         */
        public Command setResultFileErrorFormat(String format) {
            putExtra("com.termux.RUN_COMMAND_RESULT_FILE_ERROR_FORMAT", format);
            return this;
        }
        /**
         * (setResultDirectory*&setResultSingleFile false)
         * @since Termux >= 0.115
         * @see <a href="https://github.com/termux/termux-app/commit/2aafcf84?diff=unified#diff-dbd168dc286d63c515cbd3662c8e0e6dd8992bb6b00cfa96be101b27301a52e8R112">RunCommandService.java#2aafcf84-L112</a>
         */
        public Command setResultFilesSuffix(String suffix) {
            putExtra("com.termux.RUN_COMMAND_RESULT_FILES_SUFFIX", suffix);
            return this;
        }

        public void run() {
            if (!enable)
                throw new RuntimeException("Termux Starter is not enabled");  // todo
            ServicePool.getInstance(Plugin.class)
                    .getActivity()
                    .startService(this);
        }
        public Process execute() {
            run();
            return null;
        }
    }

    public static final String EXTRA_EXECUTION_ID = "execution_id";

    private static int EXECUTION_ID = 1000;

    public TermuxStarter(){
        super(TermuxStarter.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent == null) return;

        System.out.println("received execution result");

        final Bundle resultBundle = intent.getBundleExtra(TERMUX_SERVICE.EXTRA_PLUGIN_RESULT_BUNDLE);
        if (resultBundle == null) {
            System.out.println("The intent does not contain the result bundle at the \"" + TERMUX_SERVICE.EXTRA_PLUGIN_RESULT_BUNDLE + "\" key.");
            return;
        }

        final int executionId = intent.getIntExtra(EXTRA_EXECUTION_ID, 0);

        System.out.println("Execution id " + executionId + " result:\n" +
                "stdout:\n```\n" + resultBundle.getString(TERMUX_SERVICE.EXTRA_PLUGIN_RESULT_BUNDLE_STDOUT, "") + "\n```\n" +
                "stdout_original_length: `" + resultBundle.getString(TERMUX_SERVICE.EXTRA_PLUGIN_RESULT_BUNDLE_STDOUT_ORIGINAL_LENGTH) + "`\n" +
                "stderr:\n```\n" + resultBundle.getString(TERMUX_SERVICE.EXTRA_PLUGIN_RESULT_BUNDLE_STDERR, "") + "\n```\n" +
                "stderr_original_length: `" + resultBundle.getString(TERMUX_SERVICE.EXTRA_PLUGIN_RESULT_BUNDLE_STDERR_ORIGINAL_LENGTH) + "`\n" +
                "exitCode: `" + resultBundle.getInt(TERMUX_SERVICE.EXTRA_PLUGIN_RESULT_BUNDLE_EXIT_CODE) + "`\n" +
                "errCode: `" + resultBundle.getInt(TERMUX_SERVICE.EXTRA_PLUGIN_RESULT_BUNDLE_ERR) + "`\n" +
                "errmsg: `" + resultBundle.getString(TERMUX_SERVICE.EXTRA_PLUGIN_RESULT_BUNDLE_ERRMSG, "") + "`");
    }

    public static synchronized int getNextExecutionId() {
        return EXECUTION_ID++;
    }
}

/**
 * Termux app core service.
 */
final class TERMUX_SERVICE {

    /** Intent action to stop TERMUX_SERVICE */
    public static final String ACTION_STOP_SERVICE = "com.termux.service_stop"; // Default: "com.termux.service_stop"


    /** Intent action to make TERMUX_SERVICE acquire a wakelock */
    public static final String ACTION_WAKE_LOCK = "com.termux.service_wake_lock"; // Default: "com.termux.service_wake_lock"


    /** Intent action to make TERMUX_SERVICE release wakelock */
    public static final String ACTION_WAKE_UNLOCK = "com.termux.service_wake_unlock"; // Default: "com.termux.service_wake_unlock"


    /** Intent action to execute command with TERMUX_SERVICE */
    public static final String ACTION_SERVICE_EXECUTE = "com.termux.service_execute"; // Default: "com.termux.service_execute"

    /** Uri scheme for paths sent via intent to TERMUX_SERVICE */
    public static final String URI_SCHEME_SERVICE_EXECUTE = "com.termux.file"; // Default: "com.termux.file"
    /** Intent {@code String[]} extra for arguments to the executable of the command for the TERMUX_SERVICE.ACTION_SERVICE_EXECUTE intent */
    public static final String EXTRA_ARGUMENTS = "com.termux.execute.arguments"; // Default: "com.termux.execute.arguments"
    /** Intent {@code String} extra for stdin of the command for the TERMUX_SERVICE.ACTION_SERVICE_EXECUTE intent */
    public static final String EXTRA_STDIN = "com.termux.execute.stdin"; // Default: "com.termux.execute.stdin"
    /** Intent {@code String} extra for command current working directory for the TERMUX_SERVICE.ACTION_SERVICE_EXECUTE intent */
    public static final String EXTRA_WORKDIR = "com.termux.execute.cwd"; // Default: "com.termux.execute.cwd"
    /** Intent {@code boolean} extra for whether to run command in background {@link Runner#APP_SHELL} or foreground {@link Runner#TERMINAL_SESSION} for the TERMUX_SERVICE.ACTION_SERVICE_EXECUTE intent */
    @Deprecated
    public static final String EXTRA_BACKGROUND = "com.termux.execute.background"; // Default: "com.termux.execute.background"
    /** Intent {@code String} extra for command the {@link Runner} for the TERMUX_SERVICE.ACTION_SERVICE_EXECUTE intent */
    public static final String EXTRA_RUNNER = "com.termux.execute.runner"; // Default: "com.termux.execute.runner"
    /** Intent {@code String} extra for custom log level for background commands defined by {@link com.termux.shared.logger.Logger} for the TERMUX_SERVICE.ACTION_SERVICE_EXECUTE intent */
    public static final String EXTRA_BACKGROUND_CUSTOM_LOG_LEVEL = "com.termux.execute.background_custom_log_level"; // Default: "com.termux.execute.background_custom_log_level"
    /** Intent {@code String} extra for session action for {@link Runner#TERMINAL_SESSION} commands for the TERMUX_SERVICE.ACTION_SERVICE_EXECUTE intent */
    public static final String EXTRA_SESSION_ACTION = "com.termux.execute.session_action"; // Default: "com.termux.execute.session_action"
    /** Intent {@code String} extra for shell name for commands for the TERMUX_SERVICE.ACTION_SERVICE_EXECUTE intent */
    public static final String EXTRA_SHELL_NAME = "com.termux.execute.shell_name"; // Default: "com.termux.execute.shell_name"
    /** Intent {@code String} extra for the {@link ExecutionCommand.ShellCreateMode}  for the TERMUX_SERVICE.ACTION_SERVICE_EXECUTE intent. */
    public static final String EXTRA_SHELL_CREATE_MODE = "com.termux.execute.shell_create_mode"; // Default: "com.termux.execute.shell_create_mode"
    /** Intent {@code String} extra for label of the command for the TERMUX_SERVICE.ACTION_SERVICE_EXECUTE intent */
    public static final String EXTRA_COMMAND_LABEL = "com.termux.execute.command_label"; // Default: "com.termux.execute.command_label"
    /** Intent markdown {@code String} extra for description of the command for the TERMUX_SERVICE.ACTION_SERVICE_EXECUTE intent */
    public static final String EXTRA_COMMAND_DESCRIPTION = "com.termux.execute.command_description"; // Default: "com.termux.execute.command_description"
    /** Intent markdown {@code String} extra for help of the command for the TERMUX_SERVICE.ACTION_SERVICE_EXECUTE intent */
    public static final String EXTRA_COMMAND_HELP = "com.termux.execute.command_help"; // Default: "com.termux.execute.command_help"
    /** Intent markdown {@code String} extra for help of the plugin API for the TERMUX_SERVICE.ACTION_SERVICE_EXECUTE intent (Internal Use Only) */
    public static final String EXTRA_PLUGIN_API_HELP = "com.termux.execute.plugin_api_help"; // Default: "com.termux.execute.plugin_help"
    /** Intent {@code Parcelable} extra for the pending intent that should be sent with the
     * result of the execution command to the execute command caller for the TERMUX_SERVICE.ACTION_SERVICE_EXECUTE intent */
    public static final String EXTRA_PENDING_INTENT = "pendingIntent"; // Default: "pendingIntent"
    /** Intent {@code String} extra for the directory path in which to write the result of the
     * execution command for the execute command caller for the TERMUX_SERVICE.ACTION_SERVICE_EXECUTE intent */
    public static final String EXTRA_RESULT_DIRECTORY = "com.termux.execute.result_directory"; // Default: "com.termux.execute.result_directory"
    /** Intent {@code boolean} extra for whether the result should be written to a single file
     * or multiple files (err, errmsg, stdout, stderr, exit_code) in
     * {@link #EXTRA_RESULT_DIRECTORY} for the TERMUX_SERVICE.ACTION_SERVICE_EXECUTE intent */
    public static final String EXTRA_RESULT_SINGLE_FILE = "com.termux.execute.result_single_file"; // Default: "com.termux.execute.result_single_file"
    /** Intent {@code String} extra for the basename of the result file that should be created
     * in {@link #EXTRA_RESULT_DIRECTORY} if {@link #EXTRA_RESULT_SINGLE_FILE} is {@code true}
     * for the TERMUX_SERVICE.ACTION_SERVICE_EXECUTE intent */
    public static final String EXTRA_RESULT_FILE_BASENAME = "com.termux.execute.result_file_basename"; // Default: "com.termux.execute.result_file_basename"
    /** Intent {@code String} extra for the output {@link Formatter} format of the
     * {@link #EXTRA_RESULT_FILE_BASENAME} result file for the TERMUX_SERVICE.ACTION_SERVICE_EXECUTE intent */
    public static final String EXTRA_RESULT_FILE_OUTPUT_FORMAT = "com.termux.execute.result_file_output_format"; // Default: "com.termux.execute.result_file_output_format"
    /** Intent {@code String} extra for the error {@link Formatter} format of the
     * {@link #EXTRA_RESULT_FILE_BASENAME} result file for the TERMUX_SERVICE.ACTION_SERVICE_EXECUTE intent */
    public static final String EXTRA_RESULT_FILE_ERROR_FORMAT = "com.termux.execute.result_file_error_format"; // Default: "com.termux.execute.result_file_error_format"
    /** Intent {@code String} extra for the optional suffix of the result files that should
     * be created in {@link #EXTRA_RESULT_DIRECTORY} if {@link #EXTRA_RESULT_SINGLE_FILE} is
     * {@code false} for the TERMUX_SERVICE.ACTION_SERVICE_EXECUTE intent */
    public static final String EXTRA_RESULT_FILES_SUFFIX = "com.termux.execute.result_files_suffix"; // Default: "com.termux.execute.result_files_suffix"



    /**
     * The value for {@link #EXTRA_SESSION_ACTION} extra that will set the new session as
     * the current session and will start {@link TERMUX_ACTIVITY} if its not running to bring
     * the new session to foreground.
     */
    public static final int VALUE_EXTRA_SESSION_ACTION_SWITCH_TO_NEW_SESSION_AND_OPEN_ACTIVITY = 0;

    /**
     * The value for {@link #EXTRA_SESSION_ACTION} extra that will keep any existing session
     * as the current session and will start {@link TERMUX_ACTIVITY} if its not running to
     * bring the existing session to foreground. The new session will be added to the left
     * sidebar in the sessions list.
     */
    public static final int VALUE_EXTRA_SESSION_ACTION_KEEP_CURRENT_SESSION_AND_OPEN_ACTIVITY = 1;

    /**
     * The value for {@link #EXTRA_SESSION_ACTION} extra that will set the new session as
     * the current session but will not start {@link TERMUX_ACTIVITY} if its not running
     * and session(s) will be seen in Termux notification and can be clicked to bring new
     * session to foreground. If the {@link TERMUX_ACTIVITY} is already running, then this
     * will behave like {@link #VALUE_EXTRA_SESSION_ACTION_KEEP_CURRENT_SESSION_AND_OPEN_ACTIVITY}.
     */
    public static final int VALUE_EXTRA_SESSION_ACTION_SWITCH_TO_NEW_SESSION_AND_DONT_OPEN_ACTIVITY = 2;

    /**
     * The value for {@link #EXTRA_SESSION_ACTION} extra that will keep any existing session
     * as the current session but will not start {@link TERMUX_ACTIVITY} if its not running
     * and session(s) will be seen in Termux notification and can be clicked to bring
     * existing session to foreground. If the {@link TERMUX_ACTIVITY} is already running,
     * then this will behave like {@link #VALUE_EXTRA_SESSION_ACTION_KEEP_CURRENT_SESSION_AND_OPEN_ACTIVITY}.
     */
    public static final int VALUE_EXTRA_SESSION_ACTION_KEEP_CURRENT_SESSION_AND_DONT_OPEN_ACTIVITY = 3;

    /** The minimum allowed value for {@link #EXTRA_SESSION_ACTION}. */
    public static final int MIN_VALUE_EXTRA_SESSION_ACTION = VALUE_EXTRA_SESSION_ACTION_SWITCH_TO_NEW_SESSION_AND_OPEN_ACTIVITY;

    /** The maximum allowed value for {@link #EXTRA_SESSION_ACTION}. */
    public static final int MAX_VALUE_EXTRA_SESSION_ACTION = VALUE_EXTRA_SESSION_ACTION_KEEP_CURRENT_SESSION_AND_DONT_OPEN_ACTIVITY;


    /** Intent {@code Bundle} extra to store result of execute command that is sent back for the
     * TERMUX_SERVICE.ACTION_SERVICE_EXECUTE intent if the {@link #EXTRA_PENDING_INTENT} is not
     * {@code null} */
    public static final String EXTRA_PLUGIN_RESULT_BUNDLE = "result"; // Default: "result"
    /** Intent {@code String} extra for stdout value of execute command of the {@link #EXTRA_PLUGIN_RESULT_BUNDLE} */
    public static final String EXTRA_PLUGIN_RESULT_BUNDLE_STDOUT = "stdout"; // Default: "stdout"
    /** Intent {@code String} extra for original length of stdout value of execute command of the {@link #EXTRA_PLUGIN_RESULT_BUNDLE} */
    public static final String EXTRA_PLUGIN_RESULT_BUNDLE_STDOUT_ORIGINAL_LENGTH = "stdout_original_length"; // Default: "stdout_original_length"
    /** Intent {@code String} extra for stderr value of execute command of the {@link #EXTRA_PLUGIN_RESULT_BUNDLE} */
    public static final String EXTRA_PLUGIN_RESULT_BUNDLE_STDERR = "stderr"; // Default: "stderr"
    /** Intent {@code String} extra for original length of stderr value of execute command of the {@link #EXTRA_PLUGIN_RESULT_BUNDLE} */
    public static final String EXTRA_PLUGIN_RESULT_BUNDLE_STDERR_ORIGINAL_LENGTH = "stderr_original_length"; // Default: "stderr_original_length"
    /** Intent {@code int} extra for exit code value of execute command of the {@link #EXTRA_PLUGIN_RESULT_BUNDLE} */
    public static final String EXTRA_PLUGIN_RESULT_BUNDLE_EXIT_CODE = "exitCode"; // Default: "exitCode"
    /** Intent {@code int} extra for err value of execute command of the {@link #EXTRA_PLUGIN_RESULT_BUNDLE} */
    public static final String EXTRA_PLUGIN_RESULT_BUNDLE_ERR = "err"; // Default: "err"
    /** Intent {@code String} extra for errmsg value of execute command of the {@link #EXTRA_PLUGIN_RESULT_BUNDLE} */
    public static final String EXTRA_PLUGIN_RESULT_BUNDLE_ERRMSG = "errmsg"; // Default: "errmsg"

}

/**
 * Termux app run command service to receive commands sent by 3rd party apps.
 */
final class RUN_COMMAND_SERVICE {
    /** Intent action to execute command with RUN_COMMAND_SERVICE */
    public static final String ACTION_RUN_COMMAND = "com.termux.RUN_COMMAND"; // Default: "com.termux.RUN_COMMAND"

    /** Intent {@code String} extra for absolute path of command for the RUN_COMMAND_SERVICE.ACTION_RUN_COMMAND intent */
    public static final String EXTRA_COMMAND_PATH = "com.termux.RUN_COMMAND_PATH"; // Default: "com.termux.RUN_COMMAND_PATH"
    /** Intent {@code String[]} extra for arguments to the executable of the command for the RUN_COMMAND_SERVICE.ACTION_RUN_COMMAND intent */
    public static final String EXTRA_ARGUMENTS = "com.termux.RUN_COMMAND_ARGUMENTS"; // Default: "com.termux.RUN_COMMAND_ARGUMENTS"
    /** Intent {@code boolean} extra for whether to replace comma alternative characters in arguments with comma characters for the RUN_COMMAND_SERVICE.ACTION_RUN_COMMAND intent */
    public static final String EXTRA_REPLACE_COMMA_ALTERNATIVE_CHARS_IN_ARGUMENTS = "com.termux.RUN_COMMAND_REPLACE_COMMA_ALTERNATIVE_CHARS_IN_ARGUMENTS"; // Default: "com.termux.RUN_COMMAND_REPLACE_COMMA_ALTERNATIVE_CHARS_IN_ARGUMENTS"
    /** Intent {@code String} extra for the comma alternative characters in arguments that should be replaced instead of the default {@link #COMMA_ALTERNATIVE} for the RUN_COMMAND_SERVICE.ACTION_RUN_COMMAND intent */
    public static final String EXTRA_COMMA_ALTERNATIVE_CHARS_IN_ARGUMENTS = "com.termux.RUN_COMMAND_COMMA_ALTERNATIVE_CHARS_IN_ARGUMENTS"; // Default: "com.termux.RUN_COMMAND_COMMA_ALTERNATIVE_CHARS_IN_ARGUMENTS"

    /** Intent {@code String} extra for stdin of the command for the RUN_COMMAND_SERVICE.ACTION_RUN_COMMAND intent */
    public static final String EXTRA_STDIN = "com.termux.RUN_COMMAND_STDIN"; // Default: "com.termux.RUN_COMMAND_STDIN"
    /** Intent {@code String} extra for current working directory of command for the RUN_COMMAND_SERVICE.ACTION_RUN_COMMAND intent */
    public static final String EXTRA_WORKDIR = "com.termux.RUN_COMMAND_WORKDIR"; // Default: "com.termux.RUN_COMMAND_WORKDIR"
    /** Intent {@code boolean} extra for whether to run command in background {@link Runner#APP_SHELL} or foreground {@link Runner#TERMINAL_SESSION} for the RUN_COMMAND_SERVICE.ACTION_RUN_COMMAND intent */
    @Deprecated
    public static final String EXTRA_BACKGROUND = "com.termux.RUN_COMMAND_BACKGROUND"; // Default: "com.termux.RUN_COMMAND_BACKGROUND"
    /** Intent {@code String} extra for command the {@link Runner} for the RUN_COMMAND_SERVICE.ACTION_RUN_COMMAND intent */
    public static final String EXTRA_RUNNER = "com.termux.RUN_COMMAND_RUNNER"; // Default: "com.termux.RUN_COMMAND_RUNNER"
    /** Intent {@code String} extra for custom log level for background commands defined by {@link com.termux.shared.logger.Logger} for the RUN_COMMAND_SERVICE.ACTION_RUN_COMMAND intent */
    public static final String EXTRA_BACKGROUND_CUSTOM_LOG_LEVEL = "com.termux.RUN_COMMAND_BACKGROUND_CUSTOM_LOG_LEVEL"; // Default: "com.termux.RUN_COMMAND_BACKGROUND_CUSTOM_LOG_LEVEL"
    /** Intent {@code String} extra for session action of {@link Runner#TERMINAL_SESSION} commands for the RUN_COMMAND_SERVICE.ACTION_RUN_COMMAND intent */
    public static final String EXTRA_SESSION_ACTION = "com.termux.RUN_COMMAND_SESSION_ACTION"; // Default: "com.termux.RUN_COMMAND_SESSION_ACTION"
    /** Intent {@code String} extra for shell name of commands for the RUN_COMMAND_SERVICE.ACTION_RUN_COMMAND intent */
    public static final String EXTRA_SHELL_NAME = "com.termux.RUN_COMMAND_SHELL_NAME"; // Default: "com.termux.RUN_COMMAND_SHELL_NAME"
    /** Intent {@code String} extra for the {@link ExecutionCommand.ShellCreateMode}  for the RUN_COMMAND_SERVICE.ACTION_RUN_COMMAND intent. */
    public static final String EXTRA_SHELL_CREATE_MODE = "com.termux.RUN_COMMAND_SHELL_CREATE_MODE"; // Default: "com.termux.RUN_COMMAND_SHELL_CREATE_MODE"
    /** Intent {@code String} extra for label of the command for the RUN_COMMAND_SERVICE.ACTION_RUN_COMMAND intent */
    public static final String EXTRA_COMMAND_LABEL = "com.termux.RUN_COMMAND_COMMAND_LABEL"; // Default: "com.termux.RUN_COMMAND_COMMAND_LABEL"
    /** Intent markdown {@code String} extra for description of the command for the RUN_COMMAND_SERVICE.ACTION_RUN_COMMAND intent */
    public static final String EXTRA_COMMAND_DESCRIPTION = "com.termux.RUN_COMMAND_COMMAND_DESCRIPTION"; // Default: "com.termux.RUN_COMMAND_COMMAND_DESCRIPTION"
    /** Intent markdown {@code String} extra for help of the command for the RUN_COMMAND_SERVICE.ACTION_RUN_COMMAND intent */
    public static final String EXTRA_COMMAND_HELP = "com.termux.RUN_COMMAND_COMMAND_HELP"; // Default: "com.termux.RUN_COMMAND_COMMAND_HELP"
    /** Intent {@code Parcelable} extra for the pending intent that should be sent with the result of the execution command to the execute command caller for the RUN_COMMAND_SERVICE.ACTION_RUN_COMMAND intent */
    public static final String EXTRA_PENDING_INTENT = "com.termux.RUN_COMMAND_PENDING_INTENT"; // Default: "com.termux.RUN_COMMAND_PENDING_INTENT"
    /** Intent {@code String} extra for the directory path in which to write the result of
     * the execution command for the execute command caller for the RUN_COMMAND_SERVICE.ACTION_RUN_COMMAND intent */
    public static final String EXTRA_RESULT_DIRECTORY = "com.termux.RUN_COMMAND_RESULT_DIRECTORY"; // Default: "com.termux.RUN_COMMAND_RESULT_DIRECTORY"
    /** Intent {@code boolean} extra for whether the result should be written to a single file
     * or multiple files (err, errmsg, stdout, stderr, exit_code) in
     * {@link #EXTRA_RESULT_DIRECTORY} for the RUN_COMMAND_SERVICE.ACTION_RUN_COMMAND intent */
    public static final String EXTRA_RESULT_SINGLE_FILE = "com.termux.RUN_COMMAND_RESULT_SINGLE_FILE"; // Default: "com.termux.RUN_COMMAND_RESULT_SINGLE_FILE"
    /** Intent {@code String} extra for the basename of the result file that should be created
     * in {@link #EXTRA_RESULT_DIRECTORY} if {@link #EXTRA_RESULT_SINGLE_FILE} is {@code true}
     * for the RUN_COMMAND_SERVICE.ACTION_RUN_COMMAND intent */
    public static final String EXTRA_RESULT_FILE_BASENAME = "com.termux.RUN_COMMAND_RESULT_FILE_BASENAME"; // Default: "com.termux.RUN_COMMAND_RESULT_FILE_BASENAME"
    /** Intent {@code String} extra for the output {@link Formatter} format of the
     * {@link #EXTRA_RESULT_FILE_BASENAME} result file for the RUN_COMMAND_SERVICE.ACTION_RUN_COMMAND intent */
    public static final String EXTRA_RESULT_FILE_OUTPUT_FORMAT = "com.termux.RUN_COMMAND_RESULT_FILE_OUTPUT_FORMAT"; // Default: "com.termux.RUN_COMMAND_RESULT_FILE_OUTPUT_FORMAT"
    /** Intent {@code String} extra for the error {@link Formatter} format of the
     * {@link #EXTRA_RESULT_FILE_BASENAME} result file for the RUN_COMMAND_SERVICE.ACTION_RUN_COMMAND intent */
    public static final String EXTRA_RESULT_FILE_ERROR_FORMAT = "com.termux.RUN_COMMAND_RESULT_FILE_ERROR_FORMAT"; // Default: "com.termux.RUN_COMMAND_RESULT_FILE_ERROR_FORMAT"
    /** Intent {@code String} extra for the optional suffix of the result files that should be
     * created in {@link #EXTRA_RESULT_DIRECTORY} if {@link #EXTRA_RESULT_SINGLE_FILE} is
     * {@code false} for the RUN_COMMAND_SERVICE.ACTION_RUN_COMMAND intent */
    public static final String EXTRA_RESULT_FILES_SUFFIX = "com.termux.RUN_COMMAND_RESULT_FILES_SUFFIX"; // Default: "com.termux.RUN_COMMAND_RESULT_FILES_SUFFIX"

}