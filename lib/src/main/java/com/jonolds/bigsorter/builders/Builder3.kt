package com.jonolds.bigsorter.builders

import com.jonolds.bigsorter.ReaderBS
import java.io.File
import java.util.function.Function
import java.util.function.Predicate
import java.util.stream.Stream


@Suppress("UNCHECKED_CAST")
class Builder3<T>(val b: Builder<T>) {


	fun filter(predicate: Predicate<in T>): Builder3<T> {
		val currentTransform = b.transform
		return transform { reader -> currentTransform.apply(reader).filter(predicate) }
	}

	fun map(mapper: Function<in T, out T?>): Builder3<T> {
		val currentTransform = b.transform
		return transform { reader -> currentTransform.apply(reader).map(mapper) }
	}

	fun flatMap(mapper: Function<in T, out MutableList<out T>>): Builder3<T> {
		val currentTransform = b.transform
		return transform { reader -> (currentTransform.apply(reader) as ReaderBS<T>).flatMap(mapper) }
	}

	fun transform(transform: Function<in ReaderBS<T>, out ReaderBS<out T>>): Builder3<T> {
		val currentTransform = b.transform
		b.transform = Function { reader -> transform.apply(currentTransform.apply(reader) as ReaderBS<T>) }
		return this
	}

	fun transformStream(transform: Function<in Stream<T>, out Stream<out T>>): Builder3<T> {
		val currentTransform = b.transform
		b.transform = Function<ReaderBS<T>, ReaderBS<out T>> { reader -> (currentTransform.apply(reader) as ReaderBS<T>).transform(transform) }
		return this
	}

	fun output(output: File): Builder4<T> {
		b.output = output
		return Builder4(b)
	}

	fun outputAsStream(): Builder5<T> = Builder5(b)
}