package menu.impl

import Server
import core.GameKernel
import core.controllers.ResController
import core.controllers.SceneController
import menumodule.menu.Button
import menumodule.menu.EditText
import menumodule.menu.Theme
import scene.InternetScene
import utils.Global
import utils.Global.SERVER
import utils.Path
import java.awt.Graphics

//開啟SERVER的地方
class CreateRoomMenu(x: Int, y: Int, width: Int, height: Int) : PopupWindow(x, y, width, height) {

    private val buttons: MutableList<Button> = mutableListOf()
    val editFrames: MutableList<EditText> = mutableListOf()
    val img = ResController.instance.image(Path.Imgs.Backgrounds.CREATEROOMMENU)

    override fun sceneBegin() {
        buttons.add(Button(535, 10, Theme.get(5)))
        buttons[0].setClickedActionPerformed { x, y ->
            run {
                hide()
                sceneEnd()
            }
        }
        buttons.add(Button(160, 480, Theme.get(9)))
        buttons[1].setClickedActionPerformed { x, y ->
            run {
                SERVER = true
                Server.instance().create(editFrames[1].editText.toInt())//建立server，只要一個人開，放在遊戲一開始的scene
                Server.instance().start()
                println("主機IP：" + Server.instance().localAddress[0] + "\n主機PORT：" + Server.instance().localAddress[1])
                SceneController.instance.change(InternetScene(editFrames[0].editText, editFrames[1].editText.toInt(), Server.instance().localAddress[0]))//0:ip, 1:port
            }
        }
        editFrames.add((EditText(35, 195, "請輸入您的姓名：", Theme.get(6))))
        editFrames[0].setEditLimit(10)
        editFrames[0].isFocus()
        editFrames.add((EditText(35, 365, "請輸入欲連線的PORT：", Theme.get(7))))
        editFrames[1].setEditLimit(15)
        editFrames[1].isFocus()

    }

    override fun sceneEnd() {
        ResController.instance.clear()
    }

    override fun paintWindow(g: Graphics) {
        g.drawImage(img, 0, 0, 600, 600, null)
        buttons.forEach {
            it.paint(g)
        }
        editFrames.forEach {
            it.paint(g)
        }
    }

    override fun update(timePassed: Long) {
    }

    override val input: ((GameKernel.Input.Event) -> Unit)?
        get() = { e ->
            run {
                when (e) {
                    is GameKernel.Input.Event.MouseMoved -> {
                        buttons.forEach {
                            MouseTriggerImpl.mouseTrig(it, e)
                        }
                        editFrames.forEach {
                            MouseTriggerImpl.mouseTrig(it, e)
                        }
                    }
                    is GameKernel.Input.Event.MousePressed -> {
                        buttons.forEach {
                            MouseTriggerImpl.mouseTrig(it, e)
                        }
                        editFrames.forEach {
                            MouseTriggerImpl.mouseTrig(it, e)
                        }
                    }
                    is GameKernel.Input.Event.KeyTyped -> {
                        editFrames.forEach {
                            it.keyTyped(e.data.keyCode)
                        }
                    }
                }
            }
        }


}