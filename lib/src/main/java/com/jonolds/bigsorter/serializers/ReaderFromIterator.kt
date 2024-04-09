package com.jonolds.bigsorter.serializers

import com.jonolds.bigsorter.ReaderBS

class ReaderFromIterator<T>(private var iter: Iterator<T>?) : ReaderBS<T> {


	override fun read(): T? =
		if (iter == null || !iter!!.hasNext()) {
			iter = null
			null
		}
		else
			iter!!.next()


	override fun close() { }
}
