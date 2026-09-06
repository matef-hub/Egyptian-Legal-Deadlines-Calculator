export type StartRule = 'SAME_DAY' | 'NEXT_DAY';

export type DurationUnit = 'DAYS' | 'MONTHS' | 'YEARS' | 'HOURS';

export interface LegalRule {
  id: number;
  actionName: string;
  duration: number;
  unit: DurationUnit;
  distanceDays: number;
  startRule: StartRule;
  lawArticle: string;
  notes: string;
}

export interface Holiday {
  id: number;
  holidayDate: string; // YYYY-MM-DD
  name: string;
}

export interface ExcludedDay {
  date: string;
  reason: string;
}

export interface DeadlineResult {
  startDate: string;
  provisionalDate: string;
  duration: number;
  unit: DurationUnit;
  ruleDistanceDays: number;
  additionalDistanceDays: number;
  totalDistanceDays: number;
  excludedDays: ExcludedDay[];
  finalDeadline: string;
  explanation: string;
  lawArticle: string;
  notes: string;
}

export interface SavedDeadline {
  id: string;
  caseNumber: string;
  clientName: string;
  actionName: string;
  announcementDate: string;
  duration: number;
  unit: DurationUnit;
  distanceDays: number;
  finalDeadline: string;
  lawArticle: string;
  calculationExplanation: string;
  notes: string;
  createdAt: number;
  proactiveReminderDays?: number;
}
