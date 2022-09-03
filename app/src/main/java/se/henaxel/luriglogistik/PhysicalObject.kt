package se.henaxel.luriglogistik

import com.axehen.hengine.Mesh
import com.axehen.hengine.Vector

class PhysicalObject(position: Vector, rotation: Vector, meshes: List<Mesh>): Mesh.DynamicMesh(position, rotation, meshes) {
}