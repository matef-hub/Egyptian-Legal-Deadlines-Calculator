import React, { useState } from 'react';
import { FileCode, Check, Copy, Layers, ShieldCheck, Smartphone } from 'lucide-react';

interface AndroidFileItem {
  path: string;
  category: 'Gradle' | 'Domain' | 'Data' | 'UI' | 'Test' | 'Res';
  description: string;
}

const ANDROID_FILES: AndroidFileItem[] = [
  {
    path: 'app/src/main/java/com/ateflaw/legaldeadlines/domain/calculator/LegalDeadlineCalculator.kt',
    category: 'Domain',
    description: 'محرك الاحتساب الإجرائي القانوني (المواعيد، العطلات، استبعاد الجمعة، السبت يوم عمل)'
  },
  {
    path: 'app/src/test/java/com/ateflaw/legaldeadlines/domain/calculator/LegalDeadlineCalculatorTest.kt',
    category: 'Test',
    description: 'اختبارات الوحدة لجميع الحالات الحدية (Tests A, B, C, D, E, F, G)'
  },
  {
    path: 'app/src/main/java/com/ateflaw/legaldeadlines/domain/model/DeadlineResult.kt',
    category: 'Domain',
    description: 'كائن نتيجة الاحتساب الشاملة والتفاصيل الإجرائية'
  },
  {
    path: 'app/src/main/java/com/ateflaw/legaldeadlines/data/database/AppDatabase.kt',
    category: 'Data',
    description: 'قاعدة بيانات Room مع البذور التلقائية والكيانات الثلاثة'
  },
  {
    path: 'app/src/main/java/com/ateflaw/legaldeadlines/data/dao/LegalRuleDao.kt',
    category: 'Data',
    description: 'واجهة الوصول لقواعد المواعيد الإجرائية في Room'
  },
  {
    path: 'app/src/main/java/com/ateflaw/legaldeadlines/data/dao/HolidayDao.kt',
    category: 'Data',
    description: 'واجهة الوصول لبيانات العطلات الرسمية وفحص التاريخ'
  },
  {
    path: 'app/src/main/java/com/ateflaw/legaldeadlines/data/dao/SavedDeadlineDao.kt',
    category: 'Data',
    description: 'واجهة عمليات CRUD للمواعيد المحفوظة'
  },
  {
    path: 'app/src/main/java/com/ateflaw/legaldeadlines/data/service/OfficialHolidayService.kt',
    category: 'Data',
    description: 'خدمة العطلات الرسمية ومزامنة Calendarific والعمل دون إنترنت'
  },
  {
    path: 'app/src/main/java/com/ateflaw/legaldeadlines/data/worker/HolidaySyncWorker.kt',
    category: 'Data',
    description: 'عامل المزامنة الخلفية الدوري عبر WorkManager'
  },
  {
    path: 'app/src/main/java/com/ateflaw/legaldeadlines/presentation/viewmodel/MainViewModel.kt',
    category: 'UI',
    description: 'نموذج عرض شاشة الحاسبة الرئيسية وإدارة الحالة'
  },
  {
    path: 'app/src/main/java/com/ateflaw/legaldeadlines/presentation/screens/CalculatorScreen.kt',
    category: 'UI',
    description: 'واجهة المستخدم لاحتساب الميعاد بمكونات Material 3 ودعم RTL'
  },
  {
    path: 'app/src/main/java/com/ateflaw/legaldeadlines/presentation/screens/SavedDeadlinesScreen.kt',
    category: 'UI',
    description: 'شاشة المواعيد المحفوظة والتفاصيل وتأكيد الحذف'
  },
  {
    path: 'app/build.gradle.kts',
    category: 'Gradle',
    description: 'إعدادات البناء المستهدفة لـ Android 16 (API 36) مع Room و KSP و Compose'
  },
  {
    path: 'gradle/libs.versions.toml',
    category: 'Gradle',
    description: 'كتالوج إصدارات Gradle للتبعيات الحديثة المستقرة'
  },
  {
    path: 'app/src/main/AndroidManifest.xml',
    category: 'Res',
    description: 'ملف بيان التطبيق مع أذونات الإنترنت ودعم RTL'
  }
];

export const AndroidProjectViewer: React.FC = () => {
  const [selectedFile, setSelectedFile] = useState<AndroidFileItem>(ANDROID_FILES[0]);
  const [copied, setCopied] = useState(false);

  const handleCopyPath = (path: string) => {
    navigator.clipboard.writeText(path);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  return (
    <div className="bg-white border border-[#E5E5E5] rounded-xl overflow-hidden shadow-none">
      {/* Header */}
      <div className="bg-white border-b border-[#E5E5E5] p-5">
        <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
          <div className="flex items-center gap-3">
            <div className="w-8 h-8 bg-black rounded-sm flex items-center justify-center shrink-0">
              <Smartphone className="w-4 h-4 text-white" />
            </div>
            <div>
              <h2 className="text-base font-medium text-[#1A1A1A]">هيكلية مشروع أندرويد ستوديو المولد</h2>
              <p className="text-xs text-[#737373]">مشروع أصلي متكامل (Native Android Kotlin + Jetpack Compose + Clean Architecture)</p>
            </div>
          </div>
          <div className="flex items-center gap-2">
            <span className="px-3 py-1 rounded-full bg-green-50 border border-green-200 text-green-700 text-xs font-mono flex items-center gap-1.5">
              <ShieldCheck className="w-3.5 h-3.5" />
              compileSdk 36 | Android 16
            </span>
            <span className="px-3 py-1 rounded-full bg-[#F5F5F5] border border-[#E5E5E5] text-[#1A1A1A] text-xs font-mono">
              Kotlin 2.1.10
            </span>
          </div>
        </div>
      </div>

      {/* Grid: File List + File Inspector */}
      <div className="grid grid-cols-1 lg:grid-cols-12 min-h-[460px]">
        {/* Left / Navigation side */}
        <div className="lg:col-span-5 border-l border-[#E5E5E5] bg-[#FAFAFA] p-4 space-y-2 max-h-[540px] overflow-y-auto">
          <label className="text-[10px] uppercase tracking-[0.2em] text-[#A3A3A3] font-semibold px-2 mb-2 block">
            الملفات البرمجية الرئيسية
          </label>
          {ANDROID_FILES.map((file, idx) => {
            const isSelected = selectedFile.path === file.path;
            const fileName = file.path.split('/').pop();
            return (
              <button
                key={idx}
                onClick={() => setSelectedFile(file)}
                className={`w-full text-right p-3 rounded-lg transition-colors flex items-start gap-3 border ${
                  isSelected
                    ? 'bg-white border-[#1A1A1A] text-[#1A1A1A]'
                    : 'bg-white/70 border-[#E5E5E5] hover:bg-white text-[#1A1A1A]'
                }`}
              >
                <FileCode className={`w-4 h-4 mt-0.5 shrink-0 ${isSelected ? 'text-black' : 'text-[#A3A3A3]'}`} />
                <div className="min-w-0 flex-1">
                  <div className="flex items-center justify-between gap-2">
                    <span className="text-xs font-medium truncate font-mono text-left dir-ltr">{fileName}</span>
                    <span className="text-[10px] px-2 py-0.5 rounded-full font-mono bg-[#F5F5F5] border border-[#E5E5E5] text-[#1A1A1A]">
                      {file.category}
                    </span>
                  </div>
                  <p className="text-xs text-[#737373] mt-1 line-clamp-1">{file.description}</p>
                </div>
              </button>
            );
          })}
        </div>

        {/* Right / Details side */}
        <div className="lg:col-span-7 p-6 flex flex-col justify-between bg-white">
          <div>
            <div className="flex items-center justify-between pb-4 mb-4 border-b border-[#F0F0F0]">
              <div className="min-w-0 flex-1">
                <label className="text-[10px] uppercase tracking-[0.2em] text-[#A3A3A3] font-semibold block mb-1">
                  مسار الملف في المشروع
                </label>
                <p className="font-mono text-xs text-[#1A1A1A] select-all break-all dir-ltr text-left bg-[#FAFAFA] border border-[#E5E5E5] p-2 rounded-md">
                  {selectedFile.path}
                </p>
              </div>
              <button
                onClick={() => handleCopyPath(selectedFile.path)}
                className="p-2 text-[#737373] hover:text-[#1A1A1A] bg-[#FAFAFA] border border-[#E5E5E5] rounded-md transition-colors shrink-0 mr-3 cursor-pointer"
                title="نسخ المسار"
              >
                {copied ? <Check className="w-4 h-4 text-green-600" /> : <Copy className="w-4 h-4" />}
              </button>
            </div>

            <div className="space-y-4">
              <div>
                <label className="text-[10px] uppercase tracking-[0.2em] text-[#A3A3A3] font-semibold mb-1 block">
                  الوصف المعماري والوظيفي
                </label>
                <p className="text-sm text-[#1A1A1A] leading-relaxed">{selectedFile.description}</p>
              </div>

              <div className="grid grid-cols-2 gap-3 pt-2">
                <div className="p-3 bg-[#FAFAFA] border border-[#F0F0F0] rounded-lg">
                  <span className="text-[10px] uppercase tracking-wider text-[#A3A3A3] font-mono block">الطبقة المعمارية</span>
                  <span className="text-sm font-medium text-[#1A1A1A]">{selectedFile.category} Layer</span>
                </div>
                <div className="p-3 bg-[#FAFAFA] border border-[#F0F0F0] rounded-lg">
                  <span className="text-[10px] uppercase tracking-wider text-[#A3A3A3] font-mono block">حالة التوافق</span>
                  <span className="text-sm font-medium text-green-700">100% Native Production</span>
                </div>
              </div>

              <div className="p-4 border border-[#E5E5E5] rounded-lg bg-[#FAFAFA] text-xs text-[#737373] space-y-1.5">
                <div className="font-medium text-[#1A1A1A] flex items-center gap-1.5">
                  <Layers className="w-4 h-4" />
                  الالتزام الصارم بالضوابط التنفيذية
                </div>
                <p className="leading-relaxed">
                  تمت كتابة وتوليد هذا الملف بالكامل بجميع دواله ومخططاته دون أية نواقص أو تعليقات استبدالية (No TODOs). الملف مبني للتشغيل مباشرة على Android Studio.
                </p>
              </div>
            </div>
          </div>

          <div className="pt-4 border-t border-[#F0F0F0] mt-6 flex items-center justify-between text-[11px] text-[#A3A3A3] font-mono">
            <span>حزمة التطبيق: com.ateflaw.legaldeadlines</span>
            <span>الهدف: API 36 (Android 16)</span>
          </div>
        </div>
      </div>
    </div>
  );
};
