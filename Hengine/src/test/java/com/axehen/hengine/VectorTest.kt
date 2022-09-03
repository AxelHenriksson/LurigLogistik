package com.axehen.hengine

import org.junit.jupiter.api.Assertions.*
import java.lang.ArithmeticException
import java.lang.IllegalArgumentException
import kotlin.math.sqrt
import kotlin.random.Random

internal class VectorTest {

    @org.junit.jupiter.api.Test
    fun normalize() {
        assert(Vector(1.0, 0.0, 0.0).normalize() == Vector(1.0, 0.0, 0.0))
        assert(Vector(0.31, 5.32, 1123.3).normalize().length() == 1.0)
        assert(Vector(232.135).normalize().length() == 1.0)
    }

    @org.junit.jupiter.api.Test
    fun equals() {

        assert(Vector() == Vector())
        assert(Vector(0.0, 0.1, 1.0, 50.32, 123.22, 0.93) == Vector(0.0, 0.1, 1.0, 50.32, 123.22, 0.93))
        assert(Vector() != Vector(2.3))
    }

    @org.junit.jupiter.api.Test
    fun length() {
        assert(Vector().length() == 0.0)
        assert(Vector(0.0, 0.0).length() == 0.0)
        assert(Vector(1.0, 1.0, 1.0).length() == sqrt(3.0))
        assert(Vector(1.0/sqrt(4.0), 1.0/sqrt(4.0), 1.0/sqrt(4.0), 1.0/sqrt(4.0)).length() == 1.0)
        assert(Vector(-2.0).length() == 2.0)
        assert(Vector(0.0, 0.0, -209.5).length() == -209.5)
    }

    @org.junit.jupiter.api.Test
    fun coerceLengthWithin() {
        assertThrows(ArithmeticException::class.java) { Vector().coerceLengthWithin(1.0, 20.0) }
        assertThrows(ArithmeticException::class.java) { Vector(0.0).coerceLengthWithin(0.0, 230.01) }
        assertThrows(ArithmeticException::class.java) { Vector(0.0, 0.0, 0.0).coerceLengthWithin(31.0, 120.0) }
        assertThrows(IllegalArgumentException::class.java) { Vector(0.3).coerceLengthWithin(-991.2, 20.0) }
        assertThrows(IllegalArgumentException::class.java) { Vector(0.9, 12.4).coerceLengthWithin(-0.2, 20.0) }
        assertThrows(IllegalArgumentException::class.java) { Vector(12.3, -162.3).coerceLengthWithin(-32.3, -230.01) }
        assertThrows(IllegalArgumentException::class.java) { Vector(0.0, 0.0, 0.0).coerceLengthWithin(121.01, 120.0) }
    }

    @org.junit.jupiter.api.Test
    fun div() {
    }

    @org.junit.jupiter.api.Test
    fun times() {
    }

    @org.junit.jupiter.api.Test
    fun plus() {
    }

    @org.junit.jupiter.api.Test
    fun minus() {
    }

    @org.junit.jupiter.api.Test
    operator fun unaryMinus() {
    }

    @org.junit.jupiter.api.Test
    fun plusAssign() {
    }

    @org.junit.jupiter.api.Test
    fun minusAssign() {
    }

    @org.junit.jupiter.api.Test
    fun dot() {
    }

    @org.junit.jupiter.api.Test
    fun deflect() {
    }

    @org.junit.jupiter.api.Test
    fun getX() {
    }

    @org.junit.jupiter.api.Test
    fun setX() {
    }

    @org.junit.jupiter.api.Test
    fun getY() {
    }

    @org.junit.jupiter.api.Test
    fun setY() {
    }

    @org.junit.jupiter.api.Test
    fun getZ() {
    }

    @org.junit.jupiter.api.Test
    fun setZ() {
    }

    @org.junit.jupiter.api.Test
    fun getW() {
    }

    @org.junit.jupiter.api.Test
    fun setW() {
    }

    @org.junit.jupiter.api.Test
    fun getR() {
    }

    @org.junit.jupiter.api.Test
    fun setR() {
    }

    @org.junit.jupiter.api.Test
    fun getG() {
    }

    @org.junit.jupiter.api.Test
    fun setG() {
    }

    @org.junit.jupiter.api.Test
    fun getB() {
    }

    @org.junit.jupiter.api.Test
    fun setB() {
    }

    @org.junit.jupiter.api.Test
    fun getA() {
    }

    @org.junit.jupiter.api.Test
    fun setA() {
    }

    @org.junit.jupiter.api.Test
    fun getSize() {
    }

    @org.junit.jupiter.api.Test
    fun get() {
    }

    @org.junit.jupiter.api.Test
    fun set() {
    }
}