package com.jonolds.bigsorter.v2.tests

class OpsRunner<R>(
    inline val opsFunc: OpsRunner<R>.(R) -> Unit,
) {

    var x: Any? = null


    fun ops2(value: R) {
        opsFunc(value)
    }

}


