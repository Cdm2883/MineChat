package vip.cdms.minechat;

import androidx.annotation.NonNull;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Objects;
import java.util.stream.IntStream;

import vip.cdms.mcoreui.util.StringUtils;
import vip.cdms.minechat.plugin.PluginManager;
import vip.cdms.minechat.protocol.app.Accounts;
import vip.cdms.minechat.protocol.app.Plugins;
import vip.cdms.minechat.protocol.app.Storage;
import vip.cdms.minechat.protocol.dataexchange.bean.Account;
import vip.cdms.minechat.protocol.plugin.Plugin;
import vip.cdms.minechat.protocol.util.GsonUtils;
import vip.cdms.minechat.protocol.util.StorageUtils;

public class ProtocolInitializer {
    public static void init(MineChatApplication context) {
        Storage.FILES_DIR = context.getFilesDir();
        Storage.EXTERNAL_FILES = context.getExternalFilesDir(null);
        Storage.CACHE_DIR = context.getCacheDir();
        Storage.EXTERNAL_CACHE = context.getExternalCacheDir();

        Accounts.INSTANCE = new AccountsImpl();
        Plugins.INSTANCE = new PluginsImpl();
    }
}

class AccountsImpl extends Accounts {
    Account defaultAccount;
    ArrayList<Account> accounts = new ArrayList<>();
    void load() {
        defaultAccount = null;
        accounts.clear();

        JsonObject json = StorageUtils.read(Storage.FILES_DIR, "accounts", JsonObject.class);
        if (json == null) return;

        JsonArray array = json.getAsJsonArray("accounts");
        if (array == null || array.isEmpty()) return;

        String defaultId = json.get("default") instanceof JsonPrimitive primitive ? primitive.getAsString() : null;

        for (JsonElement element : array) {
            Account account = GsonUtils.GSON.fromJson(element, Account.class);
            if (Objects.equals(account.id(), defaultId)) defaultAccount = account;
            accounts.add(account);
        }
        if (defaultAccount == null) defaultAccount = accounts.get(0);
    }
    void save() {
        if (defaultAccount == null || accounts.isEmpty()) return;

        JsonObject json = new JsonObject();
        json.addProperty("default", defaultAccount.id());

        JsonArray array = new JsonArray();
        for (Account account : accounts)
            array.add(GsonUtils.GSON.toJsonTree(account));
        json.add("accounts", array);

        StorageUtils.write(Storage.FILES_DIR, "accounts", json);
    }

    AccountsImpl() {
        load();
    }

    @Override
    public Account[] getAccounts() {
        return accounts.toArray(new Account[0]);
    }

    @Override
    public Account getAccount(String id) {
        if (id == null) return defaultAccount;

        for (Account account : accounts)
            if (id.equals(account.id())) return account;
        return null;
    }

    @Override
    public void insertAccount(@NonNull Account account) {
        int index;
        if (account.id() == null || (index = IntStream.range(0, accounts.size())
                .filter(i -> account.id().equals(accounts.get(i).id()))
                .findFirst()
                .orElse(-1)) == -1) {
            Account added = account.id() == null ?
                    new Account(StringUtils.randomUUID(), account.title(), account.username(), account.isOnline())
                    : account;
            if (accounts.isEmpty()) defaultAccount = added;
            accounts.add(added);
            save();
            return;
        }

        accounts.set(index, account);
        save();
    }

    @Override
    public void setDefault(Account account) {
        insertAccount(account);
        defaultAccount = account;
        save();
    }

    @Override
    public void deleteAccount(@NonNull String id) {
        Iterator<Account> iterator = accounts.iterator();
        while (iterator.hasNext()) {
            Account account = iterator.next();
            if (account.id().equals(id)) {
                iterator.remove();
                if (account.id().equals(defaultAccount.id()))
                    defaultAccount = accounts.isEmpty() ? null : accounts.get(0);
                break;
            }
        }

        save();
    }
}

class PluginsImpl extends Plugins {
    @Override
    public boolean isEnabled(Class<? extends Plugin> clazz) {
        PluginManager.PluginConfig pluginConfig = PluginManager.getPlugin(clazz);
        return pluginConfig != null && pluginConfig.isEnabled();
    }
}