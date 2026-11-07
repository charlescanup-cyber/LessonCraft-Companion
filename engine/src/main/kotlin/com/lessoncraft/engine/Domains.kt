package com.lessoncraft.engine

/**
 * Subject knowledge for lessons that are not about reading. A lesson on mitosis needs the phases of mitosis and their vocabulary, not a reading
 * comprehension frame. A pack holds the content for one concept (accurate terms, the ordered phases or steps, a common misconception and its
 * correction), and the lesson builder puts that content into Direct Instruction, Guided Practice and the handout. A topic with no pack still gets
 * a subject-appropriate structure (a science inquiry, a math problem-solving routine, a social studies source analysis), never an ELA one.
 */
data class Term(val word: String, val meaning: String)

/** One step of a process or strategy: what happens, and what a student would observe or write to show it. */
data class Phase(val name: String, val happens: String, val evidence: String)

data class ConceptPack(
    val id: String,
    val subject: Subject,
    /** The concept in plain words: "mitosis", "the water cycle". */
    val title: String,
    /** The kind of thing it is, used in prompts: "process", "cycle", "strategy". */
    val kind: String,
    /** Any of these appearing in the topic, custom prompt or standard keywords selects the pack. */
    val triggers: List<String>,
    val phases: List<Phase>,
    /** The same idea in fewer, plainer steps, for grades before the full version. */
    val simple: List<Phase>,
    val terms: List<Term>,
    val misconception: String,
    val correction: String,
    val hooks: List<String>,
    val materials: List<String>,
    /** What students build or draw to model it. */
    val modelTask: String,
    /** A short word a Kindergarten or Grade 1 child can trace for this topic. */
    val earlyWord: String,
)

object DomainPacks {
    val all: List<ConceptPack> = listOf(
        ConceptPack(
            "mitosis", Subject.SCIENCE, "mitosis", "process",
            listOf("mitosis", "cell division", "cellular division", "cell cycle"),
            listOf(
                Phase("Interphase", "The cell grows and copies its DNA, so each chromosome is duplicated. The cell prepares to divide.", "The nucleus is intact and the DNA looks like a diffuse tangle (chromatin)."),
                Phase("Prophase", "The DNA condenses into chromosomes, each made of two identical sister chromatids joined at a centromere. The nuclear envelope begins to break down and spindle fibers form.", "Thick, visible chromosomes; the nucleus fades."),
                Phase("Metaphase", "The chromosomes line up along the middle of the cell, each attached to spindle fibers.", "A single line of chromosomes across the center of the cell."),
                Phase("Anaphase", "The sister chromatids separate and are pulled to opposite ends of the cell by the spindle fibers.", "Two sets of chromosomes moving apart; the cell looks stretched."),
                Phase("Telophase", "The chromosomes reach opposite ends and begin to unwind. New nuclear envelopes form around each set and the spindle disappears.", "Two new nuclei forming at the ends of the cell."),
                Phase("Cytokinesis", "The cytoplasm divides, producing two genetically identical daughter cells.", "The cell pinches in two (animal cells) or a cell plate forms (plant cells)."),
            ),
            listOf(
                Phase("Copy", "The cell makes a copy of its DNA.", "Each chromosome has a copy."),
                Phase("Pull apart", "The copies are pulled to opposite ends of the cell.", "Two equal sets at the two ends."),
                Phase("Split", "The cell divides into two new cells that are the same.", "Two cells where there was one."),
            ),
            listOf(
                Term("mitosis", "cell division that produces two genetically identical daughter cells"),
                Term("chromosome", "a structure of tightly coiled DNA that carries genes"),
                Term("sister chromatids", "the two identical copies of a duplicated chromosome"),
                Term("centromere", "the region where sister chromatids are joined"),
                Term("spindle fibers", "protein fibers that move chromosomes during division"),
                Term("nuclear envelope", "the membrane that surrounds the nucleus"),
                Term("cell cycle", "the repeating series of growth, DNA copying and division of a cell"),
                Term("daughter cell", "a new cell made when a parent cell divides"),
                Term("differentiation", "the process by which cells become specialized in structure and function"),
            ),
            "Mitosis makes cells with half the chromosomes of the parent cell.",
            "Mitosis makes two daughter cells with the same number of chromosomes as the parent cell. Meiosis, which makes sex cells, is the division that halves the number.",
            listOf(
                "When you heal a cut, where do the new cells come from? Ask how one cell can become two identical cells.",
                "Show a growth chart and ask: if a body grows by making more cells, what must happen to the DNA first?",
                "Pose the puzzle: every cell in your body came from one cell, yet skin cells and nerve cells look different. What stays the same, and what changes?",
            ),
            listOf("Pipe cleaners, yarn or beads to model chromosomes", "Two colors of material so sister chromatids can be told apart", "String or chalk to mark the cell boundary"),
            "Build a model of one cell going through mitosis, using the materials to show chromosomes, chromatids and spindle fibers in each phase.",
            "cell",
        ),
        ConceptPack(
            "photosynthesis", Subject.SCIENCE, "photosynthesis", "process",
            listOf("photosynthesis", "chloroplast", "plants make food"),
            listOf(
                Phase("Light is absorbed", "Chlorophyll in the chloroplasts captures light energy.", "Green leaves in light; the energy comes from the Sun."),
                Phase("Water and carbon dioxide come in", "Roots take in water. Leaves take in carbon dioxide through openings called stomata.", "Water moves up the plant; gas moves in through the leaf."),
                Phase("Energy is converted", "Light energy is used to turn water and carbon dioxide into glucose, a sugar that stores chemical energy.", "Glucose is made: 6CO2 + 6H2O + light energy makes C6H12O6 + 6O2."),
                Phase("Oxygen is released", "Oxygen is released as a by-product, and glucose is used or stored by the plant.", "Oxygen leaves the leaf; the plant has stored energy."),
            ),
            listOf(
                Phase("Light comes in", "The leaf catches sunlight.", "A leaf in the sun."),
                Phase("Water and air come in", "The plant takes in water and carbon dioxide.", "Water from the roots and air in the leaf."),
                Phase("Food is made", "The plant makes sugar for food and gives off oxygen.", "Sugar made, oxygen given off."),
            ),
            listOf(
                Term("photosynthesis", "the process plants use to turn light energy, water and carbon dioxide into glucose and oxygen"),
                Term("chlorophyll", "the green pigment that absorbs light energy"),
                Term("chloroplast", "the part of a plant cell where photosynthesis happens"),
                Term("stomata", "tiny openings in a leaf that let gases in and out"),
                Term("glucose", "a sugar that stores chemical energy"),
                Term("producer", "an organism that makes its own food"),
            ),
            "Plants get their food from the soil.",
            "Plants make their own food (glucose) from carbon dioxide and water using light energy. The soil supplies water and minerals, but not the food.",
            listOf(
                "Ask: a tiny seed grows into a huge tree. Where does the mass of the tree come from?",
                "Show a potted plant and ask: it has no mouth, so how does it get its food?",
                "Ask: why do greenhouses use light, and what would change if you took away the light?",
            ),
            listOf("Fresh leaves", "A clear container of water and a light source", "Colored pencils for labeling the model"),
            "Draw and label a model that shows what goes into a leaf, what comes out, and how light energy becomes stored chemical energy.",
            "leaf",
        ),
        ConceptPack(
            "respiration", Subject.SCIENCE, "cellular respiration", "process",
            listOf("cellular respiration", "respiration", "atp", "mitochondria"),
            listOf(
                Phase("Glucose and oxygen go in", "Cells take in glucose from food and oxygen from the air.", "Breathing and eating supply the inputs."),
                Phase("Bonds are broken", "The bonds in glucose are broken down, which releases the energy stored in them.", "Glucose is gone; energy is released."),
                Phase("Energy is transferred to ATP", "Much of that energy is captured in ATP, the molecule cells use to power their work.", "ATP is made, mostly in the mitochondria."),
                Phase("Carbon dioxide and water come out", "New compounds form: carbon dioxide and water, along with some energy given off as heat.", "Carbon dioxide leaves with each breath out."),
            ),
            listOf(
                Phase("Food and air come in", "Cells take in food and oxygen.", "Eating and breathing."),
                Phase("Energy is released", "The cell gets energy from the food.", "Energy for moving and growing."),
                Phase("Waste goes out", "The cell gives off carbon dioxide and water.", "Breathing out."),
            ),
            listOf(
                Term("cellular respiration", "the process cells use to release energy from glucose"),
                Term("ATP", "the molecule cells use to store and transfer energy for their work"),
                Term("mitochondria", "the parts of a cell where most ATP is made"),
                Term("glucose", "a sugar that stores chemical energy"),
                Term("aerobic", "needing oxygen"),
            ),
            "Only animals do cellular respiration.",
            "All living things, including plants, do cellular respiration. Plants make glucose by photosynthesis and then use it in respiration.",
            listOf(
                "Ask: why do you breathe harder when you run, and where does the extra energy come from?",
                "Ask: you eat a sandwich at lunch. What has to happen to it before your muscles can use it?",
            ),
            listOf("A stopwatch for measuring breathing rate at rest and after exercise", "Colored pencils for labeling the model"),
            "Draw and label a model of one cell showing glucose and oxygen going in, ATP being made, and carbon dioxide and water coming out.",
            "cell",
        ),
        ConceptPack(
            "watercycle", Subject.SCIENCE, "the water cycle", "cycle",
            listOf("water cycle", "evaporation", "condensation", "precipitation"),
            listOf(
                Phase("Evaporation", "The Sun's heat changes liquid water from oceans, lakes and soil into water vapor, a gas.", "Puddles shrink; water vapor is invisible."),
                Phase("Condensation", "Rising water vapor cools and changes back into tiny liquid droplets that form clouds.", "Clouds form; water beads on a cold glass."),
                Phase("Precipitation", "When droplets in clouds grow heavy, water falls as rain, snow, sleet or hail.", "Rain or snow reaches the ground."),
                Phase("Collection", "Water runs off or soaks into the ground and collects in oceans, lakes, rivers and groundwater. Then the cycle repeats.", "Streams, lakes and wet soil."),
            ),
            listOf(
                Phase("Water rises", "The Sun warms water and it goes up into the air.", "Puddles dry up."),
                Phase("Clouds form", "The water cools and makes clouds.", "Clouds in the sky."),
                Phase("Water falls", "Rain or snow falls and the water collects again.", "Rain, then puddles."),
            ),
            listOf(
                Term("evaporation", "the change of liquid water into water vapor"),
                Term("water vapor", "water as an invisible gas"),
                Term("condensation", "the change of water vapor into liquid droplets"),
                Term("precipitation", "water that falls from clouds as rain, snow, sleet or hail"),
                Term("runoff", "water that flows over the ground into streams and lakes"),
            ),
            "Clouds are made of water vapor.",
            "Water vapor is an invisible gas. Clouds are made of tiny liquid water droplets or ice crystals that condensed from the vapor.",
            listOf(
                "Ask: where does a puddle go after the rain stops?",
                "Ask: the water you drink may have been in a cloud, a river and a dinosaur's world before. How can the same water keep moving?",
            ),
            listOf("A clear cup with a lid, warm water and ice for a mini water cycle", "Colored pencils for labeling the model"),
            "Draw and label a diagram of the water cycle that shows every change of state and what causes it.",
            "rain",
        ),
        ConceptPack(
            "moonphases", Subject.SCIENCE, "the phases of the Moon", "cycle",
            listOf("moon phases", "phases of the moon", "lunar phases", "earth-sun-moon"),
            listOf(
                Phase("New Moon", "The Moon is between Earth and the Sun, so the side facing Earth is not lit.", "The Moon cannot be seen."),
                Phase("Waxing Crescent", "A thin sliver of the sunlit half becomes visible and grows each night.", "A small lit curve on the right (in the Northern Hemisphere)."),
                Phase("First Quarter", "Half of the Moon's face as seen from Earth is lit.", "A lit half on the right."),
                Phase("Waxing Gibbous", "More than half of the face is lit and growing.", "Almost full."),
                Phase("Full Moon", "Earth is between the Sun and the Moon, so the whole face is lit.", "A full circle."),
                Phase("Waning Gibbous", "The lit part is shrinking but still more than half.", "Almost full, on the left."),
                Phase("Third Quarter", "Half of the face is lit, now on the other side.", "A lit half on the left."),
                Phase("Waning Crescent", "A thin lit sliver that keeps shrinking until the next New Moon.", "A small lit curve on the left."),
            ),
            listOf(
                Phase("The Moon orbits Earth", "The Moon travels around Earth, and half of it is always lit by the Sun.", "A ball lit by a lamp."),
                Phase("We see different amounts of the lit half", "As the Moon moves, we see more or less of its lit half.", "A thin curve, a half, a full circle."),
                Phase("The pattern repeats", "The shapes repeat about every month.", "The same shapes again."),
            ),
            listOf(
                Term("phase", "the shape of the lit part of the Moon that we see"),
                Term("orbit", "the path one object takes around another"),
                Term("waxing", "growing: the lit part is getting bigger"),
                Term("waning", "shrinking: the lit part is getting smaller"),
                Term("crescent", "less than half of the face lit"),
                Term("gibbous", "more than half of the face lit"),
            ),
            "Moon phases are caused by Earth's shadow falling on the Moon.",
            "Phases are caused by how much of the Moon's sunlit half we can see as it orbits Earth. Earth's shadow causes lunar eclipses, which are rare.",
            listOf(
                "Ask: why does the Moon seem to change shape over a month? Collect ideas before teaching.",
                "Ask students to sketch what the Moon looked like last night, and compare drawings.",
            ),
            listOf("A lamp or bright flashlight to stand for the Sun", "A white ball on a stick for the Moon", "A dim room"),
            "Use the lamp and the ball to model the Moon at each position in its orbit, then sketch what an observer on Earth sees.",
            "moon",
        ),
        ConceptPack(
            "fractions", Subject.MATH, "comparing fractions", "strategy",
            listOf("compare fractions", "comparing fractions", "fractions", "fraction"),
            listOf(
                Phase("Look at the denominators", "Check whether the fractions have the same denominator or the same numerator.", "Same denominator: compare the numerators. Same numerator: the smaller denominator is the larger fraction."),
                Phase("Use a benchmark or a common denominator", "Compare each fraction to a benchmark such as 1/2, or rename both with a common denominator.", "3/8 is less than 1/2 and 3/5 is greater than 1/2."),
                Phase("Compare the numerators or the sizes", "With a common denominator, the larger numerator is the larger fraction.", "3/5 = 24/40 and 3/8 = 15/40, so 3/5 is greater."),
                Phase("Record with a symbol", "Write the comparison with >, < or =.", "3/5 > 3/8."),
                Phase("Justify with a model", "Show why with a number line, a fraction bar or equivalent fractions.", "A drawing or number line that matches the symbol."),
            ),
            listOf(
                Phase("Look at the parts", "Check how many equal parts the whole has and how many are taken.", "Parts of a whole."),
                Phase("Compare", "Use a picture-like model in words: which is bigger?", "A fraction bar."),
                Phase("Say which is greater", "Write which fraction is greater and why.", "A sentence with the answer."),
            ),
            listOf(
                Term("numerator", "the top number: how many equal parts are taken"),
                Term("denominator", "the bottom number: how many equal parts make the whole"),
                Term("equivalent fractions", "fractions that name the same amount"),
                Term("benchmark fraction", "a friendly fraction like 1/2 used to compare"),
                Term("common denominator", "a denominator that two fractions are both renamed to"),
            ),
            "A bigger denominator means a bigger fraction.",
            "A bigger denominator means the whole is cut into more, smaller equal parts, so 1/8 is less than 1/4.",
            listOf(
                "Ask: would you rather have 3/8 or 3/5 of the same pizza? How can you prove it?",
                "Ask: is 5/6 closer to 0 or to 1? How do you know without a calculator?",
            ),
            listOf("Fraction strips or paper to fold", "A number line from 0 to 1 on the board", "Counters"),
            "Make a model that proves which of two fractions is greater, using strips, a number line or equivalent fractions.",
            "half",
        ),
        ConceptPack(
            "multiplication", Subject.MATH, "multiplication as equal groups", "strategy",
            listOf("multiplication", "equal groups", "arrays", "multiply"),
            listOf(
                Phase("Name the groups", "Decide how many equal groups there are and how many are in each group.", "5 groups of 7."),
                Phase("Show it as repeated addition", "Add the group size again and again.", "7 + 7 + 7 + 7 + 7."),
                Phase("Show it as an array", "Arrange the objects in equal rows and columns.", "5 rows with 7 in each row."),
                Phase("Write the equation", "Write the multiplication equation with the factors and the product.", "5 x 7 = 35."),
                Phase("Check with another strategy", "Skip count or use a known fact to confirm the product.", "7, 14, 21, 28, 35."),
            ),
            listOf(
                Phase("Make equal groups", "Put the same number of objects in each group.", "Groups of the same size."),
                Phase("Count them all", "Count all the objects.", "The total."),
                Phase("Write it", "Write a multiplication sentence for the groups.", "A number sentence."),
            ),
            listOf(
                Term("factor", "a number that is multiplied"),
                Term("product", "the answer to a multiplication problem"),
                Term("array", "objects arranged in equal rows and columns"),
                Term("equal groups", "groups that each have the same number of objects"),
            ),
            "5 x 7 means 5 + 7.",
            "5 x 7 means 5 groups of 7, which is 7 + 7 + 7 + 7 + 7 = 35.",
            listOf(
                "Ask: there are 6 bikes in the rack. How many wheels is that? How can you find out fast?",
                "Ask: how many eggs are in 4 cartons of 12? Have students solve it in two different ways.",
            ),
            listOf("Counters or cubes", "Grid paper for drawing arrays"),
            "Build an array for a multiplication fact and show the same fact as repeated addition and as an equation.",
            "six",
        ),
    )

    private fun norm(s: String) = " " + Text.words(s).joinToString(" ") + " "

    /** The pack whose trigger words appear in the topic, the custom instructions or the standard's keywords, for this subject. */
    fun find(request: LessonRequest, standard: Standard?): ConceptPack? {
        val hay = norm("${request.standardInput} ${request.customPrompt} ${standard?.keywords?.joinToString(" ") ?: ""}")
        return all.firstOrNull { p -> p.subject == request.subject && p.triggers.any { hay.contains(norm(it)) } }
    }
}

/**
 * The structure a lesson takes when there is no concept pack for the topic, one per subject. The topic is dropped into a subject-appropriate routine
 * (an inquiry, a problem-solving routine, a source analysis), so a lesson on an unlisted science topic is still a science lesson.
 */
object GenericDomain {
    fun phases(subject: Subject, topic: String): List<Phase> = when (subject) {
        Subject.SCIENCE -> listOf(
            Phase("Observe the phenomenon", "Students look at a phenomenon related to $topic and record what they notice and wonder.", "A list of observations and questions."),
            Phase("Build a model", "Students draw or describe a model that explains how $topic works.", "A labeled model."),
            Phase("Gather evidence", "Students collect data or observations that test the model.", "A data table or a set of observations."),
            Phase("Explain with evidence", "Students write a claim about $topic, support it with evidence, and explain the reasoning that connects them.", "A claim, evidence and reasoning."),
        )
        Subject.MATH -> listOf(
            Phase("Understand the problem", "Students restate the problem about $topic and say what is known and what is asked.", "The question in the student's own words."),
            Phase("Plan a strategy", "Students choose a strategy or model for $topic, such as a drawing, a table or an equation.", "A plan."),
            Phase("Solve", "Students carry out the plan and show every step.", "Work shown."),
            Phase("Check and explain", "Students check the answer a second way and explain why it makes sense.", "A check and a justification."),
        )
        else -> listOf(
            Phase("Read the source", "Students read a short source about $topic and say who made it and when.", "The source, author and date."),
            Phase("Find the context", "Students describe what was happening when the source was made.", "A short description of the time and place."),
            Phase("Identify cause and effect", "Students find causes and effects connected to $topic.", "A cause and effect chart."),
            Phase("Interpret and support", "Students make a claim about $topic and support it with details from the source.", "A claim with evidence."),
        )
    }

    fun simple(subject: Subject, topic: String): List<Phase> = phases(subject, topic).let { p -> listOf(p[0], p[1], p.last()) }

    fun terms(subject: Subject): List<Term> = when (subject) {
        Subject.SCIENCE -> listOf(Term("claim", "a statement about how something works"), Term("evidence", "data or observations that support a claim"), Term("reasoning", "the explanation that connects evidence to a claim"), Term("model", "a drawing or description that shows how something works"))
        Subject.MATH -> listOf(Term("strategy", "a way to solve a problem"), Term("equation", "a number sentence with an equals sign"), Term("estimate", "a close answer found quickly"), Term("justify", "to explain why an answer is correct"))
        else -> listOf(Term("source", "a document or object from the past"), Term("context", "the time, place and events around something"), Term("cause", "something that makes another thing happen"), Term("effect", "what happens because of a cause"))
    }

    fun materials(subject: Subject): List<String> = when (subject) {
        Subject.SCIENCE -> listOf("Materials for a simple model of the topic (paper, string, clay or beads)", "A place to record observations and data")
        Subject.MATH -> listOf("Counters, cubes or other objects students can move", "Grid paper")
        else -> listOf("A short source or text on the topic of your choice", "Chart paper for a cause and effect chart")
    }
}
