package io.easycourse.www.easycourse.components.main.UserProfile;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import io.easycourse.www.easycourse.R;
import io.easycourse.www.easycourse.models.main.Language;
import io.easycourse.www.easycourse.utils.ListsUtils;
import com.hanks.library.AnimateCheckBox;

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

    class ViewHolder extends RecyclerView.ViewHolder {
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
        }
    }

    @Override
    public LanguageRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.activity_userprofile_language_item, viewGroup, false);
        return new LanguageRecyclerViewAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final LanguageRecyclerViewAdapter.ViewHolder languageViewHolder, int i) {
        final Language language = languageList.get(i);
        languageViewHolder.languageTextView.setText(language.getTranslation());
        if (checkable){
            languageViewHolder.languageCheckBox.setVisibility(View.VISIBLE);
            languageViewHolder.languageCheckBox.setClickable(true);
        } else {
            languageViewHolder.languageCheckBox.setVisibility(View.GONE);
            languageViewHolder.languageCheckBox.setClickable(false);
        }
        if (ListsUtils.isLanguageInList(checkedLanguageList, language.getCode())) {
            languageViewHolder.languageCheckBox.setChecked(true);
        } else {
            languageViewHolder.languageCheckBox.setChecked(false);
        }
        languageViewHolder.languageCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (languageViewHolder.languageCheckBox.isChecked()) {
                    languageViewHolder.languageCheckBox.setChecked(false);
                    language.setChecked(false, realm);
                    Language.getLanguageByCode(language.getCode(), realm).setChecked(false, realm);
                    checkedLanguageList.remove(language);
                } else {
                    languageViewHolder.languageCheckBox.setChecked(true);
                    language.setChecked(true, realm);
                    Language.getLanguageByCode(language.getCode(), realm).setChecked(true, realm);
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

    public void setLanguageList(RealmList<Language> list) {
        this.languageList = list;
    }

    public RealmList<Language> getLanguageList() {
        return languageList;
    }

    public void setCheckable(boolean bool) {
        this.checkable = bool;
    }
}
