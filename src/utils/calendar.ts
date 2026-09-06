export interface CalendarEventPayload {
  title: string;
  finalDeadlineDate: string; // 'YYYY-MM-DD'
  lawArticle?: string;
  explanation?: string;
  notes?: string;
  caseNumber?: string;
  clientName?: string;
  location?: string;
}

/**
 * Builds a direct URL to create an event in Google Calendar.
 * Uses action=TEMPLATE parameters for all-day events on the exact calculated deadline date.
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
    location = 'المحكمة المختصة'
  } = payload;

  // Format start date as YYYYMMDD
  const cleanStartDate = finalDeadlineDate.replace(/-/g, '');

  // For all-day events in Google Calendar, end date is the next day (exclusive)
  const [year, month, day] = finalDeadlineDate.split('-').map(Number);
  const nextDay = new Date(year, month - 1, day + 1);
  const nextYear = nextDay.getFullYear();
  const nextMonth = String(nextDay.getMonth() + 1).padStart(2, '0');
  const nextDate = String(nextDay.getDate()).padStart(2, '0');
  const cleanEndDate = `${nextYear}${nextMonth}${nextDate}`;

  // Assemble comprehensive event description
  const descriptionParts: string[] = [
    `⚖️ ميعاد قانوني إجرائي نهائي ملزم`,
    `----------------------------------------`
  ];

  if (caseNumber) {
    descriptionParts.push(`• رقم القضية / الدعوى: ${caseNumber}`);
  }
  if (clientName) {
    descriptionParts.push(`• اسم الموكل / الخصم: ${clientName}`);
  }
  if (lawArticle) {
    descriptionParts.push(`• السند القانوني: ${lawArticle}`);
  }

  descriptionParts.push(`• الميعاد النهائي: ${finalDeadlineDate}`);

  if (notes) {
    descriptionParts.push(`• ملاحظات: ${notes}`);
  }

  if (explanation) {
    descriptionParts.push(`\nتفاصيل الاحتساب الإجرائي:\n${explanation}`);
  }

  descriptionParts.push(`\nتم التوليد بواسطة: حاسبة المواعيد القانونية (قانون المرافعات المصري)`);

  const eventTitle = caseNumber
    ? `ميعاد قانوني: ${title} - قضية ${caseNumber}`
    : `ميعاد قانوني: ${title}`;

  const params = new URLSearchParams({
    action: 'TEMPLATE',
    text: eventTitle,
    dates: `${cleanStartDate}/${cleanEndDate}`,
    details: descriptionParts.join('\n'),
    location: location
  });

  return `https://calendar.google.com/calendar/render?${params.toString()}`;
}
