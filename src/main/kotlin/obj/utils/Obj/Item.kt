package obj.utils.Obj

import core.controllers.ResController
import core.utils.Delay
import obj.utils.Actor.Actor
import obj.utils.GameObject
import obj.utils.SkillPool
import obj.utils.SkillPool.SkillAnimator
import obj.utils.SkillPool.itemSkill
import utils.Path
import java.awt.Color
import java.awt.Graphics
import java.awt.Image

class Item(var centenX: Int, var centenY: Int, var width: Int, var height: Int, val type: ItemType) :
        GameObject(centenX, centenY, width, height, centenX, centenY, width, height) {

    enum class ItemType(val img: Image) {
        START_RED(ResController.instance.image(Path.Imgs.Objs.START_RED)),
        START_BLUE(ResController.instance.image(Path.Imgs.Objs.START_BLUE)),
        KEY_PURPLE(ResController.instance.image(Path.Imgs.Objs.KEY_PURPLE)),
        TREASURE_BLUE(ResController.instance.image(Path.Imgs.Objs.TREASURE_BLUE)),
    }

    var state: SkillPool.SkillState = SkillPool.SkillState.USEABLE
    var user: Actor? = null

    lateinit var skill: SkillPool.Skill
    lateinit var animator: SkillAnimator
    lateinit var skillKeeptime: Delay
    lateinit var skillCdtime: Delay

    override fun paintComponent(g: Graphics) {
        if (state != SkillPool.SkillState.USEABLE && type == ItemType.TREASURE_BLUE) {
            animator.paint(painter.left, painter.top, painter.right, painter.bottom, g)
            return
        }
        if (state == SkillPool.SkillState.USEABLE) {
            g.drawImage(type.img, painter.left, painter.top, painter.width, painter.height, null)
            if (type == ItemType.TREASURE_BLUE) {
                g.color = Color.RED
            } else {
                g.color = Color.BLUE
            }
            g.drawOval(painter.left, painter.top, painter.width, painter.height)
            g.color = Color.BLACK
        }
    }

    fun update(timePassed: Long, User: Actor, Affected: MutableList<Actor>) {
        if (type == ItemType.TREASURE_BLUE) {
            skill.update(this, User, Affected)
            animator.update()
        }
    }

    override fun update(timePassed: Long) {
    }

    fun setPosition(x: Int, y: Int) {
        painter.setCenter(x, y)
        collider.setCenter(x, y)
    }

    fun addSkill(r:Int){
        skill = itemSkill[r]
        animator = SkillAnimator(skill)
        skillKeeptime = Delay(skill.keepTime)
        skillCdtime = Delay(skill.cdTime)
    }


}