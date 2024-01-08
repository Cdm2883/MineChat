package vip.cdms.minechat.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;

import vip.cdms.mcoreui.util.MCFontWrapper;
import vip.cdms.mcoreui.util.ResourcesUtils;
import vip.cdms.mcoreui.util.SoundPlayer;
import vip.cdms.mcoreui.view.dialog.CustomFormBuilder;
import vip.cdms.minechat.MainActivity;
import vip.cdms.minechat.R;
import vip.cdms.minechat.databinding.FragmentPluginBinding;
import vip.cdms.minechat.plugin.PluginManager;
import vip.cdms.minechat.protocol.plugin.Plugin;
import vip.cdms.minechat.protocol.plugin.ProtocolExtension;
import vip.cdms.minechat.protocol.plugin.ProtocolProvider;
import vip.cdms.minechat.protocol.plugin.ServiceExtension;
import vip.cdms.minechat.protocol.util.ExceptionHandler;
import vip.cdms.minechat.view.SlideListAdapter;

public class PluginsFragment extends Fragment {
    private MainActivity activity;
    private FragmentPluginBinding binding;
    private SlideListAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (MainActivity) getActivity();
    }

    private void refresh() {
        adapter.clearDataItems();
        int checked = binding.textButtonToggleGroup.getChecked();
        ArrayList<PluginManager.PluginConfig> plugins = PluginManager.getPlugins();
        for (PluginManager.PluginConfig config : plugins) ExceptionHandler.tryCatch(activity, () -> {
            Plugin plugin = config.createInstance(activity);
            if (checked == 0 && !(plugin instanceof ProtocolProvider)) return;
            else if (checked == 1 && !(plugin instanceof ProtocolExtension)) return;
            else if (checked == 2 && !(plugin instanceof ServiceExtension)) return;

            SlideListAdapter.DataItem dataItem =
                    new SlideListAdapter.DataItem(
                            ExceptionHandler.ror(plugin::getPluginIcon,
                                    ResourcesUtils.getDrawable(activity, R.drawable.icon_plugin)),
                            MCFontWrapper.WRAPPER.wrap(plugin.getPluginTitle()),
                            MCFontWrapper.WRAPPER.wrap(plugin.getPluginSummary()),
                            v -> SoundPlayer.playClickSound(activity)
                    );

            if (plugin instanceof ProtocolExtension
                    || plugin instanceof ServiceExtension)
                dataItem.setNumber(config.getPriority());

            if (!(plugin instanceof ProtocolProvider))
                dataItem.addSlide(null, null);

            if (plugin instanceof ProtocolExtension)
                dataItem.setSwitch((switchWeight, isChecked) -> {
                    config.setEnabled(isChecked);
                }, config.isEnabled());

            if (plugin instanceof ServiceExtension service)
                dataItem.setSwitch((switchWeight, isChecked) -> {
                    config.setEnabled(isChecked);
                    if (isChecked) service.onStart();
                    else service.onDestroy();
                }, config.isEnabled());

            if (plugin instanceof ProtocolExtension
                    || plugin instanceof ServiceExtension)
                dataItem.setOnClickListener(v -> new CustomFormBuilder()
                        .setTitle(getString(R.string.fragment_plugin_priority_dialog_title))
                        .addSlider(getString(R.string.fragment_plugin_priority_dialog_content), -100, 100, 1, config.getPriority())
                        .setCallback(callback -> {
                            if (callback.isCancel()) return;
                            config.setPriority(callback.responseData().get(0).getAsInt());
                            refresh();
                        })
                        .show(getActivity()));

            dataItem.addSlide(activity, R.drawable.icon_small_setting, v -> plugin.openPluginSetting());
            dataItem.addSlide(activity, R.drawable.icon_small_about, v -> plugin.openPluginAbout());
            adapter.addDataItem(dataItem);
        });
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentPluginBinding.inflate(inflater);
        View root = binding.getRoot();
        root.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));

        binding.textButtonToggleGroup.addOnButtonCheckedListener((group, position) -> refresh());
        adapter = binding.recyclerView.getAdapter();
        binding.recyclerView.setOnItemMoveListener((fromPosition, toPosition) -> false);
        refresh();

        return binding.getRoot();
    }
}