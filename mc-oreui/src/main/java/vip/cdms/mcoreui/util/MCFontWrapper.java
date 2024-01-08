package vip.cdms.mcoreui.util;

import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;

/**
 * 字体包装器 (MC格式文本解析 + 像素字体 + 图标字体)
 * @author Cdm2883
 */
public class MCFontWrapper implements FontWrapper {
    public static final MCFontWrapper WRAPPER = new MCFontWrapper();

    @Override
    public CharSequence wrap(CharSequence charSequence) {
        return FontWrapper.mix(
                IconFont.WRAPPER,
                PixelFont.WRAPPER,
                MCTextParser.WRAPPER
        ).wrap(charSequence);
    }

    public Spanned prefix(CharSequence prefix, CharSequence charSequence) {
        return ((SpannableStringBuilder) wrap(charSequence)).insert(0, prefix);
    }
    public Spanned prefix(Object what, CharSequence charSequence) {
        SpannableStringBuilder spannable = ((SpannableStringBuilder) wrap(charSequence)).insert(0, " ");
        spannable.setSpan(what, 0, 1, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        return spannable;
    }
}
