package com.jonolds.bigsorter.serializers

enum class LineDelimiter(private val delimiter: String) {
	LINE_FEED("\n"),  //

	CARRIAGE_RETURN_LINE_FEED("\r\n");

	fun value(): String = delimiter
}
