package com.jonolds.bigsorter

import java.io.*
import java.nio.channels.FileChannel
import java.nio.file.Files

object Util {



	fun close(c: Closeable) = try {
		c.close()
	} catch (e: IOException) {
		throw UncheckedIOException(e)
	}

	fun nextTempFile(tempDirectory: File, prefix2: String = ""): File =
		Files.createTempFile(tempDirectory.toPath(), "big-sorter$prefix2", "").toFile()


	fun nextTempFileWithElemCount(tempDirectory: File, prefix2: String = "", elemCount: Int = 0): FileWithElemCount =
		FileWithElemCount(Files.createTempFile(tempDirectory.toPath(), "big-sorter$prefix2", ""), elemCount)


	fun File.createOrReplace(): File {
		if (exists())
			delete()
		createNewFile()
		return this
	}


	fun FileWithElemCount.createOrReplaceWithCount(): FileWithElemCount {
		if (exists())
			delete()
		createNewFile()
		return this
	}


	fun File.toInStream(bufferSize: Int): InputStream = BufferedInputStream(FileInputStream(this), bufferSize)
//		.also { throwCatchStreamException("InStream") }

	fun File.toOutStream(bufferSize: Int): OutputStream = BufferedOutputStream(FileOutputStream(this), bufferSize)
//		.also { throwCatchStreamException("OutStream") }




	fun File.inChannel(): FileChannel = RandomAccessFile(this, "r").channel
	fun File.outChannel(): FileChannel = RandomAccessFile(this, "rw").channel


	private fun throwCatchStreamException(tag: String) = try {
		throw Exception(tag)
	}catch (e: Exception) {
		e.printStackTrace()
	}


	fun toRuntimeException(e: Throwable?): RuntimeException = when (e) {
		is IOException -> UncheckedIOException(e as IOException?)
		is RuntimeException -> e
		else -> RuntimeException(e)
	}


	fun <S, T> convert(
		input: File,
		readerFactory: ReaderFactory<S>,
		out: File,
		writerFactory: WriterFactory<T>,
		bufferSize: Int = 8192,
		mapper: (S?) -> T?,
	) = try {

		println("CONVERT")
		readerFactory.createFileReader(input, bufferSize).use { r ->
			writerFactory.createFileWriter(out, bufferSize).use { w ->
				var s: S?
				while ((r.read().also { s = it }) != null)
					w.write(mapper(s))
			}
		}
	} catch (e: IOException) {
		throw UncheckedIOException(e)
	}


}