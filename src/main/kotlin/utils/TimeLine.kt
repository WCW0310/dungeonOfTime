package utils

import core.controllers.ResController
import core.utils.Delay
import obj.utils.Actor.Actor
import obj.utils.GameObject
import utils.Global.WINDOW_WIDTH
import utils.Global.isTeam
import java.awt.Color
import java.awt.Graphics
import java.awt.Image

class TimeLine(var left: Int, var top: Int, var width: Int, var height: Int,
               var mission: Int) {

    var count = 0.0
    var max = 0.0
    var delay = Delay(60)
    var scrollTen = ResController.instance.image(Path.Imgs.Objs.SCROLLTEN)
    var scrollTime = ResController.instance.image(Path.Imgs.Objs.SCROLLTIME)
    var scrollBattle = ResController.instance.image(Path.Imgs.Objs.SCROLLBATTLE)
    var scrollGhost = ResController.instance.image(Path.Imgs.Objs.SCROLLGHOST)
    var bullet: Bullet? = null
    var isRed = false
    var isBlue = false

    init {
        if (mission == 2) {
            count = 3600.0
            max = 3600.0
        }
        if (mission == 3 || mission == 4) {
            count = 15000.0
            max = 30000.0
        }
    }

    inner class Bullet(centerX: Int, val isLeft: Boolean) : GameObject(centerX, height / 2, 20, 8) {

        var img: Image = if (isLeft) ResController.instance.image(Path.Imgs.Objs.BULLETLEFT)
        else ResController.instance.image(Path.Imgs.Objs.BULLETRIGHT)

        override fun paintComponent(g: Graphics) {
            g.color = Color.green
            g.drawImage(img, painter.left, painter.top, 20, 8, null)
            g.color = Color.black
        }

        override fun update(timePassed: Long) {
            if (painter.left < left + middle) {
                translate(x = middle / 60)
            } else if (painter.left > left + middle) {
                translate(x = -(width - middle) / 60)
            }
        }
    }

    val middle: Int
        get() = (count / max * width).toInt()

    fun update(timePassed: Long) {
        if (mission == 2) {
            count -= 1
        }
    }//網路連線混戰模式

    fun update(timePassed: Long, redKey: Int, blueKey: Int) {
        when (mission) {
            2 -> count -= 1
            3 -> compare(redKey, blueKey)
            4 -> compare(redKey, blueKey)
        }
        bullet?.update(timePassed)
    }//網路連線隊伍模式

    fun update(timePassed: Long, player1: Actor, player2: Actor) {
        when (mission) {
            2 -> count -= 1
            3 -> compare(player1.key, player2.key)
        }
        bullet?.update(timePassed)
    }// 雙人單機模式

    fun compare(key1: Int, key2: Int) {
        if (key1 > key2) {
            count += 1 * key1
            isRed = true
            isBlue = false
            if (bullet == null || (bullet != null && bullet!!.painter.left > left + middle)) {
                bullet = Bullet(left + 10, true)
            }
        } else if (key1 < key2) {
            count -= 1 * key2
            isRed = false
            isBlue = true
            if (bullet == null || (bullet != null && bullet!!.painter.left < left + middle)) {
                bullet = Bullet(left + width - 10, false)
            }
        } else {
            bullet = null
        }
        if (bullet != null) {
            if (isRed && bullet!!.painter.right > left + middle) {
                bullet = null
            } else if (isBlue && bullet!!.painter.left < left + middle) {
                bullet = null
            }
        }

    }

    fun paint(g: Graphics) {
        if (mission != 1) {
            g.color = Color.GREEN
            g.drawRect(left, top, width, height)
            g.color = Color.RED
            g.fillRect(left + 1, top + 1, middle, height - 1)
            g.color = Color.BLUE
            g.fillRect(middle + left + 1, top + 1, width - middle - 1, height - 1)
            g.color = Color.black
            bullet?.paint(g)
        }
        when (mission) {
            1 -> g.drawImage(scrollTen, WINDOW_WIDTH / 2 - 100, 0, 200, 100, null)
            2 -> g.drawImage(scrollTime, WINDOW_WIDTH / 2 - 100, 16, 200, 100, null)
            3 -> g.drawImage(scrollBattle, WINDOW_WIDTH / 2 - 100, 16, 200, 100, null)
            4 -> g.drawImage(scrollGhost, WINDOW_WIDTH / 2 - 100, 16, 200, 100, null)
        }
    }

}