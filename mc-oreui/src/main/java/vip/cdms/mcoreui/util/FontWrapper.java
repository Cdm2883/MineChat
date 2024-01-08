package vip.cdms.mcoreui.util;

/**
 * 字体包装器
 * @author Cdm2883
 */
public interface FontWrapper {
    FontWrapper NON = charSequence -> charSequence;
    CharSequence wrap(CharSequence charSequence);

    static FontWrapper mix(FontWrapper ...wrappers) {
        return charSequence -> {
            for (int i = wrappers.length - 1; i >= 0; i--)
                charSequence = wrappers[i].wrap(charSequence);
            return charSequence;
        };
    }
}
