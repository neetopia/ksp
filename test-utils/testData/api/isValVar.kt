

// TEST PROCESSOR: ParameterMutabilityProcessor
// EXPECTED:
// END
// MODULE: lib
// FILE: LibClass.kt
class LibClass(
    field: String,
    val valField: String,
    var varField: String,
)


// MODULE: main(lib)
// FILE: SourceClass.kt
class SourceClass(
    field: String,
    val valField: String,
    var varField: String,
)
