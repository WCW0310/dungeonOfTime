package obj.utils.Actor

import core.controllers.ResController
import core.utils.Delay
import obj.utils.SkillPool
import utils.Global
import utils.Global.UNIT_X
import utils.Global.UNIT_Y
import utils.Path
import java.awt.Graphics
import java.awt.Image

object CharacterPool {

    enum class Character(val charName: String, val changedSpeed: Double, val charSkill: SkillPool.Skill
                         , val radius: Float, val type: Int, val skillImg:Image) {
        WARRIOR1("Nero", 0.55, SkillPool.Skill.DOUBLESPEED,190.0f,0,
                ResController.instance.image(Path.Imgs.Skills.CdSkills.DOUBLESPEED)),
        WARRIOR2("Tifa", 0.45, SkillPool.Skill.BURST,200.0f, 1,
                ResController.instance.image(Path.Imgs.Skills.CdSkills.BURST)),
        WARRIOR3("Reno", 0.55, SkillPool.Skill.HAWKEYE, 210.0f,2,
                ResController.instance.image(Path.Imgs.Skills.CdSkills.HAWKEYE)),
        WARRIOR4("Vladimir", 0.65, SkillPool.Skill.DARK,190.0f, 3,
                ResController.instance.image(Path.Imgs.Skills.CdSkills.DARK)),
        WARRIOR5("Aerith", 0.6, SkillPool.Skill.TIMESTOP,180.0f, 4,
                ResController.instance.image(Path.Imgs.Skills.CdSkills.TIMESTOP) ),
        WARRIOR6("Karthus", 0.6, SkillPool.Skill.EARTHQUAKE,190.0f, 5,
                ResController.instance.image(Path.Imgs.Skills.CdSkills.EARTHQUAKE)),
        WARRIOR7("Kayle", 0.45, SkillPool.Skill.FLY,210.0f, 6,
                ResController.instance.image(Path.Imgs.Skills.CdSkills.FLY)),
        WARRIOR8("Sephiroth", 0.55, SkillPool.Skill.MAGICPOWER,190.0f, 7,
                ResController.instance.image(Path.Imgs.Skills.CdSkills.MAGICPOWER));

        val animator = CharAnimator(this.type, ResController.instance.image(Path.Imgs.Actors.Actor1))
        val ghostAnimator = CharAnimator(0, ResController.instance.image(Path.Imgs.Actors.GHOST))
    }

    class CharAnimator(charType:Int, charImg:Image) {

        enum class State(val speed: Int, val arr: IntArray) {
            NORMAL(0, intArrayOf((0))),
            WALK(30, intArrayOf(0, 1, 2, 1)),
            RUN(20, intArrayOf(0, 2))
        }

        var img = charImg
        private var state = State.RUN
        private var delay = Delay(state.speed)
        private var count = 0
        var type = charType

        init {
            delay.loop()
        }

        fun setSta(state: State) {
            this.state = state
            delay.countLimit = state.speed
            count %= state.arr.size
        }

        fun paint(dir: Global.Direction, left: Int, top: Int, right: Int, bottom: Int, g: Graphics) {
            g.drawImage(img, left, top, right, bottom,
                    (type % 4) * UNIT_X * 3 + UNIT_X * state.arr[count],
                    (type / 4) * UNIT_Y * 4 + UNIT_Y * dir.num,
                    (type % 4) * UNIT_X * 3 + UNIT_X + UNIT_X * state.arr[count],
                    (type / 4) * UNIT_Y * 4 + UNIT_Y + UNIT_Y * dir.num, null)
        }

        fun update() {
            if (delay.count()) {
                count = ++count % state.arr.size
            }
        }
    }


}