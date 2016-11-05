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
import com.example.markwen.easycourse.models.signup.Course;
import com.hanks.library.AnimateCheckBox;

import java.util.ArrayList;

/**
 * Created by nisarg on 29/10/16.
 */

public class SignupChooseCoursesAdapter extends RecyclerView.Adapter<SignupChooseCoursesAdapter.CourseViewHolder> {

    private static final String TAG = "SignupChooseCoursesAdap";

    private ArrayList<Course> coursesList = new ArrayList<>();
    private ArrayList<Course> checkedCourseList = new ArrayList<>();

    private TextView checkedCoursesTextView;

    public SignupChooseCoursesAdapter(ArrayList<Course> coursesList, TextView checkedCoursesTextView) {
        this.coursesList = coursesList;
        this.checkedCoursesTextView = checkedCoursesTextView;
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
        CourseViewHolder courseViewHolder = new CourseViewHolder(v);
        return courseViewHolder;
    }


    @Override
    public void onBindViewHolder(final CourseViewHolder courseViewHolder, final int i) {
        final Course course = coursesList.get(i);
        courseViewHolder.nameTextView.setText(course.getName());
        courseViewHolder.titleTextView.setText(course.getTitle());
        //Fixes weird problems
        courseViewHolder.courseHolder.setOnClickListener(null);
        courseViewHolder.courseCheckBox.setChecked(course.isSelected());

        courseViewHolder.courseHolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e("com.example.easycourse", "Clicked " + courseViewHolder.getAdapterPosition());
                if (!courseViewHolder.nameTextView.getText().equals(course.getName())) return;
                if (course.isSelected()) {
                    course.setSelected(false);
                    courseViewHolder.courseCheckBox.setChecked(course.isSelected());
                    checkedCourseList.remove(course);
                    changeTextView();


                } else {
                    course.setSelected(true);
                    courseViewHolder.courseCheckBox.setChecked(course.isSelected());
                    checkedCourseList.add(course);
                    changeTextView();
                }
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

    private void changeTextView() {
        if (checkedCourseList.size() == 0) {
            checkedCoursesTextView.setText("");
        } else if (checkedCourseList.size() == 1) {
            checkedCoursesTextView.setText(checkedCourseList.get(0).getName());
        } else {
            int i;
            String newText = "";
            for (i = 0; i < checkedCourseList.size() - 1; i++) {
                newText += checkedCourseList.get(i).getName() + ", ";
            }
            newText += checkedCourseList.get(i).getName();
            checkedCoursesTextView.setText(newText);
        }
    }

    public ArrayList<Course> getCheckedCourseList() {
        return checkedCourseList;
    }
}
