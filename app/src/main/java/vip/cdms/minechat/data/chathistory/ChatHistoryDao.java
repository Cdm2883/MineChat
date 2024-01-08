package vip.cdms.minechat.data.chathistory;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.ArrayList;
import java.util.List;

@Dao
public interface ChatHistoryDao {
    @Insert
    void insert(AChatHistory chatRecord);

    @Delete
    void delete(AChatHistory chatRecord);

    @Query("DELETE FROM chat_histories WHERE server_id = :serverId")
    void clear(String serverId);

    @Query("DELETE FROM chat_histories WHERE id NOT IN (SELECT id FROM chat_histories WHERE server_id = :serverId ORDER BY timestamp DESC LIMIT :count)")
    void keep(String serverId, int count);

    @Query("SELECT * FROM chat_histories WHERE server_id = :serverId ORDER BY timestamp ASC")
    LiveData<List<AChatHistory>> get(String serverId);
}
