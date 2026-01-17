package de.tum.cit.fop.maze.utils;

import com.badlogic.gdx.Gdx;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles file drag and drop functionality for the game window.
 * Uses reflection to access LWJGL3 classes to avoid compile-time dependency on
 * desktop module.
 */
public class DragDropHandler {

    public interface DropListener {
        void onFilesDropped(String[] filePaths);
    }

    private static DropListener currentListener = null;
    private static boolean initialized = false;
    private static Object dropCallback = null;

    /**
     * Initialize drag and drop handling.
     * Call this once after the application window is created.
     */
    public static void initialize() {
        if (initialized)
            return;

        try {
            // Use reflection to access LWJGL3 classes
            Class<?> appClass = Class.forName("com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application");
            if (!appClass.isInstance(Gdx.app)) {
                GameLogger.warn("DragDropHandler", "Not running on LWJGL3, drag and drop disabled");
                return;
            }

            // Get window handle via reflection
            Object app = Gdx.app;
            java.lang.reflect.Method getCurrentWindowMethod = appClass.getMethod("getCurrentWindow");
            Object window = getCurrentWindowMethod.invoke(app);

            if (window == null) {
                GameLogger.warn("DragDropHandler", "No current window found");
                return;
            }

            Class<?> windowClass = Class.forName("com.badlogic.gdx.backends.lwjgl3.Lwjgl3Window");
            java.lang.reflect.Field handleField = windowClass.getDeclaredField("windowHandle");
            handleField.setAccessible(true);
            long windowHandle = handleField.getLong(window);

            // Setup GLFW drop callback via reflection
            Class<?> glfwClass = Class.forName("org.lwjgl.glfw.GLFW");
            Class<?> dropCallbackClass = Class.forName("org.lwjgl.glfw.GLFWDropCallbackI");

            // Create a proxy for the callback interface
            dropCallback = java.lang.reflect.Proxy.newProxyInstance(
                    dropCallbackClass.getClassLoader(),
                    new Class<?>[] { dropCallbackClass },
                    (proxy, method, args) -> {
                        if ("invoke".equals(method.getName()) && args.length == 3) {
                            long win = (Long) args[0];
                            int count = (Integer) args[1];
                            long names = (Long) args[2];

                            if (currentListener != null && count > 0) {
                                // Get the getName method
                                Class<?> dropCallbackStaticClass = Class.forName("org.lwjgl.glfw.GLFWDropCallback");
                                java.lang.reflect.Method getNameMethod = dropCallbackStaticClass.getMethod("getName",
                                        long.class, int.class);

                                String[] paths = new String[count];
                                for (int i = 0; i < count; i++) {
                                    paths[i] = (String) getNameMethod.invoke(null, names, i);
                                }

                                // Call on render thread
                                final String[] finalPaths = paths;
                                Gdx.app.postRunnable(() -> {
                                    if (currentListener != null) {
                                        currentListener.onFilesDropped(finalPaths);
                                    }
                                });
                            }
                        }
                        return null;
                    });

            // Call GLFW.glfwSetDropCallback(windowHandle, callback)
            java.lang.reflect.Method setDropCallbackMethod = glfwClass.getMethod("glfwSetDropCallback", long.class,
                    dropCallbackClass);
            setDropCallbackMethod.invoke(null, windowHandle, dropCallback);

            initialized = true;
            GameLogger.info("DragDropHandler", "Drag and drop initialized successfully");

        } catch (Exception e) {
            GameLogger.error("DragDropHandler", "Failed to initialize drag and drop: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Set the current drop listener.
     * Only one listener can be active at a time.
     */
    public static void setDropListener(DropListener listener) {
        currentListener = listener;
    }

    /**
     * Remove the current drop listener.
     */
    public static void clearDropListener() {
        currentListener = null;
    }

    /**
     * Check if a file is an image based on extension
     */
    public static boolean isImageFile(String path) {
        if (path == null)
            return false;
        String lower = path.toLowerCase();
        return lower.endsWith(".png") || lower.endsWith(".jpg") ||
                lower.endsWith(".jpeg") || lower.endsWith(".gif") ||
                lower.endsWith(".bmp");
    }

    /**
     * Filter dropped files to only include images
     */
    public static String[] filterImageFiles(String[] paths) {
        List<String> images = new ArrayList<>();
        for (String path : paths) {
            if (isImageFile(path)) {
                images.add(path);
            }
        }
        return images.toArray(new String[0]);
    }
}
