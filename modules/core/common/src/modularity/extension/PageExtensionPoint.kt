package net.derfruhling.serenity.modularity.extension

import net.derfruhling.serenity.Stack

class PageExtensionPoint<T: Function<Unit>>(@PublishedApi internal val sequence: ReferencePointSequence) {
    @PublishedApi
    internal var beforeAllOrNull: MutableList<T>? = null
        private set

    @PublishedApi
    internal var beforeEachOrNull: MutableMap<ReferencePoint, MutableList<T>>? = null
        private set

    @PublishedApi
    internal var eachOrNull: MutableMap<ReferencePoint, MutableList<T>>? = null
        private set

    @PublishedApi
    internal var afterEachOrNull: MutableMap<ReferencePoint, MutableList<T>>? = null
        private set

    @PublishedApi
    internal var afterAllOrNull: MutableList<T>? = null
        private set

    private val beforeAll by lazy { mutableListOf<T>().also { beforeAllOrNull = it } }
    private val beforeEach by lazy { mutableMapOf<ReferencePoint, MutableList<T>>().also { beforeEachOrNull = it } }
    private val each by lazy { mutableMapOf<ReferencePoint, MutableList<T>>().also { eachOrNull = it } }
    private val afterEach by lazy { mutableMapOf<ReferencePoint, MutableList<T>>().also { afterEachOrNull = it } }
    private val afterAll by lazy { mutableListOf<T>().also { afterAllOrNull = it } }

    @PublishedApi
    internal enum class State {
        BEFORE,
        RUNNING,
        AFTER
    }

    @PublishedApi
    internal var state = State.BEFORE

    @PublishedApi
    internal var lastPoint: ReferencePoint? = null

    @PublishedApi
    internal lateinit var pointIterator: Iterator<ReferencePoint>

    @PublishedApi
    internal val pointStack: Stack<ReferencePoint> = Stack()

    private operator fun ReferencePoint.compareTo(other: ReferencePoint): Int {
        return sequence.comparePoints(this, other)
    }

    operator fun invoke(fn: T) {
        each.getOrPut(sequence.default, ::mutableListOf).add(fn)
    }

    class Fn<T>(val referencePoint: ReferencePoint, val fn: T)

    fun at(referencePoint: ReferencePoint = sequence.default, fn: T) {
        each.getOrPut(referencePoint, ::mutableListOf).add(fn)
    }

    fun before(referencePoint: ReferencePoint = sequence.default, fn: T) {
        beforeEach.getOrPut(referencePoint, ::mutableListOf).add(fn)
    }

    fun after(referencePoint: ReferencePoint = sequence.default, fn: T) {
        afterEach.getOrPut(referencePoint, ::mutableListOf).add(fn)
    }

    infix fun before(fn: T) {
        beforeAll.add(fn)
    }

    infix fun after(fn: T) {
        afterAll.add(fn)
    }

    infix fun at(fn: Fn<T>) = at(fn.referencePoint, fn.fn)
    infix fun before(fn: Fn<T>) = before(fn.referencePoint, fn.fn)
    infix fun after(fn: Fn<T>) = after(fn.referencePoint, fn.fn)

    @PublishedApi
    internal fun nextPoint(): Pair<ReferencePoint?, Boolean> {
        return when {
            !pointStack.isEmpty -> pointStack.pop() to true
            pointIterator.hasNext() -> pointIterator.next() to false
            else -> null to false
        }
    }

    @PublishedApi
    internal inline fun flyState(fn: (T) -> Unit): Boolean {
        return when(state) {
            State.BEFORE -> {
                beforeAllOrNull?.forEach { fn(it) }
                pointIterator = sequence.points.iterator()
                state = State.RUNNING
                false
            }

            State.RUNNING -> false
            State.AFTER -> true
        }
    }

    @PublishedApi
    internal inline fun execute(next: ReferencePoint, isStack: Boolean, fn: (T) -> Unit, check: () -> Unit = {}) {
        lastPoint?.let { last ->
            afterEachOrNull?.get(last)?.forEach { fn(it) }
            lastPoint = null
        }

        if(!isStack) {
            beforeEachOrNull?.get(next)?.forEach { fn(it) }
        }

        check()

        eachOrNull?.get(next)?.forEach { fn(it) }
    }

    inline fun execute(fn: (T) -> Unit) {
        if(flyState(fn)) return

        do {
            val (next, isStack) = nextPoint()

            if(next != null) {
                execute(next, isStack, fn)
            }
        } while(next != null)

        finish(fn)
    }

    inline fun executeUntil(point: ReferencePoint, fn: (T) -> Unit) {
        if(flyState(fn)) return

        do {
            val (next, isStack) = nextPoint()

            if(next != null) {
                execute(next, isStack, fn) {
                    if(next == point) {
                        pointStack.push(next)
                        return
                    }
                }
            }
        } while(next != null)
    }

    inline fun finish(fn: (T) -> Unit) {
        lastPoint?.let { last ->
            afterEachOrNull?.get(last)?.forEach { fn(it) }
        }

        afterAllOrNull?.forEach { fn(it) }
        state = State.AFTER
    }

    fun reset() {
        state = State.BEFORE
    }
}
