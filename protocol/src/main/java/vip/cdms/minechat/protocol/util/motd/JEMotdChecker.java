package vip.cdms.minechat.protocol.util.motd;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import vip.cdms.minechat.protocol.dataexchange.client.Motd;
import vip.cdms.mcoreui.util.MCTextParser;

/**
 * 查看MCJE服务器信息
 * @author Cdm2883
 */
public class JEMotdChecker {
    /**
     * JE服务器信息
     * @param version 游戏版本
     * @param protocol 协议版本
     * @param maxPlayer 最大玩家数
     * @param onlinePlayer 在线玩家数
     * @param motd MOTD
     * @param favicon base64图标
     * @param delay 延迟
     */
    public record JEMotd(
            String version,
            int protocol,
            int maxPlayer,
            int onlinePlayer,
            String motd,
            String favicon,
            int delay
    ) implements Motd {}

    public static JEMotd get(String address) {
        String[] spilt = address.split(":");
        return get(spilt[0], spilt.length == 2 ? Integer.parseInt(spilt[1]) : 25565);
    }
    public static JEMotd get(String hostname, int port) {
        try (Socket socket = new Socket()) {
            socket.setSoTimeout(Motd.TIME_OUT);
            socket.connect(new InetSocketAddress(hostname, port), Motd.TIME_OUT);

            DataInputStream in = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());

            long startTime = System.currentTimeMillis();

            ByteArrayOutputStream handshakeBytes = new ByteArrayOutputStream();
            DataOutputStream handshake = new DataOutputStream(handshakeBytes);

            handshake.writeByte(0x00);
            writeVarInt(handshake, 4);
            writeVarInt(handshake, hostname.length());
            handshake.writeBytes(hostname);
            handshake.writeShort(port);
            writeVarInt(handshake, 1);
            writeVarInt(out, handshakeBytes.size());
            out.write(handshakeBytes.toByteArray());

            out.writeByte(0x01);
            out.writeByte(0x00);

            /*int size = */readVarInt(in);
            int id = readVarInt(in);
            if (id == -1) throw new IOException("Premature end of stream");
            if (id != 0) throw new IOException("Invalid packet id");
            int length = readVarInt(in);
            if (length == -1) throw new IOException("Premature end of stream");
            if (length == 0) throw new IOException("Unexpected string length");

            byte[] data = new byte[length];
            in.readFully(data);
            JsonObject json = JsonParser.parseString(new String(data)).getAsJsonObject();

            long endTime = System.currentTimeMillis();

            handshake.close();
            handshakeBytes.close();
            out.close();
            in.close();

            return new JEMotd(
                    json.get("version").getAsJsonObject().get("name").getAsString(),
                    json.get("version").getAsJsonObject().get("protocol").getAsInt(),
                    json.get("players").getAsJsonObject().get("max").getAsInt(),
                    json.get("players").getAsJsonObject().get("online").getAsInt(),
                    MCTextParser.description2text(json.get("description")),
                    json.has("favicon") ? json.get("favicon").getAsString() : null,
                    (int) (endTime - startTime)
            );
        } catch (Exception e) {
//            e.printStackTrace();
            return null;
        }
    }

    private static int readVarInt(DataInputStream in) throws IOException {
        int i = 0;
        int j = 0;
        int k;
        do {
            k = in.readByte();
            i |= (k & 0x7F) << j++ * 7;
            if (j > 5) throw new RuntimeException("VarInt too big");
        } while ((k & 0x80) == 128);
        return i;
    }
    private static void writeVarInt(DataOutputStream out, int paramInt) throws IOException {
        for (;;) {
            if ((paramInt & 0xFFFFFF80) == 0) {
                out.writeByte(paramInt);
                return;
            }
            out.writeByte(paramInt & 0x7F | 0x80);
            paramInt >>>= 7;
        }
    }
}
