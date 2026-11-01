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
