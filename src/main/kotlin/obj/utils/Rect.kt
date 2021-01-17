package obj.utils

class Rect(var left: Int, var top: Int, var right: Int, var bottom: Int) {
    /*
     constructor 是第二個建構子，並不會直接執行，有吻合條件時啟用
     : this 也就是呼叫Rect的建構子。
     跟init的差異就是會不會直接啟用
     */
    constructor(rect: Rect) : this(rect.left, rect.top, rect.right, rect.bottom)

    // kotlin 直接讓原本的方法，改成一個屬性，有個get()的方法，拿到資訊 稱為複合屬性
    val centerX: Int
        get() = (left + right) / 2

    val centerY: Int
        get() = (top + bottom) / 2

    val exactCenterX: Float
        get() = (left + right) / 2f

    val exactCenterY: Float
        get() = (top + bottom) / 2f

    val width: Int
        get() = right - left

    val height: Int
        get() = bottom - top

    /* 傳入參數的初始值 如果只有傳dx 或 dy 時 就會自動把另外一個沒有時，自動=0 (這個0可以自定義）
     外面的要抓變數名稱 dx: Int = 0 -> int dx = 0;
      像外面如果要改變GameObject時，就要寫x 因為GameObject裡面的translate是取名為(x: Int = 0)
      如果是要改變Rect時 就要寫dx 因為Rect裡面的translate是取名為(dx: Int=0)
    */
    fun translate(dx: Int = 0, dy: Int = 0) {
        left += dx
        right += dx
        top += dy
        bottom += dy
    }

    fun setCenter(x: Int, y: Int) {
        translate(x - centerX, y - centerY)
    }

    fun overlap(left: Int, top: Int, right: Int, bottom: Int): Boolean {
        if (this.left > right) {
            return false
        }
        if (this.right < left) {
            return false
        }
        if (this.top > bottom) {
            return false
        }
        return this.bottom >= top
    }

    fun overlap(b: Rect): Boolean {
        return overlap(b.left, b.top, b.right, b.bottom)
    }

    companion object {
        fun genWithCenter(x: Int, y: Int, width: Int, height: Int): Rect {
            val left = x - width / 2
            val right = left + width
            val top = y - height / 2
            val bottom = top + height
            return Rect(left, top, right, bottom)
        }
    }
}