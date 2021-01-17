# MapLoader

### **Usage**

MapLoader : 整合讀取資料
ReadBmp : 讀取Bmp檔，獲取各個物件座標
ReadFile : 讀取txt檔，獲取物件類名與尺寸

- Operation Manual:

    1.將建構器產生之bmp、txt放進載入器資料夾中
    2.在場景開始時使用下面方法產生地圖資訊陣列

``` java
@Override
public void sceneBegin() {
        try {
            ArrayList<String[]> test = new MapLoader("testMAP.bmp", "Test.txt").combineInfo();
        } catch (IOException ex) {
            Logger.getLogger(MapScene.class.getName()).log(Level.SEVERE, null, ex);
        }

```

- CreatObject interface

  MapLoader提供接口，供使用者創造地圖物件，
  需先在在該物件底下，新增該類名之String name 屬性，傳入接口方法，
   便可獲取自定義座標與大小之該物件Array。

```java
    public interface CreatObject { 
    //可依照配對類名，創建對應的座標與尺寸之該地圖物件類別
        public GameObject[] FindOBjectXYs(GameObject name, String[] mapArr);
        
    }
```

### **Maintainers**
-    **呂健羽 郭漢均**
-    **戴逸玟 王敬淵**
-    **莊偉亨 楊理智**