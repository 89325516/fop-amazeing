package de.tum.cit.fop.maze.utils;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Timer;

/**
 * Dialog工厂类 (Dialog Factory)
 * 
 * 提供统一风格的对话框创建方法，确保UI一致性。
 */
public final class DialogFactory {

    private DialogFactory() {
    } // 防止实例化

    /**
     * 创建并显示一个带自动消失功能的信息提示对话框。
     *
     * @param stage           目标Stage
     * @param skin            皮肤
     * @param title           标题（可为空字符串）
     * @param message         消息内容
     * @param autoHideSeconds 自动消失时间（秒），0表示不自动消失
     * @return 创建的Dialog实例
     */
    public static Dialog showInfoDialog(Stage stage, Skin skin, String title, String message, float autoHideSeconds) {
        Dialog dialog = new Dialog(title, skin);
        dialog.text(message);
        dialog.getTitleLabel().setAlignment(Align.center);
        dialog.button("OK");
        dialog.show(stage);

        if (autoHideSeconds > 0) {
            Timer.schedule(new Timer.Task() {
                @Override
                public void run() {
                    dialog.hide();
                }
            }, autoHideSeconds);
        }

        return dialog;
    }

    /**
     * 创建并显示一个警告对话框（标题为红色）。
     *
     * @param stage   目标Stage
     * @param skin    皮肤
     * @param title   标题
     * @param message 消息内容
     * @return 创建的Dialog实例
     */
    public static Dialog showWarningDialog(Stage stage, Skin skin, String title, String message) {
        Dialog dialog = new Dialog(title, skin);
        dialog.getTitleLabel().setColor(Color.RED);
        dialog.getTitleLabel().setAlignment(Align.center);
        dialog.text(message);
        dialog.button("OK");
        dialog.show(stage);

        return dialog;
    }

    /**
     * 创建并显示一个确认对话框（带确认和取消按钮）。
     *
     * @param stage     目标Stage
     * @param skin      皮肤
     * @param title     标题
     * @param message   消息内容
     * @param onConfirm 确认回调
     * @param onCancel  取消回调（可为null）
     * @return 创建的Dialog实例
     */
    public static Dialog showConfirmDialog(Stage stage, Skin skin, String title, String message,
            Runnable onConfirm, Runnable onCancel) {
        Dialog dialog = new Dialog(title, skin) {
            @Override
            protected void result(Object object) {
                if ((Boolean) object) {
                    if (onConfirm != null)
                        onConfirm.run();
                } else {
                    if (onCancel != null)
                        onCancel.run();
                }
            }
        };
        dialog.getTitleLabel().setAlignment(Align.center);
        dialog.text(message);
        dialog.button("Confirm", true);
        dialog.button("Cancel", false);
        dialog.show(stage);

        return dialog;
    }

    /**
     * 创建并显示一个余额不足对话框（商店专用）。
     *
     * @param stage        目标Stage
     * @param skin         皮肤
     * @param itemPrice    物品价格
     * @param currentCoins 当前金币
     * @return 创建的Dialog实例
     */
    public static Dialog showInsufficientFundsDialog(Stage stage, Skin skin, int itemPrice, int currentCoins) {
        int needed = itemPrice - currentCoins;
        String message = "Insufficient coins!\n" +
                "Need: " + itemPrice + ", Have: " + currentCoins + "\n" +
                "Short by: " + needed;

        return showInfoDialog(stage, skin, "Not Enough Gold", message, 2.0f);
    }
}
