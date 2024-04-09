package com.jonolds.bigsorter.internal




class Array2<T> constructor(
	val arr: Array<T>,
) {


	var size: Int = 0

	fun add(t: T) {
		arr[size++] = t
	}


	fun isEmpty(): Boolean = size == 0


	fun clear() {
		size = 0
	}
}