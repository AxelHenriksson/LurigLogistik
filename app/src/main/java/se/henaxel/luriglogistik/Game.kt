package se.henaxel.luriglogistik

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import com.axehen.hengine.*
import com.axehen.hengine.Mesh.StaticMesh
import com.axehen.hengine.ModelImport.Companion.parseOBJMTL
import com.axehen.hengine.Utils.Companion.toDegrees
import com.axehen.hengine.Vector.Companion.degreesToRadians
import kotlin.math.*


class Game(context: Context, attr: AttributeSet): com.axehen.hengine.AbstractGame(context, attr) {

    private val pallMesh by lazy { parseOBJMTL(this.context, this.renderer, "models/pall") }
    private val floorShader by lazy { Shader(renderer, "shaders/floor") }

    private var truck: Truck
    private val physicalObjects = arrayListOf(
        PhysicalObject(
            Vector(0.5, 2.5, 0.0),
            Vector(0.0, 0.0, 1.0, 0.0),
            pallMesh
        ),
        PhysicalObject(
            Vector(-1.5, 2.0, 0.0),
            Vector(0.0, 0.0, 1.0, 45.0),
            pallMesh
        ),
        PhysicalObject(
            Vector(0.0, 4.5, 0.0),
            Vector(0.0, 0.0, 1.0, 90.0),
            pallMesh
        )
    )

    init {
        renderer.lookAt = Vector(0.0, 0.0, 0.0)
        renderer.lookFrom = Vector(0.0, 0.0, 10.0)
        renderer.zoom = 0.1f
        renderer.setUpVec(Vector(0.0, 1.0, 0.0))

        tickPeriod = 1000/60

        physicalObjects.forEach { loadDrawable { it } }


        loadDrawable {
            StaticMesh(
                Vector(0.0, 0.0, 0.0),
                Vector(0.0, 0.0, 1.0, 0.0),
                listOf(
                    Mesh(
                        vertexCoords = floatArrayOf(
                            -10f, -10f, 0f,
                             10f, -10f, 0f,
                             10f,  10f, 0f,
                            -10f,  10f, 0f),
                        texCoords = floatArrayOf(
                            -10f, -10f,
                             10f, -10f,
                             10f,  10f,
                            -10f,  10f
                        ),
                        drawOrder = intArrayOf(
                            0,1,2,
                            0,2,3
                        ),
                        shader = floorShader
                    )
                )
            )
        }


        truck = Truck(
            Vector(0.0, 0.0, 0.1),
            Vector(0.0, 1.0, 0.0, 0.0),
            chassisMesh = Mesh.DynamicMesh(Vector(0.0, 0.0, 0.0), Vector(0.0, 0.0, 0.0, 0.0), parseOBJMTL(this.context, this.renderer, "models/truck")),
            handleMesh = Mesh.DynamicMesh(Vector(0.0, -12.0/32.0, 0.0), Vector(0.0, 0.0, 1.0, 0.0), parseOBJMTL(this.context, this.renderer, "models/handtag"))
        ). also { loadDrawable {it} }

        val touchStick = UserInterface.UITouchStick(this,
            deadZoneRadius = 0.0,
            stickCallback = { x, y ->
                Log.d("Game", "stickCallback with x:$x, y:$y")
                truck.setWheelAngle(atan2(-x,1.0).toDegrees().coerceIn(-90.0,90.0))
                truck.setSpeedPercentage(y)
            }
        )

        setUserInterface {
            UserInterface(this).also {
                it.setOnTouchBackground { event -> touchStick.onTouch(event) }
            }
        }

    }


    private var lastTime: Long = System.currentTimeMillis()
    override fun onTick() {
        Log.d("GameLoop", "ticked")

        truck.onTick(System.currentTimeMillis() - lastTime)

        renderer.lookAt = truck.position
        renderer.setUpVec(Vector(-sin(degreesToRadians(truck.rotation.a)), cos(degreesToRadians(truck.rotation.a)), 0.0))
        renderer.updateView() // As we move the camera this is required


        requestRender()         // TODO: check if this is necessary

        lastTime = System.currentTimeMillis()
    }

}