package com.jonolds.bigsorter

import com.jonolds.bigsorter.internal.Array2
import java.io.Closeable

interface WriterBS<T> : Closeable {

	fun write(value: T?)

	fun flush()


	fun writeBulk(list: List<T>, start: Int = 0, end: Int = list.size) {
		for (i in start until end)
			write(list[i])
	}


	fun writeBulk(list: Array<T>, start: Int = 0, end: Int = list.size) {
		for (i in start until end)
			write(list[i])
	}

	fun writeBulkArray(list: Array2<T>, start: Int = 0, end: Int = list.size) {
		for (i in start until end)
			write(list.arr[i])
	}

	fun <S> mapper(mapper: (S?) -> T?): WriterBS<S> {
		val w = this

		return object : WriterBS<S> {
			override fun write(value: S?) = w.write(mapper(value))

			override fun flush() = w.flush()

			override fun close() = w.close()
		}
	}
}
