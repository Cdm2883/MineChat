package vip.cdms.minechat.protocol.app;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import vip.cdms.minechat.protocol.dataexchange.bean.Account;

public abstract class Accounts {
    public static Accounts INSTANCE;

    /**
     * 获取所有账户
     * @return 所有账户
     */
    public abstract Account[] getAccounts();
    /**
     * 获取指定ID的账户或默认账户
     * @param id 账户ID, null则返回默认账户
     * @return 账户
     */
    public abstract Account getAccount(@Nullable String id);
    /**
     * 插入账户 (如果传入account的id为null或找不到id则添加, 否则修改)
     * @param account 添加/修改的账户
     */
    public abstract void insertAccount(@NonNull Account account);
    /**
     * 设置默认账户
     * @param account 要设置的账户, 如果id为null则添加后设置为默认
     */
    public abstract void setDefault(Account account);
    /**
     * 删除账户
     * @param id 账户ID
     */
    public abstract void deleteAccount(@NonNull String id);
}
