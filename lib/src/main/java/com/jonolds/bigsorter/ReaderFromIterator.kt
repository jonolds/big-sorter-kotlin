package com.jonolds.bigsorter

import com.github.davidmoten.guavamini.Preconditions
import java.io.IOException

class ReaderFromIterator<T>(private var it: Iterator<T>?) : ReaderBS<T> {


	init {
		Preconditions.checkNotNull(it)
	}

	@Throws(IOException::class)
	override fun read(): T? {
		if (it == null || !it!!.hasNext()) {
			// help gc
			it = null
			return null
		} else {
			return it!!.next()
		}
	}

	@Throws(IOException::class)
	override fun close() { }
}
