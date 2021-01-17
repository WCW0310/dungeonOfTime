package scene

import core.GameKernel
import core.Scene
import core.controllers.ResController
import core.controllers.SceneController
import network.ClientClass
import utils.Global
import java.awt.Graphics
import java.util.*

//只處理網路連線的地方
class InternetScene(val name: String, val port: Int, val hostIp: String) : Scene() {

    private var onLine: Boolean = false
    private fun inputInt(hint: String, min: Int, max: Int): Int {
        val scanner: Scanner = Scanner(System.`in`)
        var input = 0
        do {
            println(hint)
            input = scanner.nextInt()
        } while (input < min || input > max)
        return input
    }

    override fun sceneBegin() {
        Global.playerName = name
//        try {
        ClientClass.getInstance().connect(hostIp, port) //網路連線使用，客戶端才使用，單例，直接呼叫就好
//        } catch (NullPointerException)

        onLine = true
    }

    override fun sceneEnd() {
        ResController.instance.clear()
    }

    override fun update(timePassed: Long) {
        if (onLine) {
            SceneController.instance.change(InternetActorChooseScene())
        }
    }

    override fun paint(g: Graphics) {
    }

    override val input: ((GameKernel.Input.Event) -> Unit)?
        get() = null
}