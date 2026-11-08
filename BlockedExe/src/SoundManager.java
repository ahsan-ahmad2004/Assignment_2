import javax.sound.sampled.*;
import java.io.*;
import java.util.EnumMap;
import java.util.Map;

/**
 * Plays WAV effects through javax.sound.sampled.Clip.
 * Looks for assets/sounds/<name>.wav; if missing, synthesises the tone, saves it there as a WAV
 * and loads it. If audio hardware is unavailable the game simply runs silent.
 */
public class SoundManager {
    public enum Sfx { SELECT, PLACE, CLEAR, INVALID, GAME_OVER, WIN }

    private static final float RATE = 44100f;
    private final Map<Sfx, Clip> clips = new EnumMap<>(Sfx.class);
    private boolean muted;

    public SoundManager() {
        for (Sfx s : Sfx.values()) clips.put(s, load(s));
    }

    public void play(Sfx s) {
        if (muted) return;
        Clip c = clips.get(s);
        if (c == null) return;
        try {
            if (c.isRunning()) c.stop();
            c.setFramePosition(0);
            c.start();
        } catch (Exception e) {
            AppLog.error("Sound playback failed: " + s, e);
        }
    }

    public void toggleMute() { muted = !muted; }
    public boolean isMuted() { return muted; }

    private Clip load(Sfx sfx) {
        String name = sfx.name().toLowerCase();
        File f = new File("assets/sounds/" + name + ".wav");
        byte[] pcm = null;
        if (!f.exists()) {
            pcm = toBytes(synth(sfx));
            try {
                File dir = f.getParentFile();
                if (dir != null) dir.mkdirs();
                try (AudioInputStream ais = stream(pcm)) {
                    AudioSystem.write(ais, AudioFileFormat.Type.WAVE, f);
                }
            } catch (Exception e) {
                AppLog.error("Could not write " + f, e);
            }
        }
        try (AudioInputStream in = f.exists() ? AudioSystem.getAudioInputStream(f)
                                              : stream(pcm != null ? pcm : toBytes(synth(sfx)))) {
            Clip clip = AudioSystem.getClip();
            clip.open(in);
            return clip;
        } catch (Exception | Error e) {
            AppLog.error("Audio unavailable for " + name, e instanceof Exception ? (Exception) e : new Exception(e));
            return null;
        }
    }

    private static AudioInputStream stream(byte[] pcm) {
        AudioFormat fmt = new AudioFormat(RATE, 16, 1, true, false);
        return new AudioInputStream(new ByteArrayInputStream(pcm), fmt, pcm.length / 2);
    }

    private static byte[] toBytes(short[] samples) {
        byte[] b = new byte[samples.length * 2];
        for (int i = 0; i < samples.length; i++) {
            b[2 * i] = (byte) (samples[i] & 0xFF);
            b[2 * i + 1] = (byte) ((samples[i] >> 8) & 0xFF);
        }
        return b;
    }

    // ---- tiny synthesiser ----
    private static short[] tone(double f0, double f1, int ms, double vol, boolean square) {
        int n = (int) (RATE * ms / 1000);
        short[] out = new short[n];
        double phase = 0;
        for (int i = 0; i < n; i++) {
            double t = i / (double) n;
            phase += 2 * Math.PI * (f0 + (f1 - f0) * t) / RATE;
            double env = Math.min(1, i / 150.0) * Math.pow(1 - t, 1.4);
            double s = square ? Math.signum(Math.sin(phase)) * 0.55 : Math.sin(phase) + 0.3 * Math.sin(2 * phase);
            out[i] = (short) (s * env * vol * Short.MAX_VALUE * 0.55);
        }
        return out;
    }

    private static short[] concat(short[]... parts) {
        int len = 0;
        for (short[] p : parts) len += p.length;
        short[] out = new short[len];
        int pos = 0;
        for (short[] p : parts) { System.arraycopy(p, 0, out, pos, p.length); pos += p.length; }
        return out;
    }

    private static short[] synth(Sfx s) {
        switch (s) {
            case SELECT:   return tone(660, 900, 90, 0.8, false);
            case PLACE:    return concat(tone(260, 110, 110, 1.0, false), tone(520, 520, 50, 0.4, false));
            case CLEAR:    return concat(tone(523, 523, 80, 0.9, false), tone(659, 659, 80, 0.9, false),
                                         tone(784, 784, 80, 0.9, false), tone(1047, 1047, 220, 1.0, false));
            case INVALID:  return tone(170, 110, 230, 0.8, true);
            case GAME_OVER:return concat(tone(392, 392, 180, 0.9, false), tone(330, 330, 180, 0.9, false),
                                         tone(262, 262, 180, 0.9, false), tone(196, 180, 420, 1.0, false));
            default:       return concat(tone(523, 523, 110, 0.9, false), tone(659, 659, 110, 0.9, false),
                                         tone(784, 784, 110, 0.9, false), tone(1047, 1047, 120, 0.9, false),
                                         tone(784, 784, 80, 0.9, false), tone(1047, 1047, 380, 1.0, false));
        }
    }
}
