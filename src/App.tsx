import React, { useState, useEffect } from 'react';
import {
  Calendar,
  Clock,
  Bookmark,
  Calculator,
  Trash2,
  Folder,
  User,
  ShieldCheck,
  AlertCircle,
  CheckCircle2,
  CalendarDays,
  CalendarPlus,
  ExternalLink,
  Smartphone,
  ChevronDown,
  ChevronUp,
  RotateCcw,
  Check
} from 'lucide-react';
import { DeadlineResult, Holiday, LegalRule, SavedDeadline } from './types';
import { INITIAL_LEGAL_RULES, OFFICIAL_EGYPTIAN_HOLIDAYS } from './data/legalRules';
import {
  calculateLegalDeadline,
  formatDisplayDate,
  getArabicDayName,
  toIsoDate
} from './utils/calculator';
import { createGoogleCalendarUrl } from './utils/calendar';
import { AndroidProjectViewer } from './components/AndroidProjectViewer';

export default function App() {
  const [activeTab, setActiveTab] = useState<'calculator' | 'saved' | 'android'>('calculator');

  // Rules & Holidays state
  const [rules] = useState<LegalRule[]>(INITIAL_LEGAL_RULES);
  const [holidays] = useState<Holiday[]>(OFFICIAL_EGYPTIAN_HOLIDAYS);

  // Form Inputs
  const [selectedRuleId, setSelectedRuleId] = useState<number>(INITIAL_LEGAL_RULES[0].id);
  const [announcementDate, setAnnouncementDate] = useState<string>(toIsoDate(new Date()));
  const [additionalDistanceDays, setAdditionalDistanceDays] = useState<number>(0);
  const [caseNumber, setCaseNumber] = useState<string>('');
  const [clientName, setClientName] = useState<string>('');

  // Result state
  const [result, setResult] = useState<DeadlineResult | null>(null);
  const [savedDeadlines, setSavedDeadlines] = useState<SavedDeadline[]>(() => {
    try {
      const stored = localStorage.getItem('egyptian_legal_saved_deadlines');
      return stored ? JSON.parse(stored) : [];
    } catch {
      return [];
    }
  });

  const [deleteConfirmationId, setDeleteConfirmationId] = useState<string | null>(null);
  const [saveSuccessNotice, setSaveSuccessNotice] = useState<boolean>(false);
  const [expandedSavedId, setExpandedSavedId] = useState<string | null>(null);

  // Sync to local storage
  useEffect(() => {
    try {
      localStorage.setItem('egyptian_legal_saved_deadlines', JSON.stringify(savedDeadlines));
    } catch (e) {
      console.error('Failed to persist deadlines locally', e);
    }
  }, [savedDeadlines]);

  const selectedRule = rules.find((r) => r.id === selectedRuleId) || rules[0];

  // Google Calendar URL generator for current calculated result
  const googleCalendarUrl = result
    ? createGoogleCalendarUrl({
        title: selectedRule.actionName,
        finalDeadlineDate: result.finalDeadline,
        lawArticle: result.lawArticle,
        explanation: result.explanation,
        notes: result.notes,
        caseNumber: caseNumber.trim(),
        clientName: clientName.trim()
      })
    : '';

  // Today's status calculation
  const today = new Date();
  const todayIso = toIsoDate(today);
  const todayHoliday = holidays.find((h) => h.holidayDate === todayIso);
  const isTodayHoliday = !!todayHoliday;
  const todayHolidayName = todayHoliday?.name;

  const handleCalculate = () => {
    const calcResult = calculateLegalDeadline(
      announcementDate,
      selectedRule,
      additionalDistanceDays,
      holidays
    );
    setResult(calcResult);
    setSaveSuccessNotice(false);
  };

  const handleSave = () => {
    if (!result) return;
    const newDeadline: SavedDeadline = {
      id: Date.now().toString(),
      caseNumber: caseNumber.trim(),
      clientName: clientName.trim(),
      actionName: selectedRule.actionName,
      announcementDate,
      duration: result.duration,
      unit: result.unit,
      distanceDays: result.totalDistanceDays,
      finalDeadline: result.finalDeadline,
      lawArticle: result.lawArticle,
      calculationExplanation: result.explanation,
      notes: result.notes,
      createdAt: Date.now()
    };

    setSavedDeadlines((prev) => [newDeadline, ...prev]);
    setSaveSuccessNotice(true);
  };

  const handleDeleteConfirmed = () => {
    if (!deleteConfirmationId) return;
    setSavedDeadlines((prev) => prev.filter((d) => d.id !== deleteConfirmationId));
    setDeleteConfirmationId(null);
  };

  return (
    <div className="min-h-screen flex flex-col bg-[#FAFAFA] text-[#1A1A1A] antialiased">
      {/* Clean Minimalism Header */}
      <header className="h-16 border-b border-[#E5E5E5] bg-white flex items-center justify-between px-6 sm:px-8 sticky top-0 z-30">
        <div className="flex items-center gap-3">
          <div className="w-8 h-8 bg-black rounded-sm flex items-center justify-center shrink-0">
            <div className="w-3 h-3 bg-white rotate-45"></div>
          </div>
          <div>
            <h1 className="text-base sm:text-lg font-medium tracking-tight text-[#1A1A1A]">
              حاسبة المواعيد القانونية
            </h1>
            <p className="text-[11px] text-[#737373] hidden sm:block">
              قانون المرافعات المصري • السبت يوم عمل رسمي
            </p>
          </div>
        </div>

        {/* System Active Indicator & Nav Tabs */}
        <div className="flex items-center gap-3 sm:gap-6">
          <div className="hidden lg:flex items-center gap-5 text-xs text-[#737373]">
            <span className="flex items-center gap-2">
              <span className="w-2 h-2 rounded-full bg-green-500"></span>
              النظام نشط ومحدث
            </span>
            <span className="font-mono text-[#A3A3A3]">v4.0.12-EGY</span>
          </div>

          <div className="flex items-center gap-1 bg-[#FAFAFA] p-1 rounded-md border border-[#E5E5E5]">
            <button
              onClick={() => setActiveTab('calculator')}
              className={`px-3 py-1.5 rounded-md text-xs sm:text-sm font-medium transition-colors flex items-center gap-1.5 ${
                activeTab === 'calculator'
                  ? 'bg-[#1A1A1A] text-white'
                  : 'text-[#737373] hover:text-[#1A1A1A]'
              }`}
            >
              <Calculator className="w-3.5 h-3.5" />
              <span>الحاسبة</span>
            </button>
            <button
              onClick={() => setActiveTab('saved')}
              className={`px-3 py-1.5 rounded-md text-xs sm:text-sm font-medium transition-colors flex items-center gap-1.5 ${
                activeTab === 'saved'
                  ? 'bg-[#1A1A1A] text-white'
                  : 'text-[#737373] hover:text-[#1A1A1A]'
              }`}
            >
              <Bookmark className="w-3.5 h-3.5" />
              <span>المحفوظات</span>
              {savedDeadlines.length > 0 && (
                <span className="px-1.5 py-0.2 bg-[#F5F5F5] border border-[#E5E5E5] text-[#1A1A1A] rounded-full text-[10px] font-mono">
                  {savedDeadlines.length}
                </span>
              )}
            </button>
            <button
              onClick={() => setActiveTab('android')}
              className={`px-3 py-1.5 rounded-md text-xs sm:text-sm font-medium transition-colors flex items-center gap-1.5 ${
                activeTab === 'android'
                  ? 'bg-[#1A1A1A] text-white'
                  : 'text-[#737373] hover:text-[#1A1A1A]'
              }`}
            >
              <Smartphone className="w-3.5 h-3.5" />
              <span>مشروع أندرويد</span>
            </button>
          </div>
        </div>
      </header>

      {/* Main Content Area */}
      <main className="flex-1 max-w-5xl w-full mx-auto p-6 sm:p-8 space-y-6">
        {/* Status Bar */}
        <div className="bg-white border border-[#E5E5E5] p-5 rounded-xl flex flex-col md:flex-row md:items-center justify-between gap-4">
          <div className="flex items-center gap-4">
            <div className="w-10 h-10 rounded-full bg-[#FAFAFA] border border-[#E5E5E5] flex items-center justify-center shrink-0">
              <CalendarDays className="w-5 h-5 text-[#1A1A1A]" />
            </div>
            <div>
              <div className="flex items-center gap-2.5">
                <span className="text-sm font-medium text-[#1A1A1A]">
                  اليوم: {getArabicDayName(today)} {formatDisplayDate(today)}
                </span>
                <span
                  className={`text-xs px-2.5 py-0.5 rounded-full font-medium ${
                    isTodayHoliday
                      ? 'bg-amber-50 text-amber-800 border border-amber-200'
                      : 'bg-green-50 text-green-700 border border-green-200'
                  }`}
                >
                  {isTodayHoliday ? 'عطلة رسمية' : 'يوم عمل رسمي'}
                </span>
              </div>
              {isTodayHoliday && todayHolidayName && (
                <p className="text-xs text-[#737373] mt-0.5">المناسبة: {todayHolidayName}</p>
              )}
            </div>
          </div>

          <div className="flex items-center gap-2 text-xs text-[#737373] bg-[#FAFAFA] border border-[#F0F0F0] px-3.5 py-1.5 rounded-lg self-start md:self-auto font-mono">
            <ShieldCheck className="w-4 h-4 text-green-600" />
            <span>قاعدة بيانات العطلات المعتمدة: {holidays.length} عطلة</span>
          </div>
        </div>

        {/* TAB 1: Calculator */}
        {activeTab === 'calculator' && (
          <div className="space-y-6">
            {/* Calculation Form */}
            <div className="bg-white border border-[#E5E5E5] p-6 sm:p-8 rounded-xl space-y-6">
              <div className="flex justify-between items-end pb-2 border-b border-[#F0F0F0]">
                <div>
                  <h2 className="text-xl font-light tracking-tight text-[#1A1A1A] mb-1">
                    بيانات احتساب الميعاد
                  </h2>
                  <p className="text-xs text-[#737373]">
                    أدخل تفاصيل الإجراء وتاريخ الإعلان لاحتساب الميعاد النهائي الدقيق
                  </p>
                </div>
                <div className="hidden sm:block text-[10px] uppercase tracking-[0.2em] text-[#A3A3A3] font-mono">
                  STEP 01/02
                </div>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
                {/* Legal Action */}
                <div className="md:col-span-2">
                  <label className="text-[10px] uppercase tracking-[0.2em] text-[#A3A3A3] font-semibold mb-2 block">
                    الإجراء القضائي / الميعاد القانوني
                  </label>
                  <select
                    value={selectedRuleId}
                    onChange={(e) => setSelectedRuleId(Number(e.target.value))}
                    className="w-full bg-[#FAFAFA] border border-[#E5E5E5] rounded-lg px-3.5 py-2.5 text-sm text-[#1A1A1A] focus:bg-white focus:outline-none focus:border-[#1A1A1A] transition-colors"
                  >
                    {rules.map((rule) => (
                      <option key={rule.id} value={rule.id}>
                        {rule.actionName} ({rule.duration} {rule.unit === 'DAYS' ? 'أيام' : rule.unit === 'MONTHS' ? 'أشهر' : 'سنوات'})
                      </option>
                    ))}
                  </select>

                  {selectedRule && (
                    <div className="mt-2.5 p-3 border border-[#F0F0F0] rounded-lg bg-[#FAFAFA] text-xs text-[#737373] flex items-start gap-2">
                      <span className="font-semibold text-[#1A1A1A] shrink-0">السند التشريعي:</span>
                      <span>{selectedRule.lawArticle}</span>
                    </div>
                  )}
                </div>

                {/* Announcement Date */}
                <div>
                  <label className="text-[10px] uppercase tracking-[0.2em] text-[#A3A3A3] font-semibold mb-2 block">
                    تاريخ الإعلان / صدور الحكم
                  </label>
                  <input
                    type="date"
                    value={announcementDate}
                    onChange={(e) => setAnnouncementDate(e.target.value)}
                    className="w-full bg-[#FAFAFA] border border-[#E5E5E5] rounded-lg px-3.5 py-2.5 text-sm text-[#1A1A1A] focus:bg-white focus:outline-none focus:border-[#1A1A1A] transition-colors"
                  />
                  <div className="text-[11px] text-[#737373] mt-1.5 flex items-center justify-between">
                    <span>اليوم الموافق: {getArabicDayName(new Date(announcementDate))}</span>
                    <span className="font-mono text-[#A3A3A3]">DATE-INPUT</span>
                  </div>
                </div>

                {/* Additional Distance Days */}
                <div>
                  <label className="text-[10px] uppercase tracking-[0.2em] text-[#A3A3A3] font-semibold mb-2 block">
                    ميعاد المسافة الإضافي (بالأيام - اختياري)
                  </label>
                  <input
                    type="number"
                    min="0"
                    value={additionalDistanceDays}
                    onChange={(e) => setAdditionalDistanceDays(Math.max(0, parseInt(e.target.value) || 0))}
                    placeholder="0"
                    className="w-full bg-[#FAFAFA] border border-[#E5E5E5] rounded-lg px-3.5 py-2.5 text-sm text-[#1A1A1A] focus:bg-white focus:outline-none focus:border-[#1A1A1A] transition-colors"
                  />
                  <div className="text-[11px] text-[#737373] mt-1.5">
                    المسافة الأصلية للقاعدة: {selectedRule.distanceDays} يوم
                  </div>
                </div>

                {/* Case Number */}
                <div>
                  <label className="text-[10px] uppercase tracking-[0.2em] text-[#A3A3A3] font-semibold mb-2 block">
                    رقم الدعوى / القضية (اختياري)
                  </label>
                  <div className="relative">
                    <input
                      type="text"
                      value={caseNumber}
                      onChange={(e) => setCaseNumber(e.target.value)}
                      placeholder="مثال: 1042 لسنة 2026 مدني مستأنف"
                      className="w-full bg-[#FAFAFA] border border-[#E5E5E5] rounded-lg px-3.5 py-2.5 text-sm text-[#1A1A1A] focus:bg-white focus:outline-none focus:border-[#1A1A1A] transition-colors pl-9"
                    />
                    <Folder className="w-4 h-4 text-[#A3A3A3] absolute left-3 top-3" />
                  </div>
                </div>

                {/* Client Name */}
                <div>
                  <label className="text-[10px] uppercase tracking-[0.2em] text-[#A3A3A3] font-semibold mb-2 block">
                    اسم الموكل / الخصم (اختياري)
                  </label>
                  <div className="relative">
                    <input
                      type="text"
                      value={clientName}
                      onChange={(e) => setClientName(e.target.value)}
                      placeholder="مثال: أحمد عبد الرحمن"
                      className="w-full bg-[#FAFAFA] border border-[#E5E5E5] rounded-lg px-3.5 py-2.5 text-sm text-[#1A1A1A] focus:bg-white focus:outline-none focus:border-[#1A1A1A] transition-colors pl-9"
                    />
                    <User className="w-4 h-4 text-[#A3A3A3] absolute left-3 top-3" />
                  </div>
                </div>
              </div>

              {/* Action Button */}
              <div className="pt-4 border-t border-[#F0F0F0] flex items-center justify-end">
                <button
                  onClick={handleCalculate}
                  className="w-full sm:w-auto bg-[#1A1A1A] text-white hover:bg-black py-3 px-8 rounded-md text-sm font-medium transition-colors cursor-pointer flex items-center justify-center gap-2"
                >
                  <Calculator className="w-4 h-4" />
                  <span>احسب الميعاد القانوني</span>
                </button>
              </div>
            </div>

            {/* Result Display Card */}
            {result && (
              <div className="bg-white border border-[#E5E5E5] p-6 sm:p-8 rounded-xl space-y-6">
                <div className="flex items-center justify-between pb-3 border-b border-[#F0F0F0]">
                  <div className="flex items-center gap-3">
                    <div className="w-8 h-8 rounded-full bg-green-50 border border-green-100 flex items-center justify-center text-green-600">
                      <Check className="w-4 h-4" />
                    </div>
                    <div>
                      <h3 className="text-base font-medium text-[#1A1A1A]">نتيجة الاحتساب الإجرائي</h3>
                      <p className="text-xs text-[#737373]">ميعاد نهائي ملزم ومحصن قانوناً</p>
                    </div>
                  </div>
                  <span className="text-xs font-mono text-[#A3A3A3] bg-[#F5F5F5] border border-[#E5E5E5] px-2.5 py-1 rounded-full">
                    CALCULATED
                  </span>
                </div>

                {/* Final Deadline Prominent Box */}
                <div className="p-6 border border-[#E5E5E5] rounded-xl bg-[#FAFAFA] text-center space-y-2">
                  <span className="text-[10px] uppercase tracking-[0.2em] text-[#A3A3A3] font-semibold block">
                    الميعاد القانوني النهائي واجب الالتزام به
                  </span>
                  <div className="text-3xl sm:text-4xl font-light tracking-tight text-[#1A1A1A]">
                    {getArabicDayName(new Date(result.finalDeadline))} {formatDisplayDate(new Date(result.finalDeadline))}
                  </div>
                  {new Date(result.finalDeadline).getDay() === 6 && (
                    <div className="pt-2">
                      <span className="inline-flex items-center gap-1.5 px-3 py-1 bg-white border border-[#E5E5E5] text-[#1A1A1A] rounded-full text-xs font-medium">
                        ملاحظة قانونية: يوم السبت يُعد يوم عمل كامل في المحاكم المصرية
                      </span>
                    </div>
                  )}
                </div>

                {/* Law Article */}
                {result.lawArticle && (
                  <div>
                    <label className="text-[10px] uppercase tracking-[0.2em] text-[#A3A3A3] font-semibold mb-2 block">
                      السند القانوني
                    </label>
                    <div className="p-4 border border-[#F0F0F0] rounded-lg bg-[#FAFAFA] text-sm font-medium text-[#1A1A1A]">
                      {result.lawArticle}
                    </div>
                  </div>
                )}

                {/* Detailed Explanation */}
                <div>
                  <label className="text-[10px] uppercase tracking-[0.2em] text-[#A3A3A3] font-semibold mb-2 block">
                    بيان تفاصيل الاحتساب الإجرائي
                  </label>
                  <div className="p-5 border border-[#F0F0F0] rounded-lg bg-[#FAFAFA] text-xs sm:text-sm text-[#1A1A1A] leading-relaxed whitespace-pre-line font-sans">
                    {result.explanation}
                  </div>
                </div>

                {/* Notes */}
                {result.notes && (
                  <div className="p-4 border border-[#E5E5E5] rounded-lg bg-white text-xs text-[#737373]">
                    <span className="font-semibold text-[#1A1A1A] block mb-1">ملاحظات إجرائية:</span>
                    {result.notes}
                  </div>
                )}

                {/* Action Buttons */}
                <div className="pt-4 border-t border-[#F0F0F0] flex flex-col sm:flex-row items-center justify-between gap-3">
                  <div className="flex flex-wrap items-center gap-3 w-full sm:w-auto">
                    {/* Add to Google Calendar Link */}
                    <a
                      href={googleCalendarUrl}
                      target="_blank"
                      rel="noopener noreferrer"
                      className="w-full sm:w-auto py-2.5 px-5 rounded-md text-sm font-medium bg-[#FAFAFA] text-[#1A1A1A] border border-[#E5E5E5] hover:bg-[#F0F0F0] hover:border-[#CCCCCC] transition-colors flex items-center justify-center gap-2 cursor-pointer no-underline"
                      title="إضافة الميعاد النهائي إلى تقويم Google"
                    >
                      <CalendarPlus className="w-4 h-4 text-blue-600" />
                      <span>إضافة إلى تقويم Google</span>
                      <ExternalLink className="w-3.5 h-3.5 text-[#A3A3A3]" />
                    </a>

                    <button
                      onClick={handleSave}
                      disabled={saveSuccessNotice}
                      className={`w-full sm:w-auto py-2.5 px-6 rounded-md text-sm font-medium transition-colors flex items-center justify-center gap-2 cursor-pointer ${
                        saveSuccessNotice
                          ? 'bg-green-600 text-white cursor-default'
                          : 'bg-[#1A1A1A] text-white hover:bg-black'
                      }`}
                    >
                      <Bookmark className="w-4 h-4" />
                      <span>{saveSuccessNotice ? 'تم الحفظ في المحفوظات' : 'حفظ الميعاد'}</span>
                    </button>

                    <button
                      onClick={() => setResult(null)}
                      className="w-full sm:w-auto bg-[#F5F5F5] border border-[#E5E5E5] text-[#1A1A1A] hover:bg-[#EAEAEA] py-2.5 px-5 rounded-md text-sm font-medium transition-colors flex items-center justify-center gap-1.5 cursor-pointer"
                    >
                      <RotateCcw className="w-3.5 h-3.5" />
                      <span>إعادة تعيين</span>
                    </button>
                  </div>

                  {saveSuccessNotice && (
                    <span className="text-xs text-green-700 flex items-center gap-1.5 font-medium">
                      <CheckCircle2 className="w-4 h-4" />
                      تم الحفظ بنجاح، يمكنك مراجعته في تبويب المحفوظات.
                    </span>
                  )}
                </div>
              </div>
            )}
          </div>
        )}

        {/* TAB 2: Saved Deadlines List */}
        {activeTab === 'saved' && (
          <div className="bg-white border border-[#E5E5E5] p-6 sm:p-8 rounded-xl space-y-6">
            <div className="flex justify-between items-end pb-3 border-b border-[#F0F0F0]">
              <div>
                <h2 className="text-xl font-light tracking-tight text-[#1A1A1A] mb-1">
                  المواعيد المحفوظة
                </h2>
                <p className="text-xs text-[#737373]">
                  سجل المواعيد القضائية المسجلة مرتبة من الأحدث إلى الأقدم
                </p>
              </div>
              <div className="px-3 py-1 bg-[#F5F5F5] border border-[#E5E5E5] rounded-full text-xs font-mono text-[#1A1A1A]">
                {savedDeadlines.length} سجلات
              </div>
            </div>

            {savedDeadlines.length === 0 ? (
              <div className="text-center py-16 px-4">
                <Bookmark className="w-10 h-10 text-[#A3A3A3] mx-auto mb-3" />
                <h3 className="text-sm font-medium text-[#1A1A1A]">لا توجد مواعيد محفوظة حالياً</h3>
                <p className="text-xs text-[#737373] max-w-sm mx-auto mt-1">
                  قم باحتساب الميعاد من شاشة الحاسبة واضغط على "حفظ الميعاد" لأرشفته هنا.
                </p>
              </div>
            ) : (
              <div className="space-y-4">
                {savedDeadlines.map((deadline) => {
                  const isExpanded = expandedSavedId === deadline.id;
                  const finalDate = new Date(deadline.finalDeadline);

                  return (
                    <div
                      key={deadline.id}
                      className="bg-white border border-[#E5E5E5] p-5 rounded-xl transition-all hover:border-[#CCCCCC]"
                    >
                      <div className="flex flex-col sm:flex-row sm:items-start justify-between gap-4">
                        <div className="space-y-2 flex-1">
                          <div className="text-sm font-medium text-[#1A1A1A] leading-snug">
                            {deadline.actionName}
                          </div>

                          <div className="flex flex-wrap items-center gap-3 text-xs text-[#737373]">
                            {deadline.caseNumber && (
                              <span className="px-2.5 py-0.5 bg-[#FAFAFA] border border-[#E5E5E5] rounded-md font-mono text-[#1A1A1A]">
                                قضية: {deadline.caseNumber}
                              </span>
                            )}
                            {deadline.clientName && (
                              <span className="px-2.5 py-0.5 bg-[#FAFAFA] border border-[#E5E5E5] rounded-md text-[#1A1A1A]">
                                الموكل: {deadline.clientName}
                              </span>
                            )}
                            <span className="text-[#A3A3A3] font-mono">
                              الإعلان: {formatDisplayDate(new Date(deadline.announcementDate))}
                            </span>
                          </div>

                          <div className="flex items-center gap-2 pt-1">
                            <span className="text-xs text-[#737373]">الميعاد النهائي:</span>
                            <span className="text-sm font-medium text-[#1A1A1A] bg-[#FAFAFA] px-2.5 py-0.5 rounded-md border border-[#E5E5E5]">
                              {getArabicDayName(finalDate)} {formatDisplayDate(finalDate)}
                            </span>
                          </div>
                        </div>

                        {/* Actions */}
                        <div className="flex items-center gap-2 shrink-0 self-end sm:self-auto">
                          {/* Add to Google Calendar Link */}
                          <a
                            href={createGoogleCalendarUrl({
                              title: deadline.actionName,
                              finalDeadlineDate: deadline.finalDeadline,
                              lawArticle: deadline.lawArticle,
                              explanation: deadline.calculationExplanation,
                              notes: deadline.notes,
                              caseNumber: deadline.caseNumber,
                              clientName: deadline.clientName
                            })}
                            target="_blank"
                            rel="noopener noreferrer"
                            className="p-2 text-[#737373] hover:text-blue-600 bg-[#FAFAFA] hover:bg-blue-50 border border-[#E5E5E5] hover:border-blue-200 rounded-md text-xs font-medium flex items-center gap-1 transition-colors"
                            title="إضافة الميعاد إلى تقويم Google"
                          >
                            <CalendarPlus className="w-3.5 h-3.5 text-blue-600" />
                            <span className="hidden sm:inline">إضافة للتقويم</span>
                          </a>

                          <button
                            onClick={() => setExpandedSavedId(isExpanded ? null : deadline.id)}
                            className="p-2 text-[#737373] hover:text-[#1A1A1A] bg-[#FAFAFA] border border-[#E5E5E5] rounded-md text-xs font-medium flex items-center gap-1 transition-colors"
                          >
                            {isExpanded ? <ChevronUp className="w-3.5 h-3.5" /> : <ChevronDown className="w-3.5 h-3.5" />}
                            <span>{isExpanded ? 'طي' : 'التفاصيل'}</span>
                          </button>
                          <button
                            onClick={() => setDeleteConfirmationId(deadline.id)}
                            className="p-2 text-[#A3A3A3] hover:text-red-600 bg-[#FAFAFA] border border-[#E5E5E5] rounded-md transition-colors"
                            title="حذف الميعاد"
                          >
                            <Trash2 className="w-3.5 h-3.5" />
                          </button>
                        </div>
                      </div>

                      {/* Expandable Explanation */}
                      {isExpanded && (
                        <div className="mt-4 pt-4 border-t border-[#F0F0F0] space-y-3">
                          {deadline.lawArticle && (
                            <div>
                              <span className="text-[10px] uppercase tracking-[0.2em] text-[#A3A3A3] font-semibold block mb-1">
                                السند القانوني:
                              </span>
                              <span className="text-xs text-[#1A1A1A]">{deadline.lawArticle}</span>
                            </div>
                          )}

                          <div>
                            <span className="text-[10px] uppercase tracking-[0.2em] text-[#A3A3A3] font-semibold block mb-1">
                              تفاصيل الاحتساب:
                            </span>
                            <div className="p-4 border border-[#F0F0F0] rounded-lg bg-[#FAFAFA] text-xs text-[#1A1A1A] whitespace-pre-line leading-relaxed font-sans">
                              {deadline.calculationExplanation}
                            </div>
                          </div>
                        </div>
                      )}
                    </div>
                  );
                })}
              </div>
            )}
          </div>
        )}

        {/* TAB 3: Android Studio Project Explorer */}
        {activeTab === 'android' && <AndroidProjectViewer />}
      </main>

      {/* Clean Minimalism Footer */}
      <footer className="h-10 bg-white border-t border-[#E5E5E5] flex items-center justify-between px-6 sm:px-8 text-[10px] text-[#A3A3A3] uppercase tracking-widest mt-auto font-mono">
        <div>قانون المرافعات المصري • جمهورية مصر العربية</div>
        <div>السبت يوم عمل رسمي • النظام جاهز</div>
      </footer>

      {/* Delete Confirmation Modal */}
      {deleteConfirmationId && (
        <div className="fixed inset-0 z-50 bg-black/40 backdrop-blur-xs flex items-center justify-center p-4">
          <div className="bg-white border border-[#E5E5E5] rounded-xl max-w-md w-full p-6 space-y-4 shadow-xl">
            <div className="w-10 h-10 rounded-full bg-red-50 border border-red-100 text-red-600 flex items-center justify-center mx-auto">
              <AlertCircle className="w-5 h-5" />
            </div>
            <div className="text-center">
              <h3 className="text-base font-medium text-[#1A1A1A]">تأكيد حذف الميعاد المحفوظ</h3>
              <p className="text-xs text-[#737373] mt-1">
                هل أنت متأكد من رغبتك في حذف هذا الميعاد بشكل نهائي من قاعدة البيانات؟
              </p>
            </div>
            <div className="flex items-center gap-3 pt-2">
              <button
                onClick={handleDeleteConfirmed}
                className="flex-1 bg-red-600 hover:bg-red-700 text-white font-medium text-sm py-2.5 rounded-md transition-colors cursor-pointer"
              >
                حذف
              </button>
              <button
                onClick={() => setDeleteConfirmationId(null)}
                className="flex-1 bg-[#F5F5F5] border border-[#E5E5E5] hover:bg-[#EAEAEA] text-[#1A1A1A] font-medium text-sm py-2.5 rounded-md transition-colors cursor-pointer"
              >
                إلغاء
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
