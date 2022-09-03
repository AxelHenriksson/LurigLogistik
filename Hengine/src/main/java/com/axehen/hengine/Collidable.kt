package com.axehen.hengine


interface Collidable {

    abstract fun collide(other: Collidable): Collection<Collision>

    fun collide(others: Collection<Collidable>): Collection<Collision> {
        val result = ArrayList<Collision>()
        for(other in others) {
            result.addAll(this.collide(other))
        }
        return result
    }


    data class Collision(val position: Vector, val force: Vector)

    class CompoundCollidable(position: Vector, private var rotation: Vector, val collidables: Collection<Collidable>): Collidable {
        override fun collide(other: Collidable): Collection<Collision> {
            val result = ArrayList<Vector>()
            collidables.map { it.collide(other) }
            TODO("Not Implemented")
        }
    }

    class BoxCollidable(position: Vector, var rotation: Vector, var dimensions: Vector): Collidable {

        override fun collide(other: Collidable): Collection<Collision> {
            when(other::class) {
                CompoundCollidable::class ->
                    return this.collide(other)
                BoxCollidable::class ->
                    TODO("Not Implemented")

                else -> return listOf()

            }
        }

    }

}