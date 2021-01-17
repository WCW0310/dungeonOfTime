package core.controllers

import core.GameKernel
import core.Scene
import java.awt.Graphics

class SceneController private constructor() : GameKernel.GameInterface {
    private var lastScene: Scene? = null
    private var currentScene: Scene? = null

    fun change(scene: Scene) {
        lastScene = currentScene
        scene.sceneBegin()
        lastScene?.run { ResController.instance.keep() }
        currentScene = scene
    }

    override fun paint(g: Graphics) {
        currentScene?.paint(g)
        // currentScene? = if(currentScene != null)
    }

    override fun update(timePassed: Long) {
        lastScene?.let {
            it.sceneEnd()
            lastScene = null
            ResController.instance.release()
        }
        currentScene?.update(timePassed)
    }
    /*當lastScene it(Scene) != null  時，執行Scene 的End
    此時 it 是 Scene (lastScene物件實體) this 是SceneController
    並把SceneController的lastScene = null  ；不能寫it = null 沒有意義唷～
    執行SceneController的ResController.instance.release()
    */

    override val input: ((GameKernel.Input.Event) -> Unit)?
        get() = currentScene?.input

    companion object {
        val instance: SceneController by lazy { SceneController() }
    }
}