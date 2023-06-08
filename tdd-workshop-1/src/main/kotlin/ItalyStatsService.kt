import kotlin.math.ceil
import kotlin.math.roundToInt

object ItalyStatsService : StatsService() {

    override val minGrade: Grade = "18"
    override val maxGrade: Grade = GRADE_DISTINCTION
    override val supportsGPA: Boolean = true
    override val supportsGraduationBase: Boolean = true
    override val gradesDistributionClasses: Array<Grade>
        get() = arrayOf("18", "21", "24", "27", "30")

    override fun validateProjectionExamInput(ectsCents: Int, grade: Grade): Boolean {
        return when {
            ectsCents < 0 -> false
            ectsCents > 360_00 -> false
            else -> isGradeValidForGpa(grade)
        }
    }

    override fun validateProjectionGpaInput(ectsCents: Int, gpa: Double): Boolean {
        return when {
            ectsCents < 0 -> false
            ectsCents > 360_00 -> false
            else -> isGradeValidForGpa(convertDoubleToGrade(gpa))
        }
    }

    override fun validateProjectionGraduationBaseInput(ectsCents: Int, graduationBase: Double): Boolean {
        return when {
            ectsCents < 0 -> false
            ectsCents > 360_00 -> false
            else -> graduationBase in 66.0..110.0
        }
    }

    override val defaultGpaPeerDistribution: List<Pair<Grade, Double>> = listOf(
        "18" to 1.0,
        "19" to 1.0,
        "20" to 2.0,
        "21" to 4.0,
        "22" to 7.0,
        "23" to 10.0,
        "24" to 12.0,
        "25" to 14.0,
        "26" to 15.0,
        "27" to 13.0,
        "28" to 11.0,
        "29" to 7.0,
        "30" to 2.0,
        GRADE_DISTINCTION to 1.0
    )

    override fun calculateDefaultGpaPerformance(gpa: Double): Int =
        when {
            gpa >= 29.5 -> 98
            gpa >= 29.0 -> 95
            gpa >= 28.44 -> 90
            gpa >= 27.67 -> 80
            gpa >= 26.83 -> 67
            gpa >= 25.71 -> 50
            gpa >= 24.27 -> 30
            gpa >= 23.0 -> 15
            else -> 2

        }

    override fun convertGradeToDouble(grade: Grade): Double =
        when (grade) {
            GRADE_PASSED -> 1.0
            GRADE_DISTINCTION -> 30.0
            GRADE_TODO -> 0.0
            else -> mapVoteToDouble(grade)
        }

    fun mapVoteToDouble(vote: String?): Double {
        return if (vote != null) {
            val safeVote = vote.replace("/30", "")

            if (safeVote.contains(",")) {
                safeVote.replace(",", ".").toDouble()
            } else {
                safeVote.toDouble()
            }
        } else {
            0.0
        }
    }

    override fun convertDoubleToGrade(grade: Double, roundCeil: Boolean): Grade {
        return when (grade) { // TODO skipping @todo because it might be misconverted till we won't move it from 0.0
            GRADE_DISTINCTION_DOUBLE -> GRADE_DISTINCTION
            GRADE_PASSED_DOUBLE -> GRADE_PASSED
            else -> when {
                grade < 18 -> GRADE_TODO
                grade > 30 -> GRADE_DISTINCTION
                else -> (if (roundCeil) ceil(grade).roundToInt() else grade.roundToInt()).toString()
            }
        }
    }

    override fun convertGradeToString(grade: Grade): String =
        when (grade) {
            GRADE_PASSED -> "I"
            GRADE_DISTINCTION -> "30L"
            GRADE_TODO -> ""
            else -> grade.toDouble().roundToInt().toString()
        }

    override fun convertGradeToGradeLevel(grade: Grade): GradeLevel = when (grade) {
        GRADE_TODO -> GradeLevel.NONE
        GRADE_DISTINCTION -> GradeLevel.TOP
        GRADE_PASSED -> GradeLevel.FINE
        else -> {
            val dbl = convertGradeToDouble(grade)
            when {
                dbl >= 27 -> GradeLevel.TOP
                dbl >= 24 -> GradeLevel.FINE
                else -> GradeLevel.SOSO
            }
        }
    }

    override fun convertAverageGradeToDifficultyIndex(average: Double): DifficultyIndex =
        when {
            average >= 28.44 -> DifficultyIndex.VERY_EASY
            average >= 26.35 -> DifficultyIndex.EASY
            average >= 24.27 -> DifficultyIndex.AVERAGE
            average >= 23.0 -> DifficultyIndex.HARD
            average >= 18.0 -> DifficultyIndex.VERY_HARD
            else -> DifficultyIndex.UNKNOWN
        }


    override fun isGradePositive(grade: Grade): Boolean = grade.matches(positiveGradesRegex)

    override fun parseGrade(grade: String?): Grade {
        val safeInput = grade.orEmpty().trim().replace(",", ".")
        try {

            if (safeInput.isEmpty()) {
                return GRADE_TODO
            }

            if (safeInput.findAnyOf(distinctionFormats, ignoreCase = true) != null) {
                return GRADE_DISTINCTION
            }

            if (safeInput == "I") {
                return GRADE_PASSED
            }

            if (safeInput == "Insufficiente") {
                return GRADE_TODO
            }

            return if (safeInput.matches(passedRegex) || safeInput.length > 2) {
                GRADE_PASSED
            } else if (safeInput.matches(validVoteRegex)) {
                safeInput
            } else {
                GRADE_TODO
            }

        } catch (e: Exception) {
            return GRADE_TODO
        }
    }

    override fun calculateGpaPeerDistribution(gpas: List<Double>): List<Pair<Grade, Double>> {
        if (gpas.isEmpty()) {
            return emptyList()
        }
        return gpas
            .asSequence()
            .filter { it >= 18 }
            .map { it.roundToInt() }
            .map { if (it > 30) 30 else it }
            .groupBy { it }
            .map { (grade, list) -> grade.toString() to list.count().toDouble() }
            .toList()
            .sortedBy { it.first }
            .toList()
    }


    private val validVoteRegex = "18|19|30|2[0-9]".toRegex()
    private val passedRegex = "\\A[a-zA-Z]+\\z".toRegex()
    private val positiveGradesRegex = "18|19|30|2[0-9]|$GRADE_PASSED|$GRADE_DISTINCTION".toRegex()
    private val distinctionFormats = listOf("lode", "30L", "30 L", "30 E LODE", "31", "33")
}
