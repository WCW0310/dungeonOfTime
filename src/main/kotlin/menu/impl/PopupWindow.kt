package menu.impl

import core.GameKernel
import core.Scene
import java.awt.Graphics
import java.awt.Graphics2D

abstract class PopupWindow(var x: Int, var y: Int, var width: Int, var height: Int) : Scene() {
    var isShow = false
    private var isCancelable = false

    fun setCancelable() {
        isCancelable = true
    }

    fun show() {
        isShow = true
    }

    fun hide() {
        isShow = false
    }

    abstract override fun sceneBegin()
    abstract override fun sceneEnd()
    abstract fun paintWindow(g: Graphics)
    override fun paint(g: Graphics) {
        val g2d = g as Graphics2D
        val tf = g2d.transform
        g2d.translate(x, y)
        paintWindow(g)
        g2d.transform = tf
    }

    abstract override fun update(timePassed: Long)

//    fun mouseListener(obj: Label, e: GameKernel.Input.Event){
//        when (e) {
//            is GameKernel.Input.Event.MouseReleased -> {
//                if (obj is Button) {
//                    obj.unFocus()
//                }
//            }
//            else -> {
//            }
//        }
//    }

    fun mouseListener(): ((GameKernel.Input.Event) -> Unit) {
        return { e ->
            if (isCancelable && e is GameKernel.Input.Event.MousePressed) { //滑鼠點外面他會hide()
                if (e.data.x < x || e.data.x > x + width || e.data.y < y || e.data.y > y + height) {
                    hide()
                }
            }
            when (e){
                is GameKernel.Input.Event.MousePressed -> {
                    e.data.translatePoint(-x,-y)
                }
                is GameKernel.Input.Event.MouseMoved -> {
                    e.data.translatePoint(-x,-y)
                }
                is GameKernel.Input.Event.MouseReleased -> {
                    e.data.translatePoint(-x,-y)
                }
                is GameKernel.Input.Event.MouseDragged -> {
                    e.data.translatePoint(-x,-y)
                }
                is GameKernel.Input.Event.MouseEntered -> {
                    e.data.translatePoint(-x,-y)
                }
                is GameKernel.Input.Event.MouseExited -> {
                    e.data.translatePoint(-x,-y)
                }
                is GameKernel.Input.Event.MouseWheelMoved -> {
                    e.data.translatePoint(-x,-y)
                }
                else -> {

                }
            }
            input?.invoke(e)
            if (!isShow) {
                sceneEnd()
            }
        }
    }

    abstract override val input: ((GameKernel.Input.Event) -> Unit)?

}