package com.jonolds.bigsorter.v2



fun main() {

    val f0 = MapTransf<Int, Double> { it + 2.0 }
    val f1 = MapTransf<Double, Float> { (it + 3).toFloat() }

    val func = combine<Int, Double, Float>(f0::eval, f1::eval)


}

inline fun <A, B, C> combine(crossinline f0: (A) -> B, crossinline f1: (B) -> C): (A) -> C = {
    f1(f0(it))
}


class MapTransf<A, B>(
    val mapper: (A) -> B
) {
    inline fun eval(a: A): B {
        return mapper(a)
    }
}




