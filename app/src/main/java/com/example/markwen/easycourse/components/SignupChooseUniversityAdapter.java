package com.example.markwen.easycourse.components;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.markwen.easycourse.R;
import com.example.markwen.easycourse.models.University;
import com.hanks.library.AnimateCheckBox;

import java.util.ArrayList;

/**
 * Created by nisarg on 29/10/16.
 */

public class SignupChooseUniversityAdapter extends RecyclerView.Adapter<SignupChooseUniversityAdapter.UniversityViewHolder> {

    private ArrayList<University> universityList = new ArrayList<>();

    public SignupChooseUniversityAdapter(ArrayList<University> universityList) {
        this.universityList = universityList;
    }

    public static class UniversityViewHolder extends RecyclerView.ViewHolder {
        CardView uniCardView;
        TextView uniTextView;
        AnimateCheckBox uniCheckBox;
        RelativeLayout uniLayout;

        UniversityViewHolder(View itemView) {
            super(itemView);
            uniCardView = (CardView) itemView.findViewById(R.id.cardViewSingleItem);
            uniTextView = (TextView) itemView.findViewById(R.id.textViewSingleItem);
            uniCheckBox = (AnimateCheckBox) itemView.findViewById(R.id.checkBoxSingleItem);
            uniLayout = (RelativeLayout) itemView.findViewById(R.id.relativeLayoutSingleItem);
            uniCheckBox.setEnabled(false);
            uniCheckBox.setClickable(false);
        }
    }


    @Override
    public UniversityViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.signup_choose_single_item, viewGroup, false);
        UniversityViewHolder uniViewHolder = new UniversityViewHolder(v);
        return uniViewHolder;
    }

    @Override
    public void onBindViewHolder(final UniversityViewHolder uniViewHolder, int i) {
        final University university = universityList.get(i);
        uniViewHolder.uniTextView.setText(universityList.get(i).getName());
        if (university.isSelected())
            uniViewHolder.uniCheckBox.setChecked(true);

        uniViewHolder.uniLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Uncheck everything else
                for (University uni : universityList) {
                    if (uni != university)
                        uni.setSelected(false);
                }
                if (university.isSelected()) {
                    uniViewHolder.uniCheckBox.setChecked(false);
                    university.setSelected(false);
                } else {
                    uniViewHolder.uniCheckBox.setChecked(true);
                    university.setSelected(true);
                }

            }
        });
    }

    @Override
    public int getItemCount() {
        return universityList.size();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    public ArrayList<University> getUniversityList() {
        return universityList;
    }
}
