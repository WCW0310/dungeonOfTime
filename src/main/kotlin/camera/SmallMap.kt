package camera


import obj.utils.GameObject
import utils.Global.UNIT_Y
import java.awt.Color
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.Image


class SmallMap(cam: Camera, smallMapZoomX: Double, smallMapZoomY: Double) : Camera(cam) {
    private var smallMapZoomX : Double// 小地圖的X縮放率
    private var smallMapZoomY : Double// 小地圖的Y縮放率

    //顯示全圖：鏡頭大小與地圖大小相同，不要setChaseObj，並且要setCameraStartLocation將鏡頭起始位置設成(0,0)的位置
    //不顯示全圖：鏡頭大小自訂，設成要畫出來的大小，並且要setChaseObj。

    init {
        this.smallMapZoomX = smallMapZoomX
        this.smallMapZoomY = smallMapZoomY
        setChaseDivisorX(1.0)
        setChaseDivisorY(1.0)
    }

    /*
   以下paint為提供常用的方法overload目前除放入圖片外，皆是畫長方形，
   如果要改成其他形狀，請利用Paint介面自定義要怎麼paint。
   如果有自定義物件畫出大小時，由於鏡頭視野框線，是以該物件實際所在的中心點畫出，
   因此小地圖上的物件可能不在鏡頭中心點。
   */

    interface Paint {   // 可以使用這個接口自定義paint 的方法
        fun paint(g: Graphics, obj: GameObject)
    }

    //將物件轉換成方格畫出，自動將該類型物件生成相同顏色，大小依物件縮放
    fun paint(g: Graphics, obj: GameObject) {
        val c = getColor("" + obj.javaClass)
        g.color = c
        g.fillRect(obj.painter.left, obj.painter.top,
                obj.painter.width, obj.painter.height)
        g.color = Color.BLACK
    }
    //將物件轉換成方格畫出，自動將該類型物件生成相同顏色，大小自訂
    fun paint(g: Graphics, obj: GameObject, width: Int, height: Int) {
        val c = getColor("" + obj.javaClass)
        g.color = c
        g.fillRect(obj.painter.left, obj.painter.top,
                width, height)
        g.color = Color.BLACK
    }
    //將物件轉換成方格畫出，顏色自訂，大小依物件縮放
    fun paint(g: Graphics, obj: GameObject, c: Color?) {
        g.color = c
        g.fillRect(obj.painter.left, obj.painter.top,
                obj.painter.width, obj.painter.height)
        g.color = Color.BLACK
    }
    //將物件轉換成方格畫出，顏色自訂
    fun paint(g: Graphics, obj: GameObject, c: Color?, width: Int, height: Int) {
        g.color = c
        g.fillRect(obj.painter.left, obj.painter.top,
                width, height)
        g.color = Color.BLACK
    }
    //將物件轉換成圓畫出，顏色自訂
    fun paint(g: Graphics, obj: GameObject, c: Color?, width: Int) {
        g.color = c
        g.fillOval(obj.painter.left, obj.painter.top - UNIT_Y * 2,// 讓圖示跟地形不會相交
                width, width)
        g.color = Color.BLACK
    }
    // 小地圖的物件顯示圖片（可以相同也可以自己放），大小依照物件本身縮小
    fun paint(g: Graphics, obj: GameObject, img: Image?) {
        g.drawImage(img, obj.painter.left, obj.painter.top,
                obj.painter.width, obj.painter.height, null)
    }
    // 小地圖的物件顯示圖片（可以相同也可以自己放），大小自訂
    fun paint(g: Graphics, obj: GameObject, img: Image?, width: Int, height: Int) {
        g.drawImage(img, obj.painter.left, obj.painter.top, width, height, null)
    }

    //畫追蹤物件的鏡頭框大小，camera 請放追蹤物件的主鏡頭，將在小地圖上顯示目前主鏡頭可見的範圍。
    fun paint(g: Graphics, camera: Camera, color: Color?) {
        g.color = color
        var targetX: Int = camera.obj()!!.painter.centerX - camera.painter.width / 2
        var targetY: Int = camera.obj()!!.painter.centerY - camera.painter.height / 2
        if (targetX < painter.left) {
            targetX = painter.left
        }
        if (targetY < painter.top) {
            targetY = painter.top
        }
        if (targetX > painter.right - camera.painter.width) {
            targetX = painter.right - camera.painter.width
        }
        if (targetY > painter.bottom - camera.painter.height) {
            targetY = painter.bottom - camera.painter.height
        }
        g.drawRect(targetX, targetY,
                camera.painter.width, camera.painter.height)
        g.color = Color.BLACK
    }

    /*使用時，請在場景的paint方法中
    1.smallMap.start(g) //將畫布移動到您的顯示視窗範圍(0,0)
    2.放入您的物件(請讓每個物件與smallMap做isCollision碰撞判斷，有重疊到才paint)
    EX: if(smallMap.isCollision(ac)){  // 如果只要主鏡頭內才顯示，就把smallMap 改成主鏡頭
            smallMap.paint(g,ac);
        }
    3. smallMap.end(g) 將畫布移回原位
    4. 如果有第二顆camera 再次操作 1 ~ 3。
    */
    override fun start(g: Graphics) {
        val g2d = g as Graphics2D //Graphics2D 為Graphics的子類，先做向下轉型。
        tmpCanvas = g2d.transform // 暫存畫布
        g2d.scale(smallMapZoomX, smallMapZoomY) // 將畫布整體做縮放 ( * 縮放的係數)
        // 先將畫布初始移到(0,0) 然後再到自己想定位的地點(+ cameraWindowX. Y)，因為有被縮放的話要將為位移點調整-> (/ 縮放的係數)
        g2d.translate(-painter.left + cameraWindowX() / smallMapZoomX, -painter.top + cameraWindowY() / smallMapZoomY)
        // 將畫布依照鏡頭大小作裁切
        g.setClip(painter.left, painter.top, painter.width, painter.height)
    }

    fun getColor(str: String): Color {
        var colorCode = 0
        val arr = str.toCharArray()
        for (i in arr.indices) {
            colorCode += arr[i].toInt()
        }
        val colorCodeArr = IntArray(3)
        val temp = intArrayOf(colorCode % 255, colorCode % 175, colorCode % 90) // 裝可能性，將得到的數字分不同參數做mod運算，以獲得差異較大的色碼
        for (i in 0..1) {
            colorCodeArr[i] = temp[i]
        }
        return Color(colorCodeArr[0], colorCodeArr[1], colorCodeArr[2])
    }

    fun setSmallMapZoomX(num: Double) {
        smallMapZoomX = num
    }
    fun smallMapZoomX(): Double {
        return smallMapZoomX
    }
    fun setSmallMapZoomY(num: Double) {
        smallMapZoomY = num
    }
    fun smallMapZoomY(): Double {
        return smallMapZoomY
    }

    override fun paintComponent(g: Graphics) {
        g.color = Color.BLUE
        g.drawRect(collider.left, collider.top, collider.width, collider.height)
        g.color = Color.BLACK
    }

}

