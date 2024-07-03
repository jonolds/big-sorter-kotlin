package com.jonolds.bigsorter.v2

import com.jonolds.bigsorter.ChannelReaderFactory
import java.io.File


inline fun <T> pushFromFile(
    file: File,
    channelReaderFactory: ChannelReaderFactory<T>,
    bufferSize: Int,
    maxItemsPerPart: Int,
    accumulator: MutableList<T>,
    writeList: (List<T>) -> Unit
) {

    if (!file.isFile) return

    val reader = channelReaderFactory.createFileReader(file, bufferSize)

    while (true) {
        val last = accumulator.size
        reader.readBulk(maxItemsPerPart-accumulator.size, accumulator)
        if (accumulator.size == maxItemsPerPart) {
            writeList(accumulator)
            accumulator.clear()
        }
        else if (accumulator.size == last)
            break
    }

    reader.close()
    
    
}