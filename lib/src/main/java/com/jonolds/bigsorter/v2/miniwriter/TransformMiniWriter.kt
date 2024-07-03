package com.jonolds.bigsorter.v2.miniwriter

import java.util.LinkedList

abstract class TransformMiniWriter<T, C>(
    val child: MiniWriter<C>
): MiniWriter<T>() {


    abstract val action: (T) -> Unit

    var result: MutableList<C> = ArrayList()
//    var result: MutableList<C> = LinkedList()


    override fun writeBulk(list: List<T>) {
        for (elem in list)
            action(elem)
        flush()
    }


    open fun flush() {
        if (child is TransformMiniWriter<*, *>) {
            child.flush()
        }
        else {
            child.writeBulk(result)
            result = ArrayList()
//            result = LinkedList()
        }
    }

    override fun close() {
        child.close()
    }


}


class FilterMiniWriter<T>(
    child: MiniWriter<T>,
    val predicate: (T) -> Boolean
): TransformMiniWriter<T, T>(child) {

    override val action: (T) -> Unit =
        if (child is TransformMiniWriter<T, *>) { elem ->
            if(predicate(elem))
                child.action(elem)
        }
        else { elem ->
            if (predicate(elem))
                result.add(elem)
        }
}


class MapMiniWriter<T, C>(
    child: MiniWriter<C>,
    val mapper: (T) -> C
): TransformMiniWriter<T, C>(child) {


    override val action: (T) -> Unit =
        if (child is TransformMiniWriter<C, *>) { elem ->
            child.action(mapper(elem))
        }
        else { elem ->
            result.add(mapper(elem))
        }


}