package com.jonolds.bigsorter.v2

import com.jonolds.bigsorter.serializers.BinLineTempSerializer
import com.jonolds.bigsorter.v2.phase.tag
import java.lang.reflect.Array.newInstance


fun main() {

    val s = DatMapContext()
        .input(BinLineTempSerializer(), "")
        .map2 { it.toString() }
        .map2 { it.toCharArray() }
        .map2 { it.toString().toByteArray() }
        .output("", BinLineTempSerializer())
        .tag("test")


    val p: Array<in String> = Array<Any?>(3) { "null" } as Array<in String>


    val temp = test(listOf("as", "ap"))

    println(test2(temp.arr as Array<String>))

}


fun test2(arr: Array<String>) {
    println(arr.contentToString())
}

class Temp<T>(
    val clazz: Class<T>,
    elems: List<T>
) {

    val arr: Array<T> = newInstance(clazz, 3) as Array<T>

    init {
        elems.forEachIndexed { i, t -> arr[i] = t }
    }

}


inline fun <reified T>test(list: List<T>): Temp<*> {
    return Temp(T::class.java, list)
}





