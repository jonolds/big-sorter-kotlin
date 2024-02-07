package com.jonolds.bigsorter

import java.io.*

fun interface OutputStreamWriterFactory<T> {
	fun createWriter(out: OutputStream): WriterBS<T>
}

fun <T> OutputStreamWriterFactory<T>.createWriterFile(file: File): WriterBS<T> =
	createWriter(BufferedOutputStream(FileOutputStream(file)))