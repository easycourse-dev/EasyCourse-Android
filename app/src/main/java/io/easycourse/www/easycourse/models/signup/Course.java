package io.easycourse.www.easycourse.models.signup;


public class Course {
    private String id = "";
    private String originCourseId  = "";
    private String name;
    private String title;
    private String description  = "";
    private int creditHours = 0;
    private String universityId  = "";
    private boolean isSelected = false;

    public Course(String name, String title, String id, String universityId) {
        this.name = name;
        this.title = title;
        this.id = id;
        this.universityId = universityId;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOriginCourseId() {
        return originCourseId;
    }

    public void setOriginCourseId(String originCourseId) {
        this.originCourseId = originCourseId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getCreditHours() {
        return creditHours;
    }

    public void setCreditHours(int creditHours) {
        this.creditHours = creditHours;
    }

    public String getUniversityId() {
        return universityId;
    }

    public void setUniversityId(String universityId) {
        this.universityId = universityId;
    }
}
