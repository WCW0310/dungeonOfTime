package obj.utils.Actor

import camera.MapInformation.mapInfo
import core.GameKernel
import core.utils.Delay
import network.ClientClass
import obj.utils.GameObject
import obj.utils.SkillPool
import utils.Global
import utils.Global.Command.INPUT
import utils.Global.Command.bale
import utils.Global.IS_DEBUG
import utils.Path
import java.awt.Color
import java.awt.Graphics


open class Actor(centerX: Int, centerY: Int, width: Int, height: Int, val char: CharacterPool.Character, private val keySet: MutableList<Int>?, var id: Int, var name: String?, var team: Team?) :
        GameObject(centerX, centerY, width, height) {

    enum class State(val dist: FloatArray, val colors:Array<Color>){
        NORMAL(floatArrayOf(0.0f, 0.8f, 1.0f), arrayOf(
                Color(0, 0, 0, 0),
                Color(0, 0, 0, 150),
                Color(0, 0, 0, 255))),
        DOUBLESPEED(floatArrayOf(0.0f, 0.7f, 0.8f , 0.9f, 1.0f), arrayOf(
                Color(0, 0, 0, 0),
                Color(228, 229, 203,100),
                Color(244, 242, 168, 150),
                Color(241, 252, 132,200),
                Color(0, 0, 0, 255))),
        TIMESTOP(floatArrayOf(0.0f, 0.4f, 0.6f , 0.8f, 1.0f), arrayOf(
                Color(0, 0, 0, 0),
                Color(165, 165, 165,100),
                Color(120, 120, 120, 150),
                Color(95, 95, 95,200),
                Color(0, 0, 0, 255))),
        EARTHQUAKE(floatArrayOf(0.0f, 0.4f, 0.6f , 0.8f, 1.0f), arrayOf(
                Color(0, 0, 0, 0),
                Color(210, 163, 128,100),
                Color(218, 183, 112, 150),
                Color(203, 168, 39,200),
                Color(0, 0, 0, 255))),
        RETARD(floatArrayOf(0.0f, 0.4f, 0.6f , 0.8f, 1.0f), arrayOf(
                Color(0, 0, 0, 0),
                Color(225, 254, 172,100),
                Color(155, 254, 110, 150),
                Color(0, 152, 36,200),
                Color(0, 0, 0, 255))),
        SPRINT(floatArrayOf(0.0f, 0.6f, 0.7f , 0.8f, 1.0f), arrayOf(
                Color(0, 0, 0, 0),
                Color(243, 234, 171,100),
                Color(245, 245, 165, 150),
                Color(222, 247, 71,200),
                Color(0, 0, 0, 255))),
        FREEZE(floatArrayOf(0.0f, 0.4f, 0.6f , 0.8f, 1.0f), arrayOf(
                Color(0, 0, 0, 0),
                Color(222, 248, 250,100),
                Color(124, 245, 248, 150),
                Color(33, 229, 255,200),
                Color(0, 0, 0, 255))),
        BLACKHOLE(floatArrayOf(0.0f, 0.6f, 0.7f , 0.8f, 1.0f), arrayOf(
                Color(0, 0, 0, 0),
                Color(167, 5, 159,100),
                Color(152, 86, 138, 150),
                Color(113, 113, 113,200),
                Color(0, 0, 0, 255))),
        DEATH(floatArrayOf(0.0f, 0.4f, 0.6f , 0.8f, 1.0f), arrayOf(
                Color(0, 0, 0, 0),
                Color(255, 0, 0,100),
                Color(253, 27, 3, 150),
                Color(242, 0, 0,200),
                Color(0, 0, 0, 255))),
        KILL(floatArrayOf(0.0f, 0.4f, 0.6f, 0.7f , 0.8f, 1.0f), arrayOf(
                Color(0, 0, 0, 0),
                Color(53, 77, 253,100),
                Color(37, 1, 255,150),
                Color(72, 31, 103, 200),
                Color(255, 0, 0,225),
                Color(0, 0, 0, 255))),
        SUN(floatArrayOf(0.0f, 0.6f, 0.7f , 0.8f, 1.0f), arrayOf(
                Color(0, 0, 0, 0),
                Color(255, 210, 0,100),
                Color(255, 170, 0, 150),
                Color(255, 89, 0,200),
                Color(0, 0, 0, 255))),
    }

    enum class Team(val color:Color){
        RED(Color.RED),
        BLUE(Color.BLUE),
        YELLOW(Color.YELLOW),
        GREEN(Color.GREEN)

    }

    var changedX = 0.0 // X軸慣性移動速度
    var changedY = 0.0 // Y軸慣性移動速度
    var changedSpeed = char.changedSpeed // 輸入方向時的速度改變量，走路0.5、跑步0.8
    private val friction = 0.9 //摩擦力，造成的反方向加速度(非真正的加速度觀念，直接以等比方式削減速度)0.9 ~ <1.0，小於0.9角色移動會變得很慢
    var jumped = false //跳過沒
    private val gravity = 0.8//重力，下墜時的加速度
    var dir = Global.Direction.RIGHT//初始角色方向
    var skill = char.charSkill
    var skillAnimator = SkillPool.SkillAnimator(skill)
    var skillState = SkillPool.SkillState.USEABLE
    var skillKeeptime = Delay(skill.keepTime)
    var skillCdtime = Delay(skill.cdTime)
    var radius = char.radius
    var key = 0
    var jumpHeight = 20.0
    var state = State.NORMAL
    var ghost = false
    var deathDelay = Delay(60)

    //實現重力
    fun fall() {
        translate(y = changedY.toInt())
        if (changedY < 0.0) {//往上跳時，上升力指數下降
            changedY *= 0.9
        }
        changedY += gravity//重力加速度
    }

    //只處裡水平移動
    open fun move() {
        translate(x = changedX.toInt())
        changedX *= friction
    }


    open fun alwaysInMap() {
        // mapInfo()!!.top.plus(Global.UNIT_Y / 2)  !! 代表假設mapInfo 一定不是null , .plus 代表 +
        if (touchTop) {
            setPosition(collider.centerX, mapInfo()!!.top.plus(Global.UNIT_Y / 2))
        }
        if (touchBottom) {
            setPosition(collider.centerX, mapInfo()!!.bottom.plus(-Global.UNIT_Y / 2))
            changedY = 0.0//垂直力歸0
            jumped = false//重置跳
        }
        if (touchLeft) {
            setPosition(mapInfo()!!.left.plus(Global.UNIT_X / 2), collider.centerY)
        }
        if (touchRight) {
            setPosition(mapInfo()!!.right.plus(-Global.UNIT_X / 2), collider.centerY)
        }
    }

    fun setPosition(x: Int, y: Int) {
        painter.setCenter(x, y)
        collider.setCenter(x, y)
    }

    override fun paintComponent(g: Graphics) {
        if(ghost){
            char.ghostAnimator.paint(dir, painter.left, painter.top, painter.right, painter.bottom, g)
        }else{
            char.animator.paint(dir, painter.left, painter.top, painter.right, painter.bottom, g)
        }
        if (skillState != SkillPool.SkillState.USEABLE) {
            skillAnimator.paint(painter.left, painter.top, painter.right, painter.bottom, g)
        }
    }

    //左右位移，限定不能出視窗，技能更新，動畫更新
    override fun update(timePassed: Long) {
        if (keySet != null) {
            move()
            alwaysInMap()
        }
        if(state == State.DEATH || state == State.KILL){
            if(deathDelay.count()){
                state = State.NORMAL
            }
        }
        if(ghost){
            char.ghostAnimator.update()
        }else{
            char.animator.update()
        }
        skillAnimator.update()
    }

    // 續發動作，輸入方向改變不同方向的力
    open fun changeSpeed(keyCode: Int) {
        if (keySet != null) {
            if (keyCode == keySet[1] || keyCode == keySet[2]) {
                ClientClass.getInstance().sent(INPUT, bale("${CharacterPool.CharAnimator.State.RUN}"))
                if(ghost){
                    char.ghostAnimator.setSta(CharacterPool.CharAnimator.State.RUN)
                }else{
                    char.animator.setSta(CharacterPool.CharAnimator.State.RUN)
                }
            }
            when (keyCode) {// 輸入方向改變不同方向的力
                keySet[1] -> {
                    ClientClass.getInstance().sent(INPUT, bale("${Global.Direction.LEFT}"))
                    dir = Global.Direction.LEFT
                    changedX -= changedSpeed
                }
                keySet[2] -> {
                    ClientClass.getInstance().sent(INPUT, bale("${Global.Direction.RIGHT}"))
                    dir = Global.Direction.RIGHT
                    changedX += changedSpeed
                }
            }
        }
    }

    // 點發動作
    open fun action(keyCode: Int) {
        if (keySet != null) {
            when (keyCode) {
                keySet[0] -> { //跳
                    if (!jumped) {
                        ClientClass.getInstance().sent(INPUT, bale("${Global.Direction.UP}"))
                        AudioResourceController.getInstance().shot(Path.Sounds.JUMP)
                        dir = Global.Direction.UP
                        changedY = -jumpHeight
                        if (!IS_DEBUG) {
                            jumped = true
                        }
                    }
                }
                keySet[3] -> if (skillState == SkillPool.SkillState.USEABLE) {
                    ClientClass.getInstance().sent(INPUT, bale("${SkillPool.SkillState.START}"))
                    skillState = SkillPool.SkillState.START
                }
            }
        }
    }

    override val input: ((GameKernel.Input.Event) -> Unit)? = { e ->
        run {
            when (e) {
                is GameKernel.Input.Event.KeyKeepPressed -> changeSpeed(e.data.keyCode)//可以連續做
                is GameKernel.Input.Event.KeyPressed -> action(e.data.keyCode)//按一次動一次
                is GameKernel.Input.Event.KeyReleased ->
                    if (keySet != null) {
                        if (e.data.keyCode == this.keySet[1] || e.data.keyCode == keySet[2]) {
                            ClientClass.getInstance().sent(INPUT, bale("${CharacterPool.CharAnimator.State.NORMAL}"))
                            if(ghost){
                                char.ghostAnimator.setSta(obj.utils.Actor.CharacterPool.CharAnimator.State.NORMAL)
                            }else{
                                char.animator.setSta(obj.utils.Actor.CharacterPool.CharAnimator.State.NORMAL)
                            }
                        }
                    }
                else -> {}
            }
        }
    }


}