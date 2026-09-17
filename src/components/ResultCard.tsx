import React, { useState } from 'react';
import {
  CalendarPlus,
  Bookmark,
  RotateCcw,
  Copy,
  Check,
  Printer,
  ExternalLink,
  Bell,
  BellRing,
  AlertTriangle,
  Scale,
  CalendarDays,
  ShieldCheck,
  CheckCircle2,
  Clock
} from 'lucide-react';
import { DeadlineResult, Holiday, LegalRule } from '../types';
import { formatDisplayDate, getArabicDayName, parseIsoDate } from '../utils/calculator';
import { calculateProactiveAlertDate, createGoogleCalendarUrl } from '../utils/calendar';
import { CalculationTimeline } from './CalculationTimeline';

interface ResultCardProps {
  result: DeadlineResult;
  selectedRule: LegalRule;
  announcementDate: string;
  caseNumber: string;
  clientName: string;
  additionalDistanceDays: number;
  holidays: Holiday[];
  proactiveReminderDays: number;
  setProactiveReminderDays: (days: number) => void;
  calendarTarget: 'FINAL_DEADLINE' | 'PROACTIVE_ALERT';
  setCalendarTarget: (target: 'FINAL_DEADLINE' | 'PROACTIVE_ALERT') => void;
  onSave: () => void;
  isSaved: boolean;
  onReset: () => void;
}

export const ResultCard: React.FC<ResultCardProps> = ({
  result,
  selectedRule,
  announcementDate,
  caseNumber,
  clientName,
  additionalDistanceDays,
  holidays,
  proactiveReminderDays,
  setProactiveReminderDays,
  calendarTarget,
  setCalendarTarget,
  onSave,
  isSaved,
  onReset
}) => {
  const [copied, setCopied] = useState(false);
  const [showTimeline, setShowTimeline] = useState(true);

  const finalDate = parseIsoDate(result.finalDeadline);
  const isFinalSaturday = finalDate.getDay() === 6;

  // Countdown calculations
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  const finalDateTime = new Date(finalDate);
  finalDateTime.setHours(0, 0, 0, 0);
  const diffDays = Math.round(
    (finalDateTime.getTime() - today.getTime()) / (1000 * 60 * 60 * 24)
  );

  // Proactive date calculations
  const proactiveDateIso =
    proactiveReminderDays > 0
      ? calculateProactiveAlertDate(result.finalDeadline, proactiveReminderDays)
      : null;

  const proactiveHoliday = proactiveDateIso
    ? holidays.find((h) => h.holidayDate === proactiveDateIso)
    : null;
  const isProactiveFriday = proactiveDateIso
    ? parseIsoDate(proactiveDateIso).getDay() === 5
    : false;

  const googleCalendarUrl = createGoogleCalendarUrl({
    title: selectedRule.actionName,
    finalDeadlineDate: result.finalDeadline,
    lawArticle: result.lawArticle,
    explanation: result.explanation,
    notes: result.notes,
    caseNumber: caseNumber.trim(),
    clientName: clientName.trim(),
    proactiveReminderDays,
    calendarTarget
  });

  const proactiveDirectCalendarUrl =
    proactiveReminderDays > 0
      ? createGoogleCalendarUrl({
          title: selectedRule.actionName,
          finalDeadlineDate: result.finalDeadline,
          lawArticle: result.lawArticle,
          explanation: result.explanation,
          notes: result.notes,
          caseNumber: caseNumber.trim(),
          clientName: clientName.trim(),
          proactiveReminderDays,
          calendarTarget: 'PROACTIVE_ALERT'
        })
      : '';

  // Copy structured summary to clipboard
  const handleCopySummary = () => {
    const summary = `⚖️ تقرير احتساب الميعاد القضائي
---------------------------------
• الإجراء: ${selectedRule.actionName}
• السند القانوني: ${selectedRule.lawArticle}
${caseNumber ? `• رقم القضية: ${caseNumber}\n` : ''}${clientName ? `• الموكل: ${clientName}\n` : ''}• تاريخ الإعلان: ${formatDisplayDate(parseIsoDate(announcementDate))} (${getArabicDayName(parseIsoDate(announcementDate))})
• مدة الميعاد: ${result.duration} ${result.unit === 'DAYS' ? 'أيام' : result.unit === 'MONTHS' ? 'أشهر' : 'سنوات'}${result.totalDistanceDays > 0 ? ` (+ ${result.totalDistanceDays} أيام مسافة)` : ''}
• الميعاد النهائي الملزم: ${formatDisplayDate(finalDate)} (${getArabicDayName(finalDate)})
${isFinalSaturday ? '• ملاحظة: يوم السبت يوم عمل رسمي في المحاكم المصرية.\n' : ''}• بيان الاحتساب:
${result.explanation}
---------------------------------
تم الاحتساب آلياً بواسطة حاسبة المواعيد القانونية المصرية`;

    navigator.clipboard.writeText(summary);
    setCopied(true);
    setTimeout(() => setCopied(false), 2500);
  };

  const handlePrint = () => {
    window.print();
  };

  return (
    <div className="space-y-6 animate-in fade-in-50 duration-300">
      {/* Main Result Card */}
      <div className="bg-white border-2 border-amber-500/30 rounded-2xl p-6 sm:p-8 space-y-6 shadow-lg shadow-amber-950/5 relative overflow-hidden">
        {/* Top Gold Ribbon */}
        <div className="absolute top-0 right-0 left-0 h-1.5 bg-gradient-to-r from-amber-500 via-amber-400 to-amber-600"></div>

        {/* Card Header */}
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 pb-4 border-b border-slate-100">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-xl bg-emerald-500/10 border border-emerald-500/20 text-emerald-700 flex items-center justify-center shrink-0">
              <ShieldCheck className="w-6 h-6" />
            </div>
            <div>
              <h3 className="text-base sm:text-lg font-bold text-slate-900">
                نتيجة الاحتساب الإجرائي النهائي
              </h3>
              <p className="text-xs text-slate-500">
                ميعاد قضائي محصن قانوناً وفقاً لمواد قانون المرافعات المصري
              </p>
            </div>
          </div>

          {/* Countdown Pill */}
          <div className="self-start sm:self-auto">
            {diffDays > 0 ? (
              <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-bold bg-emerald-50 text-emerald-800 border border-emerald-200">
                <Clock className="w-3.5 h-3.5 text-emerald-600" />
                متبقي على الميعاد: {diffDays} {diffDays === 1 ? 'يوم' : diffDays === 2 ? 'يومان' : 'أيام'}
              </span>
            ) : diffDays === 0 ? (
              <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-bold bg-amber-100 text-amber-900 border border-amber-300 animate-pulse">
                <AlertTriangle className="w-3.5 h-3.5 text-amber-700" />
                اليوم هو الميعاد النهائي الأخير!
              </span>
            ) : (
              <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-bold bg-red-50 text-red-800 border border-red-200">
                <AlertTriangle className="w-3.5 h-3.5 text-red-600" />
                انقضى الميعاد منذ {Math.abs(diffDays)} يوم
              </span>
            )}
          </div>
        </div>

        {/* Hero Final Date Box */}
        <div className="bg-gradient-to-br from-[#0F2027] via-[#1B3542] to-[#203A43] text-white p-6 sm:p-8 rounded-2xl text-center space-y-3 shadow-xl relative overflow-hidden">
          <div className="text-amber-400/90 text-xs font-bold tracking-widest uppercase flex items-center justify-center gap-2">
            <Scale className="w-4 h-4 text-amber-400" />
            <span>الميعاد القانوني النهائي واجب الالتزام به</span>
            <Scale className="w-4 h-4 text-amber-400" />
          </div>

          <div className="text-3xl sm:text-5xl font-black tracking-tight text-white font-['Cairo']" dir="rtl">
            <span dir="ltr" className="font-mono">{formatDisplayDate(finalDate)}</span> ({getArabicDayName(finalDate)})
          </div>

          <div className="flex flex-wrap items-center justify-center gap-2 pt-2">
            {isFinalSaturday ? (
              <span className="px-3.5 py-1 rounded-full text-xs font-bold bg-emerald-500/20 text-emerald-300 border border-emerald-400/40 flex items-center gap-1.5">
                <CheckCircle2 className="w-3.5 h-3.5 text-emerald-400" />
                السبت: يوم عمل رسمي كامل في المحاكم المصرية
              </span>
            ) : (
              <span className="px-3 py-1 rounded-full text-xs font-semibold bg-white/10 text-slate-200 border border-white/15">
                يوم عمل قضائي رسمي
              </span>
            )}

            {result.excludedDays.length > 0 && (
              <span className="px-3.5 py-1 rounded-full text-xs font-bold bg-amber-500/20 text-amber-300 border border-amber-400/40">
                امتد الميعاد رسمياً لمصادفة عطلة
              </span>
            )}
          </div>
        </div>

        {/* Legal Article Badge */}
        <div className="p-4 bg-slate-50 border border-slate-200 rounded-xl space-y-1">
          <span className="text-[11px] font-bold text-amber-700 uppercase tracking-wider block">
            السند التشريعي المعتمد:
          </span>
          <p className="text-sm font-bold text-slate-900">
            {result.lawArticle}
          </p>
          {result.notes && (
            <p className="text-xs text-slate-600 mt-1 leading-relaxed">
              {result.notes}
            </p>
          )}
        </div>

        {/* Toggle Timeline Button */}
        <div className="flex items-center justify-between pt-2">
          <button
            type="button"
            onClick={() => setShowTimeline(!showTimeline)}
            className="text-xs font-bold text-slate-700 hover:text-amber-700 transition-colors flex items-center gap-1.5 cursor-pointer"
          >
            <span>{showTimeline ? 'إخفاء المسار الزمني التفصيلي' : 'عرض المسار الزمني التفصيلي للاحتساب'}</span>
          </button>
        </div>

        {/* Step-by-Step Procedural Timeline */}
        {showTimeline && (
          <CalculationTimeline
            result={result}
            announcementDateStr={announcementDate}
          />
        )}

        {/* Detailed Explanation */}
        <div className="space-y-2">
          <label className="text-xs font-bold text-slate-700 uppercase tracking-wider block">
            بيان الاحتساب الإجرائي:
          </label>
          <div className="p-4 bg-slate-50 border border-slate-200/80 rounded-xl text-xs sm:text-sm text-slate-800 whitespace-pre-line leading-relaxed font-sans">
            {result.explanation}
          </div>
        </div>

        {/* Proactive Reminder & Google Calendar Settings */}
        <div className="p-5 bg-blue-50/50 border border-blue-200/80 rounded-2xl space-y-4">
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2 pb-3 border-b border-blue-200/60">
            <div className="flex items-center gap-2.5">
              <div className="w-8 h-8 rounded-xl bg-blue-600 text-white flex items-center justify-center shrink-0">
                <Bell className="w-4 h-4" />
              </div>
              <div>
                <h4 className="text-sm font-bold text-blue-950">
                  التنبيه الاستباقي ومزامنة تقويم Google
                </h4>
                <p className="text-xs text-blue-800/80">
                  تحديد تذكير مسبق قبل حلول الميعاد النهائي لتجهيز المذكرة والصحيفة في وقت كافٍ
                </p>
              </div>
            </div>

            {proactiveReminderDays > 0 && (
              <span className="text-xs font-bold bg-blue-600 text-white px-3 py-1 rounded-full self-start sm:self-auto shadow-xs">
                تنبيه مبكر: قبل {proactiveReminderDays} {proactiveReminderDays === 1 ? 'يوم' : proactiveReminderDays === 2 ? 'يومين' : 'أيام'}
              </span>
            )}
          </div>

          {/* Days selector */}
          <div className="space-y-2">
            <label className="text-[11px] font-bold text-blue-900 block">
              اختر موعد التنبيه المسبق قبل الميعاد النهائي:
            </label>
            <div className="grid grid-cols-2 sm:grid-cols-6 gap-2">
              {[
                { days: 1, label: 'قبل بيوم' },
                { days: 2, label: 'قبل بيومين' },
                { days: 3, label: 'قبل 3 أيام', recommended: true },
                { days: 5, label: 'قبل 5 أيام' },
                { days: 7, label: 'قبل أسبوع' },
                { days: 0, label: 'بدون تنبيه' }
              ].map((opt) => {
                const isSelected = proactiveReminderDays === opt.days;
                return (
                  <button
                    key={opt.days}
                    type="button"
                    onClick={() => setProactiveReminderDays(opt.days)}
                    className={`py-2 px-2.5 rounded-xl text-xs font-semibold border transition-all text-center cursor-pointer ${
                      isSelected
                        ? 'bg-blue-600 text-white border-blue-700 shadow-sm'
                        : 'bg-white text-slate-700 border-blue-200/80 hover:bg-blue-50'
                    }`}
                  >
                    <span>{opt.label}</span>
                    {opt.recommended && (
                      <span
                        className={`block text-[9px] font-bold mt-0.5 ${
                          isSelected ? 'text-blue-100' : 'text-blue-600'
                        }`}
                      >
                        مستحسن
                      </span>
                    )}
                  </button>
                );
              })}
            </div>
          </div>

          {/* Calculated Proactive Date Feedback */}
          {proactiveReminderDays > 0 && proactiveDateIso && (
            <div className="p-3.5 bg-white border border-blue-200 rounded-xl space-y-2 text-xs">
              <div className="flex items-center justify-between flex-wrap gap-2">
                <div className="flex items-center gap-2">
                  <CalendarDays className="w-4 h-4 text-blue-600 shrink-0" />
                  <span className="font-bold text-slate-800">موعد التنبيه الاستباقي:</span>
                  <span className="font-bold text-blue-900 bg-blue-50 px-2.5 py-0.5 rounded border border-blue-200" dir="rtl">
                    <span dir="ltr" className="font-mono">{formatDisplayDate(parseIsoDate(proactiveDateIso))}</span> ({getArabicDayName(parseIsoDate(proactiveDateIso))})
                  </span>
                </div>
                <span className="text-[11px] text-slate-500">
                  (يسبق الميعاد بـ {proactiveReminderDays} {proactiveReminderDays === 1 ? 'يوم' : 'أيام'})
                </span>
              </div>

              {isProactiveFriday && (
                <div className="p-2 bg-amber-50 border border-amber-200 rounded-lg text-amber-900 text-[11px] flex items-center gap-2">
                  <AlertTriangle className="w-3.5 h-3.5 text-amber-600 shrink-0" />
                  <span>
                    تنبيه: يوم التذكير يوافق يوم جمعة (عطلة رسمية)، لذا يوصى ببدء إعداد المذكرة قبلها بيوم عمل.
                  </span>
                </div>
              )}

              {proactiveHoliday && (
                <div className="p-2 bg-amber-50 border border-amber-200 rounded-lg text-amber-900 text-[11px] flex items-center gap-2">
                  <AlertTriangle className="w-3.5 h-3.5 text-amber-600 shrink-0" />
                  <span>
                    تنبيه: يوم التذكير يوافق عطلة رسمية ({proactiveHoliday.name}).
                  </span>
                </div>
              )}
            </div>
          )}

          {/* Target choice */}
          {proactiveReminderDays > 0 && (
            <div className="space-y-1.5">
              <label className="text-[11px] font-bold text-blue-900 block">
                تحديد وجهة الحدث في رابط تقويم Google:
              </label>
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-2">
                <button
                  type="button"
                  onClick={() => setCalendarTarget('FINAL_DEADLINE')}
                  className={`p-3 text-right rounded-xl text-xs border transition-all cursor-pointer ${
                    calendarTarget === 'FINAL_DEADLINE'
                      ? 'bg-white border-blue-600 ring-2 ring-blue-600/20 shadow-xs'
                      : 'bg-white/80 border-blue-200 hover:bg-white'
                  }`}
                >
                  <div className="font-bold text-slate-900 flex items-center justify-between">
                    <span>جدولة الميعاد النهائي</span>
                    {calendarTarget === 'FINAL_DEADLINE' && (
                      <Check className="w-4 h-4 text-blue-600" />
                    )}
                  </div>
                  <p className="text-[11px] text-slate-500 mt-1">
                    تثبيت الحدث في يوم الميعاد النهائي وإدراج موعد التنبيه في التفاصيل.
                  </p>
                </button>

                <button
                  type="button"
                  onClick={() => setCalendarTarget('PROACTIVE_ALERT')}
                  className={`p-3 text-right rounded-xl text-xs border transition-all cursor-pointer ${
                    calendarTarget === 'PROACTIVE_ALERT'
                      ? 'bg-white border-blue-600 ring-2 ring-blue-600/20 shadow-xs'
                      : 'bg-white/80 border-blue-200 hover:bg-white'
                  }`}
                >
                  <div className="font-bold text-slate-900 flex items-center justify-between">
                    <span>جدولة التنبيه الاستباقي</span>
                    {calendarTarget === 'PROACTIVE_ALERT' && (
                      <Check className="w-4 h-4 text-blue-600" />
                    )}
                  </div>
                  <p className="text-[11px] text-slate-500 mt-1">
                    تثبيت الحدث في يوم التنبيه المبكر لإشعارك بتجهيز أوراق القضية.
                  </p>
                </button>
              </div>
            </div>
          )}
        </div>

        {/* Action Buttons Toolbar */}
        <div className="pt-4 border-t border-slate-100 flex flex-wrap items-center justify-between gap-3">
          <div className="flex flex-wrap items-center gap-2.5 w-full sm:w-auto">
            {/* Google Calendar Link */}
            <a
              href={googleCalendarUrl}
              target="_blank"
              rel="noopener noreferrer"
              className="py-2.5 px-4 rounded-xl text-xs font-bold bg-blue-600 hover:bg-blue-700 text-white transition-all flex items-center gap-2 shadow-xs cursor-pointer no-underline"
            >
              <CalendarPlus className="w-4 h-4" />
              <span>إضافة لتقويم Google</span>
              <ExternalLink className="w-3 h-3 text-blue-200" />
            </a>

            {/* Copy Summary */}
            <button
              type="button"
              onClick={handleCopySummary}
              className="py-2.5 px-4 rounded-xl text-xs font-bold bg-slate-100 hover:bg-slate-200 text-slate-800 transition-all flex items-center gap-1.5 cursor-pointer border border-slate-200"
            >
              {copied ? (
                <>
                  <Check className="w-4 h-4 text-emerald-600" />
                  <span className="text-emerald-700 font-bold">تم نسخ التقرير!</span>
                </>
              ) : (
                <>
                  <Copy className="w-4 h-4 text-slate-600" />
                  <span>نسخ التقرير</span>
                </>
              )}
            </button>

            {/* Print */}
            <button
              type="button"
              onClick={handlePrint}
              className="py-2.5 px-3.5 rounded-xl text-xs font-bold bg-slate-100 hover:bg-slate-200 text-slate-700 transition-all flex items-center gap-1.5 cursor-pointer border border-slate-200"
              title="طباعة تقرير الميعاد"
            >
              <Printer className="w-4 h-4" />
              <span>طباعة</span>
            </button>

            {/* Save Button */}
            <button
              type="button"
              onClick={onSave}
              disabled={isSaved}
              className={`py-2.5 px-5 rounded-xl text-xs font-bold transition-all flex items-center gap-2 cursor-pointer ${
                isSaved
                  ? 'bg-emerald-600 text-white cursor-default shadow-xs'
                  : 'bg-[#0F2027] hover:bg-black text-amber-400 shadow-md'
              }`}
            >
              <Bookmark className="w-4 h-4" />
              <span>{isSaved ? 'تم الحفظ في المحفوظات' : 'حفظ الميعاد'}</span>
            </button>
          </div>

          {/* Reset button */}
          <button
            type="button"
            onClick={onReset}
            className="py-2.5 px-4 rounded-xl text-xs font-semibold text-slate-500 hover:text-slate-800 hover:bg-slate-100 transition-colors flex items-center gap-1.5 cursor-pointer mr-auto sm:mr-0"
          >
            <RotateCcw className="w-3.5 h-3.5" />
            <span>حساب جديد</span>
          </button>
        </div>

        {/* Direct Link to proactive calendar event if dual */}
        {proactiveReminderDays > 0 && calendarTarget === 'FINAL_DEADLINE' && proactiveDateIso && (
          <div className="pt-2">
            <a
              href={proactiveDirectCalendarUrl}
              target="_blank"
              rel="noopener noreferrer"
              className="text-xs text-blue-700 hover:text-blue-900 font-medium flex items-center gap-1.5"
            >
              <BellRing className="w-3.5 h-3.5 text-blue-600" />
              <span>
                رابط إضافي: إضافة حدث التنبيه الاستباقي مباشرة لتقويم Google بتاريخ (
                <span dir="ltr" className="font-mono">{formatDisplayDate(parseIsoDate(proactiveDateIso))}</span> {getArabicDayName(parseIsoDate(proactiveDateIso))})
              </span>
              <ExternalLink className="w-3 h-3 text-blue-400" />
            </a>
          </div>
        )}
      </div>
    </div>
  );
};
