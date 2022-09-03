package se.henaxel.luriglogistik

import android.util.Log
import com.axehen.hengine.Mesh
import com.axehen.hengine.Mesh.SprattelMesh
import com.axehen.hengine.Mesh.CompoundMesh
import com.axehen.hengine.Vector
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

class Truck(
    position: Vector,
    rotation: Vector,
    var maxSpeed: Double = 1.0,
    var maxWheelAngle: Double = 90.0,
    private val pivotYOffset: Double = 20.0/32.0,
    private val turnWheelYOffset: Double = -12.0/32.0,
    private val chassisMesh: Mesh.DynamicMesh,
    private val handleMesh: Mesh.DynamicMesh
)
    : SprattelMesh(position, rotation, arrayOf(chassisMesh, handleMesh)) {

    private var speed: Double = 0.0
    private var wheelAngle: Double = 0.0

    fun onTick(deltaMillis: Long) {
        Log.d("Truck", "Truck.onTick deltaMillis=$deltaMillis, position=$position, rotation=$rotation, speed=$speed, wheelAngle=$wheelAngle")
        val angularVelocity = (speed * sin(2*PI*wheelAngle/360.0) * 360.0 /(2 * PI * (abs(turnWheelYOffset))))
        this.position = position + Vector(speed * (deltaMillis/1000.0) * sin(-2*PI*(rotation.a)/360.0), speed * (deltaMillis/1000.0) * cos(2*PI*(rotation.a)/360.0), 0.0) + Vector(2*PI*pivotYOffset*angularVelocity/360*(deltaMillis/1000.0) * cos(2*PI*rotation.a/360.0), 2*PI*pivotYOffset*angularVelocity/360*(deltaMillis/1000.0) * sin(2*PI*rotation.a/360.0), 0.0)
        this.rotation = Vector(0.0, 0.0, 1.0, rotation.a + (deltaMillis/1000.0) * angularVelocity)
    }

    fun setWheelAngle(wheelAngle: Double) {
        this.wheelAngle = wheelAngle.coerceIn(-maxWheelAngle, maxWheelAngle)
        handleMesh.rotation = Vector(0.0, 0.0, 1.0, -wheelAngle)
        Log.d(TAG, "setWheelAngle(wheelAngle=$wheelAngle), maxWheelAngle=$maxWheelAngle")
    }
    fun setWheelAnglePercentage(wheelAnglePercentage: Double) {
        this.wheelAngle = (wheelAnglePercentage * maxWheelAngle).coerceIn(-maxWheelAngle, maxWheelAngle)
        handleMesh.rotation = Vector(0.0, 0.0, 1.0, -wheelAngle)
        Log.d(TAG, "setWheelAnglePercentage(wheelAnglePercentage=$wheelAnglePercentage) => wheelAngle=$wheelAngle, maxWheelAngle=$maxWheelAngle")
    }
    fun setSpeed(speed: Double) {
        this.speed = speed.coerceIn(-maxSpeed, maxSpeed)
        Log.d(TAG, "setSpeed(speed=$speed), maxSpeed=$maxSpeed")
    }
    fun setSpeedPercentage(speedPercentage: Double) {
        this.speed = (speedPercentage * maxSpeed).coerceIn(-maxSpeed, maxSpeed)
        Log.d(TAG, "setSpeedPercentage(speedPercentage=$speedPercentage) => speed=$speed, maxSpeed=$maxSpeed")
    }

    companion object {
        const val TAG = "Truck"

        enum class SprattelIndex(val value: Int) {
            CHASSIS(0),
            HANDLE(1);

            companion object {
                fun fromInt(value: Int) = SprattelIndex.values().first { it.value == value }
            }

        }
    }
}