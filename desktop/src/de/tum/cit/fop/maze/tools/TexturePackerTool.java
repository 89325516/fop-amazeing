package de.tum.cit.fop.maze.tools;

import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.tools.texturepacker.TexturePacker;
import java.io.File;

/**
 * Utility tool for packing texture assets into a TextureAtlas.
 * This tool runs as a standalone Java application (not part of the game
 * runtime)
 * to process raw images from the {@code raw_assets} directory and output them
 * to the {@code assets/images} directory.
 */
public class TexturePackerTool {
    private static final String INPUT_DIR = "../raw_assets";
    private static final String OUTPUT_DIR = "../assets/images";

    /**
     * Main entry point for the texture packer tool.
     * Executes the texture packing process.
     *
     * @param args Command line arguments (not used).
     */
    public static void main(String[] args) {
        packTexturesOptimized();
    }

    /**
     * Packs textures with optimized settings for the game.
     * <p>
     * It scans subdirectories in the input folder and creates a separate atlas for
     * each.
     * Two passes are performed:
     * 1. High-resolution pack (scale 1.0)
     * 2. Low-resolution pack (scale 0.5) for performance/lower-end devices.
     * </p>
     */
    private static void packTexturesOptimized() {
        TexturePacker.Settings settings = new TexturePacker.Settings();
        settings.maxWidth = 2048;
        settings.maxHeight = 2048;
        settings.stripWhitespaceX = false;
        settings.stripWhitespaceY = false;
        settings.paddingX = 2;
        settings.paddingY = 2;
        settings.filterMin = TextureFilter.MipMapLinearLinear;
        settings.filterMag = TextureFilter.Linear;
        settings.combineSubdirectories = true;
        settings.flattenPaths = true;

        File inputDirFile = new File(INPUT_DIR);
        if (!inputDirFile.exists()) {
            System.out.println("Skipping texture packing: " + inputDirFile.getAbsolutePath() + " not found.");
            return;
        }

        File[] subDirs = inputDirFile.listFiles(File::isDirectory);
        if (subDirs == null || subDirs.length == 0) {
            System.out.println("No subdirectories found in " + inputDirFile.getAbsolutePath());
            return;
        }

        for (File subDir : subDirs) {
            String packName = subDir.getName(); // e.g., "gameplay", "ui"
            System.out.println("Packing " + packName + "...");

            try {
                // Pass 1: High Res (1.0)
                // Reset scale to avoid side effects
                // If we use scale array, GDX might create folders.
                // Let's try NOT setting scale for 1.0 if possible, or verify behavior.
                // Actually, if we just want the output to be exactly in OUTPUT_DIR with name
                // packName.atlas,
                // we should be careful.
                // Experiment: For 1.0, let's try standard process without scale array first?
                // But we need the loop.

                // Let's try this:
                // Using a hack: if we use a different output directory for 1.0?
                // No.

                // Pass 1: High Res (1.0)
                // When using a single scale, TexturePacker puts output in the root (no
                // subfolder usually).
                settings.scale = new float[] { 1.0f };
                settings.scaleSuffix = new String[] { "" };

                TexturePacker.process(settings, subDir.getAbsolutePath(), OUTPUT_DIR, packName);

                // Pass 2: Low Res (0.5)
                settings.scale = new float[] { 0.5f };
                settings.scaleSuffix = new String[] { "_low" };

                TexturePacker.process(settings, subDir.getAbsolutePath(), OUTPUT_DIR, packName);

            } catch (Exception e) {
                System.err.println("Error packing " + packName + ": " + e.getMessage());
            }
        }
    }
}
