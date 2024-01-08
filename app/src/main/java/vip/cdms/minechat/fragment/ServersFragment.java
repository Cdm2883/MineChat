package vip.cdms.minechat.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import vip.cdms.mcoreui.util.MCFontWrapper;
import vip.cdms.mcoreui.util.MCTextParser;
import vip.cdms.mcoreui.util.ReflectionUtils;
import vip.cdms.mcoreui.util.StringUtils;
import vip.cdms.mcoreui.view.button.TextButton;
import vip.cdms.mcoreui.view.dialog.DialogBuilder;
import vip.cdms.mcoreui.view.dialog.SimpleFormBuilder;
import vip.cdms.minechat.MainActivity;
import vip.cdms.minechat.R;
import vip.cdms.minechat.activity.chat.ChatActivity;
import vip.cdms.minechat.data.chathistory.ChatHistoryDatabase;
import vip.cdms.minechat.databinding.FragmentServerBinding;
import vip.cdms.minechat.plugin.PluginManager;
import vip.cdms.minechat.protocol.app.Storage;
import vip.cdms.minechat.protocol.plugin.ProtocolProvider;
import vip.cdms.minechat.protocol.util.ExceptionHandler;
import vip.cdms.minechat.protocol.util.StorageUtils;
import vip.cdms.minechat.view.SlideListAdapter;

public class ServersFragment extends Fragment {
    private MainActivity activity;
    private FragmentServerBinding binding;
    private SlideListAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (MainActivity) getActivity();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    public record AServer(
            String id,
            String protocolProvider,
            ProtocolProvider.ServerConfig config
    ) {
        public JsonObject toJson() {
            JsonObject json = new JsonObject();
            json.addProperty("id", id);
            json.addProperty("protocol_provider", protocolProvider);
            json.add("config", config.toJson());
            return json;
        }
        public static AServer formJson(JsonObject json) {
            return new AServer(
                    json.get("id").getAsString(),
                    json.get("protocol_provider").getAsString(),
                    ProtocolProvider.ServerConfig.formJson(  // 向下兼容
                            json.has("config") ?
                            json.get("config").getAsJsonObject() : json.get("options").getAsJsonObject()
                    )
            );
        }
    }
    static ThreadPoolExecutor threadPool = new ThreadPoolExecutor(0, Integer.MAX_VALUE,
            60L, TimeUnit.SECONDS,
            new SynchronousQueue<>());
    // 为了防止用户在拖动排序之后motd还没有加载完成 (BB: UDP不用握手真的比TCP快很多), motd加载完成之后信息出现错位的情况. 这是一个索引的对照表
    ArrayList<Integer> indexLookupTables = new ArrayList<>();
    int refreshTimes = 0;  // 防止上一次加载过慢的信息在本次被显示
    JsonArray servers;
    /** @noinspection unchecked*/
    private void refresh() {
        final int finalRefreshTimes = ++refreshTimes;

        binding.swipeRefresh.setRefreshing(true);
        indexLookupTables.clear();
        adapter.clearDataItems();
        servers = StorageUtils.read(Storage.FILES_DIR, "servers", JsonArray.class);
        if (servers == null || servers.isEmpty()) {
            binding.recyclerView.setVisibility(View.GONE);
            binding.empty.setVisibility(View.VISIBLE);
            binding.swipeRefresh.setRefreshing(false);
            return;
        } else {
            binding.recyclerView.setVisibility(View.VISIBLE);
            binding.empty.setVisibility(View.GONE);
        }
        for (int i = 0; i < servers.size(); i++) try {
            int finalI = i;

            var jsonElement = servers.get(i);
            if (!(jsonElement instanceof JsonObject element))
                throw new IllegalArgumentException("Need JsonObject but got " + jsonElement.getClass().getSimpleName());
            indexLookupTables.add(i, i);
            AServer aServer = AServer.formJson(element);
            ProtocolProvider protocolProvider =
                        PluginManager.createInstance(activity,
                                (Class<ProtocolProvider>) ReflectionUtils.classForName(aServer.protocolProvider()));

            SlideListAdapter.DataItem dataItem = new SlideListAdapter.DataItem(activity, R.drawable.icon_home, "...", "...", null);
            dataItem.setOnClickListener(v -> startActivity(new Intent(activity, ChatActivity.class)
                            .putExtra("title", String.valueOf(dataItem.title))
                            .putExtra("data", new Gson().toJson(aServer.toJson()))))
                    .addSlide(null, null)
                    .addSlide(activity, R.drawable.icon_small_setting, v -> ExceptionHandler.tryCatch(activity, () ->
                            protocolProvider.createOrEdit(aServer.config(), config -> {
                                servers.set(finalI, new AServer(aServer.id(), aServer.protocolProvider(), config).toJson());
                                StorageUtils.write(Storage.FILES_DIR, "servers", servers);
                                refresh();
                            })))
                    .addSlide(activity, R.drawable.icon_small_about, v -> new DialogBuilder(getActivity())
                            .setTitle(getString(R.string.fragment_server_info_dialog_title))
                            .setContent(getString(R.string.fragment_server_info_dialog_content, aServer.id))
                            .addAction(getString(R.string.fragment_server_info_dialog_protocol), v1 ->
                                    protocolProvider.openPluginAbout())
                            .addAction(getString(R.string.fragment_server_info_dialog_clear_hist), TextButton.Style.RED, v1 ->
                                    Completable.fromAction(() -> ChatHistoryDatabase.INSTANCE().getChatHistoryDao().clear(aServer.id))
                                    .subscribeOn(Schedulers.io())
                                    .subscribe())
                            .show())
                    .addSlide(activity, R.drawable.icon_small_delete, v -> new DialogBuilder(getActivity())
                            .setTitle(getString(R.string.fragment_server_delete_dialog_title))
                            .setContent(getString(R.string.fragment_server_delete_dialog_content, dataItem.title))
                            .addAction(getString(R.string.fragment_server_delete_dialog_no))
                            .addAction(getString(R.string.fragment_server_delete_dialog_yes), TextButton.Style.RED, v1 -> {
                                servers.remove(finalI);
                                StorageUtils.write(Storage.FILES_DIR, "servers", servers);
                                refresh();
                            })
                            .show());
            adapter.addDataItem(dataItem);

            /*new Thread*/threadPool.execute(() -> protocolProvider.motd(
                    aServer.config(),
                    ProtocolProvider.MotdCallback.make(
                            icon -> { if (finalRefreshTimes == refreshTimes) adapter.changeDataItem(indexLookupTables.indexOf(finalI), dataItem.setIcon(icon)); },
                            title -> { if (finalRefreshTimes == refreshTimes) adapter.changeDataItem(indexLookupTables.indexOf(finalI), dataItem.setTitle(title)); },
                            summary -> { if (finalRefreshTimes == refreshTimes) adapter.changeDataItem(indexLookupTables.indexOf(finalI), dataItem.setSummary(summary)); },
                            topText -> { if (finalRefreshTimes == refreshTimes) adapter.changeDataItem(indexLookupTables.indexOf(finalI), dataItem.setTopText(topText)); },
                            topIcon -> { if (finalRefreshTimes == refreshTimes) adapter.changeDataItem(indexLookupTables.indexOf(finalI), dataItem.setTopIcon(topIcon)); }
                    )
            ))/*.start()*/;
        } catch (Throwable e) {
            int finalI = i;
            indexLookupTables.add(i, i);
            adapter.addDataItem(new SlideListAdapter.DataItem(
                    activity, R.drawable.icon_small_warning, MCFontWrapper.WRAPPER.wrap(MCTextParser.SS + "4ERROR"), e.toString(), null)
                    .setOnClickListener(v -> ExceptionHandler.printStackTrace(activity, e))
                    .addSlide(null, null)
                    .addSlide(activity, R.drawable.icon_small_about, v -> new DialogBuilder(activity)
                            .setTitle("JSON")
                            .setContent(new GsonBuilder().setPrettyPrinting()
                                    .create().toJson(servers.get(finalI)), false)
                            .show()));
        }
        binding.swipeRefresh.setRefreshing(false);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentServerBinding.inflate(inflater);

        adapter = binding.recyclerView.getAdapter();
        binding.recyclerView.setOnItemMoveListener((fromPosition, toPosition) -> {
            servers.set(fromPosition, servers.set(toPosition, servers.get(fromPosition)));
            Collections.swap(indexLookupTables, fromPosition, toPosition);
            return true;
        });
        binding.recyclerView.setOnItemMovedListener((fromPosition, toPosition) -> {
            if (fromPosition == toPosition) return;
            StorageUtils.write(Storage.FILES_DIR, "servers", servers);
        });
        binding.swipeRefresh.setOnRefreshListener(this::refresh);
        refresh();

        binding.addServerButton.setOnClickListener(v -> ExceptionHandler.tryCatch(activity, () -> {
            ArrayList<PluginManager.PluginConfig> plugins = PluginManager.getPlugins();
            ArrayList<ProtocolProvider> instances = new ArrayList<>();
            SimpleFormBuilder simpleFormBuilder = new SimpleFormBuilder()
                    .setTitle(getString(R.string.fragment_server_add))
                    .setContent(getString(R.string.fragment_server_add_dialog_content))
                    .setFontWrapper(MCFontWrapper.WRAPPER)
                    .setCallback(callback -> ExceptionHandler.tryCatch(activity, () -> {
                        if (callback.isCancel()) return;
                        ProtocolProvider protocolProvider = instances.get(callback.responseData().getAsInt());
                        protocolProvider.createOrEdit(null, options -> {
                            servers = StorageUtils.read(Storage.FILES_DIR, "servers", JsonArray.class);
                            if (servers == null) servers = new JsonArray();
                            servers.add(new AServer(StringUtils.randomUUID(), protocolProvider.getClass().getName(), options).toJson());
                            StorageUtils.write(Storage.FILES_DIR, "servers", servers);
                            refresh();
                            binding.recyclerView.scrollToBottom();
                        });
                    }));
            for (PluginManager.PluginConfig pluginConfig : plugins) {
                ProtocolProvider protocolProvider = pluginConfig.createInstance(ProtocolProvider.class, activity);
                if (protocolProvider == null) continue;
                instances.add(protocolProvider);
                CharSequence title = protocolProvider.getPluginTitle();
                CharSequence summary = protocolProvider.getPluginSummary();

                SpannableStringBuilder content = new SpannableStringBuilder(title + "\n" + summary);
                content.setSpan(new RelativeSizeSpan(0.5f), title.length(), content.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                simpleFormBuilder.addButton(content, protocolProvider.getPluginIcon());
            }
            simpleFormBuilder.show(activity);
        }));

        View root = binding.getRoot();
        root.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        return binding.getRoot();
    }
}