import { describe, it } from 'node:test';
import assert from 'node:assert/strict';
import {
  calculateLegalDeadline,
  calculateLocalDistanceDays,
  parseIsoDate,
  toIsoDate,
  addMonthsClamped,
  addYearsClamped,
  VERIFIED_HOLIDAY_YEARS
} from './calculator';
import { Holiday, LegalRule } from '../types';

describe('Egyptian Legal Deadline Calculator — Web Unit Tests', () => {
  const sampleHoliday2026: Holiday[] = [
    { id: 1, holidayDate: '2026-10-06', name: 'عيد القوات المسلحة' },
    { id: 2, holidayDate: '2026-10-03', name: 'عطلة رسمية استثنائية' }
  ];

  // =========================================================================
  // 1. Friday Exclusion (Article 18 Procedures Law)
  // =========================================================================
  it('proves Friday is excluded and extends deadline to Saturday (working day)', () => {
    // Thursday Oct 1, 2026 + 1 day with SAME_DAY -> Friday Oct 2 (provisional)
    // Friday excluded -> Final deadline is Saturday Oct 3
    const rule: LegalRule = {
      id: 1,
      actionName: 'اختبار ميعاد يصادف الجمعة',
      duration: 2,
      unit: 'DAYS',
      distanceDays: 0,
      startRule: 'SAME_DAY',
      lawArticle: 'المادة 18 مرافعات',
      notes: ''
    };

    const result = calculateLegalDeadline({
      announcementDate: '2026-10-01',
      rule,
      distanceType: 'NONE',
      additionalDistanceDays: 0,
      holidays: []
    });

    assert.equal(result.provisionalDate, '2026-10-02'); // Friday
    assert.equal(result.finalDeadline, '2026-10-03'); // Saturday
    assert.equal(result.excludedDays.length, 1);
    assert.match(result.excludedDays[0].reason, /الجمعة/);
  });

  // =========================================================================
  // 2. Saturday Working Day Rule (Article 18 Procedures Law)
  // =========================================================================
  it('proves Saturday is a judicial working day in Egypt and is NEVER excluded', () => {
    // Thursday Oct 1, 2026 + 2 days with SAME_DAY -> Saturday Oct 3 (provisional)
    // Saturday must remain Saturday without rolling over to Sunday
    const rule: LegalRule = {
      id: 2,
      actionName: 'اختبار ميعاد يصادف السبت',
      duration: 3,
      unit: 'DAYS',
      distanceDays: 0,
      startRule: 'SAME_DAY',
      lawArticle: 'المادة 18 مرافعات',
      notes: ''
    };

    const result = calculateLegalDeadline({
      announcementDate: '2026-10-01',
      rule,
      distanceType: 'NONE',
      additionalDistanceDays: 0,
      holidays: []
    });

    assert.equal(result.provisionalDate, '2026-10-03'); // Saturday
    assert.equal(result.finalDeadline, '2026-10-03'); // Saturday
    assert.equal(result.excludedDays.length, 0);
  });

  // =========================================================================
  // 3. Official Holiday Extension
  // =========================================================================
  it('proves an official holiday on the provisional deadline rolls over to the next working day', () => {
    // Oct 6 is Armed Forces Day (Tuesday)
    const rule: LegalRule = {
      id: 3,
      actionName: 'اختبار عطلة 6 أكتوبر',
      duration: 1,
      unit: 'DAYS',
      distanceDays: 0,
      startRule: 'SAME_DAY',
      lawArticle: 'المادة 18 مرافعات',
      notes: ''
    };

    const result = calculateLegalDeadline({
      announcementDate: '2026-10-06',
      rule,
      distanceType: 'NONE',
      additionalDistanceDays: 0,
      holidays: sampleHoliday2026
    });

    assert.equal(result.provisionalDate, '2026-10-06'); // Tuesday (Holiday)
    assert.equal(result.finalDeadline, '2026-10-07'); // Wednesday (Working day)
    assert.equal(result.excludedDays.length, 1);
    assert.match(result.excludedDays[0].reason, /عيد القوات المسلحة/);
  });

  // =========================================================================
  // 4. Consecutive Exclusions Roll-over
  // =========================================================================
  it('proves consecutive exclusions (Friday + official holiday on Saturday) roll forward to Sunday', () => {
    // Oct 2, 2026 is Friday.
    // Oct 3, 2026 is Saturday, set as official holiday in sampleHoliday2026.
    // Result must roll from Friday -> Saturday (holiday) -> Sunday Oct 4!
    const rule: LegalRule = {
      id: 4,
      actionName: 'اختبار عطلات متتالية',
      duration: 1,
      unit: 'DAYS',
      distanceDays: 0,
      startRule: 'SAME_DAY',
      lawArticle: 'المادة 18 مرافعات',
      notes: ''
    };

    const result = calculateLegalDeadline({
      announcementDate: '2026-10-02', // Friday
      rule,
      distanceType: 'NONE',
      additionalDistanceDays: 0,
      holidays: sampleHoliday2026
    });

    assert.equal(result.provisionalDate, '2026-10-02');
    assert.equal(result.finalDeadline, '2026-10-04'); // Sunday
    assert.equal(result.excludedDays.length, 2);
    assert.match(result.excludedDays[0].reason, /الجمعة/);
    assert.match(result.excludedDays[1].reason, /عطلة رسمية استثنائية/);
  });

  // =========================================================================
  // 5. Start Counting Convention (Article 15 Procedures Law)
  // =========================================================================
  it('proves NEXT_DAY start counting convention does not count the day of the event', () => {
    const rule: LegalRule = {
      id: 5,
      actionName: 'الطعن بالاستئناف',
      duration: 10,
      unit: 'DAYS',
      distanceDays: 0,
      startRule: 'NEXT_DAY',
      lawArticle: 'المادة 15 مرافعات',
      notes: ''
    };

    const result = calculateLegalDeadline({
      announcementDate: '2026-05-10', // Sunday
      rule,
      distanceType: 'NONE',
      additionalDistanceDays: 0,
      holidays: []
    });

    assert.equal(result.startDate, '2026-05-11'); // Monday (Next day)
    assert.equal(result.provisionalDate, '2026-05-20');
    assert.equal(result.finalDeadline, '2026-05-20');
  });

  // =========================================================================
  // 6. Month Clamping & Leap Year Arithmetic
  // =========================================================================
  it('proves month addition clamps to end of shorter months (Jan 31 + 1 month = Feb 28)', () => {
    const rule: LegalRule = {
      id: 6,
      actionName: 'التماس إعادة النظر',
      duration: 1,
      unit: 'MONTHS',
      distanceDays: 0,
      startRule: 'SAME_DAY',
      lawArticle: 'المادة 15 فقرة 4 مرافعات',
      notes: ''
    };

    const result = calculateLegalDeadline({
      announcementDate: '2026-01-31',
      rule,
      distanceType: 'NONE',
      additionalDistanceDays: 0,
      holidays: []
    });

    assert.equal(result.provisionalDate, '2026-02-28');
    assert.equal(result.finalDeadline, '2026-02-28'); // Saturday is a working day
  });

  it('proves leap year arithmetic preserves Feb 29 when adding 1 month (Feb 29 2024 -> Mar 29 2024)', () => {
    const d = addMonthsClamped(new Date(2024, 1, 29), 1);
    assert.equal(toIsoDate(d), '2024-03-29');
  });

  it('proves year addition clamps Feb 29 to Feb 28 on non-leap years', () => {
    const d = addYearsClamped(new Date(2024, 1, 29), 1);
    assert.equal(toIsoDate(d), '2025-02-28');
  });

  // =========================================================================
  // 7. Article 16 Domestic Distance (Kilometer Formula & Validation)
  // =========================================================================
  it('proves Article 16 domestic distance kilometer formula', () => {
    assert.equal(calculateLocalDistanceDays(0), 0);
    assert.equal(calculateLocalDistanceDays(30), 0); // Not exceeding 30 km
    assert.equal(calculateLocalDistanceDays(31), 1); // Exceeding 30 km adds 1 day
    assert.equal(calculateLocalDistanceDays(50), 1);
    assert.equal(calculateLocalDistanceDays(80), 1); // 50 + 30 = 1 day
    assert.equal(calculateLocalDistanceDays(81), 2); // 50 + 31 = 2 days
    assert.equal(calculateLocalDistanceDays(135), 3); // 100 + 35 = 3 days
    assert.equal(calculateLocalDistanceDays(200), 4);
    assert.equal(calculateLocalDistanceDays(600), 4); // Capped at 4 days
  });

  it('proves domestic distance adds up to 4 days and creates proper milestone', () => {
    const rule: LegalRule = {
      id: 7,
      actionName: 'استئناف مدني مع مسافة محلية',
      duration: 10,
      unit: 'DAYS',
      distanceDays: 0,
      startRule: 'SAME_DAY',
      lawArticle: 'المادة 16 مرافعات',
      notes: '',
      distancePolicy: 'APPLICABLE'
    };

    const result = calculateLegalDeadline({
      announcementDate: '2026-04-01', // Wednesday
      rule,
      distanceType: 'LOCAL',
      additionalDistanceDays: 4,
      holidays: []
    });

    assert.equal(result.additionalDistanceDays, 4);
    assert.equal(result.distanceType, 'LOCAL');
    assert.equal(result.provisionalDate, '2026-04-14');
    assert.equal(result.finalDeadline, '2026-04-14');
    assert.ok(result.milestones?.some((m) => m.legalBasis?.includes('المادة 16')));
  });

  it('rejects domestic distance exceeding 4 days and throws explicit Error', () => {
    const rule: LegalRule = {
      id: 8,
      actionName: 'اختبار تجاوز مسافة محلية',
      duration: 10,
      unit: 'DAYS',
      distanceDays: 0,
      startRule: 'SAME_DAY',
      lawArticle: 'المادة 16 مرافعات',
      notes: ''
    };

    assert.throws(
      () =>
        calculateLegalDeadline({
          announcementDate: '2026-04-01',
          rule,
          distanceType: 'LOCAL',
          additionalDistanceDays: 5 // Illegal!
        }),
      /4 أيام/
    );
  });

  // =========================================================================
  // 8. Article 16 Border Area (15 Days Statutory)
  // =========================================================================
  it('proves Article 16 Para 2 border area applies statutory 15 days distance', () => {
    const rule: LegalRule = {
      id: 9,
      actionName: 'ميعاد مسافة مناطق حدودية',
      duration: 10,
      unit: 'DAYS',
      distanceDays: 0,
      startRule: 'NEXT_DAY',
      lawArticle: 'المادة 16 فقرة 2 مرافعات',
      notes: ''
    };

    const result = calculateLegalDeadline({
      announcementDate: '2026-05-01',
      rule,
      distanceType: 'BORDER_AREA',
      additionalDistanceDays: 15,
      holidays: []
    });

    assert.equal(result.additionalDistanceDays, 15);
    assert.equal(result.distanceType, 'BORDER_AREA');
    assert.ok(result.milestones?.some((m) => m.description.includes('الحدودية') && m.description.includes('15')));
  });

  // =========================================================================
  // 9. Article 17 Domiciled Abroad (60 Days vs 90 Days Rejection)
  // =========================================================================
  it('proves Article 17 ordinary distance abroad applies 60 days', () => {
    const rule: LegalRule = {
      id: 10,
      actionName: 'ميعاد مسافة الخارج',
      duration: 10,
      unit: 'DAYS',
      distanceDays: 0,
      startRule: 'SAME_DAY',
      lawArticle: 'المادة 17 مرافعات',
      notes: ''
    };

    const result = calculateLegalDeadline({
      announcementDate: '2026-01-01',
      rule,
      distanceType: 'ABROAD',
      additionalDistanceDays: 60,
      holidays: []
    });

    assert.equal(result.additionalDistanceDays, 60);
    assert.equal(result.distanceType, 'ABROAD');
    assert.match(result.explanation, /المادة 17/);
  });

  it('rejects obsolete 90 days abroad distance and throws explicit Error', () => {
    const rule: LegalRule = {
      id: 11,
      actionName: 'اختبار 90 يوماً المرفوضة',
      duration: 10,
      unit: 'DAYS',
      distanceDays: 0,
      startRule: 'SAME_DAY',
      lawArticle: 'المادة 17 مرافعات',
      notes: ''
    };

    assert.throws(
      () =>
        calculateLegalDeadline({
          announcementDate: '2026-01-01',
          rule,
          distanceType: 'ABROAD',
          additionalDistanceDays: 90 // Must be rejected under current law!
        }),
      /60 يوماً/
    );
  });

  // =========================================================================
  // 10. Article 17 Judicial Reduction & Service in Egypt Exceptions
  // =========================================================================
  it('proves Article 17 Para 2 judicial reduction applies ordered days with documentation', () => {
    const rule: LegalRule = {
      id: 12,
      actionName: 'إنقاص ميعاد بأمر وقتي',
      duration: 15,
      unit: 'DAYS',
      distanceDays: 0,
      startRule: 'NEXT_DAY',
      lawArticle: 'المادة 17 فقرة 2 مرافعات',
      notes: ''
    };

    const result = calculateLegalDeadline({
      announcementDate: '2026-02-01',
      rule,
      distanceType: 'ABROAD_JUDICIAL_REDUCTION',
      additionalDistanceDays: 20,
      judicialOrderDetails: 'أمر على عريضة رقم 35 لسنة 2026 جنوب القاهرة',
      holidays: []
    });

    assert.equal(result.additionalDistanceDays, 20);
    assert.equal(result.distanceType, 'ABROAD_JUDICIAL_REDUCTION');
    assert.match(result.explanation, /أمر على عريضة رقم 35/);
  });

  it('throws Error if judicial reduction is selected without order details', () => {
    const rule: LegalRule = {
      id: 13,
      actionName: 'إنقاص ميعاد بدون أمر',
      duration: 15,
      unit: 'DAYS',
      distanceDays: 0,
      startRule: 'NEXT_DAY',
      lawArticle: 'المادة 17 فقرة 2 مرافعات',
      notes: ''
    };

    assert.throws(
      () =>
        calculateLegalDeadline({
          announcementDate: '2026-02-01',
          rule,
          distanceType: 'ABROAD_JUDICIAL_REDUCTION',
          additionalDistanceDays: 20,
          judicialOrderDetails: '' // Missing!
        }),
      /الأمر القضائي/
    );
  });

  it('proves Article 17 Para 3 served in person in Egypt results in 0 distance days', () => {
    const rule: LegalRule = {
      id: 14,
      actionName: 'إعلان أجنبي في مصر لشخصه',
      duration: 30,
      unit: 'DAYS',
      distanceDays: 0,
      startRule: 'NEXT_DAY',
      lawArticle: 'المادة 17 فقرة 3 مرافعات',
      notes: ''
    };

    const result = calculateLegalDeadline({
      announcementDate: '2026-03-01',
      rule,
      distanceType: 'ABROAD_SERVED_IN_EGYPT',
      additionalDistanceDays: 0,
      holidays: []
    });

    assert.equal(result.additionalDistanceDays, 0);
    assert.equal(result.distanceType, 'ABROAD_SERVED_IN_EGYPT');
    assert.match(result.explanation, /المادة 17 فقرة 3 مرافعات/);
  });

  // =========================================================================
  // 11. Rule distancePolicy = NOT_APPLICABLE
  // =========================================================================
  it('rejects distance if rule distancePolicy is NOT_APPLICABLE', () => {
    const rule: LegalRule = {
      id: 15,
      actionName: 'إجراء غير قابل للمسافة',
      duration: 8,
      unit: 'DAYS',
      distanceDays: 0,
      startRule: 'NEXT_DAY',
      lawArticle: 'قانون الرسوم القضائية',
      notes: '',
      distancePolicy: 'NOT_APPLICABLE'
    };

    assert.throws(
      () =>
        calculateLegalDeadline({
          announcementDate: '2026-04-01',
          rule,
          distanceType: 'LOCAL',
          additionalDistanceDays: 2
        }),
      /لا يقبل تطبيق مواعيد المسافة/
    );
  });

  // =========================================================================
  // 12. Transitional Provisions & Effective Dates (Law 174/2025 amended by 160/2026)
  // =========================================================================
  it('throws Error if calculating under Law 174 of 2025 before Oct 1, 2027', () => {
    const futureRule: LegalRule = {
      id: 16,
      actionName: 'استئناف في قانون الإجراءات الجنائية الجديد',
      duration: 15,
      unit: 'DAYS',
      distanceDays: 0,
      startRule: 'NEXT_DAY',
      lawArticle: 'قانون 174 لسنة 2025 المعدل بالقانون 160 لسنة 2026',
      notes: '',
      effectiveFrom: '2027-10-01'
    };

    assert.throws(
      () =>
        calculateLegalDeadline({
          announcementDate: '2026-10-17', // Prior to 2027-10-01
          rule: futureRule,
          distanceType: 'NONE',
          additionalDistanceDays: 0
        }),
      /غير سارية في تاريخ الواقعة/
    );
  });

  it('succeeds calculating under Law 174 of 2025 on or after Oct 1, 2027', () => {
    const newCriminalRule: LegalRule = {
      id: 17,
      actionName: 'استئناف جنائي بموجب القانون 174 لسنة 2025',
      duration: 15,
      unit: 'DAYS',
      distanceDays: 0,
      startRule: 'NEXT_DAY',
      lawArticle: 'المادة 410 من القانون 174 لسنة 2025',
      notes: '',
      effectiveFrom: '2027-10-01',
      effectiveTo: null
    };

    const result = calculateLegalDeadline({
      announcementDate: '2027-10-05',
      rule: newCriminalRule,
      distanceType: 'NONE',
      additionalDistanceDays: 0
    });

    assert.equal(result.startDate, '2027-10-06');
    assert.ok(result.finalDeadline.length === 10);
  });

  it('throws Error if calculating under expired rule past its effectiveTo date', () => {
    const expiredRule: LegalRule = {
      id: 18,
      actionName: 'قاعدة جنائية بقانون ملغي',
      duration: 10,
      unit: 'DAYS',
      distanceDays: 0,
      startRule: 'NEXT_DAY',
      lawArticle: 'قانون 150 لسنة 1950',
      notes: '',
      effectiveFrom: '1951-10-15',
      effectiveTo: '2027-09-30'
    };

    assert.throws(
      () =>
        calculateLegalDeadline({
          announcementDate: '2027-10-05', // After expiry
          rule: expiredRule,
          distanceType: 'NONE',
          additionalDistanceDays: 0
        }),
      /منتهية السريان/
    );
  });

  // =========================================================================
  // 13. Invalid Input Validation (Calendar Errors & Format)
  // =========================================================================
  it('rejects invalid or non-existent calendar dates (e.g. Feb 31 or invalid format)', () => {
    const rule: LegalRule = {
      id: 19,
      actionName: 'اختبار تاريخ غير صالح',
      duration: 10,
      unit: 'DAYS',
      distanceDays: 0,
      startRule: 'SAME_DAY',
      lawArticle: '',
      notes: ''
    };

    assert.throws(() => parseIsoDate('2026-02-31'), /غير موجود/);
    assert.throws(() => parseIsoDate('2026/02/15'), /صيغة التاريخ/);
    assert.throws(() => parseIsoDate('not-a-date'), /صيغة التاريخ/);
  });
});
