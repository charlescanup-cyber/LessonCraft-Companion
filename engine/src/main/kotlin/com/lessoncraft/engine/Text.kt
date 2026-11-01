package com.lessoncraft.engine

import kotlin.random.Random

/** Small text helpers shared by the engine. No Android, no network, no randomness except from an explicit seed. */
internal object Text {
    private val STOP = ("a an the and or of to in on for with from by at as is are be it its this that these those your you we our students student teacher " +
        "lesson activity use using about into each every their they them then than not can will how what why who when where which while have has had do does " +
        "i my me so if but").split(" ").toSet()

    /** Lowercase words, punctuation removed. */
    fun words(s: String): List<String> = s.lowercase().replace(Regex("[^a-z0-9'\\s-]"), " ").split(Regex("\\s+")).filter { it.isNotEmpty() }

    /** A light stem so "retelling", "retells" and "retell" meet. */
    fun stem(w: String): String {
        var x = w.removeSuffix("'s")
        for (suf in listOf("ing", "ed", "es", "s")) if (x.length > suf.length + 2 && x.endsWith(suf)) { x = x.dropLast(suf.length); break }
        return x
    }

    /** Content words: no stop words, short words dropped, stemmed. */
    fun tokens(s: String): Set<String> = words(s).filter { it.length > 2 && it !in STOP }.map(::stem).toSet()

    fun jaccard(a: Set<String>, b: Set<String>): Double {
        if (a.isEmpty() || b.isEmpty()) return 0.0
        val hit = a.count { it in b }
        return hit.toDouble() / (a.size + b.size - hit)
    }

    /** A stable 32-bit hash (FNV-1a), so the same request always seeds the same ideas. */
    fun hash(s: String): Int {
        var h = 0x811C9DC5.toInt()
        for (c in s) { h = h xor c.code; h *= 16777619 }
        return h
    }

    fun rng(seed: String): Random = Random(hash(seed))

    fun sentences(s: String): List<String> = s.split(Regex("(?<=[.!?])\\s+")).map { it.trim() }.filter { it.isNotEmpty() }

    fun cap(s: String): String = s.replaceFirstChar { it.uppercase() }
    fun decap(s: String): String = if (s.length > 1 && s[1].isLowerCase()) s.replaceFirstChar { it.lowercase() } else s

    fun clip(s: String, n: Int): String = if (s.length <= n) s else s.take(n - 1).trimEnd() + "…"
}
