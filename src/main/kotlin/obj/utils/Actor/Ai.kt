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
import kotlin.random.Random


class Ai(centerX: Int, centerY: Int, width: Int, height: Int, char: CharacterPool.Character,keySet: MutableList<Int>?, id: Int, name: String?, team: Team?, var level:Int) :
        Actor(centerX, centerY, width, height, char, null, id, "Computer", team) {

    var delay = Delay(60)
    var walkDelay = Delay(60)
    var r = 1

    init{
        walkDelay.loop()
    }

    // 續發動作，輸入方向改變不同方向的力
    override fun changeSpeed(keyCode: Int) {
        if (keyCode == 1 || keyCode == 2) {
            ClientClass.getInstance().sent(INPUT, bale("${CharacterPool.CharAnimator.State.RUN}"))
            char.animator.setSta(CharacterPool.CharAnimator.State.RUN)
        }
        when (keyCode) {// 輸入方向改變不同方向的力
            1 -> {
                ClientClass.getInstance().sent(INPUT, bale("${Global.Direction.LEFT}"))
                dir = Global.Direction.LEFT
                changedX -= changedSpeed
            }
            2 -> {
                ClientClass.getInstance().sent(INPUT, bale("${Global.Direction.RIGHT}"))
                dir = Global.Direction.RIGHT
                changedX += changedSpeed
            }
        }
    }

    // 點發動作
    override fun action(keyCode: Int) {
            when (keyCode) {
                0 -> { //跳
                    if (!jumped) {
                        ClientClass.getInstance().sent(INPUT, bale("${Global.Direction.UP}"))
                        dir = Global.Direction.UP
                        changedY = -jumpHeight
                        if (!IS_DEBUG) {
                            jumped = true
                        }
                    }
                }
                3 -> if (skillState == SkillPool.SkillState.USEABLE) {
                    ClientClass.getInstance().sent(INPUT, bale("${SkillPool.SkillState.START}"))
                    skillState = SkillPool.SkillState.START
                }
            }
    }

    //左右位移，限定不能出視窗，技能更新，動畫更新
    override fun update(timePassed: Long) {
        if(walkDelay.count()){
            r = Random.nextInt(1,3)
        }
        changeSpeed(r)
        action(if(skillState == SkillPool.SkillState.USEABLE)Random.nextInt(1,5) else Random.nextInt(0,10))
        move()
        alwaysInMap()
        char.animator.update()
        skillAnimator.update()
    }

    override val input: ((GameKernel.Input.Event) -> Unit)? = null
}