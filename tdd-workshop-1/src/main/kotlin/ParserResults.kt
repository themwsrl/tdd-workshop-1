sealed class ParserResult private constructor(val type: EntityType, val data: List<ParsedData>) {
    class ExamStats(data: List<ExamStatsData>) : ParserResult(type = EntityType.EXAMSTATS, data = data)
    class ExamGrade(data: List<ExamGradeData>) : ParserResult(type = EntityType.EXAMGRADE, data = data)
}

class ParserResults(val serviceCode: String, val parserResults: List<ParserResult>) {
    val results = parserResults.filter { it.data.all { parsedData -> parsedData.isSafe() } }.associate { it.type to it.data }

    fun size(): Int = results.size

    fun <T : ParsedData> get(entityType: EntityType): List<T> = (results[entityType] ?: emptyList<T>()) as List<T>

    fun <T : ParsedData> contains(entityType: EntityType): Boolean = this.results.containsKey(entityType) && get<T>(entityType).isNotEmpty()
    override fun toString() = "ParserResults(serviceCode='$serviceCode', results=$results)"
}
