package com.jonolds.bigsorter.builders

import com.jonolds.bigsorter.ReaderBS
import com.jonolds.bigsorter.ReaderFactory
import com.jonolds.bigsorter.WriterFactory
import com.jonolds.bigsorter.serializers.SerializerBS
import java.io.File
import java.time.format.DateTimeFormatter
import java.util.*


@Suppress("UNCHECKED_CAST")
class Builder<T>(val serializer: SerializerBS<T>) {


	val inputs: MutableList<() -> ReaderBS<out T>> = ArrayList()
	var output: File? = null
	var comparator: Comparator<in T>? = null
	var maxFilesPerMerge: Int = 100
	var maxItemsPerFile: Int = 100000
	var logger: ((String) -> Unit)?= null
	var bufferSize: Int = 8192
	var tempDirectory: File = File(System.getProperty("java.io.tmpdir") + "/big-soreter")
	var transform: (ReaderBS<T>) -> ReaderBS<out T> = { it }
	var unique: Boolean = false
	var initialSortInParallel: Boolean = false
	var outputWriterFactory: Optional<WriterFactory<T?>> = Optional.empty()


	fun tempDirectory(tempDirectory: File): Builder<T> {
		this.tempDirectory = tempDirectory
		return this
	}


	fun comparator(comparator: Comparator<*>): Builder2<T> {
		this.comparator = comparator as Comparator<T>
		return Builder2(this)
	}


	fun <S: Comparable<S>> naturalOrder(): Builder2<T> = comparator(Comparator.naturalOrder<S>())



	companion object {
		val DATE_TIME_PATTERN: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.Sxxxx")
	}
}

