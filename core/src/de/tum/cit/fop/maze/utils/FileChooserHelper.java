package de.tum.cit.fop.maze.utils;

import com.badlogic.gdx.Gdx;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;

/**
 * Platform-agnostic file chooser utility.
 * Uses Swing JFileChooser with a dedicated Frame to avoid blocking issues on
 * macOS.
 */
public class FileChooserHelper {

    public interface FileChosenCallback {
        void onFileChosen(String absolutePath);

        void onCancelled();
    }

    private static File lastDirectory = null;
    private static JFrame helperFrame = null;

    /**
     * Get or create a helper frame for dialogs
     */
    private static JFrame getHelperFrame() {
        if (helperFrame == null) {
            helperFrame = new JFrame();
            helperFrame.setUndecorated(true);
            helperFrame.setSize(0, 0);
            helperFrame.setLocationRelativeTo(null);
        }
        return helperFrame;
    }

    /**
     * Show a file chooser dialog for selecting image files.
     */
    public static void chooseImageFile(FileChosenCallback callback) {
        // Run file chooser in a new thread to avoid blocking
        Thread chooserThread = new Thread(() -> {
            try {
                // Set look and feel
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception ignored) {
                }

                // Create and configure file chooser
                JFileChooser chooser = new JFileChooser();
                chooser.setDialogTitle("Select Sprite Image");
                chooser.setFileFilter(new FileNameExtensionFilter(
                        "Image Files (*.png, *.jpg, *.jpeg, *.gif)",
                        "png", "jpg", "jpeg", "gif"));
                chooser.setAcceptAllFileFilterUsed(false);

                // Remember last directory
                if (lastDirectory != null && lastDirectory.exists()) {
                    chooser.setCurrentDirectory(lastDirectory);
                }

                // Show dialog with helper frame as parent
                JFrame frame = getHelperFrame();
                frame.setVisible(true);
                frame.toFront();
                frame.requestFocus();

                int result = chooser.showOpenDialog(frame);

                frame.setVisible(false);

                if (result == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = chooser.getSelectedFile();
                    lastDirectory = selectedFile.getParentFile();
                    final String path = selectedFile.getAbsolutePath();
                    Gdx.app.postRunnable(() -> callback.onFileChosen(path));
                } else {
                    Gdx.app.postRunnable(() -> callback.onCancelled());
                }
            } catch (Exception e) {
                GameLogger.error("FileChooserHelper", "Failed to open file dialog: " + e.getMessage());
                Gdx.app.postRunnable(() -> callback.onCancelled());
            }
        }, "FileChooser-Thread");

        chooserThread.setDaemon(true);
        chooserThread.start();
    }

    /**
     * Show a file chooser dialog for selecting any file.
     */
    public static void chooseFile(String title, FileChosenCallback callback) {
        Thread chooserThread = new Thread(() -> {
            try {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception ignored) {
                }

                JFileChooser chooser = new JFileChooser();
                chooser.setDialogTitle(title);

                if (lastDirectory != null && lastDirectory.exists()) {
                    chooser.setCurrentDirectory(lastDirectory);
                }

                JFrame frame = getHelperFrame();
                frame.setVisible(true);
                frame.toFront();

                int result = chooser.showOpenDialog(frame);

                frame.setVisible(false);

                if (result == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = chooser.getSelectedFile();
                    lastDirectory = selectedFile.getParentFile();
                    final String path = selectedFile.getAbsolutePath();
                    Gdx.app.postRunnable(() -> callback.onFileChosen(path));
                } else {
                    Gdx.app.postRunnable(() -> callback.onCancelled());
                }
            } catch (Exception e) {
                GameLogger.error("FileChooserHelper", "Failed to open file dialog: " + e.getMessage());
                Gdx.app.postRunnable(() -> callback.onCancelled());
            }
        }, "FileChooser-Thread");

        chooserThread.setDaemon(true);
        chooserThread.start();
    }

    /**
     * Show a directory chooser dialog.
     */
    public static void chooseDirectory(String title, FileChosenCallback callback) {
        Thread chooserThread = new Thread(() -> {
            try {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception ignored) {
                }

                JFileChooser chooser = new JFileChooser();
                chooser.setDialogTitle(title);
                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

                if (lastDirectory != null && lastDirectory.exists()) {
                    chooser.setCurrentDirectory(lastDirectory);
                }

                JFrame frame = getHelperFrame();
                frame.setVisible(true);
                frame.toFront();

                int result = chooser.showOpenDialog(frame);

                frame.setVisible(false);

                if (result == JFileChooser.APPROVE_OPTION) {
                    File selectedDir = chooser.getSelectedFile();
                    lastDirectory = selectedDir;
                    final String path = selectedDir.getAbsolutePath();
                    Gdx.app.postRunnable(() -> callback.onFileChosen(path));
                } else {
                    Gdx.app.postRunnable(() -> callback.onCancelled());
                }
            } catch (Exception e) {
                GameLogger.error("FileChooserHelper", "Failed to open directory dialog: " + e.getMessage());
                Gdx.app.postRunnable(() -> callback.onCancelled());
            }
        }, "FileChooser-Thread");

        chooserThread.setDaemon(true);
        chooserThread.start();
    }

    /**
     * Cleanup helper frame on game dispose
     */
    public static void dispose() {
        if (helperFrame != null) {
            helperFrame.dispose();
            helperFrame = null;
        }
    }
}
