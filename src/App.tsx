import React, { useState, useEffect } from 'react';
import { DeadlineResult, Holiday, LegalRule, SavedDeadline } from './types';
import { INITIAL_LEGAL_RULES, OFFICIAL_EGYPTIAN_HOLIDAYS } from './data/legalRules';
import { calculateLegalDeadline, toIsoDate } from './utils/calculator';
import { Header } from './components/Header';
import { CalculatorForm } from './components/CalculatorForm';
import { ResultCard } from './components/ResultCard';
import { SavedDeadlinesList } from './components/SavedDeadlinesList';
import { HolidaysModal } from './components/HolidaysModal';
import { AndroidProjectViewer } from './components/AndroidProjectViewer';

export default function App() {
  const [activeTab, setActiveTab] = useState<'calculator' | 'saved' | 'android'>('calculator');
  const [isHolidaysModalOpen, setIsHolidaysModalOpen] = useState(false);

  // Rules & Holidays state
  const [rules] = useState<LegalRule[]>(INITIAL_LEGAL_RULES);
  const [holidays] = useState<Holiday[]>(OFFICIAL_EGYPTIAN_HOLIDAYS);

  // Form Inputs
  const [selectedRuleId, setSelectedRuleId] = useState<number>(INITIAL_LEGAL_RULES[0].id);
  const [announcementDate, setAnnouncementDate] = useState<string>(toIsoDate(new Date()));
  const [additionalDistanceDays, setAdditionalDistanceDays] = useState<number>(0);
  const [caseNumber, setCaseNumber] = useState<string>('');
  const [clientName, setClientName] = useState<string>('');

  // Proactive Alert Settings (Default: 3 days before)
  const [proactiveReminderDays, setProactiveReminderDays] = useState<number>(3);
  const [calendarTarget, setCalendarTarget] = useState<'FINAL_DEADLINE' | 'PROACTIVE_ALERT'>('FINAL_DEADLINE');

  // Calculation Result & Saved state
  const [result, setResult] = useState<DeadlineResult | null>(null);
  const [savedDeadlines, setSavedDeadlines] = useState<SavedDeadline[]>(() => {
    try {
      const stored = localStorage.getItem('egyptian_legal_saved_deadlines');
      return stored ? JSON.parse(stored) : [];
    } catch {
      return [];
    }
  });
  const [saveSuccessNotice, setSaveSuccessNotice] = useState<boolean>(false);

  // Sync saved deadlines to localStorage
  useEffect(() => {
    try {
      localStorage.setItem('egyptian_legal_saved_deadlines', JSON.stringify(savedDeadlines));
    } catch (e) {
      console.error('Failed to persist deadlines locally', e);
    }
  }, [savedDeadlines]);

  const selectedRule = rules.find((r) => r.id === selectedRuleId) || rules[0];

  const handleCalculate = () => {
    const calcResult = calculateLegalDeadline(
      announcementDate,
      selectedRule,
      additionalDistanceDays,
      holidays
    );
    setResult(calcResult);
    setSaveSuccessNotice(false);

    // Smooth scroll down to result card on mobile
    setTimeout(() => {
      window.scrollTo({ top: 350, behavior: 'smooth' });
    }, 100);
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
      createdAt: Date.now(),
      proactiveReminderDays: proactiveReminderDays > 0 ? proactiveReminderDays : undefined
    };

    setSavedDeadlines((prev) => [newDeadline, ...prev]);
    setSaveSuccessNotice(true);
  };

  const handleDeleteDeadline = (id: string) => {
    setSavedDeadlines((prev) => prev.filter((d) => d.id !== id));
  };

  const handleImportDeadlines = (imported: SavedDeadline[]) => {
    setSavedDeadlines((prev) => [...imported, ...prev]);
  };

  const handleClearAll = () => {
    setSavedDeadlines([]);
  };

  return (
    <div className="min-h-screen flex flex-col bg-[#F8FAFC] text-slate-900 antialiased font-['Cairo',sans-serif] selection:bg-amber-500/20 selection:text-amber-900">
      {/* Supreme Egyptian Legal Styled Header */}
      <Header
        activeTab={activeTab}
        setActiveTab={setActiveTab}
        savedCount={savedDeadlines.length}
        holidays={holidays}
        onOpenHolidaysModal={() => setIsHolidaysModalOpen(true)}
      />

      {/* Main Content Area */}
      <main className="flex-1 max-w-5xl w-full mx-auto px-4 sm:px-8 py-6 space-y-6">
        {/* TAB 1: Calculator */}
        {activeTab === 'calculator' && (
          <div className="space-y-6">
            <CalculatorForm
              rules={rules}
              selectedRuleId={selectedRuleId}
              setSelectedRuleId={(id) => {
                setSelectedRuleId(id);
                setResult(null);
                setSaveSuccessNotice(false);
              }}
              announcementDate={announcementDate}
              setAnnouncementDate={setAnnouncementDate}
              additionalDistanceDays={additionalDistanceDays}
              setAdditionalDistanceDays={setAdditionalDistanceDays}
              caseNumber={caseNumber}
              setCaseNumber={setCaseNumber}
              clientName={clientName}
              setClientName={setClientName}
              onCalculate={handleCalculate}
            />

            {result && (
              <ResultCard
                result={result}
                selectedRule={selectedRule}
                announcementDate={announcementDate}
                caseNumber={caseNumber}
                clientName={clientName}
                additionalDistanceDays={additionalDistanceDays}
                holidays={holidays}
                proactiveReminderDays={proactiveReminderDays}
                setProactiveReminderDays={setProactiveReminderDays}
                calendarTarget={calendarTarget}
                setCalendarTarget={setCalendarTarget}
                onSave={handleSave}
                isSaved={saveSuccessNotice}
                onReset={() => {
                  setResult(null);
                  setSaveSuccessNotice(false);
                }}
              />
            )}
          </div>
        )}

        {/* TAB 2: Saved Deadlines List */}
        {activeTab === 'saved' && (
          <SavedDeadlinesList
            savedDeadlines={savedDeadlines}
            onDeleteDeadline={handleDeleteDeadline}
            onImportDeadlines={handleImportDeadlines}
            onClearAll={handleClearAll}
          />
        )}

        {/* TAB 3: Android Project Explorer */}
        {activeTab === 'android' && <AndroidProjectViewer />}
      </main>

      {/* Egyptian Legal Footer */}
      <footer className="bg-white border-t border-slate-200 py-4 px-6 sm:px-8 text-xs text-slate-500 mt-auto">
        <div className="max-w-5xl mx-auto flex flex-col sm:flex-row items-center justify-between gap-3 text-center sm:text-right">
          <div className="flex items-center gap-2">
            <span className="w-2 h-2 rounded-full bg-emerald-500"></span>
            <span>
              قانون المرافعات المدنية والتجارية المصري رقم 13 لسنة 1968 وتعديلاته
            </span>
          </div>
          <div className="text-[11px] text-slate-400">
            اعتماد السبت يوم عمل قضائي • استبعاد الجمعة والعطلات الرسمية
          </div>
        </div>
      </footer>

      {/* Holidays Modal */}
      <HolidaysModal
        isOpen={isHolidaysModalOpen}
        onClose={() => setIsHolidaysModalOpen(false)}
        holidays={holidays}
      />
    </div>
  );
}
