package com.lessoncraft.engine

/**
 * What the app knows about a book, offline. Only facts that are well established (author, setting, main characters, themes) and discussion
 * questions that rest on them. Nothing here retells plot or invents chapters: a lesson built on a book that is not in this catalog says so and
 * leaves chapter and passage choices to the teacher's own copy.
 */
data class BookInfo(
    val title: String,
    val author: String,
    val gradeSpan: String,
    val genre: String,
    val setting: String,
    val characters: List<String>,
    val themes: List<String>,
    /** Open questions that can be answered from the text, in the order a class usually meets them. */
    val questions: List<String>,
    /** Short, decodable words from a picture book that a Kindergarten or Grade 1 child can trace and write. */
    val earlyWords: List<String> = emptyList(),
    val aliases: List<String> = emptyList(),
)

/** The book a request is about: the title as typed (or as catalogued), the author if known, and the catalog entry if there is one. */
data class BookRef(val title: String, val author: String?, val info: BookInfo?) {
    val known: Boolean get() = info != null
    fun describe(): String = if (author != null) "$title by $author" else title
    val characters: List<String> get() = info?.characters ?: emptyList()
    val themes: List<String> get() = info?.themes ?: emptyList()
}

object BookCatalog {
    val all: List<BookInfo> = listOf(
        BookInfo(
            "The Outsiders", "S. E. Hinton", "7-9", "realistic fiction novel", "Tulsa, Oklahoma, in the 1960s",
            listOf("Ponyboy Curtis", "Sodapop Curtis", "Darry Curtis", "Johnny Cade", "Dally Winston", "Two-Bit Mathews", "Cherry Valance"),
            listOf("social class and conflict", "loyalty and friendship", "identity", "family", "violence and its consequences"),
            listOf(
                "How do the Greasers and the Socs show loyalty to their own groups?",
                "What does \"Stay gold\" mean to Johnny, and what does it come to mean to Ponyboy?",
                "How does Ponyboy's view of the Socs change as the story goes on?",
                "Which character's choices do you think matter most to the story's outcome, and why?",
            ),
            aliases = listOf("outsiders"),
        ),
        BookInfo(
            "To Kill a Mockingbird", "Harper Lee", "8-10", "novel", "Maycomb, Alabama, in the 1930s",
            listOf("Scout Finch", "Jem Finch", "Atticus Finch", "Boo Radley", "Tom Robinson", "Calpurnia"),
            listOf("justice and prejudice", "empathy", "courage", "loss of innocence"),
            listOf(
                "What does Atticus teach Scout about understanding other people?",
                "How does Scout's view of Boo Radley change?",
                "What does the title's mockingbird stand for?",
            ),
            aliases = listOf("mockingbird"),
        ),
        BookInfo(
            "The Giver", "Lois Lowry", "7-8", "science fiction novel", "a community with strict rules and no color or memory of the past",
            listOf("Jonas", "The Giver", "Fiona", "Asher", "Lily", "Gabriel"),
            listOf("freedom and control", "memory", "individuality", "the cost of sameness"),
            listOf(
                "What does the community give up to keep everyone safe?",
                "How does Jonas change after he becomes the Receiver?",
                "Is the community's way of life worth what it costs? Why or why not?",
            ),
        ),
        BookInfo(
            "Hatchet", "Gary Paulsen", "6-8", "survival adventure novel", "the Canadian wilderness",
            listOf("Brian Robeson"),
            listOf("survival", "self-reliance", "resilience", "learning from mistakes"),
            listOf(
                "What does Brian learn from his mistakes?",
                "How does Brian change as he survives alone?",
            ),
        ),
        BookInfo(
            "Holes", "Louis Sachar", "5-7", "novel", "Camp Green Lake, a dry desert juvenile detention camp",
            listOf("Stanley Yelnats", "Zero (Hector Zeroni)", "Mr. Sir", "the Warden"),
            listOf("fate and luck", "friendship", "injustice", "persistence"),
            listOf(
                "How does Stanley's friendship with Zero change both of them?",
                "What parts of the story show fate or luck at work?",
            ),
        ),
        BookInfo(
            "Wonder", "R. J. Palacio", "4-6", "realistic fiction novel", "a middle school in New York",
            listOf("August \"Auggie\" Pullman", "Via Pullman", "Jack Will", "Summer", "Julian"),
            listOf("kindness", "acceptance", "seeing from other points of view"),
            listOf(
                "How does the story change when a different character tells it?",
                "What choices show kindness, and what choices do not?",
            ),
        ),
        BookInfo(
            "Charlotte's Web", "E. B. White", "2-4", "animal fantasy novel", "the Zuckerman farm",
            listOf("Wilbur", "Charlotte", "Fern", "Templeton"),
            listOf("friendship", "loyalty", "helping others", "life and death"),
            listOf(
                "How does Charlotte help Wilbur?",
                "What makes Charlotte a true friend?",
            ),
            aliases = listOf("charlottes web"),
        ),
        BookInfo(
            "Brown Bear, Brown Bear, What Do You See?", "Bill Martin Jr.", "K-1", "picture book with a repeating pattern", "a pattern of animals and colors",
            listOf("the animals in the book"),
            listOf("patterns", "colors", "animals"),
            listOf("What is the same on every page?", "What do you think the next animal will be?"),
            earlyWords = listOf("bear", "see"),
            aliases = listOf("brown bear"),
        ),
        BookInfo(
            "Where the Wild Things Are", "Maurice Sendak", "K-2", "picture book", "Max's bedroom and an island of wild things",
            listOf("Max", "the wild things"),
            listOf("imagination", "big feelings", "home"),
            listOf("How does Max feel at the start and at the end?", "Why does Max go home?"),
            earlyWords = listOf("home", "wild"),
        ),
    )

    private fun norm(s: String): String = " " + Text.words(s.replace("'", "")).joinToString(" ") + " "

    /** Words that wrap a title without being part of it: "novel study: The Giver". */
    private val FILLER = setOf("novel", "book", "study", "unit", "read", "aloud", "reading", "class", "lesson", "the", "a", "an", "of", "on", "about", "for", "by")

    fun byTitle(title: String): BookInfo? {
        val t = norm(title).trim()
        val noThe = t.removePrefix("the ").trim()
        return all.firstOrNull { b -> norm(b.title).trim().let { it == t || it.removePrefix("the ").trim() == noThe } || b.aliases.any { norm(it).trim() == noThe } }
    }

    /**
     * Find a catalogued book mentioned in free text. A title made of one distinctive word ("Holes", "Wonder", "Hatchet", "The Giver") is common
     * language, so it only counts when the text is essentially just the title (plus words like "novel study"), never inside a longer sentence.
     */
    fun find(text: String): BookInfo? {
        val n = norm(text)
        val core = Text.words(text.replace("'", "")).filter { it !in FILLER }.joinToString(" ")
        return all.firstOrNull { b ->
            val titleWords = Text.words(b.title.replace("'", "")).filter { it !in setOf("the", "a", "an") }
            val phrases = (listOf(b.title) + b.aliases).map { norm(it) }
            if (titleWords.size >= 2) phrases.any { n.contains(it) } && !(b.aliases.isEmpty() && false)
            else core == titleWords.joinToString(" ") || b.aliases.any { Text.words(it).joinToString(" ") == core }
        }
    }
}

object BookResolver {
    private val BY = Regex("^(?:novel study|book study|literature circle|unit|reading|read aloud|novel|book)?\\s*[:\\-]?\\s*(.{2,80}?)\\s+by\\s+([A-Z][\\w.'-]+(?:\\s+[A-Z][\\w.'-]+){0,3})\\s*$", RegexOption.IGNORE_CASE)
    private val QUOTED = Regex("[\"“*_]([A-Z][^\"”*_]{3,80}?)[\"”*_]")

    private fun ref(title: String, author: String? = null): BookRef {
        val info = BookCatalog.byTitle(title)
        return BookRef(info?.title ?: title.trim(), author ?: info?.author, info)
    }

    /** The book this request is about, from the Book field, a "Title by Author" in the topic box, a catalogued title, or a quoted title. */
    fun resolve(r: LessonRequest): BookRef? {
        if (r.bookTitle.isNotBlank()) return ref(r.bookTitle.trim().removeSurrounding("\""))
        val std = r.standardInput.trim()
        if (std.isNotEmpty()) {
            BY.matchEntire(std)?.let { m ->
                val title = m.groupValues[1].trim().removeSurrounding("\"")
                // "Fractions by Ms. Lee" is a handout, not a book: require a title-like phrase (capitalized words) or a catalogued book.
                if (BookCatalog.byTitle(title) != null || title.split(" ").count { it.firstOrNull()?.isUpperCase() == true } >= 1 && title.first().isUpperCase()) return ref(title, m.groupValues[2].trim())
            }
            BookCatalog.find(std)?.let { return ref(it.title) }
        }
        val custom = r.customPrompt.trim()
        if (custom.isNotEmpty()) {
            BookCatalog.find(custom)?.let { return ref(it.title) }
            QUOTED.find(custom)?.groupValues?.get(1)?.takeIf { it.trim().split(" ").size >= 2 }?.let { return ref(it) }
        }
        QUOTED.find(std)?.groupValues?.get(1)?.takeIf { it.trim().split(" ").size >= 2 }?.let { return ref(it) }
        return null
    }
}
