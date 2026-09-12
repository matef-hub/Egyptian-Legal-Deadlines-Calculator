package com.ateflaw.legaldeadlines.data.seed

import com.ateflaw.legaldeadlines.domain.model.DurationUnit
import com.ateflaw.legaldeadlines.domain.model.Holiday
import com.ateflaw.legaldeadlines.domain.model.LegalRule
import com.ateflaw.legaldeadlines.domain.model.StartRule
import java.time.LocalDate

/**
 * Seed data provider for Egyptian legal procedural rules and initial official national holidays.
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
                notes = "ميعاد الاستئناف أربعون يوماً ما لم ينص القانون على غير ذلك، ويبدأ من اليوم التالي لصدور الحكم أو إعلانه بحسب الأحوال.",
                summary = "استئناف مدني وتجاري"
            ),
            LegalRule(
                id = 2,
                actionName = "الطعن بالاستئناف في المواد المستعجلة والقضاء المستعجل",
                duration = 15,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 227 فقرة 2 من قانون المرافعات المدنية والتجارية",
                notes = "ميعاد استئناف الأحكام الصادرة في المواد المستعجلة خمسة عشر يوماً أياً كانت المحكمة التي أصدرتها.",
                summary = "استئناف قضاء مستعجل"
            ),
            LegalRule(
                id = 3,
                actionName = "الطعن بالنقض في المواد المدنية والتجارية والأحوال الشخصية",
                duration = 60,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 252 من قانون المرافعات المدنية والتجارية",
                notes = "ميعاد الطعن بالنقض ستون يوماً من تاريخ صدور الحكم إذا كان حضورياً أو إعلانه إذا كان بمثابة الحضوري.",
                summary = "نقض مدني وتجاري"
            ),
            LegalRule(
                id = 4,
                actionName = "إيداع أسباب الطعن بالنقض في المواد الجنائية والتقرير به",
                duration = 60,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 34 من القانون رقم 57 لسنة 1959 بشأن حالات وإجراءات الطعن أمام محكمة النقض",
                notes = "يحصل الطعن بالتقرير به في قلم كتاب المحكمة خلال ستين يوماً من تاريخ الحكم الحضوري مع إيداع الأسباب في ذات الميعاد.",
                summary = "نقض جنائي"
            ),
            LegalRule(
                id = 5,
                actionName = "الطعن بالمعارضة في الأحكام الغيابية في مواد الجنح",
                duration = 10,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 398 من قانون الإجراءات الجنائية رقم 150 لسنة 1950",
                notes = "تقبل المعارضة في الأحكام الغيابية الصادرة في الجنح والمخالفات خلال العشرة أيام التالية لإعلان الحكم الغيابي للمحكوم عليه.",
                summary = "معارضة أحكام غيابية"
            ),
            LegalRule(
                id = 6,
                actionName = "الطعن بالاستئناف في أحكام محاكم الجنح والمخالفات",
                duration = 10,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 406 من قانون الإجراءات الجنائية",
                notes = "يحصل الاستئناف بتقرير في قلم كتاب المحكمة التي أصدرت الحكم في ظرف عشرة أيام من تاريخ النطق بالحكم الحضوري أو إعلانه.",
                summary = "استئناف جنح ومخالفات"
            ),
            LegalRule(
                id = 7,
                actionName = "التظلم من أمر الأداء القضائي",
                duration = 10,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 206 من قانون المرافعات المدنية والتجارية",
                notes = "يجوز للمدين التظلم من أمر الأداء خلال عشرة أيام من تاريخ إعلانه إليه، ويحصل التظلم أمام المحكمة الجزئية أو الابتدائية حسب الأحوال ويجب أن يكون مسبباً وإلا كان باطلاً.",
                summary = "تظلم أمر أداء"
            ),
            LegalRule(
                id = 8,
                actionName = "استئناف أمر الأداء الصادر من القاضي الجزئي أو الابتدائي",
                duration = 40,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادتان 206 و227 من قانون المرافعات المدنية والتجارية",
                notes = "يبدأ ميعاد استئناف أمر الأداء من تاريخ فوات ميعاد التظلم أو من تاريخ اعتبار التظلم كأن لم يكن (م206)، ويخضع في مدته لقواعد استئناف الأحكام العادية (40 يوماً وفق م227).",
                summary = "استئناف أمر أداء"
            ),
            LegalRule(
                id = 9,
                actionName = "الطعن بالتماس إعادة النظر في الأحكام النهائية",
                duration = 40,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 242 من قانون المرافعات المدنية والتجارية",
                notes = "ميعاد الالتماس أربعون يوماً ويبدأ في حالات الغش أو الورقة المزورة من اليوم الذي ظهر فيه الغش أو ثبت فيه التزوير.",
                summary = "التماس إعادة نظر"
            ),
            LegalRule(
                id = 10,
                actionName = "إعلان صحيفة الدعوى وتكليف الخصم بالحضور أمام المحكمة الجزئية",
                duration = 8,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 66 من قانون المرافعات المدنية والتجارية",
                notes = "ميعاد الحضور أمام المحكمة الجزئية ثمانية أيام كاملة على الأقل قبل موعد الجلسة، ويجوز في حالة الضرورة نقصه إلى ثلاثة أيام أو أربع وعشرين ساعة.",
                summary = "إعلان محكمة جزئية"
            ),
            LegalRule(
                id = 11,
                actionName = "إعلان صحيفة الدعوى وتكليف الخصم بالحضور أمام المحكمة الابتدائية والاستئناف",
                duration = 15,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 66 من قانون المرافعات المدنية والتجارية",
                notes = "ميعاد الحضور أمام المحكمة الابتدائية ومحكمة الاستئناف خمسة عشر يوماً كاملة على الأقل قبل الجلسة، ويجوز في حالة الضرورة نقصه إلى ثلاثة أيام أو أربع وعشرين ساعة.",
                summary = "إعلان محكمة ابتدائية"
            ),
            LegalRule(
                id = 12,
                actionName = "سقوط الخصومة القضائية لعدم السير فيها بفعل المدعي أو امتناعه",
                duration = 180,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 134 من قانون المرافعات المدنية والتجارية (المعدلة بالقانون رقم 18 لسنة 1999)",
                notes = "تسقط الخصومة بانقضاء ستة أشهر من آخر إجراء صحيح اتخذ في الدعوى بفعل المدعي أو امتناعه (كانت المدة سنة قبل تعديل 1999).",
                summary = "سقوط الخصومة القضائية"
            ),
            LegalRule(
                id = 13,
                actionName = "انقضاء الخصومة بمضي المدة (التقادم الإجرائي المسقط)",
                duration = 3,
                unit = DurationUnit.YEARS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 140 من قانون المرافعات المدنية والتجارية",
                notes = "في جميع الأحوال تنقضي الخصومة بمضي ثلاث سنوات على آخر إجراء صحيح اتخذ فيها.",
                summary = "انقضاء الخصومة القضائية"
            ),
            LegalRule(
                id = 14,
                actionName = "تجديد الدعوى من الشطب وإعلان صحيفة التجديد",
                duration = 60,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 82 من قانون المرافعات المدنية والتجارية",
                notes = "إذا بقيت الدعوى مشطوبة ستين يوماً ولم يطلب أحد الخصوم السير فيها أو لم يحضر الطرفان اعتبرت كأن لم تكن.",
                summary = "تجديد من الشطب"
            ),
            LegalRule(
                id = 15,
                actionName = "تجديد الدعوى المحكوم بوقفها جزائياً بعد انتهاء مدة الوقف",
                duration = 30,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 99 من قانون المرافعات المدنية والتجارية",
                notes = "يجب على المدعي تعجيل الدعوى وإعلان الخصم خلال ثلاثين يوماً التالية لانتهاء مدة الوقف الجزائي وإلا حُكم باعتبارها كأن لم تكن.",
                summary = "تجديد وقف جزائي"
            ),
            LegalRule(
                id = 16,
                actionName = "رد القضاة وأعضاء الدائرة القضائية",
                duration = 3,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المواد 152 و153 و157 من قانون المرافعات المدنية والتجارية",
                notes = "لا يقبل طلب الرد بعد قفل باب المرافعة (م152) دون مدة محددة كقاعدة عامة، ويقدم بتقرير مسبب في قلم كتاب المحكمة مع إيداع الكفالة (م153). أما إذا كان الرد في حق قاضٍ منتدب لإجراء من إجراءات الإثبات فيجب تقديم الطلب خلال ثلاثة أيام من يوم ندبه أو من إعلانه به إن صدر الندب في غيبته (م157).",
                summary = "طلب رد القضاة"
            ),
            LegalRule(
                id = 17,
                actionName = "الاعتراض على إنذار الطاعة في مسائل الأحوال الشخصية",
                duration = 30,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 11 مكرر ثانياً من القانون رقم 25 لسنة 1929 المعدل بالقانون 100 لسنة 1985",
                notes = "للزوجة حق الاعتراض على إنذار الطاعة أمام محكمة الأسرة خلال ثلاثين يوماً من تاريخ إعلانها بالإنذار.",
                summary = "اعتراض إنذار طاعة"
            ),
            LegalRule(
                id = 18,
                actionName = "الطعن أمام المحكمة الإدارية العليا في أحكام القضاء الإداري",
                duration = 60,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 44 من قانون مجلس الدولة رقم 47 لسنة 1972",
                notes = "ميعاد الطعن أمام المحكمة الإدارية العليا ستون يوماً من تاريخ صدور الحكم المطعون فيه.",
                summary = "طعن إدارية عليا"
            ),
            LegalRule(
                id = 19,
                actionName = "ميعاد التظلم الوجوبي من القرار الإداري قبل رفع دعوى الإلغاء",
                duration = 60,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 24 من قانون مجلس الدولة رقم 47 لسنة 1972",
                notes = "ميعاد رفع دعوى الإلغاء ستون يوماً من تاريخ نشر القرار الإداري أو إعلانه للذوي الشأن أو العلم اليقيني به.",
                summary = "تظلم قرار إداري"
            ),
            LegalRule(
                id = 20,
                actionName = "المعارضة في أمر تقدير الرسوم القضائية النسبية والخدمات",
                duration = 8,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 17 من القانون رقم 90 لسنة 1944 بشأن الرسوم القضائية في المواد المدنية",
                notes = "يجوز للمدين بالرسوم المعارضة في مقدارها بتقرير في قلم الكتاب خلال ثمانية أيام من تاريخ إعلانه بأمر التقدير.",
                summary = "معارضة رسوم قضائية"
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
