package com.lessoncraft.engine

/**
 * The standards the app knows about offline. This is a starter catalog of Common Core standards (with the Georgia codes derived from them), not
 * every standard. A request that matches nothing here is still planned from its topic; add entries to grow the catalog.
 */
object StandardsCatalog {
    private fun ela(code: String, grade: Grade, strand: String, description: String, vararg keywords: String) =
        Standard("CCSS.ELA-LITERACY.$code", description, grade, Subject.ELA, strand, keywords.toList())

    private fun math(code: String, grade: Grade, strand: String, description: String, vararg keywords: String) =
        Standard("CCSS.MATH.CONTENT.$code", description, grade, Subject.MATH, strand, keywords.toList())

    val all: List<Standard> = listOf(
        ela("RL.K.1", Grade.K, "Reading Literature", "With prompting and support, ask and answer questions about key details in a text.", "key details", "questions", "key details", "story"),
        ela("RL.K.2", Grade.K, "Reading Literature", "With prompting and support, retell familiar stories, including key details.", "retelling", "retell", "retelling", "sequence", "story", "beginning", "middle", "end"),
        ela("RL.K.3", Grade.K, "Reading Literature", "With prompting and support, identify characters, settings, and major events in a story.", "story elements", "characters", "setting", "events", "story elements"),
        ela("RL.1.1", Grade.G1, "Reading Literature", "Ask and answer questions about key details in a text.", "key details", "questions", "key details", "story"),
        ela("RL.1.2", Grade.G1, "Reading Literature", "Retell stories, including key details, and demonstrate understanding of their central message or lesson.", "retelling", "retell", "retelling", "sequence", "sequencing", "central message", "lesson", "story"),
        ela("RL.1.3", Grade.G1, "Reading Literature", "Describe characters, settings, and major events in a story, using key details.", "story elements", "characters", "setting", "events", "story elements"),
        ela("RL.2.2", Grade.G2, "Reading Literature", "Recount stories, including fables and folktales from diverse cultures, and determine their central message, lesson, or moral.", "fables", "recount", "fable", "folktale", "moral", "central message"),
        ela("RL.2.3", Grade.G2, "Reading Literature", "Describe how characters in a story respond to major events and challenges.", "characters", "characters", "challenges", "problem", "solution"),
        ela("RL.3.2", Grade.G3, "Reading Literature", "Recount stories, including fables, folktales, and myths from diverse cultures; determine the central message, lesson, or moral and explain how it is conveyed through key details in the text.", "central message", "recount", "fable", "myth", "central message", "moral", "key details"),
        ela("RI.3.2", Grade.G3, "Reading Informational Text", "Determine the main idea of a text; recount the key details and explain how they support the main idea.", "main idea", "main idea", "key details", "nonfiction", "informational"),
        ela("RL.4.3", Grade.G4, "Reading Literature", "Describe in depth a character, setting, or event in a story or drama, drawing on specific details in the text.", "story details", "character", "setting", "event", "details"),
        ela("RI.4.2", Grade.G4, "Reading Informational Text", "Determine the main idea of a text and explain how it is supported by key details; summarize the text.", "main idea", "main idea", "summary", "summarize", "key details", "nonfiction"),
        ela("RL.5.2", Grade.G5, "Reading Literature", "Determine a theme of a story, drama, or poem from details in the text, including how characters in a story or drama respond to challenges or how the speaker in a poem reflects upon a topic; summarize the text.", "theme", "theme", "summary", "summarize", "poem", "challenges"),
        math("K.CC.B.4", Grade.K, "Counting and Cardinality", "Understand the relationship between numbers and quantities; connect counting to cardinality.", "counting", "counting", "count", "numbers", "quantity", "cardinality"),
        math("K.CC.C.6", Grade.K, "Counting and Cardinality", "Identify whether the number of objects in one group is greater than, less than, or equal to the number of objects in another group.", "comparing groups", "compare", "greater", "less", "equal", "more", "fewer", "groups"),
        math("K.OA.A.1", Grade.K, "Operations and Algebraic Thinking", "Represent addition and subtraction with objects, fingers, mental images, drawings, sounds, acting out situations, verbal explanations, expressions, or equations.", "addition and subtraction", "addition", "subtraction", "add", "subtract", "represent"),
        math("1.OA.A.1", Grade.G1, "Operations and Algebraic Thinking", "Use addition and subtraction within 20 to solve word problems involving situations of adding to, taking from, putting together, taking apart, and comparing, with unknowns in all positions.", "word problems", "addition", "subtraction", "word problems", "add", "subtract", "within 20"),
        math("1.NBT.B.2", Grade.G1, "Number and Operations in Base Ten", "Understand that the two digits of a two-digit number represent amounts of tens and ones.", "tens and ones", "tens", "ones", "place value", "two-digit"),
        math("2.OA.B.2", Grade.G2, "Operations and Algebraic Thinking", "Fluently add and subtract within 20 using mental strategies.", "addition and subtraction", "fluency", "addition", "subtraction", "mental math", "within 20"),
        math("2.NBT.B.5", Grade.G2, "Number and Operations in Base Ten", "Fluently add and subtract within 100 using strategies based on place value, properties of operations, and/or the relationship between addition and subtraction.", "place value", "addition", "subtraction", "place value", "within 100", "regrouping"),
        math("3.OA.A.1", Grade.G3, "Operations and Algebraic Thinking", "Interpret products of whole numbers, e.g., interpret 5 × 7 as the total number of objects in 5 groups of 7 objects each.", "multiplication", "multiplication", "products", "groups", "arrays", "equal groups"),
        math("3.NF.A.1", Grade.G3, "Number and Operations - Fractions", "Understand a fraction 1/b as the quantity formed by 1 part when a whole is partitioned into b equal parts; understand a fraction a/b as the quantity formed by a parts of size 1/b.", "fractions", "fractions", "fraction", "unit fraction", "equal parts", "numerator", "denominator"),
        math("4.NF.A.2", Grade.G4, "Number and Operations - Fractions", "Compare two fractions with different numerators and different denominators; record the results of comparisons with symbols >, =, or <, and justify the conclusions.", "comparing fractions", "fractions", "compare", "comparing fractions", "denominator", "numerator"),
    )

    fun forGradeAndSubject(grade: Grade, subject: Subject): List<Standard> = all.filter { it.grade == grade && it.subject == subject }
}

/** The outcome of reading the "Topic / standard" box. */
data class Resolution(
    /** The catalog standard the input names or most closely matches, if any. */
    val standard: Standard?,
    /** True when the input named the standard's code directly (CCSS or Georgia form), not just a keyword match. */
    val exact: Boolean,
    /** The teaching focus in plain words: the topic the teacher typed, the standard's own wording, or the start of the custom prompt. */
    val topic: String,
)

object StandardResolver {
    private fun normalizeCode(s: String): String =
        s.uppercase().replace("CCSS.", "").replace("ELA-LITERACY.", "").replace("MATH.CONTENT.", "").replace(Regex("[\\s_]"), "")

    fun resolve(request: LessonRequest): Resolution {
        val input = request.standardInput.trim()
        val pool = StandardsCatalog.forGradeAndSubject(request.grade, request.subject)

        if (input.isNotEmpty()) {
            val code = normalizeCode(input)
            // A code, in CCSS or Georgia form, for any grade: honor it even if it is not this request's grade.
            val direct = StandardsCatalog.all.firstOrNull { normalizeCode(it.ccss) == code || it.shortCode.uppercase() == code || it.georgiaCode.uppercase() == code }
            if (direct != null) return Resolution(direct, true, Text.decap(direct.description.removeSuffix(".")))
        }

        // Keywords in the topic (or, failing that, in the custom prompt) pick the closest standard for this grade and subject.
        val probe = Text.tokens(if (input.isNotEmpty()) input else request.customPrompt)
        val scored = pool.map { s ->
            val bag = Text.tokens(s.keywords.joinToString(" ") + " " + s.description)
            val kw = s.keywords.count { k -> Text.tokens(k).let { t -> t.isNotEmpty() && t.all { it in probe } } }
            s to (kw * 3 + probe.count { it in bag })
        }.filter { it.second >= 3 }.maxByOrNull { it.second }?.first

        val topic = when {
            input.isNotEmpty() && scored == null -> input
            scored != null && input.isNotEmpty() -> input
            scored != null -> Text.decap(scored.description.removeSuffix("."))
            else -> Text.clip(request.customPrompt.trim().replace(Regex("\\s+"), " "), 80)
        }
        return Resolution(scored, false, topic)
    }
}
