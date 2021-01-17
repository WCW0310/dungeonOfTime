package camera

import obj.utils.Rect

object MapInformation{

    // 記得要在地圖那邊做地圖資訊的設定唷～

    private var mapInfo: Rect? = null

    fun mapInfo(): Rect? {
        return mapInfo
    }

    fun setMapInfo(left: Int, top: Int, right: Int, bottom: Int) {
        mapInfo = Rect(left, top, right, bottom)
    }

}