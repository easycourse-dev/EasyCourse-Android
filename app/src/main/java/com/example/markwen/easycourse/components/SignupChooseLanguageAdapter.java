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

public class    SignupChooseLanguageAdapter extends RecyclerView.Adapter<SignupChooseLanguageAdapter.LanguageViewHolder>{

    private ArrayList<Language> languageList = new ArrayList<>();

    public SignupChooseLanguageAdapter(ArrayList<Language> coursesList){
        this.languageList = coursesList;
    }

    public static class LanguageViewHolder extends RecyclerView.ViewHolder {
        CardView languageCardView;
        TextView languageTextView;
        AnimateCheckBox languageCheckBox;
        RelativeLayout languageLayout;

        LanguageViewHolder(View itemView) {
            super(itemView);
            languageCardView = (CardView)itemView.findViewById(R.id.cardViewLanguageItem);
            languageTextView = (TextView)itemView.findViewById(R.id.textViewLanguageItem);
            languageCheckBox = (AnimateCheckBox)itemView.findViewById(R.id.checkBoxLanguageItem);
            languageLayout = (RelativeLayout) itemView.findViewById(R.id.relativeLayoutLanguageItem);
        }
    }

    @Override
    public LanguageViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.signup_choose_language_item, viewGroup, false);
        LanguageViewHolder languageViewHolder = new LanguageViewHolder(v);
        return languageViewHolder;
    }

    @Override
    public void onBindViewHolder(final LanguageViewHolder languageViewHolder, int i) {
        languageViewHolder.languageTextView.setText(languageList.get(i).getName());
        final Language language = languageList.get(i);

        languageViewHolder.languageLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(languageViewHolder.languageCheckBox.isChecked()) {
                    languageViewHolder.languageCheckBox.setChecked(false);
                    language.setChecked(false);
                }
                else {
                    languageViewHolder.languageCheckBox.setChecked(true);
                    language.setChecked(true);
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

    public ArrayList<Language> getLanguageList(){
        return languageList;
    }
}