package vip.cdms.minechat.protocol.dataexchange.ui;

import androidx.annotation.Nullable;

public interface ScoreboardController {
    record AScore(
            String id,
            CharSequence displayName,
            int score
    ) {
        public AScore(String displayName, int score) {
            this(displayName, displayName, score);
        }
        @Override
        public boolean equals(@Nullable Object obj) {
            if (!(obj instanceof AScore aScore)) return false;
            return aScore.id.equals(id) || String.valueOf(aScore.displayName).equals(String.valueOf(displayName));
        }
    }

    enum DisplaySlot {
        sidebar,
        list
    }

    /**
     * 展示计分板
     * @param displaySlot 显示位置
     * @param board 计分板id
     * @param displayName 显示名称
     */
    void show(DisplaySlot displaySlot, String board, String displayName);

    /**
     * 清空并隐藏计分板
     * @param board 计分板id
     */
    void clear(String board);

    void insert(String board, int index, AScore... adds);
    void insert(String board, String id, AScore... adds);

    void remove(String board, int index);
    void remove(String board, String id);
    void remove(String board, AScore... removes);
}
