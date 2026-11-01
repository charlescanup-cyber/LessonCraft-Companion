package com.lessoncraft.engine

/**
 * Saves a lesson as JSON text and reads it back, with no library, so the same code runs on the phone and in plain JVM tests. The format is the
 * database's own: add fields at the end and keep reading old ones with defaults.
 */
object LessonCodec {
    fun encode(l: Lesson): String = Json.write(
        mapOf(
            "v" to 1,
            "title" to l.title, "grade" to l.grade.name, "subject" to l.subject.name,
            "standard" to l.standard?.ccss, "topic" to l.topic, "customNotes" to l.customNotes,
            "objective" to l.objective, "objectiveBloom" to l.objectiveBloom.name,
            "materials" to l.materials,
            "udlPlan" to udl(l.udlPlan),
            "sequence" to l.sequence.map { mapOf("phase" to it.phase, "minutes" to it.minutes, "title" to it.title, "description" to it.description, "bloom" to it.bloom.name, "udl" to udl(it.udl)) },
            "items" to l.items.map {
                mapOf(
                    "number" to it.number, "kind" to it.kind.name, "prompt" to it.prompt, "bloom" to it.bloom.name, "answer" to it.answer,
                    "teacherScript" to it.teacherScript, "dots" to (it.visual as? Visual.Dots)?.count,
                )
            },
            "differentiation" to mapOf("iep504" to l.differentiation.iep504, "onLevel" to l.differentiation.onLevel, "enrichment" to l.differentiation.enrichment),
            "tags" to l.tags,
            "bookTitle" to l.book?.title, "bookAuthor" to l.book?.author,
        ),
    )

    private fun udl(u: Udl) = mapOf("engagement" to u.engagement, "representation" to u.representation, "actionExpression" to u.actionExpression)

    @Suppress("UNCHECKED_CAST")
    fun decode(text: String): Lesson {
        val m = Json.read(text) as Map<String, Any?>
        fun s(map: Map<String, Any?>, k: String) = map[k] as? String ?: ""
        fun u(x: Any?): Udl { val o = x as? Map<String, Any?> ?: emptyMap(); return Udl(s(o, "engagement"), s(o, "representation"), s(o, "actionExpression")) }
        val diff = m["differentiation"] as? Map<String, Any?> ?: emptyMap()
        val ccss = m["standard"] as? String
        return Lesson(
            title = s(m, "title"),
            grade = Grade.valueOf(s(m, "grade")),
            subject = Subject.valueOf(s(m, "subject")),
            standard = ccss?.let { c -> StandardsCatalog.all.firstOrNull { it.ccss == c } },
            topic = s(m, "topic"),
            customNotes = s(m, "customNotes"),
            objective = s(m, "objective"),
            objectiveBloom = Bloom.fromLabel(s(m, "objectiveBloom")) ?: Bloom.UNDERSTAND,
            materials = (m["materials"] as? List<Any?>)?.map { it.toString() } ?: emptyList(),
            udlPlan = u(m["udlPlan"]),
            sequence = (m["sequence"] as? List<Map<String, Any?>>)?.map {
                Step(s(it, "phase"), (it["minutes"] as? Number)?.toInt() ?: 0, s(it, "title"), s(it, "description"), Bloom.fromLabel(s(it, "bloom")) ?: Bloom.UNDERSTAND, u(it["udl"]))
            } ?: emptyList(),
            items = (m["items"] as? List<Map<String, Any?>>)?.map {
                StudentItem(
                    number = (it["number"] as? Number)?.toInt() ?: 0,
                    kind = ItemKind.entries.firstOrNull { k -> k.name == s(it, "kind") } ?: ItemKind.QUESTION,
                    prompt = s(it, "prompt"),
                    bloom = Bloom.fromLabel(s(it, "bloom")) ?: Bloom.UNDERSTAND,
                    answer = s(it, "answer"),
                    teacherScript = it["teacherScript"] as? String,
                    visual = (it["dots"] as? Number)?.toInt()?.let { n -> Visual.Dots(n) },
                )
            } ?: emptyList(),
            differentiation = Differentiation(s(diff, "iep504"), s(diff, "onLevel"), s(diff, "enrichment")),
            tags = (m["tags"] as? List<Any?>)?.map { it.toString() } ?: emptyList(),
            book = (m["bookTitle"] as? String)?.let { t -> BookRef(t, m["bookAuthor"] as? String, BookCatalog.byTitle(t)) },
        )
    }

    /** One searchable text blob for a lesson: everything a teacher might type to find it again. */
    fun searchText(l: Lesson): String = buildString {
        append(l.objective).append(' ').append(l.customNotes).append(' ')
        l.sequence.forEach { append(it.title).append(' ').append(it.description).append(' ') }
        l.items.forEach { append(it.prompt).append(' ') }
        l.materials.forEach { append(it).append(' ') }
    }
}

/** A small JSON reader and writer: objects, arrays, strings, numbers, booleans and null. */
internal object Json {
    fun write(v: Any?): String = StringBuilder().also { put(it, v) }.toString()

    private fun put(sb: StringBuilder, v: Any?) {
        when (v) {
            null -> sb.append("null")
            is String -> quote(sb, v)
            is Boolean, is Int, is Long -> sb.append(v.toString())
            is Double -> sb.append(v.toString())
            is Map<*, *> -> {
                sb.append('{')
                var first = true
                for ((k, x) in v) {
                    if (!first) sb.append(',')
                    first = false
                    quote(sb, k.toString()); sb.append(':'); put(sb, x)
                }
                sb.append('}')
            }
            is Iterable<*> -> {
                sb.append('[')
                var first = true
                for (x in v) { if (!first) sb.append(','); first = false; put(sb, x) }
                sb.append(']')
            }
            else -> quote(sb, v.toString())
        }
    }

    private fun quote(sb: StringBuilder, s: String) {
        sb.append('"')
        for (c in s) when (c) {
            '"' -> sb.append("\\\"")
            '\\' -> sb.append("\\\\")
            '\n' -> sb.append("\\n")
            '\r' -> sb.append("\\r")
            '\t' -> sb.append("\\t")
            else -> if (c < ' ') sb.append("\\u%04x".format(c.code)) else sb.append(c)
        }
        sb.append('"')
    }

    fun read(text: String): Any? {
        val p = Parser(text)
        val v = p.value()
        p.ws()
        require(p.done()) { "Unexpected text after the JSON value." }
        return v
    }

    private class Parser(val s: String) {
        var i = 0
        fun done() = i >= s.length
        fun ws() { while (i < s.length && s[i].isWhitespace()) i++ }

        fun value(): Any? {
            ws()
            require(i < s.length) { "Unexpected end of JSON." }
            return when (val c = s[i]) {
                '{' -> obj()
                '[' -> arr()
                '"' -> str()
                't' -> lit("true", true)
                'f' -> lit("false", false)
                'n' -> lit("null", null)
                else -> if (c == '-' || c.isDigit()) num() else throw IllegalArgumentException("Unexpected '$c' at $i")
            }
        }

        private fun lit(word: String, v: Any?): Any? { require(s.startsWith(word, i)) { "Bad literal at $i" }; i += word.length; return v }

        private fun num(): Number {
            val start = i
            while (i < s.length && (s[i].isDigit() || s[i] in "+-.eE")) i++
            val t = s.substring(start, i)
            return t.toLongOrNull() ?: t.toDouble()
        }

        private fun str(): String {
            i++ // opening quote
            val sb = StringBuilder()
            while (true) {
                require(i < s.length) { "Unterminated string." }
                val c = s[i++]
                when (c) {
                    '"' -> return sb.toString()
                    '\\' -> {
                        when (val e = s[i++]) {
                            'n' -> sb.append('\n'); 'r' -> sb.append('\r'); 't' -> sb.append('\t'); 'b' -> sb.append('\b'); 'f' -> sb.append('\u000C')
                            'u' -> { sb.append(s.substring(i, i + 4).toInt(16).toChar()); i += 4 }
                            else -> sb.append(e)
                        }
                    }
                    else -> sb.append(c)
                }
            }
        }

        private fun arr(): List<Any?> {
            i++
            val out = mutableListOf<Any?>()
            ws()
            if (s[i] == ']') { i++; return out }
            while (true) {
                out += value(); ws()
                when (s[i++]) { ',' -> continue; ']' -> return out; else -> throw IllegalArgumentException("Bad array at $i") }
            }
        }

        private fun obj(): Map<String, Any?> {
            i++
            val out = linkedMapOf<String, Any?>()
            ws()
            if (s[i] == '}') { i++; return out }
            while (true) {
                ws()
                val k = str(); ws()
                require(s[i++] == ':') { "Expected ':' at $i" }
                out[k] = value(); ws()
                when (s[i++]) { ',' -> continue; '}' -> return out; else -> throw IllegalArgumentException("Bad object at $i") }
            }
        }
    }
}
