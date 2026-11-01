package com.lessoncraft.engine

/**
 * Three lesson ideas from rules, not a model. The same request and session history always give the same ideas (a seed from both), and every
 * "New ideas" round is drawn only from approaches, hook styles, themes and core texts not yet used, then checked against everything already
 * shown for word overlap, so a new round is built on different ground rather than reworded.
 */
object IdeaEngine {
    private class Angle(val name: String, val doing: String, val product: String)

    private val ANGLES = listOf(
        Angle("hands-on investigation", "investigate with everyday objects and record what they find", "an investigation sheet"),
        Angle("game", "play a game with rules they can explain to a partner", "a rule card for the game"),
        Angle("story-driven lesson", "follow a character with a problem and help solve it", "the next page of the story"),
        Angle("mystery challenge", "crack a set of clues that unlock the next step", "a solved case file"),
        Angle("movement lesson", "move around the room to act out and sort the ideas", "a class movement map"),
        Angle("maker project", "make something they can show to the class", "a finished model"),
        Angle("structured debate", "take a side, give reasons and respond to others", "a one-minute argument"),
        Angle("real-world problem", "plan, budget or design a solution to a real problem", "a one-page plan"),
        Angle("role-play", "act as the people in the problem and talk it through", "a short scene"),
        Angle("station rotation", "rotate through three stations, each a different way in", "a station passport"),
        Angle("song or chant", "write and perform a chant that holds the key ideas", "a class chant"),
        Angle("data mini-study", "collect data about themselves and read what it says", "a class chart"),
        Angle("peer teaching", "become experts on one part and teach it to others", "a teaching card"),
        Angle("gallery walk", "walk the room reading and adding thinking notes", "a page of notes"),
    )

    private val HOOKS = listOf(
        "a surprising question with no obvious answer", "a mystery object the teacher reveals", "a 30-second teacher skit with a mistake to catch",
        "a would-you-rather choice that has to be justified", "a startling fact or number read aloud", "a quick physical challenge before anyone sits",
        "the first line of a story with the ending missing", "two truths and a lie about the topic", "a broken model students have to repair", "a student-voted prediction",
    )

    private val THEMES = listOf(
        "a busy kitchen", "the school playground", "a community garden", "a sports tournament", "a pet shop", "a road trip", "a museum", "a weather station",
        "a farmers' market", "a construction site", "a detective agency", "a camping trip", "a bakery", "a music studio", "a post office", "an inventor's workshop",
    )

    private val TEXTS = listOf(
        "a short read-aloud story", "a short nonfiction passage", "a poem, song lyric or chant", "a data table or survey results",
        "a real document such as a letter, schedule or label", "a short scene with dialogue", "a how-to text", "a fable or folktale",
        "a set of student-collected observations", "a model paragraph to study and imitate",
    )

    private val TITLES = listOf(
        { t: String, a: String -> "$t: The $a" }, { t: String, a: String -> "$a for $t" }, { t: String, a: String -> "Inside $t: A $a" },
        { t: String, a: String -> "The $t $a" }, { t: String, a: String -> "$t Through a $a" }, { t: String, a: String -> "A $a About $t" },
    )

    private fun fresh(pool: List<String>, used: Collection<String?>, n: Int, recent: Collection<String?>): List<String> {
        val left = pool.filter { it !in used }
        if (left.size >= n) return left
        val some = pool.filter { it !in recent }
        return if (some.size >= n) some else pool
    }

    /** How many rounds the teacher has already seen (three ideas each). */
    fun round(seen: List<SeenIdea>) = seen.size / 3

    fun generate(request: LessonRequest, resolution: Resolution, seen: List<SeenIdea>, count: Int = 3): List<Idea> {
        require(request.hasInput) { "Enter a topic or standard, or describe the lesson." }
        val needs = CustomPrompt.read(request.customPrompt)
        val usesTexts = request.subject != Subject.MATH
        val used = seen.map { it.lens }
        val key = "${request.grade}|${request.subject}|${request.standardInput}|${request.customPrompt}|${seen.size}"

        // Redraft with a new seed until nothing echoes an earlier idea or another new one (a few tries, then the best drafts available).
        var best: List<Idea> = emptyList()
        var bestBad = Int.MAX_VALUE
        for (attempt in 0 until 6) {
            val rng = Text.rng("$key|$attempt")
            val recent = used.takeLast(count)
            val angles = fresh(ANGLES.map { it.name }, used.map { it.angle }, count, recent.map { it.angle }).shuffled(rng).take(count)
            val hooks = fresh(HOOKS, used.map { it.hook }, count, recent.map { it.hook }).shuffled(rng).take(count)
            val themes = fresh(THEMES, used.map { it.theme }, count, recent.map { it.theme }).shuffled(rng).take(count)
            val texts = if (usesTexts) fresh(TEXTS, used.mapNotNull { it.text }, count, recent.mapNotNull { it.text }).shuffled(rng).take(count) else emptyList()
            val titleStyles = TITLES.indices.shuffled(rng).take(count)

            val drafts = (0 until count).map { i ->
                val angle = ANGLES.first { it.name == angles[i] }
                val topic = Text.cap(LessonBuilder.focusPhrase(resolution))
                // A teacher's own topic or custom theme is the setting; the random theme only fills in when they gave only a standard code.
                val setting = needs.themes.getOrNull(i % maxOf(needs.themes.size, 1)) ?: if (request.standardInput.isNotBlank() && resolution.exact && needs.themes.isEmpty()) themes[i] else null
                val standardNote = resolution.standard?.let { " It addresses ${it.georgiaCode} (${it.ccss})." } ?: ""
                val textNote = texts.getOrNull(i)?.let { " Students work with $it." } ?: ""
                val settingNote = setting?.let { " The context is $it." } ?: ""
                val supportNote = needs.supports.firstOrNull()?.let { " Built for your students: ${Text.decap(it)}" } ?: ""
                val title = Text.clip(TITLES[titleStyles[i]](short(topic), Text.cap(angle.name)), 70)
                val hook = hookLine(hooks[i], topic)
                val summary = "Students ${angle.doing}, and finish with ${angle.product}. " +
                    "They build toward: ${Text.decap(resolution.standard?.description?.removeSuffix(".") ?: resolution.topic)}.$standardNote$textNote$settingNote$supportNote"
                Idea(title, summary.trim(), hook, Lens(angle.name, hooks[i], setting ?: themes[i], texts.getOrNull(i)))
            }

            val seenLike = seen.map { Overlap(it.title, it.hook, it.summary) }
            val bad = echoes(drafts.map { Overlap(it.title, it.hook, it.summary) }, seenLike).size
            if (bad < bestBad) { best = drafts; bestBad = bad }
            if (bad == 0) break
        }
        return best
    }

    private fun short(topic: String): String = topic.split(" ").take(5).joinToString(" ").trimEnd(',', '.', ';')

    private fun hookLine(style: String, topic: String): String = when {
        style.startsWith("a surprising question") -> "Open with a question that has no obvious answer: what would happen if ${Text.decap(short(topic))} did not work the way we expect?"
        style.startsWith("a mystery object") -> "Reveal a covered object linked to ${Text.decap(short(topic))} and let students guess what it has to do with today."
        style.startsWith("a 30-second") -> "Act out a quick skit about ${Text.decap(short(topic))} with a deliberate mistake, and have students catch it."
        style.startsWith("a would-you-rather") -> "Pose a would-you-rather choice about ${Text.decap(short(topic))} and make students justify their pick."
        style.startsWith("a startling") -> "Read aloud one startling fact or number about ${Text.decap(short(topic))} and ask what it makes them wonder."
        style.startsWith("a quick physical") -> "Start with a 30-second physical challenge, then connect it to ${Text.decap(short(topic))}."
        style.startsWith("the first line") -> "Read the first line of a story about ${Text.decap(short(topic))} and stop: students predict the ending."
        style.startsWith("two truths") -> "Share two truths and a lie about ${Text.decap(short(topic))}; students vote and defend."
        style.startsWith("a broken model") -> "Show a model of ${Text.decap(short(topic))} with an error built in; students find and repair it."
        else -> "Take a quick vote predicting what they will learn about ${Text.decap(short(topic))}, then revisit it at the end."
    }

    internal data class Overlap(val title: String, val hook: String, val summary: String)

    /** Which drafts (by index) overlap an earlier idea or an earlier draft too much. */
    internal fun echoes(fresh: List<Overlap>, earlier: List<Overlap>, threshold: Double = 0.34): List<Int> {
        fun tok(o: Overlap) = Text.tokens("${o.title} ${o.title} ${o.hook} ${o.summary}")
        val bad = mutableSetOf<Int>()
        fresh.forEachIndexed { i, f ->
            if (earlier.any { Text.jaccard(tok(f), tok(it)) >= threshold }) bad += i
            for (j in 0 until i) if (Text.jaccard(tok(f), tok(fresh[j])) >= threshold + 0.06) bad += i
        }
        return bad.toList()
    }
}
