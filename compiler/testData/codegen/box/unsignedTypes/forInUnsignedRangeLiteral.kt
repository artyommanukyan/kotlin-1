// IGNORE_BACKEND: JVM_IR
// WITH_RUNTIME

const val MaxUI = UInt.MAX_VALUE
const val MinUI = UInt.MIN_VALUE

const val MaxUL = ULong.MAX_VALUE
const val MinUL = ULong.MIN_VALUE

val M = MaxUI.toULong()

fun testSimpleUIntLoop() {
    var s = 0
    for (i in 1u .. 6u) {
        s = s*10 + i.toInt()
    }
    if (s != 123456) throw AssertionError("$s")
}

fun testSimpleULongLoop() {
    var s = 0
    for (i in 1UL .. 6UL) {
        s = s*10 + i.toInt()
    }
    if (s != 123456) throw AssertionError("$s")
}

fun testULongLoop() {
    var s = 0
    for (i in M+1UL..M+6UL) {
        s = s*10 + (i-M).toInt()
    }
    if (s != 123456) throw AssertionError("$s")
}

fun testMaxUItoMinUI() {
    val xs = ArrayList<UInt>()
    for (i in MaxUI..MinUI) {
        xs.add(i)
        if (xs.size > 23) break
    }
    if (xs.size > 0) {
        throw AssertionError("Wrong elements for MaxUI..MinUI: $xs")
    }
}

fun testMaxULtoMinUL() {
    val xs = ArrayList<ULong>()
    for (i in MaxUL..MinUL) {
        xs.add(i)
        if (xs.size > 23) break
    }
    if (xs.size > 0) {
        throw AssertionError("Wrong elements for MaxUI..MinUI: $xs")
    }
}

fun box(): String {
    testSimpleUIntLoop()
    testSimpleULongLoop()
    testULongLoop()
    testMaxUItoMinUI()
    testMaxULtoMinUL()

    return "OK"
}