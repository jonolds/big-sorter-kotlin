package com.jonolds.bigsorter.v2

import com.jonolds.bigsorter.v2.phase.MapContextPhase
import com.jonolds.bigsorter.v2.phase.MapPhase
import com.jonolds.bigsorter.v2.phase.OpContainer
import com.jonolds.bigsorter.v2.phase.Sender
import com.jonolds.bigsorter.v2.phase.mapContextPhase
import com.jonolds.bigsorter.v2.phase.mapPhase
import com.jonolds.bigsorter.v2.phase.opContainer

/* Map */

inline fun <reified A, reified B> Sender<A>.map10(
    crossinline mapper: (A) -> B
): OpContainer<A, B> = opContainer(null, mapPhase(this, mapper))
    .also { child = it }


inline fun <reified A, reified B> Sender<A>.mapWithContext10(
    crossinline mapperFactory: () -> ((A) -> B)
): OpContainer<A, B> =
    opContainer(null, mapContextPhase(this) { mapperFactory() })
        .also{ child = it }


inline fun <reified A, B, reified C> MapPhase<A, B>.map10(
    crossinline mapper: (B) -> C
): MapPhase<A, C> =
    mapPhase(parent) { mapper(this.mapper(it)) }
        .also { parent!!.child = it }


inline fun <reified A, B, reified C> MapPhase<A, B>.map20(
    crossinline mapper: (B) -> C
): MapPhase<A, C> =
    mapPhase(parent) { mapper(this.mapper(it)) }
        .also { parent!!.child = it }