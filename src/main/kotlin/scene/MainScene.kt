package scene

import camera.Camera
import camera.MapInformation
import camera.SmallMap
import camera.SpotLight
import core.GameKernel
import core.controllers.ResController
import core.Scene
import core.controllers.SceneController
import core.utils.Delay
import maploader.MapInfo
import maploader.MapLoader
import menu.impl.OptionMenu
import obj.utils.Actor.Actor
import obj.utils.Actor.Ai
import obj.utils.Actor.CharacterPool
import obj.utils.GameObject
import obj.utils.Obj.Brick
import obj.utils.Obj.ChooseItem
import obj.utils.Obj.Item
import obj.utils.SkillPool
import utils.Global
import utils.Global.IS_DEBUG
import utils.Global.LEFT_1P
import utils.Global.LEFT_2P
import utils.Global.RIGHT_1P
import utils.Global.RIGHT_2P
import utils.Global.SCREEN_Y
import utils.Global.SKILL_1P
import utils.Global.SKILL_2P
import utils.Global.UNIT_X
import utils.Global.UNIT_Y
import utils.Global.UP_1P
import utils.Global.UP_2P
import utils.Global.WINDOW_HEIGHT
import utils.Global.WINDOW_WIDTH
import utils.Global.birthPlaces
import utils.Global.isTeam
import utils.Path
import utils.TimeLine
import java.awt.*
import java.awt.event.KeyEvent
import kotlin.random.Random

class MainScene(val chars: MutableList<CharacterPool.Character>) : Scene() {
    //單機雙人鍵盤組
    private val KEY_1P: MutableList<Int> = mutableListOf()
    private val KEY_2P: MutableList<Int> = mutableListOf()

    private val players: MutableList<Actor> = mutableListOf()
    private val bricks: MutableList<Brick> = mutableListOf()//所有平台 + 四周牆
    private val items: MutableList<Item> = mutableListOf()

    //鏡頭改良版，限制視野
    private val spotLights: MutableList<SpotLight> = mutableListOf()

    //小地圖 //鏡頭長寬 + 1 是因為最後Double 轉 Int 有誤差無法印出完整碰撞框
    private val smallMap: SmallMap = SmallMap(Camera.Builder(UNIT_X * 100 + 1, UNIT_Y * 50 + 1).setChaseObj(null).setCameraStartLocation(0, 0).setCameraWindowLocation(600 - (UNIT_X * 100 * 0.1 / 2).toInt(), 400).gen(), 0.1, 0.1)

    // 創建時間線
    lateinit var timeLine: TimeLine

    // 生成鑰匙、寶箱的Delay
    private val keyGenDelay = Delay(180)
    private val treasureChestGenDelay = Delay(300)

    //場景美編設定
    private val keyRed = ResController.instance.image(Path.Imgs.Objs.KEY_RED)
    private val keyBlue = ResController.instance.image(Path.Imgs.Objs.KEY_BLUE)
    private val font = Font("Algerian", Font.BOLD, 35)
    private val bgImg = ResController.instance.image(Path.Imgs.Backgrounds.MAINSCENE600)
    private lateinit var optionMenu: OptionMenu
    private var redPick = false
    private var bluePick = false
    private var redDelay = Delay(30)
    private var blueDelay = Delay(30)

    //勝利條件
    lateinit var mission: () -> Unit
    var redKey = 0
    var blueKey = 0

    val setMission = {
        val r = Random.nextInt(1, 4)
        when (r) {
            1 -> mission = MissionMode().battle1
            2 -> mission = MissionMode().battle2
            3 -> mission = MissionMode().battle3
        }
        timeLine = TimeLine(WINDOW_WIDTH / 2 - 200, 0, 400, 32, r)
    }

    inner class MissionMode {
        //判斷輸贏
        val battle1 = {
            if (isTeam) {
                if (redKey >= 10) {
                    val winner = "RedTeam: " + winner(Actor.Team.RED)
                    SceneController.instance.change(GameOverScene(winner))
                } else if (blueKey >= 10) {
                    val winner = "BlueTeam: " + winner(Actor.Team.BLUE)
                    SceneController.instance.change(GameOverScene(winner))
                }
            } else {
                players.forEach {
                    if (it.key >= 10) {
                        val winner = "" + it.char.charName
                        SceneController.instance.change(GameOverScene(winner))
                    }
                }
            }
        }// 最先拿到10把鑰匙獲勝
        val battle2 = {
            if (timeLine.count <= 0) {
                if (isTeam) {
                    when {
                        redKey > blueKey -> {
                            val winner = "RedTeam: " + winner(Actor.Team.RED)
                            SceneController.instance.change(GameOverScene(winner))
                        }
                        redKey == blueKey -> {
                            val winner = "The match ended in a tie."
                            SceneController.instance.change(GameOverScene(winner))
                        }
                        else -> {
                            val winner = "BlueTeam: " + winner(Actor.Team.BLUE)
                            SceneController.instance.change(GameOverScene(winner))
                        }
                    }
                } else {
                    var max = 0
                    for (i in 0..players.size - 2) {
                        if (players[i].key < players[i + 1].key) {
                            max = i + 1
                        }
                    }
                    val winner = "" + players[max].char.charName
                    SceneController.instance.change(GameOverScene(winner))
                }
            }
        }// 60秒內拿最多鑰匙獲勝
        val battle3 = {
            if (timeLine.count <= 0 || timeLine.count >= timeLine.max) {
                val winner = if (timeLine.count <= 0) {
                    if (isTeam) "BlueTeam: " + winner(Actor.Team.BLUE) else "BLUE_PLAYER"
                } else {
                    if (isTeam) "RedTeam: " + winner(Actor.Team.RED) else "RED_PLAYER"
                }
                SceneController.instance.change(GameOverScene(winner))
            }
        } // 兩人鑰匙扣血模式.

        fun sum(team: Actor.Team) {
            var tmp = 0
            players.forEach {
                if (it.team!!.equals(team)) {
                    tmp += it.key
                }
            }
            if (team == Actor.Team.RED) {
                redKey = tmp
            } else if (team == Actor.Team.BLUE) {
                blueKey = tmp
            }
        }

        fun winner(team: Actor.Team): String {
            var str = ""
            players.forEach {
                if (it.team!! == team) {
                    str += it.name?.plus("   ") ?: it.char.charName + "   "
                }
            }
            return str
        }
    }

    override fun sceneBegin() {
        Global.log("Game Start")
        AudioResourceController.getInstance().loop(Path.Sounds.MAINSCENEBGM, -1)
        setMission()
        birthPlaces.clear()
        mapInitialize()
        optionMenu = OptionMenu(WINDOW_WIDTH / 4, SCREEN_Y / 4, WINDOW_WIDTH / 2, WINDOW_HEIGHT / 2, chars)
        optionMenu.setCancelable()
        keyGenDelay.loop()
        treasureChestGenDelay.loop()
        players.add(Actor(256, 256, UNIT_X - 5, UNIT_Y - 5, chars[0], KEY_1P, 0, null, Actor.Team.RED))
        players.add(Actor(2916, 1280, UNIT_X - 5, UNIT_Y - 5, chars[1], KEY_2P, 0, null, Actor.Team.RED))
        spotLights.add(SpotLight(Camera.Builder(WINDOW_WIDTH / 2, SCREEN_Y).setChaseObj(players[0], 5.0, 5.0).setCameraWindowLocation(0, 0).gen(), players[0].radius, 0, 0, WINDOW_WIDTH / 2, SCREEN_Y))
        spotLights.add(SpotLight(Camera.Builder(WINDOW_WIDTH / 2, SCREEN_Y).setChaseObj(players[1], 5.0, 5.0).setCameraWindowLocation(WINDOW_WIDTH / 2, 0).gen(), players[1].radius, WINDOW_WIDTH / 2, 0, WINDOW_WIDTH, SCREEN_Y))
        if (isTeam) {

            players.add(Ai(256, 1280, UNIT_X - 5, UNIT_Y - 5, CharacterPool.Character.values()[Random.nextInt(6)], KEY_2P, 0, null, Actor.Team.BLUE, Random.nextInt(2, 7)))
            players.add(Ai(2916, 256, UNIT_X - 5, UNIT_Y - 5, CharacterPool.Character.values()[Random.nextInt(6)], KEY_2P, 0, null, Actor.Team.BLUE, Random.nextInt(2, 7)))
            spotLights.add(SpotLight(Camera.Builder(WINDOW_WIDTH / 2, SCREEN_Y).setChaseObj(players[2], 5.0, 5.0).setCameraWindowLocation(WINDOW_WIDTH + 50, 0).gen(), players[2].radius, WINDOW_WIDTH + 50, 0, WINDOW_WIDTH, SCREEN_Y))
            spotLights.add(SpotLight(Camera.Builder(WINDOW_WIDTH / 2, SCREEN_Y).setChaseObj(players[3], 5.0, 5.0).setCameraWindowLocation(WINDOW_WIDTH + 50, 0).gen(), players[3].radius, WINDOW_WIDTH + 50, 0, WINDOW_WIDTH, SCREEN_Y))
            players.forEach {
                if (it is Ai) {
                    it.delay.loop()
                }
            }
        }

        //鍵盤組加入global的按鍵配置
        KEY_1P.add(UP_1P)//w 0
        KEY_1P.add(LEFT_1P)//a 1
        KEY_1P.add(RIGHT_1P)//d 2
        KEY_1P.add(SKILL_1P)//g 3

        KEY_2P.add(UP_2P)//up 0
        KEY_2P.add(LEFT_2P)//left 1
        KEY_2P.add(RIGHT_2P)//right 2
        KEY_2P.add(SKILL_2P)//數字鍵盤0 3
    }

    override fun sceneEnd() {
        ResController.instance.clear()  // 新版GameKernel 處裡了舊場景會被清掉的問題，所以直接這邊處裡即可
        Global.log("Game End")
    }

    //角色更新與物件的碰撞判定
    private fun charUpdate(char: Actor, cam: Camera, timePassed: Long) {
        char.fall()//垂直位移，重力
        for (i in bricksInCamera(cam, bricks)) {
            //角色碰撞磚塊、角色左!=磚塊右、角色右!=磚塊左
            if (char.isCollision(i) && char.collider.left != i.collider.right && char.collider.right != i.collider.left) {
                //角色底低於磚塊頂、角色頂高於磚塊頂
                if (char.collider.bottom > i.collider.top && char.collider.top < i.collider.top) {
                    char.setPosition(char.collider.centerX, i.collider.top - UNIT_Y / 2)
                    char.changedY = 0.0
                    char.jumped = false
                    //與其他角色碰撞時
                    for (actor in players) {
                        if (actor != char) {
                            if (char.isCollision(actor)) {
                                if (actor.collider.right > char.collider.left && actor.collider.left < char.collider.left) {
                                    actor.setPosition(char.collider.left - UNIT_X / 2, actor.collider.centerY)
                                } else if (actor.collider.left < char.collider.right && actor.collider.right > char.collider.right) {
                                    actor.setPosition(char.collider.right + UNIT_X / 2, actor.collider.centerY)
                                } else {
                                    actor.setPosition(char.collider.centerX, char.collider.top - UNIT_Y / 2)
                                }
                            }
                        }
                    }
                }
                //角色頂高於磚塊底、角色底低於磚塊底
                else if (char.collider.top < i.collider.bottom && char.collider.bottom > i.collider.bottom) {
                    char.setPosition(char.collider.centerX, i.collider.bottom + UNIT_Y / 2)
                    //與其他角色碰撞時
                    for (actor in players) {
                        if (actor != char) {
                            if (char.isCollision(actor)) {
                                if (actor.collider.right > char.collider.left && actor.collider.left < char.collider.left) {
                                    actor.setPosition(char.collider.left - UNIT_X / 2, actor.collider.centerY)
                                } else if (actor.collider.left < char.collider.right && actor.collider.right > char.collider.right) {
                                    actor.setPosition(char.collider.right + UNIT_X / 2, actor.collider.centerY)
                                } else {
                                    actor.setPosition(char.collider.centerX, char.collider.bottom + UNIT_Y / 2)
                                }
                            }
                        }
                    }
                }
            }
        }
        char.update(timePassed)//左右位移，限定不能出視窗，技能更新，動畫更新
        for (i in bricksInCamera(cam, bricks)) {
            //角色碰撞磚塊、角色底!=磚塊頂、角色頂!=磚塊底
            if (char.isCollision(i) && char.collider.bottom != i.collider.top && char.collider.top != i.collider.bottom) {
                //角色右右於磚塊左、角色左左於磚塊左
                if (char.collider.right > i.collider.left && char.collider.left < i.collider.left) {
                    char.setPosition(i.collider.left - UNIT_X / 2, char.collider.centerY)
                    //與其他角色碰撞時
                    for (actor in players) {
                        if (actor != char) {
                            if (char.isCollision(actor)) {
                                actor.setPosition(char.collider.left - UNIT_X / 2, actor.collider.centerY)
                            }
                        }
                    }
                }
                //角色左左於磚塊右、角色右右於磚塊右
                else if (char.collider.left < i.collider.right && char.collider.right > i.collider.right) {
                    char.setPosition(i.collider.right + UNIT_X / 2, char.collider.centerY)
                    //與其他角色碰撞時
                    for (actor in players) {
                        if (actor != char) {
                            if (char.isCollision(actor)) {
                                actor.setPosition(char.collider.right + UNIT_X / 2, actor.collider.centerY)
                            }
                        }
                    }
                }
            }
        }
        //判定角色碰到鑰匙、寶箱
        var i = 0
        while (i < items.size - 1) {
            val item = items[i]
            if (char.isCollision(item) && item.type == Item.ItemType.KEY_PURPLE) {
                AudioResourceController.getInstance().shot(Path.Sounds.PICKKEY)
                char.key += 1
                if(char == players[0]){
                    redPick = true
                    redDelay.play()
                }else{
                    bluePick = true
                    blueDelay.play()
                }
                items.removeAt(i--)
            }
            if (char.isCollision(item) && item.type == Item.ItemType.TREASURE_BLUE) {
                if (item.state == SkillPool.SkillState.USEABLE) {
                    item.user = char
                    item.state = SkillPool.SkillState.START
                }
            }
            if (item.user != null && char == item.user) {
                item.update(timePassed, char, players)
                if (item.state == SkillPool.SkillState.REMOVE) {
                    items.removeAt(i--)
                }
            }
            i++
        }
    }

    //角色彼此碰撞判定//判斷從哪個方位來，changedX，changedY//下一幀會碰撞就要*-1，不能讓他們碰在一起
    private fun charsCollide(char: Actor, char2: Actor) {
        val nextFrameCollider1: obj.utils.Rect = obj.utils.Rect(char.collider)
        val nextFrameCollider2: obj.utils.Rect = obj.utils.Rect(char2.collider)
        //移動至下一幀位置
        nextFrameCollider1.translate(char.changedX.toInt(), char.changedY.toInt())
        nextFrameCollider2.translate(char2.changedX.toInt(), char2.changedY.toInt())
        if (nextFrameCollider1.overlap(nextFrameCollider2)) {
            if (nextFrameCollider1.bottom >= nextFrameCollider2.top || nextFrameCollider1.top <= nextFrameCollider2.bottom) {
                char.changedY *= -1.0
            }
            if ((nextFrameCollider1.right >= nextFrameCollider2.left || nextFrameCollider1.left <= nextFrameCollider2.right)) {
                char.changedX *= -1.0
            }
        }
    }

    private fun bricksInCamera(cam: Camera, objects: MutableList<Brick>): List<GameObject> {
        val objectsInCamera: MutableList<GameObject> = mutableListOf()
        for (i in objects) {
            if (i.isCollision(cam)) {
                objectsInCamera.add(i)
            }
        }
        return objectsInCamera
    }

    override fun update(timePassed: Long) {
        if (!optionMenu.isShow) {
            //判斷輸贏
            if (isTeam) {
                MissionMode().sum(Actor.Team.RED)
                MissionMode().sum(Actor.Team.BLUE)
            }
            mission()
            //生成鑰匙
            if (keyGenDelay.count()) {
                genItem(Item.ItemType.KEY_PURPLE)
            }
            //生成寶箱
            if (treasureChestGenDelay.count()) {
                genItem(Item.ItemType.TREASURE_BLUE)
            }
            //血條更新
            if (isTeam) {
                timeLine.update(timePassed, redKey, blueKey)
            } else {
                timeLine.update(timePassed, players[0], players[1])
            }
            //技能更新，用來讓技能正常使用
            players.forEach {
                it.skill.update(it, players)
            }
            //角色彼此碰撞
            charsCollide(players[0], players[1])
            charsCollide(players[1], players[0])
            //角色更新與物件的碰撞判定
            for (i in 0 until players.size) {
                charUpdate(players[i], spotLights[i], timePassed)
            }
            //ai更新
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
            //鏡頭更新
            spotLights.forEach { it.update(timePassed) }
            //小地圖更新
            smallMap.update(timePassed)
        }
    }

    fun genItem(type: Item.ItemType) {
        var newItem: Item
        do {
            var reItem = false
            val randomX = UNIT_X * 4 + (Math.random() * UNIT_X * 96).toInt()
            val randomY = UNIT_Y * 4 + (Math.random() * UNIT_X * 46).toInt()
            newItem = Item(randomX, randomY, UNIT_X, UNIT_Y, type)
            for (i in bricks) {
                if (newItem.isCollision(i)) {
                    reItem = true
                    break
                }
            }
        } while (reItem)
        if (newItem.type == Item.ItemType.TREASURE_BLUE) {
            newItem.addSkill(Random.nextInt(0, 12))
        }
        items.add(newItem)
    }

    override fun paint(g: Graphics) {
        var i = 0
        spotLights.forEach {
            if (!(it.obj() is Ai)) {
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
                    for (j in 0..players.size - 1) {
                        if (isCollision(players[j])) {
                            players[j].paint(g)
                        }
                    }
                    end(g)
                }
            }
        }
        smallMap.run {
            start(g)
            if (IS_DEBUG) {
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
            for (i in players.indices) {
                if (i == 0) {
                    smallMap.paint(g, players[0], Color.red, 100)
                } else if (i == 1 && isTeam) {
                    smallMap.paint(g, players[1], Color.green, 100)
                } else {
                    smallMap.paint(g, players[i], Color.blue, 100)
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
        if (isTeam) {
            g.drawString("X " + redKey, 60, 55)
        } else {
            g.drawString("X " + players[0].key, 60, 55)
        }
        g.color = Color.BLUE
        if (bluePick) {
            g.drawImage(keyBlue, 1030, 10, 80, 80, null)
            if (blueDelay.count()) {
                bluePick = false
            }
        } else {
            g.drawImage(keyBlue, 1050, 10, 50, 50, null)
        }
        if (isTeam) {
            g.drawString("X " + blueKey, 1100, 55)
        } else {
            g.drawString("X " + players[1].key, 1100, 55)
        }
        if (players[0].skillState == SkillPool.SkillState.USEABLE) {
            g.drawImage(players[0].char.skillImg, 10, 500, 50, 50, null)
        }
        if (players[1].skillState == SkillPool.SkillState.USEABLE) {
            g.drawImage(players[1].char.skillImg, 1145, 500, 50, 50, null)
        }
        timeLine.paint(g)
        if (optionMenu.isShow) {
            optionMenu.paint(g)
        }
    }

    // 改寫input的getter 作法
    override val input: ((GameKernel.Input.Event) -> Unit)?
        get() = { e ->
            run {
                when (e) {
                    is GameKernel.Input.Event.KeyKeepPressed -> {
                        if (!optionMenu.isShow) {
                            players.forEach {
                                it.input?.invoke(e)
                            }
                            spotLights.forEach {
                                it.input?.invoke(e)
                            }
                        }
                    }
                    //debug模式可調鑰匙數量
                    is GameKernel.Input.Event.KeyPressed -> {
                        if (!optionMenu.isShow) {
                            players.forEach {
                                it.input?.invoke(e)
                            }
                            if (e.data.keyCode == KeyEvent.VK_B && IS_DEBUG) {
                                players[0].key++
                            } else if (e.data.keyCode == KeyEvent.VK_N && IS_DEBUG) {
                                players[1].key++
                            }
                        }
                        if (e.data.keyCode == KeyEvent.VK_P) {
                            if (optionMenu.isShow) {
                                optionMenu.hide()
                                optionMenu.sceneEnd()
                            } else {
                                optionMenu.sceneBegin()
                                optionMenu.show()
                            }
                        }
                    }
                    is GameKernel.Input.Event.KeyReleased -> {
                        if (!optionMenu.isShow) {
                            players.forEach {
                                it.input?.invoke(e)
                            }
                        }
                    }
                    is GameKernel.Input.Event.MousePressed -> {
                        if (optionMenu.isShow) {
                            optionMenu.mouseListener().invoke(e)
                        }
                    }
                    is GameKernel.Input.Event.MouseMoved -> {
                        if (optionMenu.isShow) {
                            optionMenu.mouseListener().invoke(e)
                        }
                    }
                }
            }
        }

    private fun mapInitialize() {
        var mainMap:ArrayList<MapInfo> = arrayListOf()
        when(Random.nextInt(1,3)){
            1 -> {
                mainMap = MapLoader(Path.Maps.MAINMAP1_BMP,Path.Maps.MAINMAP1_TXT).combineInfo()
                MapInformation.setMapInfo(UNIT_X * 4, UNIT_Y * 4, UNIT_X * 96, UNIT_Y * 46)
            }
            2 -> {
                mainMap = MapLoader(Path.Maps.MAINMAP2_BMP, Path.Maps.MAINMAP2_TXT).combineInfo()
                MapInformation.setMapInfo(UNIT_X * 4, UNIT_Y * 4, UNIT_X * 96, UNIT_Y * 46)
            }
        }
        for (tmp in mainMap) {
            val info = intArrayOf(tmp.x, tmp.y, tmp.sizeX, tmp.sizeY)
            when (tmp.name) {
                "RedStart" -> {
                    items.add(Item(UNIT_X * info[0] + UNIT_X * info[2] / 2,
                            UNIT_Y * info[1] + UNIT_Y * info[3] / 2,
                            UNIT_X * info[2], UNIT_Y * info[3], Item.ItemType.START_RED))
                    birthPlaces.add(UNIT_X * info[0] + UNIT_X * info[2] / 2)
                    birthPlaces.add(UNIT_Y * info[1] + UNIT_Y * info[3] / 2)
                }
                "BlueStart" -> {
                    items.add(Item(UNIT_X * info[0] + UNIT_X * info[2] / 2,
                            UNIT_Y * info[1] + UNIT_Y * info[3] / 2,
                            UNIT_X * info[2], UNIT_Y * info[3], Item.ItemType.START_BLUE))
                    birthPlaces.add(UNIT_X * info[0] + UNIT_X * info[2] / 2)
                    birthPlaces.add(UNIT_Y * info[1] + UNIT_Y * info[3] / 2)
                }
                "TimeKey" -> items.add(Item(UNIT_X * info[0] + UNIT_X * info[2] / 2,
                        UNIT_Y * info[1] + UNIT_Y * info[3] / 2,
                        UNIT_X * info[2], UNIT_Y * info[3], Item.ItemType.KEY_PURPLE))
                "TreasureBlue" -> {
                    val treasere = Item(UNIT_X * info[0] + UNIT_X * info[2] / 2,
                            UNIT_Y * info[1] + UNIT_Y * info[3] / 2,
                            UNIT_X * info[2], UNIT_Y * info[3], Item.ItemType.TREASURE_BLUE)
                    treasere.addSkill(Random.nextInt(0, 12))
                    items.add(treasere)
                }
                "WallTop" -> bricks.add(Brick(UNIT_X * info[0] + UNIT_X * info[2] / 2,
                        UNIT_Y * info[1] + UNIT_Y * info[3] / 2,
                        UNIT_X * info[2], UNIT_Y * info[3], Brick.BrickType.CEILING))
                "WallLR" -> bricks.add(Brick(UNIT_X * info[0] + UNIT_X * info[2] / 2,
                        UNIT_Y * info[1] + UNIT_Y * info[3] / 2,
                        UNIT_X * info[2], UNIT_Y * info[3], Brick.BrickType.CONNER))
                "WallBottom" -> bricks.add(Brick(UNIT_X * info[0] + UNIT_X * info[2] / 2,
                        UNIT_Y * info[1] + UNIT_Y * info[3] / 2,
                        UNIT_X * info[2], UNIT_Y * info[3], Brick.BrickType.FLOOR))
                "Plat1" -> bricks.add(Brick(UNIT_X * info[0] + UNIT_X * info[2] / 2,
                        UNIT_Y * info[1] + UNIT_Y * info[3] / 2,
                        UNIT_X * info[2], UNIT_Y * info[3], Brick.BrickType.PLAT1))
                "Plat2" -> bricks.add(Brick(UNIT_X * info[0] + UNIT_X * info[2] / 2,
                        UNIT_Y * info[1] + UNIT_Y * info[3] / 2,
                        UNIT_X * info[2], UNIT_Y * info[3], Brick.BrickType.PLAT2))
                "Plat3" -> bricks.add(Brick(UNIT_X * info[0] + UNIT_X * info[2] / 2,
                        UNIT_Y * info[1] + UNIT_Y * info[3] / 2,
                        UNIT_X * info[2], UNIT_Y * info[3], Brick.BrickType.PLAT3))
                "Plat4" -> bricks.add(Brick(UNIT_X * info[0] + UNIT_X * info[2] / 2,
                        UNIT_Y * info[1] + UNIT_Y * info[3] / 2,
                        UNIT_X * info[2], UNIT_Y * info[3], Brick.BrickType.PLAT4))
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