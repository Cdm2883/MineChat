package vip.cdms.minechat.protocol.util.motd;

import android.content.Context;
import android.graphics.drawable.Drawable;

import vip.cdms.mcoreui.util.ResourcesUtils;
import vip.cdms.minechat.protocol.R;
import vip.cdms.minechat.protocol.dataexchange.client.Motd;

public class MotdChecker {
    public static Motd get(String address) {
        String[] spilt = address.split(":");
        return get(spilt[0], spilt.length == 2 ? Integer.parseInt(spilt[1]) : 19132);
    }
    public static Motd get(String hostname, int port) {
        BEMotdChecker.BEMotd beMotd = BEMotdChecker.get(hostname, port);
        if (beMotd != null) return beMotd;
        return JEMotdChecker.get(hostname, port);
    }

    public static Drawable delayIcon(Context context, int delay) {
        int icon;
        if (delay < 100) icon = R.drawable.ping_green_dark;
        else if (delay < 200) icon = R.drawable.ping_yellow_dark;
        else icon = R.drawable.ping_red_dark;
        return ResourcesUtils.getPixelDrawable(context, icon);
    }
}
