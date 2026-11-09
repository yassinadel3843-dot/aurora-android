# TECHNICAL_NOTES.md — المرجع التقني لمشروع Aurora

> أُعد من ملفات المشروع الفعلية + سجل جلسة OpenCode + تجارب بناء واختبار فعلية.
> آخر تحديث: 2026-11-08.

## 1. البطاقة الأساسية

- اسم التطبيق: Aurora (أورورا).
- Application ID: `com.aurora.workspace`.
- versionCode = 1، versionName = "1.0.0".
- اللغة: Kotlin (بدون plugin منفصل — AGP 9 يدمجه).
- minSdk = 21، targetSdk = 36، compileSdk = 37، buildToolsVersion = "36.0.0".
- الحد الأدنى للإصدار: Android 5.0 Lollipop.

## 2. الأدوات والإصدارات

- AGP 9.5.1 في `build.gradle.kts` الجذري.
- Gradle 9.6.0 (Wrapper داخل المشروع، ونسخة محلية ثابتة في
  `..\..\.tools\gradle-9.6.0` خارج مجلد `Aurora`).
- JDK: من Android Studio (JBR):
  `C:\Program Files\Android\Android Studio\jbr`.
- SDK: `C:\Users\yassin adel\AppData\Local\Android\Sdk` (مُثبَّت في
  `local.properties`).
- ADB: `$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe`.
- محاكى الاختبار: AVD اسمه `Medium_Phone_API_37.0` (شاشة 1080x2400،
  density 420).

## 3. الاعتماديات

- `androidx.core:core-ktx:1.13.1`
- `androidx.appcompat:appcompat:1.7.0`
- `com.google.android.material:material:1.12.0`
- `androidx.recyclerview:recyclerview:1.3.2`
- `androidx.fragment:fragment-ktx:1.8.5`
- `junit:junit:4.13.2` (اختبارات الوحدة فقط)

وضوابط مهمة في `app/build.gradle.kts`:

- `buildFeatures { viewBinding = true }`.
- `vectorDrawables.useSupportLibrary = true` (لعمل الأيقونات vector على API 21).
- `packaging.resources.excludes` على `META-INF/*.kotlin_module` و
  `META-INF/DEPENDENCIES`.
- إعداد `release` يوقّع تلقائيًا بـ **debug keystore**
  (من `~/.android/debug.keystore`) — مناسب للتجربة لا للنشر.
- لا Compose، لا Room، لا Firebase، لا أي SDK إضافي.

## 4. التحذير الأهم في البناء

**لا تُضف `org.jetbrains.kotlin.android` إلى plugins — سيكسر البناء.**
AGP 9 يدمج Kotlin داخليًا. جرّبنا إضافته فعليًا وفشل، والقرار المسجل
(DECISIONS.md §1-3): Kotlin بدون plugin منفصل.

أيضًا `gradle.properties` يحتوي `android.suppressUnsupportedCompileSdk=37`
لأن compileSdk 37 أحدث من شهادة AGP المسجلة — لا تحذفه.

## 5. بناء المشروع

من PowerShell:

```powershell
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
$env:ANDROID_HOME = "$env:LOCALAPPDATA\Android\Sdk"
& "C:\Users\yassin adel\Documents\Default Project\.tools\gradle-9.6.0\bin\gradle.bat" `
  -p "C:\Users\yassin adel\Documents\Default Project\Aurora" :app:assembleDebug --console=plain
```

- نفس الأمر يعمل أيضًا عبر `gradlew.bat` داخل مجلد `Aurora`.
- الناتج: `Aurora\app\build\outputs\apk\debug\app-debug.apk`.
- آخر بناء ناجح: 2026-11-08، حجم APK 6,147,109 بايت (بعد دعم الشاشات).
- لفحص الـ lint: `:app:lintDebug` (نجح سابقًا بـ 0 errors).

## 6. التثبيت والتشغيل على المحاكي

```powershell
$adb = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"
& $adb devices          # تأكد أن AVD ظاهر
& $adb install -r "C:\Users\yassin adel\Documents\Default Project\Aurora\app\build\outputs\apk\debug\app-debug.apk"
& $adb shell monkey -p com.aurora.workspace -c android.intent.category.LAUNCHER 1
```

ملاحظة مفيدة: `MainActivity` غير `exported`، فلا يمكن تشغيله بـ
`am start com.aurora.workspace/.ui.MainActivity` من shell — استخدم `monkey`
أو الشاشة الرئيسية للجهاز.

## 7. بنية الملفات (فروع مهمة فقط)

```
Aurora/
├─ settings.gradle.kts         # مشروع واحد :app
├─ build.gradle.kts            # AGP 9.5.1
├─ gradle.properties           # إعدادات Gradle + suppress compileSdk 37
├─ local.properties            # sdk.dir
├─ gradlew / gradlew.bat       # Wrapper 9.6.0
├─ README.md / .gitignore
└─ app/
   ├─ build.gradle.kts
   └─ src/main/
      ├─ AndroidManifest.xml
      ├─ java/com/aurora/workspace/
      │  ├─ AuroraApp.kt         (Application: لغة + Store.init)
      │  ├─ MainActivity.kt      (هيكل التابات الأربعة)
      │  ├─ data/                Models.kt, SeedData.kt, Store.kt
      │  ├─ theme/               LocaleHelper.kt, ThemeManager.kt
      │  ├─ ui/                  AuroraActivity.kt, AuthActivity.kt,
      │  │                       ProjectDetailActivity.kt, ChatActivity.kt,
      │  │                       home/, projects/, chat/, profile/, adapters/
      │  ├─ util/                ThemeExt.kt, GoogleAccountHelper.kt, DateText.kt,
      │  │                       ScreenFit.kt (تكييف الشاشات: عمود مركزي + grid),
      │  │                       Priority.kt (ألوان/تسميات الأولوية),
      │  │                       NotificationHelper.kt (قناة + إذن + إشعارات)
      │  └─ widgets/             AuroraBackgroundView, GlassCard,
      │                          ProgressRingView, AvatarView
      └─ res/
         ├─ layout/   (15 layout: 4 شاشات + عناصر + حواران)
         ├─ values/   attrs.xml, colors.xml, strings.xml, themes.xml
         ├─ values-ar/strings.xml  (ترجمة عربية كاملة)
         ├─ drawable/ (~40 شكل، زجاج وأيقونات vector)
         └─ mipmap-*/ (أيقونة launcher: adaptive + PNG بكل كثافات)
```

يوجد `app/src/test/java/com/aurora/workspace/` بثمانية اختبارات وحدة
(`DateTextTest` + `ModelsTest`). لا يوجد `app/src/androidTest/` بعد.

## 8. القواعد المعمارية المسجّلة

1. `AuroraActivity` أساس كل الشاشات: يطبق الثيم من `ThemeManager`، واللغة
   عبر `LocaleHelper.wrap` في `attachBaseContext`، وedge-to-edge مع
   احترام insets لـ `safeArea` و`ime`.
2. `AuroraApp.attachBaseContext` يستدعي `LocaleHelper.wrap` أيضًا — لذلك أي
   قراءة تفضيلات داخل Application تمر بـ `applicationContext ?: context`
   لتجنب كراش أول إقلاع (خطأ سابق فعلي).
3. `Store.kt` مصدر بيانات وحيد، حفظ JSON في SharedPreferences، إعادة بذر
   (re-seed) عند غياب البيانات فقط. اسم ملف التفضيلات `aurora_store`.
4. `LocaleHelper` يخزّن اللغة في ملف تفضيلات اسمه `aurora_ui` (المفتاح `language`)،
   وقيمها: `system` / `en` / `ar`. التبديل يستعمل `restartApp()`.
5. `ThemeManager` يخزن الثيم في نفس ملف تفضيلات `aurora_ui` (المفتاح `theme`)،
   القيم: `night` / `glass` / `day` / `emerald` / `sunset`.
6. `DateText.due(context, raw)` هو المنفّذ الوحيد لعرض تاريخ الاستحقاق —
   لا تُعرض القيمة الخام من `dueLabel` مباشرة.
7. كل نص UI في `strings.xml` + `values-ar/strings.xml`. لا نص عربي داخل الكود.
8. RTL: التخطيطات تستخدم start/end فقط (لا left/right)، و`supportsRtl=true`
   في Manifest.

## 9. كيف تختبر UI يدويًا (روتين مثبت)

```powershell
$adb = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"
# حالة الشاشة النشطة
& $adb shell dumpsys activity activities | Select-String topResumedActivity
# هيكل الواجهة الحالية (XML) — تُستخرج إلى جهاز الكمبيوتر وتُقرأ كملف
& $adb shell uiautomator dump /sdcard/ui.xml
& $adb pull /sdcard/ui.xml "C:\Users\yassin adel\AppData\Local\Temp\opencode\ui.xml"
# فحص الكراشات
& $adb logcat -d | Select-String "FATAL EXCEPTION" -Context 0,8
# لقطة شاشة (بالطريقة الصحيحة: لا تستخدم إعادة توجيه > في PowerShell)
& $adb shell screencap -p /sdcard/shot.png
& $adb pull /sdcard/shot.png "C:\Users\yassin adel\AppData\Local\Temp\opencode\shot.png"
```

- التحقق بالنقر عبر `input tap X Y` يعمل — لكن اقرأ الإحداثيات دائمًا
  من `uiautomator` قبل النقر (الشاشة 1080x2400).
- لقراءة النصوص العربية داخل dumps: اقرأ الملف كـ UTF-8 مباشرة،
  ولا تعتمد على طباعة الكونسول (تظهر `?`).
- **تنبيه مهم في إدخال النص:** `adb shell input text "A B C"` يقبل أول كلمة
  فقط (الـ shell يقسّم على المسافات). استخدم `%s` مكان المسافة، مثلًا
  `input text "Write%slaunch%schecklist"`. ولتنظيف حقل: `keyevent 123`
  (END) ثم تكرار `keyevent 67` (DEL).
- مفاتيح الأزرار داخل الحوارات تتحرك عندما تفتح لوحة المفاتيح: خذ
  `uiautomator dump` بعد فتحها واقرأ الإحداثيات الجديدة قبل الضغط
  (لا تستخدم BACK للإخفاء — قد يغلق الحوار نفسه).

## 10. اختبار مقاسات الشاشات بمحاكاة `wm size/density` (طريقة مجرَّبة 2026-11-08)

لا يوجد AVD تابلت على الجهاز (image واحد فقط: `android-37.0/google_apis_playstore/x86_64`)،
لكن كل بنية التطبيق تتجاوب تلقائيًا من `configuration.screenWidthDp`، فلا
حاجة لبناء AVD جديد: نغيّر مقاس/كثافة المحاكي الحالي مؤقتًا بـ ADB:

```powershell
# تابلت محاكى (≈ 1066dp عرضًا):
& $adb shell wm size 1600x2560
& $adb shell wm density 240
# هاتف أفقي (≈ 1200dp عرضًا):
& $adb shell wm size 2400x1080
# هاتف صغير (≈ 308dp عرضًا):
& $adb shell wm density 560
# الرجوع دائمًا بعد الاختبار:
& $adb shell wm size reset
& $adb shell wm density reset
```

- بعد تغيير `wm`: اجعل `am force-stop com.aurora.workspace` ثم أعد التشغيل
  بـ `monkey` حتى يعاد بناء الواجهات بالمقاس الجديد.
- تابع الروتين المعتاد (§9): `screencap` + فحص `FATAL EXCEPTION` بعد كل مقاس.
- انتبه: الإحداثيات تتغير مع كل مقاس — اقرأ `uiautomator dump` جديدًا قبل
  كل `input tap`.
- القرار السلوكي الكامل (متى يتفعّل المحتوى المركزي وكم عمودًا للقائمة)
  في `DECISIONS.md` §2-7 وكود `util/ScreenFit.kt`.

## 11. المشكلات التقنية المعروفة والحلول المجرَّبة

1. **كراش أول إقلاع** — قراءة SharedPreferences داخل
   `Application.attachBaseContext` كانت تفشل لأن `applicationContext` غير
   مرفوع بعد. الحل: `(context.applicationContext ?: context)` في
   `LocaleHelper.prefs()` و`ThemeManager.prefs()`.
2. **"1970" الزائفة في التاريخ** — تاريخ بلا سنة كان يُفسَّر عام 1970؛
   عدّلنا `DateText.kt` ليضيف السنة فقط إذا كانت القيمة محتوية على سنة
   صريحة مختلفة عن السنة الحالية.
3. **نافذة Google/GMS على المحاكي** — عندما لا يوجد حساب Google على الجهاز
   يفتح النظام تسجيل حساب في متصفح GMS لا يمكن إكماله. الحل عملي:
   إلغاؤها بـ BACK ويظهر حوار "Use demo account" (fallback التطبيق)، أو
   `force-stop com.google.android.gms`.
4. **تحذيرات lint غير مانعة**: `UnusedResource / HardcodedText / SmallSp`.
   النتيجة الكلية 0 errors — لا تُعالج تلقائيًا.
5. **العربية في كونسول PowerShell تظهر `?`** — ليست خطأ في الملفات؛
   اقرأ الملفات مباشرة كـ UTF-8 (مثل `File.ReadAllText` بترميز صريح).
6. **بعد تغيير `wm size/density`** قد تظل الواجهة القديمة معروضة؛ أعد
   تشغيل التطبيق (`force-stop` + `monkey`) قبل التقاط اللقطات، وارجع
   بـ `reset` دائمًا بعد انتهاء الاختبار.

## 12. ملف `util/ScreenFit.kt` (دعم الشاشات — أُضيف 2026-11-08)

دوال وحدها معتمدة من كود الشاشات — لا مكتبات ولا مكوّنات جديدة:

| الدالة | الغرض |
|---|---|
| `View.fitCenteredColumn(maxWidthDp)` | تُقيِّد عرض العنصر المعطى وتُمركزه أفقيًا عندما يكون العرض الفعلي ≥ 600dp؛ no-op على الهاتف العمودي |
| `ViewGroup.gridSpan()` | عدد أعمدة القوائم: 2 على الشاشات العريضة، 1 وإلا |

الموضع (استدعاءات واحدة لكل شاشة):

- `MainActivity`: `safeArea.fitCenteredColumn(720)` + `navBar.fitCenteredColumn(480)`.
- `AuthActivity`: `authContent.fitCenteredColumn(560)`.
- `ProjectDetailActivity`: `detailContent.fitCenteredColumn(720)`.
- `ChatActivity`: `safeArea.fitCenteredColumn(720)`.
- `HomeFragment`: `homeContent.fitCenteredColumn(720)`.
- `ProfileFragment`: `profileContent.fitCenteredColumn(720)`.
- `ProjectsFragment`: عمود مركزي 720 + `GridLayoutManager(root.gridSpan())`.
- `ChatFragment`: عمود مركزي 720 فقط (قائمة linear).

الـ `id`s الجديدة في layouts (لا خصائص أخرى تغيّرت):
`navBar` (activity_main)، `authContent` (activity_auth)،
`detailContent` (activity_project_detail)، `homeContent` (fragment_home)،
`profileContent` (fragment_profile).

## 13. تنظيف بيانات الاختبار على المحاكي

```powershell
& $adb shell pm clear com.aurora.workspace    # يعيد التطبيق حالة أول تشغيل
```

يُستعمل فقط عند الحاجة إلى إعادة Seed data من الصفر — فهو يمحو بيانات
التطبيق على الجهاز نهائيًا (لا يُنفَّذ إلا بطلب المستخدم).

## 14. الأمان

لا مفاتيح API ولا أسرار داخل المشروع حاليًا. أبقِ الأمر هكذا؛ إن قُرر لاحقًا
تفعيل OAuth فلتُخزَّن الأسرار خارج المستودع (local.properties أو متغيرات
بيئة) وألا تُكتب في التوثيق.
