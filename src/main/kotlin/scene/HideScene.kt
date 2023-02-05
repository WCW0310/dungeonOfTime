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
import maploader.MapLoader
import menu.impl.OptionMenu
import network.ClientClass
import obj.utils.Actor.Actor
import obj.utils.Actor.CharacterPool
import obj.utils.GameObject
import obj.utils.Obj.Brick
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

// 鬼抓人模次Debug用
class HideScene(val chars:MutableList<CharacterPool.Character>) : Scene() {
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
    private val smallMap2: SmallMap = SmallMap(Camera.Builder(Global.UNIT_X * 100 + 1, Global.UNIT_Y * 50 + 1).setChaseObj(null).setCameraStartLocation(0, 0).setCameraWindowLocation(0, 400).gen(), 0.1, 0.1)

    // 創建時間線
    lateinit var timeLine: TimeLine

    // 生成鑰匙、寶箱的Delay
    val keyGenDelay = Delay(180)
    val treasureChestGenDelay = Delay(300)

    //場景美編設定
    private val keyRed = ResController.instance.image(Path.Imgs.Objs.KEY_RED)
    private val keyBlue = ResController.instance.image(Path.Imgs.Objs.KEY_BLUE)
    private val font = Font("Algerian", Font.BOLD, 35)
    private val bgImg = ResController.instance.image(Path.Imgs.Backgrounds.MAINSCENE600)
    private lateinit var optionMenu: OptionMenu

    //勝利條件
    lateinit var mission: () -> Unit
    var redKey = 0
    var blueKey = 0
    private lateinit var ghost:Actor
    var redPick = false
    var bluePick = false
    var redDelay = Delay(30)
    var blueDelay = Delay(30)

    val setMission = {
        mission = MissionMode().Battle4
        isTeam = true
        ClientClass.getInstance().sent(Global.Command.SETMISSION, Global.Command.bale("$4"))
        timeLine = TimeLine(Global.WINDOW_WIDTH / 2 - 200, 0, 400, 32, 4)
    }

    inner class MissionMode {
        //判斷輸贏
        val Battle4 = {
            if (timeLine.count <= 0 || timeLine.count >= timeLine.max) {
                var winnerInfo =""
                if (timeLine.count <= 0) {
                    ClientClass.getInstance().sent(Global.Command.VICTORY, Global.Command.bale("2"))
                    winnerInfo = "People: " + winner("Blue")
                } else {
                    ClientClass.getInstance().sent(Global.Command.VICTORY, Global.Command.bale("1"))
                    winnerInfo = "Boss: " + winner("Red")
                }
                SceneController.instance.change(InternetGameOverScene(winnerInfo))
            }
        } // 鬼抓人模式

        fun sum(team: String) {
            var tmp = 0
            players.forEach {
                if (it.team!!.equals(team)) {
                    tmp += it.key
                }
            }
            if (team.equals("Red")) {
                redKey = tmp
            } else if (team.equals("Blue")) {
                blueKey = tmp
            }
        }

        fun winner(team: String): String {
            var str = ""
            players.forEach {
                if (it.team!!.equals(team)) {
                    str += it.name ?: it.char.charName + " "
                }
            }
            return str
        }
    }

    override fun sceneBegin() {
        Global.log("Game Start")
        isTeam = false
        AudioResourceController.getInstance().loop(Path.Sounds.MAINSCENEBGM, -1)
        setMission()
        birthPlaces.clear()
        mapInitialize()
        optionMenu = OptionMenu(WINDOW_WIDTH / 4, SCREEN_Y / 4, WINDOW_WIDTH / 2, WINDOW_HEIGHT / 2, chars)
        optionMenu.setCancelable()
        MapInformation.setMapInfo(UNIT_X * 4, UNIT_Y * 4, UNIT_X * 96, UNIT_Y * 46)
        keyGenDelay.loop()
        treasureChestGenDelay.loop()
        players.add(Actor(256, 256, UNIT_X - 5, UNIT_Y - 5, chars[0], KEY_1P, 0, null,Actor.Team.RED))
        players.add(Actor(2916, 256, UNIT_X - 5, UNIT_Y - 5, chars[1], KEY_2P, 0, null,Actor.Team.BLUE))
        var r = Random.nextInt(0,players.size)
        setGhost(r)
        spotLights.add(SpotLight(Camera.Builder(WINDOW_WIDTH / 2, SCREEN_Y).setChaseObj(players[0], 5.0, 5.0).setCameraWindowLocation(0, 0).gen(), players[0].radius, 0, 0, WINDOW_WIDTH / 2, SCREEN_Y))
        spotLights.add(SpotLight(Camera.Builder(WINDOW_WIDTH / 2, SCREEN_Y).setChaseObj(players[1], 5.0, 5.0).setCameraWindowLocation(WINDOW_WIDTH / 2, 0).gen(), players[1].radius, WINDOW_WIDTH / 2, 0, WINDOW_WIDTH, SCREEN_Y))

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

    fun setGhost(r: Int){
        players[r].run{
            ghost = true
            changedSpeed *= 1.5
            jumpHeight *= 1.5
            skillCdtime = Delay(skillCdtime.countLimit / 2)
        }
        ghost = players[r]
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
                }
                //角色頂高於磚塊底、角色底低於磚塊底
                else if (char.collider.top < i.collider.bottom && char.collider.bottom > i.collider.bottom) {
                    char.setPosition(char.collider.centerX, i.collider.bottom + UNIT_Y / 2)
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
                }
                //角色左左於磚塊右、角色右右於磚塊右
                else if (char.collider.left < i.collider.right && char.collider.right > i.collider.right) {
                    char.setPosition(i.collider.right + UNIT_X / 2, char.collider.centerY)
                }
            }
        }
        //與其他角色碰撞時
        for (actor in players) {
            if (actor != ghost) {
                if (ghost.isCollision(actor)) {
                    var tmp = 0
                    if(ghost.team!! == Actor.Team.RED){
                        tmp = if(blueKey >= 5)5 else  blueKey
                        redPick = true
                        redDelay.play()
                    }else{
                        tmp = if(redKey >= 5)5 else  redKey
                        bluePick = true
                        blueDelay.play()
                    }
                    ghost.key += tmp
                    actor.key -= tmp
                    ghost.state = Actor.State.KILL
                    ghost.deathDelay.play()
                    actor.state = Actor.State.DEATH
                    actor.deathDelay.play()
                    do {
                        var rePoint = false
                        actor.setPosition(UNIT_X * 4 + (Math.random() * UNIT_X * 96).toInt(),
                                UNIT_Y * 4 + (Math.random() * UNIT_X * 46).toInt())
                        for (brick in bricks) {
                            if (actor.isCollision(brick)) {
                                rePoint = true
                                break
                            }
                        }
                    } while (rePoint)
                }
            }
        }
        //判定角色碰到鑰匙、寶箱 //不會用for迴圈寫
        var i = 0
        while (i < items.size - 1) {
            var item = items[i]
            if (char.isCollision(item) && item.type == Item.ItemType.KEY_PURPLE && !char.ghost) {
                AudioResourceController.getInstance().shot(Path.Sounds.PICKKEY)
                char.key += 1
                if(char.team!! == Actor.Team.RED){
                    redPick = true
                    redDelay.play()
                }else if(char.team!! == Actor.Team.BLUE){
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

    private fun bricksInCamera(cam: Camera, objects: MutableList<Brick>): List<GameObject> {
        return cam.isCollision(objects)
    }

    override fun update(timePassed: Long) {
        if (!optionMenu.isShow) {
            //判斷輸贏
            if(isTeam){
                MissionMode().sum("Red")
                MissionMode().sum("Blue")
            }
            mission()
            //生成鑰匙、寶箱
            if (keyGenDelay.count()) {
                var reGenKey: Boolean = false
                var newKey: Item
                do {
                    reGenKey = false
                    newKey = Item(UNIT_X * 4 + (Math.random() * UNIT_X * 96).toInt(), UNIT_Y * 4 + (Math.random() * UNIT_X * 46).toInt(), UNIT_X, UNIT_Y, Item.ItemType.KEY_PURPLE)
                    for (i in bricks) {
                        if (newKey.isCollision(i)) {
                            reGenKey = true
                            break
                        }
                    }
                } while (reGenKey)
                items.add(newKey)
            }
            if (treasureChestGenDelay.count()) {
                var reGenTreasureChest: Boolean = false
                var newChest: Item
                do {
                    reGenTreasureChest = false
                    newChest = Item(UNIT_X * 4 + (Math.random() * UNIT_X * 96).toInt(), UNIT_Y * 4 + (Math.random() * UNIT_X * 46).toInt(), UNIT_X, UNIT_Y, Item.ItemType.TREASURE_BLUE)
                    for (i in bricks) {
                        if (newChest.isCollision(i)) {
                            reGenTreasureChest = true
                            break
                        }
                    }
                } while (reGenTreasureChest)
                var r = Random.nextInt(0,12)
                newChest.addSkill(r)
                items.add(newChest)
            }

            //血條更新
            if(isTeam){
                timeLine.update(timePassed, redKey, blueKey)
            }else{
                timeLine.update(timePassed, players[0], players[1])
            }

            //技能更新，用來讓技能正常使用
            players.forEach {
                it.skill.update(it, players)
            }
            //角色更新與物件的碰撞判定
            for (i in 0 until players.size) {
                charUpdate(players[i], spotLights[i], timePassed)
            }
            //鏡頭更新
            spotLights.forEach { it.update(timePassed) }
            //小地圖更新
            smallMap.update(timePassed)
            smallMap2.update(timePassed)
        }
    }

    override fun paint(g: Graphics) {
        var i = 0
        spotLights.forEach {
            it.radius = players[i].radius
            it.dist = players[i].state.dist
            it.colors = players[i++].state.colors
            it.run {
                start(g)
                g.drawImage(bgImg,
                        painter.left, painter.top, painter.width, painter.height, null)
                //先畫道具，鑰匙在地圖生成時，多層牆中會卡鑰匙，後畫的Brick會把卡牆鑰匙蓋住
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
        smallMap.run {
            start(g)
            if (IS_DEBUG) {
                //先畫道具，鑰匙在地圖生成時，多層牆中會卡鑰匙，後畫的Brick會把卡牆鑰匙蓋住
                for (item in items) {
                    if (isCollision(item)) {
                        paint(g, item)
                    }
                }
                for (brick in bricks) {
                    if (isCollision(brick)) {
                        paint(g, brick)
                    }
                }
            }
            if (isCollision(players[0])) {
                if(!players[0].ghost){
                    paint(g, players[0], Color.red, 100)
                }
            }
            if (isCollision(players[1])) {
                if(!players[1].ghost) {
                    paint(g, players[1], Color.blue, 100)
                }
            }
            paint(g)
            end(g)
        }
        smallMap2.run {
            start(g)
            if (Global.IS_DEBUG) {
                //先畫道具，鑰匙在地圖生成時，多層牆中會卡鑰匙，後畫的Brick會把卡牆鑰匙蓋住
                for (item in items) {
                    if (isCollision(item)) {
                        paint(g, item)
                    }
                }
                for (brick in bricks) {
                    if (isCollision(brick)) {
                        paint(g, brick)
                    }
                }
            }
            for (i in players) {
                if (isCollision(i)) {
                    if (i == players[0]) {
                        paint(g, i, Color.red, 100)
                    } else {
                        paint(g, i, Color.blue, 100)
                    }
                }
            }
            for(i in 0 until players.size){
                if(players[i].ghost){
                    paint(g,spotLights[i],Color.RED)
                }
            }
            paint(g)
            end(g)
        }

        g.font = font
        g.color = Color.RED
        if(redPick){
            g.drawImage(keyRed, 0, 10, 80, 80, null)
            if(redDelay.count()){
                redPick = false
            }
        }else{
            g.drawImage(keyRed, 10, 10, 50, 50, null)
        }
        if(isTeam){
            g.drawString("X " + redKey, 60, 55)
        }else{
            g.drawString("X " + players[0].key, 60, 55)
        }
        g.color = Color.BLUE
        if(bluePick){
            g.drawImage(keyBlue, 1030, 10, 80, 80, null)
            if(blueDelay.count()){
                bluePick = false
            }
        }else{
            g.drawImage(keyBlue, 1050, 10, 50, 50, null)
        }
        if(isTeam){
            g.drawString("X " + blueKey, 1100, 55)
        }else{
            g.drawString("X " + players[1].key, 1100, 55)
        }
        g.color = Color.BLACK
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
                        } else {
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
                        } else {
                        }
                    }
                    is GameKernel.Input.Event.MouseMoved -> {
                        if (optionMenu.isShow) {
                            optionMenu.mouseListener().invoke(e)
                        } else {
                        }
                    }
                    else -> {}
                }
            }
        }

    private fun mapInitialize() {
        val mainMap = MapLoader("mainMap.bmp",
                "mainMap.txt").combineInfo()
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
                    var r = Random.nextInt(0,12)
                    var tmp = Item(UNIT_X * info[0] + UNIT_X * info[2] / 2,
                            UNIT_Y * info[1] + UNIT_Y * info[3] / 2,
                            UNIT_X * info[2], UNIT_Y * info[3],
                            Item.ItemType.TREASURE_BLUE)
                    tmp.addSkill(r)
                    items.add(tmp)
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
            }
        }
    }
}