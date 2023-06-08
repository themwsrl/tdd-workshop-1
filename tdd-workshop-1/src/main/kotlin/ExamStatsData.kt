class ExamStatsData : ParsedData {

    //Media aritmetica
    var arithmeticMean: Double? = null

    //Media ponderata
    var weightedAverage: Double? = null

    //Matricola :-(
    var studentCode: String? = null

    //Media aritmetica calcolata?
    var selfComputedArithmeticMean = false

    //Media pesata calcolata?
    var selfComputedWeightedAverage = false

    var cfuTotal: Int
        get() = cfuTotalCents / CENTS
        set(value) {
            cfuTotalCents = value * CENTS
        }

    //CFU totali (somma degli attuali esami in piano di studio)
    var cfuTotalCents: Int = 0

    var cfuDone: Int?
        get() = cfuDoneCents?.let { it / CENTS }
        set(value) {
            cfuDoneCents = value?.let { it * CENTS }
        }

    //CFU totali  fatti (somma degli attuali esami passati)
    var cfuDoneCents: Int? = null

    override fun isSafe(): Boolean = true
    override fun getImportantPropertyNames(): List<String> = listOf("arithmeticMean", "cfuDoneCents", "cfuTotalCents", "weightedAverage")

    override fun toString(): String {
        return "ExamStatsData(arithmeticMean=$arithmeticMean, weightedAverage=$weightedAverage, studentCode='$studentCode', " +
                "selfComputedArithmeticMean=$selfComputedArithmeticMean, selfComputedWeightedAverage=$selfComputedWeightedAverage, cfuTotal=$cfuTotal, " +
                "cfuTotalCents=$cfuTotalCents, cfuDone=$cfuDone, cfuDoneCents=$cfuDoneCents)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ExamStatsData

        if (arithmeticMean != other.arithmeticMean) return false
        if (weightedAverage != other.weightedAverage) return false
        if (studentCode != other.studentCode) return false
        if (selfComputedArithmeticMean != other.selfComputedArithmeticMean) return false
        if (selfComputedWeightedAverage != other.selfComputedWeightedAverage) return false
        if (cfuTotalCents != other.cfuTotalCents) return false
        if (cfuDoneCents != other.cfuDoneCents) return false

        return true
    }

    override fun hashCode(): Int {
        var result = arithmeticMean?.hashCode() ?: 0
        result = 31 * result + (weightedAverage?.hashCode() ?: 0)
        result = 31 * result + (studentCode?.hashCode() ?: 0)
        result = 31 * result + selfComputedArithmeticMean.hashCode()
        result = 31 * result + selfComputedWeightedAverage.hashCode()
        result = 31 * result + cfuTotalCents
        result = 31 * result + (cfuDoneCents ?: 0)
        return result
    }


}
