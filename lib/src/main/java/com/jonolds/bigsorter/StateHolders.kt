package com.jonolds.bigsorter

import java.io.File


class Source(
	val type: SourceType,
	val source: Any
)


enum class SourceType {
	SUPPLIER_INPUT_STREAM, SUPPLIER_READER
}


class State<T>(
	val file: File,
	var reader: ReaderBS<T>,
	var value: T?
)