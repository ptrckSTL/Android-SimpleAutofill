# **Description**

This is a sample project for **Android's Autofill Framework**. This project is compatible with parsing native applications, in-app browsers and external browsers. However, some external browser still having challenges in parsing content.

## **Requirements**

Project built with:
- Android Studio: **Koala**
- Gradle: **8.7**
- AGP: **8.5.2**
- Kotlin: **1.9.0**
- Java: **17**
- Min SDK: **26**
- Compile SDK: **34**

## Project Explanation

- [auto_fill_service.xml](https://github.com/amirraza/Android-AutofillFramework/blob/main/app/src/main/res/xml/auto_fill_service.xml)
  - Defines the autofill service settings activity
    
- [ParsedStructure.kt](https://github.com/amirraza/Android-AutofillFramework/blob/main/app/src/main/java/dev/amirraza/autofill/model/ParsedStructure.kt)
  - A model class used to keep the [AutofillId](https://developer.android.com/reference/android/view/autofill/AutofillId) that used to fill the fields.
    - ParsedStructure class has [identifier](https://github.com/amirraza/Android-AutofillFramework/blob/51f47a27564d36b9fc95952acebedf096adba67a/app/src/main/java/dev/amirraza/autofill/model/ParsedStructure.kt#L9) that is used to insert records uniquely into db(Room) and used to show the specific Autofill hints when visiting client's applications/browsers
      
- [MyAutoFillService.kt](https://github.com/amirraza/Android-AutofillFramework/blob/main/app/src/main/java/dev/amirraza/autofill/MyAutoFillService.kt)
  - The autofill service uses to parse the client's applications (native apps or browsers) and extract the [AutofillId](https://developer.android.com/reference/android/view/autofill/AutofillId) for name, username, password fields and keep their references to **ParsedStructure**
 
- [AutofillLockedActivity.kt](https://github.com/amirraza/Android-AutofillFramework/blob/main/app/src/main/java/dev/amirraza/autofill/AutofillLockedActivity.kt)
  - This is a mocked master password activity, that will be used to show the locked Autofill hints. This will cover the scenario where the autofill hint won't show if the password manager application is locked.



