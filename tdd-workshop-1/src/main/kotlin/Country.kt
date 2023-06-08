import java.util.*
enum class Country {
    ANY,
    IT,
    DE,
    FR,
    ES,
    UK,
    NL,
    BE,
    SE,
    DK,
    FI,
    NO,
    US,
    PT,
    EE;

    companion object {
        fun fromValue(value: String?): Country = value
            ?.let { values().firstOrNull { v -> v.name == value.uppercase(Locale.getDefault()) } ?: ANY }
            ?: ANY
    }
}
