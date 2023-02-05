package maploader;

import core.controllers.ResController;

import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class ReadBmp {

    public ArrayList<int[][]> readBmp(String path) {
        ArrayList<int[][]> rgbArr = new ArrayList<>();
        BufferedImage bi;

        bi = (BufferedImage) ResController.Companion.getInstance().image(path);

        int width = bi.getWidth();
        int height = bi.getHeight();
        int minx = bi.getMinX();
        int miny = bi.getMinY();
        for (int i = minx; i < width; i++) {
            for (int j = miny; j < height; j++) {
                int[][] rgbContent = new int[2][];
                int pixel = bi.getRGB(i, j);
                rgbContent[0] = new int[]{i, j};
                rgbContent[1] = new int[]{pixel};
                rgbArr.add(rgbContent);
            }
        }
        return rgbArr;
    }
}
