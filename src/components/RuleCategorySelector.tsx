import React, { useState } from 'react';
import { Search, ChevronDown, Check, Scale, BookOpen, Clock, Tag } from 'lucide-react';
import { LegalRule } from '../types';
import { LEGAL_CATEGORIES } from '../data/legalRules';

interface RuleCategorySelectorProps {
  rules: LegalRule[];
  selectedRuleId: number;
  onSelectRule: (ruleId: number) => void;
}

export const RuleCategorySelector: React.FC<RuleCategorySelectorProps> = ({
  rules,
  selectedRuleId,
  onSelectRule
}) => {
  const [activeCategory, setActiveCategory] = useState<string>('الكل');
  const [searchTerm, setSearchTerm] = useState<string>('');
  const [isDropdownOpen, setIsDropdownOpen] = useState<boolean>(false);

  const selectedRule = rules.find((r) => r.id === selectedRuleId) || rules[0];

  // Filtering
  const filteredRules = rules.filter((rule) => {
    const matchesCategory =
      activeCategory === 'الكل' || rule.category === activeCategory;
    const matchesSearch =
      searchTerm.trim() === '' ||
      rule.actionName.includes(searchTerm) ||
      rule.lawArticle.includes(searchTerm) ||
      rule.notes.includes(searchTerm);
    return matchesCategory && matchesSearch;
  });

  return (
    <div className="space-y-3">
      <div className="flex items-center justify-between">
        <label className="text-xs font-bold text-slate-700 uppercase tracking-wider flex items-center gap-1.5">
          <Scale className="w-3.5 h-3.5 text-amber-600" />
          <span>الإجراء القضائي أو الميعاد القانوني</span>
        </label>
        <span className="text-[11px] text-slate-500 font-medium">
          متوفر {rules.length} ميعاد معتمد
        </span>
      </div>

      {/* Category Filter Chips */}
      <div className="flex items-center gap-1.5 overflow-x-auto pb-1.5 scrollbar-none text-xs">
        {LEGAL_CATEGORIES.map((cat) => {
          const isSelected = activeCategory === cat;
          return (
            <button
              key={cat}
              type="button"
              onClick={() => setActiveCategory(cat)}
              className={`px-3 py-1.5 rounded-full text-xs font-semibold whitespace-nowrap transition-all cursor-pointer border ${
                isSelected
                  ? 'bg-[#0F2027] text-amber-400 border-[#0F2027] shadow-xs'
                  : 'bg-white text-slate-600 border-slate-200 hover:bg-slate-50 hover:text-slate-900'
              }`}
            >
              {cat}
            </button>
          );
        })}
      </div>

      {/* Selected Rule Trigger Card */}
      <div className="relative">
        <button
          type="button"
          onClick={() => setIsDropdownOpen(!isDropdownOpen)}
          className="w-full text-right p-4 rounded-xl border border-slate-200 bg-white hover:border-amber-500/80 transition-all shadow-xs flex items-center justify-between gap-4 cursor-pointer focus:outline-none focus:ring-2 focus:ring-amber-500/20"
        >
          <div className="space-y-1.5 flex-1 min-w-0">
            <div className="flex items-center gap-2 flex-wrap">
              {selectedRule.category && (
                <span className="text-[10px] font-bold bg-amber-50 text-amber-800 border border-amber-200/80 px-2 py-0.5 rounded-full">
                  {selectedRule.category}
                </span>
              )}
              <span className="text-[11px] font-bold text-emerald-700 bg-emerald-50 border border-emerald-200 px-2 py-0.5 rounded-full flex items-center gap-1">
                <Clock className="w-3 h-3 text-emerald-600" />
                {selectedRule.duration}{' '}
                {selectedRule.unit === 'DAYS'
                  ? 'أيام'
                  : selectedRule.unit === 'MONTHS'
                  ? 'أشهر'
                  : 'سنوات'}
              </span>
            </div>
            <h3 className="text-sm sm:text-base font-bold text-slate-900 truncate">
              {selectedRule.actionName}
            </h3>
            <p className="text-xs text-slate-500 truncate">
              {selectedRule.lawArticle}
            </p>
          </div>

          <div className="w-8 h-8 rounded-lg bg-slate-100 flex items-center justify-center text-slate-600 shrink-0">
            <ChevronDown
              className={`w-4 h-4 transition-transform duration-200 ${
                isDropdownOpen ? 'rotate-180 text-amber-600' : ''
              }`}
            />
          </div>
        </button>

        {/* Dropdown Menu */}
        {isDropdownOpen && (
          <div className="absolute top-full right-0 left-0 mt-2 z-30 bg-white rounded-2xl border border-slate-200 shadow-xl overflow-hidden animate-in fade-in zoom-in-95 duration-150">
            {/* Search within dropdown */}
            <div className="p-3 border-b border-slate-100 bg-slate-50">
              <div className="relative">
                <Search className="w-4 h-4 text-slate-400 absolute right-3 top-2.5" />
                <input
                  type="text"
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                  placeholder="ابحث باسم الإجراء (مثال: استئناف، نقض، معارضة)..."
                  className="w-full bg-white border border-slate-200 rounded-xl pr-9 pl-4 py-2 text-xs text-slate-900 focus:outline-none focus:border-amber-500"
                  autoFocus
                />
              </div>
            </div>

            {/* List of rules */}
            <div className="max-h-72 overflow-y-auto divide-y divide-slate-100">
              {filteredRules.length === 0 ? (
                <div className="p-6 text-center text-xs text-slate-400">
                  لا توجد نتائج مطابقة لبحثك
                </div>
              ) : (
                filteredRules.map((rule) => {
                  const isSelected = rule.id === selectedRuleId;
                  return (
                    <button
                      key={rule.id}
                      type="button"
                      onClick={() => {
                        onSelectRule(rule.id);
                        setIsDropdownOpen(false);
                      }}
                      className={`w-full text-right p-3.5 transition-colors flex items-start justify-between gap-3 cursor-pointer ${
                        isSelected
                          ? 'bg-amber-50/70 text-slate-900'
                          : 'hover:bg-slate-50 text-slate-700'
                      }`}
                    >
                      <div className="space-y-1 flex-1">
                        <div className="flex items-center gap-2">
                          {rule.category && (
                            <span className="text-[9px] font-bold bg-slate-100 text-slate-600 px-1.5 py-0.2 rounded">
                              {rule.category}
                            </span>
                          )}
                          <span className="text-xs font-bold text-slate-900">
                            {rule.actionName}
                          </span>
                        </div>
                        <p className="text-[11px] text-slate-500">
                          {rule.lawArticle}
                        </p>
                      </div>

                      <div className="flex items-center gap-2 shrink-0">
                        <span className="text-[10px] font-semibold text-emerald-700 bg-emerald-50 border border-emerald-200 px-2 py-0.5 rounded-full">
                          {rule.duration} {rule.unit === 'DAYS' ? 'يوم' : rule.unit === 'MONTHS' ? 'شهر' : 'سنة'}
                        </span>
                        {isSelected && <Check className="w-4 h-4 text-amber-600" />}
                      </div>
                    </button>
                  );
                })
              )}
            </div>
          </div>
        )}
      </div>

      {/* Selected Rule Legislative Basis Box */}
      {selectedRule && (
        <div className="p-3.5 bg-slate-50 border border-slate-200/80 rounded-xl space-y-1 text-xs">
          <div className="flex items-center gap-2 text-slate-900 font-bold">
            <BookOpen className="w-3.5 h-3.5 text-amber-600" />
            <span>السند التشريعي المعتمد:</span>
            <span className="font-medium text-slate-700">{selectedRule.lawArticle}</span>
          </div>
          {selectedRule.notes && (
            <p className="text-[11px] text-slate-500 leading-relaxed pr-5">
              {selectedRule.notes}
            </p>
          )}
        </div>
      )}
    </div>
  );
};
