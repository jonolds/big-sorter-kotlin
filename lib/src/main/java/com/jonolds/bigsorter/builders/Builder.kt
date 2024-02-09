package com.jonolds.bigsorter.builders

import com.jonolds.bigsorter.*
import com.jonolds.bigsorter.serializers.SerializerBS
import java.io.File
import java.io.InputStream
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.function.Consumer
import java.util.function.Function
import java.util.function.Supplier


@Suppress("UNCHECKED_CAST")
class Builder<T>(val serializer: SerializerBS<T>) {


	var inputs: MutableList<Source> = ArrayList()
	var inputReaderFactory: Optional<InputStreamReaderFactory<T>> = Optional.empty()
	var output: File? = null
	var comparator: Comparator<in T>? = null
	var maxFilesPerMerge: Int = 100
	var maxItemsPerFile: Int = 100000
	var logger: Consumer<in String>? = null
	var bufferSize: Int = 8192
	var tempDirectory: File = File(System.getProperty("java.io.tmpdir"))
	var transform: Function<in ReaderBS<T>, out ReaderBS<out T>> =
		Function<ReaderBS<T>, ReaderBS<out T>> { r: ReaderBS<T> -> r }
	var unique: Boolean = false
	var initialSortInParallel: Boolean = false
	var outputWriterFactory: Optional<OutputStreamWriterFactory<T?>> = Optional.empty()


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
		check(inputReaderFactory.isEmpty)
		this.inputReaderFactory = Optional.of(InputStreamReaderFactory { inStr -> readerFactory.createReader(inStr).map(mapper) })
		return this
	}


	fun comparator(comparator: Comparator<*>): Builder2<T> {
		this.comparator = comparator as Comparator<in T>
		return Builder2(this)
	}


	fun <S: Comparable<S>> naturalOrder(): Builder2<T> = comparator(Comparator.naturalOrder<S>())


	fun buildInputSuppliers(): List<Supplier<ReaderBS<out T>>> = inputs.map { source ->
		Supplier {
			if (source.type == SourceType.SUPPLIER_INPUT_STREAM)
				transform.apply(inputReaderFactory.orElse(serializer).createReader((source.source as Supplier<InputStream>).get()))
			else // Supplier of a Reader
				transform.apply((source.source as Supplier<ReaderBS<T>>).get())
		}
	}

	companion object {
		val DATE_TIME_PATTERN: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.Sxxxx")
	}
}

