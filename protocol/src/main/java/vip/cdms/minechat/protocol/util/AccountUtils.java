package vip.cdms.minechat.protocol.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import vip.cdms.mcoreui.view.dialog.CustomFormBuilder;
import vip.cdms.mcoreui.view.dialog.FormBuilder;
import vip.cdms.minechat.protocol.app.Accounts;
import vip.cdms.minechat.protocol.dataexchange.bean.Account;
import vip.cdms.minechat.protocol.plugin.JLanguage;
import vip.cdms.minechat.protocol.plugin.ProtocolProvider;

public class AccountUtils {
    public static final String LANG_TITLE_ZH = "连接账户";
    public static final String LANG_TITLE_EN = "Account";
    public static final String LANG_DEFAULT_ACCOUNT_ZH = "默认账户";
    public static final String LANG_DEFAULT_ACCOUNT_EN = "Default account";
    public static final String LANG_NONE_ACCOUNT_ZH = "添加账户后才可以连接服务器!";
    public static final String LANG_NONE_ACCOUNT_EN = "Add an account before you connect to the server!";
    public static final String DEFAULT_STORAGE_KEY = "account";
    public record AccountSelector(
            CharSequence title,
            CharSequence defaultAccount,
            CharSequence noneAccount,
            ProtocolProvider.ServerConfig config,
            String storageKey,
            AtomicInteger _index
    ) implements FormBuilder.Processor<CustomFormBuilder> {
        public AccountSelector(CharSequence title, CharSequence defaultAccount, CharSequence noneAccount, ProtocolProvider.ServerConfig config, String storageKey) {
            this(title, defaultAccount, noneAccount, config, storageKey, new AtomicInteger(0));
        }
        public AccountSelector(JLanguage lang, ProtocolProvider.ServerConfig config, String storageKey) {
            this(lang.add(AccountUtils.LANG_TITLE_EN, AccountUtils.LANG_TITLE_ZH),
                    lang.add(AccountUtils.LANG_DEFAULT_ACCOUNT_EN, AccountUtils.LANG_DEFAULT_ACCOUNT_ZH),
                    lang.add(AccountUtils.LANG_NONE_ACCOUNT_EN, AccountUtils.LANG_NONE_ACCOUNT_ZH),
                    config, storageKey);
        }
        public AccountSelector(JLanguage lang, ProtocolProvider.ServerConfig config) {
            this(lang, config, DEFAULT_STORAGE_KEY);
        }

        @Override
        public CustomFormBuilder processing(CustomFormBuilder builder) {
            Account[] accounts = Accounts.INSTANCE.getAccounts();
            ArrayList<CharSequence> items = new ArrayList<>();
            if (accounts.length != 0) items.add(defaultAccount);

            int index = accounts.length == 0 ? -1 : 0;
            String id = config != null && config.get(storageKey) instanceof JsonPrimitive primitive && primitive.isString() ? primitive.getAsString() : null;
            for (int i = 0; i < accounts.length; i++) {
                Account account = accounts[i];
                items.add(account.title());
                if (Objects.equals(account.id(), id)) index = i + 1;
            }

            return ExceptionHandler.processing(
                    builder.addDropdown(title, items.toArray(new CharSequence[0]), index),
                    target -> {
                        _index.set(target.getElements().size() - 1);
                        return null;
                    }
            );
        }

        public void save(FormBuilder.FormCallback<JsonArray> callback, ProtocolProvider.ServerConfig edited) {
            int index = callback.responseData().get(_index.get()).getAsInt();
            if (index == -1)
                throw new IllegalArgumentException(String.valueOf(noneAccount));
            edited.add(storageKey, index == 0 ? JsonNull.INSTANCE : new JsonPrimitive(Accounts.INSTANCE.getAccounts()[index - 1].id()));
        }

        public static Account getAccount(ProtocolProvider.ServerConfig config, String storageKey) {
            String id = ExceptionHandler.ror(() -> config.get(storageKey).getAsString(), (String) null);
            return Accounts.INSTANCE.getAccount(id);
        }
        public static Account getAccount(ProtocolProvider.ServerConfig config) {
            return getAccount(config, DEFAULT_STORAGE_KEY);
        }
    }
}
