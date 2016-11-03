package com.example.markwen.easycourse.components;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.markwen.easycourse.R;
import com.hanks.library.AnimateCheckBox;

import java.util.ArrayList;

/**
 * Created by nisarg on 29/10/16.
 */

public class SignupChooseCoursesAdapter extends RecyclerView.Adapter<SignupChooseCoursesAdapter.CourseViewHolder>{

    private ArrayList<Course> coursesList = new ArrayList<>();

    public SignupChooseCoursesAdapter(ArrayList<Course> coursesList){
        this.coursesList = coursesList;
    }

    public static class CourseViewHolder extends RecyclerView.ViewHolder{
        RelativeLayout courseHolder;
        CardView courseCardView;
        TextView nameTextView;
        TextView titleTextView;
        AnimateCheckBox courseCheckBox;

        CourseViewHolder(View itemView) {
            super(itemView);
            courseHolder = (RelativeLayout)itemView.findViewById(R.id.course_holder_layout);
            courseCardView = (CardView)itemView.findViewById(R.id.course_card_view);
            nameTextView = (TextView)itemView.findViewById(R.id.name_text);
            titleTextView = (TextView)itemView.findViewById(R.id.title_text);
            courseCheckBox = (AnimateCheckBox)itemView.findViewById(R.id.course_check_box);
        }
    }



    @Override
    public CourseViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.signup_choose_courses_item, viewGroup, false);
        CourseViewHolder courseViewHolder = new CourseViewHolder(v);
        return courseViewHolder;
    }

    @Override
    public void onBindViewHolder(final CourseViewHolder courseViewHolder, int i) {
        courseViewHolder.nameTextView.setText(coursesList.get(i).getName());
        courseViewHolder.titleTextView.setText(coursesList.get(i).getTitle());
        final int index = i;
        courseViewHolder.courseHolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e("com.example.easycourse", "Clicked "+ index);
                if(courseViewHolder.courseCheckBox.isChecked())
                    courseViewHolder.courseCheckBox.setChecked(false);
                else
                    courseViewHolder.courseCheckBox.setChecked(true);
            }
        });
    }

    @Override
    public int getItemCount() {
        return coursesList.size();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }
}
