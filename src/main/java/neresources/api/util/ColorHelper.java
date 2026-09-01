package neresources.api.util;

/**
 * Colours for the distribution graph. Values are 0xAARRGGBB, and the accessors return the
 * components as floats so they can be handed straight to {@code GL11.glColor4f}.
 */
public final class ColorHelper {
    public static final int BLACK = 0xFF000000;
    public static final int WHITE = 0xFFFFFFFF;
    public static final int GRAY = 0xFF888888;
    public static final int LIGHT_GRAY = 0xFFCCCCCC;
    public static final int DARK_GRAY = 0xFF444444;
    public static final int RED = 0xFFFF0000;
    public static final int GREEN = 0xFF00FF00;
    public static final int BLUE = 0xFF0000FF;
    public static final int CYAN = 0xFF00FFFF;
    public static final int MAGENTA = 0xFFFF00FF;
    public static final int YELLOW = 0xFFFFFF00;
    public static final int ORANGE = 0xFFFF9900;
    public static final int BROWN = 0xFF8B5A2B;
    public static final int LIME = 0xFF88FF44;
    public static final int PINK = 0xFFFF88BB;
    public static final int TEAL = 0xFF008888;

    private ColorHelper() {
    }

    public static float getRed(int color) {
        return ((color >> 16) & 0xFF) / 255.0F;
    }

    public static float getGreen(int color) {
        return ((color >> 8) & 0xFF) / 255.0F;
    }

    public static float getBlue(int color) {
        return (color & 0xFF) / 255.0F;
    }

    public static float getAlpha(int color) {
        int alpha = (color >> 24) & 0xFF;
        return alpha == 0 ? 1.0F : alpha / 255.0F;
    }
}
