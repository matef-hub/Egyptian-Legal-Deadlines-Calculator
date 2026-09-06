export type CalendarEventType = 'FINAL_DEADLINE' | 'PROACTIVE_ALERT';

export interface CalendarEventPayload {
  title: string;
  finalDeadlineDate: string; // 'YYYY-MM-DD'
  lawArticle?: string;
  explanation?: string;
  notes?: string;
  caseNumber?: string;
  clientName?: string;
  location?: string;
  proactiveReminderDays?: number; // e.g. 1, 2, 3, 5, 7, 10
  calendarTarget?: CalendarEventType; // 'FINAL_DEADLINE' | 'PROACTIVE_ALERT'
}

/**
 * Calculates the proactive alert date prior to the final statutory deadline.
 */
export function calculateProactiveAlertDate(finalDeadlineIso: string, daysBefore: number): string {
  if (daysBefore <= 0) return finalDeadlineIso;
  const [year, month, day] = finalDeadlineIso.split('-').map(Number);
  const target = new Date(year, month - 1, day);
  target.setDate(target.getDate() - daysBefore);

  const y = target.getFullYear();
  const m = String(target.getMonth() + 1).padStart(2, '0');
  const d = String(target.getDate()).padStart(2, '0');
  return `${y}-${m}-${d}`;
}

/**
 * Builds a direct URL to create an event in Google Calendar with proactive reminder support.
 * Uses action=TEMPLATE parameters for all-day events.
 */
export function createGoogleCalendarUrl(payload: CalendarEventPayload): string {
  const {
    title,
    finalDeadlineDate,
    lawArticle,
    explanation,
    notes,
    caseNumber,
    clientName,
    location = 'المحكمة المختصة',
    proactiveReminderDays = 0,
    calendarTarget = 'FINAL_DEADLINE'
  } = payload;

  const hasProactiveReminder = proactiveReminderDays > 0;
  const proactiveDateIso = hasProactiveReminder
    ? calculateProactiveAlertDate(finalDeadlineDate, proactiveReminderDays)
    : finalDeadlineDate;

  // Determine which date the calendar event is scheduled for
  const isAlertEvent = calendarTarget === 'PROACTIVE_ALERT';
  const targetDateIso = isAlertEvent ? proactiveDateIso : finalDeadlineDate;

  // Format start date as YYYYMMDD
  const cleanStartDate = targetDateIso.replace(/-/g, '');

  // For all-day events in Google Calendar, end date is the next day (exclusive)
  const [year, month, day] = targetDateIso.split('-').map(Number);
  const nextDay = new Date(year, month - 1, day + 1);
  const nextYear = nextDay.getFullYear();
  const nextMonth = String(nextDay.getMonth() + 1).padStart(2, '0');
  const nextDate = String(nextDay.getDate()).padStart(2, '0');
  const cleanEndDate = `${nextYear}${nextMonth}${nextDate}`;

  // Assemble comprehensive event description
  const descriptionParts: string[] = [];

  if (isAlertEvent) {
    descriptionParts.push(`🚨 تنبيه استباقي مبكر لميعاد قضائي`);
    descriptionParts.push(`----------------------------------------`);
    descriptionParts.push(`• تذكير: متبقي ${proactiveReminderDays} أيام حتى الميعاد النهائي الملزم (${finalDeadlineDate}).`);
    descriptionParts.push(`• الإجراء المطلوب: بدء تجهيز صحيفة الدعوى أو الطعن ومستنداتها وإيداعها بقلم الكتاب.`);
  } else {
    descriptionParts.push(`⚖️ ميعاد قانوني إجرائي نهائي ملزم`);
    descriptionParts.push(`----------------------------------------`);
    descriptionParts.push(`• الميعاد النهائي واجب الالتزام: ${finalDeadlineDate}`);
    if (hasProactiveReminder) {
      descriptionParts.push(`• 🔔 تنبيه استباقي: قبل الميعاد بـ ${proactiveReminderDays} أيام (بتاريخ ${proactiveDateIso}) لضمان قيد الطعن مبكراً.`);
    }
  }

  if (caseNumber) {
    descriptionParts.push(`• رقم القضية / الدعوى: ${caseNumber}`);
  }
  if (clientName) {
    descriptionParts.push(`• اسم الموكل / الخصم: ${clientName}`);
  }
  if (lawArticle) {
    descriptionParts.push(`• السند القانوني: ${lawArticle}`);
  }

  if (notes) {
    descriptionParts.push(`• ملاحظات: ${notes}`);
  }

  if (explanation) {
    descriptionParts.push(`\nتفاصيل الاحتساب الإجرائي:\n${explanation}`);
  }

  descriptionParts.push(`\nتم التوليد بواسطة: حاسبة المواعيد القانونية (قانون المرافعات المصري)`);

  let eventTitle = '';
  if (isAlertEvent) {
    eventTitle = caseNumber
      ? `🚨 تذكير مبكر (${proactiveReminderDays} أيام): ${title} - قضية ${caseNumber}`
      : `🚨 تذكير مبكر (${proactiveReminderDays} أيام): ${title}`;
  } else {
    const reminderBadge = hasProactiveReminder ? ` (تنبيه مسبق: ${proactiveReminderDays} أيام)` : '';
    eventTitle = caseNumber
      ? `ميعاد قانوني: ${title} - قضية ${caseNumber}${reminderBadge}`
      : `ميعاد قانوني: ${title}${reminderBadge}`;
  }

  const params = new URLSearchParams({
    action: 'TEMPLATE',
    text: eventTitle,
    dates: `${cleanStartDate}/${cleanEndDate}`,
    details: descriptionParts.join('\n'),
    location: location
  });

  return `https://calendar.google.com/calendar/render?${params.toString()}`;
}
