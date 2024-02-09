package com.jonolds.bigsorter

import java.io.*

fun interface InputStreamReaderFactory<T> {

	fun createReader(inStr: InputStream): ReaderBS<T>


	fun createReaderFile(file: File): ReaderBS<T> =
		createReader(BufferedInputStream(FileInputStream(file)))

}


