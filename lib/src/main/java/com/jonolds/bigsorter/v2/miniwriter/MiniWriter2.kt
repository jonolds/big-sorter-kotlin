package com.jonolds.bigsorter.v2.miniwriter

import com.jonolds.bigsorter.ChannelWriterFactory
import com.jonolds.bigsorter.Util.createOrReplace
import com.jonolds.bigsorter.WriterBS
import com.jonolds.bigsorter.v2.phase.Sender
import java.io.Closeable
import java.io.File
import java.util.*

abstract class MiniWriter2<T, C>(
    val child: MiniWriter2<C, *>? = null
): Closeable {


    abstract fun writeBulk(list: List<T>, start: Int, end: Int)


    fun writeBulk(list: List<T>) = writeBulk(list, 0, list.size)



    fun <S> map(mapper: (S) -> T): MiniWriter2<S, T> = object : MiniWriter2<S, T>(this) {

        override fun writeBulk(list: List<S>, start: Int, end: Int) {

            val result = ArrayList<T>()
            for (i in start until end)
                result.add(mapper(list[i]))

            this@MiniWriter2.writeBulk(result, start, end)
        }

        override fun close() {
            this@MiniWriter2.close()
        }

    }


    fun filter(predicate: (T) -> Boolean): MiniWriter2<T, T> = object : MiniWriter2<T, T>(this) {

        override fun writeBulk(list: List<T>, start: Int, end: Int) {

            val result = ArrayList<T>(list.size)
            for (i in start until end)
                if (predicate(list[i]))
                    result.add(list[i])

            this@MiniWriter2.writeBulk(result, 0, result.size)
        }

        inline fun <O> writeBulk(list: List<O>, start: Int, end: Int, transform: (O) -> T) {

            val result = ArrayList<T>(list.size)
            for (i in start until end) {
                val trans = transform(list[i])
                if (predicate(trans))
                    result.add(trans)
            }

            this@MiniWriter2.writeBulk(result, 0, result.size)

        }

        override fun close() {
            this@MiniWriter2.close()
        }

    }


    fun filter(predicateFactory: () -> ((T)->Boolean)): MiniWriter2<T, T> = object : MiniWriter2<T, T>(this) {

        val predicate = predicateFactory()

        override fun writeBulk(list: List<T>, start: Int, end: Int) {

            val result = ArrayList<T>()
            for (i in start until end)
                if (predicate(list[i]))
                    result.add(list[i])

            this@MiniWriter2.writeBulk(result, 0, result.size)
        }

        override fun close() {
            this@MiniWriter2.close()
        }

    }

    fun reduce(
        comparator: (lastValue: T, value: T) -> Boolean,
        combiningFunction: (lastValue: T, value: T) -> T,
    ): MiniWriter2<T, T> = object : MiniWriter2<T, T>() {

        var lastValue: T? = null

        var isFirstRound = true

        override fun writeBulk(list: List<T>, start: Int, end: Int) {
            if (list.isEmpty()) return

            val start0 = if (isFirstRound) {
                isFirstRound = false
                lastValue = list[0]
                start+1
            } else start

            val result = ArrayList<T>()

            for (i in start0 until end)
                if (comparator(lastValue!!, list[i]))
                    lastValue = combiningFunction(lastValue!!, list[i])
                else {
                    result.add(lastValue!!)
                    lastValue = list[i]
                }


            this@MiniWriter2.writeBulk(result, 0, result.size)
        }

        override fun close() {
            if (lastValue != null)
                this@MiniWriter2.writeBulk(listOf(lastValue!!))
            this@MiniWriter2.close()
        }


    }




}

fun <T, C> MiniWriter2<T, C>.nonClosable(): MiniWriter2<T, C> = object : MiniWriter2<T, C>() {

    override fun writeBulk(list: List<T>, start: Int, end: Int) = this@nonClosable.writeBulk(list, start, end)

    override fun close() { }
}


class MultiOutputMiniWriter2<T, C>(
    val writers: List<MiniWriter2<T, C>>
) : MiniWriter2<T, C>() {

    override fun writeBulk(list: List<T>, start: Int, end: Int) {
        for (w in writers)
            w.writeBulk(list, start, end)
    }

    override fun close() = writers.forEach { it.close() }

}



class MultiInputMiniWriter2<T, C>(
    extras: List<Sender<T>>,
    val writer: MiniWriter2<T, C>
) : MiniWriter2<T, C>() {

    val stack = Stack<Sender<T>>()
        .also { it.addAll(extras) }

    override fun writeBulk(list: List<T>, start: Int, end: Int) = writer.writeBulk(list, start, end)

    override fun close() {
        if (stack.isNotEmpty())
//            return stack.pop().useFromBelow(this)
        writer.close()
    }

}


fun <T> WriterBS<T>.toMiniWriter2(): MiniWriter2<T, Nothing> = object : MiniWriter2<T, Nothing>() {

    override fun writeBulk(list: List<T>, start: Int, end: Int) = this@toMiniWriter2.writeBulk(list, start, end)

    override fun close() = this@toMiniWriter2.close()

}


fun <T> ChannelWriterFactory<T>.miniWriter2(filename: String, bufferSize: Int): MiniWriter2<T, Nothing> =
    createFileWriter(File(filename).createOrReplace(), bufferSize).toMiniWriter2()