package vip.cdms.minechat.data.chathistory;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "chat_histories")
public class AChatHistory {
    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "server_id")
    private String serverId;
    private String message;
    private boolean isMe;
    private long timestamp;

    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }
    public String getServerId() {
        return serverId;
    }
    public void setServerId(String serverId) {
        this.serverId = serverId;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public boolean isMe() {
        return isMe;
    }
    public void setMe(boolean me) {
        isMe = me;
    }
    public long getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
