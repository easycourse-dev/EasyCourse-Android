package com.example.markwen.easycourse.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by noahrinehart on 11/2/16.
 */

public class UserSetup {

    private String universityID;
    private int[] languageCodeArray;
    private String[] courseCodeArray;

    public UserSetup() {
    }


    public String getUniversityID() {
        return universityID;
    }

    public void setUniversityID(String universityID) {
        this.universityID = universityID;
    }

    public int[] getLanguageCodeArray() {
        return languageCodeArray;
    }

    public void setLanguageCodeArray(int[] languageCodeArray) {
        this.languageCodeArray = languageCodeArray;
    }

    public String[] getCourseCodeArray() {
        return courseCodeArray;
    }

    public void setCourseCodeArray(String[] courseCodeArray) {
        this.courseCodeArray = courseCodeArray;
    }

}
