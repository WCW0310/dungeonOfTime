package menu.impl

import core.GameKernel
import core.controllers.ResController
import core.controllers.SceneController
import menumodule.menu.Button
import menumodule.menu.EditText
import menumodule.menu.Theme
import scene.InternetScene
import utils.Path
import java.awt.Graphics

//客戶端連線專用
class InternetConnectMenu(x: Int, y: Int, width: Int, height: Int) : PopupWindow(x, y, width, height) {

    private val buttons: MutableList<Button> = mutableListOf()
    val editFrames: MutableList<EditText> = mutableListOf()
    val img = ResController.instance.image(Path.Imgs.Backgrounds.INTERNETCONNECTMENU)


    override fun sceneBegin() {
        buttons.add(Button(535, 10, Theme.get(5)))
        buttons[0].setClickedActionPerformed { x, y ->
            run {
                hide()
                sceneEnd()
            }
        }
        buttons.add(Button(160, 480, Theme.get(10)))
        buttons[1].setClickedActionPerformed { x, y ->
            run {
                SceneController.instance.change(InternetScene(editFrames[0].editText, editFrames[2].editText.toInt(), editFrames[1].editText))
            }
        }
        editFrames.add((EditText(35, 125, "請輸入您的姓名：", Theme.get(6))))
        editFrames[0].setEditLimit(10)
        editFrames[0].isFocus()
        editFrames.add((EditText(35, 270, "請輸入欲連線的IP：", Theme.get(7))))
        editFrames[1].setEditLimit(15)
        editFrames[1].isFocus()
        editFrames.add((EditText(35, 415, "請輸入欲連線的PORT：", Theme.get(8))))
        editFrames[2].setEditLimit(15)
        editFrames[2].isFocus()
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
                    else -> {}
                }
            }
        }


}