package com.jonolds.bigsorter

import java.io.Closeable
import java.io.IOException
import java.util.function.Function

interface WriterBS<T> : Closeable {

	fun write(value: T?)

	fun flush()

	fun <S> map(mapper: Function<in S?, out T?>): WriterBS<S> {

		val w = this

		return object : WriterBS<S> {
			override fun write(value: S?) = w.write(mapper.apply(value))

			override fun flush() = w.close()

			override fun close() = w.close()
		}
	}
}
