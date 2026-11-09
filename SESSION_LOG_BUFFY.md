# SESSION_LOG_BUFFY.md — سجل جلسة العمل (Buffy)

> ملف سجل مخصص لهذه الجلسة: كل خطوة، كل ملف اتغيّر، كل عقبة وحلّها.
> أُنشئ بتاريخ 2026-11-09. لا يستبدل أي ملف توثيق قائم.

---

## 0) تعريف الجلسة والنطاق

- **الوكيل:** Buffy.
- **مجلد العمل الرسمي:** `C:\Users\yassin adel\Documents\Default Project\Aurora`.
- **ملاحظة:** مجلد جلسة الأداة هو `Aurora\app`، وكل ملفات التوثيق تقع في جذر
  `Aurora` — لذلك لم تُقرأ `AGENTS.md` تلقائيًا؛ قُرئت يدويًا مع باقي الوثائق.
- **وكيل آخر يعمل على نفس الشجرة في نفس الوقت** (تعديلات `GlassCard.kt`
  08:05 + إضافة `drawable/ic_launcher_monochrome.xml` → هو في بند M3 ثم M4).
- **النطاق الذي تولّيته:** **M5** (حركة انتقال التابات) ثم **M6** (توحيد سماكة
  الأيقونات) — وهما البندان اللذان لا يتقاطعان مع ملفات الوكيل الآخر.
- **قاعدة التنسيق المتّبعة:** لا لمس لـ `GlassCard.kt` / `themes.xml` /
  `res/layout/*` / `mipmap-*` / `ic_launcher_*` / `build.gradle.kts`.

## 1) خط الأساس قبل أي تعديل (تحقّق فعلي)

| الفحص | النتيجة |
|---|---|
| `:app:assembleDebug` | BUILD SUCCESSFUL (3m15s) |
| `:app:testDebugUnitTest` | 8 اختبارات، 0 فشل (2 DateText + 6 Models) |
| APK | 7,300,322 بايت |
| المحاكي | `emulator-5554` يعمل، 1080x2400@420، التطبيق على `ui.MainActivity` |

## 2) التعديلات المنفَّذة

### M5 — حركة انتقال التابات (انتقالة "Aurora settle")

**ملفات جديدة**

1. `app/src/main/res/anim/fragment_enter.xml`
   - `alpha` من 0 إلى 1 خلال 220ms.
   - `translateY` من `2%p` إلى 0 خلال 240ms (حركة رأسية فقط).
   - `scale` من 0.985 إلى 1 خلال 240ms (نقطة الارتكاز المنتصف).
   - `interpolator = @android:interpolator/fast_out_slow_in`.
2. `app/src/main/res/anim/fragment_exit.xml`
   - `alpha` من 1 إلى 0 خلال 150ms.
   - `translateY` من 0 إلى `-1.5%p` خلال 170ms.
   - `scale` من 1 إلى 0.99 خلال 170ms.
   - `interpolator = @android:interpolator/linear_out_slow_in`.

**لماذا حركة رأسية فقط:** لا يوجد أي `translateX`، لذلك الشكل متطابق حرفيًا في
LTR وRTL (مهم لأن التطبيق ثنائي اللغة و`supportsRtl=true`).

**تعديل موجود**

3. `app/src/main/java/com/aurora/workspace/ui/MainActivity.kt` — داخل `select()`:
   - كان: `setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)` دائمًا.
   - أصبح: الحركة تُطبَّق فقط عند `animate == true` (أي عند لمس تاب فعلي).
   - **السبب:** الإقلاع البارد واستعادة الحالة يمرّان بـ `select(tab, animate = false)`؛
     بهذا يفتح التطبيق على شاشة مستقرة بدون فلاش، والحركة تبقى للمستخدم فقط.
   - نبضة أيقونة التاب (`animateIcon`) لم تُمس.

### M6 — توحيد سماكة الأيقونات

4. **11 أيقونة** في `app/src/main/res/drawable/ic_*.xml`: كلها الآن
   `android:strokeWidth="1.8"` (كانت: 1.7×5، 1.8×1، 1.9×2، 2×3).
   - أيقونة اللانشر (`ic_launcher_foreground.xml` / `ic_launcher_monochrome.xml`)
     بسماكة 7 لم تُمس — لأنها مقياس مختلف (108dp) وليست ضمن مجموعة الواجهة.
   - النتيجة النهائية: `11 × 1.8` + `4 × 7` (لانشر فقط).

## 3) التحقق الفعلي بعد التعديل

| الفحص | النتيجة |
|---|---|
| `:app:assembleDebug` | **BUILD SUCCESSFUL** (1m48s) |
| تثبيت على المحاكي | Success على `emulator-5554` |
| لمس التابات الأربعة (Home → Projects → Chat → Profile) | كل شاشة ظهرت بمحتواها الصحيح |
| `FATAL EXCEPTION` في logcat | **0** |
| اللغة/الاتجاه | عربي RTL كامل، والشريط السفلي معكوس صحيحًا |
| الثيم الحالي | الغروب (Sunset) ظاهر ومتّسق |
| `res/anim/*.xml` | كلاهما XML صالح (تم تحليله بـ Python) |
| قياس الأداء (gfxinfo) | أثناء التبديل: 88.73% إطارات متقطعة / وسيط 81ms<br>**الضبط: التطبيق ساكن 6 ثوانٍ بدون أي لمس = 97.92% / وسيط 81ms** |

**الاستنتاج من القياس:** التقطيع سببه رندر المحاكي (GPU برمجي)، وليس حركة
الانتقال — لأن التطبيق بدون أي تفاعل يسجّل نفس الرقم أو أسوأ.

**حدود التحقق (بأمانة):** الحركة لم تُشاهد كفيديو متصل (لا يوجد ffmpeg لتفكيك
`screenrecord`)، فالمُثبت هو: البناء، تنفيذ التبديل، صفر كراش، واللقطات المستقرة
بعد كل تاب. نعومة الحركة بالعين تحتاج جولة بشرية أو جهاز حقيقي.

## 4) العقبات التي واجهتني وحلولها

1. **`adb: command not found` في Git Bash** رغم تصدير `PATH` →
   استخدمت المسار الكامل:
   `C:/Users/yassin adel/AppData/Local/Android/Sdk/platform-tools/adb.exe`.
2. **Git Bash يحوّل `/sdcard/...` إلى مسار ويندوز** (ظهر في رسالة
   `.../resources/git/sdcard/ui.xml`) → `export MSYS_NO_PATHCONV=1`.
3. **حلقة `while read` كانت تنتهي بعد أول سطر** لأن `adb shell` يقرأ
   `stdin` ويستهلك بقية الأسطر → إضافة `< /dev/null` لكل أمر `adb` داخل الحلقة.
4. **أول 4 لقطات شاشة طلعت بيضاء بالكامل** (24KB) رغم أن
   `uiautomator dump` يقرأ كل نصوص الواجهة العربية، وأن
   `dumpsys window` يقول `mDrawState=HAS_DRAWN` و`isOnScreen=true`.
   السبب: طبقة **Splash Screen** عالقة فوق نافذة التطبيق في SurfaceFlinger.
   **الحل:** `am force-stop` ثم إعادة تشغيل (`monkey`) والانتظار 8 ثوانٍ —
   بعدها أصبحت اللقطة 958KB بمحتوى حقيقي. **ليست مشكلة في كود Aurora.**
5. **لا يوجد PIL** في `python` الافتراضي (`py -3` يفشل أيضًا)، و`magick` غير
   موجود → تصغير اللقطات عبر PowerShell + `System.Drawing`.
6. **المشروع ليس Git repo** (`fatal: not a git repository`) → لا يمكن عمل
   commit؛ لذلك كل سجل التغييرات في هذا الملف.
7. **`uiautomator` يطبع العربية كـ `?` في الكونسول** (ترميز cp1252) →
   كتابة المخرجات إلى ملف UTF-8 ثم قراءته.

## 5) ملاحظات لاحظتها ولم أُعدّلها (ليست في نطاقي)

1. مشروع **Sky Notes** على الجهاز وصفه «Where teams create together» — هذه
   بيانات قديمة محفوظة من قبل إصلاح `project_desc_empty` (بيانات جهاز، لا كود).
2. في تاب Projects، نص النسبة «%100 اكتمل» ملتصق بالحد الأمامي للبطاقة في RTL —
   يحتاج نظرة بصرية من مالك بند الهوية (M3/M4).
3. في Home: بطاقة إحصائية تحتوي مستطيلًا دائريًا فارغًا، وسطر الاقتباس فوقه
   كبسولة فارغة — تحتاج مراجعة بصرية أيضًا (قد تكون عناصر تصميم مقصودة).
4. انظر بند الأداء أعلاه: المحاكي بطيء في الرسم بطبيعته.

## 6) المتبقي في بند «صقل الهوية» (بعد هذه الجلسة)

- **M3** ظل الزجاج — الوكيل الآخر (الكود مطبَّق ومثبَّت).
- **M4** أيقونة اللانشر — **أنجزها الوكيل الآخر أثناء الجلسة**:
  `mipmap-*/ic_launcher*.png` أُعيد توليدها الساعة 08:17، و
  `drawable/ic_launcher_background.xml` + `ic_launcher_foreground.xml` +
  `ic_launcher_monochrome.xml` + `mipmap-anydpi-v26/ic_launcher*.xml`
  (بها `<monochrome>`) الساعة 08:18. بناء جلسة 08:19–08:21 وتثبيت 08:21
  **يشملان أصول M4 بالفعل**، ولم ألمسها.
- **تحقّق أمني على M6:** حلقة `sed` أعادت كتابة كل `ic_*.xml` (فتغيّر mtime)،
  لكن أصول اللانشر لم تتأثر: `strokeWidth="1.8"` = **0** في ملفات
  `ic_launcher_*`، والأيقونة النشطة (background/foreground/monochrome) سليمة.
- **M5** ✅ أُنجز في هذه الجلسة.
- **M6** ✅ أُنجز في هذه الجلسة.
- **اختبار انحدار كامل:** 5 ثيمات × عربي/إنجليزي × 4 مقاسات شاشة + فحص
  `FATAL EXCEPTION` بعد كل بند (يُفضّل بعد اكتمال M4).
- **تحديث `PROJECT_STATUS.md` و`NEXT_STEPS.md`**: لم أكتب فيهما هذه الجلسة
  لتجنّب تعارض الكتابة مع الوكيل الآخر؛ هذا الملف يمثل سجلي إلى حين الدمج.

## 7) أوامر مُعادة الاستخدام (نسخة مجرَّبة)

```bash
export MSYS_NO_PATHCONV=1
export JAVA_HOME="C:/Program Files/Android/Android Studio/jbr"
export ANDROID_HOME="C:/Users/yassin adel/AppData/Local/Android/Sdk"
ADB="$ANDROID_HOME/platform-tools/adb.exe"
cd "C:/Users/yassin adel/Documents/Default Project/Aurora"

./gradlew.bat :app:assembleDebug :app:testDebugUnitTest --console=plain
"$ADB" install -r app/build/outputs/apk/debug/app-debug.apk
"$ADB" shell am force-stop com.aurora.workspace
"$ADB" logcat -c
"$ADB" shell monkey -p com.aurora.workspace -c android.intent.category.LAUNCHER 1
sleep 6   # لا تلتقط قبل ذلك: قد تظهر طبقة splash بيضاء
"$ADB" shell screencap -p /sdcard/s.png && "$ADB" pull /sdcard/s.png <path>
"$ADB" logcat -d | grep -c "FATAL EXCEPTION"
```

## 8) Git — تسجيل الشغل (2026-11-09)

- الوكيل الآخر أنشأ المستودع: فرع `main`، وCommit أساسي
  `5a150b9 Baseline: Aurora v1.0.0 with full identity polish` (08:32).
  الرسالة تذكر صراحةً M5 وM6، أي أن شغلي داخل الـ Baseline.
- **تحقّق مباشر من الـ Commit** (مش من الرسالة):
  - `app/src/main/res/anim/fragment_enter.xml` ✅ موجود
  - `app/src/main/res/anim/fragment_exit.xml` ✅ موجود
  - `app/src/main/java/com/aurora/workspace/ui/MainActivity.kt` سطر 96 =
    `setCustomAnimations(R.anim.fragment_enter, R.anim.fragment_exit)` ✅
  - `SESSION_LOG_BUFFY.md` ✅ موجود
  - M6: كل `ic_*.xml` مسجّل بسماكة 1.8 (والـ 11 أيقونة داخل الـ commit).
- **الملف الوحيد اللي كان غير مسجّل:** `COORDINATION.md` (أنشأته بعد الـ
  Baseline) → سُجّل في Commit مستقل مع تحديثي هذا الملف.
- **سلامة `.gitignore`:** `app/build/outputs/apk/debug/app-debug.apk` و
  `local.properties` مستبعدان فعليًا (تم التحقق بـ `git check-ignore -v`).
- **قاعدة العمل معه:** أي commit جديد لازم يكون محدّد النطاق، ومفيش `amend`
  على commit حد تاني، ومفيش push بدون طلب المستخدم.

## 9) الركن المنسي: بحث المحادثات (2026-11-09 ~08:50)

**المشكلة المكتشفة:** زرار البحث في `fragment_chat.xml` موجود من v1.0.0،
و`ChatFragment` كان يرد عليه بـ `Toast(R.string.search)` فقط — واجهة كذابة
بلا أي وظيفة، ولم تُذكر في `PROJECT_STATUS.md` ولا في أي بند.

**التنفيذ:**
1. `res/layout/fragment_chat.xml`: صف بحث `searchBar` مخفي في البداية
   (أيقونة بحث + `EditText` + زر إغلاق)، بخلفية `@drawable/bg_input` الموجودة
   (بلا أي مورد جديد سوى نصين).
2. `ui/chat/ChatFragment.kt`: بحث حي أثناء الكتابة يطابق
   **عنوان المحادثة + المعاينة + اسم العضو + نص كل رسالة** (ignoreCase)،
   مع فتح/إغلاق الحقل ولوحة المفاتيح، و`IME_ACTION_SEARCH`.
3. نصان جديدان في `values/strings.xml` و`values-ar/strings.xml`:
   `search_conversations` و`no_search_results` (135 → 137 مفتاح في كل لغة).
4. حذف `setOnLongClickListener { false }` الميت من `ConversationsAdapter`.

**التحقق الفعلي (على المحاكي، بالعربية RTL):**

| الحالة | النتيجة |
|---|---|
| القائمة قبل البحث | 4 محادثات |
| كتابة `lina` | **2** محادثات (Design crew + Lina Farah) |
| زر الإغلاق | رجعت **4** وتفريغ الحقل |
| كتابة `zzz` | **0**، وظهر «لا توجد محادثات مطابقة لبحثك.» |
| `FATAL EXCEPTION` | **0** |
| البناء | BUILD SUCCESSFUL (18s) |

**الدليل البصري:** `app/build/tmp/shots/chat_search_lina.png` و
`chat_search_none.png` (لقطات 08:50).

## 10) الركن الثاني: إنشاء المشروع (2026-11-09 ~09:05)

**المشكلة المكتشفة:** حوار «مشروع جديد» كان يوزّع الأيقونة واللون بـ
`emojis.random()` و`accents.random()` — المستخدم لا يختار شيئًا، ومشروعان
متتاليان قد يخرجان متطابقين. ونص `project_icon` («الأيقونة») كان موجودًا في
اللغتين وغير مستخدم أبدًا — أي أن الاختيار كان مقصودًا ولم يُكمَل.

**التنفيذ:**
1. `res/layout/dialog_new_project.xml`: صف أيقونات (`emojiRow` داخل
   `HorizontalScrollView`) وصف ألوان (`accentRow`)، مع عناوين من النصوص الموجودة.
2. `ProjectsFragment.kt`: قائمة ثابتة `PROJECT_EMOJIS` (7) و`PROJECT_ACCENTS` (5)،
   رسم الشرائح برمجيًا مع تحديد واضح (شريحة مختارة + حلقة بيضاء حول اللون)،
   وتمرير الاختيار إلى `Store.addProject` بدلًا من العشوائي.
3. **إصلاح خلل جانبي حقيقي:** `ConversationsAdapter` كان يلوّن أفاتار المجموعة حسب
   **موضع الصف** (`conversationColor(position)`) — أي أن البحث الجديد كان سيغيّر
   لون المجموعة مع كل تصفية. الآن اللون مشتق من `id` بـ `Math.floorMod`.
4. `ProjectDetailActivity`: زر الحوار كان يستخدم `R.string.cd_add` (نص وصف
   لقارئ الشاشة) كنص مرئي — أُضيف `add` («إضافة») وأصبح الزر يستخدمه.
5. نصوص جديدة: `add` و`project_accent` في اللغتين (137 → 139 مفتاح).

**التحقق:**
- البناء: **BUILD SUCCESSFUL**.
- لقطة الحوار: `app/build/tmp/shots/dialog_picker.png` — تظهر صفوف الأيقونات
  والألوان والتحديد، وخلفها مشروع «Picker Test» منشأ بأيقونة 🚀.
- `FATAL EXCEPTION` = 0.

**عقبات في الاختبار (تسجيل أمانة):**
- `uiautomator dump` كان يتأخر خطوة أحيانًا فيرى شاشة قديمة، فأوقعت لمسات على
  إحداثيات خاطئة وأغلقت الحوار. الحل المعتمد: **لقطة شاشة للتحقق البصري** +
  dump طازج قبل كل لمسة، وعدم استخدام BACK (يقفل الحوار).
- تعطيل الـ IME مؤقتًا لم يمنع ظهور الحوار لكنه جعل التطبيق يخرج للانشر في
  محاولة؛ أُعيد تفعيله (`ime enable/set`) والحالة الآن سليمة.

## 11) النشر على GitHub + التحديث الذاتي (2026-11-09 ~10:25)

**الطلب:** نشر التطبيق على GitHub بصفحة تنزيل ووثائق عربي/إنجليزي بلا أي ادعاء
غير حقيقي، وإضافة تحديث ذاتي يجلب الإشعار ويحدّث النسخة بدلًا من التنزيل اليدوي.

**ما كان موجودًا فعلًا:** تطبيق أندرويد واحد (لا نسخة ويب ولا سطح مكتب)، بلا
remote، بلا توقيع إصدار حقيقي (كان يوقّع بمفتاح debug)، وبلا أي قناة تحديث.

**التنفيذ:**
1. `util/UpdateChecker.kt` — قراءة `releases/latest` من GitHub بـ
   `HttpURLConnection` (بلا مكتبات جديدة)، مقارنة إصدارات رقمية نقطية، حفظ وقت
   آخر فحص (مرة/24 ساعة)، فحص صامت عند فتح الشاشة الرئيسية وفحص يدوي من
   «عن أورورا»، وإشعار على قناة مستقلة `aurora_updates_channel`.
2. `util/UpdateInstaller.kt` — تنزيل APK بـ `DownloadManager` إلى مجلد التطبيق،
   استقبال `DOWNLOAD_COMPLETE`، إشعار «اضغط للتثبيت»، ثم `FileProvider` +
   `ACTION_VIEW` للمثبّت مع معالجة إذن «تثبيت تطبيقات غير معروفة» (أندرويد 8+).
3. المانيفست: `REQUEST_INSTALL_PACKAGES` + provider + مستقبِلان (كلاهما غير مُصدَّر).
4. نصوص EN/AR: 19 مفتاحًا لكل لغة (157 = 157 مفتاحًا).
5. التوقيع: مفتاح إصدار حقيقي `keystore/aurora-release.jks` (alias `aurora`)،
   وقراءة الإعدادات من `keystore.properties` أو `AURORA_*` مع رجوع لمفتاح التصحيح.
6. `.github/workflows/release.yml` — على وسم `v*` يبني APK موقّعًا وينشره، ويرفض
   العمل إن اختلف الوسم عن `versionName` أو غاب سر المفتاح.
   و`.github/workflows/ci.yml` للبناء والاختبارات.
7. `README.md` ثنائي اللغة، `PRIVACY.md`، `CHANGELOG.md`، و`docs/index.html`
   (صفحة تنزيل ثنائية اللغة مع 4 لقطات) و`docs/screens/*.png`.

**التحقق الفعلي:**
- `assembleDebug` + `assembleRelease` ناجحان، و**17 اختبار وحدة** ناجحًا
  (منها 9 جديدة: مقارنة الإصدارات + قراءة رد GitHub).
- `apksigner verify`: الملف `app-release.apk` موقّع بـ **CN=Aurora**، بصمة
  SHA-256 تبدأ `cedf2993…` — أي أن مفتاح الإصدار يعمل فعلًا.
- على المحاكي: «عن أورورا» يعرض الإصدار من الحزمة (1.0.0)، وزر
  CHECK FOR UPDATES يُظهر توست فشل لطيف لأن المستودع لسه غير منشور (404)،
  وصفر `FATAL EXCEPTION`.
- صفحة التنزيل: تُخدَم محليًا (200) والصور تُحمّل (480px)، وتبديل الإنجليزية/
  العربية يقلب `dir` إلى `rtl` ويغيّر العنوان والنصوص، ورابط التنزيل يشير إلى
  `releases/latest`.

**عقبات سجّلتها:**
- `keytool` غير موجود على PATH في Git Bash → استُخدم المسار الكامل
  `C:/Program Files/Android/Android Studio/jbr/bin/keytool.exe`.
- داخل بلوك `android { }` في Kotlin DSL فشل `java.util.Properties()`
  («Unresolved reference 'util'») → الحل `import java.util.Properties` أعلى
  `app/build.gradle.kts`. (التعديل الأول كسر `assembleRelease` وأصلحته قبل التسجيل.)
- **غير مُتحقَّق:** نجاح GitHub Actions نفسه — لا يمكن تشغيل Runner من هنا.
  والريبو يستخدم `compileSdk 37`، وقد لا تكون المنصّة متاحة على الـ runner إلا
  من قناة المعاينة؛ لذلك الـ workflow يحاول القناة المستقرة ثم `--channel=3`.
  أول تشغيل حقيقي على GitHub يبقى الاختبار الحقيقي.
- لقطات الصفحة تحتوي بيانات اختبار («Picker Test» مرتين) — لم أمسح بيانات
  المحاكي لأن الحذف يحتاج إذنًا.

## 12) النشر الفعلي ونجاح الـ workflow (2026-11-09 ~11:05)

**ما صار فعلًا (مُتحقَّق، لا مجرد خطة):**

- الـ remote الحقيقي `origin` = `github.com/yassinadel3843-dot/aurora-android`،
  ودفعت كل الـ commits (`2185a98`, `7be7448`, `35c66e2`) ووسم `v1.0.0`.
- **Release v1.0.0 منشور** بمرفق `aurora-1.0.0.apk` (5,213,691 بايت) — بناه
  الـ workflow نفسه على runner، لا ملف محلي مرفوع يدويًا.
- **التوقيع مُتحقَّق عليه من داخل الـ CI:** `apksigner` على ملف الـ release
  يعطي `CN=Aurora` وSHA-256 `CEDF2993…` — نفس بصمة المفتاح المحلي حرفًا بحرف،
  أي أن تحديث النسخ المثبّتة سيعمل.
- `aapt2`: `versionName=1.0.0`, `versionCode=1`, `minSdk=21`, `targetSdk=36`.
- **GitHub Pages مفعّل** من `docs/` على `main`:
  <https://yassinadel3843-dot.github.io/aurora-android/> — يرد 200 للزائر المجهول،
  والصور ترد 200، والنسخة العربية تعمل.
- **المستودع عام (PUBLIC)** — تحقّق: نداء API مجهول يرد 200، وتنزيل الأبق
  المجهول يرد 200.
- `description` + `homepage` + 6 topics مضبوطة على المستودع.
- أسرار التوقيع الأربعة مضافة باسمائها فقط (`gh secret list`) والقيم لم تُطبع
  في أي مكان: `AURORA_KEYSTORE_BASE64`, `AURORA_STORE_PASSWORD`,
  `AURORA_KEY_ALIAS`, `AURORA_KEY_PASSWORD`.

**عقبة حقيقية اكتُشفت وأُصلحت:** الـ workflow كان يطلب `platforms;android-37`،
وهو غير موجود في SDK؛ اسم الحزمة الفعلي **`platforms;android-37.0`** (رقم
رئيسي.فرعي) كما يظهر في `platforms/android-37.0/package.xml`. بعد التصحيح:
- **CI نجح على GitHub** (بناء + 17 اختبارًا) في 2m17s.
- **Release نجح** وأنتج الأبق المنشور.

**تحقق تعاقد البيانات:** رد `releases/latest` الحقيقي يحتوي `tag_name`,
`html_url`, `body` (315 حرفًا), و`assets[0]` باسم `.apk` وحجمه ورابطه — أي
نفس الحقول التي يقرأها `UpdateChecker.parse` (مثبتة أيضًا باختبارات وحدة
على نفس الشكل).

**ما لم يُختبر بعد (أمانة):** مسار «تنزيل ثم تثبيت» عبر `DownloadManager` لم
يُجرَّب عمليًا لأن جهاز المحاكي عليه نسخة **debug** بتوقيع مختلف، وتثبيت نسخة
release فوقها يحتاج حذف بيانات التطبيق (يحتاج إذنك). أول تحديث حقيقي (1.0.1)
سيكون اختباره الأول.
