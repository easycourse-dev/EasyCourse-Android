package com.example.markwen.easycourse.components;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.markwen.easycourse.R;
import com.example.markwen.easycourse.models.Language;
import com.hanks.library.AnimateCheckBox;

import java.util.ArrayList;

/**
 * Created by noahrinehart on 10/29/16.
 */

public class SignupChooseLanguageAdapter extends RecyclerView.Adapter<SignupChooseLanguageAdapter.LanguageViewHolder> {

    private ArrayList<Language> languageList = new ArrayList<>();
    private ArrayList<Language> checkedLanguageList = new ArrayList<>();

    public SignupChooseLanguageAdapter(ArrayList<Language> coursesList) {
        this.languageList = coursesList;
    }

    public static class LanguageViewHolder extends RecyclerView.ViewHolder {
        CardView languageCardView;
        TextView languageTextView;
        AnimateCheckBox languageCheckBox;
        RelativeLayout languageLayout;

        LanguageViewHolder(View itemView) {
            super(itemView);
            languageCardView = (CardView) itemView.findViewById(R.id.cardViewSingleItem);
            languageTextView = (TextView) itemView.findViewById(R.id.textViewSingleItem);
            languageCheckBox = (AnimateCheckBox) itemView.findViewById(R.id.checkBoxSingleItem);
            languageLayout = (RelativeLayout) itemView.findViewById(R.id.relativeLayoutSingleItem);
            languageCheckBox.setClickable(false);
            languageCheckBox.setEnabled(false);
        }
    }

    @Override
    public LanguageViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.signup_choose_single_item, viewGroup, false);
        LanguageViewHolder languageViewHolder = new LanguageViewHolder(v);
        return languageViewHolder;
    }

    @Override
    public void onBindViewHolder(final LanguageViewHolder languageViewHolder, int i) {
        final Language language = languageList.get(i);
        languageViewHolder.languageTextView.setText(language.getName());

        languageViewHolder.languageLayout.setOnClickListener(new View.OnClickListener() {
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

    public ArrayList<Language> getLanguageList() {
        return languageList;
    }

    public ArrayList<Language> getCheckedLanguageList() {
        return checkedLanguageList;
    }
}