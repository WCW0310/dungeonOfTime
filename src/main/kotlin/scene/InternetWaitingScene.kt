package scene

import camera.Camera
import camera.MapInformation
import core.GameKernel
import core.Scene
import core.controllers.ResController
import core.controllers.SceneController
import maploader.MapLoader
import menu.impl.MouseTriggerImpl
import menumodule.menu.Button
import menumodule.menu.Theme
import network.ClientClass
import obj.utils.Actor.Actor
import obj.utils.Actor.Ai
import obj.utils.Actor.CharacterPool
import obj.utils.Obj.Brick
import obj.utils.Obj.ChooseItem
import obj.utils.Obj.Item
import obj.utils.SkillPool
import utils.Global
import utils.Global.Command.AUTO_TEAM
import utils.Global.Command.CHANGE_TEAM
import utils.Global.Command.CONNECT
import utils.Global.Command.GAMESTART
import utils.Global.Command.GEN_AI
import utils.Global.Command.GHOSTMISSION
import utils.Global.Command.bale
import utils.Global.IS_DEBUG
import utils.Global.LEFT_1P
import utils.Global.RIGHT_1P
import utils.Global.SCREEN_Y
import utils.Global.SERVER
import utils.Global.SKILL_1P
import utils.Global.UNIT_X
import utils.Global.UNIT_Y
import utils.Global.UP_1P
import utils.Global.WINDOW_HEIGHT
import utils.Global.WINDOW_WIDTH
import utils.Path
import java.awt.Color
import java.awt.Graphics
import java.awt.event.KeyEvent
import java.util.*
import kotlin.random.Random

//等待玩家到齊的房間，只有房主(開啟SERVER的人)有開始遊戲的按鈕，會將所有玩家資料(players)傳到遊戲房
class InternetWaitingScene(val chars: MutableList<CharacterPool.Character>) : Scene() {
    //單機雙人鍵盤組
    private val KEY_1P: MutableList<Int> = mutableListOf()

    //遊戲玩家，[0]是本地玩家，其他玩家順序不固定，依靠id來區分
    private val players: MutableList<Actor> = mutableListOf()

    //所有平台 + 四周牆
    private val bricks: MutableList<Brick> = mutableListOf()

    //道具
    private val items: MutableList<ChooseItem> = mutableListOf()

    //鏡頭改良版，限制視野
    private val cameras: MutableList<Camera> = mutableListOf()

    //遊戲開始鍵
    private var button: Button? = null
    private var bgImg = ResController.instance.image(Path.Imgs.Backgrounds.INTERNETWAITINGROOM)

    //關卡選擇(false: 3選一, true: 鬼抓人)
    private var ghostMission: Boolean = false

    //1:1:1:1
    private var autoTeam = false

    override fun sceneBegin() {
        Global.log("Internet Waiting Room")
        mapInitialize()
        MapInformation.setMapInfo(UNIT_X, UNIT_Y, UNIT_X * 38, UNIT_Y * 18)
        players.add(Actor(600, 500, UNIT_X - 5, UNIT_Y - 5, chars[0], KEY_1P, ClientClass.getInstance().id, Global.playerName, Actor.Team.RED))
        cameras.add(Camera.Builder(WINDOW_WIDTH, SCREEN_Y).setChaseObj(players[0]).setCameraWindowLocation(0, 0).gen())
        //房主才有的開始遊戲按鈕
        if (SERVER) {
            button = Button(515, 30, Theme.get(17))
            //設定按鈕被點擊後執行的動作
            button!!.setClickedActionPerformed { x, y ->
                run {
                    //隨機抽地圖(2張)
                    var map = Random.nextInt(1, 3)
                    //1~3隨機抽關卡
                    var r = Random.nextInt(1, 4)
                    //抽鬼抓人模式的鬼
                    var ghost = Random.nextInt(0, players.size)
                    if (ghostMission) {
                        r = 4
                        for (i in players) {
                            if (i.id == players[ghost].id) {
                                i.team = Actor.Team.BLUE
                            } else {
                                i.team = Actor.Team.RED
                            }
                            when (i.team) {
                                Actor.Team.RED -> ClientClass.getInstance().sent(CHANGE_TEAM, bale("red"))
                                Actor.Team.BLUE -> ClientClass.getInstance().sent(CHANGE_TEAM, bale("blue"))
                                else -> {}
                            }
                        }
                        //鬼抓人模式人數少於4就加入AI
                        if (players.size < 4) {
                            for (i in 1..(4 - players.size)) {
                                var random = Random.nextInt(11)
                                players.add(Ai(600, 500, UNIT_X - 5, UNIT_Y - 5, CharacterPool.Character.values()[Random.nextInt(6)], null, i, "Computer_$i", Actor.Team.RED, random))
                                ClientClass.getInstance().sent(GEN_AI, bale("$i", "$random"))
                            }
                        }
                        ClientClass.getInstance().sent(GAMESTART, bale("$r", "${players[ghost].id}", "$map"))
                        //換場
                        SceneController.instance.change(InternetMainScene(players, r, players[ghost], map))
                    } else {
                        if (players.size == 1 || !canPlay()) {
                            //只有一人沒反應，人數不同沒反應
                        } else {
                            //人數非偶數時不能pk模式，有黃或綠不能pk，有機會再修
                            var yellowGreenExist = false
                            for (i in players) {
                                if (i.team == Actor.Team.YELLOW || i.team == Actor.Team.GREEN) {
                                    yellowGreenExist = true
                                    break
                                }
                            }
                            if ((players.size % 2 != 0) && r == 3 || yellowGreenExist) {
                                r = Random.nextInt(1, 3)
                            }
                            //都選同色時，自動分隊>>亂鬥模式
                            if (autoTeam) {
                                ClientClass.getInstance().sent(AUTO_TEAM, bale(""))
                                for (i in 0 until players.size) {
                                    players[i].team = Actor.Team.values()[(players[i].id - 100) % 4]
                                }
                                //要防止進入特殊關卡
                                if (r == 3) {
                                    r = Random.nextInt(1, 3)
                                }

                            }

                            ClientClass.getInstance().sent(GAMESTART, bale("$r", "${players[ghost].id}", "$map"))


                            //換場
                            SceneController.instance.change(InternetMainScene(players, r, players[ghost], map))
                        }
                    }
                }
            }
        }

        //鍵盤組加入global的按鍵配置
        KEY_1P.add(UP_1P)//w 0
        KEY_1P.add(LEFT_1P)//a 1
        KEY_1P.add(RIGHT_1P)//d 2
        KEY_1P.add(SKILL_1P)//g 3

        //傳出本地角色的創建資訊
        ClientClass.getInstance().sent(CONNECT, bale("${players[0].char}", players[0].name!!))
    }

    override fun sceneEnd() {
        ResController.instance.clear()  // 新版GameKernel 處裡了舊場景會被清掉的問題，所以直接這邊處裡即可
        Global.log("Game End")
    }

    //計算是否符合開始遊戲條件，一定要全部人同色(自動打亂配對)，或每隊人數相同才過
    private fun canPlay(): Boolean {
        var redNum: Int = 0
        var blueNum: Int = 0
        var yellowNum: Int = 0
        var greenNum: Int = 0
        //實際有人的隊伍陣列
        var teams: MutableList<Int> = mutableListOf()
        //填入各顏色隊伍人數
        players.forEach {
            when (it.team) {
                Actor.Team.RED -> {
                    redNum += 1
                }
                Actor.Team.BLUE -> {
                    blueNum += 1
                }
                Actor.Team.YELLOW -> {
                    yellowNum += 1
                }
                Actor.Team.GREEN -> {
                    greenNum += 1
                }
                else -> {}
            }
        }
        if (redNum > 0) {
            teams.add(redNum)
        }
        if (blueNum > 0) {
            teams.add(blueNum)
        }
        if (yellowNum > 0) {
            teams.add(yellowNum)
        }
        if (greenNum > 0) {
            teams.add(greenNum)
        }
        //都是同一種顏色>>自動轉換
        if (teams.size == 1) {
            autoTeam = true
            return true
        }
        var tmp: Int = 0
        for (i in 0 until teams.size) {
            tmp = teams[i]
            for (j in i until teams.size) {
                if (tmp != teams[j]) {
                    return false
                }
            }
            return true
        }
        return false
    }

    //角色更新與物件的碰撞判定
    private fun charUpdate(char: Actor, timePassed: Long) {
        char.fall()//垂直位移，重力
        for (i in bricks) {
            //角色碰撞磚塊、角色左!=磚塊右、角色右!=磚塊左
            if (char.isCollision(i) && char.collider.left != i.collider.right && char.collider.right != i.collider.left) {
                //角色底低於磚塊頂、角色頂高於磚塊頂
                if (char.collider.bottom > i.collider.top && char.collider.top < i.collider.top) {
                    char.setPosition(char.collider.centerX, i.collider.top - UNIT_Y / 2)
                    char.changedY = 0.0
                    char.jumped = false
                }
                //角色頂高於磚塊底、角色底低於磚塊底
                else if (char.collider.top < i.collider.bottom && char.collider.bottom > i.collider.bottom) {
                    char.setPosition(char.collider.centerX, i.collider.bottom + UNIT_Y / 2)
                }
            }
        }
        char.update(timePassed)//左右位移，限定不能出視窗，技能更新，動畫更新
        for (i in bricks) {
            //角色碰撞磚塊、角色底!=磚塊頂、角色頂!=磚塊底
            if (char.isCollision(i) && char.collider.bottom != i.collider.top && char.collider.top != i.collider.bottom) {
                //角色右右於磚塊左、角色左左於磚塊左
                if (char.collider.right > i.collider.left && char.collider.left < i.collider.left) {
                    char.setPosition(i.collider.left - UNIT_X / 2, char.collider.centerY)
                }
                //角色左左於磚塊右、角色右右於磚塊右
                else if (char.collider.left < i.collider.right && char.collider.right > i.collider.right) {
                    char.setPosition(i.collider.right + UNIT_X / 2, char.collider.centerY)
                }
            }
        }
        //碰到顏色石改TEAM
        for (i in items) {
            if (char.isCollision(i)) {
                when (i.type) {
                    ChooseItem.ItemType.GEM_RED -> {
                        if (char.team != Actor.Team.RED) {
                            ClientClass.getInstance().sent(CHANGE_TEAM, bale("red"))
                            char.team = Actor.Team.RED
                        }
                    }
                    ChooseItem.ItemType.GEM_BLUE -> {
                        if (char.team != Actor.Team.BLUE) {
                            ClientClass.getInstance().sent(CHANGE_TEAM, bale("blue"))
                            char.team = Actor.Team.BLUE

                        }
                    }
                    ChooseItem.ItemType.GEM_YELLOW -> {
                        if (char.team != Actor.Team.YELLOW) {
                            ClientClass.getInstance().sent(CHANGE_TEAM, bale("yellow"))
                            char.team = Actor.Team.YELLOW
                        }
                    }
                    ChooseItem.ItemType.GEM_GREEN -> {
                        if (char.team != Actor.Team.GREEN) {
                            ClientClass.getInstance().sent(CHANGE_TEAM, bale("green"))
                            char.team = Actor.Team.GREEN
                        }
                    }
                    ChooseItem.ItemType.MISSION1 -> {
                        if (ghostMission && SERVER) {
                            ClientClass.getInstance().sent(GHOSTMISSION, bale("!ghostMission"))
                            ghostMission = false
                        }
                    }
                    ChooseItem.ItemType.MISSION2 -> {
                        if (!ghostMission && SERVER) {
                            ClientClass.getInstance().sent(GHOSTMISSION, bale("ghostMission"))
                            ghostMission = true
                        }
                    }
                }
            }
        }
    }

    override fun update(timePassed: Long) {
        //傳出更新資訊，本地角色位置x,y
        ClientClass.getInstance().sent(Global.Command.UPDATE, bale("${players[0].collider.centerX}", "${players[0].collider.centerY}", "${players[0].key}"))
        //每次更新都先接收server傳回的資料，並依類型作不同處理
        ClientClass.getInstance().consume { serialNum: Int, commandCode: Int, strs: ArrayList<String> ->
            when (commandCode) {
                //創建其他玩家角色
                CONNECT -> {
                    //判斷角色創建沒
                    var isBurn = false
                    for (i in players) {
                        if (i.id == serialNum) {
                            isBurn = true
                            break
                        }
                    }
                    if (!isBurn) {
                        var char: CharacterPool.Character? = null
                        when (strs[0]) {
                            "WARRIOR1" -> {
                                char = CharacterPool.Character.WARRIOR1
                            }
                            "WARRIOR2" -> {
                                char = CharacterPool.Character.WARRIOR2
                            }
                            "WARRIOR3" -> {
                                char = CharacterPool.Character.WARRIOR3
                            }
                            "WARRIOR4" -> {
                                char = CharacterPool.Character.WARRIOR4
                            }
                            "WARRIOR5" -> {
                                char = CharacterPool.Character.WARRIOR5
                            }
                            "WARRIOR6" -> {
                                char = CharacterPool.Character.WARRIOR6
                            }
                            "WARRIOR7" -> {
                                char = CharacterPool.Character.WARRIOR7
                            }
                            "WARRIOR8" -> {
                                char = CharacterPool.Character.WARRIOR8
                            }
                        }
                        players.add(Actor(600, 500, Global.UNIT_X - 5, UNIT_Y - 5, char!!, null, serialNum, strs[1], Actor.Team.RED))

                        //回傳角色創建資訊，雖然scenebegin已經傳過一次，但後連線的人不會解讀到。
                        ClientClass.getInstance().sent(CONNECT, bale("${players[0].char}", players[0].name!!))
                    }
                }
                //更新其他玩家位置，本地只運作本地玩家的移動，其他玩家的各種狀態在要改變時才傳封包
                Global.Command.UPDATE -> {
                    for (i in players) {
                        //如果該封包serialNum是本地的話則不做處理
                        if (players[0].id == serialNum) {
                            break
                        }
                        if (i.id == serialNum) {
                            i.setPosition(strs[0].toInt(), strs[1].toInt())
                            i.key = strs[2].toInt()
                            break
                        }
                    }
                }
                //更新其他玩家的角色狀態
                Global.Command.INPUT -> {
                    for (i in players) {
                        if (players[0].id == serialNum) {
                            break
                        }
                        if (i.id == serialNum) {
                            when (strs[0]) {
                                "RUN" -> {
                                    i.char.animator.setSta(CharacterPool.CharAnimator.State.RUN)
                                }
                                "WALK" -> {
                                    i.char.animator.setSta(CharacterPool.CharAnimator.State.WALK)
                                }
                                "NORMAL" -> {
                                    i.char.animator.setSta(CharacterPool.CharAnimator.State.NORMAL)
                                }
                                "LEFT" -> {
                                    i.dir = Global.Direction.LEFT
                                }
                                "RIGHT" -> {
                                    i.dir = Global.Direction.RIGHT
                                }
                                "UP" -> {
                                    i.dir = Global.Direction.UP
                                    i.changedY = -i.jumpHeight
                                    if (!IS_DEBUG) {
                                        i.jumped = true
                                    }
                                }
                                "START" -> {
                                    i.skillState = SkillPool.SkillState.START
                                }
                                "SPPEDDOUBLE" -> {
                                    i.skill = SkillPool.Skill.DOUBLESPEED
                                }
                                "BURST" -> {
                                    i.skill = SkillPool.Skill.BURST
                                }
                                "HAWKEYE" -> {
                                    i.skill = SkillPool.Skill.HAWKEYE
                                }
                                "DARKEYE" -> {
                                    i.skill = SkillPool.Skill.DARK
                                }
                                "TIMESTOP" -> {
                                    i.skill = SkillPool.Skill.TIMESTOP
                                }
                                "EARTHQUAKE" -> {
                                    i.skill = SkillPool.Skill.EARTHQUAKE
                                }
                                "FLY" -> {
                                    i.skill = SkillPool.Skill.FLY
                                }
                            }
                            break
                        }
                    }
                }
                //遊戲開始
                GAMESTART -> {
                    if (players[0].id != serialNum) {
                        var ghost: Actor? = null
                        for (i in players) {
                            if (i.id == strs[1].toInt()) {
                                ghost = i
                            }
                        }
                        if (strs[0].toInt() == 4) {
                            for (i in players) {
                                if (i == ghost) {
                                    i.team = Actor.Team.BLUE
                                } else {
                                    i.team = Actor.Team.RED
                                }
                            }
                        }
                        SceneController.instance.change(InternetMainScene(players, strs[0].toInt(), ghost!!, strs[2].toInt()))
                    }
                }
                //設定隊伍
                CHANGE_TEAM -> {
                    for (i in players) {
                        if (players[0].id == serialNum) {
                            break
                        }
                        if (i.id == serialNum) {
                            when (strs[0]) {
                                "red" -> {
                                    i.team = Actor.Team.RED
                                }
                                "blue" -> {
                                    i.team = Actor.Team.BLUE
                                }
                                "yellow" -> {
                                    i.team = Actor.Team.YELLOW
                                }
                                "green" -> {
                                    i.team = Actor.Team.GREEN
                                }
                            }
                            break
                        }
                    }
                }
                //模式選擇
                GHOSTMISSION -> {
                    if (players[0].id != serialNum) {
                        when (strs[0]) {
                            "!ghostMission" -> {
                                ghostMission = false
                            }
                            "ghostMission" -> {
                                ghostMission = true
                            }
                        }
                    }
                }
                //Client創建AI
                GEN_AI -> {
                    if (!SERVER) {
                        var i = strs[0].toInt()
                        players.add(Ai(600, 500, UNIT_X - 5, UNIT_Y - 5, CharacterPool.Character.values()[Random.nextInt(8)], null, i, "Computer_$i", Actor.Team.RED, strs[1].toInt()))
                    }
                }
                //都選同色時，自動分隊，照id分隊
                AUTO_TEAM -> {
                    if (players[0].id != serialNum) {
                        for (i in 0 until players.size) {
                            players[i].team = Actor.Team.values()[(players[i].id - 100) % 4]
                        }
                    }
                }
            }
        }
        charUpdate(players[0], timePassed)
        //鏡頭更新
        cameras.forEach { it.update(timePassed) }
    }

    override fun paint(g: Graphics) {
        var i = 0
        g.drawImage(bgImg, 0, 0, 1200, 600, null)
        cameras.forEach {
            it.run {
                start(g)
                items.forEach { it.paint(g) }
                bricks.forEach { it.paint(g) }
                players.forEach {
                    it.paint(g)
                    //名字的顏色，跟隊伍同色
                    if (it.team == Actor.Team.RED) {
                        g.color = Color.red
                    } else if (it.team == Actor.Team.BLUE) {
                        g.color = Color.blue
                    } else if (it.team == Actor.Team.YELLOW) {
                        g.color = Color.yellow
                    } else if (it.team == Actor.Team.GREEN) {
                        g.color = Color.green
                    }
                    g.drawString(it.name, it.collider.centerX - 16, it.collider.centerY - 16)
                }
                end(g)
            }
        }
        g.color = Color.BLACK
        //畫開始按鈕
        if (SERVER) {
            button!!.paint(g)
        }
        if (ghostMission) {
            g.drawImage(ResController.instance.image(Path.Imgs.Objs.MISSION2), 560, 75, 50, 50, null)
        } else {
            g.drawImage(ResController.instance.image(Path.Imgs.Objs.MISSION1), 560, 75, 50, 50, null)
        }
    }

    // 改寫input的getter 作法
    override val input: ((GameKernel.Input.Event) -> Unit)?
        get() = { e ->
            run {
                when (e) {
                    is GameKernel.Input.Event.KeyKeepPressed -> {
                        players.forEach {
                            it.input?.invoke(e)
                        }
                        cameras.forEach {
                            it.input?.invoke(e)
                        }
                    }
                    //debug模式可調鑰匙數量
                    is GameKernel.Input.Event.KeyPressed -> {
                        players.forEach {
                            if (e.data.keyCode != KeyEvent.VK_G) {
                                it.input?.invoke(e)
                            }
                        }
                        if (SERVER) {
                            MouseTriggerImpl.mouseTrig(this.button!!, e)
                        }
                    }
                    is GameKernel.Input.Event.KeyReleased -> {
                        players.forEach {
                            it.input?.invoke(e)
                        }
                    }
//                    //按鈕可以被按
//                    is GameKernel.Input.Event.MousePressed -> {
//                        if (SERVER) {
//                            MouseTriggerImpl.mouseTrig(this.button!!, e)
//                        }
//                    }
//                    //滑鼠經過時按鈕大小改變
//                    is GameKernel.Input.Event.MouseMoved -> {
//                        if (SERVER) {
//                            MouseTriggerImpl.mouseTrig(this.button!!, e)
//                        }
//                    }
                    else -> {}
                }
            }
        }

    private fun mapInitialize() {
        val mainMap = MapLoader(Path.Maps.WAITMAP_BMP, Path.Maps.WAITMAP_TXT).combineInfo()
        for (tmp in mainMap) {
            val info = intArrayOf(tmp.x, tmp.y, tmp.sizeX, tmp.sizeY)
            when (tmp.name) {
                "plat1" -> bricks.add(Brick(UNIT_X * info[0] + UNIT_X * info[2] / 2,
                        UNIT_Y * info[1] + UNIT_Y * info[3] / 2,
                        UNIT_X * info[2], UNIT_Y * info[3], Brick.BrickType.PLAT3))
                "plat2" -> bricks.add(Brick(UNIT_X * info[0] + UNIT_X * info[2] / 2,
                        UNIT_Y * info[1] + UNIT_Y * info[3] / 2,
                        UNIT_X * info[2], UNIT_Y * info[3], Brick.BrickType.PLAT4))
                "gemRed" -> items.add(ChooseItem(UNIT_X * info[0] + UNIT_X * info[2] / 2,
                        UNIT_Y * info[1] + UNIT_Y * info[3] / 2,
                        UNIT_X * info[2], UNIT_Y * info[3], ChooseItem.ItemType.GEM_RED))
                "gemBlue" -> items.add(ChooseItem(UNIT_X * info[0] + UNIT_X * info[2] / 2,
                        UNIT_Y * info[1] + UNIT_Y * info[3] / 2,
                        UNIT_X * info[2], UNIT_Y * info[3], ChooseItem.ItemType.GEM_BLUE))
                "gemGreen" -> items.add(ChooseItem(UNIT_X * info[0] + UNIT_X * info[2] / 2,
                        UNIT_Y * info[1] + UNIT_Y * info[3] / 2,
                        UNIT_X * info[2], UNIT_Y * info[3], ChooseItem.ItemType.GEM_GREEN))
                "gemYellow" -> items.add(ChooseItem(UNIT_X * info[0] + UNIT_X * info[2] / 2,
                        UNIT_Y * info[1] + UNIT_Y * info[3] / 2,
                        UNIT_X * info[2], UNIT_Y * info[3], ChooseItem.ItemType.GEM_YELLOW))
                "mission1" -> items.add(ChooseItem(UNIT_X * info[0] + UNIT_X * info[2] / 2,
                        UNIT_Y * info[1] + UNIT_Y * info[3] / 2,
                        UNIT_X * info[2], UNIT_Y * info[3], ChooseItem.ItemType.MISSION1))
                "mission2" -> items.add(ChooseItem(UNIT_X * info[0] + UNIT_X * info[2] / 2,
                        UNIT_Y * info[1] + UNIT_Y * info[3] / 2,
                        UNIT_X * info[2], UNIT_Y * info[3], ChooseItem.ItemType.MISSION2))
            }
        }
    }

}