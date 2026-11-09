# Privacy · الخصوصية

**English**

Aurora is an offline Android app. It has no account system, no analytics, no ads,
no crash reporting and no third-party SDKs.

What stays on the device:

- Projects, tasks, conversations, messages and the profile you enter.
- Theme, language and preference choices.

The only network request the app makes:

- Once a day (and only if that request has not already succeeded that day) it
  asks the public GitHub API for the latest release of this repository:
  `https://api.github.com/repos/yassinadel3843-dot/Aurora/releases/latest`.
- That request sends no identifier, no account, no device model, no location and
  no content from your workspace. GitHub receives an IP address and the
  user-agent string `Aurora-Android`, as with any HTTPS request.
- If a newer release exists, the app may download its APK from GitHub Releases
  when you tap "Download & install". That download is the only time a file is
  fetched.

Permissions and why they exist:

| Permission | Why |
|---|---|
| `INTERNET` | The update check and the APK download. |
| `REQUEST_INSTALL_PACKAGES` | To hand the downloaded APK to Android's installer. |
| `POST_NOTIFICATIONS` | Update notices and the reminder switch in Profile. |
| `GET_ACCOUNTS` | To list Google accounts already on the device when you tap "Link". |
| `VIBRATE` | The haptics switch. |

You can clear everything by uninstalling the app or clearing its storage. There
is no server-side copy to delete.

---

**العربية**

أورورا تطبيق أندرويد يعمل بلا إنترنت. لا يوجد نظام حسابات، ولا تحليلات، ولا
إعلانات، ولا إبلاغ عن الأعطال، ولا أي SDK طرف ثالث.

ما يبقى على الجهاز:

- المشاريع والمهام والمحادثات والرسائل وبيانات الملف الشخصي التي تدخلها.
- اختيارات الثيم واللغة والتفضيلات.

طلب الشبكة الوحيد:

- مرة واحدة يوميًا (وفقط إذا لم ينجح الطلب في ذلك اليوم) يسأل GitHub API العام عن
  أحدث إصدار من هذا المستودع:
  `https://api.github.com/repos/yassinadel3843-dot/Aurora/releases/latest`.
- الطلب لا يرسل أي معرّف ولا حساب ولا طراز جهاز ولا موقع ولا أي محتوى من مساحة
  عملك. GitHub يستقبل عنوان IP وسلسلة `Aurora-Android`، كما في أي طلب HTTPS.
- عند وجود إصدار أحدث قد ينزّل التطبيق ملف APK من GitHub Releases عند ضغطك على
  «تنزيل وتثبيت». هذا التنزيل هو المرة الوحيدة التي يُجلب فيها ملف.

الأذونات وسببها:

| الإذن | السبب |
|---|---|
| `INTERNET` | التحقق من التحديث وتنزيل APK. |
| `REQUEST_INSTALL_PACKAGES` | تسليم ملف APK المَنزّل إلى مثبّت أندرويد. |
| `POST_NOTIFICATIONS` | إشعارات التحديث ومفتاح التذكيرات في حسابي. |
| `GET_ACCOUNTS` | لعرض حسابات Google الموجودة على الجهاز عند الضغط على «ربط». |
| `VIBRATE` | مفتاح الاهتزاز. |

يمكنك محو كل شيء بإزالة التطبيق أو محو تخزينه. لا توجد نسخة على أي سيرفر لحذفها.
