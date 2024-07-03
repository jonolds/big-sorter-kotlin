package com.jonolds.bigsorter

import java.io.File
import java.nio.file.Path
import kotlin.io.path.pathString


class InputTransform<T, out R>(
	val readerFactory: FileReaderFactory<T>,
	val mapper: (T) -> R
) {
	fun transformed(): FileReaderFactory<out R> = readerFactory.mapper(mapper)
}


open class FileWithElemCount(
	path: String,
	var elemCount: Int = 0
): File(path) {

	constructor(path: Path, elemCount: Int = 0): this(path.pathString, elemCount)
}


class FileState<T>(
	path: String,
	var reader: ReaderBS<T>,
	var currentValue: T?,
): FileWithElemCount(path) {

	constructor(file: File, reader: ReaderBS<T>): this(file.path, reader, reader.read())

}