package com.axehen.hengine

import kotlin.math.PI
import kotlin.math.sqrt


class Vector(private var list: MutableList<Double>): MutableList<Double> {

    constructor(vararg x: Double): this(x.toMutableList())


    fun normalize(): Vector = this/this.length()
    fun length(): Double = sqrt(sumOf { x -> x*x })

    /**
     * Rescales any vectors whose length is not within the specified ranged so that its length is within the range
     */
    fun coerceLengthWithin(min: Double, max: Double): Vector {
        if(max < 0.0) throw IllegalArgumentException("A vector cannot be negative and thus cannot be coerced within the range [$min, $max] which includes negative values.")
        if(max < min) throw IllegalArgumentException("Minimum value ($min) must be greater than maximum ($max)")
        return if(length() < min) {
            if(length() <= 0.0) throw ArithmeticException("A vector of length ${length()} was attempted to be coerced within [$min, $max]. Scaling of such a vector is undefined.")
            this*(min/length())
        } else if (length() > max) {
            this*(length()/max)
        } else {
            this
        }
    }


    operator fun div(factor: Double)    = Vector( map{ it / factor } )
    operator fun times(factor: Double)  = Vector( map{ it * factor })
    operator fun plus(other: Vector): Vector {
        if(this.count() != other.count()) throw IllegalArgumentException("Attempted to add vectors of differing sizes: $size != ${other.size}")
        return Vector(zip(other) { first, second -> first + second })
    }
    operator fun minus(other: Vector): Vector {
        if (count() != other.count()) throw IllegalArgumentException("Attempted to subtract vectors of differing sizes: $size != ${other.size}")
        return Vector(zip(other) { first, second -> first - second })
    }
    operator fun unaryMinus(): Vector {
        return Vector(map{ -it })
    }
    operator fun plusAssign(other: Vector) {
        if(size != other.size) throw IllegalArgumentException("Attempted to add vectors of differing sizes: $size != ${other.size}")
        forEachIndexed { index, _ -> this[index] += other[index] }
    }
    operator fun minusAssign(other: Vector) {
        if(size != other.size) throw IllegalArgumentException("Attempted to subtract vectors of differing sizes: $size != ${other.size}")
        forEachIndexed { index, _ -> this[index] -= other[index] }
    }
    infix fun dot(other: Vector): Double {
        if(size != other.size) throw IllegalArgumentException("Attempted to dot product vectors of differing sizes: $size != ${other.size}")
        return (zip(other) { a, b -> a * b }).sum()
    }

    infix fun deflect(prohibitor: Vector): Vector {
        return if((this dot prohibitor) < 0)  this - prohibitor * (this dot (prohibitor.normalize()))
        else this
    }

    override fun toString(): String = list.toString()

    // Vector indexing helpers
    var x: Double
        get()       = list[0]
        set(value)  { list[0] = value }
    var y: Double
        get()       = list[1]
        set(value)  { list[1] = value }
    var z: Double
        get()       = list[2]
        set(value)  { list[2] = value }
    var w: Double
        get()       = list[3]
        set(value)  { list[3] = value }
    var r: Double
        get()       = list[0]
        set(value)  { list[0] = value }
    var g: Double
        get()       = list[1]
        set(value)  { list[1] = value }
    var b: Double
        get()       = list[2]
        set(value)  { list[2] = value }
    var a: Double
        get()       = list[3]
        set(value)  { list[3] = value }

    // Overridden list functions
    override val size: Int
        get() = list.size
    override fun contains(element: Double): Boolean = list.contains(element)
    override fun containsAll(elements: Collection<Double>): Boolean = list.containsAll(elements)
    override fun get(index: Int): Double = list.get(index)
    override fun indexOf(element: Double): Int = list.indexOf(element)
    override fun isEmpty(): Boolean = list.isEmpty()
    override fun iterator(): MutableIterator<Double> = list.iterator()
    override fun lastIndexOf(element: Double): Int = list.lastIndexOf(element)
    override fun listIterator(): MutableListIterator<Double> = list.listIterator()
    override fun listIterator(index: Int): MutableListIterator<Double> = list.listIterator(index)
    override fun subList(fromIndex: Int, toIndex: Int): MutableList<Double> = subList(fromIndex, toIndex)
    override fun add(element: Double): Boolean = list.add(element)
    override fun add(index: Int, element: Double) = list.add(index, element)
    override fun addAll(index: Int, elements: Collection<Double>): Boolean = list.addAll(index, elements)
    override fun addAll(elements: Collection<Double>): Boolean = list.addAll(elements)
    override fun clear() = list.clear()
    override fun remove(element: Double): Boolean = list.remove(element)
    override fun removeAll(elements: Collection<Double>): Boolean = list.removeAll(elements)
    override fun removeAt(index: Int): Double = list.removeAt(index)
    override fun retainAll(elements: Collection<Double>): Boolean = list.retainAll(elements)
    override fun set(index: Int, element: Double): Double = list.set(index, element)

    companion object {
        fun degreesToRadians(angle: Double) = angle * PI/180.0
        fun radiansToDegrees(angle: Double) = angle * 180.0/PI

        operator fun invoke(list: List<Double>) = Vector(list.toMutableList())
    }
}

