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
        val objective = objective(resolution, focus) + (resolution.book?.let { " Evidence comes from ${it.describe()}." } ?: "")
        val domain = domainView(request, resolution, grade, focus)
        val sequence = sequence(grade, idea, resolution, focus, needs, level, domain)
        val items = if (grade.isEarly) earlyItems(request, focus, resolution, needs) else if (resolution.book != null) bookItems(grade, resolution.book) else if (domain != null) domainItems(grade, domain) else laterItems(request, idea, focus, needs)
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
            materials = materials(request, needs, idea, resolution.book, domain),
            udlPlan = Udl(
                "Offer a choice in how students show learning and connect ${focus} to their own lives.${needs.supports.firstOrNull()?.let { " $it" } ?: ""}",
                "Present ${focus} three ways: spoken, written and hands-on. Pre-teach key words and point out patterns.",
                "Let students respond by writing, drawing, building or explaining aloud, with a checklist for each step.",
            ),
            sequence = sequence,
            items = safeItems,
            differentiation = Differentiation(
                iep504 = "Same standard, same expectation. Add the supports in each student's plan: read directions aloud, break the work into steps, allow extra time, and offer a word bank.${needs.supports.drop(1).firstOrNull()?.let { " $it" } ?: ""}",
                onLevel = "Teach the sequence as written. Students complete the handout and discuss their answers with a partner." + (resolution.book?.let { b -> BookScaling.note(b, grade)?.let { " $it" } } ?: ""),
                enrichment = "Extend one Bloom's level higher: students ${extension(grade)} about $focus.",
            ),
            tags = tags(request, resolution, needs, idea),
            book = resolution.book,
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
        resolution.book?.let { return it.title }
        resolution.pack?.let { return it.title }
        resolution.standard?.let { if (it.catalogued || resolution.topic.isBlank()) return it.focus }
        val words = Text.words(resolution.topic).filter { it.length > 2 && it !in STOP_FOCUS }.take(3)
        return words.joinToString(" ").ifBlank { "this topic" }
    }

    private fun objective(resolution: Resolution, focus: String): String {
        val s = resolution.standard
        val lead = "With prompting and support, "
        if (s != null && !s.catalogued) return "Students will be able to meet ${s.ccss} by explaining and applying $focus."
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

    private fun sequence(grade: Grade, idea: Idea, resolution: Resolution, focus: String, needs: CustomNeeds, standardLevel: Bloom, domain: DomainView?): List<Step> {
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
                if (domain != null) domain.directInstruction(resolution.standard) else "Model $focus ${if (resolution.book != null) "with a short passage read aloud and a thinking-aloud talk about what the words show" else "with a worked example, thinking aloud"}. Name the key words. Pause twice to check understanding, then have students explain the idea to a partner.${resolution.standard?.let { " Keep the standard in view: ${Text.clip(it.description, 110)}" } ?: ""}",
                Bloom.UNDERSTAND,
                udl("Connect the example to something students care about.", "Show the example spoken, written and with objects.", "Students show understanding by telling a partner, not only by writing."),
            ),
            Step(
                "Guided Practice", 15, "Practice with support",
                if (domain != null) domain.guidedPractice(support) else "Students ${idea.lens.angle.let { Text.decap(it) }} together on the handout while you circulate. Start with items 1 to 3 as a group, then release students to try the rest. ${support ?: "Offer hints before answers."}",
                Bloom.APPLY,
                udl("Students pick a partner or work alone.", "Post the steps and an example where all can see.", "Students may write, draw or explain aloud."),
            ),
            Step(
                "Exit Ticket", 5, "Show what you know",
                if (domain != null) domain.exitTicket() else "Each student answers one question on $focus before leaving, at the highest level the class reached today. Collect the tickets and sort them into got it, almost and not yet.",
                exit,
                udl("Students pick the question they will answer.", "Read the question aloud.", "Students answer in words, a drawing or a number."),
            ),
        )
    }

    private fun materials(request: LessonRequest, needs: CustomNeeds, idea: Idea, book: BookRef?, domain: DomainView?): List<String> {
        val m = mutableListOf<String>()
        // One required text: the book itself. Nothing else is offered as a second required text.
        if (book != null) m += "Class copies of ${book.describe()} (the one required text)"
        m += listOf("Student handout (printed or on a device)", "Pencils and paper for drawing and notes", "Chart paper or a board for the shared example")
        when (request.subject) {
            Subject.MATH -> m += domain?.materials ?: listOf("Counters, cubes or other objects students can move")
            Subject.SCIENCE -> m += domain?.materials ?: listOf("Everyday objects for observing, and a place to record what students notice")
            Subject.ELA -> if (book == null) m += "One mentor text of your choice that fits the standard${idea.lens.text?.let { " (${it})" } ?: ""}"
            Subject.SOCIAL_STUDIES -> m += "A short source or text of your choice that fits the standard"
        }
        if (needs.supports.any { it.contains("Hands-on") }) m += "Manipulatives for every pair"
        if (needs.supports.any { it.contains("movement", true) }) m += "Open floor space for movement breaks"
        return m
    }

    private fun tags(request: LessonRequest, resolution: Resolution, needs: CustomNeeds, idea: Idea): List<String> {
        val t = linkedSetOf(request.subject.label, request.grade.label, request.grade.band.label)
        resolution.book?.let { t += it.title; it.author?.let { a -> t += a }; it.info?.genre?.let { g -> t += g } }
        resolution.pack?.let { t += it.title }
        resolution.standard?.let { t += it.shortCode; t += it.georgiaCode; if (resolution.book == null && it.catalogued) t += it.focus }
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
        resolution.pack?.earlyWord?.let { w -> return (listOf(w) + FALLBACK_WORDS).distinct().take(2) }
        resolution.book?.let { book ->
            book.info?.earlyWords?.takeIf { it.isNotEmpty() }?.let { w -> return (w + FALLBACK_WORDS).distinct().take(2) }
            // A chapter book read to the youngest: trace and write two short character names from the book.
            val names = book.characters.mapNotNull { Text.words(it).firstOrNull { w -> w.matches(Regex("[a-z]{3,7}")) && w !in setOf("the", "mrs", "mr") } }.distinct().sortedBy { it.length }
            if (names.size >= 2) return names.take(2)
        }
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

    /* ---------- A book-based handout: every item is answered from the text ---------- */

    /**
     * A book-based handout pitched at the target grade, whatever the book's usual grade. Younger grades get concrete, short tasks (who, what, where,
     * quote one line); middle grades compare, cite and decide; the oldest analyze the author's choices and evaluate how well they work. Sentences stay
     * within the grade's limit, and "the book" stands in for a long title where the limit is tight.
     */
    private fun bookItems(grade: Grade, book: BookRef): List<StudentItem> {
        val band = grade.band
        val ladder = BloomPlan.ladder(band, 6)
        val ref = if (grade.ordinal <= Grade.G3.ordinal || Text.words(book.title).size > 4 && band == GradeBand.G35) "the book" else book.title
        // Names short enough to fit a student sentence; a group description ("the animals in the book") is not a name to compare or write a scene about.
        val people = book.characters.filter { Text.words(it).size <= 3 && !it.startsWith("the animals") }
        val a = people.getOrNull(0) ?: "a main character"
        // The second character is a contrast to the first (the fifth named, else the last), so comparisons and new scenes have something to work with.
        val b = people.takeIf { it.size > 1 }?.let { it[if (it.size > 4) 4 else it.size - 1] } ?: "another character"
        val th = book.themes.getOrNull(0) ?: "a theme"
        val q = book.info?.questions?.takeIf { BookScaling.hasOlderQuestions(book) }?.getOrNull(0)
        val eg = if (people.isNotEmpty()) " (for example ${people.take(3).joinToString(", ")})" else ""

        fun make(level: Bloom, again: Int): Pair<String, String> = when (band) {
            GradeBand.K2 -> when (level) {
                Bloom.REMEMBER -> if (again == 0) "Name two people in $ref." to "Accurate names from the text$eg." else "Say who the book is about. Write one name." to "A correct name from the text$eg."
                Bloom.UNDERSTAND -> if (again == 0) "Tell what $ref is about. Use two sentences." to "Two sentences that say what happens, in the student's words." else "Tell how it begins and ends." to "A beginning and an end that match the text."
                Bloom.APPLY -> if (again == 0) "Find a line in $ref. Copy it. Say where." to "A line copied exactly from the student's copy, with its place." else "Find a word in $ref you like. Copy it. Tell why." to "A word copied exactly, with a reason that is the student's own."
                else -> if (again == 0) "Compare two people in $ref. Make a table." to "At least one likeness and one difference, each from the text." else "Tell what is the same. Tell what differs." to "One likeness and one difference, each from the text."
            }
            GradeBand.G35 -> when (level) {
                Bloom.REMEMBER -> "Name three characters in $ref. Write one fact about each." to "Accurate names from the text$eg, each with a fact the book supports."
                Bloom.UNDERSTAND -> "Explain the theme of $th in $ref. Use a sentence frame." to "An explanation in the student's words that names $th and points to something in the book."
                Bloom.APPLY -> "Find a quote about $th in $ref. Copy it. Say where it is." to "A quote copied exactly, with its place, that shows $th."
                Bloom.ANALYZE -> "Compare $a and $b. Use a table. Add one quote each." to "Two or more likenesses and differences between $a and $b, each backed by a quote."
                Bloom.EVALUATE -> "Which choice in $ref matters most? Give two reasons." to "A clear choice with two reasons from the text. Score the evidence, not the opinion."
                Bloom.CREATE -> "Write a new scene where $a meets $b. Keep their voices true." to "An original scene that keeps the characters' voices consistent with the book."
            }
            GradeBand.G68 -> when (level) {
                Bloom.REMEMBER -> "Name three characters in $ref. Write one fact about each." to "Accurate names from the text$eg, each with a fact the book supports."
                Bloom.UNDERSTAND -> "Explain the theme of $th in $ref. Finish: In $ref, ___ shows $th because ___." to "An explanation in the student's words that names $th and points to something that happens in the book."
                Bloom.APPLY -> "Find a quote from $ref that shows $th. Copy it exactly. Say where it is." to "A quote copied exactly from the student's copy, with its location, that shows $th."
                Bloom.ANALYZE -> "Compare $a and $b in a table: alike and different. Give one quote for each." to "At least two accurate likenesses and two differences between $a and $b, each backed by a quote."
                Bloom.EVALUATE -> (if (q != null) "$q Give two reasons. Use evidence from the text." else "Which choice in $ref matters most to the story? Give two reasons.") to "A clear position supported by two reasons that cite the text. Score the evidence, not the opinion."
                Bloom.CREATE -> "Write a new scene for $ref where $a meets $b. Keep their voices true to the book." to "An original scene that keeps the characters' voices and the setting consistent with the text."
            }
            GradeBand.G912 -> when (level) {
                Bloom.REMEMBER -> "Name the key characters in $ref. Write one fact about each." to "Accurate names from the text$eg, each with a fact the book supports."
                Bloom.UNDERSTAND -> "Explain how $th develops in $ref. Finish: ___ shows $th because ___." to "An explanation that traces $th through specific moments in the text."
                Bloom.APPLY -> "Find two quotes from $ref that show $th. Say where each appears." to "Two exact quotes, with locations, that really show $th."
                Bloom.ANALYZE -> "Analyze how the author uses $a and $b to develop $th. Cite two quotes." to "An analysis of how each character's role builds $th, supported by two quotes."
                Bloom.EVALUATE -> (if (q != null) "$q Evaluate the evidence for each side. Use at least two places in the text." else "Evaluate how effectively $ref develops $th. Use evidence from at least two places.") to "A reasoned judgment weighing evidence from at least two places in the text. Score the reasoning and the evidence."
                Bloom.CREATE -> "Write a new scene for $ref where $a meets $b. Match the author's voice and style." to "An original scene that matches the author's voice, style and setting, with choices that fit the characters."
            }
        }
        return ladder.mapIndexed { i, level ->
            val (p, ans) = make(level, ladder.take(i).count { it == level })
            StudentItem(i + 1, when (level) { Bloom.ANALYZE -> ItemKind.DATA_TABLE; Bloom.UNDERSTAND -> ItemKind.SENTENCE_FRAME; else -> ItemKind.QUESTION }, p, level, ans)
        }
    }

    /* ---------- Science, math and social studies: the subject's own concepts, not reading frames ---------- */

    /** What a non-ELA lesson teaches: a concept pack's phases and terms, or the subject's generic routine with the topic dropped in. */
    class DomainView(
        val title: String,
        val kind: String,
        val phases: List<Phase>,
        val terms: List<Term>,
        val misconception: String?,
        val correction: String?,
        val materials: List<String>,
        val modelTask: String,
        val subject: Subject,
        /** True when the content comes from a built-in concept pack, so its terms and phases are verified subject facts. */
        val fromPack: Boolean,
    ) {
        val stepWord: String get() = when (kind) { "process" -> "phase"; "cycle" -> "stage"; else -> "step" }
        private val stepWords: String get() = stepWord + "s"

        fun directInstruction(standard: Standard?): String = buildString {
            append("Introduce $title with a labeled model on the board. Teach the ${phases.size} $stepWords in order, naming each term as you go: ")
            append(phases.mapIndexed { i, p -> "${i + 1}. ${p.name}: ${p.happens}" }.joinToString(" "))
            append(" Key terms: ${terms.take(6).joinToString("; ") { "${it.word} (${it.meaning})" }}.")
            if (misconception != null) append(" Address this common misconception directly: \"$misconception\" ${correction ?: ""}")
            append(" Pause after each $stepWord to check understanding.")
            if (standard != null && standard.catalogued) append(" Keep the standard in view: ${Text.clip(standard.description, 110)}")
        }

        fun guidedPractice(support: String?): String =
            "Students build the model together: $modelTask Then, in groups, they complete a three-column table ($stepWord, what happens, what you would see) for every $stepWord, " +
                "and check each row with the class before moving on. Start with items 1 to 3 as a group, then release students to try the rest. ${support ?: "Offer hints before answers."}"

        fun exitTicket(): String {
            val p = phases[phases.size / 2]
            val t = termsFor(p).joinToString(" and ") { it.word }
            return "Each student explains the $stepWord \"${p.name}\" in writing, using the terms $t, before leaving. Collect the tickets and sort them into got it, almost and not yet."
        }

        /** The terms that appear in a phase's own description (so a question names real vocabulary), else the first two. */
        fun termsFor(p: Phase): List<Term> = terms.filter { t -> p.happens.lowercase().contains(t.word.lowercase().removeSuffix("s")) }.take(2).ifEmpty { terms.take(2) }
    }

    private fun domainView(request: LessonRequest, resolution: Resolution, grade: Grade, focus: String): DomainView? {
        if (request.subject == Subject.ELA || resolution.book != null) return null
        val younger = grade.band == GradeBand.K2 || grade.band == GradeBand.G35
        val pack = resolution.pack
        if (pack != null) {
            return DomainView(pack.title, pack.kind, if (younger) pack.simple else pack.phases, pack.terms, pack.misconception, pack.correction, pack.materials, pack.modelTask, request.subject, true)
        }
        val topic = if (focus == "this topic") resolution.topic else focus
        val phases = if (younger) GenericDomain.simple(request.subject, topic) else GenericDomain.phases(request.subject, topic)
        val model = when (request.subject) {
            Subject.SCIENCE -> "Draw and label a model that explains $topic, and say what evidence would test it."
            Subject.MATH -> "Show the problem about $topic with a drawing or objects, then solve it."
            else -> "Make a cause and effect chart about $topic from the source."
        }
        return DomainView(topic, "step", phases, GenericDomain.terms(request.subject), null, null, GenericDomain.materials(request.subject), model, request.subject, false)
    }

    /**
     * A handout for science, math and social studies, climbing Bloom's levels at the grade's demand. Every item asks about the concept's own phases,
     * terms and misconception, so a mitosis lesson asks about prophase and chromosomes, not about "a main character".
     */
    private fun domainItems(grade: Grade, d: DomainView): List<StudentItem> {
        val band = grade.band
        val ladder = BloomPlan.ladder(band, 6)
        val short = grade.ordinal <= Grade.G3.ordinal
        val names = d.phases.map { it.name }
        val first = d.phases.first()
        val second = d.phases[1 % d.phases.size]
        val last = d.phases.last()
        val strategy = d.kind == "strategy" || !d.fromPack
        val terms = d.termsFor(second).map { it.word }
        val termText = if (terms.size >= 2) "${terms[0]} and ${terms[1]}" else terms.firstOrNull() ?: "a key term"
        val order = (if (names.size >= 3) names.drop(2) + names.take(2) else names.reversed()).take(5)
        val sw = d.stepWord
        val older = band == GradeBand.G68 || band == GradeBand.G912

        fun make(level: Bloom): Pair<String, String> = when (level) {
            Bloom.REMEMBER ->
                (if (short) "Name the ${sw}s in order." else "List the ${sw}s of ${d.title} in order.") to "${names.joinToString(", then ")}."
            Bloom.UNDERSTAND ->
                (if (band == GradeBand.K2) "Tell what happens in $sw 2." else "Explain what happens in ${second.name}. Use the ${if (terms.size >= 2) "terms" else "term"} $termText.") to
                    "${second.happens} Look for ${termText} used correctly."
            Bloom.APPLY ->
                (if (strategy) (if (short) "Make up a problem. Solve it with the ${sw}s." else "Make up a new problem about ${d.title} and solve it with the ${sw}s. Show each ${sw}.")
                else if (band == GradeBand.K2) "Put in order: ${order.joinToString(", ")}." else "Put these ${sw}s in order: ${order.joinToString(", ")}.") to
                    (if (strategy) "A new problem solved correctly with every $sw shown." else "${names.filter { it in order }.joinToString(", then ")}.")
            Bloom.ANALYZE ->
                (if (short) "Compare two ${sw}s. Make a table." else if (strategy) "Compare two ways to work on ${d.title}. Make a table: what is the same, what is different."
                else "Compare ${first.name} and ${last.name} in a table: what happens and what you would see.") to
                    (if (strategy) "At least two likenesses and two differences, each explained." else "${first.name}: ${first.happens} (${first.evidence}) ${last.name}: ${last.happens} (${last.evidence})")
            Bloom.EVALUATE ->
                (if (d.misconception != null && older) "A student claims: \"${d.misconception}\" Decide if it is correct. Give two reasons."
                else if (older) "Which $sw of ${d.title} is hardest to explain? Give two reasons." else "Which $sw is most important? Give two reasons.") to
                    (d.correction ?: "A clear choice supported by two reasons. Score the reasons, not the choice.")
            Bloom.CREATE ->
                (if (short) "Draw and label a model. Show each $sw." else if (band == GradeBand.G35) "Make a labeled model of ${d.title}. Show every $sw."
                else "Design a labeled model of ${d.title} for a younger student. Show and explain every $sw.") to
                    "A model that shows every $sw in order, uses the key terms correctly, and could be understood by someone new to the topic."
        }
        return ladder.mapIndexed { i, level ->
            val (p, ans) = make(level)
            StudentItem(i + 1, when (level) { Bloom.ANALYZE -> ItemKind.DATA_TABLE; Bloom.UNDERSTAND -> ItemKind.QUESTION; Bloom.CREATE -> ItemKind.DRAW_BOX; else -> ItemKind.QUESTION }, p, level, ans)
        }
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
