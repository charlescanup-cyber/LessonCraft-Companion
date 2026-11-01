package com.lessoncraft.engine

/** What the on-device search sees of a saved lesson. The app builds this from its own database; nothing leaves the phone. */
data class Searchable(
    val id: Long,
    val title: String,
    val subject: String,
    val grade: String,
    val standardCodes: String,
    val topic: String,
    val tags: List<String>,
    val body: String,
    val favorite: Boolean,
    val folderId: Long?,
    val createdAt: Long,
)

data class SearchHit(val doc: Searchable, val score: Double, val matched: List<String>)

/** Which saved lessons to consider: everything, favorites only, or one folder. */
sealed interface LibraryFilter {
    data object All : LibraryFilter
    data object Favorites : LibraryFilter
    data class Folder(val id: Long) : LibraryFilter
    data object Unfiled : LibraryFilter
}

/**
 * Air-gapped search: a deterministic weighted keyword ranker over the lessons stored on the device. No network, no model, no index service. The same
 * query over the same library always gives the same order. Titles and standard codes count most, then tags and topic, then the lesson text; a
 * word that merely starts a longer word counts for less, and a query that appears as a phrase gets a bonus.
 */
object LessonSearch {
    private const val TITLE = 5.0
    private const val CODE = 6.0
    private const val TAG = 3.0
    private const val TOPIC = 3.0
    private const val FIELD = 2.0
    private const val BODY = 1.0
    private const val PREFIX = 0.6

    fun applyFilter(docs: List<Searchable>, filter: LibraryFilter): List<Searchable> = when (filter) {
        LibraryFilter.All -> docs
        LibraryFilter.Favorites -> docs.filter { it.favorite }
        is LibraryFilter.Folder -> docs.filter { it.folderId == filter.id }
        LibraryFilter.Unfiled -> docs.filter { it.folderId == null }
    }

    fun search(query: String, docs: List<Searchable>, filter: LibraryFilter = LibraryFilter.All): List<SearchHit> {
        val pool = applyFilter(docs, filter)
        val q = Text.words(query).map(Text::stem).filter { it.isNotEmpty() }
        // Empty query: the newest first, favorites ahead of the rest.
        if (q.isEmpty()) return pool.sortedWith(compareByDescending<Searchable> { it.favorite }.thenByDescending { it.createdAt }).map { SearchHit(it, 0.0, emptyList()) }
        val phrase = query.trim().lowercase()
        return pool.mapNotNull { d ->
            val matched = linkedSetOf<String>()
            var score = 0.0
            fun field(name: String, text: String, weight: Double) {
                val words = Text.words(text).map(Text::stem)
                var hits = 0.0
                for (t in q) {
                    val exact = words.any { it == t }
                    val prefix = !exact && t.length >= 3 && words.any { it.startsWith(t) }
                    if (exact) hits += weight else if (prefix) hits += weight * PREFIX
                }
                if (hits > 0) { score += hits; matched += name }
            }
            field("title", d.title, TITLE)
            field("standard", d.standardCodes.replace(".", " ").replace("-", " "), CODE)
            if (d.standardCodes.lowercase().replace(" ", "").contains(phrase.replace(" ", ""))) { score += CODE * 2; matched += "standard" }
            field("tags", d.tags.joinToString(" "), TAG)
            field("topic", d.topic, TOPIC)
            field("subject", d.subject, FIELD)
            field("grade", d.grade, FIELD)
            field("text", d.body, BODY)
            if (phrase.contains(" ") && d.title.lowercase().contains(phrase)) score += TITLE * 2
            // Every query word must be found somewhere, so "retell kindergarten" does not return every Kindergarten lesson.
            val covered = q.all { t -> listOf(d.title, d.standardCodes, d.tags.joinToString(" "), d.topic, d.subject, d.grade, d.body).any { f -> Text.words(f).map(Text::stem).any { it == t || (t.length >= 3 && it.startsWith(t)) } } }
            if (!covered || score <= 0) null else SearchHit(d, score + if (d.favorite) 0.25 else 0.0, matched.toList())
        }.sortedWith(compareByDescending<SearchHit> { it.score }.thenByDescending { it.doc.createdAt })
    }
}

/** What a locked action asks the teacher to do. The app is offline, so this is a message and a link to the web app, not a purchase flow. */
enum class Tier { FREE, BUNDLE }

enum class GatedAction(val label: String) { PRINT("Print"), EXPORT("Export") }

sealed interface GateResult {
    data object Allowed : GateResult
    data class Locked(val title: String, val message: String, val actionLabel: String) : GateResult
}

/**
 * Printing and exporting are part of the web app's Bundle subscription. Everything else in the Companion (planning, search, favorites, folders)
 * works for every tier. The tier is decided on the device (see EntitlementStore); this only answers "may this tier do that?".
 */
object Gate {
    fun check(action: GatedAction, tier: Tier): GateResult = when (tier) {
        Tier.BUNDLE -> GateResult.Allowed
        Tier.FREE -> GateResult.Locked(
            title = "${action.label} is part of the Bundle",
            message = "${action.label}ing lesson plans, worksheets and answer keys is included with the LessonCraft web app Bundle subscription. " +
                "Planning, searching, favorites and folders stay free on this device.",
            actionLabel = "See the Bundle",
        )
    }
}

/** Plain HTML for a lesson, used by print and export once the gate allows them. Teacher answers are on their own page after the student pages. */
object LessonHtml {
    private fun esc(s: String) = s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;")

    fun render(l: Lesson): String = buildString {
        append("<!doctype html><html><head><meta charset=\"utf-8\"><title>${esc(l.title)}</title>")
        append("<style>body{font-family:sans-serif;margin:24px;line-height:1.45;color:#111}h1{font-size:22px}h2{font-size:17px;margin-top:22px;border-bottom:1px solid #999}")
        append(".item{margin:14px 0}.line{border-bottom:1px solid #333;height:26px;margin-top:8px}.box{border:2px dashed #666;height:110px;margin-top:8px}.dots{font-size:26px;letter-spacing:6px}")
        append(".script{background:#eef;border-left:4px solid #447;padding:8px;margin:8px 0}.pb{page-break-before:always}</style></head><body>")
        append("<h1>${esc(l.title)}</h1>")
        l.book?.let { append("<p><b>Book:</b> ${esc(it.describe())}</p>") }
        append("<p><b>${esc(l.grade.label)} &middot; ${esc(l.subject.label)}</b> &middot; ${esc(l.standardLabel)} &middot; ${l.totalMinutes} minutes</p>")
        append("<h2>Objective (${esc(l.objectiveBloom.label)})</h2><p>${esc(l.objective)}</p>")
        append("<h2>Materials</h2><ul>${l.materials.joinToString("") { "<li>${esc(it)}</li>" }}</ul>")
        append("<h2>Instructional sequence</h2>")
        l.sequence.forEach { s ->
            append("<h3>${esc(s.phase)} (${s.minutes} min, ${esc(s.bloom.label)}): ${esc(s.title)}</h3><p>${esc(s.description)}</p>")
            append("<ul><li><b>Engagement:</b> ${esc(s.udl.engagement)}</li><li><b>Representation:</b> ${esc(s.udl.representation)}</li><li><b>Action &amp; expression:</b> ${esc(s.udl.actionExpression)}</li></ul>")
        }
        append("<h2>Differentiation</h2><p><b>IEP / 504:</b> ${esc(l.differentiation.iep504)}</p><p><b>On level:</b> ${esc(l.differentiation.onLevel)}</p><p><b>Enrichment:</b> ${esc(l.differentiation.enrichment)}</p>")
        append("<div class=\"pb\"></div><h1>Student handout</h1><p>Name: ______________________ Date: ____________</p>")
        l.items.forEach { it.appendTo(this) }
        append("<div class=\"pb\"></div><h1>Teacher answer key</h1><ol>")
        l.items.forEach { append("<li>${esc(it.answer)} <i>(${esc(it.bloom.label)})</i></li>") }
        append("</ol></body></html>")
    }

    private fun StudentItem.appendTo(sb: StringBuilder) {
        sb.append("<div class=\"item\">")
        if (teacherScript != null) sb.append("<div class=\"script\">${esc(teacherScript)}</div>")
        sb.append("<b>$number.</b> ${esc(prompt)}")
        (visual as? Visual.Dots)?.let { sb.append("<div class=\"dots\">${"● ".repeat(it.count).trim()}</div>") }
        when (kind) {
            ItemKind.DRAW_BOX -> sb.append("<div class=\"box\"></div>")
            ItemKind.QUESTION, ItemKind.SENTENCE_FRAME, ItemKind.SEQUENCE, ItemKind.DATA_TABLE -> sb.append("<div class=\"line\"></div><div class=\"line\"></div>")
            else -> sb.append("<div class=\"line\"></div>")
        }
        sb.append("</div>")
    }
}
