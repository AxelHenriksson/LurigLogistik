package com.axehen.hengine

import android.content.Context
import android.opengl.GLES31.*
import android.util.Log
import com.axehen.hengine.Utils.Companion.contentEquals

open class Shader(private val renderer: GameRenderer, val asset: String, protected val textures: List<AbstractTexture>) {
    val id: Int by lazy { renderer.loadShader(this) }

    constructor(renderer: GameRenderer, asset: String) : this(renderer, asset, listOf())

    open fun load() {


        // Load textures
        glUseProgram(id)

        var slot = 0
        textures.forEach { texture ->

            if (texture is AbstractTexture.Texture) {
                //if (textures!![i].id != -1) continue
                texture.load(GL_TEXTURE0 + slot)

                glBindTexture(GL_TEXTURE_2D, slot)

                // Insert texture into shader
                val textureUniformLocation = glGetUniformLocation(id, texture.uniform)

                glUniform1i(textureUniformLocation, slot)

                slot++
            } else if (texture is AbstractTexture.CubeMap) {
                //if (cubeMap.ID != -1) //TODO: Do something if the texture already exists

                texture.load()

                glBindTexture(GL_TEXTURE_CUBE_MAP, texture.id)

                val cubemapUniformLocation = glGetUniformLocation(id, texture.uniform)

                glUniform1i(cubemapUniformLocation, 0)
            }
        }
    }

    open fun bindTextures() {
        glUseProgram(id)

        var slot = 0
        textures.forEach { texture ->
            if (texture is AbstractTexture.Texture) {
                glActiveTexture(GL_TEXTURE0 + slot)
                glBindTexture(GL_TEXTURE_2D, texture.id)
                slot++
            } else if (texture is AbstractTexture.CubeMap) {
                glBindTexture(GL_TEXTURE_CUBE_MAP, texture.id)
            }
        }
    }


    override fun hashCode(): Int =
        asset.hashCode() + textures.hashCode()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Shader

        if (asset != other.asset) return false
        if (!(textures contentEquals other.textures)) return false

        return true
    }

    companion object {
        private const val TAG = "Shader.kt"

        /**
         * Creates and compiles an openGL shader into GLES memory from a string of shader code
         * @param type          GLES shader type, typically GL_VERTEX_SHADER or GL_FRAGMENT_SHADER
         * @param shaderCode    Bare string containing the shaders code
         * @return The compiled shader's id
         */
        private fun loadShader(type: Int, shaderCode: String): Int {
            val shader = glCreateShader(type)

            // add the source code to the shader and compile it
            glShaderSource(shader, shaderCode)
            glCompileShader(shader)

            Log.d(TAG, "loadShader on \n$shaderCode\n returns shaderID: $shader ${glGetShaderInfoLog(shader).let {if (it.isNotEmpty()) "with infolog: $it" else ""}}")
            return shader
        }

        /**
         * Reads a shader text file from an asset file and creates and compiles the shader into GLES memory
         * @param context       The current context, required to read resource
         * @param type          GLES shader type, typically GL_VERTEX_SHADER or GL_FRAGMENT_SHADER
         * @param asset         Name of the shader .vert and .frag files without extension
         * @return              The compiled shader's id
         */
        fun loadShaderFromAsset(context: Context, type: Int, asset: String): Int = loadShader(type, Utils.getStringFromAsset(context, asset))

        fun checkCompileErrors(shaderId: Int, type: Int) {
            val compileStatus = IntArray(1)
            glGetShaderiv(shaderId, GL_COMPILE_STATUS, compileStatus, 0)
            if (compileStatus[0] != GL_TRUE) {
                glDeleteShader(shaderId)
                throw RuntimeException(
                    "Could not compile ${if(type == GL_VERTEX_SHADER) "vertex shader" else if(type == GL_FRAGMENT_SHADER) "fragment shader" else "shader"}: "
                            + glGetShaderInfoLog(shaderId)
                )
            }
        }
        fun checkLinkErrors(programId: Int) {
            val linkStatus = IntArray(1)
            glGetProgramiv(programId, GL_LINK_STATUS, linkStatus, 0)
            if (linkStatus[0] != GL_TRUE) {
                glDeleteProgram(programId)
                throw RuntimeException(
                    "Could not link program: "
                            + glGetProgramInfoLog(programId)
                )
            }
        }
    }

    data class UniformColor(val uniform: String, val color: Vector) {
        constructor(uniform: String, r: Double, g: Double, b: Double, a: Double): this(uniform, Vector(r, g, b, a))
        val r get() = color.r
        val g get() = color.g
        val b get() = color.b
        val a get() = color.a
    }

    class MTLShader(renderer: GameRenderer, context: Context, private val colors: List<UniformColor>, textures: List<AbstractTexture?>): Shader(renderer, "shaders/mtl", textures.filterNotNull()) {
        private val missingMaps = mutableSetOf("map_Kd")
        private val whiteTexture = AbstractTexture.Texture(Utils.getBitmap(context, "textures/white.png"), "")

        override fun load() {

            // Load all supplied textures
            super.load()

            // Get a HashSet of all map uniforms not supplied with a texture from [textures], these are to be supplied with a white texture
            textures.forEach { texture ->
                if (texture is AbstractTexture.Texture) {
                    missingMaps.remove(texture.uniform)
                }
            }

            // Create the white texture
            whiteTexture.load(GL_TEXTURE0 + textures.size)  // When super.load() has loaded all textures in textures, load this one after them.

            glBindTexture(GL_TEXTURE_2D, textures.size)

            // Insert texture into all non-supplied uniforms
            for (uniform in missingMaps) {
                glUniform1i(glGetUniformLocation(id, uniform), textures.size)
            }

            glUseProgram(id)

            // Insert color into shader
            for (color in colors) {
                glUniform4f(glGetUniformLocation(id, color.uniform), color.r.toFloat(), color.g.toFloat(), color.b.toFloat(), color.a.toFloat())
            }
        }

        override fun bindTextures() {
            super.bindTextures()

            // Bind white texture to all non-supplied uniforms
            for (uniform in missingMaps) {
                glActiveTexture(GL_TEXTURE0 + textures.size)
                glBindTexture(GL_TEXTURE_2D, whiteTexture.id)
            }
        }

        override fun hashCode(): Int {
            return super.hashCode() + colors.hashCode()
        }


        override fun toString(): String {
            return "MTLShader(asset=${asset}, textures=$textures, colors=$colors)"
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            if (!super.equals(other)) return false

            other as MTLShader

            if (colors != other.colors) return false

            return true
        }
    }

}