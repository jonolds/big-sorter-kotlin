package com.jonolds.bigsorter.internal





class FixedArray (
	val recLen: Int,
	val maxSize: Int = 1000
) {

	val entries: Array<Entry> = Array(maxSize) { Entry(it*recLen) }

	val arr: ByteArray = ByteArray(maxSize*recLen)

	var size: Int = 0


	fun add(srcArr: ByteArray, offset: Int) {
		System.arraycopy(srcArr, offset, arr, recLen*size++, recLen)
	}


	fun isEmpty(): Boolean = size == 0


	fun clear() {
		size = 0
	}


	inner class Entry(
		val start: Int,
		val end: Int = start+recLen,

	)
}