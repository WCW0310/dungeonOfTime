package menu.impl

import core.GameKernel
import core.controllers.ResController
import utils.Path
import java.awt.Graphics
import java.awt.Image
import java.awt.event.KeyEvent


class GameGuideMenu(x: Int, y: Int, width: Int, height: Int) : PopupWindow(x, y, width, height) {

    private val imgs : MutableList<Image> = mutableListOf()
    private var count = 0

    init{
        imgs.add(ResController.instance.image(Path.Imgs.Backgrounds.GAMEINTRO1))
        imgs.add(ResController.instance.image(Path.Imgs.Backgrounds.GAMEINTRO2))
        imgs.add(ResController.instance.image(Path.Imgs.Backgrounds.GAMEINTRO3))
        imgs.add(ResController.instance.image(Path.Imgs.Backgrounds.GAMEINTRO4))
    }

    override fun sceneBegin() {
    }

    override  fun sceneEnd() {
        ResController.instance.clear()
    }

    override fun paintWindow(g: Graphics) {
        g.drawImage(imgs[count], 0,0,1195,574,null)
    }

    override fun update(timePassed: Long) {}

    override val input: ((GameKernel.Input.Event) -> Unit)?
        get() = { e ->
            run{
                when(e){
                    is GameKernel.Input.Event.KeyPressed -> {
                        if(e.data.keyCode == KeyEvent.VK_A){
                            if(count > 0){
                                count--
                                AudioResourceController.getInstance().shot(Path.Sounds.BUTTON)
                            }
                        }
                        if(e.data.keyCode == KeyEvent.VK_D){
                            if(count < imgs.size - 1){
                                count++
                                AudioResourceController.getInstance().shot(Path.Sounds.BUTTON)
                            }
                        }
                    }
                    else -> {}
                }
            }

        }
}