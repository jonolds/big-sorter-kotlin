package com.jonolds.bigsorter

import com.jonolds.bigsorter.internal.Array2
import com.jonolds.bigsorter.internal.FixedArray
import java.io.Closeable
import java.io.IOException
import java.io.UncheckedIOException
import java.util.*
import java.util.stream.Stream
import java.util.stream.StreamSupport


interface ReaderBS<T> : Closeable, Iterable<T> {

	/**
	 * Returns the next read value. If no more values returns null.
	 *
	 * @return the next read value or null if no more values
	 * @throws IOException on IO problem
	 */
	fun read(): T?


	fun readBulk(limit: Int, result: MutableList<in T>): Int {
		var count = 0
		while (count < limit) {

			val t = read() ?: break
			result.add(t)
			count++
		}
		return count
	}


	fun readBulkArray(limit: Int, result: Array2<T>): Array2<T> {
		TODO("Not yet implemented")
	}


	fun <S> readBulkArray(limit: Int, result: Array2<S>, mapper: (T) -> S): Array2<S> {
		TODO("Not yet implemented")
	}

	/**
	 * Returns the next read value. If no more values close() is called then null
	 * returned.
	 *
	 * @return the next read value or null if no more values
	 * @throws IOException on IO problem
	 */
	fun readAutoClosing(): T? = read() ?: let {
		close()
		null
	}


	fun <S> mapper(mapper: (T) -> S): ReaderBS<S> {

		val tReader = this

		return object : ReaderBS<S> {


			override fun read(): S? {
				val t = tReader.read()
				return if (t == null) null else mapper(t)
			}

			override fun readBulk(limit: Int, result: MutableList<in S>): Int {
				val newResult = ArrayList<T>(limit)
				val count = tReader.readBulk(limit, newResult)

				result.addAll(newResult.map { mapper(it) })
				return count
			}


			override fun readBulkArray(limit: Int, result: Array2<S>): Array2<S> {
				tReader.readBulkArray(limit, result, mapper)
				return result
			}

			override fun close() = tReader.close()
		}
	}


	override fun iterator(): Iterator<T> = object : Iterator<T> {

		var t: T? = null

		override fun hasNext(): Boolean {
			load()
			return t != null
		}

		override fun next(): T {
			load()
			if (t == null)
				throw NoSuchElementException()
			else {
				val v: T = t!!
				t = null
				return v
			}
		}

		fun load() {
			if (t == null) {
				try {
					t = read()
				} catch (e: IOException) {
					throw UncheckedIOException(e)
				}
			}
		}
	}

	fun stream(): Stream<T> = StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator(), Spliterator.ORDERED), false)
			.onClose { this@ReaderBS.close() }
}

