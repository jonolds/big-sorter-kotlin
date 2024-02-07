package com.jonolds.bigsorter


class Source(
	val type: SourceType,
	val source: Any
)


enum class SourceType {
	SUPPLIER_INPUT_STREAM, SUPPLIER_READER
}