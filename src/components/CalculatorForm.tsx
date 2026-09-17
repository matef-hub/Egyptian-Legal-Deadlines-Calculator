import React from 'react';
import {
  Calculator,
  Calendar,
  Folder,
  User,
  MapPin,
  Sparkles,
  Info,
  CalendarDays,
  Building2,
  Clock
} from 'lucide-react';
import { LegalRule } from '../types';
import { RuleCategorySelector } from './RuleCategorySelector';
import { formatDisplayDate, getArabicDayName, parseIsoDate, toIsoDate } from '../utils/calculator';

interface CalculatorFormProps {
  rules: LegalRule[];
  selectedRuleId: number;
  setSelectedRuleId: (id: number) => void;
  announcementDate: string;
  setAnnouncementDate: (date: string) => void;
  additionalDistanceDays: number;
  setAdditionalDistanceDays: (days: number) => void;
  caseNumber: string;
  setCaseNumber: (val: string) => void;
  clientName: string;
  setClientName: (val: string) => void;
  onCalculate: () => void;
}

export const CalculatorForm: React.FC<CalculatorFormProps> = ({
  rules,
  selectedRuleId,
  setSelectedRuleId,
  announcementDate,
  setAnnouncementDate,
  additionalDistanceDays,
  setAdditionalDistanceDays,
  caseNumber,
  setCaseNumber,
  clientName,
  setClientName,
  onCalculate
}) => {
  const selectedRule = rules.find((r) => r.id === selectedRuleId) || rules[0];
  const currentDateObj = parseIsoDate(announcementDate);

  // Quick Date Helpers
  const setQuickDate = (offsetDays: number) => {
    const d = new Date();
    d.setDate(d.getDate() - offsetDays);
    setAnnouncementDate(toIsoDate(d));
  };

  return (
    <div className="bg-white border border-slate-200 rounded-2xl p-6 sm:p-8 space-y-6 shadow-sm">
      {/* Form Header */}
      <div className="flex items-center justify-between pb-3 border-b border-slate-100">
        <div className="space-y-1">
          <div className="flex items-center gap-2">
            <span className="w-2 h-2 rounded-full bg-amber-500"></span>
            <h2 className="text-lg sm:text-xl font-bold text-slate-900">
              بيانات احتساب الميعاد الإجرائي
            </h2>
          </div>
          <p className="text-xs text-slate-500">
            حدد الإجراء القضائي وتاريخ الإعلان لاحتساب الميعاد النهائي المحصن قانوناً
          </p>
        </div>

        <span className="text-[10px] font-mono font-bold bg-amber-500/10 text-amber-800 border border-amber-500/20 px-3 py-1 rounded-full">
          المرحلة الأولى
        </span>
      </div>

      <div className="space-y-6">
        {/* Rule Category and Search Selector */}
        <RuleCategorySelector
          rules={rules}
          selectedRuleId={selectedRuleId}
          onSelectRule={setSelectedRuleId}
        />

        {/* Form Inputs Grid */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-5 pt-2">
          {/* Announcement Date */}
          <div className="space-y-2">
            <div className="flex items-center justify-between">
              <label className="text-xs font-bold text-slate-700 uppercase tracking-wider flex items-center gap-1.5">
                <Calendar className="w-3.5 h-3.5 text-amber-600" />
                <span>تاريخ الإعلان أو صدور الحكم</span>
              </label>
              <span className="text-[11px] font-bold text-slate-700 bg-slate-100 px-2.5 py-0.5 rounded-md" dir="rtl">
                <span dir="ltr" className="font-mono ml-1">{formatDisplayDate(currentDateObj)}</span> ({getArabicDayName(currentDateObj)})
              </span>
            </div>

            <input
              type="date"
              value={announcementDate}
              onChange={(e) => setAnnouncementDate(e.target.value)}
              className="w-full bg-slate-50 border border-slate-200 rounded-xl px-4 py-2.5 text-xs sm:text-sm text-slate-900 focus:bg-white focus:outline-none focus:border-amber-500 transition-colors shadow-2xs font-sans"
            />

            {/* Quick date presets */}
            <div className="flex items-center gap-1.5 pt-1 text-[11px]">
              <span className="text-slate-400 text-[10px]">اختيار سريع:</span>
              <button
                type="button"
                onClick={() => setQuickDate(0)}
                className="px-2 py-0.5 rounded-md bg-slate-100 hover:bg-slate-200 text-slate-700 font-medium transition-colors cursor-pointer"
              >
                اليوم
              </button>
              <button
                type="button"
                onClick={() => setQuickDate(1)}
                className="px-2 py-0.5 rounded-md bg-slate-100 hover:bg-slate-200 text-slate-700 font-medium transition-colors cursor-pointer"
              >
                أمس
              </button>
              <button
                type="button"
                onClick={() => setQuickDate(7)}
                className="px-2 py-0.5 rounded-md bg-slate-100 hover:bg-slate-200 text-slate-700 font-medium transition-colors cursor-pointer"
              >
                منذ أسبوع
              </button>
              <button
                type="button"
                onClick={() => setQuickDate(30)}
                className="px-2 py-0.5 rounded-md bg-slate-100 hover:bg-slate-200 text-slate-700 font-medium transition-colors cursor-pointer"
              >
                منذ شهر
              </button>
            </div>
          </div>

          {/* Additional Distance Days */}
          <div className="space-y-2">
            <div className="flex items-center justify-between">
              <label className="text-xs font-bold text-slate-700 uppercase tracking-wider flex items-center gap-1.5">
                <MapPin className="w-3.5 h-3.5 text-amber-600" />
                <span>ميعاد المسافة الإضافي (بالأيام)</span>
              </label>
              <span className="text-[11px] text-slate-400 font-medium">
                المادة 16 مرافعات
              </span>
            </div>

            <div className="relative">
              <input
                type="number"
                min="0"
                value={additionalDistanceDays}
                onChange={(e) =>
                  setAdditionalDistanceDays(Math.max(0, parseInt(e.target.value) || 0))
                }
                placeholder="0"
                className="w-full bg-slate-50 border border-slate-200 rounded-xl px-4 py-2.5 text-xs sm:text-sm text-slate-900 focus:bg-white focus:outline-none focus:border-amber-500 transition-colors shadow-2xs"
              />
              <span className="absolute left-3 top-2.5 text-xs text-slate-400 font-medium pointer-events-none">
                يوم
              </span>
            </div>

            <p className="text-[11px] text-slate-500">
              يضاف ميعاد مسافة إذا كان موطن الخصم خارج دائرة اختصاص المحكمة وفقاً للمسافات المحددة في القانون.
            </p>
          </div>

          {/* Case Number */}
          <div className="space-y-2">
            <label className="text-xs font-bold text-slate-700 uppercase tracking-wider flex items-center gap-1.5">
              <Folder className="w-3.5 h-3.5 text-slate-500" />
              <span>رقم الدعوى أو القضية (اختياري)</span>
            </label>
            <input
              type="text"
              value={caseNumber}
              onChange={(e) => setCaseNumber(e.target.value)}
              placeholder="مثال: 1245 لسنة 2026 مدني مستأنف القاهرة"
              className="w-full bg-slate-50 border border-slate-200 rounded-xl px-4 py-2.5 text-xs sm:text-sm text-slate-900 focus:bg-white focus:outline-none focus:border-amber-500 transition-colors shadow-2xs"
            />
          </div>

          {/* Client Name */}
          <div className="space-y-2">
            <label className="text-xs font-bold text-slate-700 uppercase tracking-wider flex items-center gap-1.5">
              <User className="w-3.5 h-3.5 text-slate-500" />
              <span>اسم الموكل أو الخصم (اختياري)</span>
            </label>
            <input
              type="text"
              value={clientName}
              onChange={(e) => setClientName(e.target.value)}
              placeholder="مثال: المستشار أحمد كمال / شركة النيل"
              className="w-full bg-slate-50 border border-slate-200 rounded-xl px-4 py-2.5 text-xs sm:text-sm text-slate-900 focus:bg-white focus:outline-none focus:border-amber-500 transition-colors shadow-2xs"
            />
          </div>
        </div>

        {/* Submit Action */}
        <div className="pt-4 border-t border-slate-100 flex items-center justify-between flex-wrap gap-3">
          <div className="flex items-center gap-2 text-xs text-slate-500">
            <Building2 className="w-4 h-4 text-emerald-600 shrink-0" />
            <span>يتم تطبيق قواعد احتساب يوم السبت كعمل رسمي وفحص العطلات آلياً.</span>
          </div>

          <button
            type="button"
            onClick={onCalculate}
            className="w-full sm:w-auto bg-gradient-to-r from-[#0F2027] via-[#1B3542] to-[#203A43] hover:from-black hover:to-[#0F2027] text-amber-400 font-bold px-8 py-3 rounded-xl text-sm transition-all shadow-md hover:shadow-lg flex items-center justify-center gap-2 cursor-pointer border border-amber-500/20"
          >
            <Calculator className="w-4 h-4 text-amber-400" />
            <span>احسب الميعاد القانوني</span>
          </button>
        </div>
      </div>
    </div>
  );
};
