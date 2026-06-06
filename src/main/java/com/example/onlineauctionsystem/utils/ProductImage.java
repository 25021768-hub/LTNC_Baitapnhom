package com.example.onlineauctionsystem.utils;

import javafx.scene.image.Image;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public final class ProductImage {
    private ProductImage() {}

    // ── Đường dẫn folder ──────────────────────────────────────────
    public static final String FOLDER_NAME = "Product_Image";
    public static final String FOLDER_PATH =
            System.getProperty("user.dir") + "/src/main/resources/" + FOLDER_NAME + "/";

    // ── Lưu ảnh vào folder ────────────────────────────────────────
    // Trả về đường dẫn tương đối để lưu vào DB
    // VD: "Product_Image/1234567890_laptop.png"
    public static String save(File sourceFile) {
        if (sourceFile == null || !sourceFile.exists()) return null;

        try {
            File destFolder = new File(FOLDER_PATH);
            // Fallback sang user.home nếu thư mục src không tồn tại (máy client).
            if (!destFolder.exists() && !destFolder.mkdirs()) {
                destFolder = new File(System.getProperty("user.home") + "/" + FOLDER_NAME + "/");
                destFolder.mkdirs();
            }

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
            // Cách 1: Load từ file system khi chạy trong IntelliJ.
            File file = new File(FOLDER_PATH +
                    imagePath.replace(FOLDER_NAME + "/", ""));
            if (file.exists()) {
                return new Image(file.toURI().toString(), width, height, true, true);
            }

            // Cách 2: Load từ user.home/Product_Image khi chạy nhiều máy.
            File homeFile = new File(System.getProperty("user.home") + "/" + FOLDER_NAME + "/" +
                    imagePath.replace(FOLDER_NAME + "/", ""));
            if (homeFile.exists()) {
                return new Image(homeFile.toURI().toString(), width, height, true, true);
            }

            // Cách 3: Load từ classpath resource sau khi đóng gói.
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
    // Dùng khi seller xóa sản phẩm
    public static boolean delete(String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) return false;
        try {
            File file = new File(FOLDER_PATH +
                    imagePath.replace(FOLDER_NAME + "/", ""));
            if (file.exists()) {
                return file.delete();
            }

            File homeFile = new File(System.getProperty("user.home") + "/" + FOLDER_NAME + "/" +
                    imagePath.replace(FOLDER_NAME + "/", ""));
            return homeFile.exists() && homeFile.delete();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}