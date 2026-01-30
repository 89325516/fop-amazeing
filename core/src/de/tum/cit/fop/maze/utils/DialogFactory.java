package de.tum.cit.fop.maze.utils;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Timer;

/**
 * Dialog Factory.
 * 
 * Provides consistent dialog creation methods to ensure UI consistency.
 */
public final class DialogFactory {

    private DialogFactory() {
    } // Prevent instantiation

    /**
     * Creates and shows an info dialog with auto-hide functionality.
     *
     * @param stage           The target Stage.
     * @param skin            The skin to use.
     * @param title           The title (can be empty).
     * @param message         The message content.
     * @param autoHideSeconds Auto-hide duration in seconds (0 to disable
     *                        auto-hide).
     * @return The created Dialog instance.
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
     * Creates and shows a warning dialog (with red title).
     *
     * @param stage   The target Stage.
     * @param skin    The skin to use.
     * @param title   The title.
     * @param message The message content.
     * @return The created Dialog instance.
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
     * Creates and shows a confirmation dialog (with Confirm and Cancel buttons).
     *
     * @param stage     The target Stage.
     * @param skin      The skin to use.
     * @param title     The title.
     * @param message   The message content.
     * @param onConfirm Callback for confirmation.
     * @param onCancel  Callback for cancellation (can be null).
     * @return The created Dialog instance.
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
     * Creates and shows an insufficient funds dialog (specifically for shops).
     *
     * @param stage        The target Stage.
     * @param skin         The skin to use.
     * @param itemPrice    The item price.
     * @param currentCoins The current coins.
     * @return The created Dialog instance.
     */
    public static Dialog showInsufficientFundsDialog(Stage stage, Skin skin, int itemPrice, int currentCoins) {
        int needed = itemPrice - currentCoins;
        String message = "Insufficient coins!\n" +
                "Need: " + itemPrice + ", Have: " + currentCoins + "\n" +
                "Short by: " + needed;

        return showInfoDialog(stage, skin, "Not Enough Gold", message, 2.0f);
    }
}
