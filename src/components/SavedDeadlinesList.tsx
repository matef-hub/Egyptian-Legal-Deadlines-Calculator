import React, { useState } from 'react';
import {
  Bookmark,
  Search,
  CalendarPlus,
  Trash2,
  ChevronDown,
  ChevronUp,
  Folder,
  User,
  Clock,
  AlertCircle,
  Download,
  Upload,
  CalendarDays,
  Bell,
  Scale,
  FileText,
  AlertTriangle,
  CheckCircle2
} from 'lucide-react';
import { SavedDeadline } from '../types';
import { formatDisplayDate, getArabicDayName, parseIsoDate } from '../utils/calculator';
import { createGoogleCalendarUrl } from '../utils/calendar';

interface SavedDeadlinesListProps {
  savedDeadlines: SavedDeadline[];
  onDeleteDeadline: (id: string) => void;
  onImportDeadlines: (deadlines: SavedDeadline[]) => void;
  onClearAll: () => void;
}

export const SavedDeadlinesList: React.FC<SavedDeadlinesListProps> = ({
  savedDeadlines,
  onDeleteDeadline,
  onImportDeadlines,
  onClearAll
}) => {
  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState<'ALL' | 'ACTIVE' | 'URGENT' | 'EXPIRED'>('ALL');
  const [expandedId, setExpandedId] = useState<string | null>(null);
  const [deleteConfirmationId, setDeleteConfirmationId] = useState<string | null>(null);
  const [showClearConfirm, setShowClearConfirm] = useState(false);

  const today = new Date();
  today.setHours(0, 0, 0, 0);

  // Filter deadlines
  const filtered = savedDeadlines.filter((item) => {
    const finalDate = parseIsoDate(item.finalDeadline);
    finalDate.setHours(0, 0, 0, 0);
    const diffDays = Math.round(
      (finalDate.getTime() - today.getTime()) / (1000 * 60 * 60 * 24)
    );

    // Status filter
    if (statusFilter === 'ACTIVE' && diffDays < 0) return false;
    if (statusFilter === 'URGENT' && (diffDays < 0 || diffDays > 7)) return false;
    if (statusFilter === 'EXPIRED' && diffDays >= 0) return false;

    // Search filter
    if (searchTerm.trim() !== '') {
      const term = searchTerm.toLowerCase();
      const matchAction = item.actionName.toLowerCase().includes(term);
      const matchCase = item.caseNumber.toLowerCase().includes(term);
      const matchClient = item.clientName.toLowerCase().includes(term);
      const matchArticle = item.lawArticle?.toLowerCase().includes(term);
      return matchAction || matchCase || matchClient || matchArticle;
    }
    return true;
  });

  // Export to JSON file
  const handleExportJson = () => {
    if (savedDeadlines.length === 0) return;
    const dataStr =
      'data:text/json;charset=utf-8,' +
      encodeURIComponent(JSON.stringify(savedDeadlines, null, 2));
    const downloadAnchor = document.createElement('a');
    downloadAnchor.setAttribute('href', dataStr);
    downloadAnchor.setAttribute(
      'download',
      `egyptian_legal_deadlines_backup_${new Date().toISOString().split('T')[0]}.json`
    );
    document.body.appendChild(downloadAnchor);
    downloadAnchor.click();
    downloadAnchor.remove();
  };

  // Import JSON file
  const handleImportJson = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    const reader = new FileReader();
    reader.onload = (event) => {
      try {
        const parsed = JSON.parse(event.target?.result as string);
        if (Array.isArray(parsed)) {
          onImportDeadlines(parsed);
        }
      } catch (err) {
        console.error('Failed to parse imported deadlines file', err);
      }
    };
    reader.readAsText(file);
    e.target.value = '';
  };

  return (
    <div className="bg-white border border-slate-200 rounded-2xl p-6 sm:p-8 space-y-6 shadow-sm">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-4 border-b border-slate-100">
        <div>
          <div className="flex items-center gap-2.5">
            <div className="w-9 h-9 rounded-xl bg-amber-500/10 border border-amber-500/20 text-amber-700 flex items-center justify-center">
              <Bookmark className="w-5 h-5" />
            </div>
            <div>
              <h2 className="text-lg font-bold text-slate-900">سجل المواعيد المحفوظة</h2>
              <p className="text-xs text-slate-500">
                متابعة دقيقة لمواعيد قضايا الموكلين مرتبة من الأحدث إلى الأقدم
              </p>
            </div>
          </div>
        </div>

        {/* Action buttons (Backup/Restore) */}
        <div className="flex items-center gap-2">
          {savedDeadlines.length > 0 && (
            <button
              type="button"
              onClick={handleExportJson}
              className="px-3 py-1.5 rounded-xl border border-slate-200 text-xs font-semibold text-slate-700 hover:bg-slate-50 transition-colors flex items-center gap-1.5 cursor-pointer"
              title="تصدير نسخة احتياطية من المواعيد"
            >
              <Download className="w-3.5 h-3.5 text-slate-600" />
              <span>تصدير نسخة</span>
            </button>
          )}

          <label className="px-3 py-1.5 rounded-xl border border-slate-200 text-xs font-semibold text-slate-700 hover:bg-slate-50 transition-colors flex items-center gap-1.5 cursor-pointer">
            <Upload className="w-3.5 h-3.5 text-slate-600" />
            <span>استيراد</span>
            <input
              type="file"
              accept=".json"
              onChange={handleImportJson}
              className="hidden"
            />
          </label>

          <span className="px-3 py-1 rounded-full text-xs font-mono font-bold bg-slate-100 text-slate-800 border border-slate-200">
            {savedDeadlines.length} ميعاد
          </span>
        </div>
      </div>

      {/* Search & Status Filters */}
      <div className="flex flex-col md:flex-row gap-3 items-stretch md:items-center justify-between">
        {/* Search */}
        <div className="relative flex-1">
          <Search className="w-4 h-4 text-slate-400 absolute right-3.5 top-3" />
          <input
            type="text"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            placeholder="ابحث برقم القضية، اسم الموكل، أو نوع الإجراء..."
            className="w-full bg-slate-50 border border-slate-200 rounded-xl pr-10 pl-4 py-2 text-xs text-slate-900 focus:bg-white focus:outline-none focus:border-amber-500 transition-colors"
          />
        </div>

        {/* Filter Pills */}
        <div className="flex items-center gap-1.5 overflow-x-auto pb-1 text-xs">
          {[
            { id: 'ALL', label: 'الكل' },
            { id: 'ACTIVE', label: 'السارية' },
            { id: 'URGENT', label: 'وشيكة (أقل من أسبوع)' },
            { id: 'EXPIRED', label: 'المنقضية' }
          ].map((f) => {
            const isSelected = statusFilter === f.id;
            return (
              <button
                key={f.id}
                type="button"
                onClick={() => setStatusFilter(f.id as any)}
                className={`px-3 py-1.5 rounded-xl text-xs font-semibold transition-all cursor-pointer whitespace-nowrap ${
                  isSelected
                    ? 'bg-[#0F2027] text-amber-400 font-bold shadow-xs'
                    : 'bg-slate-100 text-slate-600 hover:bg-slate-200'
                }`}
              >
                {f.label}
              </button>
            );
          })}
        </div>
      </div>

      {/* Deadlines List */}
      {savedDeadlines.length === 0 ? (
        <div className="text-center py-16 px-4 bg-slate-50/50 rounded-2xl border border-dashed border-slate-200">
          <div className="w-12 h-12 rounded-2xl bg-amber-50 border border-amber-200 text-amber-600 flex items-center justify-center mx-auto mb-3">
            <Bookmark className="w-6 h-6" />
          </div>
          <h3 className="text-sm font-bold text-slate-800">لا توجد مواعيد محفوظة بعد</h3>
          <p className="text-xs text-slate-500 max-w-sm mx-auto mt-1">
            استخدم الحاسبة لاحتساب الميعاد القانوني ثم اضغط على "حفظ الميعاد" ليتم أرشفته هنا للرجوع إليه دائماً.
          </p>
        </div>
      ) : filtered.length === 0 ? (
        <div className="text-center py-12 text-slate-400 bg-slate-50 rounded-2xl">
          <p className="text-xs">لا توجد مواعيد تطابق شروط التصفية المحددة</p>
        </div>
      ) : (
        <div className="space-y-3.5">
          {filtered.map((deadline) => {
            const isExpanded = expandedId === deadline.id;
            const finalDate = parseIsoDate(deadline.finalDeadline);
            finalDate.setHours(0, 0, 0, 0);
            const diffDays = Math.round(
              (finalDate.getTime() - today.getTime()) / (1000 * 60 * 60 * 24)
            );

            const isUrgent = diffDays >= 0 && diffDays <= 7;
            const isExpired = diffDays < 0;

            return (
              <div
                key={deadline.id}
                className={`rounded-2xl border transition-all p-5 ${
                  isUrgent
                    ? 'bg-amber-50/40 border-amber-300 shadow-xs'
                    : isExpired
                    ? 'bg-slate-50 border-slate-200 opacity-80'
                    : 'bg-white border-slate-200 hover:border-slate-300 shadow-xs'
                }`}
              >
                <div className="flex flex-col sm:flex-row sm:items-start justify-between gap-4">
                  <div className="space-y-2 flex-1 min-w-0">
                    {/* Header Action Name & Status Tag */}
                    <div className="flex items-center gap-2.5 flex-wrap">
                      <h4 className="text-sm sm:text-base font-bold text-slate-900 leading-snug">
                        {deadline.actionName}
                      </h4>

                      {isExpired ? (
                        <span className="text-[10px] font-bold bg-slate-200 text-slate-700 px-2.5 py-0.5 rounded-full">
                          انقضى الميعاد
                        </span>
                      ) : isUrgent ? (
                        <span className="text-[10px] font-bold bg-amber-500 text-slate-950 px-2.5 py-0.5 rounded-full animate-pulse">
                          وشيك: متبقي {diffDays} {diffDays === 1 ? 'يوم' : 'أيام'}
                        </span>
                      ) : (
                        <span className="text-[10px] font-bold bg-emerald-100 text-emerald-800 px-2.5 py-0.5 rounded-full">
                          سارٍ: متبقي {diffDays} يوماً
                        </span>
                      )}
                    </div>

                    {/* Meta info tags */}
                    <div className="flex flex-wrap items-center gap-3 text-xs text-slate-600">
                      {deadline.caseNumber && (
                        <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-lg bg-slate-100 font-mono text-slate-800 border border-slate-200">
                          <Folder className="w-3 h-3 text-slate-500" />
                          <span>{deadline.caseNumber}</span>
                        </span>
                      )}

                      {deadline.clientName && (
                        <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-lg bg-slate-100 text-slate-800 border border-slate-200">
                          <User className="w-3 h-3 text-slate-500" />
                          <span>{deadline.clientName}</span>
                        </span>
                      )}

                      <span className="text-slate-500 font-medium" dir="rtl">
                        تاريخ الإعلان:{' '}
                        <span dir="ltr" className="font-mono">{formatDisplayDate(parseIsoDate(deadline.announcementDate))}</span>
                      </span>
                    </div>

                    {/* Final Deadline Highlights */}
                    <div className="flex flex-wrap items-center gap-2 pt-1">
                      <span className="text-xs text-slate-500">الميعاد النهائي:</span>
                      <span className="text-xs sm:text-sm font-bold text-slate-900 bg-white px-3 py-1 rounded-lg border border-slate-200 shadow-2xs" dir="rtl">
                        <span dir="ltr" className="font-mono">{formatDisplayDate(finalDate)}</span> ({getArabicDayName(finalDate)})
                      </span>

                      {deadline.proactiveReminderDays && (
                        <span className="text-[11px] font-medium text-blue-700 bg-blue-50 border border-blue-100 px-2.5 py-0.5 rounded-lg flex items-center gap-1">
                          <Bell className="w-3 h-3 text-blue-600" />
                          تنبيه مسبق: قبل {deadline.proactiveReminderDays} أيام
                        </span>
                      )}
                    </div>
                  </div>

                  {/* Actions Toolbar */}
                  <div className="flex items-center gap-2 shrink-0 self-end sm:self-auto">
                    {/* Google Calendar Link */}
                    <a
                      href={createGoogleCalendarUrl({
                        title: deadline.actionName,
                        finalDeadlineDate: deadline.finalDeadline,
                        lawArticle: deadline.lawArticle,
                        explanation: deadline.calculationExplanation,
                        notes: deadline.notes,
                        caseNumber: deadline.caseNumber,
                        clientName: deadline.clientName,
                        proactiveReminderDays: deadline.proactiveReminderDays || 3
                      })}
                      target="_blank"
                      rel="noopener noreferrer"
                      className="p-2 text-slate-600 hover:text-blue-700 bg-white hover:bg-blue-50 border border-slate-200 hover:border-blue-200 rounded-xl text-xs font-semibold flex items-center gap-1 transition-colors cursor-pointer"
                      title="إضافة إلى تقويم Google"
                    >
                      <CalendarPlus className="w-3.5 h-3.5 text-blue-600" />
                      <span className="hidden sm:inline">التقويم</span>
                    </a>

                    {/* Toggle details */}
                    <button
                      type="button"
                      onClick={() => setExpandedId(isExpanded ? null : deadline.id)}
                      className="p-2 text-slate-600 hover:text-slate-900 bg-white border border-slate-200 rounded-xl text-xs font-semibold flex items-center gap-1 transition-colors cursor-pointer"
                    >
                      {isExpanded ? (
                        <ChevronUp className="w-3.5 h-3.5" />
                      ) : (
                        <ChevronDown className="w-3.5 h-3.5" />
                      )}
                      <span>{isExpanded ? 'طي' : 'التفاصيل'}</span>
                    </button>

                    {/* Delete button */}
                    <button
                      type="button"
                      onClick={() => setDeleteConfirmationId(deadline.id)}
                      className="p-2 text-slate-400 hover:text-red-600 bg-white hover:bg-red-50 border border-slate-200 hover:border-red-200 rounded-xl transition-colors cursor-pointer"
                      title="حذف من المحفوظات"
                    >
                      <Trash2 className="w-3.5 h-3.5" />
                    </button>
                  </div>
                </div>

                {/* Expanded Details */}
                {isExpanded && (
                  <div className="mt-4 pt-4 border-t border-slate-200/80 space-y-3 animate-in fade-in-50 duration-150">
                    {deadline.lawArticle && (
                      <div className="p-3 bg-white rounded-xl border border-slate-200 space-y-1">
                        <span className="text-[10px] font-bold text-amber-700 uppercase tracking-wider block">
                          السند القانوني:
                        </span>
                        <p className="text-xs font-medium text-slate-900">
                          {deadline.lawArticle}
                        </p>
                      </div>
                    )}

                    <div className="p-3.5 bg-white rounded-xl border border-slate-200 space-y-1">
                      <span className="text-[10px] font-bold text-slate-500 uppercase tracking-wider block">
                        تفاصيل بيان الاحتساب:
                      </span>
                      <p className="text-xs text-slate-800 whitespace-pre-line leading-relaxed">
                        {deadline.calculationExplanation}
                      </p>
                    </div>

                    {deadline.notes && (
                      <p className="text-[11px] text-slate-500 italic">
                        ملاحظات: {deadline.notes}
                      </p>
                    )}
                  </div>
                )}
              </div>
            );
          })}
        </div>
      )}

      {/* Delete Confirmation Modal */}
      {deleteConfirmationId && (
        <div className="fixed inset-0 z-50 bg-slate-950/60 backdrop-blur-xs flex items-center justify-center p-4">
          <div className="bg-white rounded-2xl max-w-md w-full p-6 space-y-4 shadow-2xl border border-slate-200 animate-in zoom-in-95 duration-150">
            <div className="w-12 h-12 rounded-2xl bg-red-50 border border-red-200 text-red-600 flex items-center justify-center mx-auto">
              <AlertCircle className="w-6 h-6" />
            </div>
            <div className="text-center">
              <h3 className="text-base font-bold text-slate-900">
                تأكيد حذف الميعاد
              </h3>
              <p className="text-xs text-slate-500 mt-1">
                هل أنت متأكد من رغبتك في حذف هذا الميعاد من سجلاتك المحلية؟ لن يمكنك التراجع عن هذا الإجراء.
              </p>
            </div>
            <div className="flex items-center gap-3 pt-2">
              <button
                type="button"
                onClick={() => {
                  onDeleteDeadline(deleteConfirmationId);
                  setDeleteConfirmationId(null);
                }}
                className="flex-1 bg-red-600 hover:bg-red-700 text-white font-semibold text-xs py-2.5 rounded-xl transition-colors cursor-pointer"
              >
                حذف نهائي
              </button>
              <button
                type="button"
                onClick={() => setDeleteConfirmationId(null)}
                className="flex-1 bg-slate-100 hover:bg-slate-200 text-slate-800 font-semibold text-xs py-2.5 rounded-xl transition-colors cursor-pointer"
              >
                إلغاء
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
