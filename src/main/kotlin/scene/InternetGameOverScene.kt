package scene

import core.GameKernel
import core.Scene
import core.controllers.ResController
import core.controllers.SceneController
import core.utils.Delay
import menu.impl.MouseTriggerImpl
import menumodule.menu.Button
import menumodule.menu.Theme
import network.ClientClass
import utils.Global
import utils.Global.SERVER
import utils.Global.isTeam
import utils.Path
import java.awt.Color
import java.awt.Font
import java.awt.Graphics

class InternetGameOverScene(val winner: String) : Scene() {

    val delay = Delay(180)
    val font = Font("Algerian", Font.BOLD, 50)
    private val buttons: MutableList<Button> = mutableListOf()
    private var show = false
    private val img = ResController.instance.image(Path.Imgs.Backgrounds.GAMEOVERSCENE)

    override fun sceneBegin() {
        buttons.add(Button(300, 350, Theme.get(11)))
        buttons[0].setClickedActionPerformed { x, y ->
            run {
                AudioResourceController.getInstance().shot(Path.Sounds.BUTTON)
                AudioResourceController.getInstance().loop(Path.Sounds.MENUSCENEBGM, -1)
                isTeam = false
                SceneController.instance.change(InternetActorChooseScene())
            }
        }

        buttons.add(Button(700, 350, Theme.get(12)))
        buttons[1].setClickedActionPerformed { x, y ->
            run {
                //斷線，有問題，回到選單後無法再進行網路模式
                ClientClass.getInstance().disConnect()
                if (SERVER) {
                    Server.instance().close()
                    SERVER = false
                }
                isTeam = false
                AudioResourceController.getInstance().shot(Path.Sounds.BUTTON)
                SceneController.instance.change(MenuScene())
            }
        }
        delay.play()
    }

    override fun sceneEnd() {
        AudioResourceController.getInstance().stop(Path.Sounds.MAINSCENEBGM)
        ResController.instance.clear()
    }

    override fun paint(g: Graphics) {
        g.drawImage(img, 0, 0, 1200, 600, null)
        g.color = Color.black
        g.font = font
        g.drawString("Winner is --- ", 100, Global.SCREEN_Y / 4)
        g.drawString("$winner! ", 150, Global.SCREEN_Y / 4 + 50)
        if (show) {
            buttons.forEach {
                it.paint(g)
            }
        }
    }

    override fun update(timePassed: Long) {
        if (delay.count()) {
            show = true
        }
    }

    override val input: ((GameKernel.Input.Event) -> Unit)?
        get() = { e ->
            run {
                when (e) {
                    is GameKernel.Input.Event.MousePressed -> {
                        buttons.forEach {
                            MouseTriggerImpl.mouseTrig(it, e)
                        }
                    }
                    is GameKernel.Input.Event.MouseMoved -> {
                        buttons.forEach {
                            MouseTriggerImpl.mouseTrig(it, e)
                        }
                    }
                    else -> {}
                }
            }
        }
}