package com.jonolds.bigsorter.v2

import com.jonolds.bigsorter.FileWithElemCount
import java.io.File
import java.nio.file.Files


class DatMapContext(
    maxFilesPerMerge: Int = 100,
    maxItemsPerPart: Int = 100000,
    bufferSize: Int = 4_000_0,
    debug: Boolean = false,
    deleteTempFiles: Boolean = true,
    tempDirectory: File = File(System.getProperty("java.io.tmpdir") + "/big-sorter")
) {

    var defaultConfig: DatMapConfig = DatMapConfig(
        maxFilesPerMerge = maxFilesPerMerge,
        maxItemsPerPart = maxItemsPerPart,
        bufferSize = bufferSize,
        debug = debug,
        deleteTempFiles = deleteTempFiles,
        tempDirectory = tempDirectory,
    )



    fun log(msg: String, vararg objects: Any?) { if (defaultConfig.debug) println(String.format(msg, *objects)) }

    fun nextTempFile(
        tempDirectory: File = defaultConfig.tempDirectory,
        prefix2: String = ""
    ): File =
        Files.createTempFile(tempDirectory.toPath(), "big-sorter$prefix2", "").toFile()
            .also { if (defaultConfig.deleteTempFiles) it.deleteOnExit() }

    fun nextTempFileWithElemCount(
        tempDirectory: File = defaultConfig.tempDirectory,
        prefix2: String = "",
        elemCount: Int = 0
    ): FileWithElemCount =
        FileWithElemCount(Files.createTempFile(tempDirectory.toPath(), "big-sorter$prefix2", ""), elemCount)
            .also { if (defaultConfig.deleteTempFiles) it.deleteOnExit() }

}

data class DatMapConfig(
    val maxFilesPerMerge: Int,
    val maxItemsPerPart: Int,
    var bufferSize: Int,
    val debug: Boolean,
    val deleteTempFiles: Boolean,
    var tempDirectory: File = File(System.getProperty("java.io.tmpdir") + "/big-sorter")
)

