package vip.cdms.minechat.protocol.dataexchange.bean;

import androidx.annotation.Nullable;

public record Account(
        String id,
        String title,
        String username,
        boolean isOnline
) {
    @Override
    public boolean equals(@Nullable Object obj) {
        if (!(obj instanceof Account account)) return false;
        if (id == null || account.id == null) return username.equals(account.username);
        return id.equals(account.id);
    }
}
