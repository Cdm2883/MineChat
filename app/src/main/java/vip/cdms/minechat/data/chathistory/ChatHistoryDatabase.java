package vip.cdms.minechat.data.chathistory;

import android.app.Application;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {AChatHistory.class}, version = 2, exportSchema = false)
public abstract class ChatHistoryDatabase extends RoomDatabase {
    public abstract ChatHistoryDao getChatHistoryDao();

    private static ChatHistoryDatabase INSTANCE;
    public static void init(Application application) {
        if (INSTANCE != null) return;
        INSTANCE = Room.databaseBuilder(application, ChatHistoryDatabase.class, "chat_database")
                .build();
    }
    public static ChatHistoryDatabase INSTANCE() {
        return INSTANCE;
    }
}
