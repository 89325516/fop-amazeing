package de.tum.cit.fop.maze.utils;

import com.badlogic.gdx.graphics.Color;

/**
 * UI常量类 (UI Constants)
 * 
 * 定义全局统一的UI尺寸、颜色等常量，确保界面一致性。
 */
public final class UIConstants {

    private UIConstants() {
    } // 防止实例化

    // ==================== 按钮尺寸 ====================

    /** 大按钮宽度（主菜单按钮、主要操作按钮） */
    public static final float BTN_WIDTH_LARGE = 300f;
    /** 大按钮高度 */
    public static final float BTN_HEIGHT_LARGE = 60f;

    /** 中等按钮宽度（对话框按钮、次要操作） */
    public static final float BTN_WIDTH_MEDIUM = 200f;
    /** 中等按钮高度 */
    public static final float BTN_HEIGHT_MEDIUM = 50f;

    /** 小按钮宽度（工具栏按钮、标签页） */
    public static final float BTN_WIDTH_SMALL = 120f;
    /** 小按钮高度 */
    public static final float BTN_HEIGHT_SMALL = 40f;

    // ==================== Viewport ====================

    /** 标准虚拟宽度 */
    public static final float VIEWPORT_WIDTH = 1920f;
    /** 标准虚拟高度 */
    public static final float VIEWPORT_HEIGHT = 1080f;

    // ==================== 背景颜色 ====================

    /** 默认深色背景 */
    public static final Color BG_COLOR_DEFAULT = new Color(0.08f, 0.08f, 0.1f, 1f);
    /** 菜单背景 */
    public static final Color BG_COLOR_MENU = new Color(0.05f, 0.05f, 0.08f, 1f);
    /** 商店背景 */
    public static final Color BG_COLOR_SHOP = new Color(0.1f, 0.1f, 0.15f, 1f);
    /** 设置背景 */
    public static final Color BG_COLOR_SETTINGS = new Color(0.1f, 0.1f, 0.15f, 1f);
    /** 胜利背景（深绿） */
    public static final Color BG_COLOR_VICTORY = new Color(0f, 0.15f, 0.05f, 1f);
    /** 失败背景（深红） */
    public static final Color BG_COLOR_GAMEOVER = new Color(0.15f, 0.02f, 0.02f, 1f);

    // ==================== 卡片/面板颜色 ====================

    /** 卡片背景（半透明深灰） */
    public static final Color CARD_BG = new Color(0.15f, 0.15f, 0.2f, 0.9f);
    /** 卡片边框颜色 */
    public static final Color CARD_BORDER = new Color(0.3f, 0.3f, 0.4f, 1f);

    // ==================== 间距 ====================

    /** 标准内边距 */
    public static final float PAD_STANDARD = 20f;
    /** 小内边距 */
    public static final float PAD_SMALL = 10f;
    /** 大内边距 */
    public static final float PAD_LARGE = 40f;

    // ==================== 稀有度颜色 ====================

    /** 普通稀有度 */
    public static final Color RARITY_COMMON = Color.WHITE;
    /** 稀有 */
    public static final Color RARITY_RARE = new Color(0.4f, 0.7f, 1.0f, 1f);
    /** 史诗 */
    public static final Color RARITY_EPIC = new Color(0.8f, 0.4f, 1.0f, 1f);
    /** 传说 */
    public static final Color RARITY_LEGENDARY = new Color(1.0f, 0.85f, 0.2f, 1f);

    // ==================== Help Screen 颜色 ====================

    /** 帮助界面科技蓝边框 */
    public static final Color HELP_BORDER_CYAN = new Color(0f, 0.83f, 1f, 1f); // #00D4FF

    /** 帮助界面导航项选中背景 */
    public static final Color HELP_NAV_SELECTED = new Color(0.1f, 0.3f, 0.5f, 0.9f);

    /** 帮助界面导航项默认背景 */
    public static final Color HELP_NAV_DEFAULT = new Color(0.12f, 0.12f, 0.16f, 0.8f);

    /** 警告/危险提示颜色 */
    public static final Color HELP_WARN = new Color(1f, 0.42f, 0.21f, 1f); // #FF6B35

    /** 帮助界面内容卡片背景 */
    public static final Color HELP_CARD_BG = new Color(0.1f, 0.1f, 0.14f, 0.95f);

    /** 帮助标题颜色 (金色) */
    public static final Color HELP_TITLE_GOLD = new Color(1f, 0.85f, 0.2f, 1f);

    // ==================== Victory Screen 颜色 ====================

    /** Victory 主标题金色 (温暖柔和) */
    public static final Color VICTORY_GOLD = new Color(1f, 0.82f, 0.3f, 1f);

    /** Victory 边框渐变起始色 (暖金) */
    public static final Color VICTORY_BORDER_START = new Color(0.85f, 0.65f, 0.2f, 1f);

    /** Victory 边框渐变结束色 (科技蓝) */
    public static final Color VICTORY_BORDER_END = new Color(0f, 0.75f, 0.9f, 1f);

    /** Victory 卡片背景 (玻璃质感) */
    public static final Color VICTORY_CARD_BG = new Color(0.06f, 0.08f, 0.12f, 0.92f);

    /** Victory 次要文字颜色 */
    public static final Color VICTORY_TEXT_DIM = new Color(0.75f, 0.78f, 0.82f, 1f);

    /** Victory 分割线颜色 */
    public static final Color VICTORY_DIVIDER = new Color(0.4f, 0.5f, 0.6f, 0.3f);

    // Rank 发光颜色
    /** S级 - 璀璨金 */
    public static final Color RANK_S_GLOW = new Color(1f, 0.85f, 0.1f, 1f);
    /** A级 - 科技蓝 */
    public static final Color RANK_A_GLOW = new Color(0.2f, 0.85f, 1f, 1f);
    /** B级 - 翡翠绿 */
    public static final Color RANK_B_GLOW = new Color(0.3f, 0.9f, 0.5f, 1f);
    /** C级 - 柠檬黄 */
    public static final Color RANK_C_GLOW = new Color(1f, 0.95f, 0.3f, 1f);
    /** D级 - 暖橙 */
    public static final Color RANK_D_GLOW = new Color(1f, 0.6f, 0.2f, 1f);

    /** 主要按钮背景渐变起始 */
    public static final Color BTN_PRIMARY_START = new Color(0.9f, 0.7f, 0.15f, 1f);
    /** 主要按钮背景渐变结束 */
    public static final Color BTN_PRIMARY_END = new Color(0.75f, 0.55f, 0.1f, 1f);

    /** 次要按钮边框颜色 */
    public static final Color BTN_SECONDARY_BORDER = new Color(0.5f, 0.55f, 0.6f, 0.8f);

    // ==================== Armor Select Screen 颜色 ====================

    /** 物理护甲主色 (钢铁蓝) */
    public static final Color ARMOR_PHYSICAL_COLOR = new Color(0.29f, 0.49f, 0.72f, 1f); // #4A7CB8

    /** 魔法护甲主色 (神秘紫) */
    public static final Color ARMOR_MAGICAL_COLOR = new Color(0.61f, 0.37f, 0.9f, 1f); // #9B5DE5

    /** 推荐护甲发光色 (璀璨金) */
    public static final Color ARMOR_RECOMMENDED_GLOW = new Color(1f, 0.85f, 0.24f, 1f); // #FFD93D

    /** 护甲卡片背景 (玻璃黑) */
    public static final Color ARMOR_CARD_BG = new Color(0.04f, 0.06f, 0.1f, 0.88f);

    /** 护甲卡片边框 */
    public static final Color ARMOR_CARD_BORDER = new Color(0.3f, 0.35f, 0.45f, 0.7f);

    /** 危险警告色 (物理威胁) */
    public static final Color DANGER_PHYSICAL = new Color(0.4f, 0.6f, 0.9f, 1f);

    /** 危险警告色 (魔法威胁) */
    public static final Color DANGER_MAGICAL = new Color(0.75f, 0.4f, 0.95f, 1f);

    // ==================== Settings Screen 常量 ====================

    /** 设置面板最大宽度 (避免过宽) */
    public static final float SETTINGS_PANEL_MAX_WIDTH = 720f;

    /** 区块内容左右内边距 */
    public static final float SETTINGS_SECTION_PAD_H = 24f;

    /** 标题下方间距 */
    public static final float SETTINGS_TITLE_BOTTOM_PAD = 36f;

    /** 设置界面区块标题颜色 (金色) */
    public static final Color SETTINGS_SECTION_TITLE = new Color(1f, 0.85f, 0.35f, 1f);

    /** 设置界面分割线颜色 */
    public static final Color SETTINGS_DIVIDER = new Color(0.5f, 0.45f, 0.3f, 0.6f);

    /** 设置界面提示文字颜色 */
    public static final Color SETTINGS_HINT = new Color(0.6f, 0.6f, 0.6f, 1f);

    /** 设置界面滑块统一宽度 */
    public static final float SETTINGS_SLIDER_WIDTH = 140f;

    /** 设置界面音量滑块宽度 */
    public static final float SETTINGS_VOLUME_SLIDER_WIDTH = 160f;

    /** 设置界面按键按钮宽度 */
    public static final float SETTINGS_KEY_BTN_WIDTH = 70f;

    /** 设置界面按键按钮高度 */
    public static final float SETTINGS_KEY_BTN_HEIGHT = 34f;

    /** 设置界面标签固定宽度 */
    public static final float SETTINGS_LABEL_WIDTH = 90f;

    /** 设置界面数值标签宽度 */
    public static final float SETTINGS_VALUE_LABEL_WIDTH = 45f;

    /** 设置界面行间距 (增大以改善可读性) */
    public static final float SETTINGS_ROW_SPACING = 18f;

    /** 设置界面区块间距 (增大留白) */
    public static final float SETTINGS_SECTION_SPACING = 28f;

    /** 设置界面内边距 */
    public static final float SETTINGS_PADDING = 45f;

    /** 设置界面 Music 按钮宽度 */
    public static final float SETTINGS_MUSIC_BTN_WIDTH = 95f;

    /** 设置界面 Fog 按钮宽度 */
    public static final float SETTINGS_FOG_BTN_WIDTH = 60f;

    /** 设置界面 Back 按钮尺寸 */
    public static final float SETTINGS_BACK_BTN_WIDTH = 170f;
    public static final float SETTINGS_BACK_BTN_HEIGHT = 48f;

    /** 滑块统一高度 */
    public static final float SETTINGS_SLIDER_HEIGHT = 28f;

    /** 音频行组件高度 */
    public static final float SETTINGS_AUDIO_ROW_HEIGHT = 36f;
}
