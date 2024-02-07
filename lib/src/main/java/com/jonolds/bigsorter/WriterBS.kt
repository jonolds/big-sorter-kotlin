package com.jonolds.bigsorter

import java.io.Closeable
import java.io.IOException
import java.util.function.Function

interface WriterBS<T> : Closeable {

	@Throws(IOException::class)
	fun write(value: T?)

	@Throws(IOException::class)
	fun flush()

	fun <S> map(mapper: Function<in S?, out T?>): WriterBS<S> {
		val w = this
		return object : WriterBS<S> {
			@Throws(IOException::class)
			override fun write(value: S?) {
				w.write(mapper.apply(value))
			}

			@Throws(IOException::class)
			override fun flush() {
				w.close()
			}

			@Throws(IOException::class)
			override fun close() {
				w.close()
			}
		}
	}
}
