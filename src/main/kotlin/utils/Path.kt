package utils

/*
object = static ：程式執行時，會直接創建出這個類別的物件實體，所以一開始就會全部創建好
object 內部如果用object 還是一樣可以，就=會先創建Path 實體然後再創建Imgs實體，再創建Actors實體
也就是像是全部都是static final的概念，因此呼叫方法就跟java的Path-全靜態版本一樣
全部都用. 的就好。
$符號 是用來取代字串連結用的， 如果$符號作為開頭+變數，會自動轉成String
，然後他又會自動判別，所以超出的部分(要增加的地方)直接輸入字串，就會連成一個字串了
 */
object Path {
    override fun toString(): String = ""

    object Imgs {
        override fun toString(): String = "$Path/imgs"

        object Actors {
            override fun toString(): String = "$Imgs/actors"

            val Actor1 = "$this/Actor.png"

            val GHOST = "$this/ghost.png"

        }

        object Objs {
            override fun toString(): String = "$Imgs/objs"

            val KEY_RED = "$this/key_red.png"
            val KEY_BLUE = "$this/key_blue.png"
            val KEY_PURPLE = "$this/key_purple.png"
            val KEY_GREEN = "$this/key_green.png"
            val KEY_YELLOW = "$this/key_yellow.png"
            val TREASURE_BLUE = "$this/treasure_blue.png"
            val TREASURE_RED = "$this/treasure_red.png"
            val GEM_BLUE = "$this/gem_blue.png"
            val GEM_GREEN = "$this/gem_green.png"
            val GEM_ORANGE = "$this/gem_orange.png"
            val GEM_PURPLE = "$this/gem_purple.png"
            val GEM_RED = "$this/gem_red.png"
            val GEM_YELLOW = "$this/gem_yellow.png"
            val START_RED = "$this/checkpoint_red.png"
            val START_BLUE = "$this/checkpoint_blue.png"
            val CEILING = "$this/rockTop.png"
            val FLOOR = "$this/rockBottom.png"
            val CONNER = "$this/rockLR.png"
            val PLAT1 = "$this/Plat1.png"
            val PLAT2 = "$this/Plat2.png"
            val PLAT3 = "$this/Plat3.png"
            val PLAT4 = "$this/Plat4.png"
            val MISSION1 = "$this/mission1.png"
            val MISSION2 = "$this/mission2.png"
            val SCROLLTEN = "$this/scrollTen.png"
            val SCROLLTIME = "$this/scrollTime.png"
            val SCROLLBATTLE = "$this/scrollBattle.png"
            val SCROLLGHOST = "$this/scrollGhost.png"
            val BULLETLEFT = "$this/bulletLeft.png"
            val BULLETRIGHT = "$this/bulletRight.png"
            val ROCKPLAT1 = "$this/rockPlat1.png"
            val ROCKPLAT2 = "$this/rockPlat2.png"
            val ROCKPLAT3 = "$this/rockPlat3.png"
            val ROCKPLAT4 = "$this/rockPlat4.png"
            val ROCKPLAT5 = "$this/rockPlat5.png"
            val ROCKPLAT6 = "$this/rockPlat6.png"
            val ROCKTOP = "$this/rockTop.png"
            val ROCKBOTTOM = "$this/rockBottom.png"
            val ROCKLR = "$this/rockLR.png"
            val WOODPLAT1 = "$this/woodPlat1.png"
            val WOODPLAT2 = "$this/woodPlat2.png"
            val WOODPLAT3 = "$this/woodPlat3.png"
            val WOODPLAT4 = "$this/woodPlat4.png"
            val WOODPLAT5 = "$this/woodPlat5.png"
            val WOODPLAT6 = "$this/woodPlat6.png"
            val WOODTOP = "$this/woodTop.png"
            val WOODBOTTOM = "$this/woodBottom.png"
            val WOODLR = "$this/woodLR.png"
            
        }

        object  Backgrounds{
            override fun toString() = "$Imgs/backgrounds"

            val ACTORCHOOSE = "$this/actorChoose.png"
            val ACTORCHOOSE_INTERNET = "$this/InternetActorChoose.png"
            val GAMEINTRO1 = "$this/gameGuide1.png"
            val GAMEINTRO2 = "$this/gameGuide2.png"
            val GAMEINTRO3 = "$this/gameGuide3.png"
            val GAMEINTRO4 = "$this/gameGuide4.png"
            val MAINSCENE1200 = "$this/mainScene1200.png"
            val MAINSCENE600 = "$this/mainScene600.png"
            val GAMEOVERSCENE = "$this/gameOverScene.png"
            val MENU = "$this/menu.png"
            val START = "$this/start.png"
            val STARTONLINE = "$this/startOnline.png"
            val CREATEROOM = "$this/createRoom.png"
            val GUIDE = "$this/guide.png"
            val QUIT = "$this/quit.png"
            val INTERNETCONNECTMENU = "$this/InternetConnectMenu.png"
            val INTERNET_BACK = "$this/Internet_back.png"
            val CONNECT = "$this/Connect.png"
            val CREATE = "$this/Create.png"
            val CREATEROOMMENU = "$this/InternetCreateMenu.png"
            val GAMEOVER_PLAYAGAIN = "$this/PlayAgain.png"
            val GAMEOVER_BACKMENU = "$this/BackMenu.png"
            val OPTION_BACKGROUND = "$this/OptionBackground.png"
            val OPTION_CANCEL = "$this/OptionCancel.png"
            val OPTION_BACK = "$this/OptionBack.png"
            val OPTION_RESET = "$this/OptionReset.png"
            val OPTION_EXIT = "$this/OptionExit.png"
            val INTERNETSTART = "$this/internetStart.png"
            val INTERNETWAITINGROOM = "$this/internetWaitingRoom.png"

        }

        object Skills{
            override fun toString() = "$Imgs/skills"

            val DOUBLESPEED = "$this/DoubleSpeed.png"
            val BURST = "$this/Burst.png"
            val HAWKEYE = "$this/Hawkeye.png"
            val DARK = "$this/Dark.png"
            val TIMESTOP = "$this/Timestop.png"
            val EARTHQUAKE = "$this/Earthquake.png"
            val FLY = "$this/Fly.png"
            val MAGICPOWER = "$this/MagicPower.png"
            val DOUBLEKEY = "$this/DoubleKey.png"
            val LOSTKEY = "$this/LostKey.png"
            val RETARD = "$this/Retard.png"
            val SPRINT = "$this/Sprint.png"
            val SPEEDUP = "$this/SpeedUp.png"
            val SPEEDDOWN = "$this/SpeedDown.png"
            val SUN = "$this/Sun.png"
            val NIGHT = "$this/Night.png"
            val FREEZE = "$this/Freeze.png"
            val TORNADO = "$this/Tornado.png"
            val TELEPORT = "$this/Teleport.png"
            val BLACKHOLE = "$this/Blackhole.png"

            object CdSkills{
                override fun toString() = "$Skills/cdSkills"

                val DOUBLESPEED = "$this/DoubleSpeed.png"
                val BURST = "$this/Burst.png"
                val HAWKEYE = "$this/Hawkeye.png"
                val DARK = "$this/Dark.png"
                val TIMESTOP = "$this/Timestop.png"
                val EARTHQUAKE = "$this/Earthquake.png"
                val FLY = "$this/Fly.png"
                val MAGICPOWER = "$this/MagicPower.png"
            }
        }

    }

    object Sounds{
        override fun toString() = "$Path/sounds"

        val MAINSCENEBGM = "$this/MainSceneBGM.wav"
        val MENUSCENEBGM = "$this/MenuSceneBGM.wav"

        val BUTTON = "$this/button.wav"

        val JUMP = "$this/jump.wav"

        val PICKKEY = "$this/pickkey.wav"

        object Skills{
            override fun toString() = "$Sounds/skills"

            val BLACKHOLE = "$this/Blackhole.wav"
            val BRUST = "$this/Brust.wav"
            val DARK = "$this/Dark.wav"
            val DOUBLEKEY = "$this/DoubleKey.wav"
            val DOUBLESPEED = "$this/DoubleSpeed.wav"
            val EARTHQUAKE = "$this/Earthquake.wav"
            val FLY = "$this/Fly.wav"
            val FREEZE = "$this/Freeze.wav"
            val HAWKEYE = "$this/HawkEye.wav"
            val LOSTKEY = "$this/LostKey.wav"
            val MAGICPOWER = "$this/MagicPower.wav"
            val NIGHT = "$this/Night.wav"
            val RETARD = "$this/Retard.wav"
            val SPEEDDOWN = "$this/SpeedDown.wav"
            val SPEEDUP = "$this/SpeedUp.wav"
            val SPRINT = "$this/Sprint.wav"
            val SUN = "$this/Sun.wav"
            val TELEPORT = "$this/Teleport.wav"
            val TIMESTOP = "$this/Timestop.wav"
            val TORNADO = "$this/Tornado.wav"

        }


    }

    object Maps{
        override fun toString(): String = "$Path/maps"

        val MAINMAP1_BMP = "$this/mainMap.bmp"
        val MAINMAP1_TXT = "$this/mainMap.txt"
        val WAITMAP_BMP = "$this/waitMap.bmp"
        val WAITMAP_TXT = "$this/waitMap.txt"
        val MAINMAP2_BMP = "$this/genMap.bmp"
        val MAINMAP2_TXT = "$this/genMap.txt"

    }
}