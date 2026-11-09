export type StartRule = 'SAME_DAY' | 'NEXT_DAY';

export type DurationUnit = 'DAYS' | 'MONTHS' | 'YEARS' | 'HOURS';

/**
 * Statutory Distance types under Articles 16, 17, and 18
 * of Egyptian Civil and Commercial Procedures Law No. 13 of 1968.
 */
export type DistanceType =
  | 'NONE'
  | 'LOCAL'
  | 'BORDER_AREA'
  | 'ABROAD'
  | 'ABROAD_JUDICIAL_REDUCTION'
  | 'ABROAD_SERVED_IN_EGYPT';

export interface LegalRule {
  id: number;
  actionName: string;
  duration: number;
  unit: DurationUnit;
  distanceDays: number;
  startRule: StartRule;
  lawArticle: string;
  notes: string;
  category?: string;
  triggerEventType?: string;
  triggerEventLabel?: string;
  courtType?: string;
  distancePolicy?: string;
  extensionPolicy?: string;
  lawName?: string;
  article?: string;
  officialSourceUrl?: string;
  effectiveFrom?: string;
  effectiveTo?: string | null;
  lastReviewedAt?: string;
  reviewStatus?: string;
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

export type MilestoneType =
  | 'TRIGGER_EVENT'
  | 'COUNTING_START'
  | 'BASE_DEADLINE'
  | 'DISTANCE_EXTENSION'
  | 'HOLIDAY_EXTENSION'
  | 'FINAL_DEADLINE';

export interface CalculationMilestone {
  type: MilestoneType;
  date: string;
  title: string;
  description: string;
  legalBasis?: string;
  isExcludedDay?: boolean;
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
  milestones?: CalculationMilestone[];
  distanceType?: DistanceType;
  judicialOrderDetails?: string;
  warnings?: string[];
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
  ruleSnapshot?: string;
  distanceType?: DistanceType;
  judicialOrderDetails?: string;
}

export interface CalculateDeadlineParams {
  announcementDate: string;
  rule: LegalRule;
  distanceType?: DistanceType;
  additionalDistanceDays?: number;
  holidays?: Holiday[];
  judicialOrderDetails?: string;
}
