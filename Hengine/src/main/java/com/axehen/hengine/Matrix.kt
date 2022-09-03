package com.axehen.hengine

/**
 * A matrix of m rows and n columns
 * @param m The number of rows in the matrix
 * @param n The number of columns in the matrix
 * @param x The matrix data
 */
data class Matrix(val m: Int, val n: Int, var x: MutableList<Double>) {

    constructor(m: Int, n: Int, vararg x: Double) : this(m, n, x.toMutableList())

    init {
        if(m <= 0) throw IllegalArgumentException("'m' argument of Matrix was less than or equal to zero ($m)")
        if(n <= 0) throw IllegalArgumentException("'n' argument of Matrix was less than or equal to zero ($n)")
        if(x.count() != m*n) throw IllegalArgumentException("The number of elements in x (${x.count()}) does not equal the number of required elements in an mxn matrix ($m*$n=${m*n})")
    }

    operator fun get(index: Int) = x[index]
    operator fun get(i: Int, j: Int) = x[ i/n + j%n]

    operator fun get(i: Int, jRange: IntRange) = jRange.map { j -> this[i,j] }
    operator fun get(iRange: IntRange, j: Int) = iRange.map { i -> this[i,j] }

    operator fun set(index: Int, value: Double) { x[index] = value }
    operator fun set(i: Int, j: Int, value: Double) { x[ i/n + j%n] = value }

    operator fun times(other: Matrix): Matrix {
        if(this.n != other.m) throw IllegalArgumentException("Matrix multiplication of matrices with non-matching size (${this.n} != ${other.m})")

        val result = Matrix(this.m, other.n, MutableList(this.m * other.n) { 0.0 })

        for(i in 0 until this.m)
            for(j in 0 until other.n)
                result[i,j] = this[i,0 until this.n].zip(other[0 until other.m, j]).sumOf { (a,b) -> a*b }

        return result
    }

    /**
     * Casts a vector into a matrix
     * @param column If true the vector is turned into a matrix containing one column containing the vector, if false the matrix contains one row containing the vector
     */
    fun Vector.toMatrix(column: Boolean = false): Matrix = Matrix(if(column) this.size else 1, if(column) 1 else this.size, this.x)

    fun toVector(): Vector {
        if(this.m > 1 && this.n > 1) throw TypeCastException("Cannot turn an array of size ${m}x${n} into a one-dimensional Vector")
        return if(this.m == 1) Vector(this[0, 0 until n])
        else Vector(this[0 until m, 0])
    }




}