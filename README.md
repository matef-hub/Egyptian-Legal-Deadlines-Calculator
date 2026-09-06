# Egyptian Legal Deadlines Calculator & Management System
# حاسبة المواعيد القانونية المصرية ونظام إدارة الأجندة القضائية

[![Android](https://img.shields.io/badge/Platform-Android%2016%20(API%2036)-brightgreen.svg)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.1.10-blue.svg)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/UI-Jetpack%20Compose%20%7C%20Material%203-blueviolet.svg)](https://developer.android.com/jetpack/compose)
[![React](https://img.shields.io/badge/Web-React%2018%20%2B%20TypeScript-61dafb.svg)](https://react.dev)
[![Law](https://img.shields.io/badge/Jurisdiction-Egyptian%20Procedures%20Code-gold.svg)](#)

---

## 📖 English Overview

The **Egyptian Legal Deadlines Calculator & Management System** is a professional-grade legal tool engineered specifically for Egyptian lawyers, legal advisors, and law firms. It automates the precise procedural calculation of statutory deadlines according to the **Egyptian Civil and Commercial Procedures Code (Law No. 13 of 1968)** and the **Criminal Procedures Code**.

The project features a **100% Native Android Application** (Kotlin, Jetpack Compose, Clean Architecture, Room Database) paired with a **High-Performance Web Companion** (React 18, TypeScript, Tailwind CSS) following a strict *Clean Minimalism* design philosophy.

### ⚖️ Core Egyptian Legal Engine Rules
- **Non-Inclusion of Announcement Day (الميعاد لا يشمل اليوم الأول)**: Pursuant to **Article 15** of the Civil and Commercial Procedures Code, the day of service, notification, or judgment pronouncement is strictly excluded from the calculation.
- **Official Egyptian Court Working Schedule**:
  - **Friday**: Official court holiday and weekly day off.
  - **Saturday**: **Official court working day (يوم السبت يوم عمل رسمي)** across all Egyptian courts. The calculator strictly recognizes Saturdays as active procedural business days.
  - **Official Holidays**: Automatic detection of Egyptian national, secular, and religious holidays (Coptic Christmas, Police Day, Eid El-Fitr, Eid El-Adha, Revolution Days, Armed Forces Day, etc.).
- **Deadline Extension (امتداد الميعاد للعطلات)**: In accordance with **Article 18**, if the final day of a deadline falls on a Friday or an official holiday, the deadline is legally extended to the first following business day.
- **Distance Deadlines (مواعيد المسافة)**: Built-in support for statutory distance extensions (Articles 16 & 17) based on geographical distance and remote border governorates.

### 🌟 Key Features
- **Statutory Rules Library**: Pre-configured with essential deadlines:
  - Civil & Commercial Appeals (*الاستئناف المدني والتجاري* - 40 days)
  - Summary Injunction Appeals (*استئناف الأمور المستعجلة* - 15 days)
  - Cassation Appeals (*الطعن بالنقض المدني والجنائي* - 60 days)
  - Petition for Review (*التماس إعادة النظر* - 40 days)
  - Opposition to Judgments in Absentia (*المعارضة في الجنح والمخالفات* - 10 days)
  - Misdemeanor Appeals (*استئناف الجنح والمخالفات* - 10 days for defendants, 30 days for prosecution)
  - Renewal from Dismissal (*التجديد من الشطب* - 60 days)
  - Abandonment of Litigation (*سقوط الخصومة* - 1 year)
- **Add to Google Calendar**: Instant one-click export to Google Calendar (`action=TEMPLATE`) and native Android Calendar (`CalendarContract`), pre-filling the case number, client name, relevant statutory articles, and procedural calculation notes.
- **Offline-First Local Storage**: Encrypted persistence of saved deadlines, case dossiers, and court hearing dates via Android Room and browser storage.
- **Clean Minimalism UI**: Focused typography, high contrast, zero bloat, and full Right-to-Left (RTL) Arabic support.

---

## 📖 نظرة عامة باللغة العربية

**حاسبة المواعيد القانونية المصرية ونظام إدارة الأجندة القضائية** هو نظام برمجي احترافي مخصص للسادة المحامين والمستشارين والإدارات القانونية في جمهورية مصر العربية، يهدف إلى أتمتة احتساب المواعيد الإجرائية المقررة في **قانون المرافعات المدنية والتجارية رقم 13 لسنة 1968** و**قانون الإجراءات الجنائية**.

يتكون المشروع من:
1. **تطبيق أندرويد أصلي متكامل (Native Android)** مكتوب بلغة Kotlin وأحدث إصدارات Jetpack Compose وفق معمارية Clean Architecture وقاعدة بيانات Room.
2. **منصة ويب تفاعلية حديثة (Web Companion)** مبنية باستخدام React 18 وTypeScript وTailwind CSS وفق نمط التصميم الهادئ والمريح للعين (*Clean Minimalism*).

### ⚖️ الضوابط الإجرائية المعتمدة في محرك الاحتساب
1. **استبعاد يوم الإعلان أو صدور الحكم (المادة 15 مرافعات)**: لا يُحسب اليوم الذي تم فيه إعلان السند أو صدور الحكم؛ ويبدأ الميعاد من اليوم التالي.
2. **يوم السبت يوم عمل رسمي في المحاكم المصرية**: يُعامل يوم السبت في النظام كيوم عمل كامل يجوز اتخاذ الإجراءات وإيداع الصحف وقيد الطعون فيه طبقاً للقواعد المقررة في المحاكم المصرية.
3. **يوم الجمعة والعطلات الرسمية (المادة 18 مرافعات)**: تُستبعد أيام الجمعة والعطلات الرسمية الصادرة بقرارات مجلس الوزراء، وإذا صادف آخر يوم في الميعاد عطلة رسمية أو يوم جمعة، يمتد الميعاد وجوباً إلى أول يوم عمل رسمي تالٍ.
4. **مواعيد المسافة (المادتان 16 و 17 مرافعات)**: إمكانية احتساب ميعاد المسافة الإضافي للمناطق البعيدة والمحافظات الحدودية (مطروح، الوادي الجديد، شمال وجنوب سيناء، البحر الأحمر).

### 🌟 أبرز المميزات
- **مكتبة شاملة للمواعيد الإجرائية**: تغطي الاستئناف العادي والمستعجل، الطعن بالنقض المدني والجنائي، التماس إعادة النظر، المعارضة في الأحكام الغيابية، تجديد الدعوى من الشطب، سقوط الخصومة، وتقادم الدعاوى الجنائية.
- **إضافة الميعاد إلى تقويم Google بنقرة واحدة**: إنشاء تذكير تلقائي في تقويم Google يتضمن بيانات القضية، اسم الموكل، السند التشريعي، وملاحظات الاحتساب.
- **سجل المواعيد المحفوظة والأجندة**: إمكانية أرشفة المواعيد المحسوبة والرجوع إليها أو تصفيتها أو حذفها عند اللزوم، والعمل دون الحاجة لشبكة الإنترنت (Offline-first).
- **تصميم مخصص للمحامين**: واجهات سريعة وواضحة خالية من التشتيت وتدعم اللغة العربية بالكامل (RTL) مع إبراز النتيجة القانونية بوضوح تام.

---

## 🏛️ Project Architecture / المعمارية البرمجية

```
├── app/                                    # Native Android Project (Kotlin / Compose)
│   ├── src/main/java/com/ateflaw/legaldeadlines/
│   │   ├── domain/                         # Domain Layer: Business logic & entities
│   │   │   ├── calculator/                 # LegalDeadlineCalculator engine
│   │   │   └── model/                      # DeadlineResult, LegalRule, Holiday
│   │   ├── data/                           # Data Layer: Room DB, DAOs, Services
│   │   │   ├── database/AppDatabase.kt     # Pre-seeded SQLite Room database
│   │   │   ├── dao/                        # LegalRuleDao, HolidayDao, SavedDeadlineDao
│   │   │   ├── service/                    # OfficialHolidayService (Offline + Sync)
│   │   │   └── worker/                     # HolidaySyncWorker (WorkManager)
│   │   └── presentation/                   # Presentation Layer: Jetpack Compose UI
│   │       ├── screens/                    # CalculatorScreen, SavedDeadlinesScreen
│   │       ├── components/                 # CalculationResultCard, SavedDeadlineItem
│   │       ├── viewmodel/                  # MainViewModel (StateFlow & MVI)
│   │       └── theme/                      # Material 3 Theme & Typography
│   └── build.gradle.kts                    # compileSdk 36 (Android 16), KSP, Room
│
├── src/                                    # Modern Web Companion (React / Vite)
│   ├── components/                         # AndroidProjectViewer & UI Components
│   ├── data/legalRules.ts                  # Procedural rules & Egyptian holiday dataset
│   ├── utils/
│   │   ├── calculator.ts                   # Web legal calculation algorithm
│   │   └── calendar.ts                     # Google Calendar URL builder
│   ├── types.ts                            # Unified TypeScript interfaces
│   ├── App.tsx                             # Main Single-Page Application
│   └── main.tsx                            # React DOM entry point
│
└── metadata.json                           # App metadata & runtime capabilities
```

---

## 🚀 Getting Started / تعليمات التشغيل

### 1. Web Application (React + Vite)
Ensure you have **Node.js (v18+)** and **npm** installed:

```bash
# Install dependencies
npm install

# Start the local development server
npm run dev

# Build production bundle
npm run build
```

### 2. Android Application (Android Studio)
1. Open the repository root or `/app` in **Android Studio Ladybug (or newer)**.
2. Make sure the **Android 16 (API 36) SDK** and **JDK 17+** are installed.
3. Sync Gradle project (`build.gradle.kts`).
4. Run on an emulator or physical device running Android 8.0 (API 26) through Android 16 (API 36).

```bash
# Run unit tests on Android domain calculator
./gradlew test
```

---

## 🧪 Legal Testing & Verification / الاختبارات القانونية

The calculation engine includes a comprehensive suite of unit tests verifying critical edge cases:
- **Test A**: Standard 40-day appeal deadline starting the day after notification.
- **Test B**: Final day falling on Friday → Automatic extension to Saturday (official business day).
- **Test C**: Final day falling on Saturday → Saturday confirmed as an active court working day.
- **Test D**: Final day falling on an official holiday (e.g. 6th of October) → Extended to next business day.
- **Test E**: Consecutive holidays (e.g. Eid El-Fitr / Eid El-Adha) spanning multiple days.
- **Test F**: Distance days calculation added to statutory period.

---

## 📄 License & Disclaimer / إخلاء مسؤولية قانوني

This application is provided as a computational assistant for practicing lawyers and legal professionals in the Arab Republic of Egypt. Calculations are grounded in the statutory provisions of the Egyptian Civil and Commercial Procedures Code. Users are advised to review the specific circumstances of each case against applicable judicial rulings and court circulars.

Designed and developed with precision for the Egyptian legal community.
صُمم وطُوّر بدقة لخدمة المجتمع القانوني المصري.
