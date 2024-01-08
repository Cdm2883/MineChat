package vip.cdms.minechat.protocol.dataexchange.client;

public interface Motd {
    int TIME_OUT = 3000;

    String version();
    int protocol();
    String motd();
    int onlinePlayer();
    int maxPlayer();
    int delay();
}
