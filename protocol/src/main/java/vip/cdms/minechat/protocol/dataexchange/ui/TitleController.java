package vip.cdms.minechat.protocol.dataexchange.ui;

public interface TitleController {
    void title(String message);
    void subtitle(String message);
    void actionbar(String message);
    void popup(String message);
    void setFadeInTime(int tick);
    void setStayTime(int tick);
    void setFadeOutTime(int tick);

    void toast(String title, String message);
}
