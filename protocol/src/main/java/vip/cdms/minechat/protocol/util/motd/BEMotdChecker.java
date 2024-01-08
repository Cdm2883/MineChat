package vip.cdms.minechat.protocol.util.motd;

import java.math.BigInteger;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import vip.cdms.minechat.protocol.dataexchange.client.Motd;

/**
 * 查看MCBE服务器信息
 * @author Cdm2883
 */
public class BEMotdChecker {
    /**
     * BE服务器信息
     * @param motd MOTD
     * @param protocol 协议版本
     * @param version 游戏版本
     * @param onlinePlayer 在线玩家数
     * @param maxPlayer 最大玩家数
     * @param world 存档名
     * @param gameMode 游戏模式
     * @param modeNum 游戏模式 (数字)
     * @param delay 延迟
     */
    public record BEMotd(
            String motd,
            int protocol,
            String version,
            int onlinePlayer,
            int maxPlayer,
            String world,
            String gameMode,
            int modeNum,
            int delay
    ) implements Motd {}

    public static BEMotd get(String address) {
        String[] spilt = address.split(":");
        return get(spilt[0], spilt.length == 2 ? Integer.parseInt(spilt[1]) : 19132);
    }
    public static BEMotd get(String hostname, int port) {
        try(DatagramSocket socket = new DatagramSocket()) {
            socket.setSoTimeout(Motd.TIME_OUT);
            socket.connect(InetAddress.getByName(hostname), port);

            byte[] send = new BigInteger("0100000000240D12D300FFFF00FEFEFEFEFDFDFDFD12345678", 16).toByteArray();
//            byte[] send = new BigInteger("01" + "0000000000000000" + "00FFFF00FEFEFEFEFDFDFDFD12345678" + "810a7974264c03f7", 16).toByteArray();

            long startTime = System.currentTimeMillis();

            socket.send(new DatagramPacket(send, send.length));

            DatagramPacket receive = new DatagramPacket(new byte[4096], 4096);
            socket.receive(receive);

            long endTime = System.currentTimeMillis();

            String data = new String(receive.getData(), StandardCharsets.UTF_8);
            String[] split = data.split(";");
//            System.out.println(Arrays.toString(split));

            return new BEMotd(
                    split[1],
                    Integer.parseInt(split[2]),
                    split[3],
                    Integer.parseInt(split[4]),
                    Integer.parseInt(split[5]),
                    split[7],
                    split[8],
                    parseInt(split[9]),
                    (int) (endTime - startTime)
            );
        } catch (Exception e) {
//            e.printStackTrace();
            return null;
        }
    }

    private static int parseInt(String string) {
        try {
            return Integer.parseInt(string);
        } catch (Exception e) {
            Pattern pattern = Pattern.compile("[^0-9]");
            Matcher matcher = pattern.matcher(string);
            if (!matcher.find()) return -1;
            return Integer.parseInt(matcher.replaceAll("").trim());
        }
    }
}
