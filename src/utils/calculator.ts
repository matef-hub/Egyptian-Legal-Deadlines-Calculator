import { DeadlineResult, ExcludedDay, Holiday, LegalRule } from '../types';

export const ARABIC_DAYS = ['الأحد', 'الاثنين', 'الثلاثاء', 'الأربعاء', 'الخميس', 'الجمعة', 'السبت'];

export function getArabicDayName(date: Date): string {
  return ARABIC_DAYS[date.getDay()];
}

export function formatDisplayDate(date: Date): string {
  const day = String(date.getDate()).padStart(2, '0');
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const year = date.getFullYear();
  return `${year}/${month}/${day}`;
}

export function formatDisplayDateWithDay(date: Date): string {
  return `${formatDisplayDate(date)} (${getArabicDayName(date)})`;
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

  // 2. Compute Base End Date according to Article 15 Procedures Law
  const baseEndDate = new Date(startDate);
  if (rule.unit === 'DAYS') {
    const effectiveDays = Math.max(0, rule.duration - 1);
    baseEndDate.setDate(startDate.getDate() + effectiveDays);
  } else if (rule.unit === 'MONTHS') {
    if (rule.startRule === 'NEXT_DAY') {
      baseEndDate.setTime(announcementDate.getTime());
      baseEndDate.setMonth(announcementDate.getMonth() + rule.duration);
    } else {
      baseEndDate.setMonth(startDate.getMonth() + rule.duration);
    }
  } else if (rule.unit === 'YEARS') {
    if (rule.startRule === 'NEXT_DAY') {
      baseEndDate.setTime(announcementDate.getTime());
      baseEndDate.setFullYear(announcementDate.getFullYear() + rule.duration);
    } else {
      baseEndDate.setFullYear(startDate.getFullYear() + rule.duration);
    }
  } else if (rule.unit === 'HOURS') {
    const equivalentDays = Math.max(1, Math.ceil(rule.duration / 24));
    baseEndDate.setDate(startDate.getDate() + (equivalentDays - 1));
  }

  // 3. Add Distance Days (Articles 16 and 17)
  const provisionalDate = new Date(baseEndDate);
  provisionalDate.setDate(baseEndDate.getDate() + totalDistanceDays);

  // 4. Final Deadline Adjustment Loop (Article 18)
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

  // 5. Build Structured Milestones
  const milestones: any[] = [
    {
      type: 'TRIGGER_EVENT',
      date: toIsoDate(announcementDate),
      title: 'الواقعة المنشئة للميعاد',
      description: `تاريخ الإعلان أو صدور الحكم القضائي (${formatDisplayDate(announcementDate)}) (${getArabicDayName(announcementDate)}).`,
      legalBasis: rule.lawArticle
    },
    {
      type: 'COUNTING_START',
      date: toIsoDate(startDate),
      title: 'بدء سريان الميعاد الإجرائي',
      description: rule.startRule === 'NEXT_DAY'
        ? `يبدأ احتساب الميعاد من اليوم التالي للإعلان (${formatDisplayDate(startDate)}) ولا يُحسب يوم الإعلان نفسه إعمالاً للمادة 15 مرافعات.`
        : `يبدأ احتساب الميعاد من نفس يوم الواقعة (${formatDisplayDate(startDate)}) وفقاً لنص القاعدة.`,
      legalBasis: 'المادة 15 مرافعات'
    },
    {
      type: 'BASE_DEADLINE',
      date: toIsoDate(baseEndDate),
      title: 'نهاية المدة القانونية الأصلية',
      description: `انقضاء مدة الميعاد الأصلية (${rule.duration} ${rule.unit === 'DAYS' ? 'يوم' : rule.unit === 'MONTHS' ? 'شهر' : 'سنة'}) في (${formatDisplayDate(baseEndDate)}).`,
      legalBasis: rule.lawArticle
    }
  ];

  if (totalDistanceDays > 0) {
    milestones.push({
      type: 'DISTANCE_EXTENSION',
      date: toIsoDate(provisionalDate),
      title: 'إضافة ميعاد مسافة الطريق',
      description: `إضافة ${totalDistanceDays} أيام مسافة ليمتد الميعاد المبدئي إلى (${formatDisplayDate(provisionalDate)}).`,
      legalBasis: totalDistanceDays > 4 ? 'المادة 17 مرافعات (موطن بالخارج)' : 'المادة 16 مرافعات (مسافة محلية)'
    });
  }

  excludedDays.forEach(ex => {
    milestones.push({
      type: 'HOLIDAY_EXTENSION',
      date: ex.date,
      title: 'امتداد الميعاد لمصادفة عطلة',
      description: `مصادفة تاريخ (${ex.reason.split('—')[0].trim()}) عطلة، ويمتد الميعاد إعمالاً للمادة 18 مرافعات.`,
      legalBasis: 'المادة 18 مرافعات',
      isExcludedDay: true
    });
  });

  milestones.push({
    type: 'FINAL_DEADLINE',
    date: toIsoDate(finalDeadline),
    title: 'الميعاد النهائي الملزم للعمل الإجرائي',
    description: `آخر موعد لرفع الطعن أو قيد الصحيفة بقلم الكتاب (${formatDisplayDate(finalDeadline)}) (${getArabicDayName(finalDeadline)})${
      finalDeadline.getDay() === 6 ? ' [السبت يوم عمل رسمي بالمحاكم المصرية].' : ''
    }`,
    legalBasis: 'المادتان 15 و 18 مرافعات'
  });

  // 6. Generate Comprehensive Arabic Explanation
  const explanationLines: string[] = [
    `١. تاريخ الإعلان / الإجراء: ${formatDisplayDate(announcementDate)} (${getArabicDayName(announcementDate)})`,
    `٢. تاريخ بدء احتساب الميعاد: ${formatDisplayDate(startDate)} (${getArabicDayName(startDate)})${
      rule.startRule === 'NEXT_DAY'
        ? ' — تطبيقاً للمادة 15 مرافعات: يبدأ احتساب الميعاد من اليوم التالي للإعلان ولا يُحسب يوم الإجراء نفسه.'
        : ' — يبدأ احتساب الميعاد من نفس يوم الإعلان وفقاً لنص القاعدة الإجرائية.'
    }`,
    `٣. مدة الميعاد القانوني: ${rule.duration} ${
      rule.unit === 'DAYS' ? 'أيام' : rule.unit === 'MONTHS' ? 'أشهر' : rule.unit === 'YEARS' ? 'سنوات' : 'ساعات'
    }`,
    `٤. وحدة الاحتساب: ${
      rule.unit === 'DAYS' ? 'أيام' : rule.unit === 'MONTHS' ? 'أشهر' : rule.unit === 'YEARS' ? 'سنوات' : 'ساعات'
    }`,
    `٥. ميعاد المسافة القانوني الأصلي: ${rule.distanceDays} يوم (المادتان 16 و17 مرافعات)`,
    `٦. ميعاد المسافة الإضافي: ${additionalDistanceDays} يوم`,
    `٧. إجمالي مواعيد المسافة المضافة: ${totalDistanceDays} يوم`,
    `٨. الميعاد المبدئي (قبل استبعاد العطلات): ${formatDisplayDate(provisionalDate)} (${getArabicDayName(provisionalDate)})`,
    excludedDays.length > 0
      ? `٩. الأيام المستبعدة والامتداد (المادة 18 مرافعات):\n${excludedDays
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
    notes: rule.notes,
    milestones
  };
}

