package com.jonolds.bigsorter.v2.tests

sealed class InlineFunc<A>

class PredicateClass<T>(
    val predicate: (T) -> Boolean
): InlineFunc<T>() {

}

class MapClass<A, B>(
    inline val mapper: (A) -> B
): InlineFunc<A>() {


}


