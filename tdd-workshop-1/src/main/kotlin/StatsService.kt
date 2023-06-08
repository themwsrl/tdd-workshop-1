import ParserUtils.parseDouble
import java.util.*
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.roundToLong

abstract class StatsService {

    abstract val minGrade: Grade

    abstract val maxGrade: Grade

    // supports calculatin GPA
    abstract val supportsGPA: Boolean

    // supports calculatin Graduation Base
    abstract val supportsGraduationBase: Boolean

    open val supportsProjection: Boolean
        get() = supportsGPA && supportsGraduationBase

    // NOTE: must have size == 5
    abstract val gradesDistributionClasses: Array<Grade>

    open fun validateProjectionExamInput(ectsCents: Int, grade: Grade) = supportsProjection

    open fun validateProjectionGpaInput(ectsCents: Int, gpa: Double) = supportsProjection

    open fun validateProjectionGraduationBaseInput(ectsCents: Int, graduationBase: Double) = supportsProjection

    open val gradesComparator: Comparator<Grade> = Comparator { g1, g2 ->
        when (g1) {
            g2 -> 0
            GRADE_TODO -> -1
            GRADE_PASSED ->
                if (g2 == GRADE_TODO) {
                    1
                } else {
                    -1
                }

            GRADE_DISTINCTION -> 1
            else -> when (g2) {
                GRADE_TODO, GRADE_PASSED -> 1
                GRADE_DISTINCTION -> -1
                else -> g1.compareTo(g2)
            }
        }
    }

    abstract val defaultGpaPeerDistribution: List<Pair<Grade, Double>>

    open fun normalizedGpa(gpa: Double?): Int? {
        if (gpa == null) {
            return null
        }
        val minGrade = convertGradeToDouble(minGrade)
        val maxGrade = convertGradeToDouble(maxGrade)
        return abs(((gpa - minGrade) / (maxGrade - minGrade) * 100).roundToInt())
    }

    open fun normalizedGrade(grade: Grade): Int {
        return when (grade) {
            GRADE_TODO -> 0
            GRADE_PASSED -> 100
            else -> {
                val gradeDouble = convertGradeToDouble(grade)
                val minGrade = convertGradeToDouble(minGrade)
                val maxGrade = convertGradeToDouble(maxGrade)
                abs(((gradeDouble - minGrade) / (maxGrade - minGrade) * 100).roundToInt())
            }
        }
    }

    open fun calculateGPA(examGrades: List<IExamGrade>, parsedWeightedAverage: Double? = null): Double =
        parsedWeightedAverage ?: calculateWeightedMean(examGrades)

    open fun calculateArithmeticMean(examGrades: List<IExamGrade>, parsedArithmeticMean: Double? = null): Double =
        parsedArithmeticMean ?: calculateArithmeticMean(examGrades)

    open fun calculateGradesDistribution(examGrades: List<IExamGrade>): List<Pair<Grade, Double>> =
        examGrades.groupBy({ it.examGradeParsed }, { true })
            .filterKeys { it !in setOf(GRADE_PASSED, GRADE_TODO) }
            .mapValues { it.value.count().toDouble() }
            .toList()
            .sortedWith(Comparator { (grade1, _), (grade2, _) ->
                gradesComparator.compare(grade1, grade2)
            })

    open fun calculateGradesProgression(examGrades: List<IExamGrade>): List<Pair<Long, Grade>>? {
        return examGrades
            .filter { it.examDate != null && it.examGradeParsed != GRADE_PASSED }
            .also { if (it.isEmpty()) return null }
            .sortedBy { it.examDate!! }
            .map { it.examDate!!.time to it.examGradeParsed }
    }

    open fun calculateEctsWithDistinction(eg: IExamGrade, distinctionWeight: Double) =
        if (eg.examGradeParsed == GRADE_DISTINCTION) {
            eg.examCfuCents + (100 * distinctionWeight)
        } else {
            eg.examCfuCents.toDouble()
        }

    abstract fun calculateDefaultGpaPerformance(gpa: Double): Int

    open fun calculateGpaPerformance(gpa: Double, gpas: List<Double>): Int {
        if (gpas.isEmpty()) {
            return calculateDefaultGpaPerformance(gpa)
        }
        val lessOrEqualCount = gpas.count { it <= gpa }
        return ((lessOrEqualCount / gpas.size.toFloat()) * 100).roundToInt()
    }

    open fun calculateGpaPeerDistribution(gpas: List<Double>): List<Pair<Grade, Double>> = emptyList()

    /**
     * Used mainly to calculate an internal version of GPA
     */
    abstract fun convertGradeToDouble(grade: Grade): Double

    /**
     * Used mainly to calculate an internal version of GPA
     */
    abstract fun convertDoubleToGrade(grade: Double, roundCeil: Boolean = false): Grade

    /**
     * Format grade for display
     */
    abstract fun convertGradeToString(grade: Grade): String

    /**
     * true if the grade indicates that the exam was passed
     */
    abstract fun isGradePositive(grade: Grade): Boolean

    /**
     * checks whether the grade can be used to calculate the GPA
     */
    open fun isGradeValidForGpa(grade: Grade): Boolean = isGradePositive(grade) && grade != GRADE_PASSED
            && grade != GRADE_TODO

    abstract fun convertGradeToGradeLevel(grade: Grade): GradeLevel

    abstract fun convertAverageGradeToDifficultyIndex(average: Double): DifficultyIndex

    open val statusesForPassed: Set<String> = emptySet()

    /**
     * Given the parsed exam grades and a possibly empty examstats object the stats are calculated
     * @param examGrades the grades
     * @param examStats the existing exam object
     * @return updated parser results array
     */
    fun calculateAllStats(examGrades: ParserResults, examStats: ParserResults) {
        try {

            if (!examStats.contains<ParsedData>(EntityType.EXAMSTATS)) {
                return
            }

            val es = examStats.get<ParsedData>(EntityType.EXAMSTATS)[0] as ExamStatsData
            val grades = examGrades.get<ExamGradeData>(EntityType.EXAMGRADE)
            calculateAllStats(grades, es)

        } catch (ignored: Exception) {
        }
    }

    fun calculateAllStats(examGrades: List<IExamGrade>, examStats: ExamStatsData) {
        calculateTotalEcts(examGrades, examStats)
        calculateDoneEcts(examGrades, examStats)
        if (examStats.selfComputedArithmeticMean) {
            calculateArithmeticMean(examGrades, examStats)
        }
        if (examStats.selfComputedWeightedAverage) {
            calculateWeightedMean(examGrades, examStats)
        }
    }

    fun calculateArithmeticMean(examGrades: List<IExamGrade>, examStats: ExamStatsData) {
        examStats.arithmeticMean = calculateArithmeticMean(examGrades)
    }

    fun calculateArithmeticMean(examGrades: List<IExamGrade>): Double {
        var arithmeticMeanPartial = 0.0
        var totalExamsDone = 0

        for (eg in examGrades) {
            if (isGradeValidForGpa(eg.examGradeParsed)) {
                totalExamsDone++
                arithmeticMeanPartial += convertGradeToDouble(eg.examGradeParsed)
            }
        }

        return if (totalExamsDone > 0 && arithmeticMeanPartial > 0.0) {
            (arithmeticMeanPartial / totalExamsDone * 100).roundToLong() / 100.0
        } else {
            0.0
        }
    }

    fun calculateWeightedMean(examGrades: List<IExamGrade>, examStats: ExamStatsData) {
        examStats.weightedAverage = calculateWeightedMean(examGrades)
    }

    fun calculateWeightedMean(examGrades: List<IExamGrade>): Double {
        var ectsDoneForWeightedAverage = 0.0
        var weightedAveragePartial = 0.0
        var totalExamsDone = 0

        for (eg in examGrades) {
            if (isGradeValidForGpa(eg.examGradeParsed)) {
                totalExamsDone++
                ectsDoneForWeightedAverage += eg.examCfuCents.toDouble()
                weightedAveragePartial += convertGradeToDouble(eg.examGradeParsed) * eg.examCfuCents
            }
        }

        return if (totalExamsDone > 0 && weightedAveragePartial > 0.0 && ectsDoneForWeightedAverage > 0) {
            (weightedAveragePartial / ectsDoneForWeightedAverage * 100).roundToLong() / 100.0
        } else {
            0.0
        }
    }

    fun calculateTotalEcts(examGrades: List<IExamGrade>, examStats: ExamStatsData) {
        examStats.cfuTotalCents = calculateTotalEcts(examGrades)
    }

    fun calculateTotalEcts(examGrades: List<IExamGrade>): Int {
        var ectsTotal = 0
        for (eg in examGrades) {
            ectsTotal += eg.examCfuCents
        }
        return ectsTotal
    }

    fun calculateDoneEcts(examGrades: List<IExamGrade>, examStats: ExamStatsData) {
        examStats.cfuDoneCents = calculateDoneEcts(examGrades)
    }

    fun calculateDoneEcts(examGrades: List<IExamGrade>): Int {
        var ectsDone = 0
        for (eg in examGrades) {
            if (isGradePositive(eg.examGradeParsed)) {
                ectsDone += eg.examCfuCents
            }
        }
        return ectsDone
    }

    fun parseEcts(input: String?): Int = parseDouble(input).orElse(0.0).roundToInt()

    fun parseEctsCents(input: String?): Int = parseDouble(input).orElse(0.0).let { it * 100 }.roundToInt()

    abstract fun parseGrade(grade: String?): Grade

    /**
     * Method used to deal with those cases where an exam has some indication whether it was passed or not but might not
     * have a grade yet
     */
    fun parseGradeWithStatus(grade: String?, statuses: List<String>): Grade {
        val parsedGrade = parseGrade(grade)
        val textSaysExamPassed = statuses.map { it.trim() }.any { it in statusesForPassed }
        //note: the exam might have been passed but not have a vote
        return if (textSaysExamPassed) {
            GRADE_PASSED
        } else {
            parsedGrade
        }
    }

    fun normalizeString(str: String) = str.trim().lowercase(Locale.ROOT).replace("[^a-z0-9]".toRegex(), "")


    companion object {

        const val DEFAULT_ECTS_BACHELOR = 18000
        const val DEFAULT_ECTS_MASTER = 12000

        internal val decimalRegex = "[0-9]+".toRegex()
        internal val doubleRegex = "[0-9]+([.,][0-9]+)?".toRegex()

        const val GRADE_PASSED: Grade = "@passed"
        const val GRADE_DISTINCTION: Grade = "@distinction"
        const val GRADE_TODO: Grade = "@todo"

        const val GRADE_PASSED_DOUBLE = -1001.0
        const val GRADE_DISTINCTION_DOUBLE = -1002.0

        // TODO in some place 0 might be (or is) a valid vote. we should use a flag value but at the moment it would break
        // TODO few computations. Definitely to be solved
        const val GRADE_TODO_DOUBLE = 0.0

//        val DE = GermanyStatsService
        val IT = ItalyStatsService
//        val NL = NetherlandsStatsService
//        val BE = BelgiumStatsService
//        val DK = DenmarkStatsService
//        val EE = EstoniaStatsService
//        val FI = FinlandStatsService
//        val NO = NorwayStatsService
//        val SE = SwedenStatsService
//        val US = UsaStatsService

        private val instances = mapOf(
//            Country.DE to DE,
            Country.IT to IT,
//            Country.NL to NL,
//            Country.BE to BE,
//            Country.DK to DK,
//            Country.EE to EE,
//            Country.FI to FI,
//            Country.NO to NO,
//            Country.SE to SE,
//            Country.US to US
        )

        val supportedCountries = instances.keys

        fun getFor(country: Country): StatsService = instances.getOrDefault(country, DummyStatsService)
    }

}
