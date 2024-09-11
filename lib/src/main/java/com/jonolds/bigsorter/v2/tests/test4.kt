package com.jonolds.bigsorter.v2.tests


typealias IntFunc = (Int) -> Int

fun main() {


//    val func0 = PredicateClass<Int>{ it %2 ==0 }
//    val func1 = MapClass<Int, Double> { it + 2.0 }


//    val f0 = { a: Int -> a + 3 }

//    val f1 = { a: Int -> a * -7 }

//    val result = { a: Int -> f0(a) * -7 }

    val result = func0().let { merge(it, { a: Int -> a * -7}) }



}


inline fun merge(
    crossinline f0: (Int) -> Int,
    crossinline f1: (Int) -> Int,
): (Int) -> Int = {
    f1(f0(it))
}


inline fun func0(): (Int) -> Int = { it + 3 }





