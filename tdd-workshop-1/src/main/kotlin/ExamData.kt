import ParserUtils.getCurrentAcademicYear

class ExamData(
    val serviceCode: String,
    val country: Country
) : ParsedData {

    var examCode: String? = null

    var academicYear: Int = getCurrentAcademicYear()

    var examDesc: String = ""

    var examCfu: Int
        get() = examCfuCents / 100
        set(value) {
            examCfuCents = value * 100
        }

    var examCfuCents: Int = 0

    // Contains the Parse UID of the chat-room
    var examRoom: String? = null

    var examStudents: Int? = null

    override fun isSafe() = true
    override fun getImportantPropertyNames(): List<String> = listOf("examCfu", "examDesc", "examCode", "academicYear")

    override fun toString() =
        "ExamData(serviceCode='$serviceCode', country=$country, examCode=$examCode, academicYear=$academicYear, examDesc='$examDesc', " +
                "examCfuCents=$examCfuCents, examRoom=$examRoom, examStudents=$examStudents)"

}
