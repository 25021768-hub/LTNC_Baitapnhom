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
    // Trả về đường dẫn tương đối để lưu vào DB
    // VD: "Product_Image/1234567890_laptop.png"
    public static String save(File sourceFile) {
        try {
            File destFolder = new File(FOLDER_PATH);
            if (!destFolder.exists()) destFolder.mkdirs();

            // Đặt tên file = timestamp + tên gốc để tránh trùng
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
            InputStream stream = ProductImage.class.getResourceAsStream("/" + imagePath);
            if (stream != null) {
                return new Image(stream, width, height, true, true);
            }

            // Cách 2: Load từ file system (khi dev, chưa build)
            File file = new File(FOLDER_PATH +
                    imagePath.replace(FOLDER_NAME + "/", ""));
            if (file.exists()) {
                return new Image(file.toURI().toString(), width, height, true, true);
            }

        } catch (Exception e) {
            System.err.println("Không load được ảnh: " + imagePath);
        }

        return null;
    }

    // ── Xóa ảnh khỏi folder ───────────────────────────────────────
    // Dùng khi seller xóa sản phẩm
    public static boolean delete(String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) return false;
        try {
            File file = new File(FOLDER_PATH +
                    imagePath.replace(FOLDER_NAME + "/", ""));
            return file.exists() && file.delete();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}