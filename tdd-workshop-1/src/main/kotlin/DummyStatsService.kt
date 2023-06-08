import kotlin.random.Random

object DummyStatsService: StatsService() {
    override val minGrade: Grade = "1"
    override val maxGrade: Grade = "10"
    override val supportsGPA: Boolean = false
    override val supportsGraduationBase: Boolean = false
    override val gradesDistributionClasses: Array<Grade>
        get() = arrayOf("1", "3", "5", "7", "9")

    override val defaultGpaPeerDistribution: List<Pair<Grade, Double>> = listOf(
        "1" to Random.nextInt(0, 20).toDouble(),
        "3" to Random.nextInt(0, 20).toDouble(),
        "5" to Random.nextInt(0, 20).toDouble(),
        "7" to Random.nextInt(0, 20).toDouble(),
        "9" to Random.nextInt(0, 20).toDouble(),
        "10" to Random.nextInt(0, 20).toDouble()
    )

    override fun convertGradeToGradeLevel(grade: Grade): GradeLevel {
        return GradeLevel.FINE
    }

    override fun convertAverageGradeToDifficultyIndex(average: Double): DifficultyIndex {
        return DifficultyIndex.AVERAGE
    }

    override fun calculateDefaultGpaPerformance(gpa: Double): Int = 87

    override fun convertGradeToDouble(grade: Grade): Double = 0.0

    override fun convertDoubleToGrade(grade: Double, roundCeil: Boolean): Grade = grade.toString()

    override fun convertGradeToString(grade: Grade): String = "0.0"

    override fun isGradePositive(grade: Grade): Boolean = true

    override fun parseGrade(grade: String?): Grade = "10.0"
}
