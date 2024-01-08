package vip.cdms.mcoreui.util;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ImageSpan;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class IconFont extends SpannableStringBuilder implements FontWrapper {
    private static final int SIZE = 64;
    public static final Map<Character, Bitmap> TABLE = new LinkedHashMap<>();

    public IconFont(CharSequence text) {
        super(text);

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
//            if (0xd800 <= c && c <= 0xdbff) {
//                i++;
//                continue;
//            }
            Bitmap icon = TABLE.get(c);
            if (icon == null) continue;
            setSpan(
                    new ImageSpan(make(icon)),
                    i,
                    i + 1,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            );
        }
    }

    public static void loadFormGlyph(int row, Bitmap glyph) {
        if (glyph == null) return;

        int width = glyph.getWidth();
        int height = glyph.getHeight();
        int aWidth = width / 16;
        int aHeight = height / 16;

        char character;
        Bitmap icon;
        int column = 0x00;
        for (int y = 0; y < height; y+=aHeight) {
            for (int x = 0; x < width; x+=aWidth) {
                icon = Bitmap.createBitmap(glyph, x, y, aWidth, aHeight);

                boolean isEmpty = true;
                // 之后看情况是否要做对图片左右的空白进行切割, 更符合原版逻辑
                OUT:
                for (int y1 = 0; y1 < aHeight; y1++) {
                    for (int x1 = 0; x1 < aWidth; x1++) {
                        int pixel = icon.getPixel(x1, y1);
                        if (Color.alpha(pixel) != 0) {
                            isEmpty = false;
                            break OUT;
                        }
                    }
                }
                character = (char) ((row << 8) | column);
                if (!isEmpty) TABLE.put(character, icon);
                column++;
            }
        }
    }
    public static void loadFormGlyph(String row, Bitmap glyph) {
        if (row == null) return;
        loadFormGlyph(Integer.parseInt(row, 16), glyph);
    }

    public static Drawable make(Bitmap bitmap) {
        Drawable drawable;
        drawable = new BitmapDrawable(bitmap);
        drawable = PixelDrawable.instance(drawable);
        drawable.setBounds(0, 0, SIZE, SIZE);
        return drawable;
    }

    public static final IconFont WRAPPER = new IconFont("");
    @Override
    public CharSequence wrap(CharSequence charSequence) {
        if (charSequence == null) return "";
        return new IconFont(charSequence);
    }
}
