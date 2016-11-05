package com.example.markwen.easycourse.models.main;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.annotations.PrimaryKey;

/**
 * Created by noahrinehart on 11/5/16.
 */

public class University extends RealmObject {

    @PrimaryKey
    private String id;
    private String name;

    public University() {

    }

    public University(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public static void updateUniversityToRealm(University university, Realm realm) {
        realm.beginTransaction();
        realm.copyToRealmOrUpdate(university);
        realm.commitTransaction();
    }

    public static boolean isUniversityInRealm(University university, Realm realm) {
        RealmResults<University> results = realm.where(University.class)
                .equalTo("id", university.getId())
                .findAll();
        return results.size() != 0;
    }

    public static void deleteUniversityFromRealm(final University university, Realm realm) {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                RealmResults<University> results = realm.where(University.class)
                        .equalTo("id", university.getId())
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
