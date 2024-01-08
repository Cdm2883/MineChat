package vip.cdms.mcoreui.preference;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.SpannedString;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceGroupAdapter;
import androidx.preference.PreferenceScreen;
import androidx.recyclerview.widget.RecyclerView;

import java.util.concurrent.atomic.AtomicReference;

import vip.cdms.mcoreui.R;
import vip.cdms.mcoreui.util.MathUtils;
import vip.cdms.mcoreui.util.PixelFont;
import vip.cdms.mcoreui.util.ResourcesUtils;
import vip.cdms.mcoreui.util.ViewUtils;

public abstract class PreferenceFragment extends PreferenceFragmentCompat {
    private int dp;
    private Drawable listBackground;

    private void setAllPreferencesToAvoidHavingExtraSpace(Preference preference) {
        preference.setIconSpaceReserved(false);
        if (preference instanceof PreferenceGroup)
            for (int i = 0; i < ((PreferenceGroup) preference).getPreferenceCount(); i++) {
                setAllPreferencesToAvoidHavingExtraSpace(((PreferenceGroup) preference).getPreference(i));
            }
    }

    @Override
    public void setPreferenceScreen(PreferenceScreen preferenceScreen) {
        if (preferenceScreen != null)
            setAllPreferencesToAvoidHavingExtraSpace(preferenceScreen);
        super.setPreferenceScreen(preferenceScreen);
    }

    @NonNull
    @SuppressLint("RestrictedApi")
    @Override
    protected RecyclerView.Adapter<?> onCreateAdapter(@NonNull PreferenceScreen preferenceScreen) {
        return new PreferenceGroupAdapter(preferenceScreen) {
            @Override
            public void onPreferenceHierarchyChange(@NonNull Preference preference) {
                setAllPreferencesToAvoidHavingExtraSpace(preference);
                super.onPreferenceHierarchyChange(preference);
            }
        };
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Context context = getContext();
        dp = MathUtils.dp2px(context, 1);
        listBackground = ResourcesUtils.getPixelDrawable(context, R.drawable.list_background);

        setDivider(null);
//        setDivider(new ColorDrawable(0xff333334));

        LinearLayout linearLayout = (LinearLayout) view;
        final RecyclerView[] recyclerView = new RecyclerView[1];
        ViewUtils.forEach(linearLayout, view1 -> {
            if (view1 instanceof RecyclerView recyclerView1) recyclerView[0] = recyclerView1;
        });
        ViewUtils.setOreUIVerticalScrollBar(recyclerView[0]);
        recyclerView[0].setItemAnimator(null);
        recyclerView[0].addOnChildAttachStateChangeListener(new RecyclerView.OnChildAttachStateChangeListener() {
            @Override
            public void onChildViewAttachedToWindow(@NonNull View view) {
                TextView title = view.findViewById(android.R.id.title);
                TextView summary = view.findViewById(android.R.id.summary);
                if (!(title instanceof vip.cdms.mcoreui.view.show.TextView) && !(title.getText() instanceof SpannedString))
                    view.getViewTreeObserver().addOnDrawListener(() -> {
                        AtomicReference<View> lastParent = new AtomicReference<>();
                        ViewUtils.forEachParent(title, parent -> {
                            if (parent instanceof RecyclerView) {
                                lastParent.get().setBackground(listBackground);
                                return false;
                            }
                            lastParent.set(parent);
                            return true;
                        });

                        title.setTextColor(0xffffffff);
                        title.setTextSize(15);
                        title.setText(new PixelFont(title.getText().toString()));

                        summary.setTextColor(0xffbfc0c2);
                        summary.setTextSize(13);
                        summary.setText(new PixelFont(summary.getText().toString()));
                        summary.setPadding(
                                summary.getPaddingLeft(),
                                4 * dp,
                                summary.getPaddingRight(),
                                summary.getPaddingBottom()
                        );
                    });
            }
            @Override
            public void onChildViewDetachedFromWindow(@NonNull View view) {}
        });
    }
}
