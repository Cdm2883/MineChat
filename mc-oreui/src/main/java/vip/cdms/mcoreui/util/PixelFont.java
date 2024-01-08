package vip.cdms.mcoreui.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.fonts.Font;
import android.graphics.fonts.FontFamily;
import android.os.Build;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.MetricAffectingSpan;

import androidx.annotation.NonNull;
import androidx.core.graphics.PaintCompat;
import androidx.core.util.Pair;

import java.nio.ByteBuffer;
import java.util.Arrays;

import vip.cdms.mcoreui.R;

/**
 * 像素字体包装器 (可以实现MCBE OREUI奇怪的字体特性awa)
 * @author Cdm2883
 */
public class PixelFont extends SpannableStringBuilder implements FontWrapper {
    public static Typeface typefaceAll = null;

    public static Typeface typefaceAscii = null;
    public static Typeface typefaceBoldAscii = null;

    public static Typeface typefaceNotAscii = null;

    public PixelFont(CharSequence text, Typeface typeface) {
        super(text);
        Object what;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            what = new android.text.style.TypefaceSpan(typeface);
        } else {
            what = new TypefaceSpan(typeface);
        }
        setSpan(what, 0, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    public PixelFont(CharSequence text) {
        this(text, false);
    }
    public PixelFont(CharSequence text, boolean bold) {
        super(text == null ? "" : text);
        if (text == null) return;
        if (text instanceof PixelFont) return;

        final Typeface typefaceAscii = bold ? typefaceBoldAscii : PixelFont.typefaceAscii;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (0xd800 <= c && c <= 0xdbff) {
                i++;
                continue;
            }

            // 简单粗暴
            boolean useAscii = bold ? isInMinecraftTen(c) : isInMojangles(c);
            setSpan(
                    new TypefaceSpan(useAscii ? typefaceAscii : typefaceNotAscii),
                    i,
                    i + 1,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            );
        }
    }

    public static boolean isInMojangles(char c) {
        return c == 0x0 || c == 0xd
                || (0x20 <= c && c <= 0x7e)
                || (0xa0 <= c && c <= 0xff)
                || c == 0x131 || c == 0x141
                || c == 0x142 || c == 0x152
                || c == 0x153 || c == 0x160
                || c == 0x161 || c == 0x178
                || c == 0x17d || c == 0x17e
                || c == 0x192 || c == 0x2c6
                || c == 0x2c7 || c == 0x2d8
                || c == 0x2d9 || c == 0x2da
                || c == 0x2db || c == 0x2dc
                || c == 0x2dd || c == 0x394
                || c == 0x2206 || c == 0x3a9
                || c == 0x2126 || c == 0x3c0
                || c == 0x2013 || c == 0x2014
                || c == 0x2018 || c == 0x2019
                || c == 0x201a || c == 0x201c
                || c == 0x201d || c == 0x201e
                || c == 0x2020 || c == 0x2021
                || c == 0x2022 || c == 0x2026
                || c == 0x2030 || c == 0x2039
                || c == 0x203a || c == 0x2044
                || c == 0x2074 || c == 0x20ac
                || c == 0x2122 || c == 0x2202
                || c == 0x220f || c == 0x2211
                || c == 0x2212 || c == 0x221a
                || c == 0x221e || c == 0x222b
                || c == 0x2248 || c == 0x2260
                || c == 0x2264 || c == 0x2265
                || c == 0x25ca || c == 0xfb01
                || c == 0xfb02;
    }
    public static boolean isInMinecraftTen(char c) {
        // 我选择脚本
        return c == 0x21 || c == 0x22 || c == 0x23 || c == 0x24 || c == 0x25 || c == 0x26 || c == 0x27 || c == 0x28 || c == 0x29 || c == 0x2a || c == 0x2b || c == 0x2c
                || c == 0x2d || c == 0x2e || c == 0x2f || c == 0x30 || c == 0x31 || c == 0x32 || c == 0x33 || c == 0x34 || c == 0x35 || c == 0x36 || c == 0x37 || c == 0x38
                || c == 0x39 || c == 0x3a || c == 0x3b || c == 0x3c || c == 0x3d || c == 0x3e || c == 0x3f || c == 0x40 || c == 0x41 || c == 0x42 || c == 0x43 || c == 0x44
                || c == 0x45 || c == 0x46 || c == 0x47 || c == 0x48 || c == 0x49 || c == 0x4a || c == 0x4b || c == 0x4c || c == 0x4d || c == 0x4e || c == 0x4f || c == 0x50
                || c == 0x51 || c == 0x52 || c == 0x53 || c == 0x54 || c == 0x55 || c == 0x56 || c == 0x57 || c == 0x58 || c == 0x59 || c == 0x5a || c == 0x5b || c == 0x5c
                || c == 0x5d || c == 0x5e || c == 0x5f || c == 0x60 || c == 0x61 || c == 0x62 || c == 0x63 || c == 0x64 || c == 0x65 || c == 0x66 || c == 0x67 || c == 0x68
                || c == 0x69 || c == 0x6a || c == 0x6b || c == 0x6c || c == 0x6d || c == 0x6e || c == 0x6f || c == 0x70 || c == 0x71 || c == 0x72 || c == 0x73 || c == 0x74
                || c == 0x75 || c == 0x76 || c == 0x77 || c == 0x78 || c == 0x79 || c == 0x7a || c == 0x7b || c == 0x7c || c == 0x7d || c == 0x7e || c == 0xa0 || c == 0xa1
                || c == 0xa2 || c == 0xa3 || c == 0xa5 || c == 0xa6 || c == 0xa8 || c == 0xa9 || c == 0xab || c == 0xac || c == 0xad || c == 0xae || c == 0xaf || c == 0xb0
                || c == 0xb1 || c == 0xb4 || c == 0xb6 || c == 0xb7 || c == 0xb8 || c == 0xbb || c == 0xbf || c == 0xc0 || c == 0xc1 || c == 0xc2 || c == 0xc3 || c == 0xc4
                || c == 0xc5 || c == 0xc6 || c == 0xc7 || c == 0xc8 || c == 0xc9 || c == 0xca || c == 0xcb || c == 0xcc || c == 0xcd || c == 0xce || c == 0xcf || c == 0xd0
                || c == 0xd1 || c == 0xd2 || c == 0xd3 || c == 0xd4 || c == 0xd5 || c == 0xd6 || c == 0xd7 || c == 0xd8 || c == 0xd9 || c == 0xda || c == 0xdb || c == 0xdc
                || c == 0xdd || c == 0xde || c == 0xdf || c == 0xe0 || c == 0xe1 || c == 0xe2 || c == 0xe3 || c == 0xe4 || c == 0xe5 || c == 0xe6 || c == 0xe7 || c == 0xe8
                || c == 0xe9 || c == 0xea || c == 0xeb || c == 0xec || c == 0xed || c == 0xee || c == 0xef || c == 0xf0 || c == 0xf1 || c == 0xf2 || c == 0xf3 || c == 0xf4
                || c == 0xf5 || c == 0xf6 || c == 0xf7 || c == 0xf8 || c == 0xf9 || c == 0xfa || c == 0xfb || c == 0xfc || c == 0xfd || c == 0xfe || c == 0xff || c == 0x100
                || c == 0x101 || c == 0x102 || c == 0x103 || c == 0x104 || c == 0x105 || c == 0x106 || c == 0x107 || c == 0x108 || c == 0x109 || c == 0x10a || c == 0x10b || c == 0x10c
                || c == 0x10d || c == 0x10e || c == 0x10f || c == 0x110 || c == 0x111 || c == 0x112 || c == 0x113 || c == 0x114 || c == 0x115 || c == 0x116 || c == 0x117 || c == 0x118
                || c == 0x119 || c == 0x11a || c == 0x11b || c == 0x11c || c == 0x11d || c == 0x11e || c == 0x11f || c == 0x120 || c == 0x121 || c == 0x122 || c == 0x123 || c == 0x124
                || c == 0x125 || c == 0x126 || c == 0x127 || c == 0x128 || c == 0x129 || c == 0x12a || c == 0x12b || c == 0x12c || c == 0x12d || c == 0x12e || c == 0x12f || c == 0x130
                || c == 0x131 || c == 0x132 || c == 0x133 || c == 0x134 || c == 0x135 || c == 0x136 || c == 0x137 || c == 0x138 || c == 0x139 || c == 0x13a || c == 0x13b || c == 0x13c
                || c == 0x13d || c == 0x13e || c == 0x13f || c == 0x140 || c == 0x141 || c == 0x142 || c == 0x143 || c == 0x144 || c == 0x145 || c == 0x146 || c == 0x147 || c == 0x148
                || c == 0x14a || c == 0x14b || c == 0x14c || c == 0x14d || c == 0x14e || c == 0x14f || c == 0x150 || c == 0x151 || c == 0x152 || c == 0x153 || c == 0x154 || c == 0x155
                || c == 0x156 || c == 0x157 || c == 0x158 || c == 0x159 || c == 0x15a || c == 0x15b || c == 0x15c || c == 0x15d || c == 0x15e || c == 0x15f || c == 0x160 || c == 0x161
                || c == 0x162 || c == 0x163 || c == 0x164 || c == 0x165 || c == 0x166 || c == 0x167 || c == 0x168 || c == 0x169 || c == 0x16a || c == 0x16b || c == 0x16c || c == 0x16d
                || c == 0x16e || c == 0x16f || c == 0x170 || c == 0x171 || c == 0x172 || c == 0x173 || c == 0x174 || c == 0x175 || c == 0x176 || c == 0x177 || c == 0x178 || c == 0x179
                || c == 0x17a || c == 0x17b || c == 0x17c || c == 0x17d || c == 0x17e || c == 0x192 || c == 0x1fc || c == 0x1fd || c == 0x1fe || c == 0x1ff || c == 0x218 || c == 0x219
                || c == 0x21a || c == 0x21b || c == 0x237 || c == 0x2c6 || c == 0x2c7 || c == 0x2c9 || c == 0x2d8 || c == 0x2d9 || c == 0x2da || c == 0x2db || c == 0x2dc || c == 0x2dd
                || c == 0x384 || c == 0x385 || c == 0x386 || c == 0x388 || c == 0x389 || c == 0x38a || c == 0x38c || c == 0x38e || c == 0x38f || c == 0x390 || c == 0x391 || c == 0x392
                || c == 0x393 || c == 0x394 || c == 0x395 || c == 0x396 || c == 0x397 || c == 0x398 || c == 0x399 || c == 0x39a || c == 0x39b || c == 0x39c || c == 0x39d || c == 0x39e
                || c == 0x39f || c == 0x3a0 || c == 0x3a1 || c == 0x3a3 || c == 0x3a4 || c == 0x3a5 || c == 0x3a6 || c == 0x3a7 || c == 0x3a8 || c == 0x3a9 || c == 0x3aa || c == 0x3ab
                || c == 0x3ac || c == 0x3ad || c == 0x3ae || c == 0x3af || c == 0x3b0 || c == 0x3b1 || c == 0x3b2 || c == 0x3b3 || c == 0x3b4 || c == 0x3b5 || c == 0x3b6 || c == 0x3b7
                || c == 0x3b8 || c == 0x3b9 || c == 0x3ba || c == 0x3bb || c == 0x3bc || c == 0x3bd || c == 0x3be || c == 0x3bf || c == 0x3c0 || c == 0x3c1 || c == 0x3c2 || c == 0x3c3
                || c == 0x3c4 || c == 0x3c5 || c == 0x3c6 || c == 0x3c7 || c == 0x3c8 || c == 0x3c9 || c == 0x3ca || c == 0x3cb || c == 0x3cc || c == 0x3cd || c == 0x3ce || c == 0x400
                || c == 0x401 || c == 0x402 || c == 0x403 || c == 0x404 || c == 0x405 || c == 0x406 || c == 0x407 || c == 0x408 || c == 0x409 || c == 0x40a || c == 0x40b || c == 0x40c
                || c == 0x40d || c == 0x40e || c == 0x40f || c == 0x410 || c == 0x411 || c == 0x412 || c == 0x413 || c == 0x414 || c == 0x415 || c == 0x416 || c == 0x417 || c == 0x418
                || c == 0x419 || c == 0x41a || c == 0x41b || c == 0x41c || c == 0x41d || c == 0x41e || c == 0x41f || c == 0x420 || c == 0x421 || c == 0x422 || c == 0x423 || c == 0x424
                || c == 0x425 || c == 0x426 || c == 0x427 || c == 0x428 || c == 0x429 || c == 0x42a || c == 0x42b || c == 0x42c || c == 0x42d || c == 0x42e || c == 0x42f || c == 0x430
                || c == 0x431 || c == 0x432 || c == 0x433 || c == 0x434 || c == 0x435 || c == 0x436 || c == 0x437 || c == 0x438 || c == 0x439 || c == 0x43a || c == 0x43b || c == 0x43c
                || c == 0x43d || c == 0x43e || c == 0x43f || c == 0x440 || c == 0x441 || c == 0x442 || c == 0x443 || c == 0x444 || c == 0x445 || c == 0x446 || c == 0x447 || c == 0x448
                || c == 0x449 || c == 0x44a || c == 0x44b || c == 0x44c || c == 0x44d || c == 0x44e || c == 0x44f || c == 0x450 || c == 0x451 || c == 0x452 || c == 0x453 || c == 0x454
                || c == 0x455 || c == 0x456 || c == 0x457 || c == 0x458 || c == 0x459 || c == 0x45a || c == 0x45b || c == 0x45c || c == 0x45d || c == 0x45e || c == 0x45f || c == 0x490
                || c == 0x491 || c == 0x1e02 || c == 0x1e03 || c == 0x1e0a || c == 0x1e0b || c == 0x1e1e || c == 0x1e1f || c == 0x1e22 || c == 0x1e23 || c == 0x1e30 || c == 0x1e31 || c == 0x1e40
                || c == 0x1e41 || c == 0x1e56 || c == 0x1e57 || c == 0x1e60 || c == 0x1e61 || c == 0x1e6a || c == 0x1e6b || c == 0x1e80 || c == 0x1e81 || c == 0x1e82 || c == 0x1e83 || c == 0x1e84
                || c == 0x1e85 || c == 0x1e9e || c == 0x1ef2 || c == 0x1ef3 || c == 0x2013 || c == 0x2014 || c == 0x2015 || c == 0x2018 || c == 0x2019 || c == 0x201a || c == 0x201c || c == 0x201d
                || c == 0x201e || c == 0x2020 || c == 0x2021 || c == 0x2022 || c == 0x2026 || c == 0x2030 || c == 0x2039 || c == 0x203a || c == 0x2044 || c == 0x20ac || c == 0x2122 || c == 0x2126
                || c == 0x2206 || c == 0x220f || c == 0x2211 || c == 0x2212 || c == 0x221e || c == 0x25ca || c == 0xf6c3 || c == 0xfb01 || c == 0xfb02;
    }
//    public static boolean isInMSFGothic(char c) {  // 太多辣
//        return ;
//    }

    public static void init(Context context) {
        if (typefaceAll != null && typefaceAscii != null && typefaceNotAscii != null && typefaceBoldAscii != null) return;
        typefaceAll = ResourcesUtils.getTypeface(context, R.font.unifont_15_0_06);
        typefaceAscii = ResourcesUtils.getTypeface(context, R.font.mojangles);
        typefaceBoldAscii = ResourcesUtils.getTypeface(context, R.font.minecraftten);
        typefaceNotAscii = ResourcesUtils.getTypeface(context, R.font.msfgothic);
    }

    public static final PixelFont WRAPPER = new PixelFont("");
    @Override
    public CharSequence wrap(CharSequence charSequence) {
        if (charSequence == null) return "";
        return new PixelFont(charSequence);
    }

    public static class TypefaceSpan extends MetricAffectingSpan {
        private final Typeface typeface;

        public TypefaceSpan(Typeface typeface) {
            this.typeface = typeface;
        }

        @Override
        public void updateDrawState(TextPaint ds) {
            applyCustomTypeface(ds);
        }

        @Override
        public void updateMeasureState(@NonNull TextPaint paint) {
            applyCustomTypeface(paint);
        }

        private void applyCustomTypeface(TextPaint paint) {
            Typeface oldTypeface = paint.getTypeface();
            int oldStyle = oldTypeface != null ? oldTypeface.getStyle() : 0;
            int fakeStyle = oldStyle & ~typeface.getStyle();

            if ((fakeStyle & Typeface.BOLD) != 0) {
                paint.setFakeBoldText(true);
            }

            if ((fakeStyle & Typeface.ITALIC) != 0) {
                paint.setTextSkewX(-0.25f);
            }

            paint.setTypeface(typeface);
        }
    }
}
