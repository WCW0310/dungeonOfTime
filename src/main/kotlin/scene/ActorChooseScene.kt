package scene

import core.GameKernel
import core.Scene
import core.controllers.ResController
import core.controllers.SceneController
import obj.utils.Actor.CharacterPool
import utils.Global
import utils.Global.isTeam
import utils.Path
import java.awt.Color
import java.awt.Font
import java.awt.Graphics
import java.awt.event.KeyEvent.*

class ActorChooseScene : Scene() {

    private val img = ResController.instance.image(Path.Imgs.Backgrounds.ACTORCHOOSE)
    private var player1 = CharacterPool.Character.WARRIOR1
    private var player2 = CharacterPool.Character.WARRIOR1
    private var p1Count = 0
    private var p2Count = 0
    private var p1Check = false
    private var p2Check = false
    private var font = Font("Algerian", Font.PLAIN, 35)

    override fun sceneBegin() {
        Global.log("ActorChooseScene")
        player1.animator.setSta(CharacterPool.CharAnimator.State.WALK)
        player2.animator.setSta(CharacterPool.CharAnimator.State.WALK)
    }

    override fun sceneEnd() {
        AudioResourceController.getInstance().stop(Path.Sounds.MENUSCENEBGM)
        ResController.instance.clear()
    }

    override fun update(timePassed: Long) {
        player1.animator.update()
        player2.animator.update()
        if (p1Check && p2Check) {
            val chars :MutableList<CharacterPool.Character> = mutableListOf()
            chars.add(player1)
            chars.add(player2)
            SceneController.instance.change(MainScene(chars))
        }
    }

    override fun paint(g: Graphics) {
        g.drawImage(img, 0, 0, null)
        g.color = Color.RED
//        for(i in 1..30){
//            g.drawLine(0,i*20,1200,i*20)
//        }
//        for(i in 1..60){
//            g.drawLine(i*20,0,i*20,600)
//        }
        player1.animator.paint(Global.Direction.DOWN, 250, 120, 350, 220, g)
        player2.animator.paint(Global.Direction.DOWN, 850, 120, 950, 220, g)
        val str = arrayOf("Character: " + player1.charName, "Skill: " + player1.charSkill.skillName
                , "Speed: " + player1.changedSpeed, "radius: " + player1.radius)
        val str2 = arrayOf("Character: " + player2.charName, "Skill: " + player2.charSkill.skillName
                , "Speed: " + player2.changedSpeed, "radius: " + player2.radius)
        g.font = font
        for (i in str.indices) {
            g.drawString(str[i], 130, 350 + i * 40)
        }
        g.drawString("Player1", 225,100)
        if(p1Check){
            g.drawString("Check!",245,265)
        }
        g.color = Color.BLUE
        for (i in str2.indices) {
            g.drawString(str2[i], 730, 350 + i * 40)
        }
        g.drawString("Player2", 825,100)
        if(p2Check){
            g.drawString("Check!",845,265)
        }
        if(isTeam){
            g.color = Color.green
            g.drawRect(475,10,255,35)
            g.drawString("TeamMode On",475,40)
        }else{
            g.color = Color.black
            g.drawRect(475,10,255,35)
            g.drawString("TeamMode Off",475,40)
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
                                    if (p1Count == 0){
                                        p1Count = CharacterPool.Character.values().size - 1
                                    }else{
                                        p1Count--
                                    }
                                    player1 = CharacterPool.Character.values()[p1Count]
                                    player1.animator.setSta(CharacterPool.CharAnimator.State.WALK)
                                }
                            }
                            VK_D -> {
                                if (!p1Check) {
                                    if (p1Count == CharacterPool.Character.values().size - 1){
                                        p1Count = 0
                                    }else{
                                        p1Count++
                                    }
                                    player1 = CharacterPool.Character.values()[p1Count]
                                    player1.animator.setSta(CharacterPool.CharAnimator.State.WALK)
                                }
                            }
                            VK_G -> {
                                p1Check = !p1Check
                                if (p1Check) {
                                    if (player1 == player2 && p2Check) {
                                        p1Check = false
                                    }
                                    AudioResourceController.getInstance().shot(Path.Sounds.BUTTON)
                                }
                            }
                            VK_LEFT -> {
                                if (!p2Check) {
                                    if (p2Count == 0){
                                        p2Count = CharacterPool.Character.values().size - 1
                                    }else{
                                        p2Count--
                                    }
                                    player2 = CharacterPool.Character.values()[p2Count]
                                    player2.animator.setSta(CharacterPool.CharAnimator.State.WALK)
                                }
                            }
                            VK_RIGHT -> {
                                if (!p2Check) {
                                    if (p2Count == CharacterPool.Character.values().size - 1){
                                        p2Count = 0
                                    }else{
                                        p2Count++
                                    }
                                    player2 = CharacterPool.Character.values()[p2Count]
                                    player2.animator.setSta(CharacterPool.CharAnimator.State.WALK)
                                }
                            }
                            VK_NUMPAD0 -> {
                                p2Check = !p2Check
                                if (p2Check) {
                                    if (player1 == player2 && p1Check) {
                                        p2Check = false
                                    }
                                    AudioResourceController.getInstance().shot(Path.Sounds.BUTTON)
                                }
                            }
                            VK_P -> {
                                isTeam = !isTeam
                                AudioResourceController.getInstance().shot(Path.Sounds.BUTTON)
                            }
                        }
                    }

                }
            }
        }
}