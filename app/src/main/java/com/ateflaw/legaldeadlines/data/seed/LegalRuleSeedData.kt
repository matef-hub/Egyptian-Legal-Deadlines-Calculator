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
            // =========================================================================
            // أولاً: قانون المرافعات المدنية والتجارية والإثبات وقواعد التنفيذ
            // =========================================================================
            LegalRule(
                id = 1,
                actionName = "إعلان قرار الحيازة بعد صدوره من النيابة العامة",
                duration = 3,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 44 مكرر فقرة 2 من قانون المرافعات المدنية والتجارية",
                notes = "يجب إعلان القرار الصادر في منازعة الحيازة لذوي الشأن خلال ثلاثة أيام من صدوره.",
                summary = "إعلان قرار الحيازة"
            ),
            LegalRule(
                id = 2,
                actionName = "التظلم من قرار النيابة العامة في منازعات الحيازة",
                duration = 15,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 44 مكرر فقرة 3 من قانون المرافعات المدنية والتجارية",
                notes = "التظلم من قرار النيابة في منازعة الحيازة أمام قاضي الأمور المستعجلة خلال 15 يوماً من تاريخ الإعلان.",
                summary = "تظلم قرار حيازة"
            ),
            LegalRule(
                id = 3,
                actionName = "التقرير والإعلان بشواهد التزوير",
                duration = 8,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 49 فقرة 2 من قانون الإثبات في المواد المدنية والتجارية",
                notes = "يجب إعلان مذكرة شواهد التزوير خلال ثمانية أيام من تاريخ التقرير بالادعاء بالتزوير في قلم الكتاب وإلا سقط الادعاء.",
                summary = "شواهد التزوير"
            ),
            LegalRule(
                id = 4,
                actionName = "إيداع المدعى عليه مذكرة دفاعه ومستنداته قبل أول جلسة",
                duration = 3,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 65 فقرة 3 من قانون المرافعات المدنية والتجارية",
                notes = "يجب على المدعى عليه في جميع الدعاوى عدا المستعجلة إيداع مذكرة بدفاعه ومستنداته قبل الجلسة المحددة بثلاثة أيام على الأقل.",
                summary = "إيداع دفاع المدعى عليه"
            ),
            LegalRule(
                id = 5,
                actionName = "إعلان صحيفة الدعوى وتكليف الخصم بالحضور أمام المحكمة الجزئية",
                duration = 8,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 66 فقرة 1 من قانون المرافعات المدنية والتجارية",
                notes = "ميعاد الحضور أمام المحكمة الجزئية ثمانية أيام كاملة على الأقل قبل موعد الجلسة، ويجوز نقصه لـ 24 ساعة للضرورة. تضاف مواعيد المسافة.",
                summary = "إعلان محكمة جزئية"
            ),
            LegalRule(
                id = 6,
                actionName = "إعلان صحيفة الدعوى وتكليف الخصم بالحضور أمام الابتدائية والاستئناف",
                duration = 15,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 66 فقرة 1 من قانون المرافعات المدنية والتجارية",
                notes = "ميعاد الحضور أمام المحكمة الابتدائية والاستئناف 15 يوماً كاملة قبل الجلسة، ويجوز نقصه لـ 3 أيام للضرورة. تضاف مواعيد المسافة.",
                summary = "إعلان ابتدائية واستئناف"
            ),
            LegalRule(
                id = 7,
                actionName = "ميعاد الحضور في الدعاوى المستعجلة",
                duration = 1,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 66 فقرة 2 من قانون المرافعات المدنية والتجارية",
                notes = "ميعاد الحضور في الدعاوى المستعجلة 24 ساعة، ويجوز نقصه وجعله من ساعة لساعة بإذن قاضي الأمور الوقتية بشرط إعلان الخصم نفسه.",
                summary = "حضور دعاوى مستعجلة"
            ),
            LegalRule(
                id = 8,
                actionName = "إعلان قلم المحضرين لصحيفة الدعوى للمدعى عليه",
                duration = 30,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 68 من قانون المرافعات المدنية والتجارية",
                notes = "على قلم المحضرين إعلان صحيفة الدعوى خلال ثلاثين يوماً على الأكثر من تاريخ تسليمها إليه وقبل الجلسة.",
                summary = "إعلان صحيفة الدعوى بالمحضرين"
            ),
            LegalRule(
                id = 9,
                actionName = "اعتبار الدعوى كأن لم تكن لعدم تكليف المدعى عليه بالحضور",
                duration = 3,
                unit = DurationUnit.MONTHS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 70 من قانون المرافعات المدنية والتجارية",
                notes = "تعتبر الدعوى كأن لم تكن إذا لم يتم تكليف المدعى عليه بالحضور خلال ثلاثة أشهر تقويمية من تقديم الصحيفة لقلم الكتاب بفعل المدعي.",
                summary = "عدم إعلان الصحيفة (3 أشهر)"
            ),
            LegalRule(
                id = 10,
                actionName = "تجديد الدعوى من الشطب وإعلان صحيفة التجديد",
                duration = 60,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 82 من قانون المرافعات المدنية والتجارية",
                notes = "إذا بقيت الدعوى مشطوبة ستين يوماً ولم يطلب أحد الخصوم السير فيها وإعلان شريكه اعتبرت كأن لم تكن.",
                summary = "تجديد دعوى من الشطب"
            ),
            LegalRule(
                id = 11,
                actionName = "تعجيل الدعوى المحكوم بوقفها جزائياً مع الإعلان",
                duration = 30,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 99 من قانون المرافعات (المعدلة بالقانون رقم 18 لسنة 1999)",
                notes = "يجب على المدعي تعجيل الدعوى وإعلان خصمه خلال ثلاثين يوماً التالية لانتهاء مدة الوقف الجزائي وإلا حُكم باعتبارها كأن لم تكن.",
                summary = "تعجيل الوقف الجزائي"
            ),
            LegalRule(
                id = 12,
                actionName = "تعجيل الدعوى بعد انتهاء مدة الوقف الاتفاقي",
                duration = 8,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 128 فقرة 2 من قانون المرافعات المدنية والتجارية",
                notes = "إذا لم تعجل الدعوى في ثمانية الأيام التالية لنهاية الأجل الاتفاقي، اعتبر المدعي تاركاً لدعواه والمستأنف تاركاً استئنافه.",
                summary = "تعجيل الوقف الاتفاقي"
            ),
            LegalRule(
                id = 13,
                actionName = "انقضاء الخصومة القضائية بمضي المدة عند انقطاع سيرها",
                duration = 2,
                unit = DurationUnit.YEARS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 129 من قانون المرافعات المدنية والتجارية",
                notes = "تنقضي الخصومة بمضي سنتين من تاريخ آخر إجراء صحيح في حالة عدم السير فيها بعد انقطاعها لوفاة أحد الخصوم أو زوال أهليته.",
                summary = "انقضاء الخصومة بعد الانقطاع"
            ),
            LegalRule(
                id = 14,
                actionName = "تعيين وكيل جديد لمن انقضت وكالته لمنع انقطاع الخصومة",
                duration = 15,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 130 فقرة 3 من قانون المرافعات المدنية والتجارية",
                notes = "للمحكمة منح أجل للخصم الذي توفي وكيله أو انقضت وكالته إذا بادر وعين وكيلاً جديداً خلال 15 يوماً من انقضاء الوكالة الأولى.",
                summary = "تعيين وكيل جديد"
            ),
            LegalRule(
                id = 15,
                actionName = "سقوط الخصومة القضائية لعدم السير فيها بفعل المدعي أو امتناعه",
                duration = 6,
                unit = DurationUnit.MONTHS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 134 من قانون المرافعات (المعدلة بالقانون رقم 18 لسنة 1999)",
                notes = "تسقط الخصومة بانقضاء ستة أشهر تقويمية من تاريخ آخر إجراء صحيح اتخذ فيها بفعل المدعي أو امتناعه عن موالاة السير.",
                summary = "سقوط الخصومة القضائية"
            ),
            LegalRule(
                id = 16,
                actionName = "انقضاء الخصومة بمضي المدة في جميع الأحوال (التقادم المسقط)",
                duration = 3,
                unit = DurationUnit.YEARS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 140 من قانون المرافعات المدنية والتجارية",
                notes = "في جميع الأحوال تنقضي الخصومة بمضي ثلاث سنوات على آخر إجراء صحيح اتخذ فيها أياً كان سبب عدم السير.",
                summary = "انقضاء الخصومة بمضي 3 سنوات"
            ),
            LegalRule(
                id = 17,
                actionName = "طلب رد قضاة الحكم أمام محكمة الموضوع",
                duration = 0,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.SAME_DAY,
                lawArticle = "المادتان 152 و153 من قانون المرافعات المدنية والتجارية",
                notes = "لا يقبل طلب الرد بعد قفل باب المرافعة في الدعوى، ويقدم بتقرير مسبب في قلم كتاب المحكمة قبل ذلك مع إيداع الكفالة.",
                summary = "رد قضاة الموضوع (قفل المرافعة)"
            ),
            LegalRule(
                id = 18,
                actionName = "تأييد طلب رد القاضي بقلم كتاب المحكمة",
                duration = 1,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 154 من قانون المرافعات المدنية والتجارية",
                notes = "إذا حصل الرد بمذكرة سلمت لكاتب الجلسة، وجب على طالب الرد تأييد الطلب بقلم الكتاب في اليوم نفسه أو في اليوم التالي وإلا سقط حقه.",
                summary = "تأييد طلب رد القاضي"
            ),
            LegalRule(
                id = 19,
                actionName = "إجابة القاضي المطلوب رده على وقائع الرد وأسبابه",
                duration = 4,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 156 من قانون المرافعات المدنية والتجارية",
                notes = "على القاضي المطلوب رده أن يجيب بالكتابة على وقائع الرد وأسبابه خلال الأربعة الأيام التالية لاطلاعه على تقرير الرد.",
                summary = "إجابة القاضي المطلوب رده"
            ),
            LegalRule(
                id = 20,
                actionName = "طلب رد القاضي المنتدب للإثبات أو التحقيق أو المعاينة",
                duration = 3,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 157 من قانون المرافعات المدنية والتجارية",
                notes = "يقدم طلب الرد خلال ثلاثة أيام من يوم ندب القاضي إذا صدر بحضوره أو من يوم إعلانه به إن صدر في غيبته.",
                summary = "رد قاضٍ منتدب للإثبات"
            ),
            LegalRule(
                id = 21,
                actionName = "طلب الخبير إعفاءه من أداء المأمورية",
                duration = 5,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 140 من قانون الإثبات في المواد المدنية والتجارية",
                notes = "للخبير خلال خمسة أيام من تسلمه صورة الحكم أن يطلب إعفاءه من المأمورية ويبدي أسبابه المقبولة لرئيس المحكمة.",
                summary = "طلب إعفاء الخبير"
            ),
            LegalRule(
                id = 22,
                actionName = "طلب رد الخبير في الدعاوى المدنية والتجارية",
                duration = 3,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 142 من قانون الإثبات في المواد المدنية والتجارية",
                notes = "يحصل طلب رد الخبير بتكليفه بالحضور في الثلاثة أيام التالية لحكم تعيينه إن كان حضورياً، أو 3 أيام من إعلانه.",
                summary = "رد الخبير مدنياً"
            ),
            LegalRule(
                id = 23,
                actionName = "تحديد الخبير لبدء عمله ودعوة الخصوم للحضور",
                duration = 15,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 146 من قانون الإثبات في المواد المدنية والتجارية",
                notes = "على الخبير تحديد بدء عمله بتاريخ لا يجاوز 15 يوماً من تكليفه، ويدعو الخصوم بكتاب مسجل يرسل قبل الجلسة بـ 7 أيام على الأقل.",
                summary = "بدء مأمورية الخبير ودعوة الخصوم"
            ),
            LegalRule(
                id = 24,
                actionName = "إخطار الخبير للخصوم بإيداع تقريره ملف الدعوى",
                duration = 1,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 151 من قانون الإثبات في المواد المدنية والتجارية",
                notes = "على الخبير إخطار الخصوم بإيداع تقريره ومحاضر أعماله خلال 24 ساعة (يوم واحد) من حصول الإيداع بكتاب مسجل.",
                summary = "إخطار إيداع تقرير الخبير"
            ),
            LegalRule(
                id = 25,
                actionName = "التظلم من أمر تقدير مصاريف الدعوى والرسوم",
                duration = 8,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 190 من قانون المرافعات المدنية والتجارية",
                notes = "يجوز التظلم من أمر تقدير المصاريف بتقرير بقلم الكتاب أو أمام المحضر خلال ثمانية أيام من تاريخ الإعلان بالأمر.",
                summary = "تظلم مصاريف الدعوى"
            ),
            LegalRule(
                id = 26,
                actionName = "التظلم من الأمر الصادر على عريضة",
                duration = 10,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 192 من قانون المرافعات المدنية والتجارية",
                notes = "يجوز للمتظلم من الأمر على عريضة رفع التظلم خلال عشرة أيام من تاريخ صدور الأمر أو من تاريخ إعلانه به.",
                summary = "تظلم من أمر على عريضة"
            ),
            LegalRule(
                id = 27,
                actionName = "سقوط الأمر على عريضة إذا لم يقدم للتنفيذ",
                duration = 30,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 200 من قانون المرافعات المدنية والتجارية",
                notes = "يسقط الأمر الصادر على عريضة إذا لم يقدم للتنفيذ خلال ثلاثين يوماً من تاريخ صدوره، ولا يمنع ذلك من استصدار أمر جديد.",
                summary = "سقوط الأمر على عريضة"
            ),
            LegalRule(
                id = 28,
                actionName = "ميعاد التكليف بالوفاء السابق على استصدار أمر الأداء",
                duration = 5,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 202 من قانون المرافعات المدنية والتجارية",
                notes = "يسبق طلب أمر الأداء تكليف المدين بالوفاء بمهلة لا تقل عن خمسة أيام كاملة بكتاب مسجل بعلم الوصول أو بروتستو.",
                summary = "تكليف بالوفاء قبل أمر الأداء"
            ),
            LegalRule(
                id = 29,
                actionName = "إصدار القاضي لأمر الأداء على العريضة",
                duration = 3,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 203 فقرة 3 من قانون المرافعات المدنية والتجارية",
                notes = "يجب أن يصدر القاضي الأمر بالأداء على إحدى نسختي العريضة خلال ثلاثة أيام على الأكثر من تقديمها.",
                summary = "إصدار أمر الأداء"
            ),
            LegalRule(
                id = 30,
                actionName = "سقوط أمر الأداء لعدم إعلانه مع الصحيفة إلى المدين",
                duration = 3,
                unit = DurationUnit.MONTHS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 205 من قانون المرافعات المدنية والتجارية",
                notes = "يعتبر أمر الأداء والعريضة كأن لم يكن إذا لم يعلنا إلى المدين خلال ثلاثة أشهر تقويمية من تاريخ صدور الأمر.",
                summary = "وجوب إعلان أمر الأداء (3 أشهر)"
            ),
            LegalRule(
                id = 31,
                actionName = "تظلم المدين من أمر الأداء الصادر ضده",
                duration = 10,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 206 من قانون المرافعات المدنية والتجارية",
                notes = "للمدين التظلم من أمر الأداء خلال عشرة أيام من تاريخ إعلانه إليه، ويحصل بتكليف بالحضور مسبباً وإلا كان باطلاً.",
                summary = "تظلم من أمر الأداء"
            ),
            LegalRule(
                id = 32,
                actionName = "تقديم طلب أمر الأداء وصحة إجراءات الحجز إلى القاضي",
                duration = 8,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 210 من قانون المرافعات المدنية والتجارية",
                notes = "يجب على الدائن الحاجز تقديم طلب الأداء وصحة إجراءات الحجز خلال ثمانية أيام من توقيعه.",
                summary = "طلب الأداء وصحة الحجز"
            ),
            LegalRule(
                id = 33,
                actionName = "استئناف الأحكام الصادرة في الدعاوى الموضوعية (مدني وتجاري)",
                duration = 40,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 227 من قانون المرافعات المدنية والتجارية",
                notes = "ميعاد الاستئناف أربعون يوماً ما لم ينص القانون على غير ذلك، ويبدأ من اليوم التالي للنطق بالحكم الحضوري أو إعلانه. تضاف مواعيد المسافة.",
                summary = "استئناف مدني موضوعي"
            ),
            LegalRule(
                id = 34,
                actionName = "استئناف الأحكام الصادرة في المواد المستعجلة (مدني وتجاري)",
                duration = 15,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 227 فقرة 2 من قانون المرافعات المدنية والتجارية",
                notes = "ميعاد استئناف الأحكام في المواد المستعجلة 15 يوماً أياً كانت المحكمة المصدرة للحكم. تضاف مواعيد المسافة.",
                summary = "استئناف مستعجل مدني"
            ),
            LegalRule(
                id = 35,
                actionName = "استئناف النائب العام للأحكام المدنية والتجارية",
                duration = 60,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 227 فقرة 3 من قانون المرافعات المدنية والتجارية",
                notes = "ميعاد الاستئناف ستون يوماً بالنسبة للنائب العام أو من يقوم مقامه، ويبدأ من اليوم التالي لصدور الحكم.",
                summary = "استئناف النائب العام مدني"
            ),
            LegalRule(
                id = 36,
                actionName = "رفع دعوى صحة الحجز التحفظي وتثبيت الحق",
                duration = 8,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 230 من قانون المرافعات المدنية والتجارية",
                notes = "يجب على الحاجز رفع دعوى صحة الحجز وثبوت الحق خلال ثمانية أيام من تاريخ توقيع الحجز وإلا اعتبر كأن لم يكن.",
                summary = "دعوى صحة الحجز التحفظي"
            ),
            LegalRule(
                id = 37,
                actionName = "الطعن بالتماس إعادة النظر في الأحكام النهائية المدنية",
                duration = 40,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 242 من قانون المرافعات المدنية والتجارية",
                notes = "ميعاد الالتماس أربعون يوماً؛ ويبدأ في حالات الغش أو الورقة المزورة من اليوم التالي لانكشاف الغش أو ثبوت التزوير. تضاف مواعيد المسافة.",
                summary = "التماس إعادة نظر مدني"
            ),
            LegalRule(
                id = 38,
                actionName = "الطعن بطريق النقض في الأحكام المدنية والتجارية",
                duration = 60,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 252 من قانون المرافعات المدنية والتجارية",
                notes = "ميعاد الطعن بطريق النقض ستون يوماً من اليوم التالي لصدور الحكم الحضوري أو إعلانه بحسب الأحوال. تضاف مواعيد المسافة.",
                summary = "طعن بالنقض مدني"
            ),
            LegalRule(
                id = 39,
                actionName = "إيداع المطعون ضده مذكرة الدفاع في الطعن بالنقض المدني",
                duration = 15,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 258 من قانون المرافعات المدنية والتجارية",
                notes = "على المطعون ضده إيداع مذكرة بدفاعه مشفوعة بمستنداته في ميعاد 15 يوماً من إعلانه بصحيفة الطعن، وللطاعن 15 يوماً للرد.",
                summary = "مذكرة دفاع المطعون ضده بالنقض"
            ),
            LegalRule(
                id = 40,
                actionName = "إخطار محامي الخصوم بجلسة الطعن بالنقض المدني",
                duration = 15,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 264 من قانون المرافعات المدنية والتجارية",
                notes = "يخطر قلم الكتاب محامي الخصوم الذين أودعوا مذكراتهم بالجلسة المحددة قبل انعقادها بـ 15 يوماً على الأقل بكتاب موصى عليه.",
                summary = "إخطار جلسة النقض مدني"
            ),
            LegalRule(
                id = 41,
                actionName = "المهلة الواجبة بعد إعلان السند التنفيذي قبل بدء التنفيذ",
                duration = 1,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 281 من قانون المرافعات المدنية والتجارية",
                notes = "يجب أن يسبق التنفيذ إعلان السند التنفيذي للمدين وتكليفه بالوفاء، ولا يجوز إجراء التنفيذ إلا بعد مضي يوم كامل على الأقل من إعلان السند.",
                summary = "مهلة إعلان السند التنفيذي"
            ),
            LegalRule(
                id = 42,
                actionName = "مهلة التنفيذ قبل ورثة المدين بعد إعلانهم بالسند التنفيذي",
                duration = 8,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 284 من قانون المرافعات المدنية والتجارية",
                notes = "إذا توفي المدين قبل البدء في التنفيذ فلا يجوز التنفيذ قبل ورثته إلا بعد مضي ثمانية أيام من تاريخ إعلانهم بالسند التنفيذي.",
                summary = "تنفيذ قبل ورثة المدين"
            ),
            LegalRule(
                id = 43,
                actionName = "إعلان المدين بالعزم على التنفيذ الجبري قبل الرجوع على الغير",
                duration = 8,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 285 من قانون المرافعات المدنية والتجارية",
                notes = "لا يجوز للغير أن يؤدي المطلوب بموجب السند التنفيذي إلا بعد إعلان المدين بالعزم على التنفيذ قبل وقوعه بثمانية أيام على الأقل.",
                summary = "إعلان العزم على التنفيذ"
            ),
            LegalRule(
                id = 44,
                actionName = "المنازعة في اقتدار الكفيل أو الحارس القضائي",
                duration = 3,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 295 من قانون المرافعات المدنية والتجارية",
                notes = "لذوي الشأن خلال ثلاثة الأيام التالية للإعلان منازعة اقتدار الكفيل أو الحارس بتكليف الخصم بالحضور أمام قاضي التنفيذ.",
                summary = "منازعة اقتدار الكفيل"
            ),
            LegalRule(
                id = 45,
                actionName = "إعلان الحاجز محضر الحجز للمدين المحجوز عليه ودعوى صحته",
                duration = 8,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 320 من قانون المرافعات المدنية والتجارية",
                notes = "يجب إعلان محضر الحجز التحفظي والأمر الصادر به للمدين ورفع دعوى ثبوت الحق خلال ثمانية أيام من توقيعه وإلا اعتبر كأن لم يكن.",
                summary = "إعلان محضر الحجز ودعوى صحته"
            ),
            LegalRule(
                id = 46,
                actionName = "إبلاغ حجز ما للمدين لدى الغير للمحجوز عليه ودعوى صحة الحجز",
                duration = 8,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادتان 332 و333 من قانون المرافعات المدنية والتجارية",
                notes = "يجب إبلاغ الحجز للمحجوز عليه ورفع دعوى ثبوت الحق وصحة الحجز خلال ثمانية الأيام التالية لإعلانه للمحجوز لديه.",
                summary = "إبلاغ حجز ما للمدين لدى الغير"
            ),
            LegalRule(
                id = 47,
                actionName = "التقرير بما في الذمة من المحجوز لديه",
                duration = 15,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المواد 329 و337 و341 من قانون المرافعات المدنية والتجارية",
                notes = "يجب على المحجوز لديه التقرير بما في ذمته في قلم الكتاب خلال خمسة عشر يوماً من تاريخ إعلانه بالحجز أو تكليفه به.",
                summary = "تقرير بما في الذمة"
            ),
            LegalRule(
                id = 48,
                actionName = "انقضاء الحجز الإداري الموقع لدى المصالح والهيئات الحكومية",
                duration = 3,
                unit = DurationUnit.YEARS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 350 من قانون المرافعات المدنية والتجارية",
                notes = "ينقضي الحجز الواقع تحت يد إحدى المصالح الحكومية أو الهيئات العامة بمضي ثلاث سنوات ما لم يعلن استمراره.",
                summary = "انقضاء حجز الجهات الحكومية"
            ),
            LegalRule(
                id = 49,
                actionName = "المهلة المحددة لإجراء بيع المنقولات المحجوزة",
                duration = 8,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 376 من قانون المرافعات المدنية والتجارية",
                notes = "لا يجوز إجراء البيع إلا بعد مضي ثمانية أيام على الأقل من تسليم صورة محضر الحجز للمدين ومضي يوم على الأقل من اللصق والنشر.",
                summary = "مهلة بيع منقولات محجوزة"
            ),
            LegalRule(
                id = 50,
                actionName = "سقوط تسجيل تنبيه نزع الملكية وإيداع قائمة شروط البيع",
                duration = 90,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 414 من قانون المرافعات المدنية والتجارية",
                notes = "يودع مباشر الإجراءات قائمة شروط البيع خلال 90 يوماً من تاريخ تسجيل تنبيه نزع الملكية وإلا سقط تسجيل التنبيه واعتبر كأن لم يكن.",
                summary = "إيداع قائمة شروط بيع العقار"
            ),
            LegalRule(
                id = 51,
                actionName = "إخبار ذوي الشأن بإيداع قائمة شروط بيع العقار",
                duration = 15,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 417 من قانون المرافعات المدنية والتجارية",
                notes = "يجب على قلم الكتاب خلال 15 يوماً التالية لإيداع القائمة إخبار المدين والحائز والكفيل العيني والدائنين المقيدين.",
                summary = "إخبار إيداع قائمة شروط البيع"
            ),
            LegalRule(
                id = 52,
                actionName = "إخطار مكتب الشهر العقاري بإخبار قائمة شروط البيع للتأشير به",
                duration = 8,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 417 فقرة 2 من قانون المرافعات المدنية والتجارية",
                notes = "على المحضر إخطار مكتب الشهر العقاري بحصول الإخبار خلال ثمانية أيام للتأشير به على هامش تسجيل التنبيه.",
                summary = "إخطار الشهر العقاري بقائمة البيع"
            ),
            LegalRule(
                id = 53,
                actionName = "ميعاد جلسة الاعتراضات على قائمة شروط البيع العقاري",
                duration = 30,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 419 من قانون المرافعات المدنية والتجارية",
                notes = "تحدد جلسة نظر الاعتراضات في أول جلسة تحل بعد انقضاء ثلاثين يوماً من تاريخ انقضاء ميعاد الإخبار.",
                summary = "جلسة اعتراضات قائمة شروط البيع"
            ),
            LegalRule(
                id = 54,
                actionName = "إعلان إيداع قائمة شروط البيع بالنشر والتعليق بالمحكمة",
                duration = 8,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 421 من قانون المرافعات المدنية والتجارية",
                notes = "يعلن قلم الكتاب عن إيداع القائمة بالنشر والتعليق بلوحة المحكمة خلال ثمانية الأيام التالية لآخر إخبار.",
                summary = "نشر وتعليق قائمة شروط البيع"
            ),
            LegalRule(
                id = 55,
                actionName = "طلب تسجيل حكم إيقاع البيع العقاري",
                duration = 3,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 447 من قانون المرافعات المدنية والتجارية",
                notes = "يقوم قلم الكتاب بالنيابة عن ذوي الشأن بطلب تسجيل الحكم بإيقاع البيع العقاري خلال ثلاثة الأيام التالية لصدوره.",
                summary = "تسجيل حكم إيقاع البيع"
            ),
            LegalRule(
                id = 56,
                actionName = "الإنذار بالعزم على صرف المبالغ المودوعة خزانة المحكمة",
                duration = 3,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 491 من قانون المرافعات المدنية والتجارية",
                notes = "يجب إنذار ذوي الشأن بالعزم على الصرف بمهلة لا تقل عن ثلاثة أيام قبل اتخاذ إجراءات الصرف الفعلي.",
                summary = "إنذار صرف مبالغ مودوعة"
            ),
            LegalRule(
                id = 57,
                actionName = "المعارضة في أمر تقدير الرسوم القضائية النسبية والخدمات",
                duration = 8,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 17 من القانون رقم 90 لسنة 1944 بشأن الرسوم القضائية",
                notes = "يجوز للمدين بالرسوم المعارضة في مقدارها بتقرير في قلم الكتاب خلال ثمانية أيام من تاريخ الإعلان بالأمر.",
                summary = "معارضة تقدير الرسوم القضائية"
            ),
            LegalRule(
                id = 58,
                actionName = "استئناف الحكم الصادر في معارضة تقدير الرسوم القضائية",
                duration = 15,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 18 من القانون رقم 90 لسنة 1944 بشأن الرسوم القضائية",
                notes = "ميعاد استئناف الحكم الصادر في معارضة تقدير الرسوم القضائية خمسة عشر يوماً من النطق بالحكم.",
                summary = "استئناف معارضة الرسوم"
            ),

            // =========================================================================
            // ثانياً: مواعيد ومدد القانون المدني المصري (سقوط وتقادم)
            // =========================================================================
            LegalRule(
                id = 59,
                actionName = "سقوط الحق في طلب إبطال العقد للغبن والاستغلال",
                duration = 1,
                unit = DurationUnit.YEARS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 129 من القانون المدني المصري",
                notes = "يسقط الحق في إقامة دعوى إبطال العقد أو إنقاص الالتزامات للغبن بانقضاء سنة واحدة من تاريخ إبرام العقد.",
                summary = "إبطال العقد للغبن"
            ),
            LegalRule(
                id = 60,
                actionName = "سقوط دعوى البطلان المطلق للعقد بمضي المدة",
                duration = 15,
                unit = DurationUnit.YEARS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 141 من القانون المدني المصري",
                notes = "تسقط دعوى البطلان المطلق بمضي خمس عشرة سنة من وقت إبرام العقد.",
                summary = "سقوط دعوى البطلان المطلق"
            ),
            LegalRule(
                id = 61,
                actionName = "سقوط دعوى التعويض عن العمل غير المشروع (المسؤولية التقصيرية)",
                duration = 3,
                unit = DurationUnit.YEARS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 172 من القانون المدني المصري",
                notes = "تسقط دعوى التعويض بانقضاء ثلاث سنوات من يوم علم المضرور بالضرر والمسؤول عنه، وتسقط بـ 15 سنة مطلقاً.",
                summary = "تقادم دعوى التعويض التقصيري"
            ),
            LegalRule(
                id = 62,
                actionName = "سقوط دعوى الإثراء بلا سبب من تاريخ علم المضرور",
                duration = 3,
                unit = DurationUnit.YEARS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 180 من القانون المدني المصري",
                notes = "تسقط دعوى الإثراء بلا سبب بانقضاء ثلاث سنوات من تاريخ علم من لحقته الخسارة بحقه في التعويض.",
                summary = "دعوى الإثراء بلا سبب (علم)"
            ),
            LegalRule(
                id = 63,
                actionName = "سقوط دعوى الإثراء بلا سبب من تاريخ نشأة الحق",
                duration = 15,
                unit = DurationUnit.YEARS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 180 من القانون المدني المصري",
                notes = "تسقط دعوى الإثراء بلا سبب في جميع الأحوال بمضي خمس عشرة سنة من اليوم الذي ينشأ فيه الحق.",
                summary = "دعوى الإثراء بلا سبب (مطلق)"
            ),
            LegalRule(
                id = 64,
                actionName = "سقوط دعوى الفضالة من تاريخ علم رب العمل بحقه",
                duration = 3,
                unit = DurationUnit.YEARS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 197 من القانون المدني المصري",
                notes = "تسقط الدعوى الناشئة عن الفضالة بانقضاء ثلاث سنوات من اليوم الذي يعلم فيه رب العمل بحقه.",
                summary = "دعوى الفضالة (علم)"
            ),
            LegalRule(
                id = 65,
                actionName = "سقوط دعوى الفضالة من تاريخ نشأة الحق",
                duration = 15,
                unit = DurationUnit.YEARS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 197 من القانون المدني المصري",
                notes = "تسقط الدعوى الناشئة عن الفضالة في جميع الأحوال بمضي خمس عشرة سنة من يوم نشأة الحق.",
                summary = "دعوى الفضالة (مطلق)"
            ),
            LegalRule(
                id = 66,
                actionName = "سقوط دعوى عدم نفاذ التصرف (الدعوى البولصية) من تاريخ العلم",
                duration = 3,
                unit = DurationUnit.YEARS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 243 من القانون المدني المصري",
                notes = "تسقط دعوى عدم نفاذ التصرف بانقضاء ثلاث سنوات من اليوم الذي يعلم فيه الدائن بسبب عدم نفاذ التصرف.",
                summary = "دعوى عدم نفاذ التصرف (علم)"
            ),
            LegalRule(
                id = 67,
                actionName = "سقوط دعوى عدم نفاذ التصرف من تاريخ التصرف المطعون فيه",
                duration = 15,
                unit = DurationUnit.YEARS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 243 من القانون المدني المصري",
                notes = "تسقط دعوى عدم نفاذ التصرف في جميع الأحوال بمضي خمس عشرة سنة من وقت صدور التصرف المطعون فيه.",
                summary = "دعوى عدم نفاذ التصرف (مطلق)"
            ),
            LegalRule(
                id = 68,
                actionName = "المعارضة في الأحكام الصادرة في دعاوى الإعسار المدني",
                duration = 8,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 252 من القانون المدني المصري",
                notes = "مدة المعارضة في الأحكام الصادرة في شأن الإعسار ثمانية أيام تبدأ من تاريخ إعلان تلك الأحكام.",
                summary = "معارضة إعسار مدني"
            ),
            LegalRule(
                id = 69,
                actionName = "استئناف الأحكام الصادرة في دعاوى الإعسار المدني",
                duration = 15,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 252 من القانون المدني المصري",
                notes = "مدة استئناف الأحكام الصادرة في شأن الإعسار خمسة عشر يوماً تبدأ من تاريخ إعلان تلك الأحكام.",
                summary = "استئناف إعسار مدني"
            ),
            LegalRule(
                id = 70,
                actionName = "التظلم من أمر الحجز الموقع على إيرادات المدين المعسر",
                duration = 3,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 259 من القانون المدني المصري",
                notes = "للمدين التظلم من الحجز على إيراداته أمام قاضي الأمور المستعجلة خلال ثلاثة أيام من إعلانه بالحجز.",
                summary = "تظلم حجز إيراد المعسر"
            ),
            LegalRule(
                id = 71,
                actionName = "سقوط حق الدائن في الاعتراض على حوالة الدين",
                duration = 6,
                unit = DurationUnit.MONTHS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 322 من القانون المدني المصري",
                notes = "إذا أعذر المحال عليه الدائن بإقرار الحوالة ولم يعترض خلال ستة أشهر تقويمية اعتبر سكوته إقراراً بها.",
                summary = "اعتراض حوالة الدين (6 أشهر)"
            ),
            LegalRule(
                id = 72,
                actionName = "تقادم الحقوق الدورية المتجددة (أجرة، فوائد، مرتبات)",
                duration = 5,
                unit = DurationUnit.YEARS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 375 من القانون المدني المصري",
                notes = "يتقادم كل حق دوري متجدد كأجرة المباني والأراضي والفوائد الدورية بانقضاء خمس سنوات.",
                summary = "تقادم الحقوق الدورية المتجددة"
            ),
            LegalRule(
                id = 73,
                actionName = "تقادم حقوق المهنيين (أطباء، صيادلة، محامين، مهندسين، خبراء)",
                duration = 5,
                unit = DurationUnit.YEARS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 376 من القانون المدني المصري",
                notes = "تتقادم حقوق الأطباء والصيادلة والمحامين والمهندسين والخبراء عما أدوه من أعمال مهنية بانقضاء خمس سنوات.",
                summary = "تقادم أتعاب المهنيين"
            ),
            LegalRule(
                id = 74,
                actionName = "تقادم الضرائب والرسوم المستحقة للدولة والحق في استردادها",
                duration = 3,
                unit = DurationUnit.YEARS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 377 من القانون المدني المصري",
                notes = "تتقادم الضرائب والرسوم المستحقة للدولة والطعن فيها والحق في استرداد ما دفع بغير حق بانقضاء ثلاث سنوات.",
                summary = "تقادم الضرائب والرسوم"
            ),
            LegalRule(
                id = 75,
                actionName = "تقادم حقوق التجار والصناع عن الموردات لغير التجار",
                duration = 1,
                unit = DurationUnit.YEARS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 378 من القانون المدني المصري",
                notes = "تتقادم حقوق التجار والصناع عن أشياء وردوها لأشخاص لا يتجرون فيها وحقوق أصحاب الفنادق بانقضاء سنة واحدة.",
                summary = "التقادم الحولي للتجار والصناع"
            ),
            LegalRule(
                id = 76,
                actionName = "تقادم دعوى تكملة الثمن أو فسخ البيع بسبب الغبن في عقار القاصر",
                duration = 3,
                unit = DurationUnit.YEARS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 426 من القانون المدني المصري",
                notes = "تسقط دعوى تكملة الثمن بسبب الغبن الذي يزيد عن الخمس بانقضاء ثلاث سنوات من وقت بلوغ سن الرشد.",
                summary = "تكملة الثمن للغبن"
            ),
            LegalRule(
                id = 77,
                actionName = "سقوط دعوى ضمان العيب الخفي في المبيع",
                duration = 1,
                unit = DurationUnit.YEARS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 452 من القانون المدني المصري",
                notes = "تسقط دعوى الضمان بسنة واحدة من وقت تسليم المبيع ما لم يثبت تعمد البائع إخفاء العيب بغش منه.",
                summary = "ضمان عيوب المبيع"
            ),
            LegalRule(
                id = 78,
                actionName = "الضمان العشري لمهندسي ومقاولي البناء",
                duration = 10,
                unit = DurationUnit.YEARS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 651 من القانون المدني المصري",
                notes = "يضمن المهندس والمقاول بالتضامن ما يحدث خلال عشر سنوات من تهدم كلي أو جزئي للمبنى أو عيب يهدد سلامته.",
                summary = "الضمان العشري للمقاولات"
            ),
            LegalRule(
                id = 79,
                actionName = "تقادم دعوى التعويض عن الضمان العشري",
                duration = 3,
                unit = DurationUnit.YEARS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 654 من القانون المدني المصري",
                notes = "تسقط دعوى الضمان ضد المهندس والمقاول بانقضاء ثلاث سنوات من وقت حصول التهدم أو انكشاف العيب.",
                summary = "تقادم دعوى الضمان العشري"
            ),
            LegalRule(
                id = 80,
                actionName = "تقادم الدعاوى الناشئة عن عقد العمل الفردي مدنياً",
                duration = 1,
                unit = DurationUnit.YEARS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 698 من القانون المدني المصري",
                notes = "تسقط الدعاوى الناشئة عن عقد العمل بانقضاء سنة واحدة تبدأ من وقت انتهاء العقد.",
                summary = "تقادم دعاوى عقد العمل مدنياً"
            ),
            LegalRule(
                id = 81,
                actionName = "سقوط دعوى نقض القسمة بسبب الغبن",
                duration = 1,
                unit = DurationUnit.YEARS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 845 من القانون المدني المصري",
                notes = "تسقط دعوى نقض قسمة المال الشائع بسبب الغبن الذي يزيد عن الخمس بانقضاء سنة واحدة من تاريخ القسمة.",
                summary = "نقض القسمة للغبن"
            ),
            LegalRule(
                id = 82,
                actionName = "إعلان الرغبة في الأخذ بالشفعة من تاريخ الإنذار الرسمي",
                duration = 15,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 940 من القانون المدني المصري",
                notes = "يجب إعلان الرغبة في الشفعة رسمياً على يد محضر خلال 15 يوماً من تاريخ الإنذار الموجه من البائع أو المشتري.",
                summary = "إعلان رغبة الشفعة"
            ),
            LegalRule(
                id = 83,
                actionName = "إيداع الثمن الحقيقي للمبيع خزانة المحكمة في الشفعة",
                duration = 30,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 942 من القانون المدني المصري",
                notes = "يجب إيداع كامل الثمن الحقيقي خزانة المحكمة خلال ثلاثين يوماً من تاريخ إعلان الرغبة بالأخذ بالشفعة وإلا سقط الحق.",
                summary = "إيداع ثمن الشفعة"
            ),
            LegalRule(
                id = 84,
                actionName = "رفع دعوى الشفعة وقيدها بجدول المحكمة المختصة",
                duration = 30,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 943 من القانون المدني المصري",
                notes = "ترفع دعوى الشفعة على البائع والمشتري وتقيد بالجدول في ميعاد ثلاثين يوماً من تاريخ إعلان الرغبة بالأخذ بالشفعة.",
                summary = "رفع دعوى الشفعة"
            ),
            LegalRule(
                id = 85,
                actionName = "سقوط دعوى استرداد الحيازة المسلوبة بالقوة أو الغصب",
                duration = 1,
                unit = DurationUnit.YEARS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 958 من القانون المدني المصري",
                notes = "تسقط دعوى استرداد الحيازة بمضي سنة واحدة من وقت فقد الحيازة أو من وقت كشف الغصب.",
                summary = "دعوى استرداد الحيازة"
            ),
            LegalRule(
                id = 86,
                actionName = "سقوط دعوى منع التعرض في الحيازة",
                duration = 1,
                unit = DurationUnit.YEARS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 961 من القانون المدني المصري",
                notes = "تسقط دعوى منع التعرض للحائز بمضي سنة واحدة تبدأ من تاريخ وقوع التعرض لحيازته.",
                summary = "دعوى منع التعرض"
            ),
            LegalRule(
                id = 87,
                actionName = "التقادم المكسب للملكية الطويل (الحيازة المجردة للعقار)",
                duration = 15,
                unit = DurationUnit.YEARS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 968 من القانون المدني المصري",
                notes = "يكسب الحائز ملكية العقار إذا استمرت حيازته الهادئة والعلنية دون انقطاع مدة خمس عشرة سنة.",
                summary = "التقادم المكسب الطويل (15 سنة)"
            ),
            LegalRule(
                id = 88,
                actionName = "التقادم المكسب للملكية القصير (بحسن نية وسبب صحيح)",
                duration = 5,
                unit = DurationUnit.YEARS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 969 من القانون المدني المصري",
                notes = "يكسب ملكية العقار من حازه بحسن نية ومستنداً إلى سبب صحيح مدة خمس سنوات مستمرة.",
                summary = "التقادم المكسب القصير (5 سنوات)"
            ),
            LegalRule(
                id = 89,
                actionName = "التقادم المكسب للحقوق العينية العقارية المورثة أو الوقف",
                duration = 33,
                unit = DurationUnit.YEARS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 970 من القانون المدني المصري",
                notes = "لا تكسب الحقوق العينية العقارية المورثة بالتقادم إلا إذا دامت الحيازة مدة ثلاث وثلاثين سنة.",
                summary = "تقادم أعيان الإرث والوقف (33 سنة)"
            ),
            LegalRule(
                id = 90,
                actionName = "طلب الدائن بيع العقار المرهون المطلوب تطهيره بالمزاد",
                duration = 30,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 1067 من القانون المدني المصري",
                notes = "يجوز للدائن المرتهن أو الكفيل طلب بيع العقار المطلوب تطهيره بالمزاد خلال 30 يوماً من آخر إعلان رسمي.",
                summary = "طلب بيع العقار المطلوب تطهيره"
            ),
            LegalRule(
                id = 91,
                actionName = "إعلان الدائن المباشر للإجراءات بتخلية العقار المرهون",
                duration = 5,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 1071 من القانون المدني المصري",
                notes = "تكون تخلية العقار بتقرير الحائز، ويجب إعلان الدائن المباشر بهذه التخلية في خلال 5 أيام من وقت التقرير.",
                summary = "إعلان تخلية العقار المرهون"
            ),

            // =========================================================================
            // ثالثاً: قانون الإجراءات الجنائية والطعون الجنائية (محدث بالقانون 1 لسنة 2024)
            // =========================================================================
            LegalRule(
                id = 92,
                actionName = "ميعاد تقديم الشكوى في الجرائم المعلقة على شكوى المجني عليه",
                duration = 3,
                unit = DurationUnit.MONTHS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 3 من قانون الإجراءات الجنائية رقم 150 لسنة 1950",
                notes = "لا تقبل الشكوى بعد مضي ثلاثة أشهر تقويمية من يوم علم المجني عليه بالجريمة وبمرتكبها ما لم ينص القانون على غير ذلك.",
                summary = "شكوى الجرائم المعلقة (3 أشهر)"
            ),
            LegalRule(
                id = 93,
                actionName = "انقضاء الدعوى الجنائية بمضي المدة في مواد الجنايات",
                duration = 10,
                unit = DurationUnit.YEARS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 15 من قانون الإجراءات الجنائية",
                notes = "تنقضي الدعوى الجنائية في مواد الجنايات بمضي عشر سنين من يوم وقوع الجريمة (مع مراعاة الجرائم المستثناة بنص الدستور).",
                summary = "تقادم الجنايات (10 سنوات)"
            ),
            LegalRule(
                id = 94,
                actionName = "انقضاء الدعوى الجنائية بمضي المدة في مواد الجنح",
                duration = 3,
                unit = DurationUnit.YEARS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 15 من قانون الإجراءات الجنائية",
                notes = "تنقضي الدعوى الجنائية في مواد الجنح بمضي ثلاث سنين من يوم وقوع الجريمة.",
                summary = "تقادم الجنح (3 سنوات)"
            ),
            LegalRule(
                id = 95,
                actionName = "انقضاء الدعوى الجنائية بمضي المدة في مواد المخالفات",
                duration = 1,
                unit = DurationUnit.YEARS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 15 من قانون الإجراءات الجنائية",
                notes = "تنقضي الدعوى الجنائية في مواد المخالفات بمضي سنة واحدة من يوم وقوع المخالفة.",
                summary = "تقادم المخالفات (سنة واحدة)"
            ),
            LegalRule(
                id = 96,
                actionName = "إرسال المتهم المقبوض عليه للنيابة العامة وسماع أقواله",
                duration = 1,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 36 من قانون الإجراءات الجنائية",
                notes = "يرسل مأمور الضبط المتهم للنيابة في مدى 24 ساعة، وتستجوبه النيابة خلال 24 ساعة وتأمر بحبسه احتياطياً أو إطلاقه.",
                summary = "إرسال المتهم واستجوابه"
            ),
            LegalRule(
                id = 97,
                actionName = "ميعاد فصل قاضي التحقيق في الدفوع والطلبات وإعلان أوامره",
                duration = 1,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادتان 82 و83 من قانون الإجراءات الجنائية",
                notes = "يفصل قاضي التحقيق في ظرف 24 ساعة في الطلبات والدفوع، وتعلن أوامره للخصوم خلال 24 ساعة من تاريخ صدورها.",
                summary = "فصل وإعلان قرارات قاضي التحقيق"
            ),
            LegalRule(
                id = 98,
                actionName = "فصل قاضي التحقيق في طلبات رد الخصوم للخبير جنائياً",
                duration = 3,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 89 من قانون الإجراءات الجنائية",
                notes = "يفصل قاضي التحقيق في طلب رد الخبير المقدم من الخصوم خلال ثلاثة أيام من تاريخ تقديمه.",
                summary = "رد الخبير جنائياً"
            ),
            LegalRule(
                id = 99,
                actionName = "أمر مراقبة وتسجيل الاتصالات الصادر من جهات التحقيق",
                duration = 30,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادتان 95 و206 من قانون الإجراءات الجنائية",
                notes = "يكون أمر ضبط الخطابات ومراقبة المحادثات مسبباً ولمدة لا تزيد على 30 يوماً قابلة للتجديد لمدد مماثلة.",
                summary = "أمر مراقبة وتسجيل الاتصالات"
            ),
            LegalRule(
                id = 100,
                actionName = "سقوط الحق في طلب استرداد الأشياء المضبوطة في الدعوى الجنائية",
                duration = 3,
                unit = DurationUnit.YEARS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 108 من قانون الإجراءات الجنائية",
                notes = "تسقط المطالبة باسترداد الأشياء المضبوطة بمضي ثلاث سنوات من تاريخ انتهاء الدعوى الجنائية وتصبح ملكاً للحكومة.",
                summary = "استرداد المضبوطات الجنائية"
            ),
            LegalRule(
                id = 101,
                actionName = "تقديم المتهم بيان الأدلة في جرائم القذف بطريق النشر",
                duration = 5,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 123 من قانون الإجراءات الجنائية",
                notes = "يقدم المتهم بيان الأدلة للمحقق عند أول استجواب أو في الخمسة أيام التالية له وإلا سقط حقه في إقامتها.",
                summary = "بيان أدلة القذف بالنشر"
            ),
            LegalRule(
                id = 102,
                actionName = "سقوط أمر الضبط والإحضار الصادر ضد المتهم بمضي المدة",
                duration = 6,
                unit = DurationUnit.MONTHS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 139 من قانون الإجراءات الجنائية",
                notes = "لا يبقى أمر الضبط والإحضار نافذ المفعول بعد مضي ستة أشهر تقويمية من تاريخ صدوره ما لم يعتمده القاضي لمدة أخرى.",
                summary = "سقوط أمر الضبط والإحضار (6 أشهر)"
            ),
            LegalRule(
                id = 103,
                actionName = "وجوب الإفراج الحتمي عن المتهم المقبوض عليه في مواد الجنح",
                duration = 8,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 142 فقرة 2 من قانون الإجراءات الجنائية",
                notes = "يجب الإفراج حتماً عن المتهم في الجنح بعد 8 أيام من استجوابه إذا كان له موطن معروف والحد الأقصى للعقوبة لا يجاوز سنة.",
                summary = "إفراج حتمي في الجنح"
            ),
            LegalRule(
                id = 104,
                actionName = "أمر الحبس الاحتياطي الصادر ابتداءً من النيابة العامة",
                duration = 4,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 201 من قانون الإجراءات الجنائية",
                notes = "الأمر بالحبس الاحتياطي الصادر من النيابة لا يكون نافذاً إلا لمدة الأربعة أيام التالية للقبض على المتهم أو تسليمه إليها.",
                summary = "حبس النيابة العامة الاحتياطي"
            ),
            LegalRule(
                id = 105,
                actionName = "تجديد الحبس الاحتياطي أمام القاضي الجزئي",
                duration = 15,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادتان 142 و202 من قانون الإجراءات الجنائية",
                notes = "للقاضي الجزئي مد الحبس الاحتياطي لمدد متعاقبة لا تزيد كل منها على 15 يوماً وبحيث لا يجاوز مجموعها 45 يوماً.",
                summary = "تجديد حبس القاضي الجزئي"
            ),
            LegalRule(
                id = 106,
                actionName = "تجديد الحبس الاحتياطي أمام محكمة الجنح المستأنفة بغرفة المشورة",
                duration = 45,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 143 من قانون الإجراءات الجنائية",
                notes = "تصدر غرفة المشورة أمرها بمد الحبس لمدد متعاقبة لا تزيد كل منها على 45 يوماً وفق الحدود القصوى المنصوص عليها قانوناً.",
                summary = "تجديد الحبس بغرفة المشورة"
            ),
            LegalRule(
                id = 107,
                actionName = "استئناف أوامر قاضي التحقيق الصادرة في التحقيق الابتدائي",
                duration = 10,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 166 من قانون الإجراءات الجنائية",
                notes = "ميعاد استئناف أوامر قاضي التحقيق بعدم وجود وجه لإقامة الدعوى عشرة أيام من تاريخ صدور الأمر أو إعلانه.",
                summary = "استئناف أوامر قاضي التحقيق"
            ),
            LegalRule(
                id = 108,
                actionName = "استئناف النيابة العامة لأمر الإفراج المؤقت عن المحبوس احتياطياً",
                duration = 1,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 166 فقرة أخيرة من قانون الإجراءات الجنائية",
                notes = "ميعاد استئناف النيابة العامة لأمر الإفراج المؤقت أربع وعشرون ساعة (يوم واحد) من وقت صدوره.",
                summary = "استئناف النيابة لأمر الإفراج"
            ),
            LegalRule(
                id = 109,
                actionName = "فصل غرفة المشورة في استئناف أمر الإفراج عن المتهم",
                duration = 3,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 168 من قانون الإجراءات الجنائية",
                notes = "إذا لم يفصل في استئناف النيابة لأمر الإفراج خلال ثلاثة أيام من تاريخ التقرير به وجب تنفيذ الإفراج فوراً.",
                summary = "فصل في استئناف أمر الإفراج"
            ),
            LegalRule(
                id = 110,
                actionName = "فصل النيابة في الادعاء بالحق المدني والطعن في قرار رفضه",
                duration = 3,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 199 مكرر من قانون الإجراءات الجنائية",
                notes = "تفصل النيابة في قبول الادعاء خلال 3 أيام، والطعن في الرفض أمام غرفة المشورة خلال 3 أيام من الإعلان.",
                summary = "ادعاء مدني وطعن في رفضه"
            ),
            LegalRule(
                id = 111,
                actionName = "عرض أمر المنع من التصرف على المحكمة الجنائية المختصة",
                duration = 7,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 208 مكرر (أ) من قانون الإجراءات الجنائية",
                notes = "يعرض النائب العام أمر المنع الوقتي من التصرف على المحكمة الجنائية خلال سبعة أيام على الأكثر وإلا اعتبر كأن لم يكن.",
                summary = "عرض أمر المنع من التصرف"
            ),
            LegalRule(
                id = 112,
                actionName = "فصل المحكمة الجنائية في أمر المنع من التصرف أو التظلم منه",
                duration = 15,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادتان 208 مكرر (أ) و208 مكرر (ب) من قانون الإجراءات الجنائية",
                notes = "تصدر المحكمة الجنائية حكمها في أمر المنع أو التظلم منه خلال مدة لا تجاوز خمسة عشر يوماً من تاريخ العرض.",
                summary = "فصل في أمر المنع من التصرف"
            ),
            LegalRule(
                id = 113,
                actionName = "طعن المدعي بالحقوق المدنية في أمر ألا وجه لإقامة الدعوى",
                duration = 10,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 210 من قانون الإجراءات الجنائية",
                notes = "للمدعي المدني الطعن في أمر حفظ النيابة (ألا وجه) بتقرير بقلم الكتاب خلال عشرة أيام من تاريخ إعلانه بالأمر.",
                summary = "طعن المدعي المدني في قرار ألا وجه"
            ),
            LegalRule(
                id = 114,
                actionName = "إعلان النيابة العامة للخصوم بأمر الإحالة لمحكمة الجنايات",
                duration = 10,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 214 فقرة 2 من قانون الإجراءات الجنائية",
                notes = "تعلن النيابة الخصوم بالأمر الصادر بالإحالة للجنايات وقائمة أدلة الثبوت خلال العشرة أيام التالية لصدوره.",
                summary = "إعلان أمر الإحالة للجنايات"
            ),
            LegalRule(
                id = 115,
                actionName = "أجل اطلاع محامي المتهم بجناية على ملف القضية",
                duration = 10,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 214 مكرر (أ) من قانون الإجراءات الجنائية",
                notes = "يحدد رئيس محكمة الاستئناف لمحامي المتهم أجلاً للاطلاع على الملف لا يجاوز عشرة أيام يبقى خلالها الملف بقلم الكتاب.",
                summary = "اطلاع محامي الجناية على الملف"
            ),
            LegalRule(
                id = 116,
                actionName = "ميعاد الحضور وتكليف الخصوم في مواد الجنح",
                duration = 3,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 233 من قانون الإجراءات الجنائية",
                notes = "يكون تكليف الخصوم بالحضور أمام محكمة الجنح بثلاثة أيام كاملة على الأقل قبل انعقاد الجلسة غير مواعيد المسافة.",
                summary = "حضور أمام محكمة الجنح"
            ),
            LegalRule(
                id = 117,
                actionName = "ميعاد الحضور وتكليف الخصوم في مواد المخالفات",
                duration = 1,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 233 من قانون الإجراءات الجنائية",
                notes = "يكون تكليف الخصوم بالحضور في المخالفات بيوم كامل قبل انعقاد الجلسة غير مواعيد مسافة الطريق.",
                summary = "حضور أمام محكمة المخالفات"
            ),
            LegalRule(
                id = 118,
                actionName = "ميعاد تكليف الشهود بالحضور في مواد الجنح والجنايات",
                duration = 1,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 277 من قانون الإجراءات الجنائية",
                notes = "يكلف الشهود بالحضور قبل الجلسة بـ 24 ساعة (يوم كامل) غير مواعيد المسافة، إلا في حالة التلبس فيكلفون في أي وقت.",
                summary = "تكليف الشهود بالحضور جنائياً"
            ),
            LegalRule(
                id = 119,
                actionName = "ميعاد تكليف المتهم والشهود بالحضور أمام محكمة الجنايات",
                duration = 8,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 374 من قانون الإجراءات الجنائية",
                notes = "يكون تكليف المتهم والشهود بالحضور أمام الجنايات قبل انعقاد الجلسة بثمانية أيام كاملة على الأقل.",
                summary = "حضور محكمة الجنايات"
            ),
            LegalRule(
                id = 120,
                actionName = "ورود رأي مفتي الجمهورية في القضايا المحكوم فيها بالإعدام",
                duration = 10,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 381 فقرة 2 من قانون الإجراءات الجنائية",
                notes = "تلتزم محكمة الجنايات بإرسال الأوراق للمفتي، فإذا لم يصل رأيه للمحكمة خلال 10 أيام تالية لإرسالها حكمت في الدعوى.",
                summary = "رأي المفتي في حكم الإعدام"
            ),
            LegalRule(
                id = 121,
                actionName = "الطعن بالمعارضة في الأحكام الغيابية (جنح ومخالفات)",
                duration = 10,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 398 من قانون الإجراءات الجنائية",
                notes = "تقبل المعارضة في الأحكام الغيابية الصادرة في الجنح والمخالفات خلال عشرة أيام تالية لإعلان الحكم للمحكوم عليه.",
                summary = "معارضة جنح ومخالفات"
            ),
            LegalRule(
                id = 122,
                actionName = "استئناف المتهم والمدعي المدني للأحكام الصادرة في الجنح والمخالفات",
                duration = 10,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 406 فقرة 1 من قانون الإجراءات الجنائية",
                notes = "يحصل الاستئناف بتقرير بقلم الكتاب في ظرف عشرة أيام من تاريخ النطق بالحكم الحضوري أو إعلانه. تضاف مواعيد المسافة.",
                summary = "استئناف جنح ومخالفات"
            ),
            LegalRule(
                id = 123,
                actionName = "استئناف النائب العام للأحكام الصادرة في الجنح والمخالفات",
                duration = 30,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 406 فقرة 2 من قانون الإجراءات الجنائية",
                notes = "للنائب العام أو المحامي العام الاستئناف في ميعاد ثلاثين يوماً من اليوم التالي لوقت صدور الحكم.",
                summary = "استئناف النائب العام جنائي"
            ),
            LegalRule(
                id = 124,
                actionName = "امتداد ميعاد الاستئناف الجنائي لباقي الخصوم",
                duration = 5,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 409 من قانون الإجراءات الجنائية",
                notes = "إذا استأنف أحد الخصوم في مدة العشرة أيام، يمتد ميعاد الاستئناف لمن له حق الاستئناف من باقي الخصوم خمسة أيام تالية.",
                summary = "امتداد استئناف جنائي"
            ),
            LegalRule(
                id = 125,
                actionName = "الطعن بالاستئناف في الأحكام الصادرة من محكمة جنايات أول درجة",
                duration = 40,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 419 مكرراً (4) من قانون الإجراءات الجنائية (المضافة بالقانون رقم 1 لسنة 2024)",
                notes = "يحصل الاستئناف بتقرير في قلم كتاب المحكمة التي أصدرت الحكم في ظرف أربعين يوماً من تاريخ النطق بالحكم الحضوري، وللنائب العام الاستئناف خلال 40 يوماً أيضاً.",
                summary = "استئناف أحكام الجنايات (40 يوماً)"
            ),
            LegalRule(
                id = 126,
                actionName = "إعلان النيابة العامة للخصوم بجلسة إعادة النظر أمام محكمة النقض",
                duration = 3,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 445 من قانون الإجراءات الجنائية",
                notes = "تعلن النيابة الخصوم للجلسة التي تحدد لنظر طلب إعادة النظر أمام النقض قبل انعقادها بثلاثة أيام كاملة على الأقل.",
                summary = "إعلان جلسة إعادة النظر بالنقض"
            ),
            LegalRule(
                id = 127,
                actionName = "ميعاد تنفيذ حكم الإعدام بعد رفع الأوراق لرئيس الجمهورية",
                duration = 14,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 470 من قانون الإجراءات الجنائية",
                notes = "ترفع أوراق الحكم البات فوراً للرئيس وينفذ الحكم إذا لم يصدر عفو أو إبدال للعقوبة خلال أربعة عشر يوماً.",
                summary = "تنفيذ حكم الإعدام"
            ),
            LegalRule(
                id = 128,
                actionName = "التقرير بالطعن بالنقض الجنائي وإيداع الأسباب",
                duration = 60,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 34 من القانون رقم 57 لسنة 1959 (المعدل بالقانون 74 لسنة 2007)",
                notes = "ميعاد التقرير بالطعن بالنقض وإيداع مذكرة الأسباب ستون يوماً من تاريخ الحكم الحضوري أو انقضاء ميعاد المعارضة.",
                summary = "طعن بالنقض جنائي (60 يوماً)"
            ),

            // =========================================================================
            // رابعاً: قوانين الأحوال الشخصية ومحاكم الأسرة
            // =========================================================================
            LegalRule(
                id = 129,
                actionName = "توثيق المطلق لإشهاد الطلاق لدى الموثق المختص",
                duration = 30,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 5 مكرر من المرسوم بقانون رقم 25 لسنة 1929 المعدل بالقانون 100 لسنة 1985",
                notes = "على المطلق توثيق إشهاد طلاقه لدى الموثق خلال ثلاثين يوماً من إيقاع الطلاق، وتعتبر الزوجة عالمة به بحضورها أو بإعلانها.",
                summary = "توثيق إشهاد الطلاق"
            ),
            LegalRule(
                id = 130,
                actionName = "اعتراض الزوجة على إنذار الطاعة أمام محكمة الأسرة",
                duration = 30,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 11 مكرر ثانياً من القانون 25 لسنة 1929 المعدل بالقانون 100 لسنة 1985",
                notes = "للزوجة حق الاعتراض على إنذار الطاعة خلال ثلاثين يوماً من تاريخ إعلانها بالإنذار وتبين أوجه اعتراضها.",
                summary = "اعتراض على إنذار طاعة"
            ),
            LegalRule(
                id = 131,
                actionName = "ميعاد التطليق لحبس الزوج تنفيذاً لحكم نهائي بعقوبة مقيدة للحرية",
                duration = 3,
                unit = DurationUnit.YEARS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 14 من القانون رقم 25 لسنة 1929 المعدل بالقانون 100 لسنة 1985",
                notes = "يجوز لزوجة المحبوس بحكم نهائي بعقوبة 3 سنوات فأكثر طلب التطليق بعد مضي سنة كاملة من حبسه.",
                summary = "تطليق لحبس الزوج (3 سنوات فأكثر)"
            ),
            LegalRule(
                id = 132,
                actionName = "تقادم دعوى نفي النسب",
                duration = 1,
                unit = DurationUnit.YEARS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 15 من القانون رقم 25 لسنة 1929",
                notes = "تسقط دعوى نفي النسب بمضي سنة واحدة من تاريخ الولادة أو تاريخ العلم بها.",
                summary = "تقادم دعوى نفي النسب"
            ),
            LegalRule(
                id = 133,
                actionName = "تقادم دعوى نفقة الزوجية عن المدة الماضية",
                duration = 1,
                unit = DurationUnit.YEARS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 17 من القانون رقم 25 لسنة 1929",
                notes = "لا تسمع دعوى النفقة عن مدة ماضية لأكثر من سنة نهايتها تاريخ رفع الدعوى القضائية.",
                summary = "تقادم متجمد نفقة الزوجية"
            ),
            LegalRule(
                id = 134,
                actionName = "سن انتهاء حق حضانة النساء للصغير والصغيرة",
                duration = 15,
                unit = DurationUnit.YEARS,
                distanceDays = 0,
                startRule = StartRule.SAME_DAY,
                lawArticle = "المادة 20 من القانون رقم 25 لسنة 1929 المعدل بالقانون رقم 4 لسنة 2005",
                notes = "ينتهي حق حضانة النساء ببلوغ الصغير أو الصغيرة سن الخامسة عشرة ويخير القاضي المحضون بعدها.",
                summary = "سن انتهاء الحضانة (15 سنة)"
            ),
            LegalRule(
                id = 135,
                actionName = "الحكم بموت المفقود في الحالات العادية بعد انقطاع أخباره",
                duration = 4,
                unit = DurationUnit.YEARS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 21 من القانون رقم 25 لسنة 1929 المعدل بالقانون رقم 140 لسنة 2017",
                notes = "يحكم بموت المفقود بعد مضي 4 سنوات من تاريخ فقده إذا غلب هلاكه، وتتقلص المدة إلى 30 يوماً في حالات الكوارث والحرب بقرار وزاري.",
                summary = "وفاة المفقود (4 سنوات / 30 يوماً)"
            ),
            LegalRule(
                id = 136,
                actionName = "سن ثبوت أهلية التقاضي في مسائل الأحوال الشخصية",
                duration = 15,
                unit = DurationUnit.YEARS,
                distanceDays = 0,
                startRule = StartRule.SAME_DAY,
                lawArticle = "المادة 2 من القانون رقم 1 لسنة 2000",
                notes = "تثبت أهلية التقاضي في مسائل الأحوال الشخصية لكل من بلغ خمس عشرة سنة ميلادية كاملة متمتعاً بقواه العقلية.",
                summary = "أهلية التقاضي للأحوال الشخصية"
            ),
            LegalRule(
                id = 137,
                actionName = "ميعاد تقديم الأخصائي الاجتماعي لتقريره لمحكمة الأسرة",
                duration = 14,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 4 من القانون رقم 1 لسنة 2000",
                notes = "يقدم الخبيران الاجتماعي والنفسي تقريرهما للمحكمة خلال مهلة لا تجاوز أربعة عشر يوماً من تاريخ التكليف.",
                summary = "تقرير الأخصائي الاجتماعي"
            ),
            LegalRule(
                id = 138,
                actionName = "عدم سماع دعوى أعيان الوقف والإرث عند الإنكار بمضي المدة",
                duration = 33,
                unit = DurationUnit.YEARS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 8 من القانون رقم 1 لسنة 2000",
                notes = "لا تسمع الدعوى عند الإنكار في حق الوقف أو الإرث بمضي ثلاث وثلاثين سنة بغير عذر شرعي.",
                summary = "عدم سماع دعوى الإرث والوقف"
            ),
            LegalRule(
                id = 139,
                actionName = "المهلة الفاصلة بين جلستي عرض الصلح في دعاوى التطليق لوجود أطفال",
                duration = 30,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 18 فقرة 2 من القانون رقم 1 لسنة 2000",
                notes = "تلتزم المحكمة بعرض الصلح مرتين تفصل بينهما مدة لا تقل عن ثلاثين يوماً ولا تزيد على ستين يوماً إذا كان للزوجين أطفال.",
                summary = "مهلة عرض الصلح في التطليق"
            ),
            LegalRule(
                id = 140,
                actionName = "ميعاد إعلان الزوج بمراجعته للزوجة المطلقة رجعياً",
                duration = 60,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 22 من القانون رقم 1 لسنة 2000",
                notes = "يجب على الزوج إعلان مطلقته بمراجعته لها لشخصها أو في موطنها خلال ستين يوماً من توثيق الرجعة وإلا سقطت آثارها.",
                summary = "إعلان مراجعة المطلقة"
            ),
            LegalRule(
                id = 141,
                actionName = "إيداع الأحكام الصادرة في مواد الحجر والمساعد القضائي والولاية والغيبة",
                duration = 8,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 53 من القانون رقم 1 لسنة 2000",
                notes = "يودع قلم الكتاب أصل الحكم في مواد الحجر أو الولاية أو الغيبة خلال ثمانية أيام من النطق به ويخطر به النيابة.",
                summary = "إيداع أحكام الحجر والولاية"
            ),
            LegalRule(
                id = 142,
                actionName = "إنهاء تسوية المنازعات الأسرية وإحالة الدعوى للمحكمة",
                duration = 15,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 8 من القانون رقم 10 لسنة 2004 بإصدار قانون محاكم الأسرة",
                notes = "يجب أن تنتهي التسوية بمكتب تسوية المنازعات الأسرية خلال 15 يوماً من تقديم الطلب، وللأطراف بعدها اللجوء للمحكمة.",
                summary = "تسوية المنازعات الأسرية"
            ),

            // =========================================================================
            // خامساً: قضاء مجلس الدولة (الإلغاء والطعون الإدارية والتأديبية)
            // =========================================================================
            LegalRule(
                id = 143,
                actionName = "رفع دعوى الإلغاء أمام محاكم مجلس الدولة ضد القرارات الإدارية",
                duration = 60,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 24 من قانون مجلس الدولة رقم 47 لسنة 1972",
                notes = "ميعاد رفع دعوى الإلغاء ستون يوماً من تاريخ نشر القرار الإداري أو إعلانه لذوي الشأن أو العلم اليقيني به.",
                summary = "دعوى إلغاء قرار إداري"
            ),
            LegalRule(
                id = 144,
                actionName = "التظلم الإداري من القرار وميعاد البت فيه قبل دعوى الإلغاء",
                duration = 60,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 24 فقرة 2 من قانون مجلس الدولة رقم 47 لسنة 1972",
                notes = "ينقطع الميعاد بالتظلم؛ وتنتظر الجهة 60 يوماً، ويعتبر فواتها دون إجابة رفضاً، ليبدأ ميعاد جديد مقداره 60 يوماً لرفع الدعوى.",
                summary = "تظلم إداري قاطع للميعاد"
            ),
            LegalRule(
                id = 145,
                actionName = "الطعن أمام محكمة القضاء الإداري في أحكام المحاكم الإدارية",
                duration = 60,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 44 فقرة 1 من قانون مجلس الدولة رقم 47 لسنة 1972",
                notes = "ميعاد الطعن أمام محكمة القضاء الإداري في الأحكام الصادرة من المحاكم الإدارية ستون يوماً من تاريخ صدور الحكم.",
                summary = "استئناف أحكام المحاكم الإدارية"
            ),
            LegalRule(
                id = 146,
                actionName = "الطعن أمام المحكمة الإدارية العليا في أحكام القضاء الإداري والتأديبي",
                duration = 60,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 44 من قانون مجلس الدولة رقم 47 لسنة 1972",
                notes = "ميعاد الطعن أمام المحكمة الإدارية العليا ستون يوماً من تاريخ صدور الحكم المطعون فيه بتقرير يودع قلم الكتاب.",
                summary = "طعن إدارية عليا (60 يوماً)"
            ),

            // =========================================================================
            // سادساً: تشريعات العمل (القانون 12 لسنة 2003 والقواعد المحدثة)
            // =========================================================================
            LegalRule(
                id = 147,
                actionName = "قيد العامل غير المسجل بمديرية القوى العاملة المختصة",
                duration = 15,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 14 من قانون العمل رقم 12 لسنة 2003",
                notes = "إذا لم يكن العامل المعين حاصلاً على شهادة القيد وجب على صاحب العمل قيده بالجهة الإدارية خلال 15 يوماً من إلحاقه بالعمل.",
                summary = "قيد عامل جديد بالجهة الإدارية"
            ),
            LegalRule(
                id = 148,
                actionName = "اعتراض وزارة القوى العاملة على عقود العمل وتراخيص التشغيل بالخارج",
                duration = 10,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 20 من قانون العمل رقم 12 لسنة 2003",
                notes = "للوزارة خلال عشرة أيام من إخطارها بعقود العمل بالخارج الاعتراض عليها لعدم مناسبة الأجر أو مخالفتها للنظام العام.",
                summary = "اعتراض القوى العاملة على عقود الخارج"
            ),
            LegalRule(
                id = 149,
                actionName = "أداء مستحقات العامل الذي ترك العمل من تلقاء نفسه (المستقيل)",
                duration = 7,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 38 من قانون العمل رقم 12 لسنة 2003",
                notes = "إذا ترك العامل العمل من تلقاء نفسه وجب على صاحب العمل أداء أجره وجميع مستحقاته في مدة لا تجاوز سبعة أيام من تاريخ المطالبة بها.",
                summary = "أداء مستحقات العامل المستقيل"
            ),
            LegalRule(
                id = 150,
                actionName = "إخطار العامل لصاحب العمل بموعد إجازة أداء الامتحانات الدراسية",
                duration = 15,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 49 من قانون العمل رقم 12 لسنة 2003",
                notes = "للعامل الحق في تحديد موعد إجازته لأداء الامتحانات بشرط إخطار صاحب العمل قبل قيامه بها بخمسة عشر يوماً على الأقل.",
                summary = "إخطار إجازة الامتحانات"
            ),
            LegalRule(
                id = 151,
                actionName = "بدء التحقيق التأديبي مع العامل في المخالفات المنسوبة إليه",
                duration = 7,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 64 من قانون العمل رقم 12 لسنة 2003",
                notes = "يحظر توقيع جزاء على العامل إلا بعد التحقيق معه، على أن يبدأ التحقيق خلال سبعة أيام على الأكثر من تاريخ اكتشاف المخالفة أو العلم بها.",
                summary = "بدء التحقيق التأديبي مع العامل"
            ),
            LegalRule(
                id = 152,
                actionName = "إيقاف العامل عن العمل مؤقتاً لمصلحة التحقيق",
                duration = 60,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 66 من قانون العمل رقم 12 لسنة 2003",
                notes = "لصاحب العمل وقف العامل عن عمله مؤقتاً لمدة لا تزيد على ستين يوماً مع صرف أجره كاملاً إذا اقتضت مصلحة التحقيق ذلك.",
                summary = "وقف العامل لمصلحة التحقيق"
            ),
            LegalRule(
                id = 153,
                actionName = "عرض وقف العامل المتهم بجناية أو جنحة على المحكمة العمالية",
                duration = 3,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 67 من قانون العمل رقم 12 لسنة 2003",
                notes = "إذا اتهم العامل بجناية مخلة بالشرف جاز وقفه، وعليه عرض الأمر على المحكمة العمالية خلال 3 أيام، وعليها البت خلال 7 أيام.",
                summary = "عرض وقف العامل جنائياً"
            ),
            LegalRule(
                id = 154,
                actionName = "إنذار العامل كتابة قبل الفصل بسبب الغياب غير المبرر المتصل",
                duration = 5,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 69 بند 4 من قانون العمل رقم 12 لسنة 2003",
                notes = "إذا تغيب العامل أكثر من 10 أيام متصلة، يشترط للفصل أن يسبقه إنذار كتابي بخطاب موصى عليه بعلم الوصول بعد غيابه 5 أيام متصلة.",
                summary = "إنذار غياب العامل المتصل"
            ),
            LegalRule(
                id = 155,
                actionName = "إنذار العامل كتابة قبل الفصل بسبب الغياب غير المبرر المتقطع",
                duration = 10,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 69 بند 4 من قانون العمل رقم 12 لسنة 2003",
                notes = "إذا تغيب العامل أكثر من 20 يوماً متقطعة بالسنة، يشترط أن يسبق الفصل إنذار كتابي بعد غيابه 10 أيام متقطعة.",
                summary = "إنذار غياب العامل المتقطع"
            ),
            LegalRule(
                id = 156,
                actionName = "طلب التسوية الودية للنزاع العمالي الفردي واللجوء للمحكمة العمالية",
                duration = 7,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 70 من قانون العمل رقم 12 لسنة 2003",
                notes = "يطلب العامل أو صاحب العمل التسوية الودية خلال 7 أيام من النزاع، ولأي منهما اللجوء للمحكمة العمالية خلال 45 يوماً من فشلها وإلا سقط الحق.",
                summary = "تسوية النزاع العمالي وسقوط الدعوى"
            ),
            LegalRule(
                id = 157,
                actionName = "إعادة العامل إلى جهة التعاقد الأصلية على نفقة صاحب العمل",
                duration = 3,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 78 من قانون العمل رقم 12 لسنة 2003",
                notes = "يلتزم صاحب العمل بإعادة العامل إلى مكان التعاقد خلال ثلاثة أيام من تاريخ انتهاء عقد العمل إلا إذا رفض كتابة.",
                summary = "إعادة العامل لمكان التعاقد"
            ),
            LegalRule(
                id = 158,
                actionName = "إجازة الوضع للمرأة العاملة وحظر تشغيلها بعدها",
                duration = 4,
                unit = DurationUnit.MONTHS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "قانون العمل (المستحدث في المادة 91 والمتوافق مع قانون الطفل)",
                notes = "تستحق العاملة إجازة وضع مدتها أربعة أشهر كاملة (120 يوماً) بتعويض مساوٍ للأجر الشامل (3 مرات طوال الخدمة)، ويحظر تشغيلها خلال الـ 45 يوماً التالية للوضع.",
                summary = "إجازة الوضع للعاملة (4 أشهر)"
            ),
            LegalRule(
                id = 159,
                actionName = "مهلة الإخطار لإنهاء عقد العمل غير محدد المدة (خدمة حتى 10 سنوات)",
                duration = 2,
                unit = DurationUnit.MONTHS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 111 من قانون العمل رقم 12 لسنة 2003",
                notes = "يجب أن يتم الإخطار بإنهاء عقد العمل قبل الإنهاء بشهرين تقويميين كاملين إذا لم تتجاوز مدة الخدمة المتصلة للعامل عشر سنوات.",
                summary = "مهلة إخطار إنهاء العقد (شهران)"
            ),
            LegalRule(
                id = 160,
                actionName = "مهلة الإخطار لإنهاء عقد العمل غير محدد المدة (خدمة تزيد على 10 سنوات)",
                duration = 3,
                unit = DurationUnit.MONTHS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 111 من قانون العمل رقم 12 لسنة 2003",
                notes = "يجب أن يتم الإخطار بإنهاء عقد العمل قبل الإنهاء بثلاثة أشهر تقويمية كاملة إذا زادت مدة خدمة العامل على عشر سنوات.",
                summary = "مهلة إخطار إنهاء العقد (3 أشهر)"
            ),
            LegalRule(
                id = 161,
                actionName = "عدول العامل المستقيل عن استقالته كتابة واعتمادها بمكتب العمل",
                duration = 7,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 119 من قانون العمل وضوابط نفاذ الاستقالة المقررة",
                notes = "يشترط لنفاذ الاستقالة اعتمادها من مكتب العمل المختص، وللعامل المستقيل العدول عنها كتابة خلال أسبوع (7 أيام) من تاريخ إخطاره بقبولها وتعتبر كأن لم تكن.",
                summary = "عدول العامل عن الاستقالة"
            ),
            LegalRule(
                id = 162,
                actionName = "إخطار صاحب العمل للعامل بالرغبة في إنهاء العقد لمرضه",
                duration = 15,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 127 من قانون العمل رقم 12 لسنة 2003",
                notes = "يحظر إنهاء العقد لمرض العامل إلا بعد استنفاد إجازاته، وعلى صاحب العمل إخطاره برغبته في الإنهاء قبل مضي 15 يوماً من استنفادها.",
                summary = "إخطار إنهاء عقد لمرض العامل"
            ),
            LegalRule(
                id = 163,
                actionName = "اختيار الوسيط في منازعات العمل الجماعية وتعيينه",
                duration = 8,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 173 من قانون العمل رقم 12 لسنة 2003",
                notes = "يقوم طرفا النزاع باختيار الوسيط وإبلاغ الجهة الإدارية خلال 8 أيام من تقديم الطلب، وتتولى الجهة تعيينه إن لم يتفقا خلال 10 أيام.",
                summary = "اختيار وسيط عمالي"
            ),
            LegalRule(
                id = 164,
                actionName = "تحديد جلسة هيئة التحكيم العمالي وإخطار طرفي النزاع بها",
                duration = 15,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 184 من قانون العمل رقم 12 لسنة 2003",
                notes = "يحدد رئيس هيئة التحكيم جلسة لا تجاوز 15 يوماً من ورود الملف، ويخطر الأطراف قبل الجلسة بثلاثة أيام على الأقل.",
                summary = "جلسة تحكيم عمالي"
            ),
            LegalRule(
                id = 165,
                actionName = "إعلان حكم هيئة التحكيم العمالي لطرفي النزاع",
                duration = 3,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 188 من قانون العمل رقم 12 لسنة 2003",
                notes = "على هيئة التحكيم إعلان كل من طرفي النزاع بصورة الحكم بكتاب مسجل بعلم الوصول خلال ثلاثة أيام من تاريخ صدوره.",
                summary = "إعلان حكم تحكيم عمالي"
            ),
            LegalRule(
                id = 166,
                actionName = "إخطار الإضراب السلمي لصاحب العمل والجهة الإدارية",
                duration = 10,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 192 من قانون العمل رقم 12 لسنة 2003",
                notes = "يجب إخطار صاحب العمل والجهة الإدارية قبل التاريخ المحدد للإضراب السلمي بعشرة أيام على الأقل بكتاب مسجل بعلم الوصول.",
                summary = "إخطار إضراب عمالي"
            ),
            LegalRule(
                id = 167,
                actionName = "البت في طلب إغلاق المنشأة أو تقليص نشاطها عمالياً",
                duration = 30,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 197 من قانون العمل رقم 12 لسنة 2003",
                notes = "على اللجنة المختصة إصدار قرارها مسبباً في طلب إغلاق المنشأة خلال ثلاثين يوماً على الأكثر من تاريخ تقديم الطلب.",
                summary = "البت في إغلاق منشأة عمالية"
            ),

            // =========================================================================
            // سابعاً: قانون البناء الموحد رقم 119 لسنة 2008
            // =========================================================================
            LegalRule(
                id = 168,
                actionName = "فحص طلب ترخيص البناء والبت فيه من الجهة الإدارية",
                duration = 30,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 42 من قانون البناء رقم 119 لسنة 2008",
                notes = "تتولى الجهة الإدارية بشئون التخطيط فحص طلب الترخيص والبت فيه خلال ثلاثين يوماً من تقديم الطلب مستوفياً.",
                summary = "البت في ترخيص البناء"
            ),
            LegalRule(
                id = 169,
                actionName = "إصدار قرار إزالة أو تصحيح الأعمال المخالفة للبناء بعد وقفها",
                duration = 15,
                unit = DurationUnit.DAYS,
                distanceDays = 0,
                startRule = StartRule.NEXT_DAY,
                lawArticle = "المادة 60 من قانون البناء رقم 119 لسنة 2008",
                notes = "يصدر المحافظ المختص أو من ينيبه قراراً مسبباً بإزالة أو تصحيح الأعمال الموقوفة خلال 15 يوماً على الأكثر من تاريخ إعلان قرار الوقف.",
                summary = "قرار إزالة أعمال بناء مخالفة"
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
