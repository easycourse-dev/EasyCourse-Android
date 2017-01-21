# EasyCourse-Android
EasyCourse Android Client

---

## Install and Running

```
git clone https://github.com/easycourse-dev/EasyCourse-Android.git
```
- Open project using Android Studio


## Features

- [Android Studio](https://developer.android.com/studio/index.html)
- [Realm](https://realm.io/docs/java/latest/)
- [Socket.io](http://socket.io/)
- [Android Asynchronous Http Client](http://loopj.com/android-async-http/)
- [Android View Animations](https://github.com/daimajia/AndroidViewAnimations)


## Coding Standards

1. #### File Directory

  ###### Java:
    - **'src/main/java/activities':** android activities
    - **'src/main/java/components':** java files related to small components
    - **'src/main/java/fragments':** fragment files
    - **'src/main/java/models':** Realm models
    - **'src/main/java/services':** services such as notificiations/wakeups/eventbus
    - **'src/main/java/utils':** singleton functions like API calls


2. #### Naming Standards

   Be as detailed as possible, always include type and purpose
   - id: editTextPassword
   - java variable: emailEditText

3. #### Comments

   Have at least one line of comment to describe each function, comments to describe each step within functions would be recommended

## Git Development

- "develop" branch is protected, only administrators are authorized to write
- When developing new features, create new "feature/FeatureName" branch from "develop"
- Always pull the latest "develop" branch and merge into your own feature branch before you start your work and when you find out new commits appear in "develop" branch
