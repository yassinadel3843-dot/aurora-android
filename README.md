# Aurora · أورورا

تطبيق أندرويد أصلي (Kotlin) للتعاون وإدارة المشاريع والإنتاجية، بتصميم مستوحى من
أبل: أسطح زجاجية شفافة، سماء ليلية متدرجة، وحركات هادئة. يعمل بالكامل بلا إنترنت
وبلا خادم، ويدعم العربية والإنجليزية مع اتجاه RTL كامل.

---

## 1. المزايا

| المجال | التفاصيل |
| --- | --- |
| المشاريع | إنشاء مشروع، إضافة مهام، تعليم المهام كمنجزة، شريط تقدّم حقيقي، أعضاء فريق، مشاركة/دعوة |
| المحادثات | قائمة محادثات فردية وجماعية، شاشة دردشة كاملة، إرسال رسائل، عدّاد غير مقروء |
| الفريق | 6 أعضاء مع أدوار وحالات اتصال، صور رمزية بالأحرف الأولى |
| الحساب | تسجيل دخول بالبريد أو بحساب Google الموجود على الجهاز، ربط/فصل الحساب، خروج |
| المظهر | 5 ثيمات كاملة (سماء ليلية، زجاج أورورا، نهاري، زمردي، غروب) + خلفية مرسومة بالكانفاس مع نجوم |
| اللغة | عربي / إنجليزي / لغة النظام، تتبديل فوري مع إعادة إنشاء المهمة (Task) واتجاه RTL |
| الإنتاجية | حلقة تقدّم اليوم، سلسلة أيام، إحصاءات المشاريع والمهام والفريق |
| اللمسات | اهتزاز خفيف عند الإرسال/الإنجاز، انتقالات ناعمة، حافة إلى حافة (edge-to-edge) |

كل البيانات تُحفظ محليًا في `SharedPreferences` بصيغة JSON — لا حاجة إلى Room ولا
إلى خادم، لذلك يعمل التطبيق على أقدم إصدارات أندرويد.

---

## 2. التوافق

- **minSdk = 21** (أندرويد 5.0 Lollipop) — أوسع انتشار عملي.
- **targetSdk = 36**، **compileSdk = 37**، buildTools `36.0.0`.
- Kotlin + AGP 9.5.1 + Gradle 9.6.0 (AGP 9 يضم دعم Kotlin مدمجًا).
- واجهات XML + Material Components + ViewBinding (لا Compose، لا مكتبات ثقيلة).

---

## 3. البناء

### من Android Studio
افتح مجلد `Aurora` كـ Project ثم شغّل التطبيق. سيستخدم المشروع الـ Gradle Wrapper
الموجود (`gradlew`).

### من سطر الأوامر (Windows)
```powershell
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
$env:ANDROID_HOME = "$env:LOCALAPPDATA\Android\Sdk"
.\gradlew.bat :app:assembleDebug --console=plain
```

ملف APK الناتج:
```
app\build\outputs\apk\debug\app-debug.apk
```

> ملاحظة: مجلد `local.properties` يحتاج مسار الـ SDK الصحيح:
> `sdk.dir=C\:\\Users\\<user>\\AppData\\Local\\Android\\Sdk`

---

## 4. بنية المشروع

```
app/src/main/java/com/aurora/workspace/
├── AuroraApp.kt                  # تهيئة التخزين
├── data/
│   ├── Models.kt                 # Member / Project / TaskItem / Conversation / Message
│   ├── SeedData.kt               # بيانات تجريبية (4 مشاريع، 6 أعضاء، 4 محادثات)
│   └── Store.kt                  # المصدر الوحيد للحقيقة + حفظ JSON + عمليات التعديل
├── theme/
│   ├── ThemeManager.kt           # الثيمات الخمسة
│   └── LocaleHelper.kt           # تغليف السياق باللغة + RTL
├── util/
│   ├── ThemeExt.kt               # colorAttr / boolAttr / dp / withAlpha
│   └── GoogleAccountHelper.kt    # اختيار حساب Google عبر AccountManager
├── widgets/                      # عناصر مرسومة بالكانفاس
│   ├── AuroraBackgroundView.kt   # تدرّج السماء + هالتان + نجوم
│   ├── GlassCard.kt              # السطح الزجاجي الأساسي
│   ├── ProgressRingView.kt       # حلقة تقدّم متحركة
│   └── AvatarView.kt             # صورة رمزية بالأحرف + نقطة اتصال
└── ui/
    ├── AuroraActivity.kt         # الثيم + اللغة + edge-to-edge
    ├── AuthActivity.kt           # الدخول / Google / حساب تجريبي
    ├── MainActivity.kt           # الحاوية + شريط التنقل الزجاجي (4 تبويبات)
    ├── ProjectDetailActivity.kt  # تفاصيل المشروع + المهام + المشاركة
    ├── ChatActivity.kt           # شاشة المحادثة
    ├── home/ projects/ chat/ profile/
    └── adapters/                 # مشاريع، محادثات، رسائل
```

---

## 5. الثيمات

كل ثيم يعرّف نفس مجموعة الخصائص (`auroraBg1`, `auroraSurface`, `auroraAccent`,
`auroraNav`, …) في `res/values/themes.xml`، لذلك تُعاد تلوين الواجهة كلها دون
تكرار أي تخطيط:

1. **Night Sky** — كحلي عميق مع أزرق سماوي (الافتراضي).
2. **Glass Aurora** — أخضر مزرق شفاف على أسود ناعم.
3. **Daylight** — نهاري فاتح، أسطح بيضاء، نصوص داكنة.
4. **Emerald** — زمردي هادئ.
5. **Sunset** — برتقالي/وردي دافئ.

التبديل من: **الملف الشخصي ← المظهر ← اختر اللون** (يعيد إنشاء الشاشة فورًا).

---

## 6. اللغة و RTL

- `LocaleHelper.wrap()` في `attachBaseContext` لكل شاشة.
- التبديل من: **الملف الشخصي ← اللغة ← العربية / English / النظام**.
- `restartApp()` يعيد تشغيل المهمة (Task) باللغة الجديدة.
- `android:supportsRtl="true"`، وكل التخطيطات تستخدم `start/end` لا `left/right`.

---

## 7. ربط حساب Google

يستخدم `AccountManager.newChooseAccountIntent` لاختيار حساب Google المسجّل على
الجهاز — يعمل على API 21+ دون OAuth Client ID ودون إنترنت.

إذا لم يوجد حساب على الجهاز يظهر بديل تجريبي (`layla.aurora@gmail.com`).
للترقية إلى OAuth حقيقي لاحقًا: استبدل `GoogleAccountHelper.intent()` بـ
`GoogleSignInClient.signInIntent` واقرأ البريد من نتيجة الـ Intent.

---

## 8. الحساب التجريبي

| الحقل | القيمة |
| --- | --- |
| الاسم | Layla Hassan |
| البريد | layla@aurora.app |
| Google | layla.aurora@gmail.com |

أو اضغط **«استخدم الحساب التجريبي»** في شاشة الدخول.

---

## 9. الأيقونة

- `mipmap-anydpi-v26/ic_launcher.xml` — أيقونة تكيّفية (خلفية كحلية + حرف A + شرارة).
- `mipmap-*dpi/ic_launcher.png` و`ic_launcher_round.png` — بدائل PNG للأجهزة الأقدم
  (48/72/96/144/192 بكسل).

---

## 10. ملاحظات تطويرية

- كل التعديلات تمر عبر `Store` ثم `Store.save(context)` — أضف أي حقل جديد إلى
  `*ToJson` و`parse*` المقابلين.
- لإضافة ثيم: أضف `<style name="Theme.Aurora.X">` في `themes.xml`، ثم مفتاحًا في
  `ThemeManager.keys` و`styleRes`.
- الإصدار الحالي: `1.0.0` (versionCode 1).
