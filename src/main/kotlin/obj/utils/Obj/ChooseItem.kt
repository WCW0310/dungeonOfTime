package obj.utils.Obj

import core.controllers.ResController
import obj.utils.GameObject
import utils.Path
import java.awt.Graphics
import java.awt.Image

class ChooseItem(var centenX: Int, var centenY: Int, var width: Int, var height: Int, val type: ItemType) :
        GameObject(centenX, centenY, width, height, centenX, centenY, width, height) {

    enum class ItemType(val img: Image) {
        GEM_RED(ResController.instance.image(Path.Imgs.Objs.GEM_RED)),
        GEM_BLUE(ResController.instance.image(Path.Imgs.Objs.GEM_BLUE)),
        GEM_GREEN(ResController.instance.image(Path.Imgs.Objs.GEM_GREEN)),
        GEM_YELLOW(ResController.instance.image(Path.Imgs.Objs.GEM_YELLOW)),
        MISSION1(ResController.instance.image(Path.Imgs.Objs.MISSION1)),
        MISSION2(ResController.instance.image(Path.Imgs.Objs.MISSION2))
    }

    override fun paintComponent(g: Graphics) {
        g.drawImage(type.img, painter.left, painter.top, painter.width, painter.height, null)
    }

    override fun update(timePassed: Long) {
    }

    fun setPosition(x: Int, y: Int) {
        painter.setCenter(x, y)
        collider.setCenter(x, y)
    }

}