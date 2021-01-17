package core

import core.controllers.SceneController
import java.awt.Graphics

class GameInterfaceImpl(startScene: Scene) : GameKernel.GameInterface {

    // 原本的GI 因為大家都一定會用到SceneController 所以就直接做好

    init {
        SceneController.instance.change(startScene)
    }
    // function 如果只有一行時，可以直接 = 方法內容，省略大括號。
    override fun update(timePassed: Long) = SceneController.instance.update(timePassed)

    override fun paint(g: Graphics) = SceneController.instance.paint(g)

    override val input: ((GameKernel.Input.Event) -> Unit)?
        get() = SceneController.instance.input
    // 實現抽象屬性 input 的 getter，如果沒寫get() 只會讀取一次
}