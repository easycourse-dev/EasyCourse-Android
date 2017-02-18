package io.easycourse.www.easycourse.components.main.NewRoom;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import io.easycourse.www.easycourse.R;
import io.easycourse.www.easycourse.models.main.Course;


public class NewRoomCoursesAdapter extends BaseAdapter {

    private ArrayList<Course> coursesList;
    private Course selectedCourse;
    private LayoutInflater inflater;

    public NewRoomCoursesAdapter(@NonNull Context context, ArrayList<Course> courses) {
        this.coursesList = courses;
        inflater = (LayoutInflater.from(context));
    }

    public Course getSelectedCourse() {
        return selectedCourse;
    }

    public void setSelectedCourse(int i) {
        selectedCourse = coursesList.get(i);
    }

    @Override
    public int getCount() {
        return coursesList.size();
    }

    @Override
    public Object getItem(int i) {
        return coursesList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        view = inflater.inflate(R.layout.activity_new_room_courses_item, null);
        TextView courseName = (TextView) view.findViewById(R.id.new_room_course_course_text);
        TextView titleName = (TextView) view.findViewById(R.id.new_room_course_title_text);
        courseName.setText(coursesList.get(i).getCoursename());
        titleName.setText(coursesList.get(i).getTitle());
        return view;
    }
}
