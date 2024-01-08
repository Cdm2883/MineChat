package vip.cdms.minechat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.os.LocaleListCompat;
import androidx.core.splashscreen.SplashScreen;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

import vip.cdms.mcoreui.view.dialog.DialogBuilder;
import vip.cdms.mcoreui.util.MathUtils;
import vip.cdms.mcoreui.util.ResourcesUtils;
import vip.cdms.mcoreui.util.SoundPlayer;
import vip.cdms.mcoreui.view.button.TextButton;
import vip.cdms.mcoreui.view.show.ImageView;
import vip.cdms.mcoreui.view.show.TextView;
import vip.cdms.minechat.databinding.ActivityMainBinding;
import vip.cdms.minechat.fragment.AccountsFragment;
import vip.cdms.minechat.fragment.PluginsFragment;
import vip.cdms.minechat.fragment.ScriptsFragment;
import vip.cdms.minechat.fragment.ServersFragment;
import vip.cdms.minechat.fragment.SettingsFragment;
import vip.cdms.minechat.plugin.PluginManager;
import vip.cdms.minechat.protocol.plugin.ServiceExtension;
import vip.cdms.minechat.protocol.util.ExceptionHandler;

public class MainActivity extends MineActivity {
    private int dp;

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (AppCompatDelegate.getApplicationLocales().toLanguageTags().isEmpty()) {
            String defaultLanguage = Locale.getDefault().getLanguage().contains("zh") ? "zh-rCN" : "en";
            SharedPreferences.Editor editor = defaultSharedPreferences.edit();
            editor.putString("language", defaultLanguage);
            editor.apply();
            LocaleListCompat appLocale = LocaleListCompat.forLanguageTags(defaultLanguage);
            AppCompatDelegate.setApplicationLocales(appLocale);
        }
        super.onCreate(savedInstanceState);
        SplashScreen.installSplashScreen(this);

        String uncaughtException = defaultSharedPreferences.getString("uncaught_exception", "");
        if (!uncaughtException.isEmpty()) {
            ExceptionHandler.printStackTrace(this, "Uncaught Exception", null, uncaughtException);
            SharedPreferences.Editor editor = defaultSharedPreferences.edit();
            editor.putString("uncaught_exception", "");
            editor.apply();
        }

        dp = MathUtils.dp2px(this, 1);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 侧边栏
        addFragment(ServersFragment.class, R.drawable.icon_home, R.string.activity_main_drawer_servers);
        addFragment(PluginsFragment.class, R.drawable.icon_plugin, R.string.activity_main_drawer_plugins);
        addFragment(ScriptsFragment.class, R.drawable.icon_script, R.string.activity_main_drawer_scripts);
        addFragment(AccountsFragment.class, R.drawable.icon_account, R.string.activity_main_drawer_accounts);
        addFragment(SettingsFragment.class, R.drawable.icon_setting, R.string.activity_main_drawer_settings);
        binding.appbarDrawer.setRightIconColor(0xffffffff);
        binding.appbarDrawer.setRightButtonOnClickListener(v -> binding.appbarDrawer.getLeftButton().callOnClick());
        binding.drawerLayout.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {
//                binding.appbarDrawer.getButton().setX(drawerView.getWidth() - (drawerView.getWidth() * slideOffset) + 4 * dp);
                binding.appbarDrawer.getRightButton().setX(drawerView.getWidth() * (1 - slideOffset) + 4 * dp);
            }
        });
    }

    private final ArrayList<Class<? extends Fragment>> fragments = new ArrayList<>();
    private Fragment fragment = null;
    private void addFragment(final Class<? extends Fragment> clazz, @DrawableRes int icon, @StringRes int title) {
        final int index = fragments.size();

        // fragment添加
        if (index == 0) {
            try {
                fragment = clazz.newInstance();
            } catch (IllegalAccessException | InstantiationException e) {
                throw new RuntimeException(e);
            }
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.fragments, fragment)
                    .show(fragment)
                    .commit();
        }

        // 侧边栏处理
        ConstraintLayout list = (ConstraintLayout) ConstraintLayout.inflate(this, R.layout.layout_drawer_list, null);
        list.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, MathUtils.dp2px(this, 48)));
        ((ImageView) list.findViewById(android.R.id.icon)).setImageResource(icon);
        ((TextView) list.findViewById(android.R.id.title)).setText(title);
        if (index != 0) list.setBackgroundResource(android.R.color.transparent);
        list.setOnClickListener(v -> {
            SoundPlayer.playClickSound(this);
            if (fragment.getClass().getName().equals(clazz.getName())) return;
            try {
                fragment = clazz.newInstance();
            } catch (IllegalAccessException | InstantiationException e) {
                throw new RuntimeException(e);
            }
            binding.drawerLayout.close();

            for (int i = 0; i < binding.drawer.getChildCount(); i++)
                binding.drawer.getChildAt(i).setBackgroundResource(android.R.color.transparent);
            int paddingLeft = v.getPaddingLeft();
            int paddingTop = v.getPaddingTop();
            int paddingRight = v.getPaddingRight();
            int paddingBottom = v.getPaddingBottom();
            v.setBackground(ResourcesUtils.getPixelDrawable(this, R.drawable.list_small_background_enabled));
            v.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);

            setTitle(index == 0 ? R.string.app_name : title);
            getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(R.anim.fragment_in, R.anim.fragment_out)
                    .replace(R.id.fragments, fragment)
                    .commit();
        });
        binding.drawer.addView(list);

        fragments.add(index, clazz);
    }

    static boolean started = false;
    @Override
    protected void onStart() {
        super.onStart();

        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.fragment_in, R.anim.fragment_out)
                .replace(R.id.fragments, fragment)
                .commit();

        if (started) return;
        started = true;

        new ExceptionHandler()
                .setUncaughtHandler(e ->
                        ExceptionHandler.printStackTrace(this, getString(R.string.activity_main_error_load_plugins_title), e))
                .hand(IOException.class, e ->
                        ExceptionHandler.printStackTrace(this, getString(R.string.activity_main_error_load_plugins_title),
                                getString(R.string.activity_main_error_load_plugins_ioexception, e.getMessage()),
                                ExceptionHandler.printStackTrace(e)))
                .tryCatch(() -> PluginManager.init(this));

        ExceptionHandler exceptionHandler = new ExceptionHandler()
                .setUncaughtHandler(e -> {
                    String message = e.getMessage();
                    if (message == null || message.isEmpty()) message = e.toString();
                    AlertDialog dialog = new DialogBuilder(this)
                            .setTitle(message)
                            .setContent(ExceptionHandler.printStackTrace(e))
                            .create();
                    Objects.requireNonNull(dialog.getWindow())
                            .setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
                    dialog.show();
                });
        for (PluginManager.PluginConfig config : PluginManager.getPlugins()) {
            ServiceExtension service = config.createInstance(ServiceExtension.class, getApplication(), exceptionHandler);
            if (service == null) continue;
            service.onActivityStart(this);
            if (config.isEnabled()) service.onStart();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        requestPermissions();
    }

    // 动态申请权限
    private void requestPermissions() {
        if (Build.VERSION.SDK_INT >= 30) {
            if (Environment.isExternalStorageManager()) return;
            new DialogBuilder(this)
                    .setTitle(getString(R.string.activity_main_permissions_request_dialog_title))
                    .setContent(getString(R.string.activity_main_permissions_request_dialog_content))
                    .addAction(getString(R.string.activity_main_permissions_request_dialog_ok), v -> {
                        Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                        startActivity(intent);
                    })
                    .addAction(getString(R.string.activity_main_permissions_request_dialog_cancel), TextButton.Style.RED, v -> android.os.Process.killProcess(android.os.Process.myPid()))
                    .setOnCancelListener(() -> android.os.Process.killProcess(android.os.Process.myPid()))
                    .show();
        } else if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            new DialogBuilder(this)
                    .setTitle(getString(R.string.activity_main_permissions_request_dialog_title))
                    .setContent(getString(R.string.activity_main_permissions_request_dialog_content))
                    .addAction(getString(R.string.activity_main_permissions_request_dialog_ok), v -> ActivityCompat.requestPermissions(this, new String[]{
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                    }, 1))
                    .addAction(getString(R.string.activity_main_permissions_request_dialog_cancel), TextButton.Style.RED, v -> android.os.Process.killProcess(android.os.Process.myPid()))
                    .setOnCancelListener(() -> android.os.Process.killProcess(android.os.Process.myPid()))
                    .show();

        if (!Settings.canDrawOverlays(this)) new DialogBuilder(this)
                    .setTitle(getString(R.string.activity_main_permissions_request_dialog_title))
                    .setContent(getString(R.string.activity_main_permissions_request_dialog_content))
                    .addAction(getString(R.string.activity_main_permissions_request_dialog_ok), v -> {
                        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                        intent.setData(Uri.parse("package:" + getPackageName()));
                        startActivityForResult(intent, 0);
                    })
                    .addAction(getString(R.string.activity_main_permissions_request_dialog_cancel), TextButton.Style.RED, v -> android.os.Process.killProcess(android.os.Process.myPid()))
                    .setOnCancelListener(() -> android.os.Process.killProcess(android.os.Process.myPid()))
                    .show();
    }
}