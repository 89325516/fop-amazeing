package de.tum.cit.fop.maze.utils;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Timer;

/**
 * Dialog Factory Class (Dialog Factory)
 * 
 * Provides unified style dialog creation methods to ensure UI consistency.
 */
public final class DialogFactory {

    private DialogFactory() {
    } // Prevent instantiation

    /**
     * Creates and shows an info tip dialog with auto-hide functionality.
     *
     * @param stage           Target Stage
     * @param skin            Skin
     * @param title           Title (can be empty string)
     * @param message         Message content
     * @param autoHideSeconds Auto-hide time (seconds), 0 means no auto-hide
     * @return Created Dialog instance
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
     * Creates and shows a warning dialog (title in red).
     *
     * @param stage   Target Stage
     * @param skin    Skin
     * @param title   Title
     * @param message Message content
     * @return Created Dialog instance
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
     * Creates and shows a confirmation dialog (with confirm and cancel buttons).
     *
     * @param stage     Target Stage
     * @param skin      Skin
     * @param title     Title
     * @param message   Message content
     * @param onConfirm Confirm callback
     * @param onCancel  Cancel callback (can be null)
     * @return Created Dialog instance
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
     * Creates and shows an insufficient funds dialog (shop specific).
     *
     * @param stage        Target Stage
     * @param skin         Skin
     * @param itemPrice    Item price
     * @param currentCoins Current coins
     * @return Created Dialog instance
     */
    public static Dialog showInsufficientFundsDialog(Stage stage, Skin skin, int itemPrice, int currentCoins) {
        int needed = itemPrice - currentCoins;
        String message = "Insufficient coins!\n" +
                "Need: " + itemPrice + ", Have: " + currentCoins + "\n" +
                "Short by: " + needed;

        return showInfoDialog(stage, skin, "Not Enough Gold", message, 2.0f);
    }
}
