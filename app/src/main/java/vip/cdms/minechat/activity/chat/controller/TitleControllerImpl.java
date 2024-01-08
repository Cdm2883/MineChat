package vip.cdms.minechat.activity.chat.controller;

import java.util.HashMap;

import vip.cdms.mcoreui.util.MCFontWrapper;
import vip.cdms.mcoreui.util.MathUtils;
import vip.cdms.mcoreui.util.TimeUtils;
import vip.cdms.mcoreui.view.show.TextView;
import vip.cdms.mcoreui.view.show.Toast;
import vip.cdms.minechat.activity.chat.ChatActivity;
import vip.cdms.minechat.activity.chat.ChatActivityConnection;
import vip.cdms.minechat.protocol.dataexchange.ui.TitleController;

public class TitleControllerImpl extends ChatActivityConnection implements TitleController {
    int fadeInTime = 500;
    int stayTime = 3500;
    int fadeOutTime = 1000;

    public TitleControllerImpl(ChatActivity activity) {
        super(activity);
    }

    final HashMap<TextView, Integer> durationProgresses = new HashMap<>();
    final HashMap<TextView, Integer> durationTimers = new HashMap<>();
    /** @noinspection DataFlowIssue*/
    void durationTitle(TextView textView, String message) {
        Integer durationTimer = durationTimers.get(textView);
        if (durationTimer != null)
            TimeUtils.clearInterval(durationTimer);
        CharSequence messaged = MCFontWrapper.WRAPPER.wrap(message);
        getActivity().runOnUiThread(() -> {
            textView.animate().alpha(1).setDuration(fadeInTime).start();
            textView.setText(messaged);
        });
        durationProgresses.put(textView, 0);
        durationTimers.put(textView, TimeUtils.setInterval(() -> {
            Integer progress = durationProgresses.get(textView);
            if (progress == null || ++progress >= stayTime) {
                getActivity().runOnUiThread(() -> textView.animate().alpha(0).setDuration(fadeOutTime).start());
                TimeUtils.clearInterval(durationTimers.get(textView));
            } else durationProgresses.put(textView, progress);
        }, 1));
    }

    @Override
    public void title(String message) {
        durationTitle(getBinding().titleTitle, message);
    }
    @Override
    public void subtitle(String message) {
        durationTitle(getBinding().titleSubtitle, message);
    }
    @Override
    public void actionbar(String message) {
        durationTitle(getBinding().titleActionbar, message);
    }
    @Override
    public void popup(String message) {
        durationTitle(getBinding().titlePopup, message);
    }

    @Override
    public void setFadeInTime(int tick) {
        fadeInTime = (int) (MathUtils.divide(tick, 20, 1000) *  1000);
    }
    @Override
    public void setStayTime(int tick) {
        stayTime = (int) (MathUtils.divide(tick, 20, 1000) *  1000);
    }
    @Override
    public void setFadeOutTime(int tick) {
        fadeOutTime = (int) (MathUtils.divide(tick, 20, 1000) *  1000);
    }

    @Override
    public void toast(String title, String message) {
        getActivity().runOnUiThread(() -> new Toast()
                .setTitle(title)
                .setMessage(message)
                .show(getBinding().titles));
    }
}
