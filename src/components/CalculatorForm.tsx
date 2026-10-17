import React, { useState } from 'react';
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
  Clock,
  AlertCircle,
  FileText
} from 'lucide-react';
import { DistanceType, LegalRule } from '../types';
import { RuleCategorySelector } from './RuleCategorySelector';
import {
  calculateLocalDistanceDays,
  formatDisplayDate,
  getArabicDayName,
  parseIsoDate,
  toIsoDate
} from '../utils/calculator';

interface CalculatorFormProps {
  rules: LegalRule[];
  selectedRuleId: number;
  setSelectedRuleId: (id: number) => void;
  announcementDate: string;
  setAnnouncementDate: (date: string) => void;
  distanceType: DistanceType;
  setDistanceType: (type: DistanceType) => void;
  additionalDistanceDays: number;
  setAdditionalDistanceDays: (days: number) => void;
  judicialOrderDetails: string;
  setJudicialOrderDetails: (val: string) => void;
  caseNumber: string;
  setCaseNumber: (val: string) => void;
  clientName: string;
  setClientName: (val: string) => void;
  calculationError?: string | null;
  onCalculate: () => void;
}

export const CalculatorForm: React.FC<CalculatorFormProps> = ({
  rules,
  selectedRuleId,
  setSelectedRuleId,
  announcementDate,
  setAnnouncementDate,
  distanceType,
  setDistanceType,
  additionalDistanceDays,
  setAdditionalDistanceDays,
  judicialOrderDetails,
  setJudicialOrderDetails,
  caseNumber,
  setCaseNumber,
  clientName,
  setClientName,
  calculationError,
  onCalculate
}) => {
  const selectedRule = rules.find((r) => r.id === selectedRuleId) || rules[0];
  const [distanceKm, setDistanceKm] = useState<string>('');

  let currentDateObj: Date;
  try {
    currentDateObj = parseIsoDate(announcementDate);
  } catch {
    currentDateObj = new Date();
  }

  // Quick Date Helpers
  const setQuickDate = (offsetDays: number) => {
    const d = new Date();
    d.setDate(d.getDate() - offsetDays);
    setAnnouncementDate(toIsoDate(d));
  };

  const isDistanceForbidden = selectedRule.distancePolicy === 'NOT_APPLICABLE';

  const handleDistanceTypeChange = (type: DistanceType) => {
    setDistanceType(type);
    switch (type) {
      case 'NONE':
        setAdditionalDistanceDays(0);
        break;
      case 'LOCAL':
        setAdditionalDistanceDays(1);
        break;
      case 'BORDER_AREA':
        setAdditionalDistanceDays(15);
        break;
      case 'ABROAD':
        setAdditionalDistanceDays(60);
        break;
      case 'ABROAD_JUDICIAL_REDUCTION':
        setAdditionalDistanceDays(30);
        break;
      case 'ABROAD_SERVED_IN_EGYPT':
        setAdditionalDistanceDays(0);
        break;
    }
  };

  const handleKmChange = (val: string) => {
    setDistanceKm(val);
    const num = parseFloat(val);
    if (!isNaN(num)) {
      const calculated = calculateLocalDistanceDays(num);
      setAdditionalDistanceDays(calculated);
    }
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
            حدد الإجراء القضائي وتاريخ الإعلان وميعاد المسافة لاحتساب الميعاد النهائي المحصن قانوناً
          </p>
        </div>

        <span className="text-[10px] font-mono font-bold bg-amber-500/10 text-amber-800 border border-amber-500/20 px-3 py-1 rounded-full">
          المرحلة الأولى
        </span>
      </div>

      {calculationError && (
        <div className="bg-rose-50 border border-rose-200 text-rose-800 p-4 rounded-xl flex items-start gap-3 text-xs sm:text-sm">
          <AlertCircle className="w-5 h-5 text-rose-600 shrink-0 mt-0.5" />
          <div className="space-y-1">
            <p className="font-bold">تنبيه قانوني / تعذر الحساب:</p>
            <p className="leading-relaxed">{calculationError}</p>
          </div>
        </div>
      )}

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
                <span>{selectedRule.triggerEventLabel || 'تاريخ الإعلان أو صدور الحكم'}</span>
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

          {/* Statutory Distance Type & Configuration */}
          <div className="space-y-2">
            <div className="flex items-center justify-between">
              <label className="text-xs font-bold text-slate-700 uppercase tracking-wider flex items-center gap-1.5">
                <MapPin className="w-3.5 h-3.5 text-amber-600" />
                <span>ميعاد مسافة الطريق (المواد 16 و 17 مرافعات)</span>
              </label>
              {isDistanceForbidden ? (
                <span className="text-[10px] font-bold text-rose-700 bg-rose-50 px-2 py-0.5 rounded border border-rose-200">
                  غير منطبق على هذا الإجراء
                </span>
              ) : (
                <span className="text-[11px] text-slate-500 font-medium">
                  {additionalDistanceDays > 0 ? `+${additionalDistanceDays} يوم` : '0 يوم'}
                </span>
              )}
            </div>

            <select
              value={distanceType}
              disabled={isDistanceForbidden}
              onChange={(e) => handleDistanceTypeChange(e.target.value as DistanceType)}
              className="w-full bg-slate-50 border border-slate-200 rounded-xl px-3 py-2.5 text-xs sm:text-sm text-slate-900 focus:bg-white focus:outline-none focus:border-amber-500 transition-colors shadow-2xs font-sans disabled:opacity-50 disabled:bg-slate-100"
            >
              <option value="NONE">بدون ميعاد مسافة (نفس الدائرة القضائية)</option>
              <option value="LOCAL">مسافة محلية داخل الجمهورية — المادة 16 (1–4 أيام)</option>
              <option value="BORDER_AREA">المناطق الحدودية النائية — المادة 16 ف2 (15 يوماً)</option>
              <option value="ABROAD">موطن بالخارج — المادة 17 (60 يوماً)</option>
              <option value="ABROAD_JUDICIAL_REDUCTION">موطن بالخارج — إنقاص الميعاد بأمر قضائي وقتي (المادة 17 ف2)</option>
              <option value="ABROAD_SERVED_IN_EGYPT">موطن بالخارج أُعلن لشخصه بمصر — المادة 17 ف3 (0 يوم)</option>
            </select>

            {/* Sub-controls based on distance type */}
            {distanceType === 'LOCAL' && !isDistanceForbidden && (
              <div className="bg-slate-50 p-3 rounded-xl border border-slate-200 space-y-2 mt-2">
                <div className="flex items-center justify-between text-xs">
                  <span className="text-slate-600 font-medium">حساب المسافة بالكيلومتر (المادة 16):</span>
                  <span className="font-bold text-amber-700">{additionalDistanceDays} يوم مضاف (أقصى 4 أيام)</span>
                </div>
                <div className="grid grid-cols-2 gap-2">
                  <div className="relative">
                    <input
                      type="number"
                      min="0"
                      placeholder="المسافة بالكم (اختياري)"
                      value={distanceKm}
                      onChange={(e) => handleKmChange(e.target.value)}
                      className="w-full bg-white border border-slate-200 rounded-lg px-2.5 py-1.5 text-xs text-slate-900"
                    />
                    <span className="absolute left-2 top-1.5 text-[10px] text-slate-400">كم</span>
                  </div>
                  <div className="flex items-center gap-1">
                    {[1, 2, 3, 4].map((d) => (
                      <button
                        key={d}
                        type="button"
                        onClick={() => setAdditionalDistanceDays(d)}
                        className={`flex-1 py-1 text-xs rounded font-medium cursor-pointer ${
                          additionalDistanceDays === d
                            ? 'bg-amber-600 text-white font-bold'
                            : 'bg-white border border-slate-200 text-slate-700 hover:bg-slate-100'
                        }`}
                      >
                        {d} يوم
                      </button>
                    ))}
                  </div>
                </div>
                <p className="text-[10px] text-slate-500">
                  المادة 16 مرافعات: يوم لكل 50 كم، وما زاد على 30 كم يعتبر 50 كم، بحد أقصى 4 أيام.
                </p>
              </div>
            )}

            {distanceType === 'BORDER_AREA' && !isDistanceForbidden && (
              <p className="text-[11px] text-amber-800 bg-amber-50 p-2.5 rounded-lg border border-amber-200">
                المادة 16 فقرة 2 مرافعات: ميعاد المسافة محدد تشريعياً بـ <strong>15 يوماً</strong> للمناطق الواقعة بالمحافظات والمناطق الحدودية النائية الصادر بتحديدها قرار من وزير العدل.
              </p>
            )}

            {distanceType === 'ABROAD' && !isDistanceForbidden && (
              <p className="text-[11px] text-amber-800 bg-amber-50 p-2.5 rounded-lg border border-amber-200">
                المادة 17 مرافعات: ميعاد المسافة لمن يكون موطنه في الخارج هو <strong>60 يوماً</strong> ثابتاً بحكم القانون، ولا يُطبق ميعاد الـ 90 يوماً السابق إلغاؤه تشريعياً.
              </p>
            )}

            {distanceType === 'ABROAD_JUDICIAL_REDUCTION' && !isDistanceForbidden && (
              <div className="bg-slate-50 p-3 rounded-xl border border-slate-200 space-y-2 mt-2">
                <label className="text-xs font-bold text-slate-700 flex items-center gap-1">
                  <FileText className="w-3.5 h-3.5 text-amber-600" />
                  <span>بيانات وسند الأمر القضائي الصادر بإنقاص الميعاد (إلزامي):</span>
                </label>
                <input
                  type="text"
                  placeholder="مثال: أمر على عريضة رقم 24 لسنة 2026 قضاء مستعجل"
                  value={judicialOrderDetails}
                  onChange={(e) => setJudicialOrderDetails(e.target.value)}
                  className="w-full bg-white border border-slate-200 rounded-lg px-3 py-1.5 text-xs text-slate-900"
                />
                <div className="flex items-center justify-between text-xs pt-1">
                  <span className="text-slate-600">الأيام المقررة بالأمر (1–59 يوماً):</span>
                  <input
                    type="number"
                    min="1"
                    max="59"
                    value={additionalDistanceDays}
                    onChange={(e) => setAdditionalDistanceDays(parseInt(e.target.value) || 1)}
                    className="w-20 bg-white border border-slate-200 rounded-lg px-2 py-1 text-xs text-center font-bold text-amber-800"
                  />
                </div>
                <p className="text-[10px] text-slate-500">
                  المادة 17 فقرة 2: يجوز بأمر من قاضي الأمور الوقتية إنقاص ميعاد الـ 60 يوماً تبعاً لسهولة المواصلات والظروف الخاصة.
                </p>
              </div>
            )}

            {distanceType === 'ABROAD_SERVED_IN_EGYPT' && !isDistanceForbidden && (
              <p className="text-[11px] text-slate-700 bg-slate-100 p-2.5 rounded-lg border border-slate-200">
                المادة 17 فقرة 3 مرافعات: <strong>لا يسري ميعاد مسافة الخارج</strong> على من أعلن لشخصه أثناء وجوده في جمهورية مصر العربية (ميعاد المسافة 0 يوم).
              </p>
            )}
          </div>

          {/* Case Number */}
          <div className="space-y-2">
            <label className="text-xs font-bold text-slate-700 uppercase tracking-wider flex items-center gap-1.5">
              <Folder className="w-3.5 h-3.5 text-slate-500" />
              <span>رقم الدعوى أو القضية (اختياري)</span>
            </label>
            <input
              type="text"
              placeholder="مثال: 1245 لسنة 2026 مدني كلي جنوب القاهرة"
              value={caseNumber}
              onChange={(e) => setCaseNumber(e.target.value)}
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
              placeholder="مثال: الأستاذ / أحمد عبد الرحمن"
              value={clientName}
              onChange={(e) => setClientName(e.target.value)}
              className="w-full bg-slate-50 border border-slate-200 rounded-xl px-4 py-2.5 text-xs sm:text-sm text-slate-900 focus:bg-white focus:outline-none focus:border-amber-500 transition-colors shadow-2xs"
            />
          </div>
        </div>

        {/* Selected Rule Summary Banner */}
        <div className="bg-slate-50 border border-slate-200 rounded-xl p-4 flex flex-col sm:flex-row items-start sm:items-center justify-between gap-3 text-xs">
          <div className="flex items-start gap-2.5">
            <Info className="w-4 h-4 text-amber-600 mt-0.5 shrink-0" />
            <div className="space-y-0.5">
              <span className="font-bold text-slate-900">
                {selectedRule.actionName}
              </span>
              <p className="text-slate-500 text-[11px] leading-relaxed">
                {selectedRule.notes || selectedRule.lawArticle}
              </p>
            </div>
          </div>
          <div className="flex items-center gap-2 shrink-0">
            <span className="bg-amber-100 text-amber-900 font-bold px-2.5 py-1 rounded-md text-[11px]">
              المدة: {selectedRule.duration}{' '}
              {selectedRule.unit === 'DAYS' ? 'يوم' : selectedRule.unit === 'MONTHS' ? 'شهر' : 'سنة'}
            </span>
            <span className="bg-slate-200 text-slate-700 px-2 py-1 rounded-md text-[10px]">
              {selectedRule.startRule === 'NEXT_DAY' ? 'يبدأ اليوم التالي (م15)' : 'يبدأ نفس اليوم'}
            </span>
          </div>
        </div>

        {/* Calculate Action Button */}
        <div className="pt-2">
          <button
            type="button"
            onClick={onCalculate}
            className="w-full bg-linear-to-r from-amber-600 to-amber-700 hover:from-amber-700 hover:to-amber-800 text-white font-bold py-3.5 px-6 rounded-xl shadow-md hover:shadow-lg transition-all duration-200 flex items-center justify-center gap-2 cursor-pointer text-sm sm:text-base"
          >
            <Calculator className="w-5 h-5" />
            <span>احتساب الميعاد النهائي والمسار الزمني التفصيلي</span>
          </button>
        </div>
      </div>
    </div>
  );
};
