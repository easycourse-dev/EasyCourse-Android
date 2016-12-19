package com.example.markwen.easycourse.components.main;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.markwen.easycourse.R;
import com.example.markwen.easycourse.models.main.Course;
import com.hanks.library.AnimateCheckBox;

import io.realm.RealmRecyclerViewAdapter;
import io.realm.RealmResults;

/**
 * Created by markw on 12/19/2016.
 */

public class NewRoomRecyclerViewAdapter extends RealmRecyclerViewAdapter<Course, RecyclerView.ViewHolder> {

    private RealmResults<Course> coursesList;
    private Course selectedCourse;
    private AnimateCheckBox lastChecked = null;

    public NewRoomRecyclerViewAdapter(@NonNull Context context, RealmResults<Course> courses) {
        super(context, courses, true);
        this.coursesList = courses;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        return new NewRoomViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.signup_choose_courses_item, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int i) {
        final Course course = coursesList.get(i);
        final NewRoomRecyclerViewAdapter.NewRoomViewHolder courseViewHolder = (NewRoomRecyclerViewAdapter.NewRoomViewHolder) holder;
        courseViewHolder.nameTextView.setText(course.getCoursename());
        courseViewHolder.titleTextView.setText(course.getTitle());
        courseViewHolder.courseHolder.setOnClickListener(null);
        courseViewHolder.courseCheckBox.setChecked(false);

        if(lastChecked == null)
        {
            lastChecked = courseViewHolder.courseCheckBox;
        }

        courseViewHolder.courseHolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(courseViewHolder.courseCheckBox.isChecked()) {
                    if(lastChecked != null) {
                        lastChecked.setChecked(false);
                    }
                    lastChecked = null;
                    selectedCourse = null;
                    courseViewHolder.courseCheckBox.setChecked(false);
                } else {
                    selectedCourse = course;
                    courseViewHolder.courseCheckBox.setChecked(true);
                    if (lastChecked != null) {
                        lastChecked.setChecked(false);
                    }
                    lastChecked = courseViewHolder.courseCheckBox;
                }
            }
        });
    }

    public Course getSelectedCourse() {
        return selectedCourse;
    }

    class NewRoomViewHolder extends RecyclerView.ViewHolder {
        RelativeLayout courseHolder;
        CardView courseCardView;
        TextView nameTextView;
        TextView titleTextView;
        AnimateCheckBox courseCheckBox;

        NewRoomViewHolder(View itemView) {
            super(itemView);
            courseHolder = (RelativeLayout) itemView.findViewById(R.id.course_holder_layout);
            courseCardView = (CardView) itemView.findViewById(R.id.course_card_view);
            nameTextView = (TextView) itemView.findViewById(R.id.name_text);
            titleTextView = (TextView) itemView.findViewById(R.id.title_text);
            courseCheckBox = (AnimateCheckBox) itemView.findViewById(R.id.course_check_box);
            courseCheckBox.setClickable(false);
            courseCheckBox.setEnabled(false);
            this.setIsRecyclable(false);
        }
    }
}
