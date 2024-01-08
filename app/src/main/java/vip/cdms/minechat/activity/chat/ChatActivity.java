package vip.cdms.minechat.activity.chat;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.IBinder;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import vip.cdms.mcoreui.util.IconFont;
import vip.cdms.mcoreui.util.ImageUtils;
import vip.cdms.mcoreui.util.MCFontWrapper;
import vip.cdms.mcoreui.util.MCTextParser;
import vip.cdms.mcoreui.util.MathUtils;
import vip.cdms.mcoreui.util.ResourcesUtils;
import vip.cdms.mcoreui.util.TimeUtils;
import vip.cdms.mcoreui.util.ViewUtils;
import vip.cdms.mcoreui.util.WebLinkWrapper;
import vip.cdms.mcoreui.view.dialog.CustomFormBuilder;
import vip.cdms.mcoreui.view.dialog.SimpleFormBuilder;
import vip.cdms.mcoreui.view.button.TextButton;
import vip.cdms.mcoreui.view.dialog.DialogBuilder;
import vip.cdms.mcoreui.view.show.ImageView;
import vip.cdms.minechat.MineActivity;
import vip.cdms.minechat.R;
import vip.cdms.minechat.activity.chat.controller.PlayerListControllerImpl;
import vip.cdms.minechat.activity.chat.controller.ScoreboardControllerImpl;
import vip.cdms.minechat.activity.chat.controller.TitleControllerImpl;
import vip.cdms.minechat.data.chathistory.AChatHistory;
import vip.cdms.minechat.data.chathistory.ChatHistoryDao;
import vip.cdms.minechat.data.chathistory.ChatHistoryDatabase;
import vip.cdms.minechat.databinding.ActivityChatBinding;
import vip.cdms.minechat.fragment.ServersFragment;
import vip.cdms.minechat.plugin.PluginManager;
import vip.cdms.minechat.protocol.dataexchange.client.Client;
import vip.cdms.minechat.protocol.dataexchange.ui.PlayerListController;
import vip.cdms.minechat.protocol.dataexchange.ui.ScoreboardController;
import vip.cdms.minechat.protocol.dataexchange.ui.TitleController;
import vip.cdms.minechat.protocol.plugin.ProtocolExtension;
import vip.cdms.minechat.protocol.plugin.ProtocolProvider;
import vip.cdms.minechat.protocol.util.ExceptionHandler;
import vip.cdms.minechat.util.BedrockResource;
import vip.cdms.minechat.util.RunnableEx;
import vip.cdms.minechat.view.SwipeRefreshLayout;

// todo 显示聊天悬浮窗时可在通知栏快捷发送消息
public class ChatActivity extends MineActivity {
    public ActivityChatBinding binding;
    public int dp;
    public String oTitle;
    public ServersFragment.AServer aServer;
    public ProtocolProvider protocolProvider;
    public final ArrayList<ProtocolExtension> protocolExtensions = new ArrayList<>();
    public Client<?> client = null;
    public Thread connectionThread;
    public ChatHistoryDao chatHistoryDao;
    public boolean canFloat = false;

    public NotificationManager notificationManager;

    public final AppbarExpander appbarExpander = new AppbarExpander(this);
    public final HashMap<String, View> playerListId2View = new HashMap<>();
    public MessageAdapter adapter;
    public TextScale textScale;

    public TitleController titleController;
    public ScoreboardController scoreboardController;
    public PlayerListController playerListController;

    // todo
    public static void startConnect(String protocolProvider, ProtocolProvider.ServerConfig config) {}

    /** @noinspection unchecked*/
    @SuppressLint({"ClickableViewAccessibility", "UnspecifiedRegisterReceiverFlag"})
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dp = MathUtils.dp2px(this, 1);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        canFloat = PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean("chat_floating_window", true);

        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        Intent intent = getIntent();
        aServer = ServersFragment.AServer.formJson(JsonParser.parseString(Objects.requireNonNull(intent.getStringExtra("data"))).getAsJsonObject());
        ExceptionHandler.tryCatch(this, () -> protocolProvider =
                PluginManager.createInstance(this, (Class<? extends ProtocolProvider>) Class.forName(aServer.protocolProvider()), new ExceptionHandler()
                        .setUncaughtHandler(this::error)));

        // 应用栏
        binding.appbar.setTitle(oTitle = intent.getStringExtra("title"));
        binding.appbar.setLeftButtonOnClickListener(v -> onBackPressed());

        binding.appbar.setRightIconColor(0xff000000);
        binding.appbar.setRightButtonOnClickListener(v -> {
            // todo i18n
            // todo 插件支持在这里注册设置项
            new CustomFormBuilder()
                    .setTitle(getString(R.string.activity_chat_setting_title))
                    .addInput(getString(R.string.activity_chat_setting_message_size), Float.toString(MessageAdapter.DEFAULT_TEXT_SIZE), Float.toString(adapter.textSize))
                    .addSwitch(getString(R.string.activity_chat_setting_auto_reconnect), autoReconnect != -1)
                    .setCallback(callback -> {
                        if (callback.isCancel()) return;
                        JsonArray response = callback.responseData();

                        textScale.scale(Float.parseFloat(response.get(0).getAsString()));

                        boolean autoReconnect = response.get(1).getAsBoolean();
                        if (autoReconnect && this.autoReconnect == -1) this.autoReconnect = 0;
                        else if (!autoReconnect) this.autoReconnect = -1;
                    })
                    .show(this);
        });

        binding.appbar.post(() -> {
            ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) binding.appbarMore.getLayoutParams();
            Rect rect = new Rect();
            getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
            layoutParams.matchConstraintMaxHeight = rect.height()
                    - binding.appbar.getHeight();
            binding.appbarMore.setLayoutParams(layoutParams);
        });
        binding.appbar.setOnTouchListener(appbarExpander);

        // 创建所有已启用的插件实例
        for (PluginManager.PluginConfig config : PluginManager.getPlugins()) {
            if (!config.isEnabled()) continue;
            ProtocolExtension extension = config.createInstance(ProtocolExtension.class, this);
            if (extension == null) continue;
            protocolExtensions.add(extension);
        }

        // 连接
        binding.clientConnect.setOnClickListener(v -> {
            titleController = new TitleControllerImpl(this);
            scoreboardController = new ScoreboardControllerImpl(this);
            playerListController = new PlayerListControllerImpl(this);
            reconnect(null, -1);
            {
                ImageView providerImage = binding.providerButton.findViewById(vip.cdms.mcoreui.R.id.image_view);
                providerImage.setImageDrawable(protocolProvider.getPluginIcon());
                TextButton providerButton = binding.providerButton.findViewById(vip.cdms.mcoreui.R.id.button);
                providerButton.setText(protocolProvider.getPluginTitle());
                providerButton.setOnClickListener((v1) -> {
                    onBackPressed();
                    client.moreOnCLick(v1);
                });
            }
        });
        binding.sidebar.setOnTouchListener(new ViewUtils.DragTouchListener(true, true));
        binding.sidebar.setOnClickListener(v -> {
            {
                ValueAnimator anim = ValueAnimator.ofFloat(v.getTranslationX(), 0);
                anim.setInterpolator(new DecelerateInterpolator());
                anim.addUpdateListener(animation -> v.setTranslationX((Float) animation.getAnimatedValue()));
                anim.start();
            }
            {
                ValueAnimator anim = ValueAnimator.ofFloat(v.getTranslationY(), 0);
                anim.setInterpolator(new DecelerateInterpolator());
                anim.addUpdateListener(animation -> v.setTranslationY((Float) animation.getAnimatedValue()));
                anim.start();
            }
            {
                ValueAnimator anim = ValueAnimator.ofFloat(v.getScaleX(), 1);
                anim.setInterpolator(new DecelerateInterpolator());
                anim.addUpdateListener(animation -> v.setScaleX((Float) animation.getAnimatedValue()));
                anim.start();
            }
            {
                ValueAnimator anim = ValueAnimator.ofFloat(v.getScaleY(), 1);
                anim.setInterpolator(new DecelerateInterpolator());
                anim.addUpdateListener(animation -> v.setScaleY((Float) animation.getAnimatedValue()));
                anim.start();
            }
        });

        // 消息列表
        binding.appSilhouette.setImageDrawable(ResourcesUtils.getPixelDrawable(this, R.drawable.app_silhouette));
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        binding.recyclerView.setLayoutManager(linearLayoutManager);
        binding.recyclerView.setAdapter(adapter = new MessageAdapter(linearLayoutManager));
        ViewUtils.setOreUIVerticalScrollBar(binding.recyclerView);
        binding.recyclerView.addOnItemTouchListener(textScale = new TextScale(adapter, binding.recyclerView));

        // 加载页面
        LinearLayoutManager loadingLinearLayoutManager = new LinearLayoutManager(this);
        loadingLinearLayoutManager.setStackFromEnd(true);
        binding.loadingRecyclerView.setLayoutManager(loadingLinearLayoutManager);
        loadingAdapter = new MessageAdapter(loadingLinearLayoutManager);
        loadingAdapter.fontWrapper = charSequence -> WebLinkWrapper.WRAPPER.wrap(MCFontWrapper.WRAPPER.wrap(charSequence));
        binding.loadingRecyclerView.setAdapter(loadingAdapter);
        binding.loadingImage.setImageDrawable(SwipeRefreshLayout.getLoadingDrawable(this));

        // 加载聊天记录
        chatHistoryDao = ChatHistoryDatabase.INSTANCE().getChatHistoryDao();
        LiveData<List<AChatHistory>> chatHistoryLiveData = chatHistoryDao.get(aServer.id());
        chatHistoryLiveData.observe(this, new Observer<>() {
            @Override
            public void onChanged(List<AChatHistory> chatHistories) {
                chatHistoryLiveData.removeObserver(this);

//                ArrayList<String> chatContents = new ArrayList<>();
//                for (var chatHistory : chatHistories) chatContents.add(chatHistory.getMessage());
//                adapter.addMessage(chatContents.toArray(new String[0]));

                for (var chatHistory : chatHistories) adapter.addMessage(chatHistory.isMe(), chatHistory.getMessage());

                loading("| " + getString(R.string.activity_chat_history_loaded));
                loaded();
            }
        });
        loading("- " + getString(R.string.activity_chat_history_load) + "...");

        // 暂停菜单
        binding.pauseMenu.setOnClickListener(v -> onBackPressed());
        binding.pauseMenuExit.setOnClickListener(v -> finish());

        // 发送按钮
        SpannableString iconSendSpannable = new SpannableString(" ");
        iconSendSpannable.setSpan(new ImageSpan(this, R.drawable.icon_small_send), 0, iconSendSpannable.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        SpannableString iconCommandSpannable = new SpannableString(" ");
        iconCommandSpannable.setSpan(new ImageSpan(this, R.drawable.icon_small_command), 0, iconCommandSpannable.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        binding.buttonSend.setText(iconSendSpannable);
        boolean[] isSendIcon = {true};
        binding.editText.addTextChangedListener((CustomFormBuilder.SimpleTextWatcher) (s, start, before, count) -> {
            if (isSendIcon[0] && s.toString().startsWith("/")) {
                isSendIcon[0] = false;
                binding.buttonSend.setText(iconCommandSpannable);
            } else if (!isSendIcon[0] && !s.toString().startsWith("/")) {
                isSendIcon[0] = true;
                binding.buttonSend.setText(iconSendSpannable);
            }
        });
        binding.buttonSend.setOnClickListener(v -> {
            if (client == null) return;
            Editable editable = binding.editText.getText();
            if (editable == null) return;
            String input = binding.editText.getText().toString();
            if (input.isEmpty()) return;
            send(input);
            binding.editText.setText("");
        });
    }

    private int autoReconnect = -1;
    private ImageSpan iconReconnectSpan;
    private final TimeUtils.ThrottleTask autoReconnectTask = new TimeUtils.ThrottleTask(1000);
    private void autoReconnect(CharSequence title, CharSequence text) {
        if (iconReconnectSpan == null)
            iconReconnectSpan = new ImageSpan(IconFont.make(ImageUtils.drawableToBitmap(ResourcesUtils.getDrawable(this, R.drawable.icon_reconnect))));

        autoReconnectTask.setRunnable(() -> {
            autoReconnect++;
            reconnect(null, -1);

            runOnUiThread(() -> {
                binding.appbar.setTitle(oTitle + " (" + autoReconnect + ")");
                justPrint(MCFontWrapper.WRAPPER.prefix(iconReconnectSpan, MCTextParser.easyFormat(" &c==========")));
                justPrint(MCFontWrapper.WRAPPER.prefix(iconReconnectSpan, MCTextParser.easyFormat(" &4" + getString(R.string.activity_chat_setting_auto_reconnect))));
                justPrint(MCFontWrapper.WRAPPER.prefix(iconReconnectSpan, MCTextParser.easyFormat(" &c==========")));
                justPrint(title);
                justPrint(text);
            });
        }).run();
    }

    public void reconnect(String host, int port) {
        if (host == null || port == -1) {
            host = aServer.config().host();
            port = aServer.config().port();
        }

        for (ProtocolExtension extension : protocolExtensions)
            extension.onDisconnect();
        if (client != null) {
            client.close();
            client = null;
        }

        String finalHost = host;
        int finalPort = port;
        runOnUiThread(((RunnableEx) () -> {
            isLoading = false;
            binding.loadingImage.setVisibility(View.GONE);
            binding.loadingContent.setVisibility(View.GONE);
            binding.loading.setVisibility(View.GONE);
            binding.buttonSend.setEnabled(false);
            binding.editText.setEnabled(false);

//            binding.appbarMoreLayout.removeAllViews();
            binding.sidebar.setVisibility(View.GONE);
            binding.sidebarTitle.setText("");
            binding.sidebarItems.removeAllViews();

            binding.pauseMenuTitle.setText("...");
            binding.pauseMenuPlayers.removeAllViews();
            binding.extensions.removeAllViews();
        }).andThen(() -> (connectionThread = new Thread(() -> new ExceptionHandler()
                .setUncaughtHandler(this::error)
                .tryCatch(() -> protocolProvider.connect(aServer.config().host(finalHost).port(finalPort), client -> ChatActivity.this.client = client
                        .setOnConnectingProgressListener(progress -> runOnUiThread(() -> loading(progress)))
                        .setOnConnectedListener(() -> runOnUiThread(() -> {
                            // 加载插件
                            for (ProtocolExtension extension : protocolExtensions) {
                                extension.onConnected(protocolProvider, new ExtensionClientImpl(this));

                                ConstraintLayout layout = (ConstraintLayout)
                                        ConstraintLayout.inflate(this, vip.cdms.mcoreui.R.layout.layout_image_button, null);
                                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                                layoutParams.topMargin = 8 * dp;
                                layout.setLayoutParams(layoutParams);

                                ImageView image = layout.findViewById(vip.cdms.mcoreui.R.id.image_view);
                                image.setImageDrawable(extension.getPluginIcon());

                                TextButton button = layout.findViewById(vip.cdms.mcoreui.R.id.button);
                                button.setText(extension.getPluginTitle());
                                button.setOnClickListener(v1 -> {
                                    onBackPressed();
                                    extension.onExtensionClick(v1);
                                });

                                binding.extensions.addView(layout);
                            }
                            stylized();

                            // 恢复页面
                            binding.clientConnect.setVisibility(View.GONE);
                            binding.clientInput.setVisibility(View.VISIBLE);
                            loaded();
                            adapter.addMessage("");
                            adapter.addMessage("");
                            adapter.addMessage("");
                        }))
                        .setOnCloseListener(this::close)
                        .setOnErrorListener(this::error)
                        .setOnReconnectListener(this::reconnect)
                        .setOnPrint(this::print)
                        .setOnJustPrint(this::justPrint)
                        .setOnModalFormRequest(formBuilder -> {
                            if (formBuilder instanceof SimpleFormBuilder simpleFormBuilder)
                                simpleFormBuilder.setDefaultImageLoader(new SimpleFormBuilder.ButtonImageLoader() {
                                    @Override
                                    public void loadUrl(String url, ImageView imageView) {
                                        Glide.with(ChatActivity.this)
                                                .load(url)
                                                .into(imageView);
                                    }
                                    @Override
                                    public void loadPath(String path, ImageView imageView) {
                                        Bitmap bitmap = BedrockResource.loadImage(path);
                                        if (bitmap == null) return;
                                        imageView.setImageDrawable(new BitmapDrawable(getResources(), bitmap));
                                    }
                                });
                            runOnUiThread(() -> formBuilder.show(this));
                        })
                        .setTitleController(titleController)
                        .setScoreboardController(scoreboardController)
                        .setPlayerListController(playerListController)
                )))).start()));
    }

    public void send(String input) {
        if (input.startsWith("/")) {
            adapter.addMessage(true, input);
            AChatHistory aChatHistory = new AChatHistory();
            aChatHistory.setServerId(aServer.id());
            aChatHistory.setMessage(input);
            aChatHistory.setMe(true);
            aChatHistory.setTimestamp(System.currentTimeMillis());
            Completable.fromAction(() -> chatHistoryDao.insert(aChatHistory))
                    .subscribeOn(Schedulers.io())
                    .subscribe();

            boolean consumed = false;
            for (ProtocolExtension extension : protocolExtensions)
                if (extension.onCommand(input.substring(1))) {
                    consumed = true;
                    break;
                }
            if (!consumed) client.executeCommand(input);
        } else client.sendMessage(input);
    }

    boolean isShowingPauseMenu = false;
    @Override
    public void onBackPressed() {
        if (appbarExpander.isExpanded) {
            appbarExpander.collapse();
            return;
        }
        if (client == null || isLoading) {
            super.onBackPressed();
            return;
        }

        if (isShowingPauseMenu) {
            ViewUtils.animateHide(binding.pauseMenu);
        } else {
            ViewUtils.animateShow(binding.pauseMenu);
        }
        isShowingPauseMenu = !isShowingPauseMenu;
    }

    // loading
    public boolean isLoading = false;
    public MessageAdapter loadingAdapter;
    public void loading(CharSequence progress) {
        if (!isLoading) {
            isLoading = true;
            binding.loadingImage.setVisibility(View.VISIBLE);
            binding.loadingContent.setVisibility(View.VISIBLE);
            ViewUtils.animateShow(binding.loading);
        }
        if (isFloating && floatingChatService != null)
            floatingChatService.print(progress);
        loadingAdapter.addMessage(progress);
        binding.buttonSend.setEnabled(false);
        binding.editText.setEnabled(false);
    }
    public void loaded() {
        isLoading = false;
        binding.loadingImage.setVisibility(View.GONE);
        binding.loadingContent.setVisibility(View.GONE);
        ViewUtils.animateHide(binding.loading, /*() -> isLoading = false*/null);
        binding.buttonSend.setEnabled(true);
        binding.editText.setEnabled(true);
    }

    boolean isFloating = false;
    private FloatingChatService floatingChatService;
    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            FloatingChatService.Binder binder = (FloatingChatService.Binder) service;
            floatingChatService = binder.getService();
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            floatingChatService = null;
        }
    };
    @Override
    protected void onPause() {
        super.onPause();
        if (client != null) {
            startService(new Intent(this, FloatingChatService.class));
            bindService(new Intent(this, FloatingChatService.class), serviceConnection, Context.BIND_AUTO_CREATE);
            isFloating = true;
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (isFloating) {
            stopService(new Intent(this, FloatingChatService.class));
            unbindService(serviceConnection);
            isFloating = false;
        }
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        autoReconnect = -1;
        for (ProtocolExtension extension : protocolExtensions)
            extension.onDisconnect();
        if (client != null) {
            client.close();
            client = null;
        }
        if (isFloating) {
            stopService(new Intent(this, FloatingChatService.class));
            unbindService(serviceConnection);
            isFloating = false;
        }
    }

    public void print(CharSequence message) {
        runOnUiThread(() -> {
            CharSequence mMessage = message;
            for (ProtocolExtension extension : protocolExtensions)
                mMessage = extension.onPrint(mMessage);

            justPrint(mMessage);
            if (isLoading) return;  //

            AChatHistory aChatHistory = new AChatHistory();
            aChatHistory.setServerId(aServer.id());
            aChatHistory.setMessage(String.valueOf(mMessage));
            aChatHistory.setMe(false);
            aChatHistory.setTimestamp(System.currentTimeMillis());
            Completable.fromAction(() -> chatHistoryDao.insert(aChatHistory))
                    .subscribeOn(Schedulers.io())
                    .subscribe();
        });
    }
    public void justPrint(CharSequence message) {
        runOnUiThread(() -> {
            if (isLoading) {
                loadingAdapter.addMessage(message);
                return;
            }
            if (isFloating)
                floatingChatService.print(message);
            adapter.addMessage(message);
        });
    }

    public void close(CharSequence reason) {
        String title = getString(R.string.activity_chat_close_dialog_title);
        CharSequence text = reason instanceof String ? MCFontWrapper.WRAPPER.wrap(reason) : reason;
        if (autoReconnect != -1) {
            autoReconnect(title, text);
            return;
        }

        runOnUiThread(() ->
                new DialogBuilder(this)
                        .setTitle(title)
                        .setContent(text)
                        .addAction(getString(R.string.activity_chat_close_dialog_action), v1 -> finish())
                        .setOnCancelListener(this::finish)
                        .show());
    }
    public void error(Throwable throwable) {
        String title = getString(R.string.activity_chat_error_dialog_title);
        String message = throwable.getMessage();
        if (message == null || message.isEmpty()) message = throwable.toString();
        CharSequence finalMessage = MCFontWrapper.WRAPPER.wrap(message);

        if (autoReconnect != -1) {
            autoReconnect(title, finalMessage);
            return;
        }

        runOnUiThread(() -> new DialogBuilder(this)
                .setTitle(title)
                .setContent(finalMessage)
                .addAction(getString(R.string.activity_chat_error_dialog_back), v1 -> finish())
                .addAction(getString(R.string.activity_chat_error_dialog_stack), v1 -> ExceptionHandler.printStackTrace(this, throwable, dialogBuilder ->
                        dialogBuilder.setOnCancelListener(this::finish)))
                        .setOnCancelListener(this::finish)
                .show());
    }
}