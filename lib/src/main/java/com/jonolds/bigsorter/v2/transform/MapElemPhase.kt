package com.jonolds.bigsorter.v2.transform

import com.jonolds.bigsorter.v2.phase.Sender



class MapElemPhase<P, R>(
    val mapper: (P) -> R,
): ElemPhase<P, R>() {



}




