package com.jonolds.bigsorter

import java.io.*
import java.nio.file.Files
import java.util.function.Function

object Util {



	fun close(c: Closeable) = try {
		c.close()
	} catch (e: IOException) {
		throw UncheckedIOException(e)
	}

	fun nextTempFile(tempDirectory: File): File =
		Files.createTempFile(tempDirectory.toPath(), "big-sorter", "").toFile()


	fun File.toInStream(bufferSize: Int): InputStream = BufferedInputStream(FileInputStream(this), bufferSize)


	fun toRuntimeException(e: Throwable?): RuntimeException = when (e) {
		is IOException -> UncheckedIOException(e as IOException?)
		is RuntimeException -> e
		else -> RuntimeException(e)
	}


	fun <S, T> convert(
		input: File,
		readerFactory: InputStreamReaderFactory<S>,
		out: File,
		writerFactory: OutputStreamWriterFactory<T>,
		mapper: Function<in S?, out T?>
	) = try {
		readerFactory.createReaderFile(input).use { r ->
			writerFactory.createWriterFile(out).use { w ->
				var s: S?
				while ((r.read().also { s = it }) != null)
					w.write(mapper.apply(s))
			}
		}
	} catch (e: IOException) {
		throw UncheckedIOException(e)
	}

}