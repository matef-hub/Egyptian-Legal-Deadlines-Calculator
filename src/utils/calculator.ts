import { DeadlineResult, ExcludedDay, Holiday, LegalRule } from '../types';

export const ARABIC_DAYS = ['الأحد', 'الاثنين', 'الثلاثاء', 'الأربعاء', 'الخميس', 'الجمعة', 'السبت'];

export function getArabicDayName(date: Date): string {
  return ARABIC_DAYS[date.getDay()];
}

export function formatDisplayDate(date: Date): string {
  const day = String(date.getDate()).padStart(2, '0');
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const year = date.getFullYear();
  return `${day}/${month}/${year}`;
}

export function toIsoDate(date: Date): string {
  const day = String(date.getDate()).padStart(2, '0');
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const year = date.getFullYear();
  return `${year}-${month}-${day}`;
}

export function parseIsoDate(iso: string): Date {
  const [year, month, day] = iso.split('-').map(Number);
  return new Date(year, month - 1, day);
}

/**
 * Procedural Legal Deadline Calculator strictly matching Egyptian procedural law.
 * Friday is excluded as a weekly holiday.
 * Saturday is a working day in Egyptian courts.
 * Official Egyptian holidays are excluded.
 */
export function calculateLegalDeadline(
  announcementDateStr: string,
  rule: LegalRule,
  additionalDistanceDays: number = 0,
  holidays: Holiday[] = []
): DeadlineResult {
  const holidayMap = new Map<string, string>();
  holidays.forEach(h => holidayMap.set(h.holidayDate, h.name));

  const announcementDate = parseIsoDate(announcementDateStr);
  const totalDistanceDays = rule.distanceDays + additionalDistanceDays;

  // 1. Determine Start Counting Date
  const startDate = new Date(announcementDate);
  if (rule.startRule === 'NEXT_DAY') {
    startDate.setDate(startDate.getDate() + 1);
  }

  // 2. Compute Provisional Deadline
  const provisionalDate = new Date(startDate);
  if (rule.unit === 'DAYS') {
    const effectiveDays = Math.max(0, rule.duration - 1);
    provisionalDate.setDate(provisionalDate.getDate() + effectiveDays + totalDistanceDays);
  } else if (rule.unit === 'MONTHS') {
    provisionalDate.setMonth(provisionalDate.getMonth() + rule.duration);
    provisionalDate.setDate(provisionalDate.getDate() + totalDistanceDays);
  } else if (rule.unit === 'YEARS') {
    provisionalDate.setFullYear(provisionalDate.getFullYear() + rule.duration);
    provisionalDate.setDate(provisionalDate.getDate() + totalDistanceDays);
  } else if (rule.unit === 'HOURS') {
    const equivalentDays = Math.max(1, Math.ceil(rule.duration / 24));
    provisionalDate.setDate(provisionalDate.getDate() + (equivalentDays - 1) + totalDistanceDays);
  }

  // 3. Final Deadline Adjustment Loop
  const currentDate = new Date(provisionalDate);
  const excludedDays: ExcludedDay[] = [];

  while (true) {
    const dayOfWeek = currentDate.getDay(); // 0: Sun, 1: Mon, ... 5: Fri, 6: Sat
    const isoCurrent = toIsoDate(currentDate);
    const isHoliday = holidayMap.has(isoCurrent);
    const holidayName = holidayMap.get(isoCurrent);

    if (dayOfWeek === 5) { // Friday
      const formatted = formatDisplayDate(currentDate);
      excludedDays.push({
        date: isoCurrent,
        reason: `${formatted} — الجمعة — عطلة أسبوعية`
      });
      currentDate.setDate(currentDate.getDate() + 1);
    } else if (isHoliday) { // Official holiday
      const formatted = formatDisplayDate(currentDate);
      excludedDays.push({
        date: isoCurrent,
        reason: `${formatted} — عطلة رسمية — ${holidayName || 'عطلة رسمية معتمدة'}`
      });
      currentDate.setDate(currentDate.getDate() + 1);
    } else {
      // Saturday (dayOfWeek === 6) is a valid court working day!
      break;
    }
  }

  const finalDeadline = new Date(currentDate);

  // 4. Generate Comprehensive Arabic Explanation
  const explanationLines: string[] = [
    `١. تاريخ الإعلان / الإجراء: ${formatDisplayDate(announcementDate)} (${getArabicDayName(announcementDate)})`,
    `٢. تاريخ بدء احتساب الميعاد: ${formatDisplayDate(startDate)} (${getArabicDayName(startDate)})${
      rule.startRule === 'NEXT_DAY'
        ? ' — تطبيقاً للقاعدة الإجرائية: يبدأ احتساب الميعاد من اليوم التالي للإعلان ولا يُحسب يوم الإجراء نفسه.'
        : ' — يبدأ احتساب الميعاد من نفس يوم الإعلان وفقاً لنص القاعدة الإجرائية.'
    }`,
    `٣. مدة الميعاد القانوني: ${rule.duration} ${
      rule.unit === 'DAYS' ? 'أيام' : rule.unit === 'MONTHS' ? 'أشهر' : rule.unit === 'YEARS' ? 'سنوات' : 'ساعات'
    }`,
    `٤. وحدة الاحتساب: ${
      rule.unit === 'DAYS' ? 'أيام' : rule.unit === 'MONTHS' ? 'أشهر' : rule.unit === 'YEARS' ? 'سنوات' : 'ساعات'
    }`,
    `٥. ميعاد المسافة القانوني الأصلي: ${rule.distanceDays} يوم`,
    `٦. ميعاد المسافة الإضافي: ${additionalDistanceDays} يوم`,
    `٧. إجمالي مواعيد المسافة المضافة: ${totalDistanceDays} يوم`,
    `٨. الميعاد المبدئي (قبل استبعاد العطلات): ${formatDisplayDate(provisionalDate)} (${getArabicDayName(provisionalDate)})`,
    excludedDays.length > 0
      ? `٩. الأيام المستبعدة لمصادفتها عطلة رسمية أو أسبوعية:\n${excludedDays
          .map((ex, i) => `   • [${i + 1}] ${ex.reason}`)
          .join('\n')}`
      : '٩. الأيام المستبعدة: لا يوجد أيام مستبعدة (صادف الميعاد المبدئي يوم عمل رسمي مباشرة).',
    `١٠. الميعاد النهائي واجب الالتزام به: ${formatDisplayDate(finalDeadline)} (${getArabicDayName(finalDeadline)})${
      finalDeadline.getDay() === 6
        ? ' [ملاحظة قانونية: يوم السبت يُعد يوم عمل كامل في المحاكم المصرية وفقاً للقانون].'
        : ''
    }`
  ];

  return {
    startDate: toIsoDate(startDate),
    provisionalDate: toIsoDate(provisionalDate),
    duration: rule.duration,
    unit: rule.unit,
    ruleDistanceDays: rule.distanceDays,
    additionalDistanceDays,
    totalDistanceDays,
    excludedDays,
    finalDeadline: toIsoDate(finalDeadline),
    explanation: explanationLines.join('\n'),
    lawArticle: rule.lawArticle,
    notes: rule.notes
  };
}
