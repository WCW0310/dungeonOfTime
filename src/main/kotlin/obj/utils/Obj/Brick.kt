package obj.utils.Obj

import core.controllers.ResController
import obj.utils.GameObject
import utils.Path
import java.awt.Graphics
import java.awt.Image

class Brick (var centenX:Int, var centenY:Int, var width:Int, var height:Int, val brickType: BrickType):
        GameObject(centenX, centenY, width, height, centenX, centenY, width, height) {

    enum class BrickType(val img: Image){
        CEILING(ResController.instance.image(Path.Imgs.Objs.CEILING)),
        CONNER(ResController.instance.image(Path.Imgs.Objs.CONNER)),
        FLOOR(ResController.instance.image(Path.Imgs.Objs.FLOOR)),
        PLAT1(ResController.instance.image(Path.Imgs.Objs.PLAT1)),
        PLAT2(ResController.instance.image(Path.Imgs.Objs.PLAT2)),
        PLAT3(ResController.instance.image(Path.Imgs.Objs.PLAT3)),
        PLAT4(ResController.instance.image(Path.Imgs.Objs.PLAT4)),
        ROCKPLAT1(ResController.instance.image(Path.Imgs.Objs.ROCKPLAT1)),
        ROCKPLAT2(ResController.instance.image(Path.Imgs.Objs.ROCKPLAT2)),
        ROCKPLAT3(ResController.instance.image(Path.Imgs.Objs.ROCKPLAT3)),
        ROCKPLAT4(ResController.instance.image(Path.Imgs.Objs.ROCKPLAT4)),
        ROCKPLAT5(ResController.instance.image(Path.Imgs.Objs.ROCKPLAT5)),
        ROCKPLAT6(ResController.instance.image(Path.Imgs.Objs.ROCKPLAT6)),
        ROCKTOP(ResController.instance.image(Path.Imgs.Objs.ROCKTOP)),
        ROCKBOTTOM(ResController.instance.image(Path.Imgs.Objs.ROCKBOTTOM)),
        ROCKLR(ResController.instance.image(Path.Imgs.Objs.ROCKLR)),
        WOODPLAT1(ResController.instance.image(Path.Imgs.Objs.WOODPLAT1)),
        WOODPLAT2(ResController.instance.image(Path.Imgs.Objs.WOODPLAT2)),
        WOODPLAT3(ResController.instance.image(Path.Imgs.Objs.WOODPLAT3)),
        WOODPLAT4(ResController.instance.image(Path.Imgs.Objs.WOODPLAT4)),
        WOODPLAT5(ResController.instance.image(Path.Imgs.Objs.WOODPLAT5)),
        WOODPLAT6(ResController.instance.image(Path.Imgs.Objs.WOODPLAT6)),
        WOODTOP(ResController.instance.image(Path.Imgs.Objs.WOODTOP)),
        WOODBOTTOM(ResController.instance.image(Path.Imgs.Objs.WOODBOTTOM)),
        WOODLR(ResController.instance.image(Path.Imgs.Objs.WOODLR));
    }

    override fun paintComponent(g: Graphics) {
        g.drawImage(brickType.img, painter.left, painter.top, painter.width, painter.height ,null)
    }

    override fun update(timePassed: Long) {
    }
}