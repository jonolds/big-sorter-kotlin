package com.jonolds.bigsorter.v2.miniwriter

import com.jonolds.bigsorter.ChannelWriterFactory
import com.jonolds.bigsorter.Util.createOrReplace
import com.jonolds.bigsorter.WriterBS
import java.io.Closeable
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

abstract class MiniWriter<T>: Closeable {


    abstract val receiverClass: Class<T>


    abstract fun writeBulk(list: List<T>)





    fun reduce(
        comparator: (lastValue: T, value: T) -> Boolean,
        combiningFunction: (lastValue: T, value: T) -> T,
    ): MiniWriter<T> = object : MiniWriter<T>() {


        override val receiverClass: Class<T> = this@MiniWriter.receiverClass

        var lastValue: T? = null

        var isFirstRound = true

        override fun writeBulk(list: List<T>) {
            if (list.isEmpty()) return

            val start0 = if (isFirstRound) {
                isFirstRound = false
                lastValue = list[0]
                1
            } else 0

            val result = ArrayList<T>(list.size)
//            val result = LinkedList<T>()

            for (i in start0 until list.size) {
                val elem = list[i]
                if (comparator(lastValue!!, elem))
                    lastValue = combiningFunction(lastValue!!, elem)
                else {
                    result.add(lastValue!!)
                    lastValue = elem
                }
            }


            this@MiniWriter.writeBulk(result)
        }

        override fun close() {
            if (lastValue != null)
                this@MiniWriter.writeBulk(listOf(lastValue!!))
            this@MiniWriter.close()
        }


    }

}



fun <T> MiniWriter<T>.filter(
    predicate: (T) -> Boolean
): MiniWriter<T> = FilterMiniWriter<T>(this, predicate)



fun <T> MiniWriter<T>.filter(
    predicateFactory: () -> ((T)->Boolean)
): MiniWriter<T> = FilterMiniWriter<T>(this, predicateFactory())


fun <C, T> MiniWriter<C>.map(
    receiverClass: Class<T>,
    mapper: (T) -> C
): MiniWriter<T> =
    MapMiniWriter<T, C>(this, receiverClass, mapper)


fun <T> MiniWriter<T>.nonClosable(): MiniWriter<T> = object : MiniWriter<T>() {

    override val receiverClass: Class<T> = this@nonClosable.receiverClass

    override fun writeBulk(list: List<T>) = this@nonClosable.writeBulk(list)

    override fun close() { }
}


fun <T> ChannelWriterFactory<T>.miniWriter(filename: String, bufferSize: Int, receiverClass: Class<T>): MiniWriter<T> =
    createFileWriter(File(filename).createOrReplace(), bufferSize).toMiniWriter(receiverClass)


fun <T> WriterBS<T>.toMiniWriter(receiverClass: Class<T>): MiniWriter<T> = object : MiniWriter<T>() {

    override val receiverClass: Class<T>  = receiverClass

    override fun writeBulk(list: List<T>) = this@toMiniWriter.writeBulk(list)

    override fun close() = this@toMiniWriter.close()

}
