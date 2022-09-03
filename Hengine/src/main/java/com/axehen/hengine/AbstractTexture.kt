package com.axehen.hengine

import android.graphics.Bitmap
import android.opengl.GLES31.*
import java.lang.IllegalArgumentException
import java.nio.ByteBuffer
import java.nio.IntBuffer


abstract class AbstractTexture(val uniform: String, var id: Int, protected val format: Int, protected val width: Int, protected val height: Int) {


    class Texture(bitmap: Bitmap, uniform: String): AbstractTexture(uniform, -1, GL_RGBA, bitmap.width, bitmap.height) {
        private val buffer = getBitmapBuffer(bitmap)

        /**
         * Loads texture into GPU memory
         * @param slot The texture slot (GL_TEXTURE0 + i) that the texture uses in GPU memory
         **/
        fun load(slot: Int) {

            //Generate texture ID
            IntBuffer.allocate(1).let { buffer ->
                glGenTextures(1, buffer)
                id = buffer[0]
            }

            glActiveTexture(slot)

            glBindTexture(GL_TEXTURE_2D, id)

            glTexParameteri(
                GL_TEXTURE_2D,
                GL_TEXTURE_MIN_FILTER,
                GL_NEAREST
            )
            glTexParameteri(
                GL_TEXTURE_2D,
                GL_TEXTURE_MAG_FILTER,
                GL_NEAREST
            )

            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT)

            glTexImage2D(
                GL_TEXTURE_2D,
                0,
                format,
                width,
                height,
                0,
                format,
                GL_UNSIGNED_BYTE,
                buffer
            )

            glGenerateMipmap(GL_TEXTURE_2D)
        }
    }

    /**
     * @param bitmaps An array of 6 face bitmaps
     * @param uniform The shader uniform to which hte cubeMap is mapped
     */
    class CubeMap(bitmaps: Array<Bitmap>, uniform: String): AbstractTexture(uniform, -1, GL_RGBA, bitmaps[0].width, bitmaps[0].height) {
        private val buffers: Array<ByteBuffer> = Array(6) { getBitmapBuffer(bitmaps[it]) }

        init {
            // Validate the provided bitmap set
            if (bitmaps.size != 6) throw IllegalArgumentException("6 bitmaps are required when initializing a CubeMap, however ${bitmaps.size} were provided")
            if(bitmaps[0].height != bitmaps[0].width)  throw IllegalArgumentException("CubeMap bitmaps are not square")
            for(i in 0 until bitmaps.size-1) {
                val bmp1 = bitmaps[i]
                val bmp2 = bitmaps[i+1]
                if(bmp1.width  != bmp2.width)  throw IllegalArgumentException("CubeMap bitmaps are not the same width")
                if(bmp1.height != bmp2.height) throw IllegalArgumentException("CubeMap bitmaps are not the same height")
            }
        }

        fun load() {
            with(IntBuffer.allocate(1)) {
                glGenTextures(1, this)
                id = this[0]
            }

            glBindTexture(GL_TEXTURE_CUBE_MAP, id)

            glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MAG_FILTER, GL_LINEAR)
            glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MIN_FILTER, GL_LINEAR)

            glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE)
            glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE)
            glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE)

            for(i in buffers.indices) {

                glTexImage2D(
                    GL_TEXTURE_CUBE_MAP_POSITIVE_X + i,
                    0,
                    format,
                    width,
                    height,
                    0,
                    format,
                    GL_UNSIGNED_BYTE,
                    buffers[i]
                )
            }

        }
    }

    companion object {
        fun getBitmapBuffer(bitmap: Bitmap): ByteBuffer {
            return with(bitmap) {
                ByteBuffer.allocateDirect(this.allocationByteCount).also {
                    this.copyPixelsToBuffer(it)
                    it.position(0)
                }
            }
        }
    }
}