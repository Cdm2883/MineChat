package vip.cdms.minechat.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Icon;

import com.google.gson.JsonPrimitive;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import vip.cdms.mcoreui.util.IconFont;
import vip.cdms.minechat.protocol.app.Storage;
import vip.cdms.minechat.protocol.util.StorageUtils;

/**
 * 基岩版资源包管理器
 * // todo 重构。 对每个uuid的资源包和底包分别存放, 不合并. 非静态, 使用uuid[]以及优先级创建实例.
 */
public class BedrockResource {
    public static File ROOT_DIR;

    public static Bitmap loadImage(String relativePath) {
        File imageFile = new File(ROOT_DIR, relativePath + ".png");
        if (!imageFile.exists()) return null;
        try {
            return BitmapFactory.decodeStream(new FileInputStream(imageFile));
        } catch (FileNotFoundException e) {
            return null;
        }
    }

    private static Map<String, Map<String, String>> languages;
    public static String loadLanguage(String language, String translation, Object... params) {
        if (language == null) language = "zh_CN";
        if (translation == null) return "";

        Map<String, String> translations = languages.get(language);
        if (translations == null) {
            Map<String, String> newTranslations = new HashMap<>();
            try (Stream<String> stream = Files.lines(new File(ROOT_DIR, "texts/" + language + ".lang").toPath(), StandardCharsets.UTF_8)) {
                stream.forEach(line -> {
                    line = line.trim();
                    if (line.isEmpty() || line.startsWith("##")) return;
                    line = line.replaceFirst("#.*$", "")
                            .replaceAll("#$", "")
                            .trim();
                    if (line.isEmpty() || line.startsWith("##")) return;
                    String[] lines = line.split("=");
                    if (lines.length < 2) return;
                    newTranslations.put(lines[0], lines[1]);
                });
            } catch (IOException e) {
                return translation;
            }
            languages.put(language, newTranslations);
            translations = newTranslations;
        }

        // 翻译内容
        for (var key : translations.keySet()) {
            var value = translations.get(key);
            if (value == null) value = "";
            if (translation.equals(key)) {
                translation = value;
                break;
            }
            translation = translation.replace('%' + key, value);
        }

        // 翻译参数
        for (int i = 0; i < params.length; i++) {
            var param = params[i];
            if (param instanceof JsonPrimitive jsonPrimitive) {
                if (jsonPrimitive.isString()) param = jsonPrimitive.getAsString();
                else if (jsonPrimitive.isBoolean()) param = jsonPrimitive.getAsBoolean();
                else if (jsonPrimitive.isNumber()) param = jsonPrimitive.getAsNumber();
            }
            if (param instanceof String paramStr && paramStr.startsWith("%")) {
                var mTranslation = translations.get(paramStr.substring(1));
                if (mTranslation != null) param = mTranslation;
            }
            params[i] = param;
        }

        return format(translation, params);
    }

    /** @noinspection StringBufferMayBeStringBuilder*/
    public static String format(String format, Object... params) {
        int currentIndex = 0;
        Matcher matcher = Pattern.compile("%(\\d+\\$)?[sdfioO]")
                .matcher(format.replaceAll("%%", "%"));
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String match = matcher.group();
            String index = matcher.group(1);

            if (index == null || index.isEmpty()) {
                matcher.appendReplacement(result, currentIndex < params.length ? String.valueOf(params[currentIndex++]) : match);
            } else {
                int argIndex = Integer.parseInt(index.substring(0, index.length() - 1)) - 1;
                matcher.appendReplacement(result, argIndex >= 0 && argIndex < params.length ? String.valueOf(params[argIndex]) : match);
            }
        }

        matcher.appendTail(result);
        return result.toString();
//        return String.format(format, params);
    }

    /** @noinspection ResultOfMethodCallIgnored*/
    public static void init() {
        ROOT_DIR = new File(Storage.EXTERNAL_FILES, "bedrock_resource_pack");
        languages = new HashMap<>();

        File fonts = new File(ROOT_DIR, "font");
        if (!fonts.exists()) return;
        Pattern pattern = Pattern.compile("^glyph_(..)\\.png$");
        fonts.listFiles((dir, name) -> {
            Matcher matcher = pattern.matcher(name);
            if (!matcher.find()) return false;
            String row = matcher.group(1);
            if (row == null || !row.startsWith("E")) return false;  // 使用自己的字体系统. 不然字体就全是图片里面的了
            IconFont.loadFormGlyph(row, loadImage("font/glyph_" + row));
            return true;
        });
    }
}
