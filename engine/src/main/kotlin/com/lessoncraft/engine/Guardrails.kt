package com.lessoncraft.engine

/** Reading level and the Kindergarten/Grade 1 safety rules, as code instead of prompt text. */
object Readability {
    /** The longest sentence a student is asked to read alone, by grade. */
    fun maxWords(grade: Grade): Int = when (grade) {
        Grade.K, Grade.G1 -> 6
        Grade.G2 -> 8
        Grade.G3 -> 12
        Grade.G4, Grade.G5 -> 15
        Grade.G6, Grade.G7, Grade.G8 -> 20
        else -> 25
    }

    /** Longest run of numbered steps a direction may have. */
    fun maxSteps(grade: Grade): Int = when (grade) {
        Grade.K, Grade.G1 -> 3
        Grade.G2, Grade.G3 -> 4
        Grade.G4, Grade.G5 -> 5
        else -> 6
    }

    fun wordCount(sentence: String): Int = Text.words(sentence).size
}

/**
 * Kindergarten and Grade 1: what a child reads alone must be decodable, short, and answerable from what is printed. Anything else is teacher-led.
 * The builder only produces text that passes these checks; the same checks run over any text before it is shown.
 */
object EarlyGuard {
    const val SCRIPT_TAG = "[TEACHER SCRIPT / READ ALOUD]"

    /** High-frequency words a child may read alone. */
    val SIGHT_WORDS: Set<String> = setOf(
        "i", "see", "a", "the", "can", "to", "is", "it", "we", "my", "you", "go", "like", "and", "up", "no", "me", "in", "on", "at", "am", "an", "do",
        "he", "she", "so", "us", "of", "yes", "look", "here", "this", "said", "are", "was", "for", "has", "have", "too", "put", "all", "one", "two", "three",
        "four", "five", "six", "seven", "eight", "nine", "ten", "write", "draw", "trace", "word", "bank", "box", "count", "circle", "name", "number", "next", "big", "dots", "copy", "each", "how", "many",
    )

    private val CVC = Regex("^[bcdfghjklmnpqrstvwxyz][aeiou][bcdfgjklmnpqrstvxz]$")
    private val DIGITS = Regex("^[0-9]+$")

    /** Interactions the printed page cannot support unless every option is typed out in the item. */
    private val PAIR_TASK = Regex("\\b(match|matching|connect|draw (?:a )?lines?|join)\\b", RegexOption.IGNORE_CASE)
    private val SELECT_TASK = Regex("\\b(circle|underline|check|tick|mark|point to|choose|pick|select|which)\\b", RegexOption.IGNORE_CASE)
    private val UNPRINTED = Regex("\\b(picture|photo|image|illustration|diagram|map|chart|graph)\\b", RegexOption.IGNORE_CASE)
    private val TEACHER_LED = Regex("\\b(partners?|choice|choices|tokens?|spoken|aloud|discuss|share|tell|explain|say)\\b", RegexOption.IGNORE_CASE)

    fun isDecodable(word: String, taught: Set<String>): Boolean {
        val w = word.lowercase()
        return w in SIGHT_WORDS || w in taught || DIGITS.matches(w) || CVC.matches(w)
    }

    /** True if the options of a choice are typed in the item: "A) cat B) dog", "cat or dog", "cat / dog", "Circle: cat dog". */
    fun hasTypedOptions(s: String): Boolean =
        Regex("(?:^|\\s)[A-D][).]\\s").findAll(s).count() >= 2 ||
            Regex("\\b[\\w'-]+\\s+or\\s+[\\w'-]+\\b", RegexOption.IGNORE_CASE).containsMatchIn(s) ||
            Regex("\\b[\\w'-]+\\s*[/|]\\s*[\\w'-]+\\b").containsMatchIn(s) ||
            Regex(":\\s*[\\w'-]+(?:\\s+[\\w'-]+){1,5}\\s*$").containsMatchIn(s)

    /** Why a student-read line is not allowed for Kindergarten or Grade 1 (empty when it is fine). `taught` are the words the item itself prints to be learned. */
    fun problems(text: String, taught: Set<String> = emptySet()): List<String> {
        val out = mutableListOf<String>()
        // A word the item itself teaches (tracing "tell", a word bank with "end") is a word to learn, not an instruction or a task type.
        val t = taught.fold(text.replace(SCRIPT_TAG, "").trim()) { acc, w -> acc.replace(Regex("\\b${Regex.escape(w)}\\b", RegexOption.IGNORE_CASE), "word") }
        if (PAIR_TASK.containsMatchIn(t)) out += "Matching tasks cannot be printed."
        if (SELECT_TASK.containsMatchIn(t) && !hasTypedOptions(t) && !t.lowercase().startsWith("circle the word you wrote")) out += "A circle-or-pick task needs every option typed in the item."
        if (UNPRINTED.containsMatchIn(t)) out += "It refers to a picture or chart the page does not contain."
        if (TEACHER_LED.containsMatchIn(t)) out += "Partner, choice or spoken work belongs in a teacher script."
        Text.sentences(t).forEach { s ->
            if (Readability.wordCount(s) > Readability.maxWords(Grade.K)) out += "Sentence is longer than ${Readability.maxWords(Grade.K)} words: \"${Text.clip(s, 40)}\""
        }
        val taughtLower = taught.map { it.lowercase() }.toSet()
        val bad = Text.words(t).filterNot { isDecodable(it.trim('\'', '-'), taughtLower) }.distinct()
        if (bad.isNotEmpty()) out += "Not decodable for a Kindergarten or Grade 1 reader: ${bad.take(4).joinToString(", ")}"
        return out
    }

    /** What to show for text that fails the guard. Used by the builder as a last line of defense. */
    const val SAFE_STUDENT_LINE = "Write one word:"
    const val SAFE_SCRIPT = "Ask the class to say one word they learned today. Have each child write that word on the line."
}

/** Bloom's Taxonomy plan by grade band: which levels a lesson's items and steps should cover. */
object BloomPlan {
    /** Target share of items at each level, in percent, by band. */
    fun mix(band: GradeBand): Map<Bloom, Int> = when (band) {
        GradeBand.K2 -> mapOf(Bloom.REMEMBER to 30, Bloom.UNDERSTAND to 30, Bloom.APPLY to 30, Bloom.ANALYZE to 10, Bloom.EVALUATE to 0, Bloom.CREATE to 0)
        GradeBand.G35 -> mapOf(Bloom.REMEMBER to 20, Bloom.UNDERSTAND to 25, Bloom.APPLY to 25, Bloom.ANALYZE to 20, Bloom.EVALUATE to 5, Bloom.CREATE to 5)
        GradeBand.G68 -> mapOf(Bloom.REMEMBER to 10, Bloom.UNDERSTAND to 20, Bloom.APPLY to 25, Bloom.ANALYZE to 20, Bloom.EVALUATE to 15, Bloom.CREATE to 10)
        GradeBand.G912 -> mapOf(Bloom.REMEMBER to 5, Bloom.UNDERSTAND to 15, Bloom.APPLY to 25, Bloom.ANALYZE to 20, Bloom.EVALUATE to 20, Bloom.CREATE to 15)
    }

    /** The order items climb in for a grade, from lower to higher: the levels with a nonzero share, once each, repeating the biggest if more are needed. */
    fun ladder(band: GradeBand, count: Int): List<Bloom> {
        val levels = mix(band).filterValues { it > 0 }.keys.toList()
        if (count <= levels.size) return levels.take(count)
        val weights = mix(band).filterValues { it > 0 }
        val extra = (count - levels.size)
        val ranked = weights.entries.sortedByDescending { it.value }.map { it.key }
        return (levels + List(extra) { ranked[it % ranked.size] }).sortedBy { it.ordinal }
    }

    /** The cognitive level a standard's own verb asks for. The standard sets the ceiling for what the lesson's objective claims. */
    fun levelOf(description: String): Bloom {
        val first = Text.words(description).firstOrNull { it !in setOf("with", "prompting", "and", "support") } ?: return Bloom.UNDERSTAND
        return when (first) {
            "ask", "identify", "name", "recognize", "state", "list" -> Bloom.REMEMBER
            "retell", "describe", "recount", "understand", "explain", "interpret", "represent", "summarize" -> Bloom.UNDERSTAND
            "use", "solve", "add", "subtract", "fluently", "apply", "calculate" -> Bloom.APPLY
            "compare", "determine", "analyze", "distinguish", "examine" -> Bloom.ANALYZE
            "evaluate", "justify", "argue", "critique" -> Bloom.EVALUATE
            "design", "create", "compose", "construct", "plan" -> Bloom.CREATE
            else -> Bloom.UNDERSTAND
        }
    }
}

/** What the free-form "Custom instructions" box asks for, read by keyword so it works with no model. */
data class CustomNeeds(
    val themes: List<String>,
    val supports: List<String>,
    val notes: String,
)

object CustomPrompt {
    private val THEMES = listOf(
        Regex("\\b(fall|autumn|harvest|pumpkin|scarecrow)\\b") to "a fall harvest",
        Regex("\\b(winter|snow|snowman)\\b") to "a snowy winter day",
        Regex("\\b(spring|garden|flowers?|plants?)\\b") to "a spring garden",
        Regex("\\b(space|planets?|rockets?|astronaut)\\b") to "a trip through space",
        Regex("\\b(ocean|sea|beach|fish)\\b") to "the ocean",
        Regex("\\b(animals?|zoo|pets?|farm)\\b") to "a class zoo",
        Regex("\\b(cook|cooking|bake|baking|pizza|kitchen|bakery)\\b") to "a busy kitchen",
        Regex("\\b(sports?|soccer|basketball|game day|tournament)\\b") to "a sports tournament",
        Regex("\\b(music|song|rhythm|band)\\b") to "a music studio",
        Regex("\\b(halloween|spooky)\\b") to "a Halloween mystery",
        Regex("\\b(back to school|first day)\\b") to "the first week of school",
    )
    private val SUPPORTS = listOf(
        Regex("\\b(english learners?|ell|esl|multilingual|newcomers?)\\b") to "Sentence frames and a printed word bank, spoken aloud first, for English learners.",
        Regex("\\b(movement|wiggly|kinesthetic|active|brain breaks?)\\b") to "Short movement breaks between steps, and tasks students can do standing or on the move.",
        Regex("\\b(dyslexia|struggling readers?|reading difficult\\w*|decoding)\\b") to "Read directions aloud and keep student text short and decodable.",
        Regex("\\b(adhd|attention|focus|fidget\\w*)\\b") to "Chunk the work into short timed steps with a visible checklist.",
        Regex("\\b(gifted|advanced|early finishers?|enrichment)\\b") to "An enrichment challenge one thinking level higher for early finishers.",
        Regex("\\b(autism|autistic|routine|predictable|transitions?)\\b") to "A predictable routine, a posted schedule, and a warning before each transition.",
        Regex("\\b(iep|504|accommodations?)\\b") to "Accommodations from students' plans applied to every task (extra time, read-aloud, reduced items).",
        Regex("\\b(hands-on|manipulatives?|concrete)\\b") to "Hands-on materials students can touch and move before they write.",
        Regex("\\b(partners?|groups?|collaborat\\w*|cooperative)\\b") to "Partner or small-group talk before independent work.",
    )

    fun read(prompt: String): CustomNeeds {
        val p = prompt.lowercase()
        return CustomNeeds(
            themes = THEMES.filter { it.first.containsMatchIn(p) }.map { it.second },
            supports = SUPPORTS.filter { it.first.containsMatchIn(p) }.map { it.second },
            notes = prompt.trim().replace(Regex("\\s+"), " "),
        )
    }
}
