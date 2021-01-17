package obj.utils

import camera.MapInformation.mapInfo
import core.GameKernel
import core.GameKernel.GameInterface
import utils.Global
import utils.Global.SCREEN_X
import utils.Global.SCREEN_Y
import java.awt.Color
import java.awt.Graphics


abstract class GameObject(val collider: Rect, val painter: Rect = Rect(collider)) : GameInterface {

    constructor(
        x: Int, y: Int, width: Int, height: Int,
        x2: Int = x, y2: Int = y, width2: Int = width, height2: Int = height
    ) : this(
        collider = Rect.genWithCenter(x, y, width, height),
        painter = Rect.genWithCenter(x2, y2, width2, height2)
    ) // 第二個建構子，如果有吻合這個條件就呼叫這個，

    val outOfScreen: Boolean
        get() {
            if (painter.bottom <= 0) {
                return true
            }
            if (painter.right <= 0) {
                return true
            }
            return if (painter.left >= SCREEN_X) {
                true
            } else {
                painter.top >= SCREEN_Y
            }
        }

    open val touchTop: Boolean
        get() = collider.top <= mapInfo()?.top ?:0

    open val touchLeft: Boolean
        get() = collider.left <= mapInfo()?.left ?: 0

    open val touchRight: Boolean
        get() = collider.right >= mapInfo()?.right ?:SCREEN_X

    open val touchBottom: Boolean
        get() = collider.bottom >= mapInfo()?.bottom ?: SCREEN_Y

    fun isCollision(obj: GameObject): Boolean {
        return collider.overlap(obj.collider)
    }

    // 因為有改寫，所以可以直接一個物件跟整個陣列比，然後回傳一個清單回去
    fun <T : GameObject> isCollision(obj: List<T>): List<T> {
        return obj.filter { it.collider.overlap(collider) }
    }

    fun translate(x: Int = 0, y: Int = 0) {
        collider.translate(x, y)
        painter.translate(x, y)
    }

    override fun paint(g: Graphics) {
        paintComponent(g)
        if (Global.IS_DEBUG) {
            g.color = Color.RED
            g.drawRect(painter.left, painter.top, painter.width, painter.height)
            g.color = Color.BLUE
            g.drawRect(collider.left, collider.top, collider.width, collider.height)
            g.color = Color.BLACK
        }
    }

    override val input: ((GameKernel.Input.Event) -> Unit)? = null

    abstract fun paintComponent(g: Graphics)
}