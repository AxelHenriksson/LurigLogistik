package com.axehen.hengine

//import android.util.Log
import android.content.Context
import java.util.*

class ModelImport {

    companion object {

        private data class Vertex(private val pos: Vector, private val texCoord: Vector, private val normal: Vector) {
            var x: Double
                get()       = pos.x
                set(value)  { pos.x = value }
            var y: Double
                get()       = pos.y
                set(value)  { pos.y = value }
            var z: Double
                get()       = pos.z
                set(value)  { pos.z = value }
            var u: Double
                get()       = texCoord.x
                set(value)  { texCoord.x = value }
            var v: Double
                get()       = texCoord.y
                set(value)  { texCoord.y = value }
            var nx: Double
                get()       = normal.x
                set(value)  { normal.x = value }
            var ny: Double
                get()       = normal.y
                set(value)  { normal.y = value }
            var nz: Double
                get()       = normal.z
                set(value)  { normal.z = value }

        }

        fun parseOBJMTL(context: Context, renderer: GameRenderer, asset: String): List<Mesh> {


            val objStream = context.assets.open("$asset.obj")
            val mtlStream = context.assets.open("$asset.mtl")

            objStream.close()
            mtlStream.close()


            val posList = ArrayList<Vector>()
            val texCoordList = ArrayList<Vector>()
            val normalList = ArrayList<Vector>()
            val faceList = ArrayList<Array<Vertex>>()
            var activeMaterial = ""
            val meshList = ArrayList<Mesh>()

            // Above is done, below is to be replaced


            // Split the obj file string into one string per object. Disregard the first element as that is above the first object declaration
            //val oStrings = objString.split("\\no [^\\s]+".toRegex()).let {
            //    it.subList(1, it.size)
            //}



            for (oString in oStrings) {
                for (line in oString.lines()) {

                    line.split(" ").let { words ->
                        when (words[0]) {
                            "v" -> posList.add(
                                Vector(
                                    words[1].toDouble(),
                                    words[2].toDouble(),
                                    words[3].toDouble()
                                )
                            )
                            "vt" -> texCoordList.add(
                                Vector(
                                    words[1].toDouble(),
                                    words[2].toDouble()
                                )
                            )
                            "vn" -> normalList.add(
                                Vector(
                                    words[1].toDouble(),
                                    words[2].toDouble(),
                                    words[3].toDouble()
                                )
                            )
                            "f" -> {
                                faceList.add(Array(/*words.size - 1*/ 3) {      // With size = 3 we only add triangles, leaving holes in the mesh if there are more than 3 vertices per face. Having size = words.size - 1, i.e. add all vertices of every face, means we get an error further down. As long as we triangulate the mesh before import we should not get holes in the mesh by using size 3.
                                    val indices = words[it + 1].split("/")
                                    val texCoord =
                                        if (indices[1].isNotEmpty()) texCoordList[indices[1].toInt() - 1] else Vector(
                                            0.0,
                                            0.0
                                        )
                                    Vertex(
                                        posList[indices[0].toInt() - 1],
                                        texCoord,
                                        normalList[indices[2].toInt() - 1]
                                    )
                                })
                            }
                            "usemtl" -> {
                                if (activeMaterial != "") {
                                    meshList.add(
                                        createMesh(
                                            faceList,
                                            getShaderFromMTL(mtlString, activeMaterial, context, renderer)
                                        )
                                    )
                                    faceList.clear()
                                }
                                activeMaterial = words[1]
                            }
                            else -> {}
                        }
                    }
                }
                meshList.add(createMesh(faceList, getShaderFromMTL(mtlString, activeMaterial, context, renderer)))
            }
            return meshList//.also {
                //Log.d("ModelImport","${objString.lines().size} lines parsed in OBJ file \"$asset\", ${posList.size} positions added, ${texCoordList.size} texCoords added, ${normalList.size} normals added, ${faceList.size} faces added" )
            //}
        }

        private fun getShaderFromMTL(
            mtlString: String,
            activeMaterial: String,
            context: Context,
            renderer: GameRenderer
        ): Shader {
            val lines = mtlString.lines()

            var mapKd: String? = null
            var kd: Shader.UniformColor? = null

            var i = lines.indexOf("newmtl $activeMaterial")+1
            while(i < lines.size && !(lines[i].startsWith("newmtl"))) {

                val words = lines[i].split("\\s+".toRegex()).toTypedArray()
                when (words[0]) {
                    "map_Kd" -> {
                        mapKd = words[1]
                        //Log.d("ModelImport", "map_Kd: $mapKd")
                    }
                    "Kd" -> { kd = Shader.UniformColor("Kd", words[1].toDouble(), words[2].toDouble(), words[3].toDouble(), 1.0)
                        //Log.d("ModelImport", "Kd: $kd")
                    }
                    else -> {}
                }

                i++
            }

            return Shader.MTLShader(
                renderer,
                context,
                arrayListOf(
                    kd ?: Shader.UniformColor("Kd", 0.5, 0.5, 0.5, 1.0),
                ),
                arrayListOf(
                    mapKd?.replace("\\", "/")?.let { mapKd -> AbstractTexture.Texture(
                        bitmap = Utils.getBitmap(context, if (mapKd.startsWith("textures/")) mapKd else "textures/$mapKd"),
                        uniform = "map_Kd"
                    ) }
                )
            )
        }

        fun parseOBJ(context: Context, asset: String, scale: Double, shader: Shader): Mesh {
            val objString = Utils.getStringFromAsset(context, "$asset.obj")

            val posList = ArrayList<Vector>()
            val texCoordList = ArrayList<Vector>()
            val normalList = ArrayList<Vector>()
            val faceList = ArrayList<Array<Vertex>>()

            for (line in objString.lines()) {

                line.split(" ").let { words ->
                    when (words[0]) {
                        "v" -> posList.add(
                            Vector(
                                words[1].toDouble(),
                                words[2].toDouble(),
                                words[3].toDouble()
                            )
                        )
                        "vt" -> texCoordList.add(Vector(words[1].toDouble(), words[2].toDouble()))
                        "vn" -> normalList.add(
                            Vector(
                                words[1].toDouble(),
                                words[2].toDouble(),
                                words[3].toDouble()
                            )
                        )
                        "f" -> {
                            faceList.add(Array(/*words.size - 1*/ 3) {      // With size = 3 we only add triangles, leaving holes in the mesh if there are more than 3 vertices per face. Having size = words.size - 1, i.e. add all vertices of every face, means we get an error further down. As long as we triangulate the mesh before import we should not get holes in the mesh by using size 3.
                                val indices = words[it + 1].split("/")
                                val texCoord =
                                    if (indices[1].isNotEmpty()) texCoordList[indices[1].toInt() - 1] else Vector(
                                        0.0,
                                        0.0
                                    )
                                Vertex(
                                    posList[indices[0].toInt() - 1] * scale,
                                    texCoord,
                                    normalList[indices[2].toInt() - 1]
                                )
                            })
                        }
                        else -> {}
                    }
                }
            }


            //Log.d("ModelImport", "${objString.lines().size} lines parsed in OBJ file, ${posList.size} positions added, ${texCoordList.size} texCoords added, ${normalList.size} normals added, ${faceList.size} faces added")

            return createMesh(faceList, shader)
        }

        private fun createMesh(faceList: List<Array<Vertex>>, shader: Shader): Mesh {
            val vertexCount = faceList.size * 3
            val vertexPositions = FloatArray(vertexCount * 3)
            val normals = FloatArray(vertexCount * 3)
            val texCoords = FloatArray(vertexCount * 2)
            val drawOrder = IntArray(vertexCount)

            var index = 0
            for (vertexArray in faceList) {
                for (vertex in vertexArray) {
                    vertexPositions[3 * index + 0] = vertex.x.toFloat()
                    vertexPositions[3 * index + 1] = vertex.y.toFloat()
                    vertexPositions[3 * index + 2] = vertex.z.toFloat()

                    normals[3 * index + 0] = vertex.nx.toFloat()
                    normals[3 * index + 1] = vertex.ny.toFloat()
                    normals[3 * index + 2] = vertex.nz.toFloat()

                    texCoords[2 * index + 0] = vertex.u.toFloat()
                    texCoords[2 * index + 1] = vertex.v.toFloat()

                    drawOrder[index] = index

                    index++
                }
            }

            return Mesh(
                vertexCoords = vertexPositions,
                normals = normals,
                texCoords = texCoords,
                drawOrder = drawOrder,
                shader = shader
            )
        }

    }
}