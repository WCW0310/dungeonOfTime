package scene


import core.GameKernel
import core.Scene
import core.controllers.ResController
import core.controllers.SceneController
import menu.impl.CreateRoomMenu
import menu.impl.GameGuideMenu
import menu.impl.InternetConnectMenu
import menu.impl.MouseTriggerImpl.mouseTrig
import menumodule.menu.*
import menumodule.menu.BackgroundType.BackgroundColor
import utils.Global
import utils.Path
import utils.Path.Sounds.MENUSCENEBGM
import java.awt.Color
import java.awt.Font
import java.awt.Graphics
import java.awt.event.KeyEvent


class MenuScene : Scene() {

    private lateinit var internetConnectMenu: InternetConnectMenu
    private lateinit var createRoomMenu: CreateRoomMenu
    private lateinit var gameGuidePop: GameGuideMenu
    private var buttons: MutableList<Button> = mutableListOf()
    private val menuImg = ResController.instance.image(Path.Imgs.Backgrounds.MENU)

    override fun sceneBegin() {
        AudioResourceController.getInstance().loop(MENUSCENEBGM, -1)
        initTheme()
        gameGuidePop = GameGuideMenu(0, 0, 1200, 600)
        gameGuidePop.setCancelable()
        internetConnectMenu = InternetConnectMenu(Global.WINDOW_WIDTH / 4, 0, Global.WINDOW_WIDTH /2, Global.WINDOW_HEIGHT)
        internetConnectMenu.setCancelable()
        createRoomMenu = CreateRoomMenu(Global.WINDOW_WIDTH / 4, 0, Global.WINDOW_WIDTH /2, Global.WINDOW_HEIGHT)
        createRoomMenu.setCancelable()
        buttons.add(Button(75, 410, Theme.get(0)))
        buttons[0].setClickedActionPerformed { x: Int, y: Int ->
            run{
                AudioResourceController.getInstance().play(Path.Sounds.BUTTON)
                SceneController.instance.change(ActorChooseScene())
            }}
        buttons.add(Button(300, 410, Theme.get(1)))
        buttons[1].setClickedActionPerformed { x: Int, y: Int ->
            run {
                AudioResourceController.getInstance().play(Path.Sounds.BUTTON)
                internetConnectMenu.sceneBegin()
                internetConnectMenu.show()
            }
        }
        buttons.add(Button(525, 410, Theme.get(2)))
        buttons[2].setClickedActionPerformed { x: Int, y: Int ->
            run {
                AudioResourceController.getInstance().play(Path.Sounds.BUTTON)
                createRoomMenu.sceneBegin()
                createRoomMenu.show()
            }
        }
        buttons.add(Button(750, 410, Theme.get(3)))
        buttons[3].setClickedActionPerformed { x: Int, y: Int ->
            run{
                AudioResourceController.getInstance().play(Path.Sounds.BUTTON)
                gameGuidePop.show()
            }}
        buttons.add(Button(975, 410, Theme.get(4)))
        buttons[4].setClickedActionPerformed { x: Int, y: Int ->
            run{
                AudioResourceController.getInstance().play(Path.Sounds.BUTTON)
                System.exit(0)
            }}
    }

    override fun sceneEnd() {
        ResController.instance.clear()
    }

    override fun update(timePassed: Long) {
        if (!internetConnectMenu.isShow) {
            buttons[1].unFocus()
        }
        if (!createRoomMenu.isShow) {
            buttons[2].unFocus()
        }
        if (!gameGuidePop.isShow) {
            buttons[3].unFocus()
        }
    }

    override fun paint(g: Graphics) {
        g.drawImage(menuImg, 0, 0,1200,600,null)
        buttons.forEach {
            it.paint(g)
        }
        if (internetConnectMenu.isShow) {
            internetConnectMenu.paint(g)
        }
        if (createRoomMenu.isShow) {
            createRoomMenu.paint(g)
        }
        if (gameGuidePop.isShow) {
            gameGuidePop.paint(g)
        }
    }

    override val input: ((GameKernel.Input.Event) -> Unit)?
        get() = {e ->
            when(e) {
                is GameKernel.Input.Event.MousePressed -> {
                    if(internetConnectMenu.isShow){
                        internetConnectMenu.mouseListener().invoke(e)
                    }else if(createRoomMenu.isShow){
                       createRoomMenu.mouseListener().invoke(e)
                    }else if (gameGuidePop.isShow) {
                        gameGuidePop.mouseListener().invoke(e)
                    }else{
                        buttons.forEach {
                            mouseTrig(it, e)
                        }
                    }
                }
                is GameKernel.Input.Event.MouseMoved -> {
                    if(internetConnectMenu.isShow){
                        internetConnectMenu.mouseListener().invoke(e)
                    }else if(createRoomMenu.isShow){
                        createRoomMenu.mouseListener().invoke(e)
                    }else if (gameGuidePop.isShow) {
                        gameGuidePop.mouseListener().invoke(e)
                    }else{
                        buttons.forEach {
                            mouseTrig(it, e)
                        }
                    }
                }
                is GameKernel.Input.Event.KeyPressed -> {
                    if(internetConnectMenu.isShow){
                        internetConnectMenu.editFrames.forEach {
                            it.keyTyped(e.data.keyCode)
                        }
                    }else if(createRoomMenu.isShow){
                        createRoomMenu.editFrames.forEach {
                            it.keyTyped(e.data.keyCode)
                        }
                    }else if(gameGuidePop.isShow){
                        gameGuidePop.input?.invoke(e)
                        if (e.data.keyCode == KeyEvent.VK_P ) {
                            if (gameGuidePop.isShow) {
                                gameGuidePop.hide()
                                gameGuidePop.sceneEnd()
                            } else {
                                gameGuidePop.sceneBegin()
                                gameGuidePop.show()
                            }
                        }
                    }
                }
                else -> {}
            }
        }

    private fun initTheme(){
        /*
        0~4 主選單按鈕
        5~9 網路選單按鈕暫停 6.name 7. port 8.ip
        10~11 遊戲結束時選擇按鈕
        12~15 option按鈕
         */
        var normal: Style = Style.StyleRect(180, 100, BackgroundType.BackgroundImage(ResController.instance.image(Path.Imgs.Backgrounds.START)))
                .setHaveBorder(true)
                .setBorderColor(Color(255, 215, 0))
                .setBorderThickness(5)
        var hover: Style = Style.StyleRect(200, 100, BackgroundType.BackgroundImage(ResController.instance.image(Path.Imgs.Backgrounds.START)))
                .setHaveBorder(true)
                .setBorderColor(Color(255, 255, 0))
                .setBorderThickness(5)
        var focus: Style = Style.StyleRect(200, 100, true, BackgroundColor(Color(184, 134, 11)))
                .setTextColor(Color.BLACK)
                .setHaveBorder(true)
                .setBorderColor(Color(230, 184, 0))
                .setBorderThickness(5)
                .setTextFont(Font("", Font.TYPE1_FONT, 28))
                .setText("Start")

        Theme.add(Theme(normal, hover, focus)) //為ArrayList<Theme>新增一實體 0

        normal = Style.StyleRect(180, 100, BackgroundType.BackgroundImage(ResController.instance.image(Path.Imgs.Backgrounds.STARTONLINE)))
                .setHaveBorder(true)
                .setBorderColor(Color(255, 215, 0))
                .setBorderThickness(5)
        hover = Style.StyleRect(200, 100, BackgroundType.BackgroundImage(ResController.instance.image(Path.Imgs.Backgrounds.STARTONLINE)))
                .setHaveBorder(true)
                .setBorderColor(Color(255, 255, 0))
                .setBorderThickness(5)
        focus = Style.StyleRect(200, 100, true, BackgroundColor(Color(184, 134, 11)))
                .setTextColor(Color.BLACK)
                .setHaveBorder(true)
                .setBorderColor(Color(230, 184, 0))
                .setBorderThickness(5)
                .setTextFont(Font("", Font.TYPE1_FONT, 28))
                .setText("STARTONLINE")

        Theme.add(Theme(normal, hover, focus))//1

        normal = Style.StyleRect(180, 100, BackgroundType.BackgroundImage(ResController.instance.image(Path.Imgs.Backgrounds.CREATEROOM)))
                .setHaveBorder(true)
                .setBorderColor(Color(255, 215, 0))
                .setBorderThickness(5)
        hover = Style.StyleRect(200, 100, BackgroundType.BackgroundImage(ResController.instance.image(Path.Imgs.Backgrounds.CREATEROOM)))
                .setHaveBorder(true)
                .setBorderColor(Color(255, 255, 0))
                .setBorderThickness(5)
        focus = Style.StyleRect(200, 100, true, BackgroundColor(Color(184, 134, 11)))
                .setTextColor(Color.BLACK)
                .setHaveBorder(true)
                .setBorderColor(Color(230, 184, 0))
                .setBorderThickness(5)
                .setTextFont(Font("", Font.TYPE1_FONT, 28))
                .setText("CREATEROOM")

        Theme.add(Theme(normal, hover, focus)) //2


        normal = Style.StyleRect(180, 100, BackgroundType.BackgroundImage(ResController.instance.image(Path.Imgs.Backgrounds.GUIDE)))
                .setHaveBorder(true)
                .setBorderColor(Color(255, 215, 0))
                .setBorderThickness(5)
        hover = Style.StyleRect(200, 100, BackgroundType.BackgroundImage(ResController.instance.image(Path.Imgs.Backgrounds.GUIDE)))
                .setHaveBorder(true)
                .setBorderColor(Color(255, 255, 0))
                .setBorderThickness(5)
        focus = Style.StyleRect(180, 100, BackgroundType.BackgroundImage(ResController.instance.image(Path.Imgs.Backgrounds.GUIDE)))
                .setHaveBorder(true)
                .setBorderColor(Color(255, 215, 0))
                .setBorderThickness(5)

        Theme.add(Theme(normal, hover, focus)) //3

        normal = Style.StyleRect(180, 100, BackgroundType.BackgroundImage(ResController.instance.image(Path.Imgs.Backgrounds.QUIT)))
                .setHaveBorder(true)
                .setBorderColor(Color(255, 215, 0))
                .setBorderThickness(5)
        hover = Style.StyleRect(200, 100, BackgroundType.BackgroundImage(ResController.instance.image(Path.Imgs.Backgrounds.QUIT)))
                .setHaveBorder(true)
                .setBorderColor(Color(255, 255, 0))
                .setBorderThickness(5)
        focus = Style.StyleRect(200, 100, true, BackgroundColor(Color(184, 134, 11)))
                .setTextColor(Color.BLACK)
                .setHaveBorder(true)
                .setBorderColor(Color(230, 184, 0))
                .setBorderThickness(5)
                .setTextFont(Font("", Font.TYPE1_FONT, 28))
                .setText("QUIT")

        Theme.add(Theme(normal, hover, focus)) //4

        // 網路連線的按鈕

        normal = Style.StyleRect(50, 50, BackgroundType.BackgroundImage(ResController.instance.image(Path.Imgs.Backgrounds.INTERNET_BACK)))
        hover = Style.StyleRect(45, 45, BackgroundType.BackgroundImage(ResController.instance.image(Path.Imgs.Backgrounds.INTERNET_BACK)))
        focus = Style.StyleRect(50, 50, BackgroundType.BackgroundImage(ResController.instance.image(Path.Imgs.Backgrounds.INTERNET_BACK)))

        Theme.add(Theme(normal, hover, focus)) //5 關閉按鈕

        normal = Style.StyleRect(550, 50, BackgroundColor(Color(0, 0, 0,0)))
                .setTextColor(Color.BLACK)
                .setTextFont(Font("TimesRoman", Font.TYPE1_FONT, 50))
        hover = Style.StyleRect(550, 50, BackgroundColor(Color(0, 0, 0,0)))
                .setTextColor(Color.BLACK)
                .setTextFont(Font("TimesRoman", Font.TYPE1_FONT, 50))
        focus = Style.StyleRect(550, 50, true, BackgroundColor(Color(0, 0, 0,0)))
                .setTextColor(Color.BLACK)
                .setTextFont(Font("TimesRoman", Font.TYPE1_FONT, 40))

        Theme.add(Theme(normal, hover, focus)) //6 輸入框 -> 玩家姓名

        normal = Style.StyleRect(550, 50, BackgroundColor(Color(0, 0, 0,0)))
                .setTextColor(Color.BLACK)
                .setTextFont(Font("TimesRoman", Font.TYPE1_FONT, 50))
        hover = Style.StyleRect(550, 50, BackgroundColor(Color(0, 0, 0,0)))
                .setTextColor(Color.BLACK)
                .setTextFont(Font("TimesRoman", Font.TYPE1_FONT, 50))
        focus = Style.StyleRect(550, 50, true, BackgroundColor(Color(0, 0, 0,0)))
                .setTextColor(Color.BLACK)
                .setTextFont(Font("TimesRoman", Font.TYPE1_FONT, 40))

        Theme.add(Theme(normal, hover, focus)) //7 輸入框 -> PORT位置

        normal = Style.StyleRect(550, 50, BackgroundColor(Color(0, 0, 0,0)))
                .setTextColor(Color.BLACK)
                .setTextFont(Font("TimesRoman", Font.TYPE1_FONT, 50))
        hover = Style.StyleRect(550, 50, BackgroundColor(Color(0, 0, 0,0)))
                .setTextColor(Color.BLACK)
                .setTextFont(Font("TimesRoman", Font.TYPE1_FONT, 50))
        focus = Style.StyleRect(550, 50, true, BackgroundColor(Color(0, 0, 0,0)))
                .setTextColor(Color.BLACK)
                .setTextFont(Font("TimesRoman", Font.TYPE1_FONT, 40))

        Theme.add(Theme(normal, hover, focus)) //8 輸入框 -> IP位置

        normal = Style.StyleRect(275, 75, BackgroundType.BackgroundImage(ResController.instance.image(Path.Imgs.Backgrounds.CREATE)))
        hover = Style.StyleRect(300, 75, BackgroundType.BackgroundImage(ResController.instance.image(Path.Imgs.Backgrounds.CREATE)))
        focus = Style.StyleRect(275, 75, BackgroundType.BackgroundImage(ResController.instance.image(Path.Imgs.Backgrounds.CREATE)))

        Theme.add(Theme(normal, hover, focus)) //9 確認 (Create)

        normal = Style.StyleRect(275, 75, BackgroundType.BackgroundImage(ResController.instance.image(Path.Imgs.Backgrounds.CONNECT)))
        hover = Style.StyleRect(300, 75, BackgroundType.BackgroundImage(ResController.instance.image(Path.Imgs.Backgrounds.CONNECT)))
        focus = Style.StyleRect(275, 75, BackgroundType.BackgroundImage(ResController.instance.image(Path.Imgs.Backgrounds.CONNECT)))

        Theme.add(Theme(normal, hover, focus)) //10 連接 (Connect)

        // 遊戲結束畫面的按鈕

        normal = Style.StyleRect(200, 110, BackgroundType.BackgroundImage(ResController.instance.image(Path.Imgs.Backgrounds.GAMEOVER_PLAYAGAIN)))
        hover = Style.StyleRect(210, 120, BackgroundType.BackgroundImage(ResController.instance.image(Path.Imgs.Backgrounds.GAMEOVER_PLAYAGAIN)))
        focus = Style.StyleRect(210, 120, BackgroundType.BackgroundImage(ResController.instance.image(Path.Imgs.Backgrounds.GAMEOVER_PLAYAGAIN)))

        Theme.add(Theme(normal, hover, focus)) //11 重玩的按鈕

        normal = Style.StyleRect(200, 110, BackgroundType.BackgroundImage(ResController.instance.image(Path.Imgs.Backgrounds.GAMEOVER_BACKMENU)))
        hover = Style.StyleRect(210, 120, BackgroundType.BackgroundImage(ResController.instance.image(Path.Imgs.Backgrounds.GAMEOVER_BACKMENU)))
        focus = Style.StyleRect(210, 120, BackgroundType.BackgroundImage(ResController.instance.image(Path.Imgs.Backgrounds.GAMEOVER_BACKMENU)))

        Theme.add(Theme(normal, hover, focus)) //12 返回主菜單的按鈕

        // option 裡面的按鈕

        normal = Style.StyleRect(50, 50, BackgroundType.BackgroundImage(ResController.instance.image(Path.Imgs.Backgrounds.OPTION_CANCEL)))
        hover = Style.StyleRect(45, 45, BackgroundType.BackgroundImage(ResController.instance.image(Path.Imgs.Backgrounds.OPTION_CANCEL)))
                .setHaveBorder(true)
                .setBorderColor(Color(255, 215, 0))
                .setBorderThickness(5)
        focus = Style.StyleRect(45, 45, BackgroundType.BackgroundImage(ResController.instance.image(Path.Imgs.Backgrounds.OPTION_CANCEL)))

        Theme.add(Theme(normal, hover, focus)) //13 關閉

        normal = Style.StyleRect(100, 100, BackgroundType.BackgroundImage(ResController.instance.image(Path.Imgs.Backgrounds.OPTION_BACK)))
        hover = Style.StyleRect(95, 95, BackgroundType.BackgroundImage(ResController.instance.image(Path.Imgs.Backgrounds.OPTION_BACK)))
                .setHaveBorder(true)
                .setBorderColor(Color(255, 215, 0))
                .setBorderThickness(5)
        focus = Style.StyleRect(95, 95, BackgroundType.BackgroundImage(ResController.instance.image(Path.Imgs.Backgrounds.OPTION_BACK)))

        Theme.add(Theme(normal, hover, focus)) //14 重選角階段開始

        normal = Style.StyleRect(100, 100, BackgroundType.BackgroundImage(ResController.instance.image(Path.Imgs.Backgrounds.OPTION_RESET)))
        hover = Style.StyleRect(95, 95, BackgroundType.BackgroundImage(ResController.instance.image(Path.Imgs.Backgrounds.OPTION_RESET)))
                .setHaveBorder(true)
                .setBorderColor(Color(255, 215, 0))
                .setBorderThickness(5)
        focus = Style.StyleRect(95, 95, BackgroundType.BackgroundImage(ResController.instance.image(Path.Imgs.Backgrounds.OPTION_RESET)))

        Theme.add(Theme(normal, hover, focus)) //15 重新開始

        normal = Style.StyleRect(100, 100, BackgroundType.BackgroundImage(ResController.instance.image(Path.Imgs.Backgrounds.OPTION_EXIT)))
        hover = Style.StyleRect(95, 95, BackgroundType.BackgroundImage(ResController.instance.image(Path.Imgs.Backgrounds.OPTION_EXIT)))
                .setHaveBorder(true)
                .setBorderColor(Color(255, 215, 0))
                .setBorderThickness(5)
        focus = Style.StyleRect(95, 95, BackgroundType.BackgroundImage(ResController.instance.image(Path.Imgs.Backgrounds.OPTION_EXIT)))

        Theme.add(Theme(normal, hover, focus)) //16 返回主選單

        normal = Style.StyleRect(150, 145, BackgroundType.BackgroundImage(ResController.instance.image(Path.Imgs.Backgrounds.INTERNETSTART)))
        hover = Style.StyleRect(150, 155, BackgroundType.BackgroundImage(ResController.instance.image(Path.Imgs.Backgrounds.INTERNETSTART)))
        focus = Style.StyleRect(150, 155, BackgroundType.BackgroundImage(ResController.instance.image(Path.Imgs.Backgrounds.INTERNETSTART)))

        Theme.add(Theme(normal, hover, focus)) //17 網路遊戲開始


    }

}
