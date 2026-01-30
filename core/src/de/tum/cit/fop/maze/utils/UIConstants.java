package de.tum.cit.fop.maze.utils;

import com.badlogic.gdx.graphics.Color;

/**
 * UI Constants
 * 
 * Defines global unified UI dimensions, colors, and other constants to ensure
 * interface consistency.
 */
public final class UIConstants {

    private UIConstants() {
    } // Prevent instantiation

    // ==================== Button Dimensions ====================

    /** Large button width (main menu, primary actions) */
    public static final float BTN_WIDTH_LARGE = 300f;
    /** Large button height */
    public static final float BTN_HEIGHT_LARGE = 60f;

    /** Medium button width (dialogs, secondary actions) */
    public static final float BTN_WIDTH_MEDIUM = 200f;
    /** Medium button height */
    public static final float BTN_HEIGHT_MEDIUM = 50f;

    /** Small button width (toolbar, tabs) */
    public static final float BTN_WIDTH_SMALL = 120f;
    /** Small button height */
    public static final float BTN_HEIGHT_SMALL = 40f;

    // ==================== Viewport ====================

    /** Standard virtual width */
    public static final float VIEWPORT_WIDTH = 1920f;
    /** Standard virtual height */
    public static final float VIEWPORT_HEIGHT = 1080f;

    // ==================== Background Colors ====================

    /** Default dark background */
    public static final Color BG_COLOR_DEFAULT = new Color(0.08f, 0.08f, 0.1f, 1f);
    /** Menu background */
    public static final Color BG_COLOR_MENU = new Color(0.05f, 0.05f, 0.08f, 1f);
    /** Shop background */
    public static final Color BG_COLOR_SHOP = new Color(0.1f, 0.1f, 0.15f, 1f);
    /** Settings background */
    public static final Color BG_COLOR_SETTINGS = new Color(0.1f, 0.1f, 0.15f, 1f);
    /** Victory background (dark green) */
    public static final Color BG_COLOR_VICTORY = new Color(0f, 0.15f, 0.05f, 1f);
    /** Game Over background (dark red) */
    public static final Color BG_COLOR_GAMEOVER = new Color(0.15f, 0.02f, 0.02f, 1f);

    // ==================== Card/Panel Colors ====================

    /** Card background (semi-transparent dark gray) */
    public static final Color CARD_BG = new Color(0.15f, 0.15f, 0.2f, 0.9f);
    /** Card border color */
    public static final Color CARD_BORDER = new Color(0.3f, 0.3f, 0.4f, 1f);

    // ==================== Spacing ====================

    /** Standard padding */
    public static final float PAD_STANDARD = 20f;
    /** Small padding */
    public static final float PAD_SMALL = 10f;
    /** Large padding */
    public static final float PAD_LARGE = 40f;

    // ==================== Rarity Colors ====================

    /** Common rarity */
    public static final Color RARITY_COMMON = Color.WHITE;
    /** Rare */
    public static final Color RARITY_RARE = new Color(0.4f, 0.7f, 1.0f, 1f);
    /** Epic */
    public static final Color RARITY_EPIC = new Color(0.8f, 0.4f, 1.0f, 1f);
    /** Legendary */
    public static final Color RARITY_LEGENDARY = new Color(1.0f, 0.85f, 0.2f, 1f);

    // ==================== Help Screen Colors ====================

    /** Help screen tech-blue border */
    public static final Color HELP_BORDER_CYAN = new Color(0f, 0.83f, 1f, 1f); // #00D4FF

    /** Help screen nav item selected background */
    public static final Color HELP_NAV_SELECTED = new Color(0.1f, 0.3f, 0.5f, 0.9f);

    /** Help screen nav item default background */
    public static final Color HELP_NAV_DEFAULT = new Color(0.12f, 0.12f, 0.16f, 0.8f);

    /** Warning/Danger hint color */
    public static final Color HELP_WARN = new Color(1f, 0.42f, 0.21f, 1f); // #FF6B35

    /** Help screen content card background */
    public static final Color HELP_CARD_BG = new Color(0.1f, 0.1f, 0.14f, 0.95f);

    /** Help title color (Gold) */
    public static final Color HELP_TITLE_GOLD = new Color(1f, 0.85f, 0.2f, 1f);

    // ==================== Victory Screen Colors ====================

    /** Victory main title gold (warm and soft) */
    public static final Color VICTORY_GOLD = new Color(1f, 0.82f, 0.3f, 1f);

    /** Victory border gradient start (warm gold) */
    public static final Color VICTORY_BORDER_START = new Color(0.85f, 0.65f, 0.2f, 1f);

    /** Victory border gradient end (tech blue) */
    public static final Color VICTORY_BORDER_END = new Color(0f, 0.75f, 0.9f, 1f);

    /** Victory card background (glass texture) */
    public static final Color VICTORY_CARD_BG = new Color(0.06f, 0.08f, 0.12f, 0.92f);

    /** Victory secondary text color */
    public static final Color VICTORY_TEXT_DIM = new Color(0.75f, 0.78f, 0.82f, 1f);

    /** Victory divider color */
    public static final Color VICTORY_DIVIDER = new Color(0.4f, 0.5f, 0.6f, 0.3f);

    // Rank glow colors /** Rank S - Brilliant Gold */
    public static final Color RANK_S_GLOW = new Color(1f, 0.85f, 0.1f, 1f);
    /** Rank A - Tech Blue */
    public static final Color RANK_A_GLOW = new Color(0.2f, 0.85f, 1f, 1f);
    /** Rank B - Emerald Green */
    public static final Color RANK_B_GLOW = new Color(0.3f, 0.9f, 0.5f, 1f);
    /** Rank C - Lemon Yellow */
    public static final Color RANK_C_GLOW = new Color(1f, 0.95f, 0.3f, 1f);
    /** Rank D - Warm Orange */
    public static final Color RANK_D_GLOW = new Color(1f, 0.6f, 0.2f, 1f);

    /** Primary button background gradient start */
    public static final Color BTN_PRIMARY_START = new Color(0.9f, 0.7f, 0.15f, 1f);
    /** Primary button background gradient end */
    public static final Color BTN_PRIMARY_END = new Color(0.75f, 0.55f, 0.1f, 1f);

    /** Secondary button border color */
    public static final Color BTN_SECONDARY_BORDER = new Color(0.5f, 0.55f, 0.6f, 0.8f);

    // ==================== Armor Select Screen Colors ====================
    /** Physical armor primary color (Steel Blue) */
    public static final Color ARMOR_PHYSICAL_COLOR = new Color(0.29f, 0.49f, 0.72f, 1f); // #4A7CB8

    /** Magical armor primary color (Mystic Purple) */
    public static final Color ARMOR_MAGICAL_COLOR = new Color(0.61f, 0.37f, 0.9f, 1f); // #9B5DE5

    /** Recommended armor glow color (Brilliant Gold) */
    public static final Color ARMOR_RECOMMENDED_GLOW = new Color(1f, 0.85f, 0.24f, 1f); // #FFD93D

    /** Armor card background (Glass Black) */
    public static final Color ARMOR_CARD_BG = new Color(0.04f, 0.06f, 0.1f, 0.88f);

    /** Armor card border */
    public static final Color ARMOR_CARD_BORDER = new Color(0.3f, 0.35f, 0.45f, 0.7f);

    /** Danger warning color (Physical threat) */
    public static final Color DANGER_PHYSICAL = new Color(0.4f, 0.6f, 0.9f, 1f);

    /** Danger warning color (Magical threat) */
    public static final Color DANGER_MAGICAL = new Color(0.75f, 0.4f, 0.95f, 1f);

    // ==================== Settings Screen Constants ====================
    /** Settings panel max width (to prevent excessive stretch) */
    public static final float SETTINGS_PANEL_MAX_WIDTH = 720f;

    /** Section content horizontal padding */
    public static final float SETTINGS_SECTION_PAD_H = 24f;

    /** Padding below titles */
    public static final float SETTINGS_TITLE_BOTTOM_PAD = 36f;

    /** Settings screen section title color (Gold) */
    public static final Color SETTINGS_SECTION_TITLE = new Color(1f, 0.85f, 0.35f, 1f);

    /** Settings screen divider color */
    public static final Color SETTINGS_DIVIDER = new Color(0.5f, 0.45f, 0.3f, 0.6f);

    /** Settings screen hint text color */
    public static final Color SETTINGS_HINT = new Color(0.6f, 0.6f, 0.6f, 1f);

    /** Settings screen unified slider width */
    public static final float SETTINGS_SLIDER_WIDTH = 140f;

    /** Settings screen volume slider width */
    public static final float SETTINGS_VOLUME_SLIDER_WIDTH = 160f;

    /** Settings screen key binding button width */
    public static final float SETTINGS_KEY_BTN_WIDTH = 70f;

    /** Settings screen key binding button height */
    public static final float SETTINGS_KEY_BTN_HEIGHT = 34f;

    /** Settings screen fixed label width */
    public static final float SETTINGS_LABEL_WIDTH = 90f;

    /** Settings screen value label width */
    public static final float SETTINGS_VALUE_LABEL_WIDTH = 45f;

    /** Settings screen row spacing (increased for better readability) */
    public static final float SETTINGS_ROW_SPACING = 18f;

    /** Settings screen section spacing (increased whitespace) */
    public static final float SETTINGS_SECTION_SPACING = 28f;

    /** Settings screen padding */
    public static final float SETTINGS_PADDING = 45f;

    /** Settings screen Music button width */
    public static final float SETTINGS_MUSIC_BTN_WIDTH = 95f;

    /** Settings screen Fog button width */
    public static final float SETTINGS_FOG_BTN_WIDTH = 60f;

    /** Settings screen Back button dimensions */
    public static final float SETTINGS_BACK_BTN_WIDTH = 170f;
    public static final float SETTINGS_BACK_BTN_HEIGHT = 48f;

    /** Unified slider height */
    public static final float SETTINGS_SLIDER_HEIGHT = 28f;

    /** Audio row component height */
    public static final float SETTINGS_AUDIO_ROW_HEIGHT = 36f;
}
