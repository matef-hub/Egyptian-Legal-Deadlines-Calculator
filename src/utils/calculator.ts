import { DeadlineResult, DistanceType, ExcludedDay, Holiday, LegalRule } from '../types';

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
 * Adds months to a Date with explicit clamping to the last day of the target month.
 * Under Article 15 Paragraph 4 of the Egyptian Civil & Commercial Procedures Law:
 * If the corresponding day does not exist in the target month (e.g. Jan 31 + 1 month),
 * the deadline expires on the last day of that month (e.g. Feb 28 or Feb 29 in leap years).
 */
export function addMonthsClamped(date: Date, months: number): Date {
  const currentYear = date.getFullYear();
  const currentMonth = date.getMonth();
  const currentDay = date.getDate();

  const totalMonths = currentMonth + months;
  const targetYear = currentYear + Math.floor(totalMonths / 12);
  const targetMonth = ((totalMonths % 12) + 12) % 12;

  // Day 0 of next month is the last day of targetMonth
  const daysInTargetMonth = new Date(targetYear, targetMonth + 1, 0).getDate();
  const targetDay = Math.min(currentDay, daysInTargetMonth);

  return new Date(targetYear, targetMonth, targetDay);
}

/**
 * Adds years to a Date with leap-year clamping (Feb 29 -> Feb 28 on non-leap years).
 */
export function addYearsClamped(date: Date, years: number): Date {
  const currentYear = date.getFullYear();
  const currentMonth = date.getMonth();
  const currentDay = date.getDate();

  const targetYear = currentYear + years;
  const daysInTargetMonth = new Date(targetYear, currentMonth + 1, 0).getDate();
  const targetDay = Math.min(currentDay, daysInTargetMonth);

  return new Date(targetYear, currentMonth, targetDay);
}

/**
 * Procedural Legal Deadline Calculator strictly matching Egyptian procedural law.
 * Friday is excluded as a weekly holiday (Article 18).
 * Saturday is a working day in Egyptian courts and must NEVER be excluded.
 * Official Egyptian holidays are excluded.
 * Articles 16 & 17 govern distance rules.
 */
export function calculateLegalDeadline(
  announcementDateStr: string,
  rule: LegalRule,
  distanceTypeOrDays: DistanceType | number = 'NONE',
  additionalDistanceDaysParam: number = 0,
  holidays: Holiday[] = []
): DeadlineResult {
  // Support both (rule, distanceType, days, holidays) and legacy (rule, days, holidays) signatures
  let distanceType: DistanceType = 'NONE';
  let additionalDistanceDays = 0;

  if (typeof distanceTypeOrDays === 'number') {
    additionalDistanceDays = distanceTypeOrDays;
    distanceType = additionalDistanceDays <= 0 ? 'NONE' : additionalDistanceDays <= 4 ? 'LOCAL' : 'ABROAD';
  } else {
    distanceType = distanceTypeOrDays;
    additionalDistanceDays = additionalDistanceDaysParam;
  }

  // Statutory distance validation
  if (distanceType === 'LOCAL') {
    additionalDistanceDays = Math.min(4, Math.max(0, additionalDistanceDays));
  } else if (distanceType === 'ABROAD') {
    additionalDistanceDays = Math.min(90, Math.max(0, additionalDistanceDays));
  } else {
    additionalDistanceDays = 0;
  }

  const holidayMap = new Map<string, string>();
  holidays.forEach(h => holidayMap.set(h.holidayDate, h.name));

  const announcementDate = parseIsoDate(announcementDateStr);
  const totalDistanceDays = rule.distanceDays + additionalDistanceDays;

  // 1. Determine Start Counting Date (Article 15 Procedures Law)
  const startDate = new Date(announcementDate);
  if (rule.startRule === 'NEXT_DAY') {
    startDate.setDate(startDate.getDate() + 1);
  }

  // 2. Compute Base End Date according to Article 15 Procedures Law
  let baseEndDate = new Date(startDate);
  if (rule.unit === 'DAYS') {
    const effectiveDays = Math.max(0, rule.duration - 1);
    baseEndDate.setDate(startDate.getDate() + effectiveDays);
  } else if (rule.unit === 'MONTHS') {
    const refDate = rule.startRule === 'NEXT_DAY' ? announcementDate : startDate;
    baseEndDate = addMonthsClamped(refDate, rule.duration);
  } else if (rule.unit === 'YEARS') {
    const refDate = rule.startRule === 'NEXT_DAY' ? announcementDate : startDate;
    baseEndDate = addYearsClamped(refDate, rule.duration);
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
      // Saturday (dayOfWeek === 6) is a valid court working day in Egypt!
      break;
    }
  }

  const finalDeadline = new Date(currentDate);

  // 5. Build Structured Milestones
  const triggerTitle = rule.triggerEventLabel || 'الواقعة المنشئة للميعاد';
  const milestones: any[] = [
    {
      type: 'TRIGGER_EVENT',
      date: toIsoDate(announcementDate),
      title: triggerTitle,
      description: `${triggerTitle} (${formatDisplayDate(announcementDate)}) (${getArabicDayName(announcementDate)}).`,
      legalBasis: rule.lawArticle || rule.lawName || 'قانون المرافعات المدنية والتجارية'
    },
    {
      type: 'COUNTING_START',
      date: toIsoDate(startDate),
      title: 'بدء سريان الميعاد الإجرائي',
      description: rule.startRule === 'NEXT_DAY'
        ? `يبدأ احتساب الميعاد من اليوم التالي للواقعة (${formatDisplayDate(startDate)}) ولا يُحسب يوم الواقعة نفسه إعمالاً للمادة 15 مرافعات.`
        : `يبدأ احتساب الميعاد من نفس يوم الواقعة (${formatDisplayDate(startDate)}) وفقاً لنص القاعدة الإجرائية.`,
      legalBasis: 'المادة 15 من قانون المرافعات'
    },
    {
      type: 'BASE_DEADLINE',
      date: toIsoDate(baseEndDate),
      title: 'نهاية المدة القانونية الأصلية',
      description: `انقضاء مدة الميعاد الأصلية (${rule.duration} ${rule.unit === 'DAYS' ? 'يوم' : rule.unit === 'MONTHS' ? 'شهر' : 'سنة'}) في (${formatDisplayDate(baseEndDate)}) (${getArabicDayName(baseEndDate)}).`,
      legalBasis: rule.lawArticle || rule.article
    }
  ];

  if (totalDistanceDays > 0 && distanceType !== 'NONE') {
    const basisText = distanceType === 'ABROAD'
      ? 'المادة 17 من قانون المرافعات (ميعاد المسافة لمن موطنه بالخارج)'
      : 'المادة 16 من قانون المرافعات (ميعاد المسافة المحلي: يوم لكل 50 كم بحد أقصى 4 أيام)';
    milestones.push({
      type: 'DISTANCE_EXTENSION',
      date: toIsoDate(provisionalDate),
      title: 'إضافة ميعاد مسافة الطريق',
      description: `إضافة ${totalDistanceDays} أيام مسافة ليمتد الميعاد المبدئي إلى (${formatDisplayDate(provisionalDate)}).`,
      legalBasis: basisText
    });
  }

  excludedDays.forEach(ex => {
    milestones.push({
      type: 'HOLIDAY_EXTENSION',
      date: ex.date,
      title: 'امتداد الميعاد لمصادفة عطلة',
      description: `مصادفة تاريخ (${ex.reason.split('—')[0].trim()}) عطلة، ويمتد الميعاد إعمالاً للمادة 18 مرافعات إلى أول يوم عمل.`,
      legalBasis: 'المادة 18 من قانون المرافعات (امتداد العطلات)',
      isExcludedDay: true
    });
  });

  milestones.push({
    type: 'FINAL_DEADLINE',
    date: toIsoDate(finalDeadline),
    title: 'الميعاد النهائي واجب الالتزام به',
    description: `آخر موعد لإيداع صحيفة الطعن أو اتخاذ الإجراء بقلم الكتاب (${formatDisplayDate(finalDeadline)}) (${getArabicDayName(finalDeadline)})${
      finalDeadline.getDay() === 6 ? ' [يوم السبت يوم عمل رسمي بالمحاكم المصرية].' : ' [يوم عمل رسمي].'
    }`,
    legalBasis: 'المادتان 15 و 18 من قانون المرافعات'
  });

  // 6. Generate Comprehensive Arabic Explanation
  const distanceDisplayName = distanceType === 'ABROAD' ? 'موطن بالخارج (المادة 17 مرافعات)' : distanceType === 'LOCAL' ? 'مسافة محلية (المادة 16 مرافعات)' : 'بدون ميعاد مسافة';
  const explanationLines: string[] = [
    `١. ${triggerTitle}: ${formatDisplayDate(announcementDate)} (${getArabicDayName(announcementDate)})`,
    `٢. تاريخ بدء احتساب الميعاد: ${formatDisplayDate(startDate)} (${getArabicDayName(startDate)})${
      rule.startRule === 'NEXT_DAY'
        ? ' — تطبيقاً للمادة 15 مرافعات: يبدأ احتساب الميعاد من اليوم التالي ولا يُحسب يوم الواقعة نفسه.'
        : ' — يبدأ احتساب الميعاد من نفس يوم الواقعة وفقاً لنص القاعدة الإجرائية.'
    }`,
    `٣. مدة الميعاد القانوني: ${rule.duration} ${
      rule.unit === 'DAYS' ? 'أيام' : rule.unit === 'MONTHS' ? 'أشهر' : rule.unit === 'YEARS' ? 'سنوات' : 'ساعات'
    }`,
    `٤. وحدة الاحتساب: ${
      rule.unit === 'DAYS' ? 'أيام' : rule.unit === 'MONTHS' ? 'أشهر' : rule.unit === 'YEARS' ? 'سنوات' : 'ساعات'
    }`,
    `٥. ميعاد المسافة: ${distanceDisplayName} — إجمالي: ${totalDistanceDays} يوم`,
    `٦. الميعاد المبدئي (قبل استبعاد العطلات): ${formatDisplayDate(provisionalDate)} (${getArabicDayName(provisionalDate)})`,
    excludedDays.length > 0
      ? `٧. الأيام المستبعدة والامتداد (المادة 18 مرافعات):\n${excludedDays
          .map((ex, i) => `   • [${i + 1}] ${ex.reason}`)
          .join('\n')}`
      : '٧. الأيام المستبعدة: لا يوجد أيام مستبعدة (صادف الميعاد المبدئي يوم عمل رسمي مباشرة).',
    `٨. الميعاد النهائي واجب الالتزام به: ${formatDisplayDate(finalDeadline)} (${getArabicDayName(finalDeadline)})${
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
    lawArticle: rule.lawArticle || rule.article || '',
    notes: rule.notes,
    milestones,
    distanceType
  };
}
