package com.pytcher.pytcher;


public class PitchConverter {

    public static final int CONTINUOUS = 0;
    public static final int CHROMATIC = 1;
    public static final int PENTATONIC = 2;
    public static final int MAJOR = 3;
    private String[] scaleNames = {"Continuous", "Chromatic", "Pentatonic", "Major"};
    private int scale = CONTINUOUS;

    private int[] pentatonicScale = {0, 2, 4, 7, 9, 12, 14, 16};
    private int[] majorScale = {0, 2, 4, 5, 7, 9, 11, 12};

    public void toggleScale() {
        scale = (scale + 1) % scaleNames.length;
    }

    public String getScaleName() {
        return scaleNames[scale];
    }

    public double getFrequency(double angle) {
        double freq;
        switch (scale) {
            case CONTINUOUS:
                freq = (Math.abs(angle)) * 1.45 + 261;
                break;
            case CHROMATIC:
                freq = (Math.abs(angle)) * 1.45 + 261;
                int halfSteps = (int) Math.round(Math.log(freq / 440) / Math.log(MainActivity.FREQ_LOG_BASE));
                halfSteps %= 12;
                freq = 440 * Math.pow(MainActivity.FREQ_LOG_BASE, halfSteps);
                break;
            case PENTATONIC:
                int noteNum = (int) (Math.abs(angle) * pentatonicScale.length / 180);
                // 261.6 is C major, starts it off there
                freq = 261.6 * Math.pow(MainActivity.FREQ_LOG_BASE,
                        pentatonicScale[noteNum % pentatonicScale.length]);
                break;
            case MAJOR:
                noteNum = (int) (Math.abs(angle) * majorScale.length / 180);
                freq = 261.6 * Math.pow(MainActivity.FREQ_LOG_BASE,
                        majorScale[noteNum % majorScale.length]);
                break;

            default:
                freq = (Math.abs(angle)) * 1.44 + 261;

        }
        return freq;
    }

}
