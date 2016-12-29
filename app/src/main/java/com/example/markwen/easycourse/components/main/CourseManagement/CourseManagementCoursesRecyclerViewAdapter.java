package com.example.markwen.easycourse.components.main.CourseManagement;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.markwen.easycourse.R;
import com.example.markwen.easycourse.activities.CourseDetailsAcitivity;
import com.example.markwen.easycourse.models.main.Course;
import com.hanks.library.AnimateCheckBox;

import java.util.ArrayList;

/**
 * Created by markw on 12/26/2016.
 */

public class CourseManagementCoursesRecyclerViewAdapter extends RecyclerView.Adapter<CourseManagementCoursesRecyclerViewAdapter.CourseViewHolder> {

    private ArrayList<Course> coursesList = new ArrayList<>();
    private ArrayList<Course> joinedCourses = new ArrayList<>();
    private boolean showJoined = true;
    private Context context;

    public CourseManagementCoursesRecyclerViewAdapter(Context context, ArrayList<Course> coursesList) {
        this.context = context;
        this.coursesList = coursesList;
    }

    static class CourseViewHolder extends RecyclerView.ViewHolder {
        RelativeLayout courseHolder;
        CardView courseCardView;
        TextView nameTextView;
        TextView titleTextView;
        AnimateCheckBox courseCheckBox;

        CourseViewHolder(View itemView) {
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

    @Override
    public CourseViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.signup_choose_courses_item, viewGroup, false);
        return new CourseViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final CourseViewHolder courseViewHolder, final int i) {
        final Course course;
        if (showJoined && joinedCourses.size() > 0) {
            course = joinedCourses.get(i);
        } else {
            course = coursesList.get(i);
        }

        courseViewHolder.nameTextView.setText(course.getCoursename());
        courseViewHolder.titleTextView.setText(course.getTitle());
        //Fixes weird problems
        courseViewHolder.courseHolder.setOnClickListener(null);

        for (int j = 0; j < joinedCourses.size(); j++) {
            if (joinedCourses.get(j).getId().equals(course.getId())) {
                courseViewHolder.courseCheckBox.setChecked(true);
                break;
            }
        }

        courseViewHolder.courseHolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent courseIntent = new Intent(context, CourseDetailsAcitivity.class);
                courseIntent.putExtra("CourseObj", course);
                context.startActivity(courseIntent);
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

    public void setJoinedCourses(ArrayList<Course> list) {
        this.joinedCourses = list;
    }

    public void showJoinedCourses(boolean show) {
        this.showJoined = show;
        this.notifyDataSetChanged();
    }
}
