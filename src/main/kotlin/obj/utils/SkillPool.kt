package obj.utils

import core.controllers.ResController
import core.utils.Delay
import network.ClientClass
import obj.utils.Actor.Actor
import obj.utils.Obj.Item
import utils.Global
import utils.Global.UNIT_X
import utils.Global.UNIT_Y
import utils.Global.birthPlaces
import utils.Global.isTeam
import utils.Path
import java.awt.Graphics
import java.awt.Image
import java.awt.Point
import kotlin.random.Random

object SkillPool {

    enum class SkillState {
        USEABLE,  // 可以使用技能
        START,   // 技能開啟
        ONUSED,  // 技能持續中 ，順發技能delay countLimit = 0
        USED,    // 技能CD中
        REMOVE;  // 道具技能刪除
    }

    interface Action {
        fun action(state: SkillState, User: Actor, Affected: MutableList<Actor>)
    }

    val itemSkill: MutableList<Skill> = mutableListOf()

    init {
        for (i in 8 until Skill.values().size) {
            itemSkill.add(Skill.values()[i])
        }
    }

    class SkillAnimator(val skill: Skill) {

        private var timeDelay = Delay(skill.animatorTime)
        private var delay = Delay(skill.skillSpeed)
        private var count = 0
        private var paint = true

        init {
            delay.loop()
            timeDelay.play()
            paint = true
        }

        fun reset() {
            delay.loop()
            timeDelay.play()
            paint = true
        }

        fun paint(left: Int, top: Int, right: Int, bottom: Int, g: Graphics) {
            if (paint) {
                g.drawImage(skill.img, left - (right - left), top - (bottom - top),
                        right + (right - left), bottom + (bottom - top),
                        UNIT_X * 2 * skill.skillArr[count],
                        0,
                        UNIT_X * 2 + UNIT_X * 2 * skill.skillArr[count],
                        UNIT_Y * 2, null)
            }
        }

        var i = 0

        fun update() {
            if (delay.count()) {
                count = ++count % skill.skillArr.size
            }
            if (timeDelay.count()) {
                paint = false
                timeDelay.stop()
            }
        }

    }

    enum class Skill(val skillName: String,val keepTime: Int, val cdTime: Int,val img: Image,
                     val skillSpeed: Int, val skillArr: IntArray, var animatorTime: Int) : Action {
        //skillSpeed 是技能施放動畫的速度; skillArr 是技能施放的動畫圖陣列； animatorTime 是技能動畫持續時間）

        DOUBLESPEED("DoubleSpeed",180, 180,
                ResController.instance.image(Path.Imgs.Skills.DOUBLESPEED), 20,
                intArrayOf(0, 1, 2), 180) {
            override fun action(state: SkillState, User: Actor, Affected: MutableList<Actor>) {
                User.run {
                    if (state == SkillState.START) {
                        changedSpeed *= 1.5
                        tmp = jumpHeight
                        jumpHeight += 10
                        AudioResourceController.getInstance().shot(Path.Sounds.Skills.DOUBLESPEED)
                        this.state = Actor.State.DOUBLESPEED
                    }
                    if (state == SkillState.USED) { // 持續時間結束後要做的事情
                        changedSpeed = if(changedSpeed  / 1.5 <= char.changedSpeed) char.changedSpeed else changedSpeed / 1.5
                        jumpHeight = if(tmp < 20) 20.0 else tmp
                        this.state = Actor.State.NORMAL
                    }
                }
            }
        },
        BURST("Burst",0, 90,
                ResController.instance.image(Path.Imgs.Skills.BURST), 5,
                intArrayOf(0, 1, 2, 3, 4, 5), 30) {
            override fun action(state: SkillState, User: Actor, Affected: MutableList<Actor>) {
                User.run {
                    if (state == SkillState.START) {
                        AudioResourceController.getInstance().shot(Path.Sounds.Skills.BRUST)
                        if (dir == Global.Direction.LEFT) {
                            changedX += -20
                        } else if (dir == Global.Direction.RIGHT) {
                            changedX += 20.0
                        }
                    }
                }
            }
        },
        HAWKEYE("HawkEye",300, 180,
                ResController.instance.image(Path.Imgs.Skills.HAWKEYE), 10,
                intArrayOf(0, 1, 2, 3, 4, 5), 60) {
            override fun action(state: SkillState, User: Actor, Affected: MutableList<Actor>) {
                User.run {
                    if  (state == SkillState.START){
                        AudioResourceController.getInstance().shot(Path.Sounds.Skills.HAWKEYE)
                    }
                    if (state == SkillState.ONUSED) {
                        if (radius < 750.0f){
                            radius += 5
                        }
                    }
                    if (state == SkillState.USED) {
                        radius = char.radius
                    }
                }
            }
        },
        DARK("Dark",180, 180,
                ResController.instance.image(Path.Imgs.Skills.DARK), 10,
                intArrayOf(0, 1, 2, 3, 4, 5), 60) {
            override fun action(state: SkillState, User: Actor, Affected: MutableList<Actor>) {
                User.run {
                    if(state == SkillState.START){
                        AudioResourceController.getInstance().shot(Path.Sounds.Skills.DARK)
                    }
                    Affected.forEach {
                        if(isTeam && User.team != null && it.team!! == team!!){}else{
                            if (this != it) {
                                if (state == SkillState.START){
                                    tmp = it.radius.toDouble()
                                }
                                if (state == SkillState.ONUSED){
                                    if(it.radius > tmp/2 ){
                                        it.radius -= 5
                                    }
                                }
                                if (state == SkillState.USED) {
                                    it.radius = it.char.radius
                                }
                            }
                        }
                    }
                }
            }
        },
        TIMESTOP("TimeStop",120, 600,
                ResController.instance.image(Path.Imgs.Skills.TIMESTOP), 15,
                intArrayOf(0, 1, 2, 3, 4, 5, 6, 7), 120) {
            override fun action(state: SkillState, User: Actor, Affected: MutableList<Actor>) {
                User.run {
                    if(state == SkillState.START){
                        AudioResourceController.getInstance().shot(Path.Sounds.Skills.TIMESTOP)
                    }
                    Affected.forEach {
                        if(isTeam && User.team != null && it.team!! == team){}else{
                            if (this != it) {
                                if (state == SkillState.ONUSED) {
                                    it.changedSpeed = 0.0
                                    tmp = jumpHeight
                                    it.jumpHeight = 0.0
                                    it.state = Actor.State.TIMESTOP
                                }
                                if (state == SkillState.USED) {
                                    it.changedSpeed = if(it.ghost)it.char.changedSpeed * 1.5 else it.char.changedSpeed
                                    it.jumpHeight = if(tmp < 20) 20.0 else tmp
                                    it.state = Actor.State.NORMAL
                                }
                            }
                        }
                    }
                }
            }
        },
        EARTHQUAKE("Earthquake",120,300,
                ResController.instance.image(Path.Imgs.Skills.EARTHQUAKE), 30,
                intArrayOf(0, 1, 2), 90) {
            override fun action(state: SkillState, User: Actor, Affected: MutableList<Actor>) {
                User.run {
                    if(state == SkillState.START){
                        AudioResourceController.getInstance().shot(Path.Sounds.Skills.EARTHQUAKE)
                    }
                    Affected.forEach {
                        if(isTeam && User.team != null && it.team!! == team!!){}else{
                            if (this != it) {
                                if (this != it) {
                                    if (state == SkillState.START) {
                                        tmp = Random.nextDouble(-0.2, 0.0)
                                        it.changedSpeed += tmp
                                        it.state = Actor.State.EARTHQUAKE
                                    }
                                    if (state == SkillState.ONUSED) {
                                        it.changedY += Random.nextInt(-10, 10)
                                        if (it.changedY >= 20) {
                                            it.changedY -= 10
                                        } else if (it.changedY <= -20) {
                                            it.changedY += 10
                                        }
                                    }
                                    if (state == SkillState.USED) {
                                        it.changedSpeed -= tmp
                                        it.state = Actor.State.NORMAL
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        FLY("Fly",20, 0,
                ResController.instance.image(Path.Imgs.Skills.FLY), 5,
                intArrayOf(0, 1), 300) {
            override fun action(state: SkillState, User: Actor, Affected: MutableList<Actor>) {
                User.run {
                    if(state == SkillState.START){
                        AudioResourceController.getInstance().shot(Path.Sounds.Skills.FLY)
                    }
                    if (state == SkillState.ONUSED) {
                        changedY += -3
                    }
                }
            }
        },
        MAGICPOWER("MagicPower",60, 5,
                ResController.instance.image(Path.Imgs.Skills.MAGICPOWER), 15,
                intArrayOf(0, 1, 2, 3), 60) {
            override fun action(state: SkillState, User: Actor, Affected: MutableList<Actor>) {
                User.run {
                    if(state == SkillState.START){
                        AudioResourceController.getInstance().shot(Path.Sounds.Skills.MAGICPOWER)
                    }
                    if (state == SkillState.USED) {
                        skill = values()[Random.nextInt(0, 7)]
                        ClientClass.getInstance().sent(Global.Command.INPUT, Global.Command.bale("$skill"))
                        skillState = SkillState.USEABLE
                        skillAnimator = SkillAnimator(skill)
                        skillKeeptime = Delay(skill.keepTime)
                        skillCdtime = if(ghost) Delay(skill.cdTime /2) else Delay(skill.cdTime)
                    }
                }
            }
        },
        DOUBLEKEY("DoubleKey",0, 20,
                ResController.instance.image(Path.Imgs.Skills.DOUBLEKEY), 10,
                intArrayOf(0, 1, 2), 30) {
            override fun action(state: SkillState, User: Actor, Affected: MutableList<Actor>) {
                if (state == SkillState.START) {
                    AudioResourceController.getInstance().shot(Path.Sounds.Skills.DOUBLEKEY)
                    User.key += if(User.key > 0)User.key else -1 * User.key
                }
            }
        },
        LOSTKEY("LostKey",0, 20,
                ResController.instance.image(Path.Imgs.Skills.LOSTKEY), 10,
                intArrayOf(0, 1, 2), 30) {
            override fun action(state: SkillState, User: Actor, Affected: MutableList<Actor>) {
                if(state == SkillState.START){
                    AudioResourceController.getInstance().shot(Path.Sounds.Skills.LOSTKEY)
                }
                Affected.forEach {
                    if(isTeam && User.team != null && User.team!! == it.team!!){}else{
                        if(User != it){
                            if (state == SkillState.START) {
                                it.key -= 2
                                if(it.key < 0 && (!isTeam || it.ghost)){
                                    it.key = 0
                                }
                            }
                        }
                    }
                }
            }
        },
        RETARD("Retard",180, 20,
                ResController.instance.image(Path.Imgs.Skills.RETARD), 20,
                intArrayOf(0, 1, 2, 3, 4, 5), 80) {
            override fun action(state: SkillState, User: Actor, Affected: MutableList<Actor>) {
                if(state == SkillState.START){
                    AudioResourceController.getInstance().shot(Path.Sounds.Skills.RETARD)
                }
                Affected.forEach {
                    if(isTeam && User.team != null && User.team!! == it.team!!){}else{
                        if(User != it){
                            if (state == SkillState.START) {
                                it.changedSpeed -= 0.15
                                it.state = Actor.State.RETARD
                            }
                            if (state == SkillState.USED) {
                                it.changedSpeed += 0.15
                                it.state = Actor.State.NORMAL
                            }
                        }
                    }
                }
            }
        },
        SPRINT("Sprint",180, 20,
                ResController.instance.image(Path.Imgs.Skills.SPRINT), 10,
                intArrayOf(0, 1, 2), 60) {
            override fun action(state: SkillState, User: Actor, Affected: MutableList<Actor>) {
                User.run {
                    if (state == SkillState.START) {
                        changedSpeed += 0.15
                        AudioResourceController.getInstance().shot(Path.Sounds.Skills.SPRINT)
                        this.state = Actor.State.SPRINT
                    }
                    if (state == SkillState.USED) {
                        changedSpeed -= 0.15
                        this.state = Actor.State.NORMAL
                    }
                }

            }
        },
        SPEEDUP("SpeedUp",180, 20,
                ResController.instance.image(Path.Imgs.Skills.SPEEDUP), 20,
                intArrayOf(0, 1, 2, 3, 4, 5), 80) {
            override fun action(state: SkillState, User: Actor, Affected: MutableList<Actor>) {
                User.run {
                    if (state == SkillState.START) {
                        changedSpeed += 0.1
                        jumpHeight += 3
                        AudioResourceController.getInstance().shot(Path.Sounds.Skills.SPEEDUP)
                    }
                }
            }
        },
        SPEEDDOWN("SpeedDown",180, 20,
                ResController.instance.image(Path.Imgs.Skills.SPEEDDOWN), 20,
                intArrayOf(0, 1, 2, 3, 4), 80) {
            override fun action(state: SkillState, User: Actor, Affected: MutableList<Actor>) {
                User.run {
                    if (state == SkillState.START) {
                        if (changedSpeed > 0.3) {
                            changedSpeed -= 0.05
                            AudioResourceController.getInstance().shot(Path.Sounds.Skills.SPEEDDOWN)
                        }
                    }
                }
            }
        },
        SUN("Sun",180, 20,
                ResController.instance.image(Path.Imgs.Skills.SUN), 20,
                intArrayOf(0, 1, 2, 3), 80) {
            override fun action(state: SkillState, User: Actor, Affected: MutableList<Actor>) {
                User.run {
                    if (state == SkillState.START){
                        tmp = radius.toDouble()
                        AudioResourceController.getInstance().shot(Path.Sounds.Skills.SUN)
                    }
                    if (state == SkillState.ONUSED) {
                        if(radius < tmp + 300){
                            radius += 3
                        }
                    }
                    if (state == SkillState.USED) {
                        radius -= 300
                        if (radius != char.radius) {
                            radius = char.radius
                        }
                    }
                }
            }
        },
        NIGHT("Night",180, 20,
                ResController.instance.image(Path.Imgs.Skills.NIGHT), 20,
                intArrayOf(0, 1, 2, 3), 80) {
            override fun action(state: SkillState, User: Actor, Affected: MutableList<Actor>) {
                if(state == SkillState.START){
                    AudioResourceController.getInstance().shot(Path.Sounds.Skills.NIGHT)
                }
                Affected.forEach {
                    if(isTeam && User.team != null && User.team!! == it.team!!){}else{
                        if(User != it){
                            if (state == SkillState.START) {
                                tmp = it.radius.toDouble()
                            }
                            if (state == SkillState.ONUSED){
                                if(it.radius > tmp - 80){
                                    it.radius -= 3
                                }
                                if (it.radius <= 50) {
                                    it.radius = 50.0f
                                }
                            }
                            if (state == SkillState.USED) {
                                it.radius = it.char.radius
                            }
                        }
                    }
                }
            }
        },
        FREEZE("Freeze",180, 20,
                ResController.instance.image(Path.Imgs.Skills.FREEZE), 10,
                intArrayOf(0, 1, 2, 3, 2, 3, 2, 3, 2, 3, 2, 3, 2, 3, 2, 3, 2, 3), 175) {
            override fun action(state: SkillState, User: Actor, Affected: MutableList<Actor>) {
                User.run {
                    if(state == SkillState.START){
                        AudioResourceController.getInstance().shot(Path.Sounds.Skills.FREEZE)
                        this.state = Actor.State.FREEZE
                    }
                    if (state == SkillState.ONUSED) {
                        changedSpeed = 0.0
                        tmp = jumpHeight
                        jumpHeight = 0.0
                    }
                    if (state == SkillState.USED) {
                        changedSpeed = if(ghost)char.changedSpeed*1.5 else char.changedSpeed
                        jumpHeight = if(tmp < 20) 20.0 else tmp
                        this.state = Actor.State.NORMAL
                    }
                }

            }
        },
        TORNADO("Tornado",60, 20,
                ResController.instance.image(Path.Imgs.Skills.TORNADO), 3,
                intArrayOf(0, 1), 60) {
            override fun action(state: SkillState, User: Actor, Affected: MutableList<Actor>) {
                User.run {
                    if (state == SkillState.START) {
                        changedY -= 300
                        AudioResourceController.getInstance().shot(Path.Sounds.Skills.TORNADO)
                    }
                }
            }
        },
        TELEPORT("Teleport",60, 20,
                ResController.instance.image(Path.Imgs.Skills.TELEPORT), 7,
                intArrayOf(0, 1, 2, 3, 4, 5, 6, 7), 63) {
            override fun action(state: SkillState, User: Actor, Affected: MutableList<Actor>) {
                User.run {
                    if (state == SkillState.START) {
                        val r = Random.nextInt(0, birthPlaces.size / 2) * 2
                        setPosition(birthPlaces[r], birthPlaces[r + 1])
                        AudioResourceController.getInstance().shot(Path.Sounds.Skills.TELEPORT)
                    }
                }
            }
        },
        BLACKHOLE("BlackHole",120, 20,
                ResController.instance.image(Path.Imgs.Skills.BLACKHOLE), 10,
                intArrayOf(0, 1, 2, 3), 120) {
            override fun action(state: SkillState, User: Actor, Affected: MutableList<Actor>) {
                User.run {
                    if(state == SkillState.START){
                        AudioResourceController.getInstance().shot(Path.Sounds.Skills.BLACKHOLE)
                    }
                    Affected.forEach {
                        if(isTeam && User.team != null && User.team!! == it.team!!){}else{
                            if (this != it) {
                                if (state == SkillState.START) {
                                    point = Point(this.painter.centerX, this.painter.centerY)
                                    it.state = Actor.State.BLACKHOLE
                                }
                                if (state == SkillState.USED) {
                                    it.setPosition(point.x, point.y)
                                    it.state = Actor.State.NORMAL
                                }
                            }
                        }
                    }
                }
            }
        };

        var tmp = 0.0 // 用來暫存移動數值
        lateinit var point: Point // 用來暫存移動數值 位置資訊

        // 技能的刷新

        fun update(User: Actor, Affected: MutableList<Actor>) {
            User.run {
                if (skillState == SkillState.USEABLE) {
                    skill.action(skillState, User, Affected)
                }
                if (skillState == SkillState.START) {
                    skill.action(skillState, User, Affected)
                    skillAnimator.reset()
                    skillState = SkillState.ONUSED
                    skillKeeptime.play()
                }
                if (skillState == SkillState.ONUSED) {
                    skill.action(skillState, User, Affected)
                    if (skillKeeptime.count()) {
                        skillState = SkillState.USED
                        skillCdtime.play()
                        skill.action(skillState, User, Affected)
                    }
                }
                if (skillState == SkillState.USED) {
                    if (skillCdtime.count()) {
                        skillState = SkillState.USEABLE
                    }
                }
            }
        }

        fun update(item: Item, User: Actor, Affected: MutableList<Actor>) {
            item.run {
                if (state == SkillState.USEABLE) {
                    skill.action(state, User, Affected)
                }
                if (state == SkillState.START) {
                    skill.action(state, User, Affected)
                    state = SkillState.ONUSED
                    skillKeeptime.play()
                }
                if (state == SkillState.ONUSED) {
                    skill.action(state, User, Affected)
                    if (skillKeeptime.count()) {
                        state = SkillState.USED
                        skill.action(state, User, Affected)
                        skillCdtime.play()
                    }
                }
                if (state == SkillState.USED) {
                    if (skillCdtime.count()) {
                        state = SkillState.REMOVE
                    }
                }
            }
        }
    }
}