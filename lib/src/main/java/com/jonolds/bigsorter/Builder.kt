package com.jonolds.bigsorter

import com.github.davidmoten.guavamini.Lists
import com.github.davidmoten.guavamini.Preconditions
import java.io.File
import java.io.InputStream
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.function.Consumer
import java.util.function.Function
import kotlin.Comparator


@Suppress("UNCHECKED_CAST")
class Builder<T>(@JvmField val serializer: SerializerBS<T>) {


	@JvmField var inputs: MutableList<Source> = Lists.newArrayList()
	@JvmField var inputReaderFactory: Optional<InputStreamReaderFactory<T>> = Optional.empty()
	@JvmField var output: File? = null
	@JvmField var comparator: Comparator<in T>? = null
	@JvmField var maxFilesPerMerge: Int = 100
	@JvmField var maxItemsPerFile: Int = 100000
	@JvmField var logger: Consumer<in String>? = null
	@JvmField var bufferSize: Int = 8192
	@JvmField var tempDirectory: File = File(System.getProperty("java.io.tmpdir"))
	@JvmField var transform: Function<in ReaderBS<T>, out ReaderBS<out T>> =
		Function<ReaderBS<T>, ReaderBS<out T>> { r: ReaderBS<T> -> r }
	@JvmField var unique: Boolean = false
	@JvmField var initialSortInParallel: Boolean = false
	@JvmField var outputWriterFactory: Optional<OutputStreamWriterFactory<T?>> = Optional.empty()

	/**
	 * Sets a conversion for input before sorting with the main serializer happens.
	 * Only applies when input is specified as a File or another supplier of an
	 * InputStream.
	 *
	 * @param <S>           input record type
	 * @param readerFactory readerFactory for the input record type (can be a [SerializerBS])
	 * @param mapper        conversion to T
	 * @return this
	</S> */
	fun <S> inputMapper(readerFactory: InputStreamReaderFactory<out S>, mapper: Function<in S, T>): Builder<T> {
		Preconditions.checkArgument(inputReaderFactory.isEmpty)
		val factory =
			InputStreamReaderFactory { inStr: InputStream ->
				readerFactory.createReader(inStr).map(mapper)
			}
		this.inputReaderFactory = Optional.of(factory)
		return this
	}

	fun comparator(comparator: Comparator<*>): Builder2<T> {
		this.comparator = comparator as Comparator<in T>
		return Builder2<T>(this)
	}

	fun <S: Comparable<S>> naturalOrder(): Builder2<T> {
		return comparator(Comparator.naturalOrder<S>())
	}

	companion object {
		val DATE_TIME_PATTERN: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.Sxxxx")
	}
}