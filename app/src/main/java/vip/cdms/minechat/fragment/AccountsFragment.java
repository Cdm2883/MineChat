package vip.cdms.minechat.fragment;

import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.gson.JsonArray;

import vip.cdms.mcoreui.view.dialog.DialogBuilder;
import vip.cdms.mcoreui.util.SoundPlayer;
import vip.cdms.mcoreui.view.dialog.CustomFormBuilder;
import vip.cdms.mcoreui.view.button.TextButton;
import vip.cdms.minechat.MainActivity;
import vip.cdms.minechat.R;
import vip.cdms.minechat.databinding.FragmentAccountBinding;
import vip.cdms.minechat.protocol.app.Accounts;
import vip.cdms.minechat.protocol.dataexchange.bean.Account;
import vip.cdms.minechat.protocol.util.ExceptionHandler;
import vip.cdms.minechat.protocol.util.MojangAPI;
import vip.cdms.minechat.view.SlideListAdapter;

public class AccountsFragment extends Fragment {
    private MainActivity activity;
    private FragmentAccountBinding binding;
    private SlideListAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (MainActivity) getActivity();
    }

    private void refresh() {
        Account[] accounts = Accounts.INSTANCE.getAccounts();
        if (accounts.length == 0) {
            binding.recyclerView.setVisibility(View.GONE);
            binding.empty.setVisibility(View.VISIBLE);
            return;
        } else {
            binding.recyclerView.setVisibility(View.VISIBLE);
            binding.empty.setVisibility(View.GONE);
        }

        adapter.clearDataItems();
        for (int i = 0; i < accounts.length; i++) {
            Account account = accounts[i];
            boolean isDefault = account.equals(Accounts.INSTANCE.getAccount(null));
            SlideListAdapter.DataItem dataItem = new SlideListAdapter.DataItem(activity,
                    account.isOnline() ? R.drawable.icon_account : vip.cdms.minechat.protocol.R.drawable.bot,
                    account.title(),
                    (isDefault ? getString(R.string.fragment_account_default) + " " : "") + account.username()
                            + (account.isOnline() ? "" : " (" + getString(R.string.fragment_account_add_dialog_offline) + ")"), v -> {
                Accounts.INSTANCE.setDefault(account);
                refresh();
            }).addSlide(activity, R.drawable.icon_small_setting, v -> edit(account))
                    .addSlide(activity, R.drawable.icon_small_delete, v -> new DialogBuilder(getActivity())
                            .setTitle(getString(R.string.fragment_account_delete_dialog_title))
                            .setContent(getString(R.string.fragment_account_delete_dialog_content, account.title()))
                            .addAction(getString(R.string.fragment_account_delete_dialog_no))
                            .addAction(getString(R.string.fragment_account_delete_dialog_yes), TextButton.Style.RED, v1 -> {
                                Accounts.INSTANCE.deleteAccount(account.id());
                                refresh();
                            })
                            .show());
            adapter.addDataItem(dataItem);
        }
    }

    void edit(Account account) {
        String title = account == null ? null : account.title();
        String user = account == null ? null : account.username();
        boolean isOnline = account != null && account.isOnline();
        ExceptionHandler.processing(
                new CustomFormBuilder()
                        .setTitle(getString(R.string.fragment_account_add))
                        .addInput(getString(R.string.fragment_account_add_dialog_title), title, title)
                        .addInput(getString(R.string.fragment_account_add_dialog_username), user, user)
                        .addSwitch(getString(R.string.fragment_account_add_dialog_offline), !isOnline)
                        .setCallback(callback -> {
                            if (callback.isCancel()) return;
                            JsonArray data = callback.responseData();
                            String mTitle = data.get(0).getAsString();
                            String mUser = data.get(1).getAsString();
                            if (mTitle.isEmpty() && mUser.isEmpty()) return;
                            boolean mIsOnline = !data.get(2).getAsBoolean();
                            Accounts.INSTANCE.insertAccount(new Account(
                                    account == null ? null : account.id(),
                                    mTitle.isEmpty() ? mUser : mTitle,
                                    mUser,
                                    mIsOnline
                            ));
                            refresh();
                        }).build(activity),
                target -> {
                    if (account != null) target.addAction(getString(R.string.fragment_account_edit_dialog_set_default), v -> {
                        Accounts.INSTANCE.setDefault(account);
                        refresh();
                    });
                    target.show();
                    return null;
                });
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAccountBinding.inflate(inflater);

        adapter = binding.recyclerView.getAdapter();
        refresh();

        binding.addAccountButton.setOnClickListener(v -> edit(null));

        View root = binding.getRoot();
        root.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        return binding.getRoot();
    }
}