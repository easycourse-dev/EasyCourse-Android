package io.easycourse.www.easycourse.models.main;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import io.easycourse.www.easycourse.utils.ListsUtils;
import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.annotations.PrimaryKey;

import static io.easycourse.www.easycourse.utils.JSONUtils.checkIfJsonExists;
import static io.easycourse.www.easycourse.utils.ListsUtils.isCourseJoined;
import static io.easycourse.www.easycourse.utils.ListsUtils.isRoomJoined;

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

    public static void syncAddCourse(JSONArray jsonArray, Realm realm) {
        String JSONId;
        JSONObject temp;
        RealmResults<Course> realmList = realm.where(Course.class).findAll();
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                temp = jsonArray.getJSONObject(i);
                JSONId = temp.getString("_id");
                if (!ListsUtils.isCourseJoined(realmList, JSONId)) {
                    updateCourseToRealm(new Course(
                            JSONId,
                            (String) checkIfJsonExists(temp, "name", null),
                            (String) checkIfJsonExists(temp, "title", null),
                            (String) checkIfJsonExists(temp, "description", null),
                            Integer.parseInt((String) checkIfJsonExists(temp, "creditHours", "0")),
                            (String) checkIfJsonExists(temp, "university", null)
                    ), realm);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public static void syncRemoveCourse(JSONArray jsonArray, Realm realm) {
        String realmId;
        Course realmTemp;
        JSONObject temp;
        RealmResults<Course> realmList = realm.where(Course.class).findAll();
        for (int i = 0; i < realmList.size(); i++) {
            try {
                realmTemp = realmList.get(i);
                realmId = realmTemp.getId();
                if (!ListsUtils.isRoomJoined(jsonArray, realmId)) {
                    temp = jsonArray.getJSONObject(i);
                    deleteCourseFromRealm(new Course(
                            realmId,
                            (String) checkIfJsonExists(temp, "name", null),
                            (String) checkIfJsonExists(temp, "title", null),
                            (String) checkIfJsonExists(temp, "description", null),
                            Integer.parseInt((String) checkIfJsonExists(temp, "creditHours", "0")),
                            (String) checkIfJsonExists(temp, "university", null)
                    ), realm);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
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
