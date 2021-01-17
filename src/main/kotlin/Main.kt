import core.GameInterfaceImpl
import core.GameKernel
import scene.MenuScene
import utils.Global
import utils.Global.WINDOW_HEIGHT
import utils.Global.WINDOW_WIDTH
import javax.swing.JFrame

fun main() {
    val gk = GameKernel(Global.UPDATE_FREQ, Global.PAINT_FREQ, GameInterfaceImpl(MenuScene()))
    /*
     = GameKernel gk = new GameKernel(60,60,new GameInterfaceImpl(new MainScene()));
     GameInterfaceImpl(MainScene()) ：這裡老師修正成直接幫你new 一個GI
     */

    JFrame().apply { // this = JFrame // java= JFrame jf = new JFrame();
        title = "Game Test 7th"  // = jf.setTitle("Game Test 7th");
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT)  // = jf.setSize(800, 600);
        defaultCloseOperation = JFrame.EXIT_ON_CLOSE  // = jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null)//視窗置中
        isResizable = false //能否調整視窗大小 = jf.setResizable(false);
        add(gk)    // = jf.add(gk);
        isVisible = true    // = jf.setVisible(true);
        gk.play(Global.IS_DEBUG)   // = gk.play(true);
    }
    /*
    kotlin的四種源生方法，從一開始就存在所有物件，也就是任一個物件都可以. 然後使用：
    run this = 你操作的當前物件 可省略this
    let it = 才是你操作的當前物件（不可省略） ， 如果在方法內需要動到外面的類別時，就會使用（大量使用時）
    :大括號跑完以後 回傳大括號最後一個方法回傳的值。
    EX: 最後方法是print 回傳值是Unit 那前面的data 就會=void 型態
    apply this = 你操作的當前物件 可省略this
    also  it = 才是你操作的當前物件（不可省略） ， 如果在方法內需要動到外面的類別時，就會使用
    ： 大括號跑完以後 回傳操作的物件本身。
    也因此在上述程式碼中，除了gk以外都省略了jf 因為 每個都=  this.方法，然後省略this
     */
}