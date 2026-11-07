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

    /** What students work from in a science lesson: data and descriptions, not a story. */
    private val SCIENCE_SOURCES = listOf(
        "a data table from an investigation", "a written description of a model", "a short informational article", "a lab procedure to follow and improve",
        "a set of student observations", "a diagram described in words",
    )

    private val MATH_SOURCES = listOf("a set of worked examples", "a real-world problem", "a table of values", "a student's solution with a mistake to find")

    private val TEXTS = listOf(
        "a short read-aloud story", "a short nonfiction passage", "a poem, song lyric or chant", "a data table or survey results",
        "a real document such as a letter, schedule or label", "a short scene with dialogue", "a how-to text", "a fable or folktale",
        "a set of student-collected observations", "a model paragraph to study and imitate",
    )

    /** Ways of working with a book's own text. A book lesson draws its approach from these, so every idea is about reading and citing the book. */
    internal class TextWork(val name: String, val product: String, val doing: (BookRef, GradeBand) -> String)

    private fun chars(b: BookRef, n: Int = 2): String = b.characters.take(n).let { if (it.isEmpty()) "a main character" else it.joinToString(" and ") }
    private fun theme(b: BookRef, i: Int = 0): String = b.themes.getOrNull(i) ?: "a theme the class chooses"

    /** The same kind of text work, pitched at the grade band: listening and telling for the youngest, evidence for the middle, analysis and judgment for the oldest. */
    internal val TEXT_WORKS = listOf(
        TextWork("close reading of a key passage", "an annotated passage") { b, band ->
            when (band) {
                GradeBand.K2 -> "listen as a short passage from ${b.title} is read aloud, then tell which words helped them see it"
                GradeBand.G35 -> "reread a short passage from ${b.title}, underline the strongest words, and say what the words show"
                GradeBand.G68 -> "reread a key passage from ${b.title}, mark the strongest words, and explain what they show"
                GradeBand.G912 -> "analyze a key passage from ${b.title}, explain how the author's word choices create meaning, and judge their effect"
            }
        },
        TextWork("character study", "a character map with a quote for each choice") { b, band ->
            when (band) {
                GradeBand.K2 -> "listen for what ${chars(b, 1)} does in ${b.title} and tell how ${chars(b, 1)} feels"
                GradeBand.G35 -> "follow ${chars(b)} through ${b.title} and tell how their actions show what they are like"
                GradeBand.G68 -> "follow ${chars(b)} through ${b.title} and track how their choices and feelings change"
                GradeBand.G912 -> "analyze how ${chars(b)} ${if (b.characters.size >= 2) "develop" else "develops"} through ${b.title} and what ${if (b.characters.size >= 2) "their" else "the character's"} choices reveal about the author's purpose"
            }
        },
        TextWork("theme tracing", "a theme tracker with three pieces of evidence") { b, band ->
            when (band) {
                GradeBand.K2 -> "talk about what ${b.title} teaches and find one part of the book that shows it"
                GradeBand.G35 -> "decide what lesson ${b.title} teaches about ${theme(b)} and find two parts of the text that show it"
                GradeBand.G68 -> "trace ${theme(b)} through ${b.title} and collect quotes from three places in the book"
                GradeBand.G912 -> "trace how ${theme(b)} develops in ${b.title}, evaluate how convincingly the author builds it, and support the judgment with quotes"
            }
        },
        TextWork("evidence-based debate", "a one-minute argument with two quotes") { b, band ->
            when (band) {
                GradeBand.K2 -> "pick a favorite part of ${b.title}, tell why, and listen to a partner's reason"
                GradeBand.G35 -> "choose a side on a question about ${b.title} and give two reasons from the text"
                GradeBand.G68 -> "take a side on a question from ${b.title} and defend it with quotes from the text"
                GradeBand.G912 -> "take a position on a debatable claim about ${b.title}, defend it with textual evidence, and answer a counterargument"
            }
        },
        TextWork("setting and conflict study", "a setting-and-conflict chart") { b, band ->
            val s = b.info?.setting?.takeIf { b.known } ?: "the setting"
            when (band) {
                GradeBand.K2 -> "listen for where ${b.title} happens and tell what the place is like"
                GradeBand.G35 -> "describe $s in ${b.title} and find a problem the place causes"
                GradeBand.G68 -> "study $s in ${b.title} and how it shapes what happens"
                GradeBand.G912 -> "analyze how $s in ${b.title} shapes its conflicts and what the setting says about the author's purpose"
            }
        },
        TextWork("writing from the text", "a paragraph with embedded quotes") { b, band ->
            when (band) {
                GradeBand.K2 -> "tell and write one sentence about ${b.title} using a sentence frame"
                GradeBand.G35 -> "write a paragraph about ${b.title} with a sentence frame and one quote"
                GradeBand.G68 -> "write a response about ${theme(b, 1)} that quotes ${b.title} to support each idea"
                GradeBand.G912 -> "write an argument about ${theme(b, 1)} in ${b.title} that integrates and analyzes quotations"
            }
        },
        TextWork("then and now comparison", "a compare table with evidence") { b, band ->
            when (band) {
                GradeBand.K2 -> "tell how ${chars(b, 1)} is at the start and at the end of ${b.title}"
                GradeBand.G35 -> "compare how ${chars(b, 1)} acts at the beginning and the end of ${b.title}"
                GradeBand.G68 -> "compare how ${chars(b, 1)} acts early and late in ${b.title}, using evidence from each part"
                GradeBand.G912 -> "evaluate how and why ${chars(b, 1)} changes across ${b.title}, using evidence from the beginning, middle and end"
            }
        },
    )

    private fun bookDrafts(request: LessonRequest, resolution: Resolution, book: BookRef, needs: CustomNeeds, seen: List<SeenIdea>, used: List<Lens>, key: String, count: Int): List<Idea> {
        val rng = Text.rng(key)
        val recent = used.takeLast(count)
        val works = fresh(TEXT_WORKS.map { it.name }, used.map { it.angle }, count, recent.map { it.angle }).shuffled(rng).take(count).map { n -> TEXT_WORKS.first { it.name == n } }
        val titleStyles = BOOK_TITLES.indices.shuffled(rng).take(count)
        return works.mapIndexed { i, w ->
            val scaling = BookScaling.note(book, request.grade)
            val standardNote = resolution.standard?.let { " It addresses ${it.georgiaCode} (${it.ccss})." } ?: ""
            val coreText = "Core text: ${book.describe()}${book.info?.let { " (${it.genre})" } ?: ""}."
            val knowNote = (if (book.known) "" else if (book.info != null) " The built-in list recognizes this title but has no verified details for it, so add the author, characters and passages from your own copy." else " Chapters and passages come from your own copy: add the ones you want students to cite.") +
                (scaling?.let { " $it" } ?: "")
            val supportNote = needs.supports.firstOrNull()?.let { " Built for your students: ${Text.decap(it)}" } ?: ""
            val themeNote = needs.themes.firstOrNull()?.let { " Your theme, $it, frames the activities." } ?: ""
            val build = resolution.standard?.let { " They build toward: ${Text.decap(it.description.removeSuffix("."))}." } ?: ""
            val title = Text.clip(BOOK_TITLES[titleStyles[i]](book.title, Text.cap(w.name)), 80)
            val hook = BookScaling.hookQuestion(book, request.grade, seen.size + i)
            val summary = "Students ${w.doing(book, request.grade.band)}, and finish with ${w.product}.$build$standardNote $coreText$themeNote$supportNote$knowNote"
            Idea(title, summary.replace("  ", " ").trim(), hook, Lens(w.name, "a question from the book", book.title, book.describe()))
        }
    }

    private val BOOK_TITLES = listOf(
        { t: String, a: String -> "$t: $a" }, { t: String, a: String -> "$a in $t" }, { t: String, a: String -> "$t Through $a" },
        { t: String, a: String -> "$a for $t" }, { t: String, a: String -> "$t, Page by Page: $a" },
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
        val pool = when (request.subject) { Subject.ELA, Subject.SOCIAL_STUDIES -> TEXTS; Subject.SCIENCE -> SCIENCE_SOURCES; Subject.MATH -> MATH_SOURCES }
        val usesTexts = true
        val used = seen.map { it.lens }
        val key = "${request.grade}|${request.subject}|${request.standardInput}|${request.customPrompt}|${request.bookTitle}|${seen.size}"
        val book = resolution.book

        // Redraft with a new seed until nothing echoes an earlier idea or another new one (a few tries, then the best drafts available).
        var best: List<Idea> = emptyList()
        var bestBad = Int.MAX_VALUE
        for (attempt in 0 until 6) {
            if (book != null) {
                val drafts = bookDrafts(request, resolution, book, needs, seen, used, "$key|$attempt", count)
                val bad = echoes(drafts.map { Overlap(it.title, it.hook, it.summary) }, seen.map { Overlap(it.title, it.hook, it.summary) }).size
                if (bad < bestBad) { best = drafts; bestBad = bad }
                if (bad == 0) break
                continue
            }
            val rng = Text.rng("$key|$attempt")
            val recent = used.takeLast(count)
            val angles = fresh(ANGLES.map { it.name }, used.map { it.angle }, count, recent.map { it.angle }).shuffled(rng).take(count)
            val hooks = fresh(HOOKS, used.map { it.hook }, count, recent.map { it.hook }).shuffled(rng).take(count)
            val themes = fresh(THEMES, used.map { it.theme }, count, recent.map { it.theme }).shuffled(rng).take(count)
            val texts = if (usesTexts) fresh(pool, used.mapNotNull { it.text }, count, recent.mapNotNull { it.text }).shuffled(rng).take(count) else emptyList()
            val titleStyles = TITLES.indices.shuffled(rng).take(count)

            val drafts = (0 until count).map { i ->
                val angle = ANGLES.first { it.name == angles[i] }
                val topic = Text.cap(LessonBuilder.focusPhrase(resolution))
                // A teacher's own topic or custom theme is the setting; the random theme only fills in when they gave only a standard code.
                val setting = needs.themes.getOrNull(i % maxOf(needs.themes.size, 1)) ?: if (request.standardInput.isNotBlank() && resolution.exact && needs.themes.isEmpty()) themes[i] else null
                val standardNote = resolution.standard?.let { " It addresses ${it.georgiaCode} (${it.ccss})." } ?: ""
                val textNote = texts.getOrNull(i)?.let { " Students work with $it." } ?: ""
                val pack = resolution.pack
                val conceptNote = pack?.let { " Key ideas: ${it.phases.joinToString(", ") { p -> p.name.lowercase() }}. Vocabulary includes ${it.terms.take(4).joinToString(", ") { t -> t.word }}." } ?: ""
                val settingNote = setting?.let { " The context is $it." } ?: ""
                val supportNote = needs.supports.firstOrNull()?.let { " Built for your students: ${Text.decap(it)}" } ?: ""
                val title = Text.clip(TITLES[titleStyles[i]](short(topic), Text.cap(angle.name)), 70)
                val hook = resolution.pack?.hooks?.let { it[(i + seen.size) % it.size] } ?: hookLine(hooks[i], topic, request.subject)
                val summary = "Students ${angle.doing}, and finish with ${angle.product}. " +
                    "They build toward: ${Text.decap(resolution.standard?.description?.removeSuffix(".") ?: resolution.topic)}.$standardNote$conceptNote$textNote$settingNote$supportNote"
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

    private fun hookLine(style: String, topic: String, subject: Subject = Subject.ELA): String = when {
        style.startsWith("a surprising question") -> "Open with a question that has no obvious answer: what would happen if ${Text.decap(short(topic))} did not work the way we expect?"
        style.startsWith("a mystery object") -> "Reveal a covered object linked to ${Text.decap(short(topic))} and let students guess what it has to do with today."
        style.startsWith("a 30-second") -> "Act out a quick skit about ${Text.decap(short(topic))} with a deliberate mistake, and have students catch it."
        style.startsWith("a would-you-rather") -> "Pose a would-you-rather choice about ${Text.decap(short(topic))} and make students justify their pick."
        style.startsWith("a startling") -> "Read aloud one startling fact or number about ${Text.decap(short(topic))} and ask what it makes them wonder."
        style.startsWith("a quick physical") -> "Start with a 30-second physical challenge, then connect it to ${Text.decap(short(topic))}."
        style.startsWith("the first line") && subject != Subject.ELA -> "Show a surprising photo or object linked to ${Text.decap(short(topic))} and have students write what they notice and wonder."
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
