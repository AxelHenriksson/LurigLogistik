package com.axehen.hengine

import android.content.Context
import android.opengl.GLES31.*
import android.opengl.GLSurfaceView
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent


// parameter context and attr is required to use AbstractGame extending classes as android Views
abstract class AbstractGame(context: Context, attr: AttributeSet) : GLSurfaceView(context) {

    val renderer: GameRenderer

    private var userInterface: UserInterface? = null
    protected fun setUserInterface(userInterface: UserInterface) {
        this.userInterface = userInterface
        renderer.setUserInterface(userInterface)
    }


    val mainHandler by lazy { Handler(Looper.getMainLooper()) }
    protected var tickPeriod: Long? = null  // Tick time period in milliseconds: 100 => 10 updates a second


    init {
        // Create an OpenGL ES 3.0 context
        super.setEGLContextClientVersion(3)
        super.setEGLConfigChooser(8, 8, 8, 8, 16, 0)

        renderer = GameRenderer(context)

        // Set the Renderer for drawing on the GLSurfaceView
        super.setRenderer(renderer)

        // Render the view only when there is a change in the drawing data
        renderMode = RENDERMODE_WHEN_DIRTY

        if(tickPeriod != null) startTick()
    }

    protected fun setUserInterface(userInterfaceLambda: () -> UserInterface) {
        // Invoking the user interface load lambda in another thread could be useful so that the app does not time out if the UI takes longer than 5 seconds to load.
        // However, just doing a Thread{ ... }.start() here creates problems with the fragment manager.
        setUserInterface(userInterfaceLambda.invoke())
    }

    protected fun loadDrawable(requestRender: Boolean = true, drawableLoadLambda: () -> Drawable) {
        Thread {
            renderer.add(drawableLoadLambda.invoke())
            if(requestRender) requestRender()
        }.start()
    }

    // Game Loop
    protected open fun onTick() { Log.w("GameLoop", "onTick was called but has not been defined by the AbstractGame implementing class. Define onTick or remove unnecessary onTick call") }

    private val tickRunnable = object : Runnable {
        override fun run() {
            if (tickPeriod == null) { stopTick(); return }
            onTick()
            mainHandler.postDelayed(this, tickPeriod!!)
            }
        }

    fun startTick() {
        Log.d("GameClock", "AbstractGame startTick() called")
        mainHandler.post(tickRunnable)
    }
    fun stopTick() {
        Log.d("GameClock", "AbstractGame stopTick() called")
        mainHandler.removeCallbacks(tickRunnable)
    }

    override fun onResume() {
        Log.d("GameClock", "AbstractGame onResume() called")
        startTick()
    }

    override fun onPause() {
        Log.d("GameClock", "AbstractGame onPause() called")
        stopTick()
    }

    // Touch Handling
    override fun onTouchEvent(event: MotionEvent): Boolean {
        // If there is a UI present, call onTouch for it else return false. If onTouch consumes the touch; request render to update the button
        return userInterface?.onTouchEvent(event) ?: false
    }

}