package com.lessoncraft.engine

/**
 * Turns an idea into a complete lesson with fixed rules: a four-step sequence that climbs Bloom's levels, a handout of items whose wording and task
 * types depend on the grade, UDL for every step, three tiers of differentiation, and answer guidance. Nothing here calls a model or the network.
 */
object LessonBuilder {
    private val STOP_FOCUS = setOf("the", "and", "about", "with", "that", "this", "from", "into", "their", "students", "including", "using", "understand")

    fun build(request: LessonRequest, resolution: Resolution, idea: Idea): Lesson {
        val grade = request.grade
        val needs = CustomPrompt.read(request.customPrompt)
        val standard = resolution.standard
        val focus = focusPhrase(resolution)
        val level = BloomPlan.levelOf(standard?.description ?: "")
        val objective = objective(resolution, focus)
        val sequence = sequence(grade, idea, resolution, focus, needs, level)
        val items = if (grade.isEarly) earlyItems(request, focus, resolution, needs) else laterItems(request, idea, focus, needs)
        // Final line of defense: Kindergarten and Grade 1 student text must pass the guard, or it becomes a safe teacher-led task.
        val taught = (if (grade.isEarly) taughtWords(resolution) else emptyList()).toSet() + setOf("dots")
        val safeItems = if (grade.isEarly) items.map { enforceEarly(it, taught) } else items
        return Lesson(
            title = idea.title,
            grade = grade,
            subject = request.subject,
            standard = standard,
            topic = resolution.topic,
            customNotes = needs.notes,
            objective = objective,
            objectiveBloom = if (standard != null) level else Bloom.UNDERSTAND,
            materials = materials(request, needs, idea),
            udlPlan = Udl(
                "Offer a choice in how students show learning and connect ${focus} to their own lives.${needs.supports.firstOrNull()?.let { " $it" } ?: ""}",
                "Present ${focus} three ways: spoken, written and hands-on. Pre-teach key words and point out patterns.",
                "Let students respond by writing, drawing, building or explaining aloud, with a checklist for each step.",
            ),
            sequence = sequence,
            items = safeItems,
            differentiation = Differentiation(
                iep504 = "Same standard, same expectation. Add the supports in each student's plan: read directions aloud, break the work into steps, allow extra time, and offer a word bank.${needs.supports.drop(1).firstOrNull()?.let { " $it" } ?: ""}",
                onLevel = "Teach the sequence as written. Students complete the handout and discuss their answers with a partner.",
                enrichment = "Extend one Bloom's level higher: students ${extension(grade)} about $focus.",
            ),
            tags = tags(request, resolution, needs, idea),
        )
    }

    private fun extension(grade: Grade) = when (grade.band) {
        GradeBand.K2 -> "explain their thinking to a new partner and add one more example"
        GradeBand.G35 -> "make up a new problem or example and teach it to the class"
        GradeBand.G68 -> "defend their approach against a counterexample, then design a new case"
        GradeBand.G912 -> "critique two approaches and design an original solution"
    }

    /** A few plain words naming what the lesson is about, safe to drop into a sentence. */
    fun focusPhrase(resolution: Resolution): String {
        resolution.standard?.let { return it.focus }
        val words = Text.words(resolution.topic).filter { it.length > 2 && it !in STOP_FOCUS }.take(3)
        return words.joinToString(" ").ifBlank { "this topic" }
    }

    private fun objective(resolution: Resolution, focus: String): String {
        val s = resolution.standard
        val lead = "With prompting and support, "
        return if (s != null && s.description.startsWith(lead)) "Students will, with prompting and support, ${Text.decap(s.description.removeSuffix(".").removePrefix(lead))}."
        else if (s != null) "Students will be able to ${Text.decap(s.description.removeSuffix("."))}."
        else "Students will be able to explain and apply the key ideas about $focus."
    }

    private fun bloomCap(grade: Grade): Bloom = when {
        grade == Grade.K || grade == Grade.G1 -> Bloom.APPLY
        grade == Grade.G2 -> Bloom.ANALYZE
        grade.band == GradeBand.G35 -> Bloom.ANALYZE
        grade.band == GradeBand.G68 -> Bloom.EVALUATE
        else -> Bloom.CREATE
    }

    private fun sequence(grade: Grade, idea: Idea, resolution: Resolution, focus: String, needs: CustomNeeds, standardLevel: Bloom): List<Step> {
        val cap = bloomCap(grade)
        val exit = listOf(maxOf(standardLevel, Bloom.APPLY, if (grade.band != GradeBand.K2) Bloom.ANALYZE else Bloom.APPLY), cap).minBy { it.ordinal }
        val support = needs.supports.firstOrNull()
        fun udl(e: String, r: String, a: String) = Udl(e, r, a)
        return listOf(
            Step(
                "Hook", 10, "Hook: ${Text.clip(idea.title, 50)}", idea.hook,
                if (grade.band == GradeBand.K2) Bloom.REMEMBER else Bloom.UNDERSTAND,
                udl("Students vote or predict, so everyone has a stake in the answer.", "Say the question aloud and write it where all can see.", "Students answer by talking, pointing or writing a word."),
            ),
            Step(
                "Direct Instruction", 10, "Teach $focus",
                "Model $focus with a worked example, thinking aloud. Name the key words. Pause twice to check understanding, then have students explain the idea to a partner.${resolution.standard?.let { " Keep the standard in view: ${Text.clip(it.description, 110)}" } ?: ""}",
                Bloom.UNDERSTAND,
                udl("Connect the example to something students care about.", "Show the example spoken, written and with objects.", "Students show understanding by telling a partner, not only by writing."),
            ),
            Step(
                "Guided Practice", 15, "Practice with support",
                "Students ${idea.lens.angle.let { Text.decap(it) }} together on the handout while you circulate. Start with items 1 to 3 as a group, then release students to try the rest. ${support ?: "Offer hints before answers."}",
                Bloom.APPLY,
                udl("Students pick a partner or work alone.", "Post the steps and an example where all can see.", "Students may write, draw or explain aloud."),
            ),
            Step(
                "Exit Ticket", 5, "Show what you know",
                "Each student answers one question on $focus before leaving, at the highest level the class reached today. Collect the tickets and sort them into got it, almost and not yet.",
                exit,
                udl("Students pick the question they will answer.", "Read the question aloud.", "Students answer in words, a drawing or a number."),
            ),
        )
    }

    private fun materials(request: LessonRequest, needs: CustomNeeds, idea: Idea): List<String> {
        val m = mutableListOf("Student handout (printed or on a device)", "Pencils and paper for drawing and notes", "Chart paper or a board for the shared example")
        when (request.subject) {
            Subject.MATH -> m += "Counters, cubes or other objects students can move"
            Subject.SCIENCE -> m += "Everyday objects for observing, and a place to record what students notice"
            Subject.ELA -> m += "One mentor text of your choice that fits the standard${idea.lens.text?.let { " (${it})" } ?: ""}"
            Subject.SOCIAL_STUDIES -> m += "A short source or text of your choice that fits the standard"
        }
        if (needs.supports.any { it.contains("Hands-on") }) m += "Manipulatives for every pair"
        if (needs.supports.any { it.contains("movement", true) }) m += "Open floor space for movement breaks"
        return m
    }

    private fun tags(request: LessonRequest, resolution: Resolution, needs: CustomNeeds, idea: Idea): List<String> {
        val t = linkedSetOf(request.subject.label, request.grade.label, request.grade.band.label)
        resolution.standard?.let { t += it.shortCode; t += it.georgiaCode; t += it.focus }
        needs.themes.forEach { t += it }
        t += idea.lens.angle
        if (resolution.standard == null) Text.words(resolution.topic).filter { it.length > 3 && it !in STOP_FOCUS }.take(3).forEach { t += it }
        return t.toList()
    }

    /* ---------- Kindergarten and Grade 1 ---------- */

    /** Words that name something a child can see, so "I see a ___" makes sense. */
    private val CONCRETE = setOf("cat", "dog", "sun", "hat", "pig", "bus", "bug", "hen", "cup", "bed", "van", "fox", "bat", "rat", "pen", "box", "map")

    private val FALLBACK_WORDS = listOf("cat", "sun", "hat", "dog", "pig", "bus")
    private val CVC = Regex("^[bcdfghjklmnpqrstvwxyz][aeiou][bcdfgjklmnpqrstvxz]$")

    /** One or two simple words the child will trace, write and draw. Taught words are the one place a child may meet a word they cannot yet decode. */
    /** Words to trace and write for the standards that have a natural pair; others fall back to words from the topic. */
    private val EARLY_WORDS = mapOf(
        "RL.K.1" to listOf("who", "what"), "RL.1.1" to listOf("who", "what"),
        "RL.K.2" to listOf("tell", "end"), "RL.1.2" to listOf("tell", "end"),
        "RL.K.3" to listOf("cat", "dog"), "RL.1.3" to listOf("cat", "dog"),
    )

    internal fun taughtWords(resolution: Resolution): List<String> {
        resolution.standard?.let { s -> EARLY_WORDS[s.shortCode]?.let { return it } }
        val candidates = (Text.words(resolution.topic) + (resolution.standard?.keywords?.flatMap { Text.words(it) } ?: emptyList()))
            .filter { it.matches(Regex("[a-z]{3,7}")) && it !in STOP_FOCUS }
            .distinct()
        val ranked = candidates.sortedBy { if (CVC.matches(it)) 0 else it.length }
        return (ranked.take(2) + FALLBACK_WORDS).distinct().take(2)
    }

    private fun earlyItems(request: LessonRequest, focus: String, resolution: Resolution, needs: CustomNeeds): List<StudentItem> {
        val grade = request.grade
        val words = taughtWords(resolution)
        val w1 = words[0]
        val w2 = words[1]
        val ladder = BloomPlan.ladder(GradeBand.K2, 6)
        val script = EarlyGuard.SCRIPT_TAG
        if (request.subject == Subject.MATH) {
            val (a, b) = if (grade == Grade.K) 3 to 5 else 8 to 12
            return listOf(
                StudentItem(1, ItemKind.COUNT, "Count. Write the number.", Bloom.REMEMBER, "$a", visual = Visual.Dots(a)),
                StudentItem(2, ItemKind.COUNT, "Count. Write the number.", Bloom.REMEMBER, "$b", visual = Visual.Dots(b)),
                StudentItem(3, ItemKind.WRITE_WORD, "Write the next number: ${a + 1} ${a + 2} ___", Bloom.UNDERSTAND, "${a + 3}"),
                StudentItem(4, ItemKind.QUESTION, "Circle the big number: $a $b", Bloom.APPLY, "$b"),
                StudentItem(5, ItemKind.DRAW_BOX, "Draw $a dots in the box.", Bloom.APPLY, "A box with $a dots."),
                StudentItem(
                    6, ItemKind.TEACHER_ORAL, "Write a number:", Bloom.ANALYZE, "Answers vary. Look for a number the child can say a reason for.",
                    teacherScript = "$script Show two groups of objects. Ask: which group has more? How do you know? Children tell a partner, then each writes one number.",
                ),
            ).take(ladder.size)
        }
        return listOf(
            StudentItem(1, ItemKind.TRACE, "Trace: $w1", ladder[0], "The child traces the word $w1."),
            if (w1 in CONCRETE && w2 in CONCRETE) StudentItem(2, ItemKind.WORD_BANK, "I see a ___ . Word bank: $w1 $w2", ladder[1], "Either word from the bank makes sense.")
            else StudentItem(2, ItemKind.WRITE_WORD, "Copy one word: $w1 $w2 ______", ladder[1], "The child copies $w1 or $w2."),
            StudentItem(
                3, ItemKind.DRAW_BOX, "Draw in the box.", ladder[2], "Drawings vary. Look for a drawing about $focus.",
                teacherScript = "$script Say: draw what you learned about $focus in the box.",
            ),
            StudentItem(4, ItemKind.WRITE_WORD, "Copy the word: $w2 ______", ladder[2], "The child writes $w2."),
            StudentItem(
                5, ItemKind.TEACHER_ORAL, "Write one word:", ladder[3], "Answers vary. Look for a word that fits the talk about $focus.",
                teacherScript = "$script Talk about $focus with the class. Ask: what is one thing we learned? Children tell a partner, then each writes one word.",
            ),
            StudentItem(
                6, ItemKind.TEACHER_ORAL, "Write one word:", ladder[3], "Answers vary. Look for a word and a reason from the child when asked.",
                teacherScript = "$script Ask: how are two parts of today's lesson alike or different? Children tell a partner, then each writes one word.",
            ),
        )
    }

    /** Anything in a Kindergarten or Grade 1 item that fails the guard is replaced by a safe, teacher-led task. */
    private fun enforceEarly(item: StudentItem, taught: Set<String>): StudentItem {
        val problems = EarlyGuard.problems(item.prompt.replace("___", " "), taught)
        if (problems.isEmpty()) return item
        return item.copy(kind = ItemKind.TEACHER_ORAL, prompt = EarlyGuard.SAFE_STUDENT_LINE, teacherScript = "${EarlyGuard.SCRIPT_TAG} ${EarlyGuard.SAFE_SCRIPT}", answer = "Answers vary.", visual = null)
    }

    /* ---------- Grade 2 and up ---------- */

    private fun laterItems(request: LessonRequest, idea: Idea, focus: String, needs: CustomNeeds): List<StudentItem> {
        val band = request.grade.band
        val ladder = BloomPlan.ladder(band, 6)
        val math = request.subject == Subject.MATH
        val text = if (request.subject == Subject.ELA || request.subject == Subject.SOCIAL_STUDIES) "Use your text. " else ""
        fun prompt(level: Bloom): Pair<String, String> = when (level) {
            Bloom.REMEMBER -> "${text}List three key words about $focus." to "Look for accurate key words from the lesson on $focus."
            Bloom.UNDERSTAND -> "Explain $focus. Use your own words. Finish: $focus means ___ because ___." to "Look for an explanation in the student's words that is accurate and gives a reason."
            Bloom.APPLY -> (if (math) "Solve a new problem about $focus. Show every step." else "Use $focus in a new example. Show how it works.") to "Look for a correct example or solution with the steps shown."
            Bloom.ANALYZE -> "Compare two examples of $focus. Make a table: alike and different." to "Look for at least two accurate likenesses and two differences, each tied to evidence."
            Bloom.EVALUATE -> "Choose the strongest example of $focus. Give two reasons." to "Look for a clear choice supported by two reasons that use evidence."
            Bloom.CREATE -> "Design your own model of $focus. List what it must show." to "Look for an original model that meets every item on the student's list."
        }
        return ladder.mapIndexed { i, level ->
            val (p, a) = prompt(level)
            val kind = when (level) {
                Bloom.ANALYZE -> ItemKind.DATA_TABLE
                Bloom.UNDERSTAND -> ItemKind.SENTENCE_FRAME
                else -> ItemKind.QUESTION
            }
            StudentItem(i + 1, kind, p, level, a)
        }
    }
}
