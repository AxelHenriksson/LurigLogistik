package com.axehen.hengine

import java.util.concurrent.atomic.AtomicReference
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/** A class containing and handling the game environment i.e. ground mesh and all world objects */
class Environment : Drawable {

    /** A world objects containing a mesh and collidable that handles its collisions */
    class EnvironmentObject(position: Vector, Vector: Vector, meshes: List<Mesh>, private val collidable: Collidable): Mesh.StaticMesh(
        position, Vector, meshes) {
        fun getCollisionVector(charPos: Vector, charRadius: Float): Vector? {
            return collidable.getCollisionVector(charPos, charRadius, Vector(position.x, position.y))
        }
    }

    interface Collidable {
        fun getCollisionVector(charPos: Vector, charRadius: Float, objPos: Vector): Vector?
    }

    class CircleCollidable(private val radius: Float): Collidable {
        override fun getCollisionVector(charPos: Vector, charRadius: Float, objPos: Vector): Vector? {
            return if((charPos - objPos).length() <= radius + charRadius) {
                (charPos - objPos).normalize()
            } else null
        }
    }
    
    class SquareCollidable(private val radius: Float, private val angle: Float): Collidable {
        override fun getCollisionVector(charPos: Vector, charRadius: Float, objPos: Vector): Vector? {
            val u = Vector(cos(angle*PI/180f), sin(angle*PI/180f))
            val v = Vector(cos((angle+90f)*PI/180f), sin((angle+90f)*PI/180f))

            val delta = (charPos - objPos).let { Vector(it.x, it.y) }
            val uProj = delta dot u
            val vProj = delta dot v
            return if(abs(uProj) > abs(vProj)) {
                if (abs(uProj) < (radius + charRadius)) {
                    if (uProj < 0)
                        -u
                    else
                        u
                } else null
            } else {
                if (abs(vProj) < (radius + charRadius)) {
                    if (vProj < 0)
                        -v
                    else
                        v
                } else null
            }
        }
    }

    val objects = ArrayList<EnvironmentObject>()
    var groundMesh: Mesh.StaticMesh? = null

    /**
     * Checks collisions between the character and every object in the environment
     * @return The exit vector of all occurring collisions
     */
    fun getCollisionVectors(charPos: Vector, charRadius: Float): ArrayList<Vector> {
        val vectorList = ArrayList<Vector>()
        for (obj in objects) {
            obj.getCollisionVector(charPos, charRadius)?.let { vectorList.add(it) }
        }
        return vectorList
    }

    override fun load() {
        groundMesh?.load()
        objects.forEach { it.load() }
    }

    override fun draw() {
        groundMesh?.draw()
        objects.forEach{ it.draw() }
    }
}