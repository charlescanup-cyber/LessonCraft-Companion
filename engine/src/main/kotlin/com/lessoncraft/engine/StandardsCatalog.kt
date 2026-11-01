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
        ela("RL.2.1", Grade.G2, "Reading Literature", "Ask and answer such questions as who, what, where, when, why, and how to demonstrate understanding of key details in a text.", "key details", "questions", "who what where"),
        ela("RL.3.1", Grade.G3, "Reading Literature", "Ask and answer questions to demonstrate understanding of a text, referring explicitly to the text as the basis for the answers.", "textual evidence", "cite", "evidence", "questions"),
        ela("RL.3.3", Grade.G3, "Reading Literature", "Describe characters in a story (e.g., their traits, motivations, or feelings) and explain how their actions contribute to the sequence of events.", "characters", "traits", "motivations", "plot"),
        ela("RL.4.1", Grade.G4, "Reading Literature", "Refer to details and examples in a text when explaining what the text says explicitly and when drawing inferences from the text.", "textual evidence", "cite", "evidence", "inference"),
        ela("RL.4.2", Grade.G4, "Reading Literature", "Determine a theme of a story, drama, or poem from details in the text; summarize the text.", "theme", "summary", "summarize"),
        ela("RL.5.1", Grade.G5, "Reading Literature", "Quote accurately from a text when explaining what the text says explicitly and when drawing inferences from the text.", "textual evidence", "quote", "cite", "evidence", "inference"),
        ela("RL.5.3", Grade.G5, "Reading Literature", "Compare and contrast two or more characters, settings, or events in a story or drama, drawing on specific details in the text.", "characters", "compare", "setting", "events"),
        ela("RL.6.1", Grade.G6, "Reading Literature", "Cite textual evidence to support analysis of what the text says explicitly as well as inferences drawn from the text.", "textual evidence", "cite", "evidence", "inference"),
        ela("RL.6.3", Grade.G6, "Reading Literature", "Describe how a particular story's or drama's plot unfolds in a series of episodes as well as how the characters respond or change as the plot moves toward a resolution.", "characters", "plot", "change", "resolution"),
        ela("RL.11-12.1", Grade.G11, "Reading Literature", "Cite strong and thorough textual evidence to support analysis of what the text says explicitly as well as inferences drawn from the text, including determining where the text leaves matters uncertain.", "textual evidence", "cite", "evidence", "inference"),
        ela("RL.11-12.2", Grade.G11, "Reading Literature", "Determine two or more themes or central ideas of a text and analyze their development over the course of the text, including how they interact and build on one another to produce a complex account; provide an objective summary of the text.", "theme", "central idea", "summary", "development"),
        ela("RL.11-12.3", Grade.G11, "Reading Literature", "Analyze the impact of the author's choices regarding how to develop and relate elements of a story or drama (e.g., where a story is set, how the action is ordered, how the characters are introduced and developed).", "characters", "author's choices", "setting", "plot"),
        ela("RL.11-12.1", Grade.G12, "Reading Literature", "Cite strong and thorough textual evidence to support analysis of what the text says explicitly as well as inferences drawn from the text, including determining where the text leaves matters uncertain.", "textual evidence", "cite", "evidence", "inference"),
        ela("RL.11-12.2", Grade.G12, "Reading Literature", "Determine two or more themes or central ideas of a text and analyze their development over the course of the text, including how they interact and build on one another to produce a complex account; provide an objective summary of the text.", "theme", "central idea", "summary", "development"),
        ela("RL.11-12.3", Grade.G12, "Reading Literature", "Analyze the impact of the author's choices regarding how to develop and relate elements of a story or drama (e.g., where a story is set, how the action is ordered, how the characters are introduced and developed).", "characters", "author's choices", "setting", "plot"),
        ela("RL.6.2", Grade.G6, "Reading Literature", "Determine a theme or central idea of a text and how it is conveyed through particular details; provide a summary of the text distinct from personal opinions or judgments.", "theme", "central idea", "summary", "novel"),
        ela("RL.7.1", Grade.G7, "Reading Literature", "Cite several pieces of textual evidence to support analysis of what the text says explicitly as well as inferences drawn from the text.", "textual evidence", "cite", "evidence", "inference", "novel"),
        ela("RL.7.2", Grade.G7, "Reading Literature", "Determine a theme or central idea of a text and analyze its development over the course of the text; provide an objective summary of the text.", "theme", "central idea", "development", "summary", "novel"),
        ela("RL.7.3", Grade.G7, "Reading Literature", "Analyze how particular elements of a story or drama interact (e.g., how setting shapes the characters or plot).", "characters", "setting", "plot", "story elements", "novel"),
        ela("RL.8.1", Grade.G8, "Reading Literature", "Cite the textual evidence that most strongly supports an analysis of what the text says explicitly as well as inferences drawn from the text.", "textual evidence", "cite", "evidence", "inference", "novel"),
        ela("RL.8.2", Grade.G8, "Reading Literature", "Determine a theme or central idea of a text and analyze its development over the course of the text, including its relationship to the characters, setting, and plot; provide an objective summary of the text.", "theme", "central idea", "development", "summary", "novel"),
        ela("RL.8.3", Grade.G8, "Reading Literature", "Analyze how particular lines of dialogue or incidents in a story or drama propel the action, reveal aspects of a character, or provoke a decision.", "characters", "dialogue", "incidents", "plot", "novel"),
        ela("RL.9-10.1", Grade.G9, "Reading Literature", "Cite strong and thorough textual evidence to support analysis of what the text says explicitly as well as inferences drawn from the text.", "textual evidence", "cite", "evidence", "inference", "novel"),
        ela("RL.9-10.2", Grade.G9, "Reading Literature", "Determine a theme or central idea of a text and analyze in detail its development over the course of the text, including how it emerges and is shaped and refined by specific details; provide an objective summary of the text.", "theme", "central idea", "development", "summary", "novel"),
        ela("RL.9-10.3", Grade.G9, "Reading Literature", "Analyze how complex characters develop over the course of a text, interact with other characters, and advance the plot or develop the theme.", "characters", "character development", "plot", "novel"),
        ela("RL.9-10.1", Grade.G10, "Reading Literature", "Cite strong and thorough textual evidence to support analysis of what the text says explicitly as well as inferences drawn from the text.", "textual evidence", "cite", "evidence", "inference", "novel"),
        ela("RL.9-10.2", Grade.G10, "Reading Literature", "Determine a theme or central idea of a text and analyze in detail its development over the course of the text, including how it emerges and is shaped and refined by specific details; provide an objective summary of the text.", "theme", "central idea", "development", "summary", "novel"),
        ela("RL.9-10.3", Grade.G10, "Reading Literature", "Analyze how complex characters develop over the course of a text, interact with other characters, and advance the plot or develop the theme.", "characters", "character development", "plot", "novel"),
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
    /** The book the request is about, when there is one: its title is then the lesson's subject matter. */
    val book: BookRef? = null,
)

object StandardResolver {
    private fun normalizeCode(s: String): String =
        s.uppercase().replace("CCSS.", "").replace("ELA-LITERACY.", "").replace("MATH.CONTENT.", "").replace(Regex("[\\s_]"), "")

    /** For a book, the literature standard of this grade that the teacher's words point to; failing that, theme (".2"), which every grade's book study covers. */
    private fun forBook(pool: List<Standard>, hint: String): Standard? {
        val lit = pool.filter { it.strand == "Reading Literature" }
        if (lit.isEmpty()) return null
        val probe = Text.tokens(hint)
        val scored = lit.map { s -> s to s.keywords.count { k -> Text.tokens(k).let { t -> t.isNotEmpty() && t.all { it in probe } } } }.filter { it.second > 0 }.maxByOrNull { it.second }?.first
        return scored ?: lit.firstOrNull { it.shortCode.endsWith(".2") } ?: lit.first()
    }

    fun resolve(request: LessonRequest): Resolution {
        val input = request.standardInput.trim()
        val pool = StandardsCatalog.forGradeAndSubject(request.grade, request.subject)
        val book = BookResolver.resolve(request)

        if (input.isNotEmpty()) {
            val code = normalizeCode(input)
            // A code, in CCSS or Georgia form, for any grade: honor it even if it is not this request's grade.
            val direct = StandardsCatalog.all.firstOrNull { normalizeCode(it.ccss) == code || it.shortCode.uppercase() == code || it.georgiaCode.uppercase() == code }
            if (direct != null) return Resolution(direct, true, book?.title ?: Text.decap(direct.description.removeSuffix(".")), book)
        }

        // A book is the subject matter. Its standard is the literature standard the teacher's words point to (cite evidence, theme, characters).
        if (book != null) {
            val std = if (request.subject == Subject.ELA) forBook(pool, "${request.customPrompt} ${request.standardInput}") else null
            return Resolution(std, false, book.title, book)
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
        return Resolution(scored, false, topic, null)
    }
}
