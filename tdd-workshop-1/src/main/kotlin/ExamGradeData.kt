import java.util.*

class ExamGradeData : ParsedData, IExamGrade {

    lateinit var exam: ExamData

    //TODO - make into enum
    // Attività didattica superata, frequentata o pianificata
    var examStatus: String? = null

    // Non fatto = 0, Idoneo = 1, 30 e lode = 31
    val examGrade: Double
        get() = StatsService.getFor(exam.country).convertGradeToDouble(examGradeParsed)

    override val examCfuCents: Int
        get() = exam.examCfuCents

    override var examGradeParsed: Grade = StatsService.GRADE_TODO

    override var examDate: Date? = null

    var examYear: Int? = null

    override fun isSafe() = ::exam.isInitialized
    override fun getImportantPropertyNames(): List<String> = listOf("examCfuCents", "examGrade", "examStatus")

    override fun toString() = "ExamGradeData(exam=$exam, examStatus=$examStatus, examGradeParsed='$examGradeParsed', examDate=$examDate, examYear=$examYear)"

}
