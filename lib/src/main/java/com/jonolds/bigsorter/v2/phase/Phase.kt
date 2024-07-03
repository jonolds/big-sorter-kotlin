package com.jonolds.bigsorter.v2.phase

import com.jonolds.bigsorter.v2.DatMapConfig
import com.jonolds.bigsorter.v2.DatMapContext


interface Phase {

    val context: DatMapContext

    val defaultConfig: DatMapConfig get() = context.defaultConfig


    var tag: String?




}

fun <T: Phase> T.tag(value: String): T {
    tag = value
    return this
}

