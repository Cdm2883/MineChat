package vip.cdms.minechat.protocol.plugin.builtin.protocol;


import static vip.cdms.mcoreui.util.MCTextParser.SS;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;
import vip.cdms.mcoreui.util.FontWrapper;
import vip.cdms.mcoreui.util.ImageUtils;
import vip.cdms.mcoreui.util.MCFontWrapper;
import vip.cdms.mcoreui.util.ResourcesUtils;
import vip.cdms.mcoreui.util.TimeUtils;
import vip.cdms.mcoreui.util.WebLinkWrapper;
import vip.cdms.mcoreui.view.dialog.CustomFormBuilder;
import vip.cdms.mcoreui.view.dialog.DialogBuilder;
import vip.cdms.mcoreui.view.dialog.FormBuilder;
import vip.cdms.mcoreui.view.dialog.SimpleFormBuilder;
import vip.cdms.mcoreui.view.button.TextButton;
import vip.cdms.minechat.protocol.dataexchange.client.Client;
import vip.cdms.minechat.protocol.dataexchange.client.Motd;
import vip.cdms.minechat.protocol.R;
import vip.cdms.minechat.protocol.dataexchange.client.SimpleClientRepeater;
import vip.cdms.minechat.protocol.dataexchange.client.SimpleClientRepeaterHelper;
import vip.cdms.minechat.protocol.dataexchange.bean.Account;
import vip.cdms.minechat.protocol.dataexchange.ui.PlayerListController;
import vip.cdms.minechat.protocol.dataexchange.ui.ScoreboardController;
import vip.cdms.minechat.protocol.dataexchange.ui.TitleController;
import vip.cdms.minechat.protocol.plugin.JLanguage;
import vip.cdms.minechat.protocol.plugin.ProtocolProvider;
import vip.cdms.minechat.protocol.util.AccountUtils;
import vip.cdms.minechat.protocol.util.ExceptionHandler;
import vip.cdms.minechat.protocol.util.motd.JEMotdChecker;
import vip.cdms.minechat.protocol.util.MojangAPI;
import vip.cdms.minechat.protocol.util.motd.MotdChecker;
import vip.cdms.minechat.protocol.util.OkHttpJsonCallback;

/**
 * [内置] 协议提供服务器插件
 * @author Cdm2883
 */
public class ProtocolServerPlugin extends ProtocolProvider {
    JLanguage lang = new JLanguage();
    public ProtocolServerPlugin(Activity activity, SharedPreferences sharedPreferences, ExceptionHandler exceptionHandler) {
        super(activity, sharedPreferences, exceptionHandler);
        lang.set(activity);
        setPluginIcon("iVBORw0KGgoAAAANSUhEUgAAAAoAAAAKCAYAAACNMs+9AAAAAXNSR0IArs4c6QAAAIZJREFUGJWtjzEOgjAYhT8siXPHLrLCSYiOnsAR49E8BgwdOpFeocs/tAfogIMBjWHAhJe87eW99xWnqprYoBJg6HuMMauBnDN103AAiDEiIosfzzMiQueueO8/jc45tNZLS3u8Y63lwo0xjQAUu38s549KqdVgCAHgDbN5+hfmWykl4A+YF9IKNxlnD1E5AAAAAElFTkSuQmCC");
        setPluginTitle(lang.add("Protocol Provider Servers", "协议提供服务器"));
        setPluginSummary(lang.add("[built-in]", "[内置]"));
    }

    @Override
    public void openPluginSetting() {
        String[] serverList = getSharedPreferences().getStringSet("server_list", new HashSet<>(List.of("127.0.0.1:8873"))).toArray(new String[0]);
        new CustomFormBuilder()
                .setTitle(lang.add("Configurations", "插件配置"))
                .addDropdown(lang.add("Default server", "默认协议提供服务器"), serverList, Arrays.asList(serverList).indexOf(getSharedPreferences().getString("server_default", "127.0.0.1:8873")))
                .addInput(lang.add("Global session attached options", "全局会话附加属性"), lang.add("Input JSON", "请输入JSON"), getSharedPreferences().getString("options_global", "{}"))
                .setSubmit(lang.add("Save", "保存"), null)
                .setCallback(callback -> {
                    if (callback.isCancel()) return;
                    SharedPreferences.Editor editor = getSharedPreferences().edit();
                    editor.putString("server_default", serverList[callback.responseData().get(0).getAsInt()]);
                    editor.putString("options_global", callback.responseData().get(1).getAsString());
                    editor.apply();
                })
                .build(getActivity())
                .addAction(lang.add("Manage Providers", "管理协议提供服务器"), TextButton.Style.PURPLE, true, v -> manageProviders())
                .show();
    }

    record ProtocolServerInfo(
            String description,
            String backend,
            int online,
            JsonArray bedrockProtocols,
            JsonArray javaProtocols
    ) {
        public ProtocolServerInfo(JsonObject body) {
            this(
                    body.get("description").getAsString(),
                    body.get("backend").getAsString(),
                    body.get("online").getAsInt(),
                    body.get("bedrock_protocols").getAsJsonArray(),
                    body.get("java_protocols").getAsJsonArray()
            );
        }
        public String toString(JLanguage lang) {
            return new StringJoiner("\n\n")
                    .add(lang.add("description", "描述") + ": " + description)
                    .add(lang.add("backend", "后端") + ": " + backend)
                    .add(lang.add("online", "在线") + ": " + online)
                    .add(lang.add("bedrock protocols", "基岩版协议") + ": " + bedrockProtocols)
                    .add(lang.add("java protocols", "Java版协议") + ": " + javaProtocols)
                    .toString();
        }

        public static void get(String host, int port, OkHttpClient client, ExceptionHandler.Handler exceptionHandler, Consumer<ProtocolServerInfo> onResponse) {
            Request request = new Request.Builder()
                    .url("http://" + host + ":" + port + "/info")
                    .get()
                    .build();
            client.newCall(request).enqueue(new OkHttpJsonCallback<JsonObject>()
                    .setOnFailure(exceptionHandler::getCaught)
                    .setOnResponse(body -> onResponse.accept(new ProtocolServerInfo(body))));
        }
    }

    void manageProviders() {
        String[] serverList = getSharedPreferences().getStringSet("server_list", new HashSet<>(List.of("127.0.0.1:8873"))).toArray(new String[0]);
        SimpleFormBuilder simpleFormBuilder = new SimpleFormBuilder()
                .setTitle(lang.add("Manage Providers", "管理协议提供服务器"))
                .setContent(serverList.length == 0 ? lang.add("Empty", "什么都木有") : lang.add("Click to view or edit", "单击以查看或编辑"))
                .setCallback(callback -> {
                    if (callback.isCancel()) {
                        openPluginSetting();
                        return;
                    }

                    int index = callback.responseData().getAsInt();
                    String[] address = serverList[index].split(":");
                    String host = address[0];
                    int port = Integer.parseInt(address[1]);

                    CustomFormBuilder editForm = new CustomFormBuilder()
                            .setTitle(lang.add("View & Edit", "查看和编辑服务器"))
                            .setCallback(editCallback -> {
                                if (editCallback.isCancel()) {
                                    manageProviders();
                                    return;
                                }
                                String addressNew = editCallback.responseData().get(1).getAsString() + ":" + editCallback.responseData().get(2).getAsString();
                                serverList[index] = addressNew;
                                ArrayList<String> serverListNew = new ArrayList<>();
                                for (String addressEdited : serverList) {
                                    if (addressEdited == null || addressEdited.isEmpty() || addressEdited.equals(":")) continue;
                                    serverListNew.add(addressEdited);
                                }
                                SharedPreferences.Editor editor = getSharedPreferences().edit();
                                editor.putStringSet("server_list", new HashSet<>(serverListNew));
                                editor.apply();
                                manageProviders();
                            });

                    Runnable show = () -> runOnUiThread(() -> editForm.addInput(lang.add("Host", "地址"), host, host)
                            .addInput(lang.add("Port", "端口"), String.valueOf(port), String.valueOf(port))
                            .show(getActivity()));
                    OkHttpClient client = new OkHttpClient.Builder().build();

                    ExceptionHandler.Handler exceptionHandler = e -> {
                        editForm.addLabel(lang.add("Request Fail", "加载服务器信息失败") + ": " + e.toString());
                        show.run();
                    };
                    getExceptionHandler().handDisposable(Exception.class, exceptionHandler);
                    tryCatch(() ->
                            ProtocolServerInfo.get(host, port, client, exceptionHandler, info -> {
                                editForm.addLabel(info.toString(lang) + '\n');
                                show.run();
                            })
                    );
                });
        for (var address : serverList)
            simpleFormBuilder.addButton(address);
        simpleFormBuilder.build(getActivity())
                .addAction(lang.add("Add...", "添加..."), v -> new CustomFormBuilder()
                        .setTitle(lang.add("Add a server", "添加服务器"))
                        .addInput(lang.add("Host", "地址"), "127.0.0.1", null)
                        .addInput(lang.add("Port", "端口"), "8873", "8873")
                        .setCallback(editCallback -> {
                            if (editCallback.isCancel()) {
                                manageProviders();
                                return;
                            }
                            String host = editCallback.responseData().get(0).getAsString();
                            if (host.isEmpty()) host = "127.0.0.1";
                            int port = Integer.parseInt(editCallback.responseData().get(1).getAsString());
                            String address = host + ":" + port;
                            ArrayList<String> serverListNew = new ArrayList<>(Arrays.asList(serverList));
                            serverListNew.add(address);
                            SharedPreferences.Editor editor = getSharedPreferences().edit();
                            editor.putStringSet("server_list", new HashSet<>(serverListNew));
                            editor.apply();
                            manageProviders();
                        })
                        .show(getActivity()))
                .show();
    }

    /** @noinspection DataFlowIssue*/
    @Override
    public void createOrEdit(@Nullable ServerConfig config, Consumer<ServerConfig> configCallback) {
        String[] protocolServers = new ArrayList<String>(){{
            add(lang.add("Default server", "默认协议提供服务器").toString());
            addAll(getSharedPreferences().getStringSet("server_list", new HashSet<>(List.of("127.0.0.1:8873"))));
        }}.toArray(new String[0]);

        AccountUtils.AccountSelector accountSelector = new AccountUtils.AccountSelector(lang, config);

        String displayName = ror(() -> config.get("name").getAsString(), "");
        String host = ror(() -> config.host(), "");
        String port = ror(() -> String.valueOf(config.port()), "");
        JsonObject sessionAttachedOptions = ror(() -> config.get("options").getAsJsonObject(), new JsonObject());

        new CustomFormBuilder()
                .setTitle(config == null ? lang.add("Add a server", "添加服务器") : lang.add("Edit the server", "编辑服务器"))
                .addDropdown(lang.add("Protocol provider server", "协议提供服务器"), protocolServers, ((Supplier<Integer>) () -> {
                    if (config == null) return 0;

                    JsonElement protocolServer = config.get("protocol_server");
                    if (protocolServer == null || protocolServer instanceof JsonNull) return 0;
                    else return Math.abs(Arrays.binarySearch(protocolServers, protocolServer.getAsString()));
                }).get())
                .processing(accountSelector)
                .addInput(lang.add("Display name", "显示名称"), "Minecraft Server", displayName)
                .addInput(lang.add("Host", "地址"), "127.0.0.1", host)
                .addInput(lang.add("Port", "端口"), "19132", port)
                .addInput(lang.add("Session attached options", "会话附加属性"), lang.add("Input JSON", "请输入JSON"), new GsonBuilder().setPrettyPrinting().create().toJson(sessionAttachedOptions))
                .setCallback(callback -> tryCatch(() -> {
                    if (callback.isCancel()) return;
                    JsonArray response = callback.responseData();

                    int protocolServerIndex = response.get(0).getAsInt();
                    String mName = response.get(2).getAsString();
                    if (mName.isEmpty()) mName = "Minecraft Server";
                    String mHost = response.get(3).getAsString();
                    if (mHost.isEmpty()) mHost = "127.0.0.1";
                    String mPort = response.get(4).getAsString();
                    if (mPort.isEmpty()) mPort = "19132";

                    ServerConfig edited = new ServerConfig(mHost, Integer.parseInt(mPort));
                    accountSelector.save(callback, edited);
                    edited.add("protocol_server", protocolServerIndex == 0 ? JsonNull.INSTANCE : new JsonPrimitive(protocolServers[protocolServerIndex]));
                    edited.add("name", mName);
                    edited.add("options", JsonParser.parseString(response.get(5).getAsString()));
                    configCallback.accept(edited);
                }))
                .show(getActivity());
    }

    @Override
    public void motd(ServerConfig config, MotdCallback motdCallback) {
        motdCallback.title(MCFontWrapper.WRAPPER.wrap(config.get("name").getAsString()));
        String host = config.host();
        int port = config.port();

        Motd motd = MotdChecker.get(host, port);
        if (motd != null) {
            motdCallback.summary(MCFontWrapper.WRAPPER.wrap(motd.motd()+ " - " + motd.version()));
            motdCallback.topText(motd.onlinePlayer() + "/" + motd.maxPlayer());
            motdCallback.topIcon(MotdChecker.delayIcon(getActivity(), motd.delay()));
            motdCallback.icon(motd instanceof JEMotdChecker.JEMotd jeMotd ?
                    ImageUtils.base64ToDrawable(getActivity(), jeMotd.favicon()) :
                    ResourcesUtils.getPixelDrawable(getActivity(), R.drawable.bedrock));
            return;
        }

        // req motd from protocol server
        String[] address = getProtocolServerAddress(config);
        Request requestMotd = new Request.Builder()
                .url("http://" + address[0] + ":" + address[1] + "/motd?host=" + host + "&port=" + port)
                .get()
                .build();
        OkHttpClient okHttpClient = new OkHttpClient();
        okHttpClient.newCall(requestMotd).enqueue(new OkHttpJsonCallback<JsonObject>()
                .setOnFailure(e -> {
                    motdCallback.summary(MCFontWrapper.WRAPPER.wrap(SS + "4Timed out"));
                })
                .setOnResponse(body -> {
                    motdCallback.summary(MCFontWrapper.WRAPPER.wrap(body.get("summary").getAsString()));
                    motdCallback.topText(body.get("count").getAsString());
                    motdCallback.topIcon(MotdChecker.delayIcon(getActivity(), body.get("delay").getAsInt()));
                    motdCallback.icon(ExceptionHandler.ror(() ->
                            ImageUtils.base64ToDrawable(getActivity(), body.get("icon").getAsString()),
                            (Drawable) null));
                }));
    }

    public class BaseBackend {
        final String name;
        public BaseBackend(String name) {
            this.name = name;
        }

        String closeReason;
        public void setCloseReason(String closeReason) {
            this.closeReason = closeReason;
        }

        WebSocket webSocket;
        public WebSocket getWebSocket() {
            return webSocket;
        }
        public void send(String type, JsonElement data) {
            if (webSocket == null) return;
            JsonObject packet = new JsonObject();
            packet.addProperty("type", type);
            packet.add("data", data == null ? JsonNull.INSTANCE : data);
            webSocket.send(new Gson().toJson(packet));
        }
        public void send(String type, String data) {
            send(type, ExceptionHandler.ror(
                    (ExceptionHandler.AutoTryCatchResult<JsonElement>) () -> JsonParser.parseString(data),
                    (ExceptionHandler.AutoTryCatchResult<JsonElement>) () -> new JsonPrimitive(data)
            ));
        }

        public void onOpen() {}
        public boolean onMessage(JsonObject body, SimpleClientRepeaterHelper clientHelper) {
            JsonElement data = body.get("data");
            switch (body.get("type").getAsString()) {
                case "waiting-connection" -> {
                    clientHelper.connectingProgress("| " + lang.add("Queuing", "正在排队") + "...");
                    TimeUtils.setTimeout(() -> send("connect", (JsonElement) null), 2000);
                    return true;
                }
                case "close" -> {
                    setCloseReason(data.getAsString());
                    return true;
                }
                case "print" -> {
                    clientHelper.print(data.getAsString());
                    return true;
                }
            }

            return false;
        }
    }

    public class OfficialBedrockBackend extends BaseBackend {
        public static final String NAME = "PrismarineJS/bedrock-protocol";
        Pattern msaLogin = Pattern.compile("^To sign in, use a web browser to open the page (.+) and enter the code (.+) to authenticate\\.$");
        public OfficialBedrockBackend() {
            super(NAME);
        }

        @Override
        public void onOpen() {
            super.onOpen();
            send("on", "join");
            send("on", "spawn");
            send("on", "start_game");

            send("on", "modal_form_request");
            send("on", "set_display_objective");
            send("on", "remove_objective");
            send("on", "set_score");

            send("on", "player_list");
            send("on", "set_title");
            send("on", "toast_request");
            send("on", "text");
        }

        boolean joined = false;
        long lastChangeScoreboard = 0;
        long now;
        final HashMap<String, String> uuid2entityUniqueId = new HashMap<>();
        /** @noinspection DataFlowIssue*/
        @Override
        public boolean onMessage(JsonObject body, SimpleClientRepeaterHelper clientHelper) {
            JsonElement data = body.get("data");
            {
                String type = body.get("type").getAsString();
                Matcher matcher;
                if (!joined && type.equals("print") && (matcher = msaLogin.matcher(data.getAsString())).find()) {
                    clientHelper.requestModalForm(new CustomFormBuilder()
                            .setTitle(lang.add("Sign in to Xbox", "登录微软账户"))
                            .addInput(lang.add("1. Use a web browser to open the page", "1. 使用浏览器打开网页"), matcher.group(1), matcher.group(1))
                            .addInput(lang.add("2. Enter the code:", "2. 输入代码:"), matcher.group(2), matcher.group(2))
                            .setSubmit(lang.add("Reconnect", "重新连接"), null)
                            .setCallback(FormBuilder.betterCallback(null, callback -> {
                                clientHelper.reconnect(null, -1);
                            })));
                    return super.onMessage(body, clientHelper);
                }
                if (super.onMessage(body, clientHelper)) return true;
                if (!type.equals("event")) return false;
            }

            TitleController titleController = clientHelper.getTitleController();
            ScoreboardController scoreboardController = clientHelper.getScoreboardController();
            PlayerListController playerListController = clientHelper.getPlayerListController();

            JsonObject objectData = data != null && data.isJsonObject() ? data.getAsJsonObject() : null;

            switch (body.get("event").getAsString()) {
                case "join" -> {
                    joined = true;
                    clientHelper.connectingProgress("| " + lang.add("Joined to the server", "已加入服务器")).connected();
                    return true;
                }
                case "spawn" -> {
                    clientHelper.justPrint(lang.add("The world has been spawn", "世界已生成").toString());
                    return true;
                }
                case "start_game" -> {
                    playerListController.setTitle(objectData.get("world_name"/*level_id*/).getAsString());
                    return true;
                }
                case "modal_form_request" -> { runOnUiThread(() ->
                    clientHelper.requestModalForm(
                            FormBuilder.create(JsonParser.parseString(objectData.get("data").getAsString()).getAsJsonObject(), null)
                                    .setFontWrapper(MCFontWrapper.WRAPPER)
                                    .setCallback(callback -> {
                                        JsonObject packet = new JsonObject();
                                        packet.addProperty("name", "modal_form_response");
                                        packet.add("params", callback.toJson(objectData.get("form_id").getAsInt()));
                                        send("queue", packet);
                                    })));
                    return true;
                }
                case "set_display_objective" -> {
                    try {
                        String displaySlot = objectData.get("display_slot").getAsString();
                        String board = objectData.get("objective_name").getAsString();
                        String displayName = objectData.get("display_name").getAsString();
                        if (displaySlot.equals("sidebar"))
                            scoreboardController.show(
                                    ScoreboardController.DisplaySlot.sidebar,
                                    board,
                                    displayName
                            );
                        else if (displaySlot.equals("list"))
                            scoreboardController.show(
                                    ScoreboardController.DisplaySlot.list,
                                    board,
                                    displayName
                            );
                    } catch (Exception ignored) {}
                    return true;
                }
                case "remove_objective" -> {
                    scoreboardController.clear(objectData.get("objective_name").getAsString());
                    return true;
                }
                case "set_score" -> {
                    now = System.currentTimeMillis();
                    if (now - lastChangeScoreboard < 1000)
                        break;
                    lastChangeScoreboard = now;
                    String action = objectData.get("action").getAsString();
                    HashMap<String, ArrayList<ScoreboardController.AScore>> boards = new HashMap<>();
                    JsonArray entries = objectData.get("entries").getAsJsonArray();
                    for (var jsonElement : entries) {
                        if (!(jsonElement instanceof JsonObject entry))
                            continue;
                        String scoreboardId = entry.get("scoreboard_id").getAsString();
                        String board = entry.get("objective_name").getAsString();
                        int score = entry.get("score").getAsInt();

                        if (action.equals("remove")) {
                            scoreboardController.remove(board, scoreboardId);
                            continue;
                        }

                        if (!action.equals("change")) continue;
                        ArrayList<ScoreboardController.AScore> scores = boards.get(board);
                        if (scores == null) scores = new ArrayList<>();

                        String entryType = entry.get("entry_type").getAsString();
                        if (entryType.equals("fake_player"))
                            scores.add(new ScoreboardController.AScore(scoreboardId, entry.get("custom_name").getAsString(), score));
                        else if (entryType.equals("player") || entryType.equals("entity"))
                            scores.add(new ScoreboardController.AScore(scoreboardId, entry.get("entity_unique_id").getAsString(), score));

                        boards.put(board, scores);
                    }
                    if (action.equals("change"))
                        boards.forEach((board, scores) -> scoreboardController.insert(
                                board,
                                -1,
                                scores.toArray(new ScoreboardController.AScore[0])
                        ));
                    return true;
                }
                case "player_list" -> {
                    String type = objectData.get("records").getAsJsonObject().get("type").getAsString();
                    JsonArray records = objectData.get("records").getAsJsonObject().get("records").getAsJsonArray();
                    for (JsonElement record : records)
                        switch (type) {
                            case "add" -> {
                                JsonObject mRecord = record.getAsJsonObject();

                                String username = mRecord.get("username").getAsString();
                                String xboxUserId = mRecord.get("xbox_user_id").getAsString();
                                String entityUniqueId = mRecord.get("entity_unique_id").getAsString();
                                uuid2entityUniqueId.put(mRecord.get("uuid").getAsString(), entityUniqueId);

                                Consumer<Consumer<Drawable>> avatar = drawableConsumer -> drawableConsumer.accept(null);
                                try {
                                    avatar = xboxUserId.isEmpty() || Long.parseLong(xboxUserId) < 0 ?
                                            avatarConsumer -> avatarConsumer.accept(ResourcesUtils.getPixelDrawable(getActivity(), R.drawable.bot))
                                            :
                                            avatarConsumer ->
                                                    MojangAPI.getAvatar(getActivity(), username, bitmap ->
                                                            avatarConsumer.accept(bitmap == null ? null : new BitmapDrawable(getActivity().getResources(), bitmap)));
                                } catch (NumberFormatException ignored) {
                                }

                                playerListController.add(
                                        entityUniqueId,
                                        username,
                                        avatar
                                );
                            }
                            case "remove" -> {
                                String uuid = record.getAsJsonObject().get("uuid").getAsString();
                                playerListController.remove(uuid2entityUniqueId.get(uuid));
                                uuid2entityUniqueId.remove(uuid);
                            }
                        }
                    return true;
                }
                case "set_title" -> {
                    String text = objectData.get("text").getAsString();
                    switch (objectData.get("type").getAsString()) {
                        case "set_title" -> titleController.title(text);
                        case "set_subtitle" -> titleController.subtitle(text);
                        case "action_bar_message" -> titleController.actionbar(text);
                        case "set_durations" -> {
                            titleController.setFadeInTime(objectData.get("fade_in_time").getAsInt());
                            titleController.setStayTime(objectData.get("stay_time").getAsInt());
                            titleController.setFadeOutTime(objectData.get("fade_out_time").getAsInt());
                        }
                    }
                    return true;
                }
                case "toast_request" -> {
                    titleController.toast(
                            objectData.get("title").getAsString(),
                            objectData.get("message").getAsString()
                    );
                    return true;
                }
                case "text" -> {
                    switch (objectData.get("type").getAsString()) {
                        case "tip" -> {
                            titleController.actionbar(objectData.get("message").getAsString());
                            return true;
                        }
                        case "jukebox_popup" -> {
                            titleController.popup(objectData.get("message").getAsString());
                            return true;
                        }
                        default -> {
                            System.out.println(" ");
                            System.out.println(data);
                        }
                    }
                }
            }

            return false;
        }
    }

    boolean closed = false;
    boolean isConnected = false;
    BaseBackend backend;
    public BaseBackend getBackend() {
        return backend;
    }
    public void setBackend(BaseBackend backend) {
        this.backend = backend;
    }

    private String[] getProtocolServerAddress(ServerConfig config) {
        JsonElement protocolServerJson = config.get("protocol_server");
        String protocolServer;
        if (protocolServerJson == null || protocolServerJson instanceof JsonNull) protocolServer = getSharedPreferences().getString("server_default", "127.0.0.1:8873");
        else protocolServer = protocolServerJson.getAsString();
        return protocolServer.split(":");
    }
    @Override
    public void connect(ServerConfig config, Consumer<Client<?>> clientCallback) {
        // options
        Account account = AccountUtils.AccountSelector.getAccount(config);

        String[] address = getProtocolServerAddress(config);
        String host = address[0];
        int port = Integer.parseInt(address[1]);

        String mcHost = config.host();
        int mcPort = config.port();
        JsonObject attachedOptions = config.get("options") == null ? new JsonObject() : config.get("options").getAsJsonObject();
        JsonObject globalAttachedOptions = JsonParser.parseString(getSharedPreferences().getString("options_global", "{}")).getAsJsonObject();

        // okhttp
        OkHttpClient okHttpClient = new OkHttpClient.Builder().build();

        // client
        Map<String, ArrayList<Consumer<Object>>> events = new HashMap<>();

        SimpleClientRepeater simpleClient = new SimpleClientRepeater();
        final SimpleClientRepeaterHelper clientHelper = simpleClient.helper()
                .setOnMoreClickListener(v -> tryCatch(() ->
                        ProtocolServerInfo.get(host, port, okHttpClient,
                                e -> runOnUiThread(() -> new DialogBuilder(getActivity())
                                        .setTitle(getPluginTitle())
                                        .setContent(ExceptionHandler.printStackTrace(e), false)
                                        .show()),
                                info -> runOnUiThread(() -> new DialogBuilder(getActivity())
                                        .setTitle(getPluginTitle())
                                        .setContent(info.toString(lang), false)
                                        .show())
                )))
                .setOnMessageListener(message ->
                        backend.send("chat", message))
                .setOnCommandListener(command ->
                        backend.send("command", command))
                .setOnCloseListener(() -> {
                    if (!isConnected) return;
                    isConnected = false;
                    if (backend != null && backend.webSocket != null) {
//                        backend.webSocket.cancel();
                        backend.webSocket.close(1000, "");
                        backend.webSocket = null;
                    }
                })
                .setOnPacketSendListener((name, obj) -> {
                    if (obj instanceof JsonElement data) {
                        backend.send(name, data);
                    } else if (obj instanceof String data) {
                        backend.send(name, data);
                    }
                })
                .setOnEventAddListener((name, callback) -> {
                    ArrayList<Consumer<Object>> nameEvents = events.get(name);
                    if (nameEvents == null) nameEvents = new ArrayList<>();
                    nameEvents.add(callback);
                    events.put(name, nameEvents);
                    if (isConnected) backend.send("on", name);
                });
        clientCallback.accept(simpleClient);

        clientHelper.connectingProgress("- " + lang.add("Checking protocol provider server status", "正在检查协议提供服务器状态")+ "... " + host + ":" + port);
        Request requestInfoCheck = new Request.Builder()
                .url("http://" + host + ":" + port + "/info")
                .get()
                .build();
        okHttpClient.newCall(requestInfoCheck).enqueue(new OkHttpJsonCallback<JsonObject>()
                .setOnFailure(e -> clientHelper.close(lang.add("Failed to get protocol provider server status", "获取协议提供服务器状态失败") + "! \n" + e.getMessage()))
                .setOnResponse(infoBody -> {
                    String backendString = infoBody.get("backend").getAsString();
                    clientHelper.connectingProgress("| " + lang.add("description", "描述") + ": " + infoBody.get("description").getAsString())
                            .connectingProgress("| " + lang.add("backend", "后端") + ": " + backendString)
                            .connectingProgress("| " + lang.add("online", "在线") + ": " + infoBody.get("online").getAsInt())
                            .connectingProgress("- " + lang.add("Requesting a session from the protocol provider server", "正在从协议提供服务器申请会话") + "...");

                    if (backend == null) backend = switch (backendString) {
                        case OfficialBedrockBackend.NAME -> new OfficialBedrockBackend();
                        default -> new BaseBackend(backendString);
                    };

                    //noinspection TrivialFunctionalExpressionUsage
                    Request requestCreateSession = new Request.Builder()
                            .url("http://" + host + ":" + port + "/session")
                            .post(RequestBody.Companion.create(((Supplier<String>) () -> {
                                JsonObject body = new JsonObject();
                                body.addProperty("host", mcHost);
                                body.addProperty("port", mcPort);
                                if (account.isOnline() && OfficialBedrockBackend.NAME.equals(backendString)) {
                                    // 保护用户账号安全
                                    String onlineType = getSharedPreferences().getString("online_" + account.username(), null);
                                    if (onlineType == null) {
                                        onlineType = UUID.randomUUID().toString();
                                        SharedPreferences.Editor editor = getSharedPreferences().edit();
                                        editor.putString("online_" + account.username(), onlineType);
                                        editor.apply();
                                    }
                                    body.addProperty("username", onlineType);
                                    body.addProperty("offline", false);
                                } else {
                                    body.addProperty("username", account.username());
                                    body.addProperty("offline", !account.isOnline());
                                }

                                for (var entry : globalAttachedOptions.entrySet()) body.add(entry.getKey(), entry.getValue());
                                for (var entry : attachedOptions.entrySet()) body.add(entry.getKey(), entry.getValue());

                                return new Gson().toJson(body);
                            }).get(), MediaType.Companion.parse("application/json")))
                            .build();
                    okHttpClient.newCall(requestCreateSession).enqueue(new OkHttpJsonCallback<JsonObject>()
                            .setOnFailure(e -> clientHelper.close(lang.add("Requesting a session from the protocol provider server failed", "从协议提供服务器申请会话失败") + "! " + e.getMessage()))
                            .setOnResponse(sessionBody -> {
                                String sessionId = sessionBody.get("data").getAsJsonObject().get("session_id").getAsString();
                                clientHelper.connectingProgress("| " + sessionId)
                                        .connectingProgress("- " + lang.add("Begin to connect the protocol provider server", "正在与协议提供服务器建立连接") + "...");
                                backend.webSocket = okHttpClient.newWebSocket(new Request.Builder()
                                        .url("ws://" + host + ":" + port + "/session/" + sessionId)
                                        .build(), new WebSocketListener() {
                                    @Override
                                    public void onClosed(@NonNull WebSocket webSocket, int code, @NonNull String reason) {
                                        super.onClosed(webSocket, code, reason);
                                        if (!isConnected) return;
                                        isConnected = false;
                                        if (reason.isEmpty()) reason = backend.closeReason;
                                        clientHelper.close(lang.add("The Websocket connection is closed", "Websocket连接关闭")+ "\n\nCODE: "+ code + "\n" + reason);
                                    }
                                    @Override
                                    public void onClosing(@NonNull WebSocket webSocket, int code, @NonNull String reason) {
                                        super.onClosing(webSocket, code, reason);
                                        if (!isConnected) return;
                                        isConnected = false;
                                        if (reason.isEmpty()) reason = backend.closeReason;
                                        clientHelper.close(lang.add("The Websocket connection is closed", "Websocket连接关闭")+ "\n\nCODE: "+ code + "\n" + reason);
                                    }
                                    @Override
                                    public void onFailure(@NonNull WebSocket webSocket, @NonNull Throwable t, @Nullable Response response) {
                                        super.onFailure(webSocket, t, response);
                                        isConnected = false;
                                        clientHelper.error(t);
                                    }

                                    @Override
                                    public void onMessage(@NonNull WebSocket webSocket, @NonNull String text) {
                                        super.onMessage(webSocket, text);
                                        if (getActivity().isFinishing()) return;

                                        JsonObject body = JsonParser.parseString(text).getAsJsonObject();
                                        JsonElement data = body.get("data");
                                        backend.onMessage(body, clientHelper);

                                        if (body.get("type").getAsString().equals("event")) events.forEach((name, consumers) -> {
                                            if (!body.get("event").getAsString().equals(name)) return;
                                            consumers.forEach(consumer -> consumer.accept(data));
                                        });
                                    }
                                    @Override
                                    public void onMessage(@NonNull WebSocket webSocket, @NonNull ByteString bytes) {
                                        super.onMessage(webSocket, bytes);
                                    }
                                    @Override
                                    public void onOpen(@NonNull WebSocket webSocket, @NonNull Response response) {
                                        super.onOpen(webSocket, response);
                                        clientHelper.connectingProgress("- " + lang.add("Binding events", "正在绑定事件") + "...");

                                        isConnected = true;
                                        backend.onOpen();

                                        for (var name : events.keySet()) backend.send("on", name);
                                        clientHelper.connectingProgress("- " + lang.add("Requesting to open the MC server connection", "正在请求开启MC服务器连接") + "...");
                                        backend.send("connect", (JsonElement) null);
                                    }
                                });
                            }));
                }));
    }
}
