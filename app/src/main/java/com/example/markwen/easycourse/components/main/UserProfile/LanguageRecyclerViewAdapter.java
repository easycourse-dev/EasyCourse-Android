package com.example.markwen.easycourse.components.main.UserProfile;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.markwen.easycourse.R;
import com.example.markwen.easycourse.models.main.Language;
import com.example.markwen.easycourse.utils.ListsUtils;
import com.hanks.library.AnimateCheckBox;

import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmList;

/**
 * Created by markw on 1/5/2017.
 */

public class LanguageRecyclerViewAdapter extends RecyclerView.Adapter<LanguageRecyclerViewAdapter.ViewHolder> {
    private RealmList<Language> languageList = new RealmList<>();
    private RealmList<Language> checkedLanguageList = new RealmList<>();
    private Realm realm;
    private boolean checkable = false;

    public LanguageRecyclerViewAdapter(RealmList<Language> languageList) {
        this.languageList = languageList;
        realm = Realm.getDefaultInstance();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView languageCardView;
        TextView languageTextView;
        AnimateCheckBox languageCheckBox;
        RelativeLayout languageLayout;

        ViewHolder(View itemView) {
            super(itemView);
            languageCardView = (CardView) itemView.findViewById(R.id.cardViewSingleItem);
            languageTextView = (TextView) itemView.findViewById(R.id.textViewSingleItem);
            languageCheckBox = (AnimateCheckBox) itemView.findViewById(R.id.checkBoxSingleItem);
            languageLayout = (RelativeLayout) itemView.findViewById(R.id.relativeLayoutSingleItem);
            languageCheckBox.setEnabled(false);
        }
    }

    @Override
    public LanguageRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.signup_choose_single_item, viewGroup, false);
        return new LanguageRecyclerViewAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final LanguageRecyclerViewAdapter.ViewHolder languageViewHolder, int i) {
        final Language language = languageList.get(i);
        languageViewHolder.languageTextView.setText(language.getTranslation());
        if (ListsUtils.isLanguageInList(checkedLanguageList, language.getCode())) {
            languageViewHolder.languageCheckBox.setChecked(true);
        } else {
            languageViewHolder.languageCheckBox.setChecked(false);
        }

        languageViewHolder.languageCheckBox.setClickable(checkable);

        languageViewHolder.languageCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (languageViewHolder.languageCheckBox.isChecked()) {
                    languageViewHolder.languageCheckBox.setChecked(false);
                    language.setChecked(false);
                    checkedLanguageList.remove(language);
                } else {
                    languageViewHolder.languageCheckBox.setChecked(true);
                    language.setChecked(true);
                    checkedLanguageList.add(language);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return languageList.size();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    public void setCheckedLanguageList(RealmList<Language> list) {
        this.checkedLanguageList = list;
    }

    public RealmList<Language> getCheckedLanguageList() {
        return checkedLanguageList;
    }

    public ArrayList<String> getCheckedLanguageCodeArrayList() {
        ArrayList<String> result = new ArrayList<>();
        for (int i = 0; i < checkedLanguageList.size(); i++) {
            result.add(checkedLanguageList.get(i).getCode());
        }
        return result;
    }

    public void setLanguageList(RealmList<Language> list, boolean bool) {
        this.checkable = bool;
        this.languageList.clear();
        this.languageList.addAll(list);
        notifyDataSetChanged();
    }

    public RealmList<Language> getLanguageList() {
        return languageList;
    }

    public void saveCheckedLanguages() {
        for (int i = 0; i < checkedLanguageList.size(); i++) {
            Language.updateLanguageToRealm(checkedLanguageList.get(i), realm);
        }
    }
}
