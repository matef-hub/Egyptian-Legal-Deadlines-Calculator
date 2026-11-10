package com.ateflaw.legaldeadlines.domain.model

/** Values entered in the catalog editor. No UI field can grant legal approval. */
data class LegalRuleDraft(
    val actionName: String,
    val lawName: String,
    val lawArticle: String,
    val articleText: String,
    val durationText: String,
    val unit: DurationUnit = DurationUnit.DAYS,
    val notes: String = ""
) {
    data class Validated(
        val actionName: String,
        val lawName: String,
        val lawArticle: String,
        val articleText: String,
        val duration: Long,
        val unit: DurationUnit,
        val notes: String
    )

    fun validate(isNew: Boolean): Validated {
        val action = actionName.trim()
        val law = lawName.trim()
        val reference = lawArticle.trim()
        val text = articleText.trim()
        val note = notes.trim()
        require(action.isNotBlank() && action.length <= 250) { "اكتب اسم الإجراء (حتى 250 حرف)." }
        require(law.isNotBlank() && law.length <= 200) { "اكتب اسم القانون (حتى 200 حرف)." }
        require(reference.isNotBlank() && reference.length <= 250) { "اكتب رقم المادة والسند القانوني." }
        require(!isNew || text.isNotBlank()) { "اكتب نص المادة القانوني عند إضافة قاعدة جديدة." }
        require(text.length <= 20000) { "نص المادة طويل جدًا." }
        require(note.length <= 2000) { "الملاحظات طويلة جدًا." }

        // Accept both Western and Arabic-Indic digits without allowing decimals,
        // negatives or empty values to be coerced into legal deadlines.
        val raw = durationText.trim()
        require(raw.isNotEmpty() && raw.length <= 6 && raw.all { it.isDigit() }) {
            "أدخل عددًا صحيحًا موجبًا للمدة."
        }
        val western = raw.map {
            val digit = Character.digit(it, 10)
            require(digit in 0..9) { "المدة تحتوي أرقامًا غير صالحة." }
            ('0'.code + digit).toChar()
        }.joinToString("")
        val duration = western.toLongOrNull()
            ?: throw IllegalArgumentException("قيمة المدة غير صالحة.")
        require(unit != DurationUnit.HOURS) { "البرنامج يستخدم الأيام كأقل وحدة إدخال؛ اكتب يومًا واحدًا للـ24 ساعة مع مراجعة توقيت بدء المهلة." }
        val max = when (unit) {
            DurationUnit.DAYS -> 36500L
            DurationUnit.MONTHS -> 1200L
            DurationUnit.YEARS -> 100L
            DurationUnit.HOURS -> 876000L
        }
        require(duration in 1L..max) { "المدة يجب أن تكون بين 1 و$max حسب الوحدة المختارة." }
        return Validated(action, law, reference, text, duration, unit, note)
    }

    companion object {
        fun fromRule(rule: LegalRule) = LegalRuleDraft(
            actionName = rule.actionName,
            lawName = rule.lawName,
            lawArticle = rule.lawArticle,
            articleText = rule.articleText,
            durationText = rule.duration.toString(),
            unit = rule.unit,
            notes = rule.notes
        )
    }
}
