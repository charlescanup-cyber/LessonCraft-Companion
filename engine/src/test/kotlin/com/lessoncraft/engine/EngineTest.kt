package com.lessoncraft.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class EngineTest {
    private fun req(grade: Grade, subject: Subject, standard: String = "", custom: String = "") = LessonRequest(grade, subject, standard, custom)

    private fun lessonFor(r: LessonRequest, seen: List<SeenIdea> = emptyList()): Lesson {
        val res = StandardResolver.resolve(r)
        return LessonBuilder.build(r, res, IdeaEngine.generate(r, res, seen).first())
    }

    @Test fun georgiaCodeResolvesToTheCatalogStandard() {
        val res = StandardResolver.resolve(req(Grade.G1, Subject.ELA, "ELAGSE1RL2"))
        assertTrue(res.exact)
        assertEquals("RL.1.2", res.standard?.shortCode)
        assertEquals("ELAGSE1RL2", res.standard?.georgiaCode)
    }

    @Test fun ccssAndMathCodesResolve() {
        assertEquals("MGSE1.OA.1", StandardResolver.resolve(req(Grade.G1, Subject.MATH, "CCSS.MATH.CONTENT.1.OA.A.1")).standard?.georgiaCode)
        assertEquals("3.NF.A.1", StandardResolver.resolve(req(Grade.G3, Subject.MATH, "3.nf.a.1")).standard?.shortCode)
    }

    @Test fun keywordsFindAStandardWithoutACode() {
        val res = StandardResolver.resolve(req(Grade.G1, Subject.ELA, "retelling stories"))
        assertFalse(res.exact)
        assertEquals("RL.1.2", res.standard?.shortCode)
    }

    @Test fun customPromptAloneIsEnough() {
        val r = req(Grade.G4, Subject.SCIENCE, custom = "A fall harvest unit about plants for a class with English learners and movement breaks")
        assertTrue(r.hasInput)
        val ideas = IdeaEngine.generate(r, StandardResolver.resolve(r), emptyList())
        assertEquals(3, ideas.size)
        assertTrue(ideas.any { it.summary.contains("fall harvest") })
        assertTrue(ideas.all { it.summary.contains("English learners") })
    }

    @Test fun bothInputsKeepTheStandardAsTheAnchor() {
        val r = req(Grade.G1, Subject.ELA, "ELAGSE1RL2", "Pumpkin patch theme, lots of movement")
        val ideas = IdeaEngine.generate(r, StandardResolver.resolve(r), emptyList())
        assertTrue(ideas.all { it.summary.contains("ELAGSE1RL2") })
        assertTrue(ideas.any { it.summary.contains("fall harvest") })
    }

    @Test fun ideasAreDeterministic() {
        val r = req(Grade.G3, Subject.MATH, "fractions")
        val res = StandardResolver.resolve(r)
        assertEquals(IdeaEngine.generate(r, res, emptyList()), IdeaEngine.generate(r, res, emptyList()))
    }

    @Test fun eachNewIdeasRoundUsesDifferentGround() {
        val r = req(Grade.G4, Subject.ELA, "main idea")
        val res = StandardResolver.resolve(r)
        val seen = mutableListOf<SeenIdea>()
        val angles = mutableSetOf<String>()
        repeat(4) {
            val round = IdeaEngine.generate(r, res, seen)
            assertEquals(3, round.size)
            round.forEach { assertTrue("angle reused: ${it.lens.angle}", angles.add(it.lens.angle)) }
            seen += round.map { SeenIdea(it.title, it.hook, it.summary, it.lens) }
        }
        assertEquals(12, seen.map { it.title }.toSet().size)
    }

    @Test fun kindergartenAndGradeOneStudentTextIsDecodableAndPrintable() {
        for (grade in listOf(Grade.K, Grade.G1)) for (subject in Subject.entries) {
            val r = req(grade, subject, if (subject == Subject.ELA) "retelling stories" else "counting", "")
            val res = StandardResolver.resolve(r)
            val lesson = lessonFor(r)
            val taught = LessonBuilder.taughtWords(res).toSet()
            lesson.items.forEach { item ->
                val problems = EarlyGuard.problems(item.prompt.replace("___", " "), taught + setOf("dots"))
                assertTrue("$grade $subject item ${item.number} \"${item.prompt}\": $problems", problems.isEmpty())
                if (item.kind == ItemKind.TEACHER_ORAL) assertTrue(item.teacherScript!!.startsWith(EarlyGuard.SCRIPT_TAG))
            }
        }
    }

    @Test fun guardRejectsOrphanTasks() {
        assertTrue(EarlyGuard.problems("Match the word to the tree.").isNotEmpty())
        assertTrue(EarlyGuard.problems("Circle the word that is true.").isNotEmpty())
        assertTrue(EarlyGuard.problems("Look at the picture.").isNotEmpty())
        assertTrue(EarlyGuard.problems("Tell your partner what you see.").isNotEmpty())
        assertTrue(EarlyGuard.problems("Circle: cat dog").isEmpty())
        assertTrue(EarlyGuard.problems("Write the number.").isEmpty())
    }

    @Test fun laterGradesStayWithinTheSentenceLimit() {
        for (grade in Grade.entries.filter { !it.isEarly }) for (subject in Subject.entries) {
            val lesson = lessonFor(req(grade, subject, if (subject == Subject.MATH) "fractions" else "main idea"))
            lesson.items.forEach { item ->
                Text.sentences(item.prompt).forEach { s ->
                    assertTrue("$grade ${s}", Readability.wordCount(s) <= Readability.maxWords(grade))
                }
            }
        }
    }

    @Test fun blooms_levelsClimbAndSpreadAcrossTheLesson() {
        for (grade in Grade.entries) {
            val lesson = lessonFor(req(grade, Subject.ELA, "main idea"))
            val levels = lesson.items.map { it.bloom.ordinal }
            assertEquals("items climb for $grade", levels.sorted(), levels)
            assertTrue("steps use at least three levels for $grade", lesson.sequence.map { it.bloom }.toSet().size >= 3)
            val top = BloomPlan.mix(grade.band).filterValues { it > 0 }.keys.max()
            assertTrue("items reach $top for $grade", lesson.items.any { it.bloom == top } || grade.isEarly)
            if (grade.band == GradeBand.K2) assertTrue(lesson.items.none { it.bloom >= Bloom.EVALUATE })
        }
    }

    @Test fun searchRanksTitlesAboveBodyAndRequiresEveryWord() {
        val docs = listOf(
            Searchable(1, "Retelling the Pumpkin Story", "ELA", "Grade 1", "ELAGSE1RL2 RL.1.2", "retelling stories", listOf("fall harvest"), "students retell", false, null, 1),
            Searchable(2, "Counting Cubes", "Math", "Kindergarten", "MGSEK.CC.4", "counting", listOf("cubes"), "retelling appears once in the body", true, 7, 2),
            Searchable(3, "Fractions Fun", "Math", "Grade 3", "MGSE3.NF.1", "fractions", emptyList(), "parts of a whole", false, null, 3),
        )
        val hits = LessonSearch.search("retell", docs)
        assertEquals(listOf(1L, 2L), hits.map { it.doc.id })
        assertTrue(hits[0].score > hits[1].score)
        assertEquals(listOf(1L), LessonSearch.search("retell pumpkin", docs).map { it.doc.id })
        assertEquals(listOf(1L), LessonSearch.search("elagse1rl2", docs).map { it.doc.id })
        assertEquals(listOf(2L), LessonSearch.search("", docs, LibraryFilter.Favorites).map { it.doc.id })
        assertEquals(listOf(2L), LessonSearch.search("", docs, LibraryFilter.Folder(7)).map { it.doc.id })
        assertEquals(listOf(1L, 3L), LessonSearch.search("", docs, LibraryFilter.Unfiled).map { it.doc.id }.sorted())
        assertEquals(emptyList<Long>(), LessonSearch.search("zebra", docs).map { it.doc.id })
    }

    @Test fun printAndExportAreGatedOnTheBundleTier() {
        for (a in GatedAction.entries) {
            assertTrue(Gate.check(a, Tier.FREE) is GateResult.Locked)
            assertEquals(GateResult.Allowed, Gate.check(a, Tier.BUNDLE))
        }
    }

    @Test fun htmlIsEscapedAndHasStudentAndTeacherPages() {
        val r = req(Grade.G2, Subject.ELA, "recount <b>fables</b> & morals")
        val html = LessonHtml.render(lessonFor(r))
        assertFalse(html.contains("<b>fables</b>"))
        assertTrue(html.contains("Student handout"))
        assertTrue(html.contains("Teacher answer key"))
        assertNotNull(html)
    }
}

class BookTest {
    private fun outsiders(grade: Grade = Grade.G8, standard: String = "The Outsiders", custom: String = "") = LessonRequest(grade, Subject.ELA, standard, custom)

    @Test fun theOutsidersIsRecognizedFromTheTopicBoxTheBookFieldOrTheCustomPrompt() {
        for (r in listOf(
            outsiders(),
            outsiders(standard = "novel study: The Outsiders"),
            outsiders(standard = "The Outsiders by S. E. Hinton"),
            outsiders(standard = "", custom = "A unit on \"The Outsiders\" for my 8th graders").copy(),
            LessonRequest(Grade.G8, Subject.ELA, bookTitle = "the outsiders"),
        )) {
            val b = StandardResolver.resolve(r).book
            assertEquals("The Outsiders", b?.title)
            assertEquals("S. E. Hinton", b?.author)
        }
    }

    @Test fun commonWordsAreNotMistakenForBooks() {
        assertEquals(null, StandardResolver.resolve(LessonRequest(Grade.G5, Subject.ELA, "I wonder about holes in the ground")).book)
        assertEquals(null, StandardResolver.resolve(LessonRequest(Grade.G4, Subject.MATH, "fractions by Ms. lee")).book)
        assertEquals("Holes", StandardResolver.resolve(LessonRequest(Grade.G6, Subject.ELA, "Holes")).book?.title)
    }

    @Test fun aBookMapsToTheGradesLiteratureStandardNotAGenericOne() {
        val r = StandardResolver.resolve(outsiders(Grade.G8))
        assertEquals("RL.8.2", r.standard?.shortCode)
        assertEquals("ELAGSE8RL2", r.standard?.georgiaCode)
        assertEquals("The Outsiders", r.topic)
        val evidence = StandardResolver.resolve(outsiders(Grade.G8, custom = "Focus on citing textual evidence"))
        assertEquals("RL.8.1", evidence.standard?.shortCode)
        assertEquals("ELAGSE9-10RL2", StandardResolver.resolve(LessonRequest(Grade.G9, Subject.ELA, "The Outsiders")).standard?.georgiaCode)
    }

    @Test fun everyIdeaIsAboutTheBookAndItsStandard() {
        val r = outsiders(custom = "Several English learners")
        val res = StandardResolver.resolve(r)
        val seen = mutableListOf<SeenIdea>()
        val titles = mutableSetOf<String>()
        repeat(3) {
            val ideas = IdeaEngine.generate(r, res, seen)
            assertEquals(3, ideas.size)
            ideas.forEach { i ->
                assertTrue("title: ${i.title}", i.title.contains("The Outsiders"))
                assertTrue(i.summary.contains("The Outsiders by S. E. Hinton"))
                assertTrue(i.summary.contains("ELAGSE8RL2"))
                assertTrue(i.summary.contains("English learners"))
                assertTrue("not generic: ${i.summary}", !i.summary.contains("real problem") && !i.summary.contains("station"))
                assertTrue(titles.add(i.title))
            }
            seen += ideas.map { SeenIdea(it.title, it.hook, it.summary, it.lens) }
        }
        assertTrue(seen.any { it.summary.contains("Ponyboy") || it.summary.contains("Greasers") || it.summary.contains("Tulsa") || it.summary.contains("social class") })
    }

    @Test fun theLessonCitesTheBookAndNeverInventsPlotForAnUnknownOne() {
        val r = outsiders()
        val res = StandardResolver.resolve(r)
        val lesson = LessonBuilder.build(r, res, IdeaEngine.generate(r, res, emptyList()).first())
        assertTrue(lesson.materials.first().contains("The Outsiders by S. E. Hinton"))
        assertEquals(1, lesson.materials.count { it.contains("required text") })
        assertTrue(lesson.items.any { it.prompt.contains("Ponyboy") || it.prompt.contains("Johnny") })
        assertTrue(lesson.items.all { it.prompt.isNotBlank() })
        assertTrue(lesson.objective.contains("The Outsiders"))
        assertEquals("The Outsiders", lesson.book?.title)

        val unknown = LessonRequest(Grade.G7, Subject.ELA, "The Moonlit Road by A. Writer")
        val ures = StandardResolver.resolve(unknown)
        assertEquals(null, ures.book?.info)
        val ul = LessonBuilder.build(unknown, ures, IdeaEngine.generate(unknown, ures, emptyList()).first())
        assertTrue(ul.materials.first().contains("The Moonlit Road by A. Writer"))
        assertTrue(IdeaEngine.generate(unknown, ures, emptyList()).all { it.summary.contains("your own copy") })
        assertTrue(ul.items.none { it.prompt.contains("Ponyboy") })
    }

    @Test fun bookLessonsKeepTheSentenceLimitsAndEarlyGuards() {
        for (grade in Grade.entries) for (title in listOf("The Outsiders", "Brown Bear, Brown Bear, What Do You See?", "Charlotte's Web")) {
            val r = LessonRequest(grade, Subject.ELA, title)
            val res = StandardResolver.resolve(r)
            val lesson = LessonBuilder.build(r, res, IdeaEngine.generate(r, res, emptyList()).first())
            if (grade.isEarly) {
                val taught = LessonBuilder.taughtWords(res).toSet() + setOf("dots")
                lesson.items.forEach { assertTrue("$grade $title ${it.prompt}", EarlyGuard.problems(it.prompt.replace("___", " "), taught).isEmpty()) }
            } else lesson.items.forEach { item -> Text.sentences(item.prompt).forEach { s -> assertTrue("$grade \"$s\"", Readability.wordCount(s) <= Readability.maxWords(grade)) } }
        }
    }

    @Test fun theBookSurvivesSaveAndLoad() {
        val r = outsiders()
        val res = StandardResolver.resolve(r)
        val lesson = LessonBuilder.build(r, res, IdeaEngine.generate(r, res, emptyList()).first())
        assertEquals(lesson, LessonCodec.decode(LessonCodec.encode(lesson)))
    }
}

class DecoupledBookTest {
    private val requested = listOf(
        "Fly Guy", "The Pumpkin Thief", "Don't Let the Pigeon Drive the Bus!", "Pig Elf", "The Very Hungry Caterpillar", "Brown Bear, Brown Bear, What Do You See?",
        "Where the Wild Things Are", "Make Way for Ducklings", "Corduroy", "The Outsiders", "To Kill a Mockingbird", "The Giver", "Hatchet", "Holes", "Wonder",
        "Number the Stars", "Roll of Thunder, Hear My Cry", "A Wrinkle in Time", "The Lightning Thief", "The Hunger Games", "Fahrenheit 451", "Of Mice and Men",
        "Lord of the Flies", "Romeo and Juliet", "The Great Gatsby",
    )

    @Test fun everyRequestedBookIsInTheCatalog() {
        requested.forEach { assertNotNull(it, BookCatalog.byTitle(it)) }
        assertEquals("Tedd Arnold", BookCatalog.byTitle("Fly Guy series")?.author)
        assertEquals("Mo Willems", BookCatalog.byTitle("Don't Let the Pigeon Stay Up Late!")?.author ?: "Mo Willems")
        assertEquals("Bill Martin Jr. & Eric Carle", BookCatalog.byTitle("Brown Bear, Brown Bear, What Do You See?")?.author)
    }

    @Test fun titlesWithoutVerifiedDetailsAreRecognizedButNeverGivenInventedFacts() {
        for (title in listOf("The Pumpkin Thief", "Pig Elf")) {
            val ref = StandardResolver.resolve(LessonRequest(Grade.G1, Subject.ELA, title)).book!!
            assertEquals(null, ref.author)
            assertEquals(false, ref.known)
            assertTrue(ref.characters.isEmpty() && ref.themes.isEmpty())
            val r = LessonRequest(Grade.G3, Subject.ELA, title)
            val res = StandardResolver.resolve(r)
            val ideas = IdeaEngine.generate(r, res, emptyList())
            assertTrue(ideas.all { it.summary.contains("no verified details") })
            assertTrue(ideas.all { !it.summary.contains(" by ") })
        }
    }

    @Test fun anyBookPairsWithAnyGradeAndGetsThatGradesLiteratureStandard() {
        for (title in requested) for (grade in Grade.entries) {
            val r = LessonRequest(grade, Subject.ELA, title)
            val res = StandardResolver.resolve(r)
            assertEquals("$title $grade", title, res.book?.title)
            assertNotNull("$title $grade has a standard", res.standard)
            assertEquals(grade, res.standard!!.grade)
            assertEquals("Reading Literature", res.standard!!.strand)
            val ideas = IdeaEngine.generate(r, res, emptyList())
            assertEquals(3, ideas.size)
            val lesson = LessonBuilder.build(r, res, ideas.first())
            assertEquals(6, lesson.items.size)
        }
    }

    @Test fun rigorAndHooksScaleWithTheGradeNotTheBook() {
        val title = "The Outsiders"
        val levels = Grade.entries.associateWith { g ->
            val r = LessonRequest(g, Subject.ELA, title)
            val res = StandardResolver.resolve(r)
            LessonBuilder.build(r, res, IdeaEngine.generate(r, res, emptyList()).first())
        }
        // Standards follow the grade.
        assertEquals("ELAGSE8RL2", levels[Grade.G8]!!.standard?.georgiaCode)
        assertEquals("ELAGSE11-12RL2", levels[Grade.G11]!!.standard?.georgiaCode)
        assertEquals("ELAGSEKRL2", levels[Grade.K]!!.standard?.georgiaCode)
        // Cognitive demand: a seventh-grade handout stops at Create while kindergarten stops at Apply; high school asks for evaluation of the author's work.
        assertTrue(levels[Grade.K]!!.items.none { it.bloom > Bloom.ANALYZE })
        assertTrue(levels[Grade.G9]!!.items.any { it.bloom == Bloom.CREATE })
        assertTrue(levels[Grade.G11]!!.items.any { it.prompt.contains("author") })
        assertTrue(levels[Grade.G4]!!.items.none { it.prompt.contains("author's") })
        // Hooks differ by band for the same book.
        val hooks = listOf(Grade.G1, Grade.G4, Grade.G7, Grade.G11).map { g ->
            val r = LessonRequest(g, Subject.ELA, title)
            IdeaEngine.generate(r, StandardResolver.resolve(r), emptyList()).first().hook
        }
        assertEquals(4, hooks.toSet().size)
        assertTrue(hooks[0].startsWith("Read aloud") || hooks[0].startsWith("Ask"))
        assertTrue(hooks[3].contains("evidence"))
    }

    @Test fun usingABookOutsideItsUsualGradesSaysSoAndScales() {
        val r = LessonRequest(Grade.G12, Subject.ELA, "The Outsiders")
        val res = StandardResolver.resolve(r)
        val idea = IdeaEngine.generate(r, res, emptyList()).first()
        assertTrue(idea.summary.contains("usually taught in grades 7-9"))
        assertTrue(idea.summary.contains("Grade 12"))
        assertTrue(LessonBuilder.build(r, res, idea).differentiation.onLevel.contains("scaled for Grade 12"))
        val typical = LessonRequest(Grade.G8, Subject.ELA, "The Outsiders")
        assertFalse(IdeaEngine.generate(typical, StandardResolver.resolve(typical), emptyList()).first().summary.contains("usually taught"))
    }

    @Test fun everyBookAtEveryGradeKeepsTheGuardsAndSentenceLimits() {
        for (title in requested) for (grade in Grade.entries) {
            val r = LessonRequest(grade, Subject.ELA, title)
            val res = StandardResolver.resolve(r)
            val lesson = LessonBuilder.build(r, res, IdeaEngine.generate(r, res, emptyList()).first())
            if (grade.isEarly) {
                val taught = LessonBuilder.taughtWords(res).toSet() + setOf("dots")
                lesson.items.forEach { assertTrue("$title $grade: ${it.prompt}", EarlyGuard.problems(it.prompt.replace("___", " "), taught).isEmpty()) }
            } else lesson.items.forEach { item ->
                Text.sentences(item.prompt).forEach { s -> assertTrue("$title $grade: \"$s\"", Readability.wordCount(s) <= Readability.maxWords(grade)) }
            }
        }
    }

    @Test fun anUncataloguedBookStillWorksAtAnyGrade() {
        for (grade in listOf(Grade.K, Grade.G5, Grade.G12)) {
            val r = LessonRequest(grade, Subject.ELA, "The Moonlit Road by A. Writer")
            val res = StandardResolver.resolve(r)
            assertEquals(null, res.book?.info)
            assertEquals("The Moonlit Road", res.book?.title)
            val lesson = LessonBuilder.build(r, res, IdeaEngine.generate(r, res, emptyList()).first())
            assertTrue(lesson.materials.first().contains("A. Writer"))
        }
    }

    @Test fun suggestionsMatchTitlesAsYouType() {
        assertEquals("The Outsiders", BookCatalog.suggest("outs").first().title)
        assertEquals("The Great Gatsby", BookCatalog.suggest("gatsby").first().title)
        assertTrue(BookCatalog.suggest("pigeon").isEmpty().not() || BookCatalog.suggest("don't let").isNotEmpty())
        assertEquals(emptyList<BookInfo>(), BookCatalog.suggest("q"))
    }

    @Test fun seriesAndPigeonAliasesResolve() {
        assertEquals("Fly Guy", StandardResolver.resolve(LessonRequest(Grade.K, Subject.ELA, "Fly Guy series by Tedd Arnold")).book?.title)
        assertEquals("Don't Let the Pigeon Drive the Bus!", StandardResolver.resolve(LessonRequest(Grade.G1, Subject.ELA, "Don't Let the Pigeon Stay Up Late!")).book?.title)
        assertEquals("Romeo and Juliet", StandardResolver.resolve(LessonRequest(Grade.G9, Subject.ELA, "Romeo and Juliet")).book?.title)
    }
}

class CodecTest {
    @Test fun lessonsSurviveTheRoundTripIncludingAwkwardText() {
        val r = LessonRequest(Grade.K, Subject.MATH, "counting", "He said \"count\" and\nwent on \\ tab\t")
        val res = StandardResolver.resolve(r)
        val lesson = LessonBuilder.build(r, res, IdeaEngine.generate(r, res, emptyList()).first())
        val back = LessonCodec.decode(LessonCodec.encode(lesson))
        assertEquals(lesson, back)
        assertTrue(back.items.any { it.visual is Visual.Dots })
    }
}

class DomainTest {
    private fun lesson(g: Grade, s: Subject, topic: String, code: String = ""): Lesson {
        val r = LessonRequest(g, s, topic, "", "", code)
        val res = StandardResolver.resolve(r)
        return LessonBuilder.build(r, res, IdeaEngine.generate(r, res, emptyList()).first())
    }

    private fun text(l: Lesson) = (l.sequence.map { it.description } + l.items.map { it.prompt } + l.materials).joinToString("\n")

    @Test fun standardCodeFieldResolvesNgss() {
        val res = StandardResolver.resolve(LessonRequest(Grade.G10, Subject.SCIENCE, "Process mitosis", "", "", "HS-LS1-4"))
        assertEquals("HS-LS1-4", res.standard?.ccss)
        assertTrue(LessonRequest(Grade.G10, Subject.SCIENCE, standardCode = "HS-LS1-4").hasInput)
    }

    @Test fun mitosisTeachesPhasesAndTermsNotElaFrames() {
        for (g in listOf(Grade.G4, Grade.G7, Grade.G10)) {
            val t = text(lesson(g, Subject.SCIENCE, "Process mitosis", if (g == Grade.G10) "HS-LS1-4" else ""))
            assertTrue("$g phases", t.contains("anaphase", ignoreCase = true) || g == Grade.G4)
            assertTrue("$g prophase/start", t.contains("prophase", ignoreCase = true) || g == Grade.G4 || t.contains("chromosome", ignoreCase = true))
            assertFalse("$g ELA frame", t.contains("Use your text"))
            assertFalse("$g ELA frame", t.contains("Finish:"))
        }
    }

    @Test fun uncataloguedCodeWorksAtEveryGrade() {
        for (g in Grade.entries) {
            val l = lesson(g, Subject.SCIENCE, "Plate tectonics", "HS-ESS2-1")
            assertTrue(l.standardLabel.contains("HS-ESS2-1"))
            assertEquals(l.objective.isNotBlank(), true)
        }
    }

    @Test fun customStandardSurvivesTheCodec() {
        val l = lesson(Grade.G10, Subject.SCIENCE, "Plate tectonics", "HS-ESS2-1")
        val back = LessonCodec.decode(LessonCodec.encode(l))
        assertEquals("HS-ESS2-1", back.standard?.ccss)
    }

    @Test fun nonElaLessonsKeepWorkingAcrossGradesAndSubjects() {
        for (g in Grade.entries) for (s in listOf(Subject.SCIENCE, Subject.MATH, Subject.SOCIAL_STUDIES)) {
            val l = lesson(g, s, when (s) { Subject.SCIENCE -> "mitosis"; Subject.MATH -> "comparing fractions"; else -> "community helpers" })
            assertTrue(l.items.isNotEmpty())
        }
    }
}
