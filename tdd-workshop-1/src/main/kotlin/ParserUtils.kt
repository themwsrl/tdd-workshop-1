@file:Suppress("MemberVisibilityCanBePrivate", "unused")

import java.util.*
import java.util.regex.Pattern

object ParserUtils {

    val PATTERN_FLOAT_NUMBER: Pattern = Pattern.compile("^\\D*([0-9]+)(?:[,.]([0-9]+))?\\D*$")
    data class DataUrl(val url: String)
    fun parseDouble(input: String?): Optional<Double> {
        val safeInput = input.orEmpty()
        val nm = PATTERN_FLOAT_NUMBER.matcher(safeInput)
        return if (nm.matches()) {
            Optional.of(
                java.lang.Double.parseDouble(
                    String.format(
                        "%s.%s",
                        nm.group(1),
                        Optional.ofNullable(nm.group(2)).orElse("")
                    )
                )
            )
        } else {
            Optional.empty()
        }
    }
    fun getAvgMean(input: String?): Double {
        var tmp = input.orEmpty()
        return try {

            if (tmp.contains("/")) {
                tmp = tmp.substring(0, tmp.indexOf("/")).trim { it <= ' ' }
            }

            tmp = tmp.replace(',', '.')

            java.lang.Double.parseDouble(tmp)
        } catch (e: Exception) {
            0.0
        }

    }

    // Just always return 2014 for now, to keep things consistent;
    //return getCurrentAcademicYear(DateTime.now());
    @Deprecated("remove this!!!")
    fun getCurrentAcademicYear() = 2014

    fun getAcademicYear(academicYear: String?): Int {
        if (academicYear.isNullOrBlank()) {
            return getCurrentAcademicYear()
        }

        val delimiter1 = "⁄"
        val delimiter2 = "-"
        val delimiter3 = "/"
        val delimiter4 = "?"
        val tokenizedAcademicYear = academicYear.replace(" ", "").split(delimiter1, delimiter2, delimiter3, delimiter4)

        val firstAcademicYear = if (tokenizedAcademicYear.isNotEmpty()) {
            tokenizedAcademicYear.first().filter { it.isDigit() }
        } else {
            getCurrentAcademicYear().toString()
        }

        return when (firstAcademicYear.length) {
            4 -> Integer.parseInt(firstAcademicYear)
            2 -> Integer.parseInt("20$firstAcademicYear")
            else -> getCurrentAcademicYear()
        }
    }
}
