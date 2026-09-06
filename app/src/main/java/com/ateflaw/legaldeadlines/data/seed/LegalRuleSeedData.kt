package com.ateflaw.legaldeadlines.data.seed

import com.ateflaw.legaldeadlines.domain.model.DurationUnit
import com.ateflaw.legaldeadlines.domain.model.Holiday
import com.ateflaw.legaldeadlines.domain.model.LegalRule
import com.ateflaw.legaldeadlines.domain.model.StartRule
import java.time.LocalDate

/**
 * Seed data provider for Egyptian legal procedural rules and initial official national holidays.
 *
 * Conforms to guidelines: contains authentic procedural rules with clear statutory basis,
 * providing a fully functional initial database seed while supporting full synchronization
 * and dynamic rule updates.
 */
object LegalRuleSeedData {

    fun getSeedRules(): List<LegalRule> {
        return listOf(
            LegalRule(
                id = 1,
                actionName = "الطعن بالاستئناف في الأحكام الصادرة في المواد المدنية والتجارية",
                duration = 40,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 227 من قانون المرافعات المدنية والتجارية رقم 13 لسنة 1968",
                notes = "ميعاد الاستئناف أربعون يوماً ما لم ينص القانون على غير ذلك، ويبدأ من اليوم التالي لصدور الحكم أو إعلانه بحسب الأحوال."
            ),
            LegalRule(
                id = 2,
                actionName = "الطعن بالاستئناف في المواد المستعجلة والقضاء المستعجل",
                duration = 15,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 227 فقرة 2 من قانون المرافعات المدنية والتجارية",
                notes = "ميعاد استئناف الأحكام الصادرة في المواد المستعجلة خمسة عشر يوماً أياً كانت المحكمة التي أصدرتها."
            ),
            LegalRule(
                id = 3,
                actionName = "الطعن بالنقض في المواد المدنية والتجارية والأحوال الشخصية",
                duration = 60,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 252 من قانون المرافعات المدنية والتجارية",
                notes = "ميعاد الطعن بالنقض ستون يوماً من تاريخ صدور الحكم إذا كان حضورياً أو إعلانه إذا كان بمثابة الحضوري."
            ),
            LegalRule(
                id = 4,
                actionName = "إيداع أسباب الطعن بالنقض في المواد الجنائية والتقرير به",
                duration = 60,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 34 من القانون رقم 57 لسنة 1959 بشأن حالات وإجراءات الطعن أمام محكمة النقض",
                notes = "يحصل الطعن بالتقرير به في قلم كتاب المحكمة خلال ستين يوماً من تاريخ الحكم الحضوري مع إيداع الأسباب في ذات الميعاد."
            ),
            LegalRule(
                id = 5,
                actionName = "الطعن بالمعارضة في الأحكام الغيابية في مواد الجنح",
                duration = 10,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 398 من قانون الإجراءات الجنائية رقم 150 لسنة 1950",
                notes = "تقبل المعارضة في الأحكام الغيابية الصادرة في الجنح والمخالفات خلال العشرة أيام التالية لإعلان الحكم الغيابي للمحكوم عليه."
            ),
            LegalRule(
                id = 6,
                actionName = "الطعن بالاستئناف في أحكام محاكم الجنح والمخالفات",
                duration = 10,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 406 من قانون الإجراءات الجنائية",
                notes = "يحصل الاستئناف بتقرير في قلم كتاب المحكمة التي أصدرت الحكم في ظرف عشرة أيام من تاريخ النطق بالحكم الحضوري أو إعلانه."
            ),
            LegalRule(
                id = 7,
                actionName = "التظلم من أمر الأداء القضائي",
                duration = 10,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 204 من قانون المرافعات المدنية والتجارية",
                notes = "يجوز للمدين التظلم من أمر الأداء خلال عشرة أيام من تاريخ إعلانه إليه بالأمر بورقة من أوراق المحضرين."
            ),
            LegalRule(
                id = 8,
                actionName = "استئناف أمر الأداء الصادر من القاضي الجزئي أو الابتدائي",
                duration = 40,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 205 من قانون المرافعات المدنية والتجارية",
                notes = "يخضع استئناف أمر الأداء للقواعد والمواعيد المقررة لاستئناف الأحكام (40 يوماً)."
            ),
            LegalRule(
                id = 9,
                actionName = "الطعن بالتماس إعادة النظر في الأحكام النهائية",
                duration = 40,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 242 من قانون المرافعات المدنية والتجارية",
                notes = "ميعاد الالتماس أربعون يوماً ويبدأ في حالات الغش أو الورقة المزورة من اليوم الذي ظهر فيه الغش أو ثبت فيه التزوير."
            ),
            LegalRule(
                id = 10,
                actionName = "إعلان صحيفة الدعوى وتكليف الخصم بالحضور أمام المحكمة الجزئية",
                duration = 8,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 84 من قانون المرافعات المدنية والتجارية",
                notes = "ميعاد الحضور أمام المحكمة الجزئية ثمانية أيام كاملة على الأقل قبل موعد الجلسة."
            ),
            LegalRule(
                id = 11,
                actionName = "إعلان صحيفة الدعوى وتكليف الخصم بالحضور أمام المحكمة الابتدائية والاستئناف",
                duration = 15,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 84 من قانون المرافعات المدنية والتجارية",
                notes = "ميعاد الحضور أمام المحكمة الابتدائية ومحكمة الاستئناف خمسة عشر يوماً كاملة على الأقل قبل الجلسة."
            ),
            LegalRule(
                id = 12,
                actionName = "سقوط الخصومة القضائية لعدم السير فيها بفعل المدعي أو امتناعه",
                duration = 1,
                unit = DurationUnit.YEARS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 134 من قانون المرافعات المدنية والتجارية",
                notes = "تسقط الخصومة بانقضاء سنة واحدة من آخر إجراء صحيح اتخذ في الدعوى."
            ),
            LegalRule(
                id = 13,
                actionName = "انقضاء الخصومة بمضي المدة (التقادم الإجرائي المسقط)",
                duration = 3,
                unit = DurationUnit.YEARS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 140 من قانون المرافعات المدنية والتجارية",
                notes = "في جميع الأحوال تنقضي الخصومة بمضي ثلاث سنوات على آخر إجراء صحيح اتخذ فيها."
            ),
            LegalRule(
                id = 14,
                actionName = "تجديد الدعوى من الشطب وإعلان صحيفة التجديد",
                duration = 60,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 82 من قانون المرافعات المدنية والتجارية",
                notes = "إذا بقيت الدعوى مشطوبة ستين يوماً ولم يطلب أحد الخصوم السير فيها أو لم يحضر الطرفان اعتبرت كأن لم تكن."
            ),
            LegalRule(
                id = 15,
                actionName = "تجديد الدعوى المحكوم بوقفها جزائياً بعد انتهاء مدة الوقف",
                duration = 30,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 99 من قانون المرافعات المدنية والتجارية",
                notes = "يجب على المدعي تعجيل الدعوى وإعلان الخصم خلال ثلاثين يوماً التالية لانتهاء مدة الوقف الجزائي وإلا حُكم باعتبارها كأن لم تكن."
            ),
            LegalRule(
                id = 16,
                actionName = "رد القضاة وأعضاء الدائرة القضائية",
                duration = 3,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 153 من قانون المرافعات المدنية والتجارية",
                notes = "يجب تقديم طلب الرد بتقرير في قلم كتاب المحكمة وتحديد أسبابه وإيداع الكفالة قبل قفل باب المرافعة."
            ),
            LegalRule(
                id = 17,
                actionName = "الاعتراض على إنذار الطاعة في مسائل الأحوال الشخصية",
                duration = 30,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 11 مكرر ثانياً من القانون رقم 25 لسنة 1929 المعدل بالقانون 100 لسنة 1985",
                notes = "للزوجة حق الاعتراض على إنذار الطاعة أمام محكمة الأسرة خلال ثلاثين يوماً من تاريخ إعلانها بالإنذار."
            ),
            LegalRule(
                id = 18,
                actionName = "الطعن أمام المحكمة الإدارية العليا في أحكام القضاء الإداري",
                duration = 60,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 44 من قانون مجلس الدولة رقم 47 لسنة 1972",
                notes = "ميعاد الطعن أمام المحكمة الإدارية العليا ستون يوماً من تاريخ صدور الحكم المطعون فيه."
            ),
            LegalRule(
                id = 19,
                actionName = "ميعاد التظلم الوجوبي من القرار الإداري قبل رفع دعوى الإلغاء",
                duration = 60,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 24 من قانون مجلس الدولة رقم 47 لسنة 1972",
                notes = "ميعاد رفع دعوى الإلغاء ستون يوماً من تاريخ نشر القرار الإداري أو إعلانه للذوي الشأن أو العلم اليقيني به."
            ),
            LegalRule(
                id = 20,
                actionName = "المعارضة في أمر تقدير الرسوم القضائية النسبية والخدمات",
                duration = 8,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 17 من القانون رقم 90 لسنة 1944 بشأن الرسوم القضائية في المواد المدنية",
                notes = "يجوز للمدين بالرسوم المعارضة في مقدارها بتقرير في قلم الكتاب خلال ثمانية أيام من تاريخ إعلانه بأمر التقدير."
            )
        )
    }

    fun getInitialOfficialHolidays(): List<Holiday> {
        return listOf(
            Holiday(id = 1, holidayDate = LocalDate.of(2026, 1, 7), name = "عيد الميلاد المجيد"),
            Holiday(id = 2, holidayDate = LocalDate.of(2026, 1, 25), name = "عيد الشرطة وثورة 25 يناير"),
            Holiday(id = 3, holidayDate = LocalDate.of(2026, 3, 20), name = "وقفة عيد الفطر المبارك"),
            Holiday(id = 4, holidayDate = LocalDate.of(2026, 3, 21), name = "عيد الفطر المبارك"),
            Holiday(id = 5, holidayDate = LocalDate.of(2026, 3, 22), name = "إجازة عيد الفطر المبارك"),
            Holiday(id = 6, holidayDate = LocalDate.of(2026, 4, 13), name = "عيد شم النسيم"),
            Holiday(id = 7, holidayDate = LocalDate.of(2026, 4, 25), name = "عيد تحرير سيناء"),
            Holiday(id = 8, holidayDate = LocalDate.of(2026, 5, 1), name = "عيد العمال"),
            Holiday(id = 9, holidayDate = LocalDate.of(2026, 5, 26), name = "وقفة عرفات"),
            Holiday(id = 10, holidayDate = LocalDate.of(2026, 5, 27), name = "عيد الأضحى المبارك"),
            Holiday(id = 11, holidayDate = LocalDate.of(2026, 5, 28), name = "إجازة عيد الأضحى المبارك"),
            Holiday(id = 12, holidayDate = LocalDate.of(2026, 6, 17), name = "رأس السنة الهجرية"),
            Holiday(id = 13, holidayDate = LocalDate.of(2026, 6, 30), name = "ثورة 30 يونيو"),
            Holiday(id = 14, holidayDate = LocalDate.of(2026, 7, 23), name = "ثورة 23 يوليو"),
            Holiday(id = 15, holidayDate = LocalDate.of(2026, 8, 26), name = "المولد النبوي الشريف"),
            Holiday(id = 16, holidayDate = LocalDate.of(2026, 10, 6), name = "عيد القوات المسلحة (حرب 6 أكتوبر)")
        )
    }
}
