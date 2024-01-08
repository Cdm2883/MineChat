package vip.cdms.mcoreui.util;

import android.graphics.Color;
import android.graphics.Typeface;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;

import androidx.annotation.NonNull;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * MC彩色文本转换器
 * (之前写给MineBuilder的工具类, 但是MineBuilder现在没有发版, 现在又需要这个功能, 就暂时拿过来用 XD)
 * @author Cdm2883
 */
public class MCTextParser implements FontWrapper {
    public static final MCTextParser WRAPPER = new MCTextParser();
    @Override
    public CharSequence wrap(CharSequence charSequence) {
        if (charSequence instanceof String string)
            return text2CharSequence(string, false);
        else return charSequence;
    }

    public static final char charSS = '§';
    public static final String SS = String.valueOf(charSS);
    public static final Map<String, String> COLORS = new HashMap<>(){{
        put(SS + "0", "000000");  // black
        put(SS + "1", "0000AA");  // dark_blue
        put(SS + "2", "00AA00");  // dark_green
        put(SS + "3", "00AAAA");  // dark_aqua
        put(SS + "4", "AA0000");  // dark_red
        put(SS + "5", "AA00AA");  // dark_purple
        put(SS + "6", "FFAA00");  // gold
        put(SS + "7", "AAAAAA");  // gray
        put(SS + "8", "555555");  // dark_gray
        put(SS + "9", /*"5455FF"*/"5555FF");  // blue
        put(SS + "a", /*"55FF56"*/"55FF55");  // green
        put(SS + "b", "55FFFF");  // aqua
        put(SS + "c", "FF5555");  // red
        put(SS + "d", "FF55FE");  // light_purple
        put(SS + "e", "FFFF55");  // yellow
        put(SS + "f", "FFFFFF");  // white
        put(SS + "g", "EECF15");  // minecoin_gold
        put(SS + "h", "E3D4D1");  // material_quartz
        put(SS + "i", "CECACA");  // material_iron
        put(SS + "j", "443A3B");  // material_netherite
//        put(SS + "m", "971607");  // material_redstone
//        put(SS + "n", "B4684D");  // material_copper
        put(SS + "p", "DEB12D");  // material_gold
        put(SS + "q", "47A036");  // material_emerald
        put(SS + "s", "2CBAA8");  // material_diamond
        put(SS + "t", "21497B");  // material_lapis
        put(SS + "u", "9A5CC6");  // material_amethyst
    }};
    public static final Map<String, String> EXTRAS = new HashMap<>(){{
        put(SS + "k", "del");  // obfuscated
        put(SS + "l", "b");  // bold
        put(SS + "m", "s");  // strikethrough
        put(SS + "n", "u");  // underline
        put(SS + "o", "i");  // italic
    }};

    public static class MCTextFragment {
        public String text = "";
        public String color = null;
        public final ArrayList<MCTextFragment> items = new ArrayList<>();
        public final ArrayList<String> extras = new ArrayList<>();
    }

    public static MCTextFragment text2MCTextFragment(String text, boolean keep) {
        MCTextFragment mcTextFragment = new MCTextFragment();
        MCTextFragment current = mcTextFragment;
        String[] strings = text.split("");

        for (int i = 0; i < strings.length; i++) {
            MCTextFragment inner;
            if (!Objects.equals(strings[i], SS)) {
                current.text += strings[i];
            } else if (i + 1 < strings.length && Objects.equals(strings[i + 1], "r")) {

                if (keep) {
                    MCTextFragment keeper = new MCTextFragment();
                    keeper.text = SS + "r";
                    keeper.color = "AAAAAA";
                    mcTextFragment.items.add(keeper);
                }

                inner = new MCTextFragment();
                mcTextFragment.items.add(inner);
                current = inner;
                i++;
            } else {
                String colorCode = SS + (i + 1 < strings.length ? strings[i + 1] : "");

                if (keep) {
                    MCTextFragment keeper = new MCTextFragment();
                    keeper.text = colorCode;
                    if (COLORS.get(colorCode) != null) {
                        Color color = Color.valueOf(Color.parseColor("#" + COLORS.get(colorCode)));
                        keeper.color = String.format("%08X", Color.argb(color.alpha(),
                                Math.max(0, color.red() - .3f),
                                Math.max(0, color.green() - .3f),
                                Math.max(0, color.blue() - .3f))
                        ).substring(2);
                    }
                    if (EXTRAS.get(colorCode) != null) {
                        keeper.color = "AAAAAA";
                        keeper.extras.add(EXTRAS.get(colorCode));
                    }
                    current.items.add(keeper);
                }

                inner = new MCTextFragment();
                if (COLORS.get(colorCode) != null) inner.color = COLORS.get(colorCode);
                if (EXTRAS.get(colorCode) != null) inner.extras.add(EXTRAS.get(colorCode));
                current.items.add(inner);
                current = inner;
                i++;
            }
        }

        return mcTextFragment;
    }
    public static String textKeepHtml(String text) {
        return /*StringEscapeUtils.escapeHtml4(*/text/*)*/
                .replaceAll("\\n\\r", "<br/>")
                .replaceAll("\\n", "<br/>")
                .replaceAll("\\r", "<br/>");
    }
    public static String mcTextFragment2Html(ArrayList<MCTextFragment> toParse) {
        StringBuilder html = new StringBuilder();
        for (MCTextFragment mcTextFragment : toParse) {
            ArrayList<String> tags = mcTextFragment.extras;
            String colorHex = mcTextFragment.color;
            StringBuilder text = new StringBuilder(textKeepHtml(mcTextFragment.text));
            for (MCTextFragment item : mcTextFragment.items)
                text.append(mcTextFragment2Html(item));

            StringBuilder resultStart = new StringBuilder();
            StringBuilder resultEnd = new StringBuilder();
            for (String tag : tags) {
                resultStart.append("<").append(tag).append(">");
                resultEnd.append("</").append(tag).append(">");
            }
            String fontStart = "";
//            if (colorHex != null) fontStart = "<span style=\"color:#" + colorHex + "\">";
//            html.append(fontStart).append(resultStart).append(text).append(resultEnd).append(fontStart.isEmpty() ? "" : "</span>");
            if (colorHex != null) fontStart = "<font color=\"#" + colorHex + "\">";
            html.append(fontStart).append(resultStart).append(text).append(resultEnd).append(fontStart.isEmpty() ? "" : "</font>");
//            html.append("<font").append(colorHex == null ? "" : (" color=\"#" + colorHex + "\"")).append(">").append(resultStart).append(text).append(resultEnd).append("</font>");
        }
        return html.toString();
    }
    /**
     * 明明可以不用这个函数的, 但是安卓自带的Html解析遇到颜色的嵌套就会出问题, 只能在解析之前把嵌套结构解开一下了
     */
    public static ArrayList<MCTextFragment> unwrapMcTextFragments(ArrayList<MCTextFragment> mcTextFragments) {
        ArrayList<MCTextFragment> singleMCTextFragments = new ArrayList<>();

        int children = 0;
        for (MCTextFragment mcTextFragment : mcTextFragments) {
            singleMCTextFragments.add(mcTextFragment);
            for (MCTextFragment childMCTextFragment : mcTextFragment.items) {
                if (childMCTextFragment.color == null) childMCTextFragment.color = mcTextFragment.color;
                for (String extra : mcTextFragment.extras) {
                    if (!childMCTextFragment.extras.contains(extra)) childMCTextFragment.extras.add(extra);
                }
                singleMCTextFragments.add(childMCTextFragment);
                children++;
            }
            mcTextFragment.items.clear();
        }

        if (children == 0)
            return singleMCTextFragments;
        else
            return unwrapMcTextFragments(singleMCTextFragments);
    }
    public static String mcTextFragment2Html(MCTextFragment mcTextFragment) {
//        return mcTextFragment2Html(new ArrayList<>(){{ add(mcTextFragment); }});
        return mcTextFragment2Html(unwrapMcTextFragments(new ArrayList<>(){{
            add(mcTextFragment);
        }}));
    }

    /**
     * MC文本格式转HTML
     * @param text MC文本格式
     * @return HTML
     */
    public static String text2Html(String text, boolean keep) {
        if (!text.contains(SS)) return text;
        return mcTextFragment2Html(text2MCTextFragment(text, keep));
    }

    /**
     * MC文本格式转安卓文字样式
     * @param text MC文本格式
     * @param keep 是否保留格式符
     * @return 安卓文字样式
     */
    public static CharSequence text2CharSequence(String text, boolean keep) {
        if (!text.contains(SS)) return text;

        SpannableStringBuilder sb = new SpannableStringBuilder();
        int     color         = -1    ;
        boolean obfuscated    = false ;  // k
        boolean bold          = false ;  // l
        boolean strikethrough = false ;  // m
        boolean underline     = false ;  // n
        boolean italic        = false ;  // o

        String[] strings = text.split("");
        for (int i = 0; i < strings.length; i++) {
            if (!Objects.equals(strings[i], SS)) {
                int start = sb.length();
                sb.append(strings[i]);
                int end = sb.length();
                if (start >= end) continue;

                int finalColor = color;
                boolean finalUnderline = underline;
                sb.setSpan(new UnderlineSpan() {
                    @Override
                    public void updateDrawState(@NonNull TextPaint ds) {
                        if (finalColor != -1) ds.setColor(finalColor);
                        ds.setUnderlineText(finalUnderline);
                    }
                }, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                if (bold && !italic) sb.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                else if (italic && !bold) sb.setSpan(new StyleSpan(Typeface.ITALIC), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                else if (italic/* && bold*/) sb.setSpan(new StyleSpan(Typeface.BOLD_ITALIC), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                if (strikethrough || obfuscated) sb.setSpan(new StrikethroughSpan(), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else if (i + 1 < strings.length && Objects.equals(strings[i + 1], "r")) {

                if (keep) {
                    int start = sb.length();
                    sb.append(SS).append("r");
                    int end = sb.length();
                    sb.setSpan(new UnderlineSpan() {
                        @Override
                        public void updateDrawState(@NonNull TextPaint ds) {
                            ds.setColor(0xffaaaaaa);
                        }
                    }, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }

                color = -1;
                obfuscated = false;
                bold = false;
                strikethrough = false;
                underline = false;
                italic = false;
                i++;
            } else {
                String formatCode = SS + (i + 1 < strings.length ? strings[i + 1] : "");
                if (COLORS.get(formatCode) != null) {
                    color = Color.parseColor("#FF" + COLORS.get(formatCode));
                }
                if (EXTRAS.get(formatCode) != null) {
                    obfuscated    = obfuscated    || formatCode.equals(SS + "k");
                    bold          = bold          || formatCode.equals(SS + "l");
                    strikethrough = strikethrough || formatCode.equals(SS + "m");
                    underline     = underline     || formatCode.equals(SS + "n");
                    italic        = italic        || formatCode.equals(SS + "o");
                }
                i++;

                if (keep) {
                    int start = sb.length();
                    sb.append(formatCode);
                    int end = sb.length();

                    Color colour = color != -1 ? Color.valueOf(color) : null;
                    if (colour != null) {
                        colour = Color.valueOf(Color.argb(
                                colour.alpha(),
                                Math.max(0, colour.red() - .3f),
                                Math.max(0, colour.green() - .3f),
                                Math.max(0, colour.blue() - .3f)
                        ));
                    }

                    Color finalColor = colour;
                    boolean finalUnderline = underline;
                    sb.setSpan(new UnderlineSpan() {
                        @Override
                        public void updateDrawState(@NonNull TextPaint ds) {
                            if (finalColor != null) ds.setColor(finalColor.toArgb());
                            else ds.setColor(0xffaaaaaa);
                            ds.setUnderlineText(finalUnderline);
                        }
                    }, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    if (bold && !italic) sb.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    else if (italic && !bold) sb.setSpan(new StyleSpan(Typeface.ITALIC), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    else if (italic/* && bold*/) sb.setSpan(new StyleSpan(Typeface.BOLD_ITALIC), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    if (strikethrough || obfuscated) sb.setSpan(new StrikethroughSpan(), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }
        }

        return sb;

//        太吃性能了!!!
//        return Html.fromHtml(text2Html(text, keep), Html.FROM_HTML_MODE_LEGACY);
    }

    /**
     * JE服务器description转MC文本格式
     * @param description json.description
     * @return MC文本格式
     */
    public static String description2text(JsonElement description) {
        StringBuilder builder = new StringBuilder();

        if (description instanceof JsonPrimitive jsonPrimitive && jsonPrimitive.isString())
            return jsonPrimitive.getAsString();

        if (description instanceof JsonObject jsonObject && jsonObject.has("extra")) {
            JsonArray extra = jsonObject.get("extra").getAsJsonArray();
            for (JsonElement item : extra) {
                if (!(item instanceof JsonObject object)) throw new RuntimeException(new IllegalArgumentException());
                if (!object.has("text")) continue;
                String text = object.get("text").getAsString();
                if (text.isEmpty()) continue;

                if (object.has("bold")          && object.get("bold").getAsBoolean()         ) text = SS + "l" + text;
                if (object.has("italic")        && object.get("italic").getAsBoolean()       ) text = SS + "o" + text;
                if (object.has("underline")     && object.get("underline").getAsBoolean()    ) text = SS + "n" + text;
                if (object.has("strikethrough") && object.get("strikethrough").getAsBoolean()) text = SS + "m" + text;
                if (object.has("obfuscated")    && object.get("obfuscated").getAsBoolean()   ) text = SS + "k" + text;

                if (object.has("color")) {
                    switch (object.get("color").getAsString()) {
                        case "black"         -> text = SS + "0" + text;
                        case "dark_blue"     -> text = SS + "1" + text;
                        case "dark_green"    -> text = SS + "2" + text;
                        case "dark_aqua"     -> text = SS + "3" + text;
                        case "dark_red"      -> text = SS + "4" + text;
                        case "dark_purple"   -> text = SS + "5" + text;
                        case "gold"          -> text = SS + "6" + text;
                        case "gray"          -> text = SS + "7" + text;
                        case "dark_gray"     -> text = SS + "8" + text;
                        case "blue"          -> text = SS + "9" + text;
                        case "green"         -> text = SS + "a" + text;
                        case "aqua"          -> text = SS + "b" + text;
                        case "red"           -> text = SS + "c" + text;
                        case "light_purple"  -> text = SS + "d" + text;
                        case "yellow"        -> text = SS + "e" + text;
                        case "white"         -> text = SS + "f" + text;
                        case "minecoin_gold" -> text = SS + "g" + text;
                    }
                }
                builder.append(SS).append("r").append(text);
            }
        } else builder.append(SS).append("4").append("[ERR] ").append(SS).append("c").append("Unable to serialize motd!");

        return builder.toString().trim();
    }

    /**
     * 清除MC文本格式
     * @param text MC文本格式
     * @return 纯文本
     */
    public static String clearText(String text) {
        return text.replaceAll("§[a-zA-Z0-9]", "");
    }

    public static String easyFormat(String text) {
        return text.replaceAll("&", SS);
    }

    private static String COLOR_STRINGS;
    private static String EXTRA_STRINGS;
    public static String random(boolean color, boolean extra, int length) {
        if (COLOR_STRINGS == null) {
            StringBuilder sb = new StringBuilder();
            for (String colour : COLORS.keySet()) sb.append(colour.substring(1));
            COLOR_STRINGS = sb.toString();
        }
        if (EXTRA_STRINGS == null) {
            StringBuilder sb = new StringBuilder();
            for (String colour : EXTRAS.keySet()) sb.append(colour.substring(1));
            EXTRA_STRINGS = sb.toString();
        }

        String characters = (color ? COLOR_STRINGS : "") + (extra ? EXTRA_STRINGS : "");
        StringBuilder random = new StringBuilder();
        for (int i = 0; i < length; i++)
            random.append(characters.charAt((int) (Math.random() * characters.length())));
        return random.toString();
    }
    public static String randomInsert(boolean color, boolean extra, String s) {
        StringBuilder random = new StringBuilder();
        for(int i = 0; i < s.length();) {
            int[] cp = new int[1];
            cp[0] = s.codePointAt(i);
            random.append(SS).append(random(color, extra, 1));
            if (Character.isSupplementaryCodePoint(cp[0])) {
                i += 2;
                random.append(new String(cp,0,1));
            } else {
                i++;
                random.append((char) cp[0]);
            }
        }
        return random.toString();
    }
}
