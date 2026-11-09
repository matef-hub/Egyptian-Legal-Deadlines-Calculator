import React from 'react';
import {
  Calendar,
  CheckCircle2,
  AlertTriangle,
  ArrowDown,
  Clock,
  Check,
  Building2,
  CalendarCheck
} from 'lucide-react';
import { DeadlineResult } from '../types';
import { formatDisplayDate, getArabicDayName, parseIsoDate } from '../utils/calculator';

interface CalculationTimelineProps {
  result: DeadlineResult;
  announcementDateStr: string;
}

export const CalculationTimeline: React.FC<CalculationTimelineProps> = ({
  result,
  announcementDateStr
}) => {
  const announceDate = parseIsoDate(announcementDateStr);
  const startDate = parseIsoDate(result.startDate);
  const provDate = parseIsoDate(result.provisionalDate);
  const finalDate = parseIsoDate(result.finalDeadline);

  const isFinalSaturday = finalDate.getDay() === 6;
  const isFinalExtended = result.excludedDays.length > 0;

  return (
    <div className="bg-white border border-slate-200 rounded-2xl p-5 sm:p-6 space-y-5 shadow-xs">
      <div className="flex items-center justify-between pb-3 border-b border-slate-100">
        <div className="flex items-center gap-2.5">
          <div className="w-8 h-8 rounded-xl bg-amber-500/10 border border-amber-500/20 text-amber-600 flex items-center justify-center">
            <Clock className="w-4 h-4" />
          </div>
          <div>
            <h4 className="text-sm font-bold text-slate-900">
              المسار الزمني الإجرائي للاحتساب
            </h4>
            <p className="text-[11px] text-slate-500">
              تسلسل تطبيق مواد قانون المرافعات خطوة بخطوة
            </p>
          </div>
        </div>

        <span className="text-[10px] font-mono font-bold bg-slate-100 text-slate-700 px-2.5 py-1 rounded-full border border-slate-200">
          المادة 15 و 18 مرافعات
        </span>
      </div>

      {/* Steps Visual List */}
      <div className="relative pl-2 pr-4 space-y-4 before:absolute before:right-6 before:top-3 before:bottom-3 before:w-0.5 before:bg-slate-200">
        {/* Step 1: Announcement Date */}
        <div className="relative flex items-start gap-4">
          <div className="w-8 h-8 rounded-full bg-slate-900 text-white flex items-center justify-center text-xs font-bold shrink-0 z-10 ring-4 ring-white shadow-xs">
            1
          </div>
          <div className="flex-1 bg-slate-50 rounded-xl p-3.5 border border-slate-200/80">
            <div className="flex items-center justify-between flex-wrap gap-2">
              <span className="text-xs font-bold text-slate-800">
                تاريخ الإعلان أو صدور الحكم (يوم العمل المادي)
              </span>
              <span className="text-xs font-semibold text-slate-900 bg-white px-2 py-0.5 rounded border border-slate-200" dir="rtl">
                <span dir="ltr" className="font-mono">{formatDisplayDate(announceDate)}</span> ({getArabicDayName(announceDate)})
              </span>
            </div>
            <p className="text-[11px] text-slate-500 mt-1">
              <strong>قاعدة المادة 15 مرافعات:</strong> يوم الإعلان أو صدور الحكم لا يحسب في الميعاد القضائي.
            </p>
          </div>
        </div>

        {/* Step 2: Day of Start */}
        <div className="relative flex items-start gap-4">
          <div className="w-8 h-8 rounded-full bg-amber-600 text-white flex items-center justify-center text-xs font-bold shrink-0 z-10 ring-4 ring-white shadow-xs">
            2
          </div>
          <div className="flex-1 bg-amber-50/60 rounded-xl p-3.5 border border-amber-200/60">
            <div className="flex items-center justify-between flex-wrap gap-2">
              <span className="text-xs font-bold text-amber-950">
                بدء سريان الميعاد القانوني (اليوم التالي)
              </span>
              <span className="text-xs font-semibold text-amber-900 bg-white px-2 py-0.5 rounded border border-amber-200" dir="rtl">
                <span dir="ltr" className="font-mono">{formatDisplayDate(startDate)}</span> ({getArabicDayName(startDate)})
              </span>
            </div>
            <p className="text-[11px] text-amber-800/90 mt-1">
              يبدأ عد الأيام اعتباراً من هذا اليوم التالي للإعلان.
            </p>
          </div>
        </div>

        {/* Step 3: Duration & Distance */}
        <div className="relative flex items-start gap-4">
          <div className="w-8 h-8 rounded-full bg-slate-700 text-white flex items-center justify-center text-xs font-bold shrink-0 z-10 ring-4 ring-white shadow-xs">
            3
          </div>
          <div className="flex-1 bg-slate-50 rounded-xl p-3.5 border border-slate-200/80">
            <div className="flex items-center justify-between flex-wrap gap-2">
              <span className="text-xs font-bold text-slate-800">
                المدة المقررة نظاماً + ميعاد المسافة
              </span>
              <span className="text-xs font-semibold text-slate-900 bg-white px-2 py-0.5 rounded border border-slate-200">
                {result.duration} {result.unit === 'DAYS' ? 'أيام' : result.unit === 'MONTHS' ? 'أشهر' : 'سنوات'}
                {result.totalDistanceDays > 0 && ` + ${result.totalDistanceDays} أيام مسافة`}
              </span>
            </div>
            <p className="text-[11px] text-slate-500 mt-1" dir="rtl">
              تاريخ الانتهاء المبدئي الحسابي: <span dir="ltr" className="font-mono font-medium">{formatDisplayDate(provDate)}</span> ({getArabicDayName(provDate)})
            </p>
          </div>
        </div>

        {/* Step 4: Extension due to Holiday or Friday (if any) */}
        {isFinalExtended && (
          <div className="relative flex items-start gap-4">
            <div className="w-8 h-8 rounded-full bg-red-600 text-white flex items-center justify-center text-xs font-bold shrink-0 z-10 ring-4 ring-white shadow-xs">
              4
            </div>
            <div className="flex-1 bg-red-50/60 rounded-xl p-3.5 border border-red-200/70 space-y-1.5">
              <div className="flex items-center gap-2 text-xs font-bold text-red-900">
                <AlertTriangle className="w-4 h-4 text-red-600" />
                <span>امتداد الميعاد لمصادفة عطلة رسمية أو جمعة (المادة 18 مرافعات)</span>
              </div>
              <p className="text-[11px] text-red-800">
                صادف انتهاء الميعاد الأيام التالية فامتد وجوباً لأول يوم عمل تالٍ:
              </p>
              <ul className="text-[11px] text-red-900 font-medium space-y-1 pr-4 list-disc">
                {result.excludedDays.map((ex, idx) => (
                  <li key={idx}>
                    {ex.date} — {ex.reason}
                  </li>
                ))}
              </ul>
            </div>
          </div>
        )}

        {/* Step 5: Final Legally Fortified Deadline */}
        <div className="relative flex items-start gap-4">
          <div className="w-8 h-8 rounded-full bg-emerald-600 text-white flex items-center justify-center text-xs font-bold shrink-0 z-10 ring-4 ring-white shadow-md">
            <Check className="w-4 h-4" />
          </div>
          <div className="flex-1 bg-emerald-50/80 rounded-xl p-4 border border-emerald-300 shadow-xs">
            <div className="flex items-center justify-between flex-wrap gap-2">
              <span className="text-xs font-bold text-emerald-950 flex items-center gap-1.5">
                <CalendarCheck className="w-4 h-4 text-emerald-700" />
                الميعاد النهائي المحصن قانوناً
              </span>
              <span className="text-sm font-extrabold text-emerald-950 bg-white px-3 py-1 rounded-lg border border-emerald-300" dir="rtl">
                <span dir="ltr" className="font-mono">{formatDisplayDate(finalDate)}</span> ({getArabicDayName(finalDate)})
              </span>
            </div>

            {isFinalSaturday && (
              <div className="mt-2.5 pt-2 border-t border-emerald-200/70 text-[11px] text-emerald-900 font-medium flex items-center gap-1.5">
                <Building2 className="w-3.5 h-3.5 text-emerald-700" />
                <span>
                  ملاحظة محورية: يوم السبت يعتبر يوم عمل رسمي كامل في كافة المحاكم المصرية ومكاتب كتاب المحاكم.
                </span>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};
