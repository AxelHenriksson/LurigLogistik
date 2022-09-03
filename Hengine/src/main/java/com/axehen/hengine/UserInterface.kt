package com.axehen.hengine

import android.view.MotionEvent
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

class UserInterface(private val game: AbstractGame): Drawable{

    val elements = ArrayList<UIRectangle>()
    val buttons = ArrayList<UIRectangle.UIButton>()

    var scale = 1f
    private var onTouchBackgroundLambda: ((MotionEvent) -> Unit)? = null

    override fun load() {
        elements.forEach { it.load() }
        buttons.forEach { it.load() }
    }

    override fun draw() {
        elements.forEach { it.draw(game.width / scale, game.height / scale) }
        buttons.forEach { it.draw(game.width / scale, game.height / scale) }
    }


    fun setOnTouchBackground(lambda: (MotionEvent) -> Unit) { onTouchBackgroundLambda = lambda }

    /**
     * @return True f the UI consumes the touch, False otherwise
     */
    fun onTouchEvent(event: MotionEvent): Boolean {

        for (button in buttons) {                                                                       // Loop through all buttons and notify them of the press/release
            if (button.touch(event, game.width, game.height, scale)) {                                  // Check if the button was touched by the press, the button also consumes the release of a pointer which is pressing the button down.
                game.requestRender()                                                                    // Request new render so that the button press/release is visible
                return true                                                                             // Return true to notify that the UI consumed the touch
            }
        }

        return onTouchBackgroundLambda?.invoke(event) != null
    }


    /**
     * A UI element which acts as a traditional thumbstick
     * @param radius The radius within which the thumbstick output goes from 0.0 at center to 1.0 at radius. [radius] is the percentage of the smallest screen dimension (width or height)
     * @param deadZoneRadius the percentage of the radius in which the thumbstick output is zero
     * @param springBack true means the thumbstick output vector returns to zero when the finger is lifted. false means the thumbstick remains at the last position value when it is no longer being touched.
     * @param followOnLimit true means the thumbstick origin follows the finger when it has been moved outside the thumbstick [radius]. False means it remains at the same position as long as the thumbstick is being pressed.
     * @param xFunction The activation function for the x component, e.g. linear, quadratic, sigmoid.
     * @param yFunction The activation function for the y component, e.g. linear, quadratic, sigmoid.
     * @param pinchCallback Is called when the thumbstick area is being pinched.
     * @param stickCallback Is called when the thumbstick is being pressed, moved or released.
     */
    class UITouchStick(
        private val game: AbstractGame,

        private val radius: Double = 0.25,
        private val deadZoneRadius: Double = 0.0,
        private val springBack: Boolean = true,
        private val followOnLimit: Boolean = false,
        private val xFunction: (x: Double) -> Double = { x ->  x },
        private val yFunction: (y: Double) -> Double = { y ->  y },
        private val pinchCallback: ((deltaDistance: Double) -> Unit)? = null,
        private val stickCallback: ((x: Double, y: Double) -> Unit)? = null) {

        private var stickPos = Vector(0.0, 0.0)
        private var previousTouch = Vector(0.0, 0.0)
        private var previousTouchDistance: Double = 0.0

        private val width by lazy { game.width }
        private val height by lazy { game.height }

        /**
         * Rescales and clamps a vectors components so that components less than the radius are zero and rise linearly from the 0 at [radius] to 1 at 1
         * @param radius The radius within which components are rounded down to zero.
         */
        private fun Vector.applyDeadZone(deadZoneRadius: Double): Vector {
            return if (deadZoneRadius == 0.0)
                this
            else
                Vector(this.map { x -> if(abs(x) <= radius) 0.0 else { if(x < 0.0)  (x/(1-deadZoneRadius))-(1.0-(1.0/(1-deadZoneRadius))) else (x/(1-deadZoneRadius))+(1.0-(1.0/(1-deadZoneRadius)))  }})
        }

        fun onTouch(event: MotionEvent) {
            val x = event.x.toDouble()
            val y = event.y.toDouble()

            // "Pinch" distance between two pointers touching the stick area
            val dist: Double = if (event.pointerCount == 2) sqrt((event.getX(0)-event.getX(1)).pow(2) + (event.getY(0)-event.getY(1)).pow(2)).toDouble() else 0.0

            when(event.action) {
                MotionEvent.ACTION_UP -> {
                    stickCallback?.let {
                        if (springBack) {
                            stickPos.x = 0.0
                            stickPos.y = 0.0
                            it(xFunction(stickPos.x), xFunction(stickPos.y))
                        }
                    }
                }
                MotionEvent.ACTION_MOVE -> {

                    val dx: Double = x - previousTouch.x
                    val dy: Double = y - previousTouch.y
                    val dDist: Double = dist - previousTouchDistance

                    when (event.pointerCount) {
                        1 -> {
                            stickCallback?.let {
                                // Add the moved distance to the stickPos
                                stickPos = stickPos + Vector(dx/(radius * min(width, height)), dy/(radius * min(width, height)))

                                // If the stick should follow the finger when it moves outside its radius we shorten the stick back to 1 so that the "origin" moves within 1 unit of the finger
                                if (followOnLimit && stickPos.length() > 1f) stickPos = stickPos.normalize()


                                val coercedStickPos = stickPos
                                    .coerceLengthWithin(0.0, 1.0)   // Make sure that the returned stickPos length is not negative or longer than 1 regardless if we have moved the origin or not.
                                    .applyDeadZone(deadZoneRadius)  // Apply a deadzone in the middle of the stick area so that small inputs are considered zero. Also rescales so that the movement is linear as zero from the deadzone radius to 1 at radius 1.

                                // Send the callback but apply the activation functions (e.g. linear, quadratic, sigmoid, etc.) beforehand.
                                it(xFunction(coercedStickPos.x), yFunction(coercedStickPos.y))
                            }
                        }
                        2 -> {

                            pinchCallback?.let{
                                it(dDist / height.toFloat())
                            }
                        }
                    }

                }
            }
            previousTouch = Vector(x, y)
            previousTouchDistance = dist
        }

        override fun toString(): String {
            return "[width=$width, height=$height, radius=$radius, pinchCallback=${if (pinchCallback != null) "defined" else "null"}, stickCallback=${if (stickCallback != null) "defined" else "null"}]"
        }
    }


}