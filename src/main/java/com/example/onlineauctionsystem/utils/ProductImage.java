package com.example.onlineauctionsystem.utils;

import javafx.scene.image.Image;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public enum ProductImage {

    ;

    // ── Đường dẫn folder ──────────────────────────────────────────
    public static final String FOLDER_NAME = "Product_Image";
    public static final String FOLDER_PATH =
            System.getProperty("user.dir") + "/src/main/resources/" + FOLDER_NAME + "/";

    // ── Lưu ảnh vào folder ────────────────────────────────────────
    public static String save(File sourceFile) {
        try {
            File destFolder = new File(FOLDER_PATH);
            if (!destFolder.exists() && !destFolder.mkdirs()) {
                destFolder = new File(System.getProperty("user.home") + "/Product_Image/");
                destFolder.mkdirs();
            }

            String newFileName = System.currentTimeMillis() + "_" + sourceFile.getName();
            File destFile = new File(destFolder, newFileName);

            Files.copy(sourceFile.toPath(), destFile.toPath(),
                    StandardCopyOption.REPLACE_EXISTING);

            return FOLDER_NAME + "/" + newFileName;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // ── Load ảnh để hiển thị lên UI ───────────────────────────────
    public static Image load(String imagePath, double width, double height) {
        if (imagePath == null || imagePath.isEmpty()) return null;

        try {
            // Cách 1: absolute path từ working dir (khi chạy trong IntelliJ)
            File file = new File(FOLDER_PATH + imagePath.replace(FOLDER_NAME + "/", ""));
            if (file.exists()) {
                return new Image(file.toURI().toString(), width, height, true, true);
            }

            // Cách 2: absolute path từ user.home/Product_Image (khi chạy nhiều máy)
            File homeFile = new File(System.getProperty("user.home")
                    + "/Product_Image/" + imagePath.replace(FOLDER_NAME + "/", ""));
            if (homeFile.exists()) {
                return new Image(homeFile.toURI().toString(), width, height, true, true);
            }

            // Cách 3: classpath resource (sau khi đóng gói JAR)
            InputStream stream = ProductImage.class.getResourceAsStream("/" + imagePath);
            if (stream != null) {
                return new Image(stream, width, height, true, true);
            }

        } catch (Exception e) {
            System.err.println("[ProductImage] Không load được ảnh: " + imagePath + " - " + e.getMessage());
        }

        return null;
    }

    // ── Xóa ảnh khỏi folder ───────────────────────────────────────
    public static boolean delete(String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) return false;
        try {
            File file = new File(FOLDER_PATH + imagePath.replace(FOLDER_NAME + "/", ""));
            return file.exists() && file.delete();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}