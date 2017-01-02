package io.easycourse.www.easycourse.models.signup;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import java.util.ArrayList;

/**
 * Created by noahrinehart on 11/2/16.
 */

public class UserSetup implements Parcelable {

    private String universityID;
    private String[] languageCodeArray;
    private String[] courseCodeArray;
    private University selectedUniversity;
    private ArrayList<Course> selectedCourses = new ArrayList<>();
    private ArrayList<Language> selectedLanguages = new ArrayList<>();

    public UserSetup() {
    }

    private UserSetup(Parcel in) {
        universityID = in.readString();
        languageCodeArray = new String[in.readInt()];
        in.readStringArray(languageCodeArray);
        courseCodeArray = new String[in.readInt()];
        in.readStringArray(courseCodeArray);
    }

    @Nullable
    public String getUniversityID() {
        return universityID;
    }

    public void setUniversityID(String universityID) {
        if (this.universityID != null && !this.universityID.equals(universityID)) {
            setSelectedCourses(new ArrayList<Course>());
            setCourseCodeArray(new String[0]);
            setSelectedLanguages(new ArrayList<Language>());
            setLanguageCodeArray(new String[0]);
        }
        this.universityID = universityID;
    }

    @Nullable
    public String[] getLanguageCodeArray() {
        return languageCodeArray;
    }

    public void setLanguageCodeArray(String[] languageCodeArray) {
        this.languageCodeArray = languageCodeArray;
    }

    @Nullable
    public String[] getCourseCodeArray() {
        return courseCodeArray;
    }

    public void setCourseCodeArray(String[] courseCodeArray) {
        this.courseCodeArray = courseCodeArray;
    }

    public University getSelectedUniversity() {
        return selectedUniversity;
    }

    public void setSelectedUniversity(University univ) {
        this.selectedUniversity = univ;
    }

    public ArrayList<Course> getSelectedCourses() {
        return selectedCourses;
    }

    public void setSelectedCourses(ArrayList<Course> courses) {
        this.selectedCourses = courses;
    }

    public ArrayList<Language> getSelectedLanguages() {
        return selectedLanguages;
    }

    public void setSelectedLanguages(ArrayList<Language> languages) {
        this.selectedLanguages = languages;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(universityID);
        dest.writeInt(languageCodeArray.length);
        dest.writeStringArray(languageCodeArray);
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