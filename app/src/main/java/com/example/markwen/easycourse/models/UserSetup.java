package com.example.markwen.easycourse.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

/**
 * Created by noahrinehart on 11/2/16.
 */

public class UserSetup implements Parcelable {

    private String universityID;
    private int[] languageCodeArray;
    private String[] courseCodeArray;

    public UserSetup() {
    }

    private UserSetup(Parcel in) {
        universityID = in.readString();
        languageCodeArray = new int[in.readInt()];
        in.readIntArray(languageCodeArray);
        courseCodeArray = new String[in.readInt()];
        in.readStringArray(courseCodeArray);
    }

    @Nullable
    public String getUniversityID() {
        return universityID;
    }

    public void setUniversityID(String universityID) {
        this.universityID = universityID;
    }

    @Nullable
    public int[] getLanguageCodeArray() {
        return languageCodeArray;
    }

    public void setLanguageCodeArray(int[] languageCodeArray) {
        this.languageCodeArray = languageCodeArray;
    }

    @Nullable
    public String[] getCourseCodeArray() {
        return courseCodeArray;
    }

    public void setCourseCodeArray(String[] courseCodeArray) {
        this.courseCodeArray = courseCodeArray;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(universityID);
        dest.writeInt(languageCodeArray.length);
        dest.writeIntArray(languageCodeArray);
        dest.writeInt(courseCodeArray.length);
        dest.writeStringArray(courseCodeArray);
    }
    public static final Parcelable.Creator<UserSetup> CREATOR
            = new Parcelable.Creator<UserSetup>() {
        @Override
        public UserSetup createFromParcel(Parcel in) {
            return new UserSetup(in);
            }

        @Override
            public UserSetup[] newArray(int size) {
            return new UserSetup[size];
        }
    };
}