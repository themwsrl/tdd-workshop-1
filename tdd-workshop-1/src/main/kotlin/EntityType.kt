enum class EntityType {
    TAX,
    TAX1,
    CAREER,
    EXAMGRADE,
    EXAMGRADE1,
    APPLIEDTEST,
    AVAILABLERESULT,
    AVAILABLETEST,
    STUDENT,
    STUDENT1,
    STUDENT2,
    EXAMSTATS,
    TESTDETAIL,
    AVAILABLEPARTIALTEST,
    EXAM,
    PROFILE,
    LOGIN;
}

fun String.toEntityType(): EntityType = EntityType.valueOf(this)
