package com.jonolds.bigsorter

import com.jonolds.bigsorter.Util.close
import java.io.Closeable
import java.io.EOFException
import java.io.IOException
import java.io.UncheckedIOException
import java.util.*
import java.util.function.Function
import java.util.function.Predicate
import java.util.stream.Stream
import java.util.stream.StreamSupport
import kotlin.NoSuchElementException


interface ReaderBS<T> : Closeable, Iterable<T> {

	/**
	 * Returns the next read value. If no more values returns null.
	 *
	 * @return the next read value or null if no more values
	 * @throws IOException on IO problem
	 */
	fun read(): T?


	fun readUnsafe(): T = try {
		read()!!
	} catch (e: Exception) {
		throw EOFException()
	}

	fun readAtMost(limit: Int, result: MutableList<T>): Int {
		var count = 0
		var t = read()
		while (t != null) {
			result.add(t)
			count++
			t = read()
		}
		return count
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

	fun filter(predicate: Predicate<in T>): ReaderBS<T> {
		val r = this

		return object : ReaderBS<T> {

			override fun read(): T? {
				var t = r.read()
				while (t != null && !predicate.test(t))
					t = r.read()
				return t
			}


			override fun close() = r.close()
		}
	}

	fun <S> map(mapper: Function<in T, out S>): ReaderBS<S> {

		val r = this

		return object : ReaderBS<S> {


			override fun read(): S? {
				val v = r.read()
				return if (v == null) null else mapper.apply(v)
			}

			override fun close() = r.close()
		}
	}

	fun flatMap(mapper: Function<in T, out List<T>>): ReaderBS<T> {

		val r = this

		return object : ReaderBS<T> {

			var list: List<T>? = null
			var index: Int = 0

			override fun read(): T? {

				while (list == null || index == list!!.size) {
					val t: T? = r.read()
					if (t == null)
						return null
					else {
						list = mapper.apply(t)
						index = 0
					}
				}

				return list!![index++]
			}

			override fun close() = r.close()
		}
	}

	fun transform(function: Function<in Stream<T>, out Stream<out T>>): ReaderBS<T> {

		val s = function.apply(stream())

		return object : ReaderBS<T> {

			val iter: Iterator<T> = s.iterator()

			override fun read(): T? = if (iter.hasNext()) iter.next() else null

			override fun close() = s.close()
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
			.onClose { close(this@ReaderBS) }
}