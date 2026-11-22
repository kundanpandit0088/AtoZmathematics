# AtoZ Mathematics APK – Cloud Build Guide (Hindi)

Is project ko apne laptop par Android Studio install kiye bina GitHub Actions se build kiya ja sakta hai.

## Steps
1. GitHub.com par free account banayein / login karein.
2. New repository banayein, naam `AtoZMathematics` rakhein. Public ya Private dono ho sakti hai.
3. Is ZIP ko extract karein. `AtoZMathematicsAndroid` folder ke andar ke saare files/folders (including hidden `.github`) repository ke root mein upload karein.
4. GitHub repository mein **Actions** tab kholein. Agar prompt aaye to **I understand my workflows, go ahead and enable them** select karein.
5. `Build AtoZ Mathematics APK` workflow select karke **Run workflow** dabayein. Ya `main` branch par push karne se workflow automatic chalega.
6. Build successful hone par workflow run ke page ke neeche **Artifacts** section mein `AtoZ-Mathematics-debug-apk` download karein.
7. ZIP extract karein. `app-debug.apk` ko Android phone mein bhejkar install karein. Phone par zarurat pade to browser/file manager ke liye **Install unknown apps** permission allow karein.

## Dhyan dein
- Yeh cloud-build workflow hai; APK isi waqt is package mein shamil nahi hai. GitHub Actions ko build chalana hoga.
- Yeh debug APK hai, Play Store release/signing ke liye nahi.
- Source project ka Student Fee Portal abhi local Room database par hai. Alag-alag students ke phones se online status dekhne ke liye Firebase/cloud backend aur secure authentication abhi banana/configure karna baaki hai.
- Build successful hone ki guarantee nahi di ja sakti jab tak GitHub Actions mein actual compile na ho. Build error aaye to error log share karein.
