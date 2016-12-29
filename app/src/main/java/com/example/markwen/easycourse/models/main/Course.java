package com.example.markwen.easycourse.models.main;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.annotations.PrimaryKey;

/**
 * Created by noahrinehart on 11/5/16.
 */

public class Course extends RealmObject {

    @PrimaryKey
    private String id;
    private String coursename;
    private String title;
    private String courseDescription;
    private int creditHours;
    private String universityID;

    public Course() {

    }

    public Course(String name, String title, String id) {
        this.coursename = name;
        this.title = title;
        this.id = id;
    }

    public Course(String id, String coursename, String title, String courseDescription, int creditHours, String universityID) {
        this.id = id;
        this.coursename = coursename;
        this.title = title;
        this.courseDescription = courseDescription;
        this.creditHours = creditHours;
        this.universityID = universityID;
    }

    public static Course getCourseById (String id, Realm realm) {
        RealmResults<Course> results = realm.where(Course.class).equalTo("id", id).findAll();
        return results.first();
    }

    public static void updateCourseToRealm(Course course, Realm realm) {
        realm.beginTransaction();
        realm.copyToRealmOrUpdate(course);
        realm.commitTransaction();
    }

    public static boolean isCourseInRealm(Course course, Realm realm) {
        RealmResults<Course> results = realm.where(Course.class)
                .equalTo("id", course.getId())
                .findAll();
        return results.size() != 0;
    }

    public static void deleteCourseFromRealm(final Course course, Realm realm) {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                RealmResults<Course> results = realm.where(Course.class)
                        .equalTo("id", course.getId())
                        .findAll();
                results.deleteAllFromRealm();
            }
        });
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCoursename() {
        return coursename;
    }

    public void setCoursename(String coursename) {
        this.coursename = coursename;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCourseDescription() {
        return courseDescription;
    }

    public void setCourseDescription(String courseDescription) {
        this.courseDescription = courseDescription;
    }

    public int getCreditHours() {
        return creditHours;
    }

    public void setCreditHours(int creditHours) {
        this.creditHours = creditHours;
    }

    public String getUniversityID() {
        return universityID;
    }

    public void setUniversityID(String universityID) {
        this.universityID = universityID;
    }
}
