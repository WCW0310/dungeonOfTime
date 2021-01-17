package scene

import camera.Camera
import camera.MapInformation
import camera.SmallMap
import camera.SpotLight
import core.GameKernel
import core.Scene
import core.controllers.ResController
import core.controllers.SceneController
import core.utils.Delay
import maploader.MapInfo
import maploader.MapLoader
import network.ClientClass
import network.ClientClass.getInstance
import obj.utils.Actor.Actor
import obj.utils.Actor.Ai
import obj.utils.Actor.CharacterPool
import obj.utils.GameObject
import obj.utils.Obj.Brick
import obj.utils.Obj.Item
import obj.utils.SkillPool
import utils.Global
import utils.Global.Command.GENITEM
import utils.Global.Command.ITEMSET
import utils.Global.Command.ITEMSTART
import utils.Global.Command.REMOVEITEM
import utils.Global.Command.TOUCH_GHOST
import utils.Global.Command.UPDATE_AI
import utils.Global.Command.VICTORY
import utils.Global.Command.bale
import utils.Global.SERVER
import utils.Global.UNIT_X
import utils.Global.UNIT_Y
import utils.Global.isTeam
import utils.Path
import utils.TimeLine
import java.awt.Color
import java.awt.Font
import java.awt.Graphics
import java.util.ArrayList
import kotlin.random.Random

class InternetMainScene(val players: MutableList<Actor>, val r: Int, val ghost: Actor, val map: Int) : Scene() {
    //所有平台 + 四周牆
    private val bricks: MutableList<Brick> = mutableListOf()

    //道具
    private val items: MutableList<Item> = mutableListOf()

    //鏡頭改良版，限制視野
    private val spotLights: MutableList<SpotLight> = mutableListOf()

    //小地圖 //鏡頭長寬 + 1 是因為最後Double 轉 Int 有誤差無法印出完整碰撞框
    private val smallMap: SmallMap = SmallMap(Camera.Builder(Global.UNIT_X * 100 + 1, Global.UNIT_Y * 50 + 1).setChaseObj(null).setCameraStartLocation(0, 0).setCameraWindowLocation(1200 - (Global.UNIT_X * 100 * 0.1).toInt() - Global.UNIT_X, 400).gen(), 0.1, 0.1)

    // 創建時間線
    lateinit var timeLine: TimeLine

    // 生成鑰匙的Delay
    val keyGenDelay = Delay(180)
    val treasureChestGenDelay = Delay(300)

    //場景美編設定
    private val keyRed = ResController.instance.image(Path.Imgs.Objs.KEY_RED)
    private val keyBlue = ResController.instance.image(Path.Imgs.Objs.KEY_BLUE)
    private val keyYellow = ResController.instance.image(Path.Imgs.Objs.KEY_YELLOW)
    private val keyGreen = ResController.instance.image(Path.Imgs.Objs.KEY_GREEN)

    private val font = Font("Algerian", Font.BOLD, 35)
    private val bgImg = ResController.instance.image(Path.Imgs.Backgrounds.MAINSCENE600)

    //勝利條件
    lateinit var mission: () -> Unit
    var redKey = 0
    var blueKey = 0
    var yellowKey = 0
    var greenKey = 0
    var redPick = false
    var bluePick = false
    var yellowPick = false
    var greenPick = false
    var redDelay = Delay(30)
    var blueDelay = Delay(30)
    var yellowDelay = Delay(30)
    var greenDelay = Delay(30)


    val setMission = {
        when (r) {
            1 -> mission = MissionMode().Battle1
            2 -> mission = MissionMode().Battle2
            3 -> mission = MissionMode().Battle3
            4 -> mission = MissionMode().Battle4
        }
        isTeam = true
        timeLine = TimeLine(Global.WINDOW_WIDTH / 2 - 200, 0, 400, 32, r)
    }

    inner class MissionMode {
        //判斷輸贏
        val Battle1 = {
            if (isTeam) {
                when {
                    (redKey >= 10) -> {
                        getInstance().sent(VICTORY, bale("1"))
                        val winner = "RedTeam: " + winner(Actor.Team.RED)
                        SceneController.instance.change(InternetGameOverScene(winner))
                    }
                    (blueKey >= 10) -> {
                        getInstance().sent(VICTORY, bale("2"))
                        val winner = "BlueTeam: " + winner(Actor.Team.BLUE)
                        SceneController.instance.change(InternetGameOverScene(winner))
                    }
                    (yellowKey >= 10) -> {
                        getInstance().sent(VICTORY, bale("10"))
                        val winner = "YellowTeam: " + winner(Actor.Team.YELLOW)
                        SceneController.instance.change(InternetGameOverScene(winner))
                    }
                    (greenKey >= 10) -> {
                        getInstance().sent(VICTORY, bale("11"))
                        val winner = "GreenTeam: " + winner(Actor.Team.GREEN)
                        SceneController.instance.change(InternetGameOverScene(winner))
                    }
                }
            }
        }// 最先拿到10把鑰匙獲勝
        val Battle2 = {
            if (timeLine.count <= 0) {
                if (isTeam) {
                    if (redKey > blueKey && redKey > yellowKey && redKey > greenKey) {
                        getInstance().sent(VICTORY, bale("1"))
                        val winner = "RedTeam: " + winner(Actor.Team.RED)
                        SceneController.instance.change(InternetGameOverScene(winner))
                    } else if (blueKey > redKey && blueKey > yellowKey && blueKey > greenKey) {
                        getInstance().sent(VICTORY, bale("2"))
                        val winner = "BlueTeam: " + winner(Actor.Team.BLUE)
                        SceneController.instance.change(InternetGameOverScene(winner))
                    } else if (yellowKey > redKey && yellowKey > blueKey && yellowKey > greenKey) {
                        getInstance().sent(VICTORY, bale("10"))
                        val winner = "YellowTeam: " + winner(Actor.Team.YELLOW)
                        SceneController.instance.change(InternetGameOverScene(winner))
                    } else if (greenKey > redKey && greenKey > blueKey && greenKey > yellowKey) {
                        getInstance().sent(VICTORY, bale("11"))
                        val winner = "GreenTeam: " + winner(Actor.Team.GREEN)
                        SceneController.instance.change(InternetGameOverScene(winner))
                    } else {
                        getInstance().sent(VICTORY, bale("5"))
                        val winner = "The match ended in a tie."
                        SceneController.instance.change(InternetGameOverScene(winner))
                    }
                }
            }
        }// 60秒內拿最多鑰匙獲勝
        val Battle3 = {
            if (timeLine.count <= 0 || timeLine.count >= timeLine.max) {
                var winner = ""
                winner = if (timeLine.count <= 0) {
                    getInstance().sent(VICTORY, bale("2"))
                    "BlueTeam: " + winner(Actor.Team.BLUE)
                } else {
                    getInstance().sent(VICTORY, bale("1"))
                    "RedTeam: " + winner(Actor.Team.RED)
                }
                SceneController.instance.change(InternetGameOverScene(winner))
            }
        } // 兩人鑰匙扣血模式.只有分兩隊的模式
        val Battle4 = {
            if (timeLine.count <= 0 || timeLine.count >= timeLine.max) {
                var winnerInfo = ""
                if (timeLine.count <= 0) {
                    getInstance().sent(Global.Command.VICTORY, Global.Command.bale("8"))
                    winnerInfo = "Boss:" + winner(Actor.Team.BLUE)
                } else {
                    getInstance().sent(Global.Command.VICTORY, Global.Command.bale("9"))
                    winnerInfo = "People: " + winner(Actor.Team.RED)
                }
                SceneController.instance.change(InternetGameOverScene(winnerInfo))
            }
        } // 鬼抓人模式

        fun sum(team: Actor.Team) {
            var tmp = 0
            players.forEach {
                if (it.team!! == team) {
                    tmp += it.key
                }
            }
            when (team) {
                Actor.Team.RED -> redKey = tmp
                Actor.Team.BLUE -> blueKey = tmp
                Actor.Team.YELLOW -> yellowKey = tmp
                Actor.Team.GREEN -> greenKey = tmp
            }
        }

        fun winner(team: Actor.Team): String {
            var str = ""
            players.forEach {
                if (it.team!! == team) {
                    str += it.name?.plus("  ") ?: it.char.charName + " "
                }
            }
            return str
        }
    }

    override fun sceneBegin() {
        Global.log("Internet Game Start")
        AudioResourceController.getInstance().loop(Path.Sounds.MAINSCENEBGM, -1)
        redKey = 0
        blueKey = 0
        yellowKey = 0
        greenKey = 0
        setMission()
        Global.birthPlaces.clear()//出生點重製，一定要寫在地圖畫出來之前，不然又會被重製
        mapInitialize()
        keyGenDelay.loop()
        treasureChestGenDelay.loop()
        //mission是Unit沒有所謂值可以比較，除非有設定回傳值
        if (r == 4) {
            var i = 0
            while (i < players.size) {
                if (players[i] == ghost) {
                    //鬼的資訊一開始就傳進來了，所以每個玩家各自設定
                    setGhost(ghost)
                    break
                }
                i++
            }
        }
        spotLights.add(SpotLight(Camera.Builder(Global.WINDOW_WIDTH, Global.SCREEN_Y).setChaseObj(players[0], 5.0, 5.0).setCameraWindowLocation(300, 0).gen(), players[0].radius, 0, 0, Global.WINDOW_WIDTH, Global.SCREEN_Y))
    }

    override fun sceneEnd() {
        ResController.instance.clear()  // 新版GameKernel 處裡了舊場景會被清掉的問題，所以直接這邊處裡即可
        Global.log("Game End")
    }

    //設定鬼的狀態與圖片
    fun setGhost(ghost: Actor) {
        ghost.run {
            ghost.ghost = true
            changedSpeed *= 1.5
            jumpHeight *= 1.5
            skillCdtime = Delay(skillCdtime.countLimit / 2)
        }
    }

    //角色更新與物件的碰撞判定，鬼抓人模式的鬼碰人
    private fun charUpdate(char: Actor, timePassed: Long) {
        char.fall()//垂直位移，重力
        for (i in bricksInCamera(spotLights[0], bricks)) {
            //角色碰撞磚塊、角色左!=磚塊右、角色右!=磚塊左
            if (char.isCollision(i) && char.collider.left != i.collider.right && char.collider.right != i.collider.left) {
                //角色底低於磚塊頂、角色頂高於磚塊頂
                if (char.collider.bottom > i.collider.top && char.collider.top < i.collider.top) {
                    char.setPosition(char.collider.centerX, i.collider.top - Global.UNIT_Y / 2)
                    char.changedY = 0.0
                    char.jumped = false
                }
                //角色頂高於磚塊底、角色底低於磚塊底
                else if (char.collider.top < i.collider.bottom && char.collider.bottom > i.collider.bottom) {
                    char.setPosition(char.collider.centerX, i.collider.bottom + Global.UNIT_Y / 2)
                }
            }
        }
        char.update(timePassed)//左右位移，限定不能出視窗，技能更新，動畫更新
        for (i in bricksInCamera(spotLights[0], bricks)) {
            //角色碰撞磚塊、角色底!=磚塊頂、角色頂!=磚塊底
            if (char.isCollision(i) && char.collider.bottom != i.collider.top && char.collider.top != i.collider.bottom) {
                //角色右右於磚塊左、角色左左於磚塊左
                if (char.collider.right > i.collider.left && char.collider.left < i.collider.left) {
                    char.setPosition(i.collider.left - Global.UNIT_X / 2, char.collider.centerY)
                }
                //角色左左於磚塊右、角色右右於磚塊右
                else if (char.collider.left < i.collider.right && char.collider.right > i.collider.right) {
                    char.setPosition(i.collider.right + Global.UNIT_X / 2, char.collider.centerY)
                }
            }
        }
        //鬼抓人模式，判斷人碰到鬼，自己不是鬼時才執行
        if (r == 4 && char != ghost) {
            if (ghost.isCollision(char)) {
                do {
                    var rePoint = false
                    char.setPosition(UNIT_X * 4 + (Math.random() * UNIT_X * 96).toInt(),
                            UNIT_Y * 4 + (Math.random() * UNIT_X * 46).toInt())
                    for (brick in bricks) {
                        if (char.isCollision(brick)) {
                            rePoint = true
                            break
                        }
                    }
                } while (rePoint)
                getInstance().sent(TOUCH_GHOST, bale(""))
                var tmp = if (redKey >= 3) 3 else redKey
                bluePick = true
                blueDelay.play()
                char.key -= tmp
                char.state = Actor.State.DEATH
                char.deathDelay.play()
            }
        }
        //判定角色碰到鑰匙、寶箱 //不會用for迴圈寫
        var i = 0
        while (i < items.size - 1) {
            var item = items[i]
            if (char.isCollision(item) && item.type == Item.ItemType.KEY_PURPLE && !char.ghost) {
                AudioResourceController.getInstance().shot(Path.Sounds.PICKKEY)
                char.key += 1
                getInstance().sent(REMOVEITEM, bale("$i"))
                when (char.team) {
                    Actor.Team.RED -> {
                        redPick = true
                        redDelay.play()
                    }
                    Actor.Team.BLUE -> {
                        bluePick = true
                        blueDelay.play()
                    }
                    Actor.Team.YELLOW -> {
                        yellowPick = true
                        yellowDelay.play()
                    }
                    Actor.Team.GREEN -> {
                        greenPick = true
                        greenDelay.play()
                    }
                }
                items.removeAt(i--)
            }
            if (char.isCollision(item) && item.type == Item.ItemType.TREASURE_BLUE) {
                if (item.state == SkillPool.SkillState.USEABLE) {
                    getInstance().sent(ITEMSET, bale("$i"))
                    item.user = char
                    item.state = SkillPool.SkillState.START
                }
                getInstance().sent(ITEMSTART, bale("$i"))
                item.update(timePassed, char, players)
            }
            if (item.user != null && char == item.user && !char.isCollision(item)) {
                getInstance().sent(ITEMSTART, bale("$i"))
                item.update(timePassed, char, players)
                if (item.state == SkillPool.SkillState.REMOVE) {
                    getInstance().sent(REMOVEITEM, bale("$i"))
                    items.removeAt(i--)
                }
            }
            i++
        }
    }

    private fun bricksInCamera(cam: Camera, objects: MutableList<Brick>): MutableList<GameObject> {
        var objectsInCamera: MutableList<GameObject> = mutableListOf()
        for (i in objects) {
            if (i.isCollision(cam)) {
                objectsInCamera.add(i)
            }
        }
        return objectsInCamera
    }

    override fun update(timePassed: Long) {
        //傳出更新資訊，本地角色位置x,y
        getInstance().sent(Global.Command.UPDATE, bale("${players[0].collider.centerX}", "${players[0].collider.centerY}", "${players[0].key}"))
        //SERVER端傳出AI資訊
        if (SERVER) {
            players.forEach {
                if (it is Ai) {
                    getInstance().sent(UPDATE_AI, bale("${it.id}", "${it.collider.centerX}", "${it.collider.centerY}", "${it.key}"))
                }
            }
        }
        //每次更新都先接收server傳回的資料，並依類型作不同處理
        getInstance().consume { serialNum: Int, commandCode: Int, strs: ArrayList<String> ->
            when (commandCode) {
                //勝利條件結果
                VICTORY -> {
                    if (players[0].id != serialNum) {
                        when (strs[0]) {
                            "1" -> {
                                val winner = "RedTeam: " + MissionMode().winner(Actor.Team.RED)
                                SceneController.instance.change(InternetGameOverScene(winner))
                            }
                            "2" -> {
                                val winner = "BlueTeam: " + MissionMode().winner(Actor.Team.BLUE)
                                SceneController.instance.change(InternetGameOverScene(winner))
                            }
                            "5" -> {
                                val winner = "The match ended in a tie."
                                SceneController.instance.change(InternetGameOverScene(winner))
                            }
                            "7" -> {
                                val winner = strs[1]
                                SceneController.instance.change(InternetGameOverScene(winner))
                            }
                            "8" -> {
                                var winnerInfo = "Boss: " + MissionMode().winner(Actor.Team.BLUE)
                                SceneController.instance.change(InternetGameOverScene(winnerInfo))
                            }
                            "9" -> {
                                var winnerInfo = "People: " + MissionMode().winner(Actor.Team.RED)
                                SceneController.instance.change(InternetGameOverScene(winnerInfo))
                            }
                            "10" -> {
                                val winner = "YellowTeam: " + MissionMode().winner(Actor.Team.YELLOW)
                                SceneController.instance.change(InternetGameOverScene(winner))
                            }
                            "11" -> {
                                val winner = "GreenTeam: " + MissionMode().winner(Actor.Team.GREEN)
                                SceneController.instance.change(InternetGameOverScene(winner))
                            }
                        }
                    }
                }
                //設定關卡
                Global.Command.SETMISSION -> {
                    if (players[0].id != serialNum) {
                        timeLine = TimeLine(Global.WINDOW_WIDTH / 2 - 200, 0, 400, 32, strs[0].toInt())
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
                //更新AI
                UPDATE_AI -> {
                    if (!SERVER) {
                        for (i in players) {
                            if (i.id == strs[0].toInt()) {
                                i.setPosition(strs[1].toInt(), strs[2].toInt())
                                i.key = strs[3].toInt()
                                break
                            }
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
                                "RUN" -> i.char.animator.setSta(CharacterPool.CharAnimator.State.RUN)
                                "WALK" -> i.char.animator.setSta(CharacterPool.CharAnimator.State.WALK)
                                "NORMAL" -> i.char.animator.setSta(CharacterPool.CharAnimator.State.NORMAL)
                                "LEFT" -> i.dir = Global.Direction.LEFT
                                "RIGHT" -> i.dir = Global.Direction.RIGHT
                                "UP" -> {
                                    i.dir = Global.Direction.UP
                                    i.changedY = -i.jumpHeight
                                    if (!Global.IS_DEBUG) {
                                        i.jumped = true
                                    }
                                }
                                "START" -> i.skillState = SkillPool.SkillState.START
                                "SPPEDDOUBLE" -> i.skill = SkillPool.Skill.DOUBLESPEED
                                "BURST" -> i.skill = SkillPool.Skill.BURST
                                "HAWKEYE" -> i.skill = SkillPool.Skill.HAWKEYE
                                "DARKEYE" -> i.skill = SkillPool.Skill.DARK
                                "TIMESTOP" -> i.skill = SkillPool.Skill.TIMESTOP
                                "EARTHQUAKE" -> i.skill = SkillPool.Skill.EARTHQUAKE
                                "FLY" -> i.skill = SkillPool.Skill.FLY
                            }
                            break
                        }
                    }
                }
                //CLIENT端更新SERVER端新增的地圖物件
                GENITEM -> {
                    if (!SERVER) {
                        when (strs[2]) {
                            "KEY_PURPLE" -> items.add(Item(strs[0].toInt(), strs[1].toInt(), UNIT_X, UNIT_Y, Item.ItemType.KEY_PURPLE))
                            "TREASURE_BLUE" -> {
                                var item = Item(strs[0].toInt(), strs[1].toInt(), UNIT_X, UNIT_Y, Item.ItemType.TREASURE_BLUE)
                                item.addSkill(strs[3].toInt())
                                items.add(item)
                            }
                        }
                    }
                }
                //移除其他玩家導致的道具移除
                REMOVEITEM -> {
                    if (players[0].id != serialNum) {
                        for (i in players) {
                            if (i.id == serialNum) {
                                when (i.team) {
                                    Actor.Team.RED -> {
                                        redPick = true
                                        redDelay.play()
                                    }
                                    Actor.Team.BLUE -> {
                                        bluePick = true
                                        blueDelay.play()
                                    }
                                    Actor.Team.YELLOW -> {
                                        yellowPick = true
                                        yellowDelay.play()
                                    }
                                    Actor.Team.GREEN -> {
                                        greenPick = true
                                        greenDelay.play()
                                    }
                                }
                                break
                            }
                        }
                        items.removeAt(strs[0].toInt())
                    }
                }
                //設定其他玩家造成的寶箱狀態轉換
                ITEMSET -> {
                    if (players[0].id != serialNum) {
                        var user: Actor? = null
                        for (i in players) {
                            if (i.id == serialNum) {
                                user = i
                                break
                            }
                        }
                        items[strs[0].toInt()].user = user
                        items[strs[0].toInt()].state = SkillPool.SkillState.START
                    }
                }
                ITEMSTART -> {
                    if (players[0].id != serialNum) {
                        var user: Actor? = null
                        for (i in players) {
                            if (i.id == serialNum) {
                                user = i
                                break
                            }
                        }
                        items[strs[0].toInt()].update(timePassed, user!!, players)
                    }
                }
                //人碰到鬼，鬼加鑰匙，改變動畫
                TOUCH_GHOST -> {
                    if (players[0].id != serialNum) {
                        bluePick = true
                        blueDelay.play()
                    }
                    if (players[0] == ghost) {
                        var tmp = if (redKey >= 3) 3 else redKey
                        ghost.key += tmp
                        ghost.state = Actor.State.KILL
                        ghost.deathDelay.play()
                    }
                }
            }
        }

        if (isTeam) {
            MissionMode().sum(Actor.Team.RED)
            MissionMode().sum(Actor.Team.BLUE)
            MissionMode().sum(Actor.Team.YELLOW)
            MissionMode().sum(Actor.Team.GREEN)
        }
        //SERVER端判斷輸贏，生成物件
        if (SERVER) {
            //判斷輸贏
            mission()
            //生成鑰匙
            var randomX: Int
            var randomY: Int
            if (keyGenDelay.count()) {
                var reGenKey: Boolean
                var newKey: Item
                do {
                    reGenKey = false
                    randomX = UNIT_X * 4 + (Math.random() * UNIT_X * 96).toInt()
                    randomY = UNIT_Y * 4 + (Math.random() * UNIT_X * 46).toInt()
                    newKey = Item(randomX, randomY, UNIT_X, UNIT_Y, Item.ItemType.KEY_PURPLE)
                    for (i in bricks) {
                        if (newKey.isCollision(i)) {
                            reGenKey = true
                            break
                        }
                    }
                } while (reGenKey)
                getInstance().sent(GENITEM, bale("$randomX", "$randomY", "${Item.ItemType.KEY_PURPLE}"))
                items.add(newKey)
            }
            //生成寶箱
            if (treasureChestGenDelay.count()) {
                var reGenTreasureChest: Boolean
                var newChest: Item
                do {
                    reGenTreasureChest = false
                    randomX = UNIT_X * 4 + (Math.random() * UNIT_X * 96).toInt()
                    randomY = UNIT_Y * 4 + (Math.random() * UNIT_X * 46).toInt()
                    newChest = Item(randomX, randomY, UNIT_X, UNIT_Y, Item.ItemType.TREASURE_BLUE)
                    for (i in bricks) {
                        if (newChest.isCollision(i)) {
                            reGenTreasureChest = true
                            break
                        }
                    }
                } while (reGenTreasureChest)

                var r = Random.nextInt(0, 12)

                newChest.addSkill(r)

                getInstance().sent(GENITEM, bale("$randomX", "$randomY", "${Item.ItemType.TREASURE_BLUE}", "$r"))
                items.add(newChest)
            }
        }

        //血條更新
        if (isTeam) {
            timeLine.update(timePassed, redKey, blueKey)
        } else {
            timeLine.update(timePassed)
        }

        //技能更新，用來讓技能正常使用
        players.forEach {
            it.skill.update(it, players)
        }

        //本地角色更新與物件的碰撞判定
        charUpdate(players[0], timePassed)
        //房主更新AI
        if (SERVER) {
            players.forEach {
                if (it is Ai) {
                    it.fall()//垂直位移，重力
                    for (i in bricks) {
                        //角色碰撞磚塊、角色左!=磚塊右、角色右!=磚塊左
                        if (it.isCollision(i) && it.collider.left != i.collider.right && it.collider.right != i.collider.left) {
                            //角色底低於磚塊頂、角色頂高於磚塊頂
                            if (it.collider.bottom > i.collider.top && it.collider.top < i.collider.top) {
                                it.setPosition(it.collider.centerX, i.collider.top - Global.UNIT_Y / 2)
                                it.changedY = 0.0
                                it.jumped = false
                            }
                            //角色頂高於磚塊底、角色底低於磚塊底
                            else if (it.collider.top < i.collider.bottom && it.collider.bottom > i.collider.bottom) {
                                it.setPosition(it.collider.centerX, i.collider.bottom + Global.UNIT_Y / 2)
                            }
                        }
                    }
                    it.update(timePassed)//左右位移，限定不能出視窗，技能更新，動畫更新
                    for (i in bricks) {
                        //角色碰撞磚塊、角色底!=磚塊頂、角色頂!=磚塊底
                        if (it.isCollision(i) && it.collider.bottom != i.collider.top && it.collider.top != i.collider.bottom) {
                            //角色右右於磚塊左、角色左左於磚塊左
                            if (it.collider.right > i.collider.left && it.collider.left < i.collider.left) {
                                it.setPosition(i.collider.left - Global.UNIT_X / 2, it.collider.centerY)
                            }
                            //角色左左於磚塊右、角色右右於磚塊右
                            else if (it.collider.left < i.collider.right && it.collider.right > i.collider.right) {
                                it.setPosition(i.collider.right + Global.UNIT_X / 2, it.collider.centerY)
                            }
                        }
                    }
                    //鬼抓人模式，判斷人碰到鬼，自己不是鬼時才執行
                    if (r == 4 && it != ghost) {
                        if (ghost.isCollision(it)) {
                            do {
                                var rePoint = false
                                it.setPosition(UNIT_X * 4 + (Math.random() * UNIT_X * 96).toInt(),
                                        UNIT_Y * 4 + (Math.random() * UNIT_X * 46).toInt())
                                for (brick in bricks) {
                                    if (it.isCollision(brick)) {
                                        rePoint = true
                                        break
                                    }
                                }
                            } while (rePoint)
                            getInstance().sent(TOUCH_GHOST, bale(""))
                            var tmp = if (redKey >= 3) 3 else redKey
                            bluePick = true
                            blueDelay.play()
                            it.key -= tmp
                            it.state = Actor.State.DEATH
                            it.deathDelay.play()
                        }
                    }
                    //判定角色碰到鑰匙、寶箱 //不會用for迴圈寫
                    var i = 0
                    while (i < items.size - 1) {
                        var item = items[i]
                        if (it.isCollision(item) && item.type == Item.ItemType.KEY_PURPLE && !it.ghost) {
                            AudioResourceController.getInstance().shot(Path.Sounds.PICKKEY)
                            it.key += 1
                            getInstance().sent(REMOVEITEM, bale("$i"))
                            when (it.team) {
                                Actor.Team.RED -> {
                                    redPick = true
                                    redDelay.play()
                                }
                                Actor.Team.BLUE -> {
                                    bluePick = true
                                    blueDelay.play()
                                }
                                Actor.Team.YELLOW -> {
                                    yellowPick = true
                                    yellowDelay.play()
                                }
                                Actor.Team.GREEN -> {
                                    greenPick = true
                                    greenDelay.play()
                                }
                            }
                            items.removeAt(i--)
                        }
                        if (it.isCollision(item) && item.type == Item.ItemType.TREASURE_BLUE) {
                            if (item.state == SkillPool.SkillState.USEABLE) {
                                getInstance().sent(ITEMSET, bale("$i"))
                                item.user = it
                                item.state = SkillPool.SkillState.START
                            }
                            getInstance().sent(ITEMSTART, bale("$i"))
                            item.update(timePassed, it, players)
                        }
                        if (item.user != null && it == item.user && !it.isCollision(item)) {
                            getInstance().sent(ITEMSTART, bale("$i"))
                            item.update(timePassed, it, players)
                            if (item.state == SkillPool.SkillState.REMOVE) {
                                getInstance().sent(REMOVEITEM, bale("$i"))
                                items.removeAt(i--)
                            }
                        }
                        i++
                    }
                }
            }
        }

        //其他角色更新角色動畫與技能動畫
        for (i in players) {
            if (i != players[0]) {
                i.char.animator.update()
                i.skillAnimator.update()
            }
        }

        //ai更新，瞬移技能
        if (SERVER) {
            players.forEach {
                if (it is Ai) {
                    if (it.delay.count()) {
                        if (Random.nextInt(1, 101) >= (100 - it.level)) {
                            var key: Item
                            do {
                                var reKey = false
                                key = items[Random.nextInt(items.size)]
                                if (key.type != Item.ItemType.KEY_PURPLE) {
                                    reKey = true
                                }
                            } while (reKey)
                            it.setPosition(key.centenX, key.centenY)
                        }
                    }
                }
            }
        }

        //鏡頭更新
        spotLights.forEach { it.update(timePassed) }
        //小地圖更新
        smallMap.update(timePassed)
    }

    override fun paint(g: Graphics) {
        var i = 0
        spotLights.forEach { it ->
            it.radius = players[i].radius
            it.dist = players[i].state.dist
            it.colors = players[i++].state.colors
            it.run {
                start(g)
                g.drawImage(bgImg,
                        painter.left, painter.top, painter.width, painter.height, null)
                for (item in items) {
                    if (isCollision(item)) {
                        item.paint(g)
                    }
                }
                for (brick in bricks) {
                    if (isCollision(brick)) {
                        brick.paint(g)
                    }
                }
                players.forEach {
                    if (isCollision(it)) {
                        it.paint(g)
                        //名字的顏色，跟隊伍同色
                        when (it.team) {
                            Actor.Team.RED -> g.color = Color.red
                            Actor.Team.BLUE -> g.color = Color.blue
                            Actor.Team.YELLOW -> g.color = Color.yellow
                            Actor.Team.GREEN -> g.color = Color.green
                        }
                        g.drawString(it.name, it.collider.centerX - 16, it.collider.centerY - 16)
                    }
                }
                end(g)
            }
        }
        smallMap.run {
            start(g)
            if (Global.IS_DEBUG) {
                for (item in items) {
                    if (isCollision(item)) {
                        smallMap.paint(g, item)
                    }
                }
                for (brick in bricks) {
                    if (isCollision(brick)) {
                        smallMap.paint(g, brick)
                    }
                }
            }
            for (i in players) {
                if (isCollision(i)) {
                    if (i == players[0]) {
                        smallMap.paint(g, i, Color.red, 100)
                        //是鬼的話印鏡頭框
                        if (i.ghost) {
                            paint(g, spotLights[0], Color.RED)
                        }
                    } else {
                        if (i.ghost) {
                        } else if (i.team == players[0].team) {
                            smallMap.paint(g, i, Color.green, 100)
                        } else {
                            smallMap.paint(g, i, Color.blue, 100)
                        }
                    }
                }
            }
            paint(g)
            end(g)
        }
        g.font = font
        g.color = Color.RED
        if (redPick) {
            g.drawImage(keyRed, 0, 10, 80, 80, null)
            if (redDelay.count()) {
                redPick = false
            }
        } else {
            g.drawImage(keyRed, 10, 10, 50, 50, null)
        }
        g.drawString("X " + redKey, 60, 55)
        g.color = Color.BLUE
        if (bluePick) {
            g.drawImage(keyBlue, 1030, 10, 80, 80, null)
            if (blueDelay.count()) {
                bluePick = false
            }
        } else {
            g.drawImage(keyBlue, 1050, 10, 50, 50, null)
        }
        g.drawString("X " + blueKey, 1100, 55)

        if (r != 3 && r != 4) {
            g.color = Color.YELLOW
            if (yellowPick) {
                g.drawImage(keyYellow, 158, 10, 80, 80, null)
                if (yellowDelay.count()) {
                    yellowPick = false
                }
            } else {
                g.drawImage(keyYellow, 178, 10, 50, 50, null)
            }
            for (i in players) {
                if (i.team == Actor.Team.YELLOW) {
                    g.drawString("X " + i.key, 228, 55)
                    break
                }
            }
            g.color = Color.GREEN
            if (greenPick) {
                g.drawImage(keyGreen, 816, 10, 80, 80, null)
                if (greenDelay.count()) {
                    greenPick = false
                }
            } else {
                g.drawImage(keyGreen, 836, 10, 50, 50, null)
            }
            for (i in players) {
                if (i.team == Actor.Team.GREEN) {
                    g.drawString("X " + i.key, 886, 55)
                    break
                }
            }
        }
        g.color = Color.BLACK
        timeLine.paint(g)
        if (players[0].skillState == SkillPool.SkillState.USEABLE) {
            g.drawImage(players[0].char.skillImg, 10, 500, 50, 50, null)
        }
    }

    override val input: ((GameKernel.Input.Event) -> Unit)?
        get() = { e ->
            run {
                when (e) {
                    is GameKernel.Input.Event.KeyKeepPressed -> {
                        players.forEach {
                            it.input?.invoke(e)
                        }
                        spotLights.forEach {
                            it.input?.invoke(e)
                        }
                    }
                    //debug模式可調鑰匙數量
                    is GameKernel.Input.Event.KeyPressed -> {
                        players.forEach {
                            it.input?.invoke(e)
                        }
                    }
                    is GameKernel.Input.Event.KeyReleased -> {
                        players.forEach {
                            it.input?.invoke(e)
                        }
                    }
                }
            }
        }


    private fun mapInitialize() {
        var mainMap:ArrayList<MapInfo> = arrayListOf()
        when(map){
            1 -> {
                mainMap = MapLoader(Path.Maps.MAINMAP1_BMP,Path.Maps.MAINMAP1_TXT).combineInfo()
                MapInformation.setMapInfo(UNIT_X * 4, UNIT_Y * 4, UNIT_X * 96, UNIT_Y * 46)
            }
            2 -> {
                mainMap = MapLoader(Path.Maps.MAINMAP2_BMP, Path.Maps.MAINMAP2_TXT).combineInfo()
                MapInformation.setMapInfo(UNIT_X * 4, UNIT_Y * 4, UNIT_X * 96, UNIT_Y * 46)
            }
        }

        MapInformation.setMapInfo(Global.UNIT_X * 4, Global.UNIT_Y * 4, Global.UNIT_X * 96, Global.UNIT_Y * 46)
        for (tmp in mainMap) {
            val info = intArrayOf(tmp.x, tmp.y, tmp.sizeX, tmp.sizeY)
            when (tmp.name) {
                "RedStart" -> {
                    items.add(Item(Global.UNIT_X * info[0] + Global.UNIT_X * info[2] / 2,
                            Global.UNIT_Y * info[1] + Global.UNIT_Y * info[3] / 2,
                            Global.UNIT_X * info[2], Global.UNIT_Y * info[3], Item.ItemType.START_RED))
                    Global.birthPlaces.add(UNIT_X * info[0] + UNIT_X * info[2] / 2)
                    Global.birthPlaces.add(UNIT_Y * info[1] + UNIT_Y * info[3] / 2)
                }
                "BlueStart" -> {
                    items.add(Item(Global.UNIT_X * info[0] + Global.UNIT_X * info[2] / 2,
                            Global.UNIT_Y * info[1] + Global.UNIT_Y * info[3] / 2,
                            Global.UNIT_X * info[2], Global.UNIT_Y * info[3], Item.ItemType.START_BLUE))
                    Global.birthPlaces.add(UNIT_X * info[0] + UNIT_X * info[2] / 2)
                    Global.birthPlaces.add(UNIT_Y * info[1] + UNIT_Y * info[3] / 2)
                }
                "TimeKey" -> items.add(Item(Global.UNIT_X * info[0] + Global.UNIT_X * info[2] / 2,
                        Global.UNIT_Y * info[1] + Global.UNIT_Y * info[3] / 2,
                        Global.UNIT_X * info[2], Global.UNIT_Y * info[3], Item.ItemType.KEY_PURPLE))
                "WallTop" -> bricks.add(Brick(Global.UNIT_X * info[0] + Global.UNIT_X * info[2] / 2,
                        Global.UNIT_Y * info[1] + Global.UNIT_Y * info[3] / 2,
                        Global.UNIT_X * info[2], Global.UNIT_Y * info[3], Brick.BrickType.CEILING))
                "WallLR" -> bricks.add(Brick(Global.UNIT_X * info[0] + Global.UNIT_X * info[2] / 2,
                        Global.UNIT_Y * info[1] + Global.UNIT_Y * info[3] / 2,
                        Global.UNIT_X * info[2], Global.UNIT_Y * info[3], Brick.BrickType.CONNER))
                "WallBottom" -> bricks.add(Brick(Global.UNIT_X * info[0] + Global.UNIT_X * info[2] / 2,
                        Global.UNIT_Y * info[1] + Global.UNIT_Y * info[3] / 2,
                        Global.UNIT_X * info[2], Global.UNIT_Y * info[3], Brick.BrickType.FLOOR))
                "Plat1" -> bricks.add(Brick(Global.UNIT_X * info[0] + Global.UNIT_X * info[2] / 2,
                        Global.UNIT_Y * info[1] + Global.UNIT_Y * info[3] / 2,
                        Global.UNIT_X * info[2], Global.UNIT_Y * info[3], Brick.BrickType.PLAT1))
                "Plat2" -> bricks.add(Brick(Global.UNIT_X * info[0] + Global.UNIT_X * info[2] / 2,
                        Global.UNIT_Y * info[1] + Global.UNIT_Y * info[3] / 2,
                        Global.UNIT_X * info[2], Global.UNIT_Y * info[3], Brick.BrickType.PLAT2))
                "Plat3" -> bricks.add(Brick(Global.UNIT_X * info[0] + Global.UNIT_X * info[2] / 2,
                        Global.UNIT_Y * info[1] + Global.UNIT_Y * info[3] / 2,
                        Global.UNIT_X * info[2], Global.UNIT_Y * info[3], Brick.BrickType.PLAT3))
                "Plat4" -> bricks.add(Brick(Global.UNIT_X * info[0] + Global.UNIT_X * info[2] / 2,
                        Global.UNIT_Y * info[1] + Global.UNIT_Y * info[3] / 2,
                        Global.UNIT_X * info[2], Global.UNIT_Y * info[3], Brick.BrickType.PLAT4))
                "woodPlat3" -> bricks.add(Brick(UNIT_X * info[0] + UNIT_X * info[2] / 2,
                        UNIT_Y * info[1] + UNIT_Y * info[3] / 2,
                        UNIT_X * info[2], UNIT_Y * info[3], Brick.BrickType.WOODPLAT3))
                "WoodLR" -> bricks.add(Brick(UNIT_X * info[0] + UNIT_X * info[2] / 2,
                        UNIT_Y * info[1] + UNIT_Y * info[3] / 2,
                        UNIT_X * info[2], UNIT_Y * info[3], Brick.BrickType.WOODLR))
                "WoodBottom" -> bricks.add(Brick(UNIT_X * info[0] + UNIT_X * info[2] / 2,
                        UNIT_Y * info[1] + UNIT_Y * info[3] / 2,
                        UNIT_X * info[2], UNIT_Y * info[3], Brick.BrickType.WOODBOTTOM))
            }
        }
    }
}