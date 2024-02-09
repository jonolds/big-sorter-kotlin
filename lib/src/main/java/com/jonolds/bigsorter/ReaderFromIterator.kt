package com.jonolds.bigsorter

import java.io.EOFException

class ReaderFromIterator<T>(private var iter: Iterator<T>?) : ReaderBS<T> {


	init {
		checkNotNull(iter)
	}

	override fun read(): T? =
		if (iter == null || !iter!!.hasNext()) {
			iter = null
			null
		}
		else
			iter!!.next()


	override fun close() { }
}
