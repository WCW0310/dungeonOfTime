package camera

import camera.MapInformation.mapInfo
import core.GameKernel
import utils.Global.SCREEN_X
import utils.Global.SCREEN_Y
import java.awt.*
import java.awt.event.KeyEvent
import java.awt.geom.Point2D


class SpotLight (tmp: Camera, var radius: Float, var windowLeft:Int,
                 var windowTop:Int,var windowRight:Int, var windowBottom:Int) : Camera(tmp) {
    // radius 漸層的半徑 -> 可以透過這邊的大小來改變亮度  越小越聚焦

    var dist = floatArrayOf(0.0f, 0.8f, 1.0f)
    // 這裡放0~1  代表著改變的幅度，0是中心點  1是終點，每個對應一個顏色 要照順序0~1 不能跳
// ex: 0.0f, 0.5f, 1.0f  這裡有幾個，下面就要有幾個顏色
    var colors = arrayOf(
            Color(0, 0, 0, 0),
            Color(0, 0, 0, 150),
            Color(0, 0, 0, 255))
    // 這裡對應著dist 要放的漸層顏色

    override fun start(g: Graphics) {
        val g2d = g as Graphics2D
        tmpCanvas = g2d.transform // 暫存畫布
        // 先將畫布初始移到(0,0) (-painter.left/top) 然後再到自己想定位的地點(+ cameraWindowX. Y)
        if (painter.left < windowLeft){
            g2d.translate(-painter.left + windowLeft, 0)
        }
        if (painter.right > windowRight) {  // 要在減painter.width / height 因為是要訂位到左上角
            g2d.translate(-painter.left + windowRight - painter.width, 0)
        }
        if (painter.top < windowTop){
            g2d.translate(0, -painter.top + windowTop)
        }
        if (painter.bottom > windowBottom) {
            g2d.translate(0, -painter.top + windowBottom - painter.height)
        }
        // 將畫布依照鏡頭大小或形狀作裁切，如果要取消，就將裡面改成null即可）
        g2d.setClip(painter.left, painter.top, painter.width, painter.height)
    }

    override fun end(g: Graphics) {
        val g2d = g as Graphics2D
        // 畫出漸層濾鏡
        val target: Point2D = Point2D.Double(painter.centerX.toDouble(), painter.centerY.toDouble()) // 從鏡頭中心點往外漸層
        val paint: Paint = RadialGradientPaint(target, radius, dist, colors) // 創建漸層屬性
        g2d.paint = paint // 設定漸層
        g2d.fillRect(painter.left, painter.top, painter.width, painter.height) //要畫的大小及形狀（跟切的形狀相同即可）
        g2d.transform = tmpCanvas // 將畫布移回原位
        g.setClip(null) //把畫布的裁切還原。
    }

    override val touchTop: Boolean
        get() = collider.centerY <= mapInfo()?.left ?:0

    override val touchLeft: Boolean
        get() = collider.centerX <= mapInfo()?.top ?:0

    override val touchRight: Boolean
        get() = collider.centerX >= mapInfo()?.right ?:SCREEN_X

    override val touchBottom: Boolean
        get() = collider.centerY >= mapInfo()?.bottom ?: SCREEN_Y

    override val input: ((GameKernel.Input.Event) -> Unit)? = { e ->
        run{
            when(e){
                is GameKernel.Input.Event.KeyKeepPressed -> {
                    //無追焦時啟用自由移動鏡頭
                    if (obj() == null) {
                        when (e.data.keyCode) {
                            KeyEvent.VK_W -> if (!touchTop) {
                                translate(y = -cameraMoveSpeed())
                            }
                            KeyEvent.VK_S -> if (!touchBottom) {
                                translate(y = cameraMoveSpeed())
                            }
                            KeyEvent.VK_A -> if (!touchLeft) {
                                translate(x = -cameraMoveSpeed())
                            }
                            KeyEvent.VK_R -> if (!touchRight) {
                                translate(x = cameraMoveSpeed())
                            }
                        }
                    }
                    when (e.data.keyCode){
                        KeyEvent.VK_O -> if(radius < 300.0f){radius += 10.0f}
                        KeyEvent.VK_P -> if(radius > 30.0f){radius -= 10.0f}
                    }
                }
                else -> {}
            }
        }
    }

}
