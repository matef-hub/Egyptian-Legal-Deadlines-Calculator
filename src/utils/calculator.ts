import {
  CalculateDeadlineParams,
  CalculationMilestone,
  DeadlineResult,
  DistanceType,
  DurationUnit,
  ExcludedDay,
  Holiday,
  LegalRule,
  MilestoneType
} from '../types';

export const ARABIC_DAYS = ['الأحد', 'الاثنين', 'الثلاثاء', 'الأربعاء', 'الخميس', 'الجمعة', 'السبت'];

export const VERIFIED_HOLIDAY_YEARS = [2024, 2025, 2026, 2027, 2028];

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

/**
 * Parses and strictly validates an ISO date string (YYYY-MM-DD).
 * Rejects invalid format or non-existent calendar dates (e.g. Feb 30).
 */
export function parseIsoDate(iso: string): Date {
  if (!/^\d{4}-\d{2}-\d{2}$/.test(iso)) {
    throw new Error(`صيغة التاريخ غير صالحة (${iso})، يجب أن تكون بتنسيق YYYY-MM-DD.`);
  }
  const [year, month, day] = iso.split('-').map(Number);
  const date = new Date(year, month - 1, day);
  if (date.getFullYear() !== year || date.getMonth() !== month - 1 || date.getDate() !== day) {
    throw new Error(`التاريخ المدخل غير موجود في التقويم الميلادي (${iso}).`);
  }
  return date;
}

/**
 * Computes domestic distance days under Article 16 from distance in kilometers:
 * 1 day per 50 km; fraction exceeding 30 km adds 1 day; maximum 4 days.
 */
export function calculateLocalDistanceDays(distanceKm: number): number {
  if (distanceKm <= 0) return 0;
  const full50s = Math.floor(distanceKm / 50);
  const remainder = distanceKm % 50;
  const extraDay = remainder > 30 ? 1 : 0;
  return Math.min(4, Math.max(0, full50s + extraDay));
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
 * Procedural Legal Deadline Calculator strictly matching Egyptian procedural law:
 * - Articles 15, 16, 17, and 18 of Law No. 13 of 1968.
 * - Friday is an official weekly holiday and must be excluded.
 * - Saturday is a working day in Egyptian courts and must NEVER be excluded.
 * - Official Egyptian holidays are excluded.
 * - Transitional provisions and effective dates strictly enforced.
 */
export function calculateLegalDeadline(
  firstArg: string | CalculateDeadlineParams,
  ruleArg?: LegalRule,
  distanceTypeArg?: DistanceType,
  additionalDistanceDaysArg?: number,
  holidaysArg?: Holiday[],
  judicialOrderDetailsArg?: string
): DeadlineResult {
  let announcementDateStr: string;
  let rule: LegalRule;
  let distanceType: DistanceType = 'NONE';
  let additionalDistanceDays = 0;
  let holidays: Holiday[] = [];
  let judicialOrderDetails: string | undefined;

  if (typeof firstArg === 'object' && 'announcementDate' in firstArg) {
    announcementDateStr = firstArg.announcementDate;
    rule = firstArg.rule;
    distanceType = firstArg.distanceType || 'NONE';
    additionalDistanceDays = firstArg.additionalDistanceDays || 0;
    holidays = firstArg.holidays || [];
    judicialOrderDetails = firstArg.judicialOrderDetails;
  } else {
    announcementDateStr = firstArg as string;
    if (!ruleArg) {
      throw new Error('القاعدة القانونية مطلوبة لاحتساب الميعاد.');
    }
    rule = ruleArg;
    distanceType = distanceTypeArg || 'NONE';
    additionalDistanceDays = additionalDistanceDaysArg || 0;
    holidays = holidaysArg || [];
    judicialOrderDetails = judicialOrderDetailsArg;
  }

  // 1. Validate date input
  const announcementDate = parseIsoDate(announcementDateStr);

  // 2. Effective dates & transitional provisions validation
  if (rule.effectiveFrom && announcementDateStr < rule.effectiveFrom) {
    throw new Error(
      `القاعدة القانونية المختارة (${rule.actionName}) غير سارية في تاريخ الواقعة (${announcementDateStr})؛ حيث يبدأ سريانها في ${rule.effectiveFrom}.`
    );
  }
  if (rule.effectiveTo && announcementDateStr > rule.effectiveTo) {
    throw new Error(
      `القاعدة القانونية المختارة (${rule.actionName}) منتهية السريان في تاريخ الواقعة (${announcementDateStr})؛ حيث انتهى سريانها في ${rule.effectiveTo}.`
    );
  }

  // 3. Distance policy check
  if (rule.distancePolicy === 'NOT_APPLICABLE') {
    if (distanceType !== 'NONE' && distanceType !== 'ABROAD_SERVED_IN_EGYPT') {
      throw new Error(
        `الإجراء القانوني (${rule.actionName}) لا يقبل تطبيق مواعيد المسافة عليه قانوناً.`
      );
    }
    if (additionalDistanceDays > 0) {
      throw new Error(
        `لا يجوز إضافة أيام مسافة لهذا الإجراء (${rule.actionName}) لأن ميعاد المسافة غير منطبق عليه قانوناً.`
      );
    }
  }

  // 4. Statutory distance validation (Articles 16 and 17)
  let validatedDistanceDays = 0;
  switch (distanceType) {
    case 'NONE':
      if (additionalDistanceDays !== 0) {
        throw new Error('لا يجوز إضافة أيام مسافة مع خيار بدون مسافة.');
      }
      validatedDistanceDays = 0;
      break;

    case 'LOCAL':
      if (additionalDistanceDays < 0 || additionalDistanceDays > 4) {
        throw new Error(
          'ميعاد المسافة المحلي لا يجوز أن يجاوز 4 أيام إعمالاً للمادة 16 من قانون المرافعات (يوم لكل 50 كم وما زاد على 30 كم يعتبر 50 كم بحد أقصى 4 أيام).'
        );
      }
      validatedDistanceDays = additionalDistanceDays;
      break;

    case 'BORDER_AREA':
      if (additionalDistanceDays !== 0 && additionalDistanceDays !== 15) {
        throw new Error(
          'ميعاد المسافة للمناطق الحدودية النائية محدد بنص القانون بـ 15 يوماً وفقاً للمادة 16 فقرة 2 مرافعات.'
        );
      }
      validatedDistanceDays = 15;
      break;

    case 'ABROAD':
      if (additionalDistanceDays !== 0 && additionalDistanceDays !== 60) {
        throw new Error(
          'ميعاد المسافة العادي لمن موطنه بالخارج محدد بنص المادة 17 مرافعات بـ 60 يوماً ولا يجوز زيادته إلى 90 يوماً.'
        );
      }
      validatedDistanceDays = 60;
      break;

    case 'ABROAD_JUDICIAL_REDUCTION':
      if (additionalDistanceDays < 1 || additionalDistanceDays > 59) {
        throw new Error(
          'ميعاد المسافة المنقوص بأمر قضائي لمن موطنه بالخارج يجب أن يكون بين 1 و 59 يوماً وفقاً للمادة 17 فقرة 2 مرافعات.'
        );
      }
      if (!judicialOrderDetails || judicialOrderDetails.trim() === '') {
        throw new Error(
          'يلزم بيان سند ورقم الأمر القضائي الصادر بإنقاص ميعاد المسافة وفقاً للمادة 17 فقرة 2 مرافعات.'
        );
      }
      validatedDistanceDays = additionalDistanceDays;
      break;

    case 'ABROAD_SERVED_IN_EGYPT':
      if (additionalDistanceDays !== 0) {
        throw new Error(
          'من أُعلن لشخصه أثناء وجوده في مصر لا يسري عليه ميعاد مسافة وفقاً للمادة 17 فقرة 3 مرافعات.'
        );
      }
      validatedDistanceDays = 0;
      break;

    default:
      throw new Error(`نوع ميعاد المسافة غير مدعوم (${distanceType}).`);
  }

  const warnings: string[] = [];
  const holidayMap = new Map<string, string>();
  holidays.forEach((h) => holidayMap.set(h.holidayDate, h.name));

  const announcementYear = announcementDate.getFullYear();
  if (!VERIFIED_HOLIDAY_YEARS.includes(announcementYear)) {
    warnings.push(
      `تنبيه: سنة الواقعة (${announcementYear}) خارج نطاق قاعدة بيانات العطلات الرسمية المدققة؛ يرجى التحقق يدوياً من المناسبات الرسمية.`
    );
  }

  const totalDistanceDays = rule.distanceDays + validatedDistanceDays;

  // 5. Determine Start Counting Date (Article 15 Procedures Law)
  const startDate = new Date(announcementDate);
  if (rule.startRule === 'NEXT_DAY') {
    startDate.setDate(startDate.getDate() + 1);
  }

  // 6. Compute Base End Date
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

  // 7. Add Distance Days
  const provisionalDate = new Date(baseEndDate);
  provisionalDate.setDate(baseEndDate.getDate() + totalDistanceDays);

  // 8. Final Deadline Adjustment Loop (Article 18 Procedures Law)
  const currentDate = new Date(provisionalDate);
  const excludedDays: ExcludedDay[] = [];

  while (true) {
    const dayOfWeek = currentDate.getDay(); // 0 = Sunday, 5 = Friday, 6 = Saturday
    const dateIso = toIsoDate(currentDate);
    const holidayName = holidayMap.get(dateIso);

    if (dayOfWeek === 5) {
      // Friday is weekly holiday
      excludedDays.push({
        date: dateIso,
        reason: `${formatDisplayDate(currentDate)} — الجمعة — عطلة أسبوعية رسمية (المادة 18 مرافعات)`
      });
      currentDate.setDate(currentDate.getDate() + 1);
    } else if (holidayName) {
      // Official Holiday
      excludedDays.push({
        date: dateIso,
        reason: `${formatDisplayDate(currentDate)} — عطلة رسمية معتمدة — ${holidayName}`
      });
      currentDate.setDate(currentDate.getDate() + 1);
    } else {
      // Saturday (dayOfWeek === 6) is a valid court working day! Loop breaks.
      break;
    }
  }

  const finalDeadline = new Date(currentDate);
  const finalDeadlineYear = finalDeadline.getFullYear();
  if (
    !VERIFIED_HOLIDAY_YEARS.includes(finalDeadlineYear) &&
    finalDeadlineYear !== announcementYear
  ) {
    warnings.push(
      `تنبيه: سنة الميعاد النهائي (${finalDeadlineYear}) خارج نطاق قاعدة بيانات العطلات الرسمية المدققة.`
    );
  }

  // 9. Structured Milestones
  const milestones: CalculationMilestone[] = [];

  const triggerTitle = rule.triggerEventLabel || 'الواقعة المنشئة للميعاد';
  milestones.push({
    type: 'TRIGGER_EVENT',
    date: toIsoDate(announcementDate),
    title: triggerTitle,
    description: `${triggerTitle} (${formatDisplayDateWithDay(announcementDate)}).`,
    legalBasis: rule.lawArticle || rule.lawName
  });

  milestones.push({
    type: 'COUNTING_START',
    date: toIsoDate(startDate),
    title: 'بدء سريان الميعاد الإجرائي',
    description:
      rule.startRule === 'NEXT_DAY'
        ? `يبدأ احتساب الميعاد من اليوم التالي (${formatDisplayDateWithDay(startDate)}) ولا يُحسب يوم الواقعة نفسه إعمالاً للمادة 15 مرافعات.`
        : `يبدأ احتساب الميعاد من نفس يوم الواقعة (${formatDisplayDateWithDay(startDate)}) وفقاً للقاعدة المقررة.`,
    legalBasis: 'المادة 15 من قانون المرافعات المدنية والتجارية'
  });

  milestones.push({
    type: 'BASE_DEADLINE',
    date: toIsoDate(baseEndDate),
    title: 'نهاية المدة القانونية الأصلية',
    description: `انقضاء مدة الميعاد الأصلية (${rule.duration} ${
      rule.unit === 'DAYS' ? 'يوم' : rule.unit === 'MONTHS' ? 'شهر' : rule.unit === 'YEARS' ? 'سنة' : 'ساعة'
    }) في (${formatDisplayDateWithDay(baseEndDate)}).`,
    legalBasis: rule.lawArticle || rule.article
  });

  if (totalDistanceDays > 0 && distanceType !== 'NONE') {
    const distDescription =
      distanceType === 'LOCAL'
        ? `إضافة ${totalDistanceDays} أيام مسافة محلية (المادة 16 مرافعات).`
        : distanceType === 'BORDER_AREA'
        ? 'إضافة 15 يوماً مسافة للمناطق الحدودية النائية (المادة 16 فقرة 2 مرافعات).'
        : distanceType === 'ABROAD'
        ? 'إضافة 60 يوماً ميعاد مسافة لمن موطنه بالخارج (المادة 17 مرافعات).'
        : distanceType === 'ABROAD_JUDICIAL_REDUCTION'
        ? `إضافة ${totalDistanceDays} يوماً مسافة منقوصة بأمر قضائي: ${judicialOrderDetails} (المادة 17 فقرة 2 مرافعات).`
        : `إضافة ${totalDistanceDays} أيام مسافة طريق.`;

    milestones.push({
      type: 'DISTANCE_EXTENSION',
      date: toIsoDate(provisionalDate),
      title: 'إضافة ميعاد مسافة الطريق',
      description: `${distDescription} ليمتد الميعاد المبدئي إلى (${formatDisplayDateWithDay(provisionalDate)}).`,
      legalBasis:
        distanceType === 'LOCAL'
          ? 'المادة 16 من قانون المرافعات'
          : distanceType === 'BORDER_AREA'
          ? 'المادة 16 فقرة 2 من قانون المرافعات'
          : 'المادة 17 من قانون المرافعات'
    });
  }

  excludedDays.forEach((excluded, idx) => {
    milestones.push({
      type: 'HOLIDAY_EXTENSION',
      date: excluded.date,
      title: `امتداد بسبب عطلة [${idx + 1}]`,
      description: `${excluded.reason}، ويمتد الميعاد لليوم التالي وفقاً للمادة 18 مرافعات.`,
      legalBasis: 'المادة 18 من قانون المرافعات المدنية والتجارية',
      isExcludedDay: true
    });
  });

  milestones.push({
    type: 'FINAL_DEADLINE',
    date: toIsoDate(finalDeadline),
    title: 'الميعاد النهائي واجب الالتزام به',
    description:
      `آخر يوم عمل لإيداع صحيفة الطعن أو اتخاذ الإجراء بقلم الكتاب (${formatDisplayDateWithDay(finalDeadline)})` +
      (finalDeadline.getDay() === 6
        ? ' [يوم السبت يوم عمل رسمي كامل بالمحاكم المصرية].'
        : ' [يوم عمل رسمي].'),
    legalBasis: 'المادتان 15 و 18 من قانون المرافعات'
  });

  // 10. Arabic explanation
  const unitArabic =
    rule.unit === 'DAYS' ? 'يوم' : rule.unit === 'MONTHS' ? 'شهر' : rule.unit === 'YEARS' ? 'سنة' : 'ساعة';

  let explanation = `١. ${triggerTitle}: ${formatDisplayDateWithDay(announcementDate)}\n`;
  explanation += `٢. تاريخ بدء احتساب الميعاد: ${formatDisplayDateWithDay(startDate)} — `;
  explanation +=
    rule.startRule === 'NEXT_DAY'
      ? 'تطبيقاً لنص المادة 15 مرافعات: يبدأ احتساب الميعاد من اليوم التالي ولا يُحسب يوم الواقعة نفسه.\n'
      : 'يبدأ احتساب الميعاد من نفس يوم الواقعة وفقاً لنص القاعدة الإجرائية.\n';
  explanation += `٣. مدة الميعاد القانوني: ${rule.duration} ${unitArabic}\n`;
  explanation += `٤. وحدة الاحتساب: ${unitArabic}\n`;

  explanation += '٥. ميعاد المسافة: ';
  if (distanceType === 'NONE' && totalDistanceDays === 0) {
    explanation += 'لا يوجد ميعاد مسافة (محل النزاع بنفس الدائرة القضائية).\n';
  } else if (distanceType === 'ABROAD_SERVED_IN_EGYPT') {
    explanation += 'لا يسري ميعاد المسافة على من أعلن لشخصه أثناء وجوده في مصر (المادة 17 فقرة 3 مرافعات).\n';
  } else {
    explanation += `إجمالي ${totalDistanceDays} يوم (${
      distanceType === 'LOCAL'
        ? 'المادة 16 مرافعات: يوم لكل 50 كم بحد أقصى 4 أيام'
        : distanceType === 'BORDER_AREA'
        ? 'المادة 16 فقرة 2 مرافعات: 15 يوماً للمناطق الحدودية النائية'
        : distanceType === 'ABROAD_JUDICIAL_REDUCTION'
        ? `المادة 17 فقرة 2 مرافعات: إنقاص بأمر قضائي (${judicialOrderDetails})`
        : 'المادة 17 مرافعات: 60 يوماً لمن موطنه بالخارج'
    })\n`;
  }

  explanation += `٦. الميعاد المبدئي (قبل تطبيق امتداد العطلات): ${formatDisplayDateWithDay(provisionalDate)}\n`;

  if (excludedDays.length > 0) {
    explanation += '٧. الأيام المستبعدة والامتداد (المادة 18 مرافعات):\n';
    excludedDays.forEach((ex, i) => {
      explanation += `   • [${i + 1}] ${ex.reason}\n`;
    });
  } else {
    explanation += '٧. الأيام المستبعدة: لا يوجد أيام مستبعدة (صادف الميعاد المبدئي يوم عمل رسمي مباشرة).\n';
  }

  explanation += `٨. الميعاد النهائي واجب الالتزام به: ${formatDisplayDateWithDay(finalDeadline)}`;
  if (finalDeadline.getDay() === 6) {
    explanation += ' [ملاحظة قانونية: يوم السبت يُعد يوم عمل كامل في المحاكم المصرية وفقاً للقانون].';
  }

  return {
    startDate: toIsoDate(startDate),
    provisionalDate: toIsoDate(provisionalDate),
    duration: rule.duration,
    unit: rule.unit,
    ruleDistanceDays: rule.distanceDays,
    additionalDistanceDays: validatedDistanceDays,
    totalDistanceDays,
    excludedDays,
    finalDeadline: toIsoDate(finalDeadline),
    explanation,
    lawArticle: rule.lawArticle || rule.article || '',
    notes: rule.notes || '',
    milestones,
    distanceType,
    judicialOrderDetails,
    warnings: warnings.length > 0 ? warnings : undefined
  };
}
