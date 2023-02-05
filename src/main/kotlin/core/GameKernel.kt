package core

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.awt.Canvas
import java.awt.Graphics
import java.awt.event.*

/*
GameKernel() 括號內就是主要建構子 ，如果有繼承就是加上 : 父類別名稱 EX: : Canvas() {
    如果建構子屬性直接設成 private 的話，就等於讓該類別擁有這個屬性，並直接接收這個輸入進來的值
    Java寫法
    private int updateFreq;
    public GameKernel(int updateFreq){
        this.updateFreq = updateFreq;
    }   這三行直接 = private val updateFreq
    */
class GameKernel(private val updateFreq: Int, private val paintFreq: Int, private val gi: GameInterface) : Canvas() {

    private val nanoUnit = 1000000000L

    private val input = Input() // 自動判斷型態 nanoUnit = Long型態

    private var gameLoop: Job? = null  // 自動判斷型態 input = Input型態

    interface GameInterface {
        fun update(timePassed: Long)
        fun paint(g: Graphics)
        val input: ((Input.Event) -> Unit)?
    }
    /*  val 是Final屬性，不是static final 只是不會被改變而已
       val input: ((Input.Event) -> Unit)?
       這代表著是個抽象屬性input 它的set 或 get 方法還沒被定義
       Kotlin 的屬性，只要有getter 或 setter 其中一個方法，就可以當作一個屬性
       Java概念寫法:
           public class Input{
               public int getNumber(){
                   return 2;
               } // getter
               public void setNumber(int number){
               } // setter
           }
       也因此kotlin擁有抽象屬性，也就是說可以先創建屬性，然後方法給繼承的人改寫
       實現時，可以只需要實現其中一個setter 或 getter即可， 透過 override get() 或 set()即可
       ex: override val input: ((GameKernel.Input.Event) -> Unit)
               get() = SceneController.instance.input
        */
    /*
    ((Input.Event) -> Unit)?  後面的? 代表著這個東西可能會=null (因為有的可能不需要input)
    Unit 代表著不回傳資料 也就是 void
    kotlin 可以直接傳方法（Java只能透過介面）
    上面可解讀為 傳入Event 不回傳值得一個方法。
     */

    init {
        addKeyListener(input)
        addMouseListener(input)
        addMouseMotionListener(input)
        addMouseWheelListener(input)
    }
    /*
    init 是代表次要建構子， 順序： 主要建構子先做完，完成後會直接跳次要建構子做完。
    用於你的資料不會直接=輸入進來的值時使用。
     */


    private fun paint() {
        val bs = bufferStrategy
        bs ?: run {
            createBufferStrategy(3)
            return
        }
        val g = bs.drawGraphics
        g.fillRect(0, 0, width, height)
        gi.paint(g)
        g.dispose()
        bs.show()
    }

    fun play(debug: Boolean) {
        if (gameLoop != null) {
            return
        }
        gameLoop = GlobalScope.launch {
            val startTime = System.nanoTime()
            var passedUpdated = 0
            var lastRepaintTime = System.nanoTime()
            var paintTimes = 0
            var timer = System.nanoTime()

            while (isActive) {
                val currentTime = System.nanoTime()
                val totalTime = currentTime - startTime
                val targetTotalUpdated = totalTime / (nanoUnit / updateFreq)
                while (passedUpdated < targetTotalUpdated) {
                    input.consumeInput(gi.input)
                    gi.update(totalTime)
                    passedUpdated++
                }
                if (currentTime - timer >= nanoUnit) {
                    if (debug) {
                        println("FPS: $paintTimes")
                    }
                    paintTimes = 0
                    timer = currentTime
                }
                if ((nanoUnit / paintFreq) <= currentTime - lastRepaintTime) {
                    lastRepaintTime = currentTime
                    paint()
                    paintTimes++
                }
            }
        }
    }

    class Input : KeyListener, MouseListener, MouseMotionListener,
        MouseWheelListener {
        private val events = mutableListOf<Event>()

        override fun mouseReleased(e: MouseEvent) {
            synchronized(this) {
                if (!events.any { it is Event.MouseReleased }) {
                    events += Event.MouseReleased(e)
                }
            }
        }

        override fun mouseEntered(e: MouseEvent) {
            synchronized(this) {
                if (!events.any { it is Event.MouseEntered }) {
                    events += Event.MouseEntered(e)
                }
            }
        }

        override fun mouseClicked(e: MouseEvent) {
            synchronized(this) {
                if (!events.any { it is Event.MouseReleased }) {
                    events += Event.MouseReleased(e)
                }
            }
        }

        override fun mouseExited(e: MouseEvent) {
            synchronized(this) {
                if (!events.any { it is Event.MouseExited }) {
                    events += Event.MouseExited(e)
                }
            }
        }

        override fun mousePressed(e: MouseEvent) {
            synchronized(this) {
                if (!events.any { it is Event.MousePressed }) {
                    events += Event.MousePressed(e)
                }
            }
        }

        override fun mouseMoved(e: MouseEvent) {
            synchronized(this) {
                if (!events.any { it is Event.MouseMoved }) {
                    events += Event.MouseMoved(e)
                }
            }
        }

        override fun mouseDragged(e: MouseEvent) {
            synchronized(this) {
                if (!events.any { it is Event.MouseDragged }) {
                    events += Event.MouseDragged(e)
                }
            }
        }

        override fun mouseWheelMoved(e: MouseWheelEvent) {
            synchronized(this) {
                if (!events.any { it is Event.MouseWheelMoved }) {
                    events += Event.MouseWheelMoved(e)
                }
            }
        }

        override fun keyTyped(event: KeyEvent) {
            synchronized(this) {
                if (!events.any { it is Event.KeyTyped && it.data.keyCode == event.keyCode }) {
                    events += Event.KeyTyped(event)
                }
            }
        }

        override fun keyPressed(event: KeyEvent) {
            synchronized(this) {
                if (!events.any {
                        (it is Event.KeyPressed && it.data.keyCode == event.keyCode) ||
                                (it is Event.KeyKeepPressed && it.data.keyCode == event.keyCode)
                    }) {
                    events += Event.KeyPressed(event)
                    events += Event.KeyKeepPressed(event)
                }
            }
        }

        override fun keyReleased(event: KeyEvent) {
            synchronized(this) {
                if (!events.any { it is Event.KeyReleased && it.data.keyCode == event.keyCode }) {
                    events += Event.KeyReleased(event)
                    events.removeIf { it is Event.KeyKeepPressed && it.data.keyCode == event.keyCode }
                }
            }
        }

        fun consumeInput(consumer: ((Event) -> Unit)?) {
            synchronized(this) {
                consumer?.run {
                    events.forEach(consumer)
                }
                events.removeIf { it !is Event.KeyKeepPressed }
            }
        }

        sealed class Event {
            data class KeyTyped(val data: KeyEvent) : Event()
            data class KeyPressed(val data: KeyEvent) : Event()
            data class KeyKeepPressed(val data: KeyEvent) : Event()// solve delay-auto-shifted
            data class KeyReleased(val data: KeyEvent) : Event()
            data class MousePressed(val data: MouseEvent) : Event()
            data class MouseReleased(val data: MouseEvent) : Event()
            data class MouseEntered(val data: MouseEvent) : Event()
            data class MouseExited(val data: MouseEvent) : Event()
            data class MouseMoved(val data: MouseEvent) : Event()
            data class MouseDragged(val data: MouseEvent) : Event()
            data class MouseWheelMoved(val data: MouseWheelEvent) : Event()
        }
    }
    /*
    sealed class 是 enum的延伸(java沒) ：稱為封裝類，enum 是一個多型的概念(屬性相同)
    而sealed 則是讓這個屬性可以不相同，也就是他們可以傳入不同的屬性，多型的概念
    如果屬性都相同就用enum即可
    data class KeyTyped(val data: KeyEvent) : Event()
    data class MouseEntered(val data: MouseEvent) : Event()
    val data 可以是 KeyEvent 、 MouseEvent... 並且因為設 val -> 沒寫時= public
    data class；是一種類別的延伸，稱為數據類，因為在後續開發時，會很常只有數據而已，沒有行為，之後可以再好好看資料
     */
}