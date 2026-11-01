package com.lessoncraft.engine

/** Grade bands the planner scales cognitive depth and reading level by. */
enum class GradeBand(val label: String) { K2("K-2"), G35("3-5"), G68("6-8"), G912("9-12") }

enum class Grade(val label: String, val short: String, val band: GradeBand) {
    K("Kindergarten", "K", GradeBand.K2),
    G1("Grade 1", "1", GradeBand.K2),
    G2("Grade 2", "2", GradeBand.K2),
    G3("Grade 3", "3", GradeBand.G35),
    G4("Grade 4", "4", GradeBand.G35),
    G5("Grade 5", "5", GradeBand.G35),
    G6("Grade 6", "6", GradeBand.G68),
    G7("Grade 7", "7", GradeBand.G68),
    G8("Grade 8", "8", GradeBand.G68),
    G9("Grade 9", "9", GradeBand.G912),
    G10("Grade 10", "10", GradeBand.G912),
    G11("Grade 11", "11", GradeBand.G912),
    G12("Grade 12", "12", GradeBand.G912);

    /** Kindergarten and Grade 1 have the strictest guardrails: decodable student text and only task types the page can really print. */
    val isEarly: Boolean get() = this == K || this == G1

    companion object {
        fun fromLabel(label: String): Grade? = entries.firstOrNull { it.label == label || it.name == label || it.short == label }
    }
}

enum class Subject(val label: String) {
    MATH("Math"), SCIENCE("Science"), ELA("ELA"), SOCIAL_STUDIES("Social Studies");

    companion object {
        fun fromLabel(label: String): Subject? = entries.firstOrNull { it.label == label || it.name == label }
    }
}

/** Bloom's Taxonomy (revised), lowest to highest. */
enum class Bloom(val label: String) {
    REMEMBER("Remember"), UNDERSTAND("Understand"), APPLY("Apply"), ANALYZE("Analyze"), EVALUATE("Evaluate"), CREATE("Create");

    companion object {
        fun fromLabel(label: String): Bloom? = entries.firstOrNull { it.label.equals(label, true) || it.name.equals(label, true) }
    }
}

/** What the teacher typed. Either text field may be empty, but not both: the standard/topic is the anchor, the custom prompt the creative layer. */
data class LessonRequest(
    val grade: Grade,
    val subject: Subject,
    val standardInput: String = "",
    val customPrompt: String = "",
) {
    val hasInput: Boolean get() = standardInput.isNotBlank() || customPrompt.isNotBlank()
}

/** An official standard from the built-in catalog. */
data class Standard(
    val ccss: String,
    val description: String,
    val grade: Grade,
    val subject: Subject,
    val strand: String,
    val keywords: List<String>,
) {
    /** The thing the standard is about, in a few plain words ("retelling", "fractions"): used in student prompts and titles. */
    val focus: String get() = keywords.first()

    /** "RL.1.2" */
    val shortCode: String get() = ccss.removePrefix("CCSS.ELA-LITERACY.").removePrefix("CCSS.MATH.CONTENT.")

    /** Georgia's code for the same standard: ELAGSE1RL2, MGSE1.OA.1 (Georgia drops the cluster letter in math). */
    val georgiaCode: String
        get() = when (subject) {
            Subject.ELA -> {
                val p = shortCode.split(".")
                "ELAGSE${p[1]}${p[0]}${p[2]}"
            }
            Subject.MATH -> {
                val p = shortCode.split(".")
                "MGSE${p[0]}.${p[1]}.${p[3]}"
            }
            else -> ccss
        }
}

/** The creative constraints one idea was written under, tracked per session so a new round is built on different ground. */
data class Lens(val angle: String, val hook: String, val theme: String, val text: String?)

data class Idea(
    val title: String,
    val summary: String,
    val hook: String,
    val lens: Lens,
)

/** An idea the teacher has already seen this session. */
data class SeenIdea(val title: String, val hook: String, val summary: String, val lens: Lens)

data class Udl(val engagement: String, val representation: String, val actionExpression: String)

data class Step(
    val phase: String,
    val minutes: Int,
    val title: String,
    val description: String,
    val bloom: Bloom,
    val udl: Udl,
)

/** Something drawn on the page by the app rather than typed: counters for counting tasks. Never a picture the page might not have. */
sealed interface Visual {
    data class Dots(val count: Int) : Visual
}

enum class ItemKind { WRITE_WORD, WORD_BANK, TRACE, DRAW_BOX, COUNT, TEACHER_ORAL, QUESTION, SENTENCE_FRAME, DATA_TABLE, SEQUENCE }

data class StudentItem(
    val number: Int,
    val kind: ItemKind,
    /** Exactly what the student reads. For Kindergarten and Grade 1 this is decodable. */
    val prompt: String,
    val bloom: Bloom,
    val answer: String,
    /** What the teacher says, when the task is teacher-led ("[TEACHER SCRIPT / READ ALOUD]"). */
    val teacherScript: String? = null,
    val visual: Visual? = null,
)

data class Differentiation(val iep504: String, val onLevel: String, val enrichment: String)

data class Lesson(
    val title: String,
    val grade: Grade,
    val subject: Subject,
    val standard: Standard?,
    val topic: String,
    val customNotes: String,
    val objective: String,
    val objectiveBloom: Bloom,
    val materials: List<String>,
    val udlPlan: Udl,
    val sequence: List<Step>,
    val items: List<StudentItem>,
    val differentiation: Differentiation,
    val tags: List<String>,
) {
    val totalMinutes: Int get() = sequence.sumOf { it.minutes }
    val standardLabel: String get() = standard?.let { "${it.georgiaCode} (${it.ccss})" } ?: "No catalog standard matched"
}
