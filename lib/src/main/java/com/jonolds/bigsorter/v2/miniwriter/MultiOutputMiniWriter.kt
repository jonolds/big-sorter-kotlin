package com.jonolds.bigsorter.v2.miniwriter

class MultiOutputMiniWriter<T>(
    val writers: List<MiniWriter<T>>,
    override val receiverClass: Class<T>
) : MiniWriter<T>() {

    override fun writeBulk(list: List<T>) {
        for (w in writers)
            w.writeBulk(list)
    }

    override fun close() = writers.forEach { it.close() }

}