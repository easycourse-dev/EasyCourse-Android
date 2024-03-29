package io.easycourse.www.easycourse.components.signup;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import io.easycourse.www.easycourse.R;
import io.easycourse.www.easycourse.models.signup.Course;
import io.easycourse.www.easycourse.utils.ListsUtils;
import com.hanks.library.AnimateCheckBox;

import java.util.ArrayList;

/**
 * Created by nisarg on 29/10/16.
 */

public class SignupChooseCoursesAdapter extends RecyclerView.Adapter<SignupChooseCoursesAdapter.CourseViewHolder> {

    private static final String TAG = "SignupChooseCoursesAdap";

    private ArrayList<Course> coursesList = new ArrayList<>();
    private ArrayList<Course> checkedCourseList = new ArrayList<>();

    public SignupChooseCoursesAdapter(ArrayList<Course> coursesList) {
        this.coursesList = coursesList;
        // Check if there are checked courses in the inserted list
        for (int i = 0; i < coursesList.size(); i++) {
            if (coursesList.get(i).isSelected()) {
                checkedCourseList.add(coursesList.get(i));
            }
        }
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
        final Course course = coursesList.get(i);
        courseViewHolder.nameTextView.setText(course.getName());
        courseViewHolder.titleTextView.setText(course.getTitle());
        // Fixes weird problems
        courseViewHolder.courseHolder.setOnClickListener(null);
        // Check if this course is checked before
        if (isCourseChecked(checkedCourseList, course)) {
            courseViewHolder.courseCheckBox.setChecked(true);
            course.setSelected(true);
        } else {
            courseViewHolder.courseCheckBox.setChecked(false);
            course.setSelected(false);
        }

        courseViewHolder.courseHolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!courseViewHolder.nameTextView.getText().equals(course.getName())) return;
                if (course.isSelected()) {
                    course.setSelected(false);
                    courseViewHolder.courseCheckBox.setChecked(false);
                    checkedCourseList.remove(ListsUtils.isCourseJoined(checkedCourseList, course.getId()));
                } else {
                    course.setSelected(true);
                    courseViewHolder.courseCheckBox.setChecked(true);
                    checkedCourseList.add(course);
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


    public ArrayList<Course> getCheckedCourseList() {
        return checkedCourseList;
    }

    // Check if the course is in the checked course list
    private boolean isCourseChecked(ArrayList<Course> list, Course course) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getId().equals(course.getId())) {
                return true;
            }
        }
        return false;
    }
}
