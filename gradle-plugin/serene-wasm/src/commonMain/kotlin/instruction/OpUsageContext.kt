package net.derfruhling.serene.wasm.instruction

import net.derfruhling.serene.wasm.module.HeapType
import net.derfruhling.serene.wasm.module.NumericType
import net.derfruhling.serene.wasm.module.RecursiveType
import net.derfruhling.serene.wasm.module.RefType
import net.derfruhling.serene.wasm.module.TableType
import net.derfruhling.serene.wasm.module.Type
import net.derfruhling.serene.wasm.module.ValueType
import net.derfruhling.serene.wasm.instruction.VectorValue as VectorData

interface OpUsageContext {
    interface ResolveContext {
        fun resolve(value: Value): Any = value
    }

    fun take(): Input
    fun args(): Array<Arg>
    fun output(fn: MutableOutput.() -> Unit): Output
    fun assert(condition: Boolean, message: ResolveContext.() -> String)
    fun assert(condition: Condition, message: ResolveContext.() -> String)
    fun suggest(condition: () -> Condition, message: ResolveContext.() -> String)

    fun ternery(condition: Condition, ifTrue: Value, ifFalse: Value): Value = Value.Null

    fun getTag(index: UInt): TagValue
    fun getFunction(index: UInt): Function
    fun getType(index: UInt): RecursiveType
    fun getElement(index: UInt): Element
    fun getTable(index: UInt): Table
    fun getData(index: UInt): Data
    fun getMemory(index: UInt): Memory
    fun getLocal(index: UInt): Variable
    fun getGlobal(index: UInt): Variable

    fun call(function: Function)
    fun returns()
    fun branchTo(label: Label)
    fun throws(tag: TagValue)
    fun legacyRethrows(label: Label, value: TagValue) {}
    fun startBlock(isLoop: Boolean)
    fun ifBlock(condition: Condition)
    fun endBlock()
    fun elseBlock()
    fun legacyCatchBlock(tagIndex: UInt) { endBlock(); startBlock(isLoop = false) }
    fun legacyTryDelegate(label: Label) { endBlock() }
    fun legacyCatchAllBlock() { endBlock(); startBlock(isLoop = false) }
    fun ifThen(condition: Condition, fn: OpUsageContext.() -> Unit): Conditional
    fun forEach(range: NumericRange, fn: (NumericValue) -> Unit)
    fun opaqueOperator(vararg values: Value): Value
    fun opaqueOperator(values: Collection<Value>): Value = opaqueOperator(*values.toTypedArray())

    fun tailCall(function: Function) {
        call(function)
        returns()
    }

    interface Conditional {
        fun otherwiseIf(condition: Condition, fn: OpUsageContext.() -> Unit): Conditional
        infix fun otherwise(fn: OpUsageContext.() -> Unit)
        infix fun otherwiseDefault(fn: OpUsageContext.() -> Unit)
    }

    interface Value {
        data object Null : Value {
            override val isNull: Condition
                get() = True
            override val isTrue: Condition
                get() = False

            fun ofType(type: HeapType): TypedValue {
                return object : TypedValue, Value by Null {
                    override val type: ValueType = RefType.Nullable(type)
                }
            }
        }

        data object Unknown : Value {
            override val isKnown: Boolean
                get() = false
            override val isNull: Condition
                get() = False
            override val isTrue: Condition
                get() = False
        }

        data object True : Condition, TypedValue {
            override val type: ValueType
                get() = NumericType.I32
            override val isNull: Condition
                get() = False
            override val isTrue: Condition
                get() = True

            override fun not(): Condition {
                return False
            }
        }

        data object False : Condition, TypedValue {
            override val type: ValueType
                get() = NumericType.I32
            override val isNull: Condition
                get() = False
            override val isTrue: Condition
                get() = False

            override fun not(): Condition {
                return True
            }
        }

        val isKnown: Boolean get() = true
        val isNull: Condition
        val isTrue: Condition

        fun asFunctionFromRef(): Function {
            throw UnsupportedOperationException()
        }
    }

    interface TypedValue : Value {
        val type: ValueType?

        fun withType(toType: ValueType): TypedValue {
            if(type == toType) return this
            return object : TypedValue by this {
                override val type: ValueType = toType
            }
        }

        fun eraseType(): Value {
            return object : Value by this {}
        }

        fun eraseTypeAny(): Value = withType(RefType.Nullable(HeapType.Any))

        fun wrapExtern(): TypedValue = withType(RefType.Nullable(HeapType.Extern))
        fun unwrapExtern(): Value = eraseTypeAny()

        fun unwrapI31(signed: Boolean): TypedValue = withType(NumericType.I32)
    }

    interface ManagedValue : Value {
        infix fun isEqualTo(other: ManagedValue): Condition
    }

    interface Input : TypedValue, ManagedValue {
        fun asCondition(): Condition
        fun asNumericValue(): NumericValue
        fun asVectorValue(): VectorValue
        fun asTagRef(): TagValue
        fun canDowncast(toType: RefType): Condition
        fun asArray(): ArrayType
    }

    interface TagValue : TypedValue

    interface Function {
        fun asRef(): TypedValue = object : TypedValue {
            override val type: ValueType
                get() = RefType.NonNull(HeapType.Func)
            override val isNull: Condition
                get() = Value.False
            override val isTrue: Condition
                get() = Value.False
        }
    }

    interface Label

    interface Arg {
        fun asType(): Type
        fun asUInt(): UInt
        fun asInt(): Int
        fun asLong(): Long
        fun asFloat(): Float
        fun asDouble(): Double
        fun asIndex(): UInt = asUInt()
        fun asByte(): Byte
        fun asVector(): VectorData
        fun asInput(): Input
        fun asMemArg(): MemArg
        fun asLabel(): Label
    }

    val zeroVector: VectorValue

    val Int.constant: NumericValue
    val Long.constant: NumericValue
    val Float.constant: NumericValue
    val Double.constant: NumericValue
    val VectorData.constant: VectorValue
    val Boolean.constant: Condition
        get() = when(this) {
            true -> Value.True
            false -> Value.False
        }

    data class NumericRange(val from: NumericValue, val to: NumericValue, val step: Int = 1) {
        infix fun step(step: Int) = copy(step = step)
    }

    interface VectorValue : TypedValue, ManagedValue

    interface NumericValue : TypedValue, ManagedValue {
        infix fun inRange(value: NumericRange): Condition
        infix fun isEqualTo(value: NumericValue): Condition

        infix fun comparedTo(other: NumericValue): NumericValue
        infix fun isGreaterThan(other: NumericValue): Condition
        infix fun isGreaterEqualTo(other: NumericValue): Condition
        infix fun isLessThan(other: NumericValue): Condition
        infix fun isLessEqualTo(other: NumericValue): Condition

        operator fun plus(other: NumericValue): NumericValue
        operator fun minus(other: NumericValue): NumericValue
        operator fun times(other: NumericValue): NumericValue
        operator fun div(other: NumericValue): NumericValue
        operator fun rem(other: NumericValue): NumericValue

        val preceding: NumericValue
        val succeeding: NumericValue

        operator fun rangeTo(other: NumericValue): NumericRange =
            NumericRange(from = this, to = other)

        operator fun rangeUntil(other: NumericValue): NumericRange =
            NumericRange(from = this, to = other.preceding)

        fun copy(): NumericValue
        fun wrapI31(): TypedValue = withType(RefType.NonNull(HeapType.I31))
    }

    interface Condition : Value {
        operator fun not(): Condition
    }

    interface MutableOutput {
        var type: ValueType

        fun from(value: Value) {
            if(value is TypedValue) {
                value.type?.let { type = it }
            }
        }
    }

    interface Output {
        val type: ValueType
    }

    interface Element {
        val type: RefType
        val size: NumericValue

        fun drop()

        operator fun get(index: NumericValue): Value = Value.Unknown
    }

    interface Data {
        val size: NumericValue
        val sizeBytes: NumericValue

        fun drop()
        fun read(address: NumericValue, size: NumericValue): Value = Value.Unknown
    }

    interface Memory {
        val size: NumericValue
        val sizeBytes: NumericValue

        fun canGrow(by: NumericValue): Condition
        fun read(address: NumericValue, size: NumericValue): Value = Value.Unknown
        fun write(address: NumericValue, size: NumericValue, value: Value) {}
        fun write(address: NumericValue, size: NumericValue, value: Data) {}
    }

    interface Table {
        val type: TableType
        val size: NumericValue

        fun canGrow(by: NumericValue): Condition

        operator fun get(index: NumericValue): Value
        operator fun set(index: NumericValue, value: Value)
    }

    interface Variable {
        val type: ValueType
        var value: Value
    }

    interface ArrayType : TypedValue {
        val size: NumericValue

        operator fun get(index: NumericValue): Value
        operator fun set(index: NumericValue, value: Value)
    }
}
