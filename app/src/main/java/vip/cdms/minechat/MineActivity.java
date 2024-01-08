package vip.cdms.minechat;

import android.annotation.SuppressLint;
import android.view.View;

import vip.cdms.mcoreui.OreUIActivity;
import vip.cdms.mcoreui.view.dialog.SimpleFormBuilder;
import vip.cdms.minechat.beta.Watermark;
import vip.cdms.minechat.protocol.plugin.ServiceExtension;
import vip.cdms.minechat.protocol.plugin.ServicePool;
import vip.cdms.minechat.protocol.plugin.builtin.service.TermuxStarter;

public class MineActivity extends OreUIActivity {
    boolean watermark = false;
    @Override
    protected void onStart() {
        super.onStart();

        if (!watermark) {
            watermark = true;
            Watermark.show(this);
        }

        for (ServiceExtension service : ServicePool.getInstances())
            service.onActivityStart(this);
    }
}
