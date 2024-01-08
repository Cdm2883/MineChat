package vip.cdms.minechat.fragment;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Files;

import vip.cdms.mcoreui.preference.PreferenceFragment;
import vip.cdms.mcoreui.view.dialog.DialogBuilder;
import vip.cdms.mcoreui.view.show.Toast;
import vip.cdms.minechat.MainActivity;
import vip.cdms.minechat.R;
import vip.cdms.minechat.protocol.app.Storage;
import vip.cdms.minechat.protocol.util.ExceptionHandler;
import vip.cdms.minechat.protocol.util.StorageUtils;
import vip.cdms.minechat.util.ApkUtils;
import vip.cdms.minechat.util.BedrockResource;

public class SettingsFragment extends PreferenceFragment {
    private MainActivity activity;

    /** @noinspection DataFlowIssue*/
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
        activity = (MainActivity) getActivity();

        findPreference("language").setOnPreferenceChangeListener((preference, newValue) -> {
            LocaleListCompat appLocale = LocaleListCompat.forLanguageTags((String) newValue);
            AppCompatDelegate.setApplicationLocales(appLocale);
            return true;
        });

        findPreference("set_up_bedrock_resource_pack").setOnPreferenceClickListener(preference -> {
            ExceptionHandler.tryCatch(activity, () -> {
                DialogBuilder dialogBuilder = new DialogBuilder(activity)
                        .setTitle(getString(R.string.fragment_setting_set_up_bedrock_resource_pack_dialog_title))
                        .setContent(getString(R.string.fragment_setting_set_up_bedrock_resource_pack_dialog_content, BedrockResource.ROOT_DIR.getPath()), false)
                        .addAction(getString(R.string.fragment_setting_set_up_bedrock_resource_pack_dialog_action));
                View decorView = dialogBuilder.show().getWindow().getDecorView();

                Field buttonsField = DialogBuilder.class.getDeclaredField("buttons");
                buttonsField.setAccessible(true);
                LinearLayout buttons = (LinearLayout) buttonsField.get(dialogBuilder);
                buttons.getChildAt(0).setOnClickListener(v -> ExceptionHandler.tryCatch(activity, () -> {
                    v.setEnabled(false);
                    String mcPackageName = "com.mojang.minecraftpe";
                    PackageInfo packageInfo = getActivity().getPackageManager().getPackageInfo(mcPackageName, 0);
                    File zip = new File(Storage.EXTERNAL_CACHE, mcPackageName + ".apk");
                    ApkUtils.extract(packageInfo, zip);
                    ApkUtils.unzip(zip, BedrockResource.ROOT_DIR, "assets/resource_packs/vanilla/", "png", "lang");
                    Files.delete(zip.toPath());
                    BedrockResource.init();
                    new Toast().setTitle("SUCCESS").setMessage("成功").show(decorView);
                }));
            });
            return true;
        });

        PackageManager packageManager = activity.getPackageManager();
        final PackageInfo[] packageInfo = new PackageInfo[1];
        ExceptionHandler.tryCatch(activity, () -> packageInfo[0] = packageManager.getPackageInfo(activity.getPackageName(), 0));
        findPreference("about").setSummary(packageInfo[0].versionName + " (" + packageInfo[0].versionCode + ")");
    }
}