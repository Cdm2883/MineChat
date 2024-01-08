package vip.cdms.minechat.protocol.plugin;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

import java.util.Arrays;
import java.util.stream.IntStream;

/**
 * 为插件提供的i18n解决方案
 */
public class JLanguage {
    public static final String en = "en";
    public static final String zh_rCN = "zh-rCN";

    public static class Trans implements CharSequence {
        private JLanguage parent;
        private final String[] translations;
        public Trans(JLanguage parent, String... translations) {
            this.parent = parent;
            this.translations = translations;
        }
        public Trans(String... translations) {
            this(null, translations);
        }

        public JLanguage parent() {
            return parent;
        }
        public Trans parent(JLanguage parent) {
            this.parent = parent;
            return this;
        }

        public String get(String language) {
            if (language == null) return translations[0];
            int index = Arrays.binarySearch(parent.languages, language);
            if (index == -1) return translations[0];
            return translations[index];
        }
        public String get() {
            return get(parent == null ? null : parent.language);
        }

        @Override
        public int length() {
            return get().length();
        }
        @Override
        public char charAt(int index) {
            return get().charAt(index);
        }
        @NonNull
        @Override
        public CharSequence subSequence(int start, int end) {
            return get().subSequence(start, end);
        }
        /** @noinspection EqualsWhichDoesntCheckParameterClass*/
        @Override
        public boolean equals(@Nullable Object obj) {
            return get().equals(obj);
        }
        @Override
        public int hashCode() {
            return get().hashCode();
        }
        @NonNull
        @Override
        public String toString() {
            return get();
        }
        @NonNull
        @Override
        public IntStream chars() {
            return get().chars();
        }
        @NonNull
        @Override
        public IntStream codePoints() {
            return get().codePoints();
        }
    }
    private String language = en;
    private final String[] languages;

    public JLanguage(String ...languages) {
        this.languages = languages;
    }
    public JLanguage() {
        this(en, zh_rCN);
    }

    public Trans add(String ...translations) {
        return new Trans(this, translations);
    }

    public JLanguage set(String language) {
        this.language = language;
        return this;
    }
    public JLanguage set(Context context) {
        SharedPreferences defaultPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return set(defaultPreferences.getString("language", en));
    }
}
