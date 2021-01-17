package camera

import core.GameKernel
import obj.utils.GameObject
import utils.Global.SCREEN_X
import utils.Global.SCREEN_Y
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.event.KeyEvent.*
import java.awt.geom.AffineTransform


open class Camera : GameObject{
    //地圖資訊由 MapInformation 類別直接呼叫，可以進行將相關資訊設定到Global 其實只需要地圖的寬跟長即可
    //相機設定
    private var cameraMoveSpeed : Int//鏡頭移動速度
    private var chaseDivisorX : Double // 追焦時X軸要除的值，越小追越快
    private var chaseDivisorY : Double  // 追焦時Y軸要除的值，越小追越快
    private var obj : GameObject? = null  //要跟焦的對象，如果null 就代表不用跟畫面可自由移動
    open var tmpCanvas : AffineTransform? = null  // 暫存畫布移動前的位置
    private var lockLeft = false
    private var lockRight = false
    private var lockUp = false
    private var lockDown = false
    private var cameraWindowX : Int  // 此顆鏡頭實際在畫面的左上角X座標
    private var cameraWindowY : Int  // 此顆鏡頭實際在畫面的左上角Y座標

    class Builder(width: Int, height: Int) {
        private val tmp: Camera = Camera(width, height)

        fun setChaseObj(obj: GameObject?): Builder {
            tmp.setObj(obj)
            return this
        }
        fun setChaseObj(obj: GameObject?, chaseDivisorX: Double, chaseDivisorY: Double): Builder {
            tmp.setObj(obj)
            tmp.setChaseDivisorX(chaseDivisorX)
            tmp.setChaseDivisorY(chaseDivisorY)
            return this
        }
        fun setCameraMoveSpeed(num: Int): Builder {
            tmp.setCameraMoveSpeed(num)
            return this
        }
        fun setCameraStartLocation(left: Int, top: Int): Builder {
            tmp.translate(left - tmp.painter.left, top - tmp.painter.top)
            return this
        }
        fun setCameraWindowLocation(left: Int, top: Int): Builder {
            tmp.setCameraWindowX(left)
            tmp.setCameraWindowY(top)
            return this
        }
        fun setCameraLockDirection(left: Boolean, up: Boolean, right: Boolean, down: Boolean): Builder {
            tmp.lockLeft(left)
            tmp.lockUp(up)
            tmp.lockRight(right)
            tmp.lockDown(down)
            return this
        }
        fun gen(): Camera {
            return Camera(tmp)
        }
    }

    private constructor(width: Int, height: Int) : super(0, 0, width, height, 0, 0, width, height) {
        cameraWindowX = 0
        cameraWindowY = 0
        cameraMoveSpeed = 10
        chaseDivisorX = 20.0
        chaseDivisorY = 20.0
    }

    constructor(tmp: Camera) : super(tmp.collider.centerX, tmp.collider.centerY, tmp.collider.width, tmp.collider.height,
            tmp.painter.centerX, tmp.painter.centerY, tmp.painter.width, tmp.painter.height) {
        setObj(tmp.obj())
        cameraMoveSpeed = tmp.cameraMoveSpeed
        chaseDivisorX = tmp.chaseDivisorX
        chaseDivisorY = tmp.chaseDivisorY
        cameraWindowX = tmp.cameraWindowX
        cameraWindowY = tmp.cameraWindowY
        lockLeft = tmp.lockLeft
        lockRight = tmp.lockRight
        lockUp = tmp.lockUp
        lockDown = tmp.lockDown
    }

    fun resetX(left: Int, right: Int) {
        collider.left = left
        painter.left = left
        collider.right = right
        painter.right = right
    }
    fun resetY(top: Int, bottom: Int) {
        collider.top = top
        painter.top = top
        collider.bottom = bottom
        painter.bottom = bottom
    }
    fun setObj(obj: GameObject?) {
        this.obj = obj
        if (this.obj != null) {
            var left: Int = obj!!.painter.centerX - painter.width / 2
            var right: Int = obj.painter.centerX + painter.width / 2
            var top: Int = obj.painter.centerY - painter.height / 2
            var bottom: Int = obj.painter.centerY + painter.height / 2
            if (touchLeft) {
                left = MapInformation.mapInfo()?.left ?:0
                right = left + painter.width
            }
            if (touchRight) {
                right = MapInformation.mapInfo()?.right ?: SCREEN_X
                left = right - painter.width
            }
            if (touchTop) {
                top = MapInformation.mapInfo()?.top ?:0
                bottom = top + painter.height
            }
            if (touchBottom) {
                bottom = MapInformation.mapInfo()?.bottom ?: SCREEN_Y
                top = bottom - painter.height
            }
            resetX(left, right)
            resetY(top, bottom)
        }
    }

    fun chaseMove() { //鏡頭追蹤加速度 數字越大追越慢
        val targeX: Double = (obj!!.painter.centerX - painter.centerX) / chaseDivisorX
        val targeY: Double = (obj!!.painter.centerY - painter.centerY) / chaseDivisorY
        if (targeX > 0 && !touchRight && !lockRight) {
            translate(x = targeX.toInt())
        } else if (targeX < 0 && !touchLeft && !lockLeft) {
            translate(x = targeX.toInt())
        }
        if (targeY > 0 && !touchBottom && !lockDown) {
            translate(y = targeY.toInt())
        } else if (targeY < 0 && !touchTop && !lockUp) {
            translate(y = targeY.toInt())
        }
    }

    /*使用時，請在場景的paint方法中
    1.camera.start(g) //將畫布移動到您的顯示視窗範圍(0,0)
    2.放入您的物件(請讓每個物件與camera做isCollision碰撞判斷，有重疊到才paint)
    EX: if(camera.isCollision(ac)){
            ac.paint(g);
        }
    3. camera.end(g) 將畫布移回原位
    4. 如果有第二顆camera 再次操作 1 ~ 3。 // 小地圖一樣要放入各物件，只是縮小放置而已～
    */
    open fun start(g: Graphics) {
        val g2d = g as Graphics2D
        tmpCanvas = g2d.transform // 暫存畫布
        // 先將畫布初始移到(0,0)（-painter.left/top) 然後再到自己想定位的地點(+ cameraWindowX/Y)
        g2d.translate(-painter.left + cameraWindowX ,  -painter.top + cameraWindowY )
        // 將畫布依照鏡頭大小作裁切
        g.setClip(painter.left, painter.top, painter.width, painter.height)
    }

    open fun end(g: Graphics) {
        val g2d = g as Graphics2D
        g2d.transform = tmpCanvas // 將畫布移回原位
        g.setClip(null) //把畫布的裁切還原。
    }

    override fun paintComponent(g: Graphics) {}
    override fun update(timePassed: Long) {
        if (obj != null) {
            chaseMove() // 追焦功能
        }
    }

    override val input: ((GameKernel.Input.Event) -> Unit)? = {e ->
        run{
            when(e){
                is GameKernel.Input.Event.KeyKeepPressed -> {
                    //無追焦時啟用自由移動鏡頭
                    if (obj == null) {
                        when (e.data.keyCode) {
                            VK_W -> if (!touchTop) {
                                translate(y = -cameraMoveSpeed)
                            }
                            VK_S -> if (!touchBottom) {
                                translate(y = cameraMoveSpeed)
                            }
                            VK_A -> if (!touchLeft) {
                                translate(x = -cameraMoveSpeed)
                            }
                            VK_R -> if (!touchRight) {
                                translate(x = cameraMoveSpeed)
                            }
                        }
                    }
                }
            }
        }
    }


    fun cameraMoveSpeed(): Int{
        return cameraMoveSpeed
    }
    fun setCameraMoveSpeed(num: Int) {
        cameraMoveSpeed = num
    }
    fun setChaseDivisorX(num: Double) {
        chaseDivisorX = num
    }
    fun setChaseDivisorY(num: Double) {
        chaseDivisorY = num
    }
    fun obj(): GameObject? {
        return obj
    }
    fun lockLeft(lock: Boolean) {
        lockLeft = lock
    }
    fun lockRight(lock: Boolean) {
        lockRight = lock
    }
    fun lockUp(lock: Boolean) {
        lockUp = lock
    }
    fun lockDown(lock: Boolean) {
        lockDown = lock
    }
    fun setCameraWindowX(num: Int) {
        cameraWindowX = num
    }
    fun cameraWindowX(): Int {
        return cameraWindowX
    }
    fun setCameraWindowY(num: Int) {
        cameraWindowY = num
    }
    fun cameraWindowY(): Int {
        return cameraWindowY
    }

}
