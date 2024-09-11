package com.jonolds.bigsorter.v2.phase

import com.jonolds.bigsorter.ChannelReaderFactory
import com.jonolds.bigsorter.v2.DatMapContext
import com.jonolds.bigsorter.v2.fastlist.FastArrayList
import com.jonolds.bigsorter.v2.miniwriter.MiniWriter
import com.jonolds.bigsorter.v2.pushFromFile
import java.io.File
import kotlin.collections.ArrayList


interface Sender<T>: Phase {

    var child: Receiver<T>?

    val senderClass: Class<T>

    fun useFromBelow(miniWriter: MiniWriter<T>)

    fun setChild(newChild: Receiver<T>) {
        this.child = newChild
        newChild.parent = this
    }

}


class MultiSourcePhase<P>(
    val mainSource: SourcePhase<P>,
    val otherSources: List<SourcePhase<P>>
) : SourcePhase<P> by mainSource {


    override var tag: String? = null

    override fun useFromBelow(miniWriter: MiniWriter<P>) {
        for (source in otherSources)
            source.push(miniWriter::writeBulk)
        mainSource.push(miniWriter::writeBulk)
        miniWriter.close()
    }

}


interface FileSourcePhase<T>: SourcePhase<T> {

    override val filenames: List<String>
    val channelReaderFactory: ChannelReaderFactory<T>

    override fun push(writeList: (List<T>) -> Unit) {

        val result = FastArrayList<T>(defaultConfig.maxItemsPerPart, senderClass)

        for (file in filenames.map { File(it) }) {
            pushFromFile(file, channelReaderFactory, defaultConfig.bufferSize,
                defaultConfig.maxItemsPerPart, result, writeList)
        }
        if (result.isNotEmpty())
            writeList(result)
    }
}


class FileInputPhase<T>(
    override val filenames: List<String>,
    override val channelReaderFactory: ChannelReaderFactory<T>,
    override val senderClass: Class<T>,
    override val context: DatMapContext
): FileSourcePhase<T> {


    override var child: Receiver<T>? = null


    override var tag: String? = null

}

