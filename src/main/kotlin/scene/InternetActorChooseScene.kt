package scene

import core.GameKernel
import core.Scene
import core.controllers.ResController
import core.controllers.SceneController
import obj.utils.Actor.CharacterPool
import utils.Global
import utils.Path
import java.awt.Color
import java.awt.Font
import java.awt.Graphics
import java.awt.event.KeyEvent.*

//連線時的選角區
class InternetActorChooseScene : Scene() {

    private val img = ResController.instance.image(Path.Imgs.Backgrounds.ACTORCHOOSE_INTERNET)
    private var player1 = CharacterPool.Character.WARRIOR1

    private var p1Count = 0

    private var p1Check = false

    private var font = Font("Algerian", Font.PLAIN, 35)

    override fun sceneBegin() {
        Global.log("InternetActorChooseScene")
        player1.animator.setSta(CharacterPool.CharAnimator.State.WALK)
    }

    override fun sceneEnd() {
        AudioResourceController.getInstance().stop(Path.Sounds.MENUSCENEBGM)
        ResController.instance.clear()
    }

    override fun update(timePassed: Long) {
        player1.animator.update()
        if (p1Check) {
            var chars: MutableList<CharacterPool.Character> = mutableListOf()
            chars.add(player1)
//            chars.add(player2)
            SceneController.instance.change(InternetWaitingScene(chars))
        }
    }

    override fun paint(g: Graphics) {
        g.drawImage(img, 0, 0, null)
        g.color = Color.RED
        //銀幕畫比對線用
//        for(i in 1..30){
//            g.drawLine(0,i*20,1200,i*20)
//        }
//        for(i in 1..60){
//            g.drawLine(i*20,0,i*20,600)
//        }
        //left, right都+300角色才會顯示在正中間
        player1.animator.paint(Global.Direction.DOWN, 250 + 300, 120, 350 + 300, 220, g)
        var str = arrayOf("Character: " + player1.charName, "Skill: " + player1.charSkill, "Speed: " + player1.changedSpeed, "radius: " + player1.radius)
        g.font = font
        for (i in 0..str.size - 1) {
            g.drawString(str[i], 130 + 300, 350 + i * 40)
        }
        g.drawString(Global.playerName, 225 + 300, 100)
        if (p1Check) {
            g.drawString("Check!", 245 + 300, 265)
        }
        if (Global.SERVER) {
            g.drawString("IP:" + Server.instance().localAddress[0] + "     PORT:" + Server.instance().localAddress[1], 350, 30)
        }
    }

    override val input: ((GameKernel.Input.Event) -> Unit)?
        get() = { e ->
            run {
                when (e) {
                    is GameKernel.Input.Event.KeyPressed -> {
                        when (e.data.keyCode) {
                            VK_A -> {
                                if (!p1Check) {
                                    if (p1Count == 0) {
                                        p1Count = CharacterPool.Character.values().size - 1
                                    } else {
                                        p1Count--
                                    }
                                    player1 = CharacterPool.Character.values()[p1Count]
                                    player1.animator.setSta(CharacterPool.CharAnimator.State.WALK)
                                }
                            }
                            VK_D -> {
                                if (!p1Check) {
                                    if (p1Count == CharacterPool.Character.values().size - 1) {
                                        p1Count = 0
                                    } else {
                                        p1Count++
                                    }
                                    player1 = CharacterPool.Character.values()[p1Count]
                                    player1.animator.setSta(CharacterPool.CharAnimator.State.WALK)
                                }
                            }
                            VK_G -> {
                                p1Check = !p1Check
                            }
                        }
                    }

                }
            }
        }
}