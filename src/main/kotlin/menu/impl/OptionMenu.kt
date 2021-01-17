package menu.impl

import core.GameKernel
import core.controllers.ResController
import core.controllers.SceneController
import menumodule.menu.Button
import menumodule.menu.Label
import menumodule.menu.Theme
import obj.utils.Actor.CharacterPool
import scene.ActorChooseScene
import scene.MainScene
import scene.MenuScene
import utils.Path
import java.awt.Color
import java.awt.Graphics


class OptionMenu(x: Int, y: Int, width: Int, height: Int,val chars:MutableList<CharacterPool.Character>) : PopupWindow(x, y, width, height) {

    private lateinit var label: Label
    private var buttons: MutableList<Button> = mutableListOf()
    private val img = ResController.instance.image(Path.Imgs.Backgrounds.OPTION_BACKGROUND)

    override fun sceneBegin() {
        label = Label(545, 5, Theme.get(13))
        label.setClickedActionPerformed { x, y ->
            run{
                AudioResourceController.getInstance().play(Path.Sounds.BUTTON)
                hide()
                sceneEnd()
            }
        }
        buttons.add(Button(50, 130, Theme.get(14)))
        buttons[0].setClickedActionPerformed { x: Int, y: Int ->
            run{
                AudioResourceController.getInstance().stop(Path.Sounds.MAINSCENEBGM)
                AudioResourceController.getInstance().loop(Path.Sounds.MENUSCENEBGM, -1)
                SceneController.instance.change(ActorChooseScene())
            } }
        buttons.add(Button(250, 130, Theme.get(15)))
        buttons[1].setClickedActionPerformed { x: Int, y: Int ->
            run{
                AudioResourceController.getInstance().stop(Path.Sounds.MAINSCENEBGM)
                SceneController.instance.change(MainScene(chars))
            } }
        buttons.add(Button(450, 130, Theme.get(16)))
        buttons[2].setClickedActionPerformed { x: Int, y: Int ->
            run{
                AudioResourceController.getInstance().stop(Path.Sounds.MAINSCENEBGM)
                SceneController.instance.change(MenuScene())
            } }
    }

    override  fun sceneEnd() {
        ResController.instance.clear()
    }

    override fun paintWindow(g: Graphics) {
        g.color = Color.GRAY
        g.drawImage(img, 0, 0, super.width, super.height , null)
        label.paint(g)
        buttons.forEach {
            it.paint(g)
        }
    }

    override fun update(timePassed: Long) {}

    override val input: ((GameKernel.Input.Event) -> Unit)?
        get() = { e ->
        run {
            when (e) {
                is GameKernel.Input.Event.MousePressed -> {
                    MouseTriggerImpl.mouseTrig(label, e)
                    buttons.forEach {
                        MouseTriggerImpl.mouseTrig(it, e)
                    }
                }
                is GameKernel.Input.Event.MouseMoved -> {
                    MouseTriggerImpl.mouseTrig(label, e)
                    buttons.forEach {
                        MouseTriggerImpl.mouseTrig(it, e)
                    }
                }
                else -> {

                }
            }
        }
    }
}