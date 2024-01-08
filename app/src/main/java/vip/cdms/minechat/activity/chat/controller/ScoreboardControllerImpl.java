package vip.cdms.minechat.activity.chat.controller;

import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import vip.cdms.mcoreui.util.MCFontWrapper;
import vip.cdms.mcoreui.util.TimeUtils;
import vip.cdms.mcoreui.view.show.TextView;
import vip.cdms.minechat.activity.chat.ChatActivity;
import vip.cdms.minechat.activity.chat.ChatActivityConnection;
import vip.cdms.minechat.databinding.ActivityChatBinding;
import vip.cdms.minechat.protocol.dataexchange.ui.ScoreboardController;

public class ScoreboardControllerImpl extends ChatActivityConnection implements ScoreboardController {
    final HashMap<String, ArrayList<ScoreboardController.AScore>> boards = new HashMap<>();

    String showingSidebar = null;
    int sidebarGoneTimer = -1;

    String showingList = null;

    
    
    ActivityChatBinding binding;
    int dp;
    HashMap<String, View> playerListId2View;
    public ScoreboardControllerImpl(ChatActivity activity) {
        super(activity);
        binding = getBinding();
        dp = activity.dp;
        playerListId2View = activity.playerListId2View;
    }
    
    

    @Override
    public void show(ScoreboardController.DisplaySlot displaySlot, String board, String displayName) {
        if (displaySlot == ScoreboardController.DisplaySlot.list) {
            showingList = board;
            CharSequence named = MCFontWrapper.WRAPPER.wrap(displayName);
            getActivity().runOnUiThread(() -> {
                binding.pauseMenuTitle.setText(named);
                changeList();
            });
            return;
        }

        if (sidebarGoneTimer != -1) TimeUtils.clearTimeout(sidebarGoneTimer);
        if (board == null || board.isEmpty() || displayName == null) {
            clear(showingSidebar);
            showingSidebar = null;
            sidebarGoneTimer = TimeUtils.setTimeout(() -> runOnUiThread(() ->
                    binding.sidebar.setVisibility(View.GONE)), 1000);
            return;
        }
        showingSidebar = board;
        CharSequence named = MCFontWrapper.WRAPPER.wrap(displayName);
        runOnUiThread(() -> {
            binding.sidebar.setVisibility(View.VISIBLE);
            binding.sidebarTitle.setText(named);
            changeSidebar();
        });
    }

    @Override
    public void clear(String board) {
        if (board == null) return;
        boards.put(board, null);
        if (board.equals(showingList)) for (int i = 0; i < binding.pauseMenuPlayers.getChildCount(); i++) {               // java.lang.NullPointerException: Attempt to invoke virtual method 'android.view.View android.view.ViewGroup.findViewById(int)' on a null object reference
            int finalI = i;                                                                                               //	at vip.cdms.minechat.activity.chat.controller.ScoreboardControllerImpl.lambda$clear$4(ScoreboardControllerImpl.java:80)
            runOnUiThread(() -> {                                                                                         //	at vip.cdms.minechat.activity.chat.controller.ScoreboardControllerImpl.$r8$lambda$7iKM1H1vd2i_QC8IATkhuhAYI_s(Unknown Source:0)
                ViewGroup playerItem = (ViewGroup) binding.pauseMenuPlayers.getChildAt(finalI);                           //	at vip.cdms.minechat.activity.chat.controller.ScoreboardControllerImpl$$ExternalSyntheticLambda5.run(Unknown Source:4)
                LinearLayout content = playerItem.findViewById(android.R.id.content);                                     //	at android.os.Handler.handleCallback(Handler.java:900)
                ConstraintLayout.LayoutParams contentParams = (ConstraintLayout.LayoutParams) content.getLayoutParams();  //	at android.os.Handler.dispatchMessage(Handler.java:103)
                contentParams.leftMargin = 0;                                                                             //	at android.os.Looper.loop(Looper.java:219)
                content.setLayoutParams(contentParams);                                                                   //	at android.app.ActivityThread.main(ActivityThread.java:8673)
                TextView summary = content.findViewById(android.R.id.summary);                                            //	at java.lang.reflect.Method.invoke(Native Method)
                summary.setVisibility(View.GONE);                                                                         //	at com.android.internal.os.RuntimeInit$MethodAndArgsCaller.run(RuntimeInit.java:513)
            });                                                                                                           //	at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:1109)
        }
    }

    @Override
    public void insert(String board, int index, ScoreboardController.AScore... adds) {
        //                        System.out.println("Inserting board " + board + Arrays.toString(adds));

        ArrayList<ScoreboardController.AScore> scores = boards.get(board);
        if (scores == null) scores = new ArrayList<>();
        if (index < 0) index = scores.size() + index;

        int scoresI = index + 1;
        for (ScoreboardController.AScore score : adds) {
            ScoreboardController.AScore scored = new ScoreboardController.AScore(score.id(), MCFontWrapper.WRAPPER.wrap(score.displayName()), score.score());

            int indexOf = indexOf(scores, score);
            if (indexOf != -1) {
                scores.set(indexOf, scored);
                continue;
            }

            scores.add(scoresI, scored);
            scoresI++;
        }

        change(board, scores);
    }
    @Override
    public void insert(String board, String id, ScoreboardController.AScore... adds) {
        insert(board, findIndexById(board, id), adds);
    }

    @Override
    public void remove(String board, int index) {
        ArrayList<ScoreboardController.AScore> scores = boards.get(board);
        if (scores == null || index < 0 || index >= scores.size()) return;
        scores.remove(index);
        change(board, scores);
    }
    @Override
    public void remove(String board, String id) {
        remove(board, findIndexById(board, id));
    }

    @Override
    public void remove(String board, ScoreboardController.AScore... removes) {
        ArrayList<ScoreboardController.AScore> scores = boards.get(board);
        if (scores == null) return;
        Iterator<ScoreboardController.AScore> iterator = scores.iterator();
        while (iterator.hasNext()) {
            ScoreboardController.AScore item = iterator.next();
            for (ScoreboardController.AScore remove : removes) {
                if (remove.equals(item)) {
                    iterator.remove();
                    break;
                }
            }
        }
        change(board, scores);
    }

    int indexOf(ArrayList<ScoreboardController.AScore> scores, ScoreboardController.AScore score) {
        for (int i = 0; i < scores.size(); i++) {
            ScoreboardController.AScore item = scores.get(i);
            if(item != null && item.equals(score)) return i;
        }
        return -1;
    }
    int findIndexById(String board, String id) {
        ArrayList<ScoreboardController.AScore> scores = boards.get(board);
        if (scores == null) return -1;
        for (int i = 0; i < scores.size(); i++) {
            ScoreboardController.AScore score = scores.get(i);
            if (id.equals(score.id())) return i;
        }
        return -1;
    }

    // ui
    void change(String board, ArrayList<ScoreboardController.AScore> scores) {
        if (scores == null || scores.isEmpty()) {
            clear(board);
            return;
        }
        boards.put(board, scores);
        runOnUiThread(() -> change(board));
    }
    void change(String board) {
        if (board.equals(showingSidebar)) changeSidebar();
        else if (board.equals(showingList)) changeList();
    }
    void changeSidebar() {
        if (showingSidebar == null) return;
        ArrayList<ScoreboardController.AScore> scores = boards.get(showingSidebar);
        if (scores == null) return;

        // view复用
        for (int i = scores.size(); i > binding.sidebarItems.getChildCount(); i--) {
            LinearLayout linearLayout = new LinearLayout(getActivity());
            linearLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            TextView nameView = new TextView(getActivity());
            LinearLayout.LayoutParams nameLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            nameLayoutParams.weight = 1;
            nameView.setLayoutParams(nameLayoutParams);
            linearLayout.addView(nameView);
            TextView scoreView = new TextView(getActivity());
            scoreView.setTextColor(0xffff0000);
            linearLayout.addView(scoreView);
            binding.sidebarItems.addView(linearLayout);
        }
        for (int i = binding.sidebarItems.getChildCount(); i > scores.size(); i--)
            binding.sidebarItems.removeViewAt(i - 1);
        for (int i = 0; i < scores.size(); i++) {
            var score = scores.get(i);
            LinearLayout linearLayout = (LinearLayout) binding.sidebarItems.getChildAt(i);
            if (linearLayout == null) continue;
            TextView nameView = (TextView) linearLayout.getChildAt(0);
            nameView.setText(score.displayName());
            TextView scoreView = (TextView) linearLayout.getChildAt(1);
            scoreView.setText(String.valueOf(score.score()));
        }
    }
    void changeList() {
        if (showingList == null) return;
        ArrayList<ScoreboardController.AScore> scores = boards.get(showingList);
        if (scores == null) return;

        for (ScoreboardController.AScore score : scores) {
            ViewGroup playerItem = (ViewGroup) playerListId2View.get(score.id());
            if (playerItem == null) return;
            LinearLayout content = playerItem.findViewById(android.R.id.content);
            ConstraintLayout.LayoutParams contentParams = (ConstraintLayout.LayoutParams) content.getLayoutParams();
            contentParams.leftMargin = 58 * dp;
            content.setLayoutParams(contentParams);
            TextView summary = content.findViewById(android.R.id.summary);
            summary.setVisibility(View.VISIBLE);
            summary.setText(String.valueOf(score.score()));
        }
    }
}