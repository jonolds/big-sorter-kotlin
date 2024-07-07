package com.jonolds.bigsorter.v2



fun main() {

    val ops = listOf(
        MapTransf<Int, Double> { it + 2.0 },
        MapTransf<Double, Float> { (it + 3).toFloat() },
        FilterTransf<Float> { it > 10 }
    )

    val listFunc = combine<Int, Float>(ops)

    val list = listOf(20, 0, 11, 4)

    val s = listFunc(list)

    println(s)


}

typealias Func = (Any) -> Unit

inline fun <A, B> combine(ops: List<Transf<*, *>>): (List<A>) -> List<B> {

    val result = ArrayList<B>()
    val start: Func = { result.add(it as B) }


    val finalFunc = ops.reversed().fold(
        initial = start,
        operation = { acc, value ->
            return@fold { a ->
               value.getExecFunc(acc)(a)
            }
        }
    )

    return { list ->

        for (elem in list)
            finalFunc(elem as Any)
        result
    }

}


abstract class Transf<A, B> {

    abstract fun  getExecFunc(nextAction: (Any) -> Unit): (Any) -> Unit
}

class MapTransf<A, B>(
    val mapper: (A) -> B
) : Transf<A, B>() {



    override inline fun getExecFunc(crossinline nextAction: (Any) -> Unit): (Any) -> Unit = {
        nextAction(mapper(it as A) as Any)
    }
}

class FilterTransf<A>(
    val predicate: (A) -> Boolean
): Transf<A, A>() {

    override inline fun getExecFunc(crossinline nextAction: (Any) -> Unit): (Any) -> Unit = {
        if (predicate(it as A))
            nextAction(it as Any)
    }
}



