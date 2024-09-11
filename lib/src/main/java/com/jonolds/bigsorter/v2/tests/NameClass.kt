package com.jonolds.bigsorter.v2.tests

//@JvmInline
//value
class NameClass(inline val name: String) {

    inline fun func() {
        println(name)
    }
}


val nameClasses = listOf(
    NameClass("a"),
    NameClass("b"),
    NameClass("c")
)
