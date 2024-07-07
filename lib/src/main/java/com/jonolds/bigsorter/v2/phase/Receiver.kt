package com.jonolds.bigsorter.v2.phase

import com.jonolds.bigsorter.ChannelWriterFactory
import com.jonolds.bigsorter.v2.DatMapContext
import com.jonolds.bigsorter.v2.miniwriter.MiniWriter
import com.jonolds.bigsorter.v2.miniwriter.miniWriter


interface Receiver<T>: Phase {

    var parent: Sender<T>?

    val receiverClass: Class<T>

    fun getWriter(): MiniWriter<T>

}






class FileOutputPhase<P>(
    val filename: String,
    val channelWriterFactory: ChannelWriterFactory<P>,
    override var parent: Sender<P>?,
    context: DatMapContext? = null,
): SinkPhase<P> {


    override val receiverClass: Class<P> get() = parent!!.senderClass

    override var tag: String? = null

    override val context: DatMapContext by lazy { context ?: parent!!.context }


    override val filenames: List<String> get() = listOf(filename)


    override fun getWriter(): MiniWriter<P> = channelWriterFactory.miniWriter(filename, defaultConfig.bufferSize, receiverClass)

}