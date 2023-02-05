package utils

import java.awt.event.KeyEvent


object Global {
    enum class Direction(val num: Int) {
        UP(3), DOWN(0), LEFT(1), RIGHT(2)
    }

    const val IS_DEBUG = false
    fun log(str: String) {
        if (IS_DEBUG) {
            println(str)
        }
    }

    // 視窗大小
    const val WINDOW_WIDTH = 1200
    const val WINDOW_HEIGHT = 600

    // 去視窗框後銀幕大小
    const val SCREEN_X = WINDOW_WIDTH - 8 - 8
    const val SCREEN_Y = WINDOW_HEIGHT - 31 - 8

    // 資料刷新時間
    const val UPDATE_FREQ = 60 // 每秒更新60次遊戲邏輯

    // 畫面更新時間
    const val PAINT_FREQ = 60

    // 單位大小
    const val UNIT_X = 32
    const val UNIT_Y = 32

    // 存出生點資訊(出生地的中心點
    var birthPlaces: MutableList<Int> = mutableListOf()

    //網路連線用server，一個人開就好，其他人只要呼叫client
    var SERVER: Boolean = false

    // 1P 按鍵
    const val UP_1P = KeyEvent.VK_W
    const val LEFT_1P = KeyEvent.VK_A
    const val RIGHT_1P = KeyEvent.VK_D
    const val SKILL_1P = KeyEvent.VK_G

    // 2P 按鍵
    const val UP_2P = KeyEvent.VK_UP
    const val LEFT_2P = KeyEvent.VK_LEFT
    const val RIGHT_2P = KeyEvent.VK_RIGHT
    const val SKILL_2P = KeyEvent.VK_NUMPAD0

    //紀錄勝利資訊
    var isTeam = false

    //玩家名，只能是英文
    var playerName: String = "PLAYER1"

    //連線用command:
    object Command {
        //傳出去的組合種類(不同組合能有不同屬性數量)
        const val CONNECT = 0//連上線時要傳出的封包，傳出創建角色需要的資訊
        const val UPDATE = 1//每幀傳出角色座標
        const val INPUT = 2//角色切換狀態時(方向、技能開始、隨機技能角色的技能...)傳出座標
        const val GAMESTART = 3//房主確認人都到齊開始遊戲
        const val GENITEM = 4
        const val REMOVEITEM = 5
        const val ITEMSET = 6
        const val ITEMSTART = 7
        const val SETMISSION = 8
        const val VICTORY = 9
        const val CHANGE_TEAM = 10
        const val GHOSTMISSION = 11
        const val TOUCH_GHOST = 12
        const val GEN_AI = 13
        const val UPDATE_AI = 14
        const val AUTO_TEAM = 15

        fun bale(vararg str: String): ArrayList<String>? {
            val tmp = ArrayList<String>()
            str.forEach { a -> tmp.add(a) }
            return tmp
        }
    }


}
