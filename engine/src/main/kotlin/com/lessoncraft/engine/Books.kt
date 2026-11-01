package com.lessoncraft.engine

/**
 * What the app knows about a book, offline. Only facts that are well established (author, setting, main characters, themes) and discussion
 * questions that rest on them. Nothing here retells plot or invents chapters: a lesson built on a book that is not in this catalog says so and
 * leaves chapter and passage choices to the teacher's own copy.
 */
data class BookInfo(
    val title: String,
    /** Null when the built-in list has the title but no verified author: the teacher fills it in. */
    val author: String?,
    /** Where the book is usually taught. A label only: it never limits which grade a book can be planned for. */
    val typicalGrades: String,
    val genre: String,
    val setting: String,
    val characters: List<String>,
    val themes: List<String>,
    /** Open questions that can be answered from the text, for older readers. Younger grades get age-appropriate questions built from the facts. */
    val questions: List<String>,
    /** Short words from a picture book that a Kindergarten or Grade 1 child can trace and write. */
    val earlyWords: List<String> = emptyList(),
    val aliases: List<String> = emptyList(),
    /** False for titles that are listed so they are recognized, but whose details have not been verified. No facts are invented for them. */
    val verified: Boolean = true,
    /** "the Pigeon series": a series the title belongs to or stands for. */
    val series: String? = null,
)

/** The book a request is about: the title as typed (or as catalogued), the author if known, and the catalog entry if there is one. */
data class BookRef(val title: String, val author: String?, val info: BookInfo?) {
    /** Verified facts (author, characters, themes) are available only for catalogued books whose details are verified. */
    val known: Boolean get() = info?.verified == true
    fun describe(): String = (if (author != null) "$title by $author" else title) + (info?.series?.let { " (and $it)" } ?: "")
    val characters: List<String> get() = if (known) info!!.characters else emptyList()
    val themes: List<String> get() = if (known) info!!.themes else emptyList()
}

object BookCatalog {
    val all: List<BookInfo> = listOf(
        /* ---------- Seasonal and early-reader favorites ---------- */
        BookInfo(
            "Fly Guy", "Tedd Arnold", "K-2", "early reader series", "everyday places around Buzz's home and town",
            listOf("Buzz", "Fly Guy"),
            listOf("friendship", "having a pet", "being different", "problem solving"),
            listOf("What do Buzz and Fly Guy do to help each other?", "How can you tell Fly Guy and Buzz are friends?"),
            earlyWords = listOf("fly", "guy"), aliases = listOf("hi fly guy"), series = "the Fly Guy series",
        ),
        BookInfo(
            "The Pumpkin Thief", null, "K-2", "seasonal (autumn) picture book", "",
            emptyList(), emptyList(), emptyList(), earlyWords = listOf("pumpkin"), verified = false,
        ),
        BookInfo(
            "Don't Let the Pigeon Drive the Bus!", "Mo Willems", "K-2", "picture book", "a bus and the page, where the Pigeon talks straight to the reader",
            listOf("the Pigeon", "the bus driver"),
            listOf("wanting and rules", "persuasion", "big feelings", "speaking to the reader", "humor"),
            listOf("What does the Pigeon say to try to change the reader's mind?", "How does the Pigeon feel at the end, and how can you tell from the words?"),
            earlyWords = listOf("bus", "pigeon"), aliases = listOf("dont let the pigeon"), series = "the Pigeon series",
        ),
        BookInfo(
            "Pig Elf", null, "K-2", "seasonal (holiday) picture book", "",
            emptyList(), emptyList(), emptyList(), earlyWords = listOf("pig", "elf"), verified = false,
        ),
        BookInfo(
            "The Very Hungry Caterpillar", "Eric Carle", "K-1", "picture book", "a leaf, a week of days, and a cocoon",
            listOf("the caterpillar"),
            listOf("the life cycle and change", "counting", "the days of the week", "growing up"),
            listOf("What does the caterpillar eat on each day, in order?", "How does the caterpillar change by the end of the book?"),
            earlyWords = listOf("eat", "egg"),
        ),
        BookInfo(
            "Brown Bear, Brown Bear, What Do You See?", "Bill Martin Jr. & Eric Carle", "K-1", "picture book with a repeating pattern", "a pattern of animals and colors",
            listOf("the animals in the book"),
            listOf("patterns", "colors", "animals"),
            listOf("What is the same on every page?", "What do you think the next animal will be?"),
            earlyWords = listOf("bear", "see"), aliases = listOf("brown bear"),
        ),
        BookInfo(
            "Where the Wild Things Are", "Maurice Sendak", "K-2", "picture book", "Max's bedroom and an island of wild things",
            listOf("Max", "the wild things"),
            listOf("imagination", "big feelings", "home"),
            listOf("How does Max feel at the start and at the end?", "Why does Max go home?"),
            earlyWords = listOf("home", "wild"),
        ),
        BookInfo(
            "Make Way for Ducklings", "Robert McCloskey", "K-2", "picture book", "Boston, Massachusetts",
            listOf("Mr. Mallard", "Mrs. Mallard", "Michael the policeman", "the ducklings"),
            listOf("family and safety", "community helpers", "kindness", "finding a home"),
            listOf("What problem do the ducks face, and who helps them?", "Why does Mrs. Mallard keep looking for a place to nest?"),
            earlyWords = listOf("duck", "quack"),
        ),
        BookInfo(
            "Corduroy", "Don Freeman", "K-2", "picture book", "a department store and a girl's home",
            listOf("Corduroy", "Lisa"),
            listOf("friendship", "belonging", "being loved as you are", "wanting something"),
            listOf("What is Corduroy missing, and how does he try to find it?", "Why does Lisa want Corduroy?"),
            earlyWords = listOf("bear", "button"),
        ),

        /* ---------- Middle grade, young adult and classics: any grade can plan them ---------- */
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
            listOf("What does Brian learn from his mistakes?", "How does Brian change as he survives alone?"),
        ),
        BookInfo(
            "Holes", "Louis Sachar", "5-7", "novel", "Camp Green Lake, a dry desert juvenile detention camp",
            listOf("Stanley Yelnats", "Zero (Hector Zeroni)", "Mr. Sir", "the Warden"),
            listOf("fate and luck", "friendship", "injustice", "persistence"),
            listOf("How does Stanley's friendship with Zero change both of them?", "What parts of the story show fate or luck at work?"),
        ),
        BookInfo(
            "Wonder", "R. J. Palacio", "4-6", "realistic fiction novel", "a middle school in New York",
            listOf("August \"Auggie\" Pullman", "Via Pullman", "Jack Will", "Summer", "Julian"),
            listOf("kindness", "acceptance", "seeing from other points of view"),
            listOf("How does the story change when a different character tells it?", "What choices show kindness, and what choices do not?"),
        ),
        BookInfo(
            "Number the Stars", "Lois Lowry", "5-7", "historical fiction novel", "Copenhagen, Denmark, in 1943 during the German occupation",
            listOf("Annemarie Johansen", "Ellen Rosen", "Kirsti Johansen", "Peter Neilsen", "Uncle Henrik"),
            listOf("courage", "friendship", "resistance", "sacrifice"),
            listOf("What acts of courage do Annemarie and her family show?", "How does the friendship between Annemarie and Ellen shape Annemarie's choices?"),
        ),
        BookInfo(
            "Roll of Thunder, Hear My Cry", "Mildred D. Taylor", "6-9", "historical fiction novel", "rural Mississippi in the 1930s",
            listOf("Cassie Logan", "Stacey Logan", "Little Man Logan", "Christopher-John Logan", "Papa (David Logan)", "Mama (Mary Logan)", "Big Ma"),
            listOf("racism and injustice", "land and independence", "family pride", "dignity and courage"),
            listOf("Why does the Logan family's land matter so much?", "How does Cassie's understanding of the world around her change?"),
        ),
        BookInfo(
            "A Wrinkle in Time", "Madeleine L'Engle", "5-8", "science fantasy novel", "the Murry home, other planets, and the planet Camazotz",
            listOf("Meg Murry", "Charles Wallace Murry", "Calvin O'Keefe", "Mrs. Whatsit", "Mrs. Who", "Mrs. Which"),
            listOf("good and evil", "conformity and individuality", "the power of love", "courage"),
            listOf("What makes Meg's strengths and flaws matter to the rescue?", "What does the planet Camazotz show about conformity?"),
            aliases = listOf("wrinkle in time"),
        ),
        BookInfo(
            "The Lightning Thief", "Rick Riordan", "5-8", "fantasy adventure novel (the first Percy Jackson book)", "modern America and Camp Half-Blood, with Greek myths",
            listOf("Percy Jackson", "Annabeth Chase", "Grover Underwood", "Luke Castellan"),
            listOf("identity", "heroism", "friendship", "family"),
            listOf("How does Percy learn who he is?", "How do Percy, Annabeth and Grover each help the quest?"),
            aliases = listOf("percy jackson", "percy jackson and the lightning thief"),
        ),
        BookInfo(
            "The Hunger Games", "Suzanne Collins", "8-10", "dystopian novel", "Panem, a nation of districts ruled by the Capitol",
            listOf("Katniss Everdeen", "Peeta Mellark", "Gale Hawthorne", "Primrose Everdeen", "Haymitch Abernathy", "President Snow", "Rue"),
            listOf("survival", "power and control", "sacrifice", "inequality", "entertainment and media"),
            listOf("What choices does Katniss make to protect others?", "How does the Capitol use the Games to control the districts?"),
        ),
        BookInfo(
            "Fahrenheit 451", "Ray Bradbury", "9-11", "dystopian novel", "a future city where firemen burn books",
            listOf("Guy Montag", "Clarisse McClellan", "Mildred Montag", "Captain Beatty", "Faber"),
            listOf("censorship", "knowledge and ignorance", "technology and conformity", "individuality"),
            listOf("Why does the society burn books?", "What causes Montag to question his work?"),
            aliases = listOf("fahrenheit"),
        ),
        BookInfo(
            "Of Mice and Men", "John Steinbeck", "9-11", "novella", "a California ranch in the Salinas Valley during the Great Depression",
            listOf("George Milton", "Lennie Small", "Candy", "Curley", "Curley's wife", "Slim", "Crooks"),
            listOf("friendship", "dreams", "loneliness", "powerlessness"),
            listOf("What does the dream of owning land mean to George and Lennie?", "Why are so many characters lonely?"),
        ),
        BookInfo(
            "Lord of the Flies", "William Golding", "9-11", "novel", "an uninhabited island where a group of boys is stranded",
            listOf("Ralph", "Piggy", "Jack Merridew", "Simon", "Roger"),
            listOf("civilization and savagery", "loss of innocence", "power and leadership", "fear"),
            listOf("What does the conch stand for, and what happens to it?", "How do Ralph and Jack lead differently?"),
        ),
        BookInfo(
            "Romeo and Juliet", "William Shakespeare", "8-10", "tragedy (play)", "Verona, Italy",
            listOf("Romeo Montague", "Juliet Capulet", "Friar Laurence", "Mercutio", "Tybalt", "Benvolio", "the Nurse"),
            listOf("love", "fate", "family feud", "impulsiveness"),
            listOf("How does the feud between the two families shape what happens?", "Which choices are made in haste, and what do they cost?"),
            aliases = listOf("romeo juliet"),
        ),
        BookInfo(
            "The Great Gatsby", "F. Scott Fitzgerald", "10-12", "novel", "Long Island and New York City in the 1920s",
            listOf("Jay Gatsby", "Nick Carraway", "Daisy Buchanan", "Tom Buchanan", "Jordan Baker", "Myrtle Wilson"),
            listOf("the American Dream", "wealth and class", "love and obsession", "illusion and reality"),
            listOf("What does Gatsby want, and why can he not have it?", "How does Nick's point of view shape what we learn?"),
            aliases = listOf("gatsby"),
        ),
    )

    /** Catalogued titles that match what the teacher has typed so far, for suggestions in the Book field. Any other title can still be typed in full. */
    fun suggest(query: String, limit: Int = 5): List<BookInfo> {
        val q = norm(query).trim().removePrefix("the ").trim()
        if (q.length < 2) return emptyList()
        return all.filter { b ->
            val names = listOf(b.title) + b.aliases
            names.any { n -> norm(n).trim().removePrefix("the ").let { it.startsWith(q) || it.contains(" $q") } }
        }.take(limit)
    }

    private fun norm(s: String): String = " " + Text.words(s.replace("'", "")).joinToString(" ") + " "

    /** Words that wrap a title without being part of it: "novel study: The Giver". */
    private val FILLER = setOf("novel", "book", "study", "unit", "read", "aloud", "reading", "class", "lesson", "the", "a", "an", "of", "on", "about", "for", "by")

    fun byTitle(title: String): BookInfo? {
        val t = norm(title).trim().removeSuffix(" series").trim()
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


/**
 * Pairs any book with any grade. Nothing about a book limits the grade it can be planned for: this turns the same facts into questions pitched at
 * the target grade's cognitive demand, so a picture book can be taught with grade-12 rigor and a novel read aloud to Kindergarten.
 */
object BookScaling {
    /** The grade range a teacher would usually find the book in, e.g. "7-9". Null when the entry is unlisted. */
    private fun typicalRange(info: BookInfo?): IntRange? {
        val m = Regex("^(K|\\d+)-(\\d+)$").matchEntire(info?.typicalGrades ?: return null) ?: return null
        val lo = if (m.groupValues[1] == "K") 0 else m.groupValues[1].toInt()
        return lo..m.groupValues[2].toInt()
    }

    /** Whether the catalog's open questions are pitched at older readers (a chapter book), not simple picture-book questions. */
    fun hasOlderQuestions(book: BookRef): Boolean = book.known && (typicalRange(book.info)?.first ?: 0) >= 4 && book.info!!.questions.isNotEmpty()

    private fun gradeNumber(g: Grade) = if (g == Grade.K) 0 else g.short.toInt()

    /** A one-sentence note when the grade is outside where the book is usually taught; null when it is typical or unknown. */
    fun note(book: BookRef, grade: Grade): String? {
        val r = typicalRange(book.info) ?: return null
        val n = gradeNumber(grade)
        if (n in r) return null
        val usual = book.info!!.typicalGrades
        val how = when {
            n < r.first -> "so the reading is shared and the thinking is simplified"
            else -> "so the thinking is raised to match"
        }
        return "${book.title} is usually taught in grades $usual; this plan is scaled for ${grade.label}, $how."
    }

    /** The opening question for a lesson on the book, pitched at the grade. Facts come only from the catalog; an unverified or unknown book gets general questions. */
    fun hookQuestion(book: BookRef, grade: Grade, index: Int): String {
        val a = book.characters.firstOrNull()
        val setting = book.info?.setting?.takeIf { it.isNotBlank() && book.known }
        val th = book.themes.firstOrNull()
        val t = book.title
        val older = book.info?.questions?.takeIf { hasOlderQuestions(book) }
        fun pick(options: List<String>) = options[index % options.size]
        return when (grade.band) {
            GradeBand.K2 -> pick(listOfNotNull(
                "Read aloud the first page of $t and ask: who is in the story, and where are they?",
                a?.let { "Read aloud a short part of $t that you choose and ask: how do you think $it feels? Which words tell you?" },
                setting?.let { "Ask: where does $t take place? Read the opening aloud and have students raise a hand when they hear a clue." },
                "Read aloud a short part of $t that you choose and ask: what happens first, next and last?",
            ))
            GradeBand.G35 -> pick(listOfNotNull(
                a?.let { "Ask: what do we learn about $it in the first pages of $t? Have students point to the words that tell them." },
                "Ask: what problem does the main character face in $t, and what clues in the text point to it?",
                "Ask students to predict how $t will end, then collect the predictions to check against the text.",
                th?.let { "Ask: what lesson about $it might the author want us to take from $t? Keep a list to test as you read." },
            ))
            GradeBand.G68 -> pick(older?.map { "Open with a question from the book and let students answer from memory first: $it" } ?: listOfNotNull(
                th?.let { "Pose a question students can argue from evidence: how does $it develop in $t?" },
                "Ask: what do you notice about how $t begins, and what does it make you expect? Return to this after the first chapter.",
            ))
            GradeBand.G912 -> pick(older?.map { "$it Require evidence from at least two places in the text." } ?: listOfNotNull(
                th?.let { "Ask: to what extent does $t support or challenge a common belief about $it? Require evidence from at least two places." },
                "Ask: what choices has the author made in the opening of $t, and how do they shape what the reader expects?",
            ))
        }
    }
}
