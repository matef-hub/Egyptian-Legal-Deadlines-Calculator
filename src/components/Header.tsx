import React from 'react';
import {
  Scale,
  Calendar,
  Bookmark,
  Calculator,
  Smartphone,
  CalendarDays,
  ShieldCheck,
  Sparkles
} from 'lucide-react';
import { motion } from 'motion/react';
import { formatDisplayDate, getArabicDayName } from '../utils/calculator';
import { Holiday } from '../types';

interface HeaderProps {
  activeTab: 'calculator' | 'saved' | 'android';
  setActiveTab: (tab: 'calculator' | 'saved' | 'android') => void;
  savedCount: number;
  holidays: Holiday[];
  onOpenHolidaysModal: () => void;
}

export const Header: React.FC<HeaderProps> = ({
  activeTab,
  setActiveTab,
  savedCount,
  holidays,
  onOpenHolidaysModal
}) => {
  const today = new Date();
  const todayIso = today.toISOString().split('T')[0];
  const todayHoliday = holidays.find((h) => h.holidayDate === todayIso);
  const isFriday = today.getDay() === 5;
  const isSaturday = today.getDay() === 6;

  return (
    <header className="sticky top-0 z-40 bg-[#0F2027] text-white border-b border-[#203A43] shadow-md">
      {/* Top micro-bar */}
      <div className="bg-[#0A161B] px-4 sm:px-8 py-1.5 border-b border-white/5 text-[11px] flex flex-wrap items-center justify-between gap-2 text-slate-300">
        <div className="flex items-center gap-3">
          <span className="flex items-center gap-1.5 text-amber-400/90 font-medium">
            <Scale className="w-3.5 h-3.5 text-amber-400" />
            جمهورية مصر العربية • قانون المرافعات المدنية والتجارية رقم 13 لسنة 1968
          </span>
          <span className="hidden md:inline text-slate-500">•</span>
          <span className="hidden md:inline text-emerald-400 font-medium">
            السبت يوم عمل رسمي في المحاكم المصرية
          </span>
        </div>

        <div className="flex items-center gap-3 text-[11px]">
          <button
            onClick={onOpenHolidaysModal}
            className="flex items-center gap-1.5 text-slate-300 hover:text-amber-300 transition-colors cursor-pointer bg-white/5 hover:bg-white/10 px-2.5 py-0.5 rounded-full border border-white/10"
          >
            <Calendar className="w-3 h-3 text-amber-400" />
            <span>جدول العطلات الرسمية ({holidays.length})</span>
          </button>
          <span className="text-slate-600 hidden sm:inline">|</span>
          <span className="font-mono text-slate-400 hidden sm:inline">v4.2.0 • Pro</span>
        </div>
      </div>

      {/* Main Bar */}
      <div className="max-w-6xl mx-auto px-4 sm:px-8 py-3.5 flex flex-col md:flex-row md:items-center justify-between gap-4">
        {/* Brand identity */}
        <div className="flex items-center gap-3.5">
          <div className="relative">
            <div className="w-11 h-11 rounded-xl bg-gradient-to-br from-amber-500 via-amber-600 to-amber-700 flex items-center justify-center shadow-lg shadow-amber-900/30 ring-1 ring-amber-300/30">
              <Scale className="w-6 h-6 text-slate-950 stroke-[2.2]" />
            </div>
            <div className="absolute -bottom-1 -left-1 w-4 h-4 rounded-full bg-emerald-500 border-2 border-[#0F2027] flex items-center justify-center">
              <span className="w-1.5 h-1.5 rounded-full bg-white"></span>
            </div>
          </div>

          <div>
            <div className="flex items-center gap-2">
              <h1 className="text-lg sm:text-xl font-bold tracking-tight text-white font-['Cairo']">
                حاسبة المواعيد القانونية
              </h1>
              <span className="px-2 py-0.5 rounded text-[10px] font-semibold bg-amber-500/20 text-amber-300 border border-amber-500/30">
                مصر
              </span>
            </div>
            <p className="text-xs text-slate-400 leading-snug">
              المرجع الإجرائي الدقيق لاحتساب مواعيد الطعن، الحضور، والإجراءات القضائية
            </p>
          </div>
        </div>

        {/* Live Day Pill & Nav Tabs */}
        <div className="flex flex-wrap items-center gap-3">
          {/* Today Indicator */}
          <div className="hidden lg:flex items-center gap-2 bg-[#172B35] border border-white/10 px-3.5 py-1.5 rounded-lg text-xs">
            <CalendarDays className="w-4 h-4 text-amber-400" />
            <span className="text-slate-300 font-medium" dir="rtl">
              <span dir="ltr" className="font-mono">{formatDisplayDate(today)}</span> ({getArabicDayName(today)})
            </span>
            <span
              className={`px-2 py-0.5 rounded text-[10px] font-semibold ${
                todayHoliday
                  ? 'bg-amber-500/20 text-amber-300 border border-amber-500/30'
                  : isFriday
                  ? 'bg-red-500/20 text-red-300 border border-red-500/30'
                  : 'bg-emerald-500/20 text-emerald-300 border border-emerald-500/30'
              }`}
            >
              {todayHoliday ? 'عطلة رسمية' : isFriday ? 'عطلة أسبوعية' : isSaturday ? 'يوم عمل قضائي' : 'يوم عمل'}
            </span>
          </div>

          {/* Navigation tabs */}
          <div className="flex items-center bg-[#172B35]/90 p-1 rounded-xl border border-white/10 backdrop-blur-md">
            <button
              onClick={() => setActiveTab('calculator')}
              className={`relative px-3.5 py-2 rounded-lg text-xs sm:text-sm font-semibold transition-all flex items-center gap-2 cursor-pointer ${
                activeTab === 'calculator'
                  ? 'bg-gradient-to-r from-amber-500 to-amber-600 text-slate-950 shadow-md font-bold'
                  : 'text-slate-300 hover:text-white hover:bg-white/5'
              }`}
            >
              <Calculator className="w-4 h-4" />
              <span>الحاسبة</span>
            </button>

            <button
              onClick={() => setActiveTab('saved')}
              className={`relative px-3.5 py-2 rounded-lg text-xs sm:text-sm font-semibold transition-all flex items-center gap-2 cursor-pointer ${
                activeTab === 'saved'
                  ? 'bg-gradient-to-r from-amber-500 to-amber-600 text-slate-950 shadow-md font-bold'
                  : 'text-slate-300 hover:text-white hover:bg-white/5'
              }`}
            >
              <Bookmark className="w-4 h-4" />
              <span>المحفوظات</span>
              {savedCount > 0 && (
                <span
                  className={`px-1.5 py-0.2 rounded-full text-[10px] font-mono font-bold ${
                    activeTab === 'saved'
                      ? 'bg-slate-950 text-amber-400'
                      : 'bg-amber-500 text-slate-950'
                  }`}
                >
                  {savedCount}
                </span>
              )}
            </button>

            <button
              onClick={() => setActiveTab('android')}
              className={`relative px-3.5 py-2 rounded-lg text-xs sm:text-sm font-semibold transition-all flex items-center gap-2 cursor-pointer ${
                activeTab === 'android'
                  ? 'bg-gradient-to-r from-amber-500 to-amber-600 text-slate-950 shadow-md font-bold'
                  : 'text-slate-300 hover:text-white hover:bg-white/5'
              }`}
            >
              <Smartphone className="w-4 h-4" />
              <span className="hidden sm:inline">أندرويد وكوتلن</span>
              <span className="sm:hidden">أندرويد</span>
            </button>
          </div>
        </div>
      </div>
    </header>
  );
};
