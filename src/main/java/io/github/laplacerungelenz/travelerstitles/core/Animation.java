package io.github.laplacerungelenz.travelerstitles.core;

public final class Animation {

    private Animation() {}

    public static float alpha(float age, int fadeIn, int hold, int fadeOut) {
        int in = Math.max(0, fadeIn), stay = Math.max(0, hold), out = Math.max(0, fadeOut);
        if (age < 0 || age >= in + stay + out) return 0;
        if (in > 0 && age < in) return age / in;
        if (age < in + stay) return 1;
        return out == 0 ? 0 : Math.max(0, (in + stay + out - age) / out);
    }
}
