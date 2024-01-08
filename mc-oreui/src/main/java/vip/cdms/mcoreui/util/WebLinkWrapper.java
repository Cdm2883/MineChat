package vip.cdms.mcoreui.util;

import android.os.Build;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.URLSpan;
import android.text.util.Linkify;

public class WebLinkWrapper implements FontWrapper {
    public static final WebLinkWrapper WRAPPER = new WebLinkWrapper();

//    public static class URLSpan extends android.text.style.URLSpan {
//        public URLSpan(String url) {
//            super(url);
//        }
//    }

    @Override
    public CharSequence wrap(CharSequence charSequence) {
        Spannable spannable = charSequence instanceof Spannable sp ? sp : new SpannableString(charSequence);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Linkify.addLinks(spannable, Linkify.WEB_URLS, URLSpan::new);
        } else {
            Linkify.addLinks(spannable, Linkify.WEB_URLS);
        }
        return spannable;
    }
}
