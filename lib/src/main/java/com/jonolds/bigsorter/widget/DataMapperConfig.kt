package com.jonolds.bigsorter.widget

data class DataMapperConfig(
	val maxFilesPerMerge: Int = 100,
	val maxItemsPerPart: Int = 100000,
	var bufferSize: Int = 8192,
	val log: Boolean = false,
)
