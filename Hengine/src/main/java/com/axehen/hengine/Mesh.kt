package com.axehen.hengine

import android.opengl.GLES31.*
import android.opengl.Matrix
import java.nio.*
import java.nio.ByteBuffer.allocateDirect
import kotlin.IllegalArgumentException
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin


open class Mesh(
    vertexCoords: FloatArray,
    normals: FloatArray?,
    texCoords: FloatArray,
    val drawOrder: IntArray,
    val shader: Shader
) {

    constructor(vertexCoords: FloatArray, texCoords: FloatArray, drawOrder: IntArray, shader: Shader): this(vertexCoords, null, texCoords, drawOrder, shader)

    private var vertexBuffer: FloatBuffer =
        // (number of coordinate values * 4 bytes per float)
        allocateDirect(vertexCoords.size * 4).run {
            // use the device hardware's native byte order
            order(ByteOrder.nativeOrder())

            // create a floating point buffer from the ByteBuffer
            asFloatBuffer().apply {
                // add the coordinates to the FloatBuffer
                put(vertexCoords)
                // set the buffer to read the first coordinate
                position(0)
            }
        }



        private var normalBuffer: FloatBuffer? = if (normals != null) {
            // (number of normal values * 4 bytes per float)
            allocateDirect(normals.size * 4).run {

                // use the device hardware's native byte order
                order(ByteOrder.nativeOrder())

                asFloatBuffer().apply {
                    put(normals)
                    position(0)
                }
            }
        } else null

    private val texCoordBuffer: FloatBuffer =
        // (number of coordinate values * 4 bytes per float)
        allocateDirect(texCoords.size * 4).run {
            // use the device hardware's native byte order
            order(ByteOrder.nativeOrder())

            asFloatBuffer().apply {
                put(texCoords)
                position(0)
            }
        }

    private val drawListBuffer: IntBuffer =
        // (# of coordinate values * 4 bytes per int)
        allocateDirect(drawOrder.size * 4).run {
            order(ByteOrder.nativeOrder())
            asIntBuffer().apply {
                put(drawOrder)
                position(0)
            }
        }


    init {
        if (vertexCoords.size % COORDS_PER_VERTEX != 0) throw IllegalArgumentException("Vertex coordinate count is not divisible by coordsPerVertex (${COORDS_PER_VERTEX})")
        if (normals != null && normals.size % COORDS_PER_VERTEX != 0) throw IllegalArgumentException("Vertex normal vector count is not divisible by coordsPerVertex (${COORDS_PER_VERTEX})")
        if (drawOrder.size % COORDS_PER_VERTEX != 0) throw IllegalArgumentException("Draw order count is not divisible by coordsPerVertex (${COORDS_PER_VERTEX})")
    }


    fun load() {
        shader.load()
    }

    private val vertexStride: Int = COORDS_PER_VERTEX * 4 // 4 bytes per vertex
    fun draw(position: Vector, rotation: Vector) {
        shader.bindTextures()

        // Create model matrix and insert it into the mModel uniform of the mesh's shader
        FloatArray(16).let { modelMatrix ->
            Matrix.setIdentityM(modelMatrix, 0)

            Matrix.translateM(modelMatrix, 0, position.x.toFloat(), position.y.toFloat(), position.z.toFloat())
            Matrix.rotateM(modelMatrix, 0, rotation.a.toFloat(), rotation.x.toFloat(), rotation.y.toFloat(), rotation.z.toFloat())
            glUniformMatrix4fv(glGetUniformLocation(shader.id, "mModel"), 1, false, modelMatrix, 0)
        }

        // get handle to vertex shader's vPosition member. positionHandle is later used to disable its attribute array
        val positionHandle = glGetAttribLocation(shader.id, "vPosition").also { handle ->
            glEnableVertexAttribArray(handle)
            glVertexAttribPointer(
                handle,
                3,
                GL_FLOAT,
                false,
                vertexStride,
                vertexBuffer
            )
        }

        // get handle to vertex shader's vNormal member. normalHandle is later used to disable its attribute array
        val normalHandle = if (normalBuffer != null) {
            glGetAttribLocation(shader.id, "vNormal").also { handle ->
                glEnableVertexAttribArray(handle)       // Enable a handle to the data
                glVertexAttribPointer(              // Prepare the normal data
                    handle,
                    3,
                    GL_FLOAT,
                    false,
                    vertexStride,
                    normalBuffer
                )
            }
        } else null

        // get handle to vertex shader's vTexCoord member. texCoordsHandle is later used to disable its attribute array
        val texCoordsHandle = glGetAttribLocation(shader.id, "vTexCoord").also { handle ->

            // Enable a handle to the triangle vertices
            glEnableVertexAttribArray(handle)

            // Prepare the triangle coordinate data
            glVertexAttribPointer(
                handle,
                2,
                GL_FLOAT,
                false,
                2 * 4,  // Two texCoord coordinates per vertex, Four bytes per coordinate float
                texCoordBuffer
            )
        }

        // Draw the triangle
        glDrawElements(GL_TRIANGLES, drawOrder.size, GL_UNSIGNED_INT, drawListBuffer)

        // Disable vertex array
        glDisableVertexAttribArray(positionHandle)
        if (normalHandle != null) glDisableVertexAttribArray(normalHandle)
        glDisableVertexAttribArray(texCoordsHandle)
    }

    companion object {
        private const val COORDS_PER_VERTEX = 3
    }

    open class CompoundMesh(private val meshes: List<Mesh>) {

        fun load() {
            meshes.forEach{ it.load() }
        }

        fun draw(position: Vector, rotation: Vector) {
            meshes.forEach{ it.draw(position, rotation) }
        }
    }

    open class StaticMesh(protected val position: Vector, protected val rotation: Vector, meshes: List<Mesh>) : CompoundMesh(meshes), Drawable {
        override fun draw() {
            super.draw(position, rotation)
        }
    }

    // TODO: Implement thread safety, Volatile and Synchronized have been removed for testing purposes
    open class DynamicMesh(//@Synchronized get @Synchronized set
        var position: Vector, //@Synchronized get @Synchronized set
        var rotation: Vector, meshes: List<Mesh>) : CompoundMesh(meshes), Drawable {

        init {
            if(position.size != 3) throw IllegalArgumentException("Position vector supplied to DynamicMesh contained ${position.size} elements but at least 3 are needed")
            if(rotation.size != 4) throw IllegalArgumentException("Rotation vector supplied to DynamicMesh contained ${position.size} elements but at least 4 are needed")
        }

        override fun draw() {
            super.draw(position, rotation)
        }

        fun drawOffset(position: Vector, rotation: Vector) {
            super.draw(position + Vector(this.position.x * cos(2*PI*rotation.a/360.0) - this.position.y * sin(2*PI*rotation.a/360.0), this.position.x * sin(2*PI*rotation.a/360.0) + this.position.y * cos(2*PI*rotation.a/360.0), 0.0), this.rotation + rotation)
        }


    }

    open class SprattelMesh(var position: Vector, var rotation: Vector, val meshes: Array<DynamicMesh>): Drawable {
        override fun load() {
            meshes.forEach { it.load() }
        }

        override fun draw() {
            meshes.forEach { it.drawOffset(position, rotation) }
        }

    }
}