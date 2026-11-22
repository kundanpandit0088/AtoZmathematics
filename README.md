# AtoZ Mathematics – Android Starter Project

This is the first native Android Studio source-code milestone for the AtoZ Mathematics tuition fee app.

## Included in this starter
- Kotlin + Jetpack Compose Android app
- Hindi / English language toggle for main screens
- Local Room (SQLite) database: student profiles, payment entries, attendance entity
- Student registration (name, phone, class/course, monthly fee, course fee)
- Payment entry with generated receipt reference number
- Payment history and basic collection reports
- Offline-first foundation

## Open in Android Studio
1. Extract this ZIP.
2. Open Android Studio -> Open -> select the `AtoZMathematics` folder.
3. Install Android SDK Platform 35 if Android Studio asks.
4. Use JDK 17.
5. Let Gradle sync finish, then Run on an Android phone or emulator.

## Important: current milestone limitations
This is a starter source project, not a finished production app or installable APK.
- The app stores data locally on this device using Room.
- Firebase cloud sync, secure admin authentication, student photo picker, WhatsApp reminder action, full pending-fee ledger, attendance screen, and printable PDF receipts are not implemented yet.
- Payment records are append-only in this first milestone; they do not yet calculate monthly dues or enforce fee plans.
- The single-admin login must be configured before real student data is entered.

## Next implementation milestone
1. Add Firebase project and `google-services.json`.
2. Add Firebase Authentication for one admin account.
3. Add a sync queue / conflict-safe sync between Room and Firestore.
4. Add photo picker + Firebase Storage.
5. Implement monthly/course fee schedules, partial payments, due calculation, and PDF receipts.
6. Add attendance UI, WhatsApp reminder links, and CSV/PDF reports.

## Firebase setup (next milestone)
Create a Firebase project in the Firebase Console, register Android package:
`in.atozmathematics.feeapp`
Download `google-services.json` into `app/`, then add Firebase BoM, Auth, Firestore and Storage dependencies plus the Google Services Gradle plugin. Do not share the config file publicly.

## Fee management milestone 2
- Monthly fees are calculated per student and billing month.
- Course fees are calculated as one course balance.
- Course fee can be paid in multiple instalments.
- The payment dialog displays the outstanding balance and prevents recording an amount above that balance.
- Reports show current-month monthly dues, course dues, and combined outstanding balance.

Note: monthly charges are based on the student's current monthly fee. A production release should add a dated fee-plan/history table so fee changes do not rewrite historical dues, plus payment edit/reversal controls and Firebase sync.


## Student fee status + WhatsApp reminder milestone 3
- Student Fee Portal screen: student enters the generated Student ID and the registered 10-digit mobile number to view the current fee status.
- Portal shows current-month monthly fee, course fee, paid amount, remaining amount, and payment history/receipt references.
- Admin can open WhatsApp Fee Reminders from the dashboard, see students with outstanding balances, and open a pre-filled bilingual WhatsApp message for each student.
- WhatsApp messages are not sent automatically: the admin reviews and taps Send in WhatsApp.
- Student IDs are visible in the admin Student Directory.

### Important limitations of this milestone
- The portal currently reads the Room database on the same device. It is a local preview/workflow, not yet a remotely accessible student login. A student using another phone cannot see the admin phone's local database.
- To make the portal work for students on their own phones, the next step is Firebase Authentication + Firestore rules/API with a student-specific sign-in and cloud sync. Do not publish this local ID + phone check as production authentication.
- WhatsApp reminder opens a pre-filled chat/link. It requires WhatsApp (or a browser) and admin confirmation; it does not send messages silently or automatically.
- Fee calculations retain the existing v2 limitation: monthly due uses the current month's fee and current student fee value; a dated fee-plan ledger is needed for historical accuracy.
- This is Android Studio source code, not an APK. Gradle build/installation has not been verified in this environment.
