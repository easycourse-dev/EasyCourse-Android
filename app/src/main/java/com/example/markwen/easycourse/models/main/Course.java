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
    private byte[] coursePicture;
    private String coursePictureUrl;
    private String title;
    private String courseDescription;
    private int creditHours;
    private String universityID;

    public Course(String id, String coursename, byte[] coursePicture, String coursePictureUrl, String title, String courseDescription, int creditHours, String universityID) {
        this.id = id;
        this.coursename = coursename;
        this.coursePicture = coursePicture;
        this.coursePictureUrl = coursePictureUrl;
        this.title = title;
        this.courseDescription = courseDescription;
        this.creditHours = creditHours;
        this.universityID = universityID;
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

    public byte[] getCoursePicture() {
        return coursePicture;
    }

    public void setCoursePicture(byte[] coursePicture) {
        this.coursePicture = coursePicture;
    }

    public String getCoursePictureUrl() {
        return coursePictureUrl;
    }

    public void setCoursePictureUrl(String coursePictureUrl) {
        this.coursePictureUrl = coursePictureUrl;
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
