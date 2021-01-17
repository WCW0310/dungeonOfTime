package core.controllers

import java.awt.Image
import javax.imageio.ImageIO


class ResController private constructor() {
    private val map: MutableMap<String, Image> = mutableMapOf()
    private val keepMap: MutableMap<String, Image> = mutableMapOf()
    private var isKeep = false

    fun image(path: String): Image {
        return if (isKeep) {
            keepMap
        } else {
            map
        }.run {
            getOrElse(path){
                val img: Image = ImageIO.read(javaClass.getResource(path))
                this[path] = img
                return img
            }
        }
    }

    fun keep() {
        isKeep = true
    }

    fun release() {
        map.putAll(keepMap)
        drop()
    }

    fun drop() {
        keepMap.clear()
        isKeep = false
    }

    fun clear() {
        map.clear()
    }

    companion object {
        val instance: ResController by lazy { ResController() }
    }
    /*
    companion object 代表著 伴生物件 代表著static 的狀態
    by 是委派，不好理解，先忽略，用來做封裝類別用的。
    因此每個物件如果只是要static的 不管是屬性還是function就放在
    companion object 的大括號內，及為static屬性、方法
     */
}
