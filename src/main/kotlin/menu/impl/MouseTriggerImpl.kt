package menu.impl

import core.GameKernel
import menumodule.menu.Button
import menumodule.menu.Label
import menumodule.menu.Style.StyleOval
import java.awt.event.KeyEvent
import kotlin.math.pow
import kotlin.math.sqrt

//選滑鼠滑進去、點擊功能擇一
//外面輸入xy(滑鼠該有反應的位置)
//使用者可以改變滑進去、點擊呈現出的style自訂
object MouseTriggerImpl {

    private fun ovalOverlap(obj: Label, eX: Int, eY: Int): Boolean {
        val r = sqrt((obj.width() / 2.toDouble()).pow(2.0) - (obj.height() / 2.toDouble()).pow(2.0)).toInt()
        val r1X = obj.x + obj.width() / 2 - r
        val r2X = obj.x + obj.width() / 2 + r
        val rY = obj.y + obj.height() / 2
        val threePointDistance = (sqrt((r1X - eX.toDouble()).pow(2.0) + (rY - eY.toDouble()).pow(2.0)) + Math.sqrt(Math.pow(r2X - eX.toDouble(), 2.0) + Math.pow(rY - eY.toDouble(), 2.0))).toInt()
        return threePointDistance <= obj.width()
    }

    private fun rectOverlap(obj: Label, eX: Int, eY: Int): Boolean {
        return eX <= obj.right() && eX >= obj.left() && eY >= obj.top() && eY <= obj.bottom()
    }

    fun mouseTrig(obj: Label, e: GameKernel.Input.Event) {
        val isOval = obj.paintStyle is StyleOval
        when (e) {
            is GameKernel.Input.Event.MouseReleased -> {
                if (obj is Button) {
                    obj.unFocus()
                }
            }
            is GameKernel.Input.Event.MouseMoved -> {
                if (isOval) {
                    if (ovalOverlap(obj, e.data.x, e.data.y)) {
                        obj.isHover()
                    } else {
                        obj.unHover()
                    }
                } else {
                    if (rectOverlap(obj, e.data.x, e.data.y)) {
                        obj.isHover()
                    } else {
                        obj.unHover()
                    }
                }
            }
            is GameKernel.Input.Event.MousePressed -> {
                if (isOval) {
                    if (ovalOverlap(obj, e.data.x, e.data.y)) {
                        obj.isFocus()
                        if (obj.clickedAction != null) {
                            obj.clickedActionPerformed()
                        }
                    } else {
                        obj.unFocus()
                    }
                } else {
                    if (rectOverlap(obj, e.data.x, e.data.y)) {
                        obj.isFocus()
                        if (obj.clickedAction != null) {
                            obj.clickedActionPerformed()
                        }
                    } else {
                        obj.unFocus()
                    }
                }
            }
            is GameKernel.Input.Event.KeyPressed -> {
                if (isOval) {
                    if (e.data.keyCode == KeyEvent.VK_SPACE) {
                        obj.isFocus()
                        if (obj.clickedAction != null) {
                            obj.clickedActionPerformed()
                        }
                    } else {
                        obj.unFocus()
                    }
                } else {
                    if (e.data.keyCode == KeyEvent.VK_SPACE) {
                        obj.isFocus()
                        if (obj.clickedAction != null) {
                            obj.clickedActionPerformed()
                        }
                    } else {
                        obj.unFocus()
                    }
                }
            }
            else -> {
            }
        }

    }
}