package com.axehen.hengine

import android.opengl.GLES31
import android.opengl.Matrix
import android.util.Log
import android.view.MotionEvent
import java.lang.IllegalArgumentException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.IntBuffer

/**
 * @param dimensions    The UIRectangle x/y dimensions in inches
 * @param margins       The UIRectangle x/y margins in inches
 * @param anchor        The UIRectangles anchor (TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT)
 */
open class UIRectangle(var dimensions: Vector, var margins: Vector, var anchor: UIAnchor, protected val shader: Shader) {

    /**
     * @return the UIRectangles origin
     */
    protected fun getOrigin(screenWidth: Int, screenHeight: Int, scale: Float): Vector {
        val xOffset = (margins.x+dimensions.x/2) * scale
        val yOffset = (margins.y+dimensions.y/2) * scale

        return when (anchor) {
            UIAnchor.TOP_LEFT       -> Vector(                        xOffset,  screenHeight - yOffset)
            UIAnchor.TOP_RIGHT      -> Vector(screenWidth - xOffset,  screenHeight - yOffset)
            UIAnchor.BOTTOM_LEFT    -> Vector(                        xOffset,                          yOffset)
            UIAnchor.BOTTOM_RIGHT   -> Vector(screenWidth - xOffset,                          yOffset)
            UIAnchor.TOP_MIDDLE     -> Vector(screenWidth/2f + margins.x * scale,screenHeight - yOffset)
            UIAnchor.BOTTOM_MIDDLE  -> Vector(screenWidth/2f + margins.x * scale,                        yOffset)
            UIAnchor.LEFT_MIDDLE    -> Vector(                        xOffset,             screenHeight/2f + margins.y * scale)
            UIAnchor.RIGHT_MIDDLE   -> Vector(screenWidth - xOffset,             screenHeight/2f + margins.y * scale)
            UIAnchor.CENTER         -> TODO("Not yet implemented")
        }
    }

    // Create a basic plane spanning the screen that is later translated and scaled in the shader
    private val vertexCoords = floatArrayOf(
        -1f, -1f, 0f,
         1f, -1f, 0f,
         1f,  1f, 0f,
        -1f,  1f, 0f)
    private val drawOrder = intArrayOf(
        0, 1, 2,
        0, 2, 3,)


    private var vertexBuffer: FloatBuffer =
        // (number of coordinate values * 4 bytes per float)
        ByteBuffer.allocateDirect(vertexCoords.size * 4).run {
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

    private val drawListBuffer: IntBuffer =
        // (# of coordinate values * 4 bytes per int)
        ByteBuffer.allocateDirect(drawOrder.size * 4).run {
            order(ByteOrder.nativeOrder())
            asIntBuffer().apply {
                put(drawOrder)
                position(0)
            }
        }


    init {
        if (vertexCoords.size % COORDS_PER_VERTEX != 0) throw IllegalArgumentException("Vertex coordinate count is not divisible by coordsPerVertex (${COORDS_PER_VERTEX})")
        if (drawOrder.size % COORDS_PER_VERTEX != 0) throw IllegalArgumentException("Draw order count is not divisible by coordsPerVertex (${COORDS_PER_VERTEX})")
    }


    fun load() {
        shader.load()
    }


    private val vertexStride: Int = COORDS_PER_VERTEX * 4 // 4 bytes per vertex


    open fun draw(width: Float, height: Float) {
        shader.bindTextures()

        // get handle to vertex shader's vPosition member. positionHandle is later used to disable its attribute array
        val positionHandle = GLES31.glGetAttribLocation(shader.id, "vPosition").also { handle ->
            GLES31.glEnableVertexAttribArray(handle)
            GLES31.glVertexAttribPointer(
                handle,
                3,
                GLES31.GL_FLOAT,
                false,
                vertexStride,
                vertexBuffer
            )
        }

        // Create a matrix to scale and move UIRectangle
        FloatArray(16).let { matrix ->
            Matrix.setIdentityM(matrix, 0)
            val xMargin = (margins.x*2f/width).toFloat()   // Margin (offset) in opengl coordinates (-1f to 1f)
            val yMargin = (margins.y*2f/height).toFloat()
            val xRadius = (dimensions.x/width).toFloat()   // Distance from center of UIRect to edge in opengl coordinates (-1f to 1f)
            val yRadius = (dimensions.y/height).toFloat()

            when (anchor) {
                UIAnchor.TOP_LEFT       -> Matrix.translateM(matrix, 0, -1f + xRadius + xMargin,  1f - yRadius - yMargin, 0f)
                UIAnchor.TOP_RIGHT      -> Matrix.translateM(matrix, 0,  1f - xRadius - xMargin,  1f - yRadius - yMargin, 0f)
                UIAnchor.BOTTOM_LEFT    -> Matrix.translateM(matrix, 0, -1f + xRadius + xMargin, -1f + yRadius + yMargin, 0f)
                UIAnchor.BOTTOM_RIGHT   -> Matrix.translateM(matrix, 0,  1f - xRadius - xMargin, -1f + yRadius + yMargin, 0f)
                UIAnchor.BOTTOM_MIDDLE  -> Matrix.translateM(matrix, 0,  xMargin,                   -1f + yRadius + yMargin, 0f)
                UIAnchor.TOP_MIDDLE     -> Matrix.translateM(matrix, 0,  xMargin,                    1f - yRadius - yMargin, 0f)
                UIAnchor.LEFT_MIDDLE    -> Matrix.translateM(matrix, 0,  -1f + xRadius + xMargin,    yMargin, 0f)
                UIAnchor.RIGHT_MIDDLE   -> Matrix.translateM(matrix, 0,   1f - xRadius - xMargin,    yMargin, 0f)
            }
            Matrix.scaleM(matrix, 0, (dimensions.x/width).toFloat(), (dimensions.y/height).toFloat(), 0f)
            GLES31.glUniformMatrix4fv(
                GLES31.glGetUniformLocation(shader.id, "mTransform"),
                1,
                false,
                matrix,
                0
            )
        }

        // Draw the triangle
        GLES31.glDrawElements(
            GLES31.GL_TRIANGLES,
            drawOrder.size,
            GLES31.GL_UNSIGNED_INT,
            drawListBuffer
        )

        // Disable vertex array
        GLES31.glDisableVertexAttribArray(positionHandle)
    }

    companion object {
        private const val TAG = "hengine.Mesh.kt"
        private const val COORDS_PER_VERTEX = 3

        enum class UIAnchor {
            TOP_LEFT,
            TOP_RIGHT,
            BOTTOM_LEFT,
            BOTTOM_RIGHT,
            TOP_MIDDLE,
            LEFT_MIDDLE,
            RIGHT_MIDDLE,
            BOTTOM_MIDDLE,
            CENTER
        }
    }

    interface UICollidable {
        fun isWithin(buttonOrigin: Vector, scale: Float, touchPos: Vector): Boolean
    }
    class CircleUICollidable(private val radius: Float): UICollidable {
        override fun isWithin(buttonOrigin: Vector, scale: Float, touchPos: Vector): Boolean = (buttonOrigin - touchPos).length() <= radius * scale
    }
    class UIButton(dimensions: Vector, margins: Vector, anchor: UIAnchor, shader: Shader, val collidable: UICollidable, val action: (pressed: Int) -> Unit): UIRectangle(dimensions, margins, anchor, shader) {

        private val registeredPointerIds: HashSet<Int> = hashSetOf()
        var isPressed = false

        /**
         * @return true if the element consumed the touch, either by the touch being performed by a pointer registered to the button. false otherwise
         */
        fun touch(event: MotionEvent, screenWidth: Int, screenHeight: Int, scale: Float): Boolean {

            val actionMasked = event.actionMasked
            val pointerIndex = ((event.action and MotionEvent.ACTION_POINTER_ID_MASK) shr MotionEvent.ACTION_POINTER_ID_SHIFT)
            val pointerID = event.getPointerId(pointerIndex)                                            // The ID of the pointer performing the motion action

            if(pointerID in registeredPointerIds) {
                if(actionMasked == MotionEvent.ACTION_UP || actionMasked == MotionEvent.ACTION_POINTER_UP) {
                        registeredPointerIds.remove(pointerID)                                          // the pointer registered to the button was release, deregister it from the button
                        if(registeredPointerIds.size < 1) { isPressed = false; action.invoke(event.action) }                   // If there are no more pointers pressing the button, execute the action with "ACTION_UP" as parameter
                }
                return true
            }
            else if (actionMasked == MotionEvent.ACTION_DOWN || actionMasked == MotionEvent.ACTION_POINTER_DOWN) {
                if(collidable.isWithin(getOrigin(screenWidth, screenHeight, scale), scale, Vector(event.getX(pointerIndex).toDouble(), (screenHeight - event.getY(pointerIndex)).toDouble()))) {
                    registeredPointerIds.add(pointerID)                                             // The pointer pressed down. Register the pointer to the button and invoke the action if it is inside the button
                    if (registeredPointerIds.size == 1) { isPressed = true; action.invoke(event.action) }                  // If the button has no registered pointers (i.e. was not previously pressed), invoke the buttons action with "ACTION_DOWN" as parameter
                    return true
                }
            }
            return false
        }

        override fun draw(width: Float, height: Float) {
            GLES31.glUseProgram(shader.id)
            GLES31.glUniform1i(GLES31.glGetUniformLocation(shader.id, "isPressed"), if(isPressed) 1 else 0)
            super.draw(width, height)
        }
    }
}