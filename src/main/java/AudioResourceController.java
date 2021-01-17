import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public class AudioResourceController {

    // 單例
    private static class ClipThread extends Thread {

        public interface FinishHandler {

            public void whenFinish(String fileName, Clip clip);
        }

        private final String fileName;
        private final int count;
        private Clip clip;
        private int framePos;
        private final FinishHandler finishHandler;

        public ClipThread(String fileName, int count, FinishHandler finishHandler) {
            this.fileName = fileName;
            this.count = count;
            this.finishHandler = finishHandler;
            this.framePos = -1;
        }

        @Override
        public void run() {
            AudioInputStream audioInputStream;
            try {
                audioInputStream = AudioSystem.getAudioInputStream(new File(this.getClass().getResource(fileName).toURI()));
                clip = AudioSystem.getClip();
                clip.open(audioInputStream);
                clip.setFramePosition(0);
                // values have min/max values, for now don't check for outOfBounds values
                FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                gainControl.setValue(5f);
                playSound();
                clip.addLineListener((LineEvent event) -> {
                    if (event.getType() == LineEvent.Type.STOP) {
                        finishHandler.whenFinish(fileName, clip);
                    }
                });
            } catch (UnsupportedAudioFileException | IOException | LineUnavailableException | URISyntaxException ex) {
                Logger.getLogger(AudioResourceController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        public void playSound() {
            if (framePos != -1) {
                clip.setFramePosition(framePos);
                framePos = -1;
            }
            if (count == 1) {
                clip.start();
            } else {
                clip.loop(count);
            }
        }

        public void stopSound() {
            if (clip != null && clip.isRunning()) {
                clip.stop();
                if (isAlive() || !isInterrupted()) {
                    interrupt();
                }
            }
        }

        public boolean isDead() {
            return clip == null;
        }
    }

    private static AudioResourceController irc;

    private Map<String, ClipThread> soundMap;
    private final ClipThread.FinishHandler finishHandler = (String fileName, Clip clip) -> {
        if (soundMap.containsKey(fileName)) {
            if (soundMap.get(fileName).framePos == -1) {
                soundMap.remove(fileName);
                clip.close();
            }
        }else{
            clip.close();
        }
    };

    private AudioResourceController() {
        soundMap = new HashMap<>();
    }

    public static AudioResourceController getInstance() {
        if (irc == null) {
            irc = new AudioResourceController();
        }
        return irc;
    }

    public void play(String fileName) {
        if (soundMap.containsKey(fileName)) {
            ClipThread ct = soundMap.get(fileName);
            if (!ct.isDead()) {
                ct.playSound();
                return;
            }
        }
        ClipThread ct = new ClipThread(fileName, 1, finishHandler);
        soundMap.put(fileName, ct);
        ct.start();
    }

    public void shot(String fileName) {
        new ClipThread(fileName, 1, finishHandler).start();
    }

    public void loop(String fileName, int count) {
        ClipThread ct = new ClipThread(fileName, count, finishHandler);
        soundMap.put(fileName, ct);
        ct.start();
    }

    public void pause(String fileName) {
        if (soundMap.containsKey(fileName)) {
            ClipThread ct = soundMap.get(fileName);
            ct.framePos = ct.clip.getFramePosition();
            ct.clip.stop();
        }
    }

    // 同樣音效連續撥放時只能停止最後一次
    public void stop(String fileName) {
        if (!soundMap.containsKey(fileName)) {
            return;
        }
        ClipThread ct = soundMap.get(fileName);
        ct.stopSound();
        soundMap.remove(fileName);
    }
}
