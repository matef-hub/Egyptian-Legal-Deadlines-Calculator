import React, { useState } from 'react';
import { X, Calendar, Search, ShieldCheck, Clock, CheckCircle, AlertCircle } from 'lucide-react';
import { Holiday } from '../types';
import { formatDisplayDate, getArabicDayName, parseIsoDate } from '../utils/calculator';

interface HolidaysModalProps {
  isOpen: boolean;
  onClose: () => void;
  holidays: Holiday[];
}

export const HolidaysModal: React.FC<HolidaysModalProps> = ({
  isOpen,
  onClose,
  holidays
}) => {
  const [searchTerm, setSearchTerm] = useState('');

  if (!isOpen) return null;

  const todayIso = new Date().toISOString().split('T')[0];

  const filtered = holidays.filter((h) =>
    h.name.includes(searchTerm) || h.holidayDate.includes(searchTerm)
  );

  return (
    <div className="fixed inset-0 z-50 bg-slate-950/70 backdrop-blur-xs flex items-center justify-center p-4">
      <div className="bg-white rounded-2xl max-w-2xl w-full border border-slate-200 shadow-2xl overflow-hidden flex flex-col max-h-[90vh] animate-in fade-in zoom-in-95 duration-200">
        {/* Modal Header */}
        <div className="bg-[#0F2027] text-white p-5 flex items-center justify-between border-b border-[#203A43]">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-xl bg-amber-500/20 border border-amber-500/30 flex items-center justify-center text-amber-400">
              <Calendar className="w-5 h-5" />
            </div>
            <div>
              <h3 className="text-base font-bold text-white">جدول العطلات الرسمية المعتمدة لعام 2026</h3>
              <p className="text-xs text-slate-300">
                العطلات الرسمية المعطلة للمحاكم والمصالح الحكومية بجمهورية مصر العربية
              </p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="w-8 h-8 rounded-lg bg-white/10 hover:bg-white/20 text-slate-300 hover:text-white flex items-center justify-center transition-colors cursor-pointer"
          >
            <X className="w-4 h-4" />
          </button>
        </div>

        {/* Search & Stats Bar */}
        <div className="p-4 bg-slate-50 border-b border-slate-200 flex flex-col sm:flex-row gap-3 items-center justify-between">
          <div className="relative w-full sm:w-72">
            <Search className="w-4 h-4 text-slate-400 absolute right-3 top-3" />
            <input
              type="text"
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              placeholder="ابحث باسم العطلة أو التاريخ..."
              className="w-full bg-white border border-slate-200 rounded-xl pr-9 pl-4 py-2 text-xs text-slate-800 focus:outline-none focus:border-amber-500 transition-colors"
            />
          </div>

          <div className="flex items-center gap-2 text-xs text-slate-600 bg-white px-3 py-1.5 rounded-xl border border-slate-200 w-full sm:w-auto justify-between sm:justify-start">
            <ShieldCheck className="w-4 h-4 text-emerald-600" />
            <span>إجمالي العطلات المسجلة: <strong className="text-slate-900">{holidays.length}</strong> عطلة</span>
          </div>
        </div>

        {/* Note on procedural rules */}
        <div className="px-5 py-2.5 bg-amber-50/70 border-b border-amber-200/60 text-xs text-amber-900 flex items-start gap-2">
          <AlertCircle className="w-4 h-4 text-amber-600 shrink-0 mt-0.5" />
          <span>
            <strong>الأثر الإجرائي:</strong> إذا صادف آخر يوم في الميعاد عطلة رسمية أو يوم جمعة، يمتد الميعاد وجوباً إلى أول يوم عمل تالٍ (المادة 18 مرافعات)، مع مراعاة أن <strong>يوم السبت يوم عمل رسمي</strong> في المحاكم.
          </span>
        </div>

        {/* Holiday list */}
        <div className="p-4 overflow-y-auto divide-y divide-slate-100 flex-1 space-y-1">
          {filtered.length === 0 ? (
            <div className="text-center py-12 text-slate-400">
              <Calendar className="w-8 h-8 mx-auto mb-2 opacity-40" />
              <p className="text-xs">لا توجد عطلات مطابقة لبحثك</p>
            </div>
          ) : (
            filtered.map((holiday) => {
              const hDate = parseIsoDate(holiday.holidayDate);
              const isPast = holiday.holidayDate < todayIso;
              const isToday = holiday.holidayDate === todayIso;

              return (
                <div
                  key={holiday.id}
                  className="py-3 px-3 rounded-xl hover:bg-slate-50 transition-colors flex items-center justify-between gap-4"
                >
                  <div className="flex items-center gap-3">
                    <div
                      className={`w-9 h-9 rounded-xl flex items-center justify-center font-bold text-xs shrink-0 ${
                        isToday
                          ? 'bg-amber-500 text-slate-950 font-extrabold shadow-md'
                          : isPast
                          ? 'bg-slate-100 text-slate-400'
                          : 'bg-emerald-50 text-emerald-700 border border-emerald-200'
                      }`}
                    >
                      {hDate.getDate()}
                    </div>
                    <div>
                      <h4 className="text-sm font-semibold text-slate-900 flex items-center gap-2">
                        {holiday.name}
                        {isToday && (
                          <span className="text-[10px] bg-amber-100 text-amber-800 px-2 py-0.2 rounded-full font-bold">
                            اليوم
                          </span>
                        )}
                      </h4>
                      <p className="text-xs text-slate-500" dir="rtl">
                        <span dir="ltr" className="font-mono font-medium">{formatDisplayDate(hDate)}</span> ({getArabicDayName(hDate)})
                      </p>
                    </div>
                  </div>

                  <div className="shrink-0 text-left">
                    {isPast ? (
                      <span className="text-[11px] font-medium text-slate-400 bg-slate-100 px-2.5 py-1 rounded-full flex items-center gap-1">
                        <CheckCircle className="w-3 h-3 text-slate-400" />
                        انقضت
                      </span>
                    ) : (
                      <span className="text-[11px] font-medium text-emerald-700 bg-emerald-50 border border-emerald-200 px-2.5 py-1 rounded-full flex items-center gap-1">
                        <Clock className="w-3 h-3 text-emerald-600" />
                        قادمة
                      </span>
                    )}
                  </div>
                </div>
              );
            })
          )}
        </div>

        {/* Footer */}
        <div className="p-4 bg-slate-50 border-t border-slate-200 flex justify-end">
          <button
            onClick={onClose}
            className="px-5 py-2 rounded-xl bg-slate-900 hover:bg-black text-white text-xs font-semibold transition-colors cursor-pointer"
          >
            إغلاق
          </button>
        </div>
      </div>
    </div>
  );
};
