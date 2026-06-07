package com.example.onlineauctionsystem.utils;

import com.example.onlineauctionsystem.network.AuctionMessage;
import com.example.onlineauctionsystem.network.AuctionClient;
import javafx.scene.image.Image;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * Tiện ích xử lý ảnh sản phẩm.
 *
 * load(): Luôn chắc chắn hiện ảnh dù chạy local hay qua mạng.
 *   1. Tìm file local (máy Server/máy Host đang chạy IntelliJ)
 *   2. Nếu không có → hỏi Server qua network, nhận byte[] rồi render
 *   3. Fallback classpath (JAR)
 *
 * save(): Lưu ảnh vào folder trên máy Server VÀ upload lên server qua socket.
 */
public enum ProductImage {
    ;

    public static final String FOLDER_NAME = "Product_Image";
    public static final String FOLDER_PATH =
            System.getProperty("user.dir") + "/src/main/resources/" + FOLDER_NAME + "/";

    // ──────────────────────────────────────────────────────────────
    //  LOAD – hiển thị ảnh lên UI (Client gọi hàm này)
    // ──────────────────────────────────────────────────────────────
    public static Image load(String imagePath, double width, double height) {
        if (imagePath == null || imagePath.isEmpty()) return null;

        // Cách 1: tìm file local (máy đang chạy IntelliJ / là Host)
        File local = new File(FOLDER_PATH + imagePath.replace(FOLDER_NAME + "/", ""));
        if (local.exists()) {
            try {
                return new Image(local.toURI().toString(), width, height, true, true);
            } catch (Exception e) {
                System.err.println("[ProductImage] Local load lỗi: " + e.getMessage());
            }
        }

        // Cách 1b: thư mục user.home (fallback khi src/ không tồn tại)
        File homeFile = new File(System.getProperty("user.home")
                + "/Product_Image/" + imagePath.replace(FOLDER_NAME + "/", ""));
        if (homeFile.exists()) {
            try {
                return new Image(homeFile.toURI().toString(), width, height, true, true);
            } catch (Exception e) {
                System.err.println("[ProductImage] HomeFile load lỗi: " + e.getMessage());
            }
        }

        // Cách 2: hỏi Server qua socket – nhận byte[] rồi render
        // (dùng khi máy Client không có file ảnh local)
        try {
            AuctionMessage res = AuctionClient.send(
                    new AuctionMessage(AuctionMessage.Action.GET_IMAGE, imagePath)
            );
            if (res.getAction() == AuctionMessage.Action.SUCCESS && res.getData() instanceof byte[] bytes) {
                return new Image(new ByteArrayInputStream(bytes), width, height, true, true);
            }
        } catch (Exception e) {
            System.err.println("[ProductImage] Network load lỗi: " + e.getMessage());
        }

        // Cách 3: classpath (sau khi đóng gói JAR)
        try {
            InputStream stream = ProductImage.class.getResourceAsStream("/" + imagePath);
            if (stream != null) {
                return new Image(stream, width, height, true, true);
            }
        } catch (Exception e) {
            System.err.println("[ProductImage] Classpath load lỗi: " + e.getMessage());
        }

        System.err.println("[ProductImage] Không tìm thấy ảnh: " + imagePath);
        return null;
    }

    // ──────────────────────────────────────────────────────────────
    //  SAVE – lưu ảnh vào disk cục bộ (Seller gọi khi đăng sản phẩm)
    //  Trả về đường dẫn tương đối, ví dụ: "Product_Image/12345_abc.jpg"
    //  Lưu ý: AddProductController sẽ tự gọi uploadImage() sau hàm này
    // ──────────────────────────────────────────────────────────────
    public static String save(File sourceFile) {
        String newFileName = System.currentTimeMillis() + "_" + sourceFile.getName();

        // Ưu tiên lưu vào thư mục src/main/resources (môi trường dev)
        File destFolder = new File(FOLDER_PATH);
        if (!destFolder.exists()) {
            destFolder.mkdirs();
        }

        // Nếu mkdirs thất bại (ví dụ JAR mode), dùng user.home
        if (!destFolder.exists() || !destFolder.isDirectory()) {
            destFolder = new File(System.getProperty("user.home") + "/Product_Image/");
            destFolder.mkdirs();
        }

        File destFile = new File(destFolder, newFileName);
        try {
            Files.copy(sourceFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return FOLDER_NAME + "/" + newFileName;
    }

    // ──────────────────────────────────────────────────────────────
    //  LOAD ASYNC – load ảnh trên background thread, gán vào ImageView
    //  Tránh block UI thread khi phải gọi network
    // ──────────────────────────────────────────────────────────────
    public static void loadAsync(String imagePath, double width, double height,
                                 javafx.scene.image.ImageView imageView) {
        if (imagePath == null || imagePath.isEmpty()) return;
        Thread t = new Thread(() -> {
            Image img = load(imagePath, width, height);
            if (img != null) {
                javafx.application.Platform.runLater(() -> imageView.setImage(img));
            }
        });
        t.setDaemon(true);
        t.start();
    }

    // ──────────────────────────────────────────────────────────────
    //  DELETE – xóa ảnh khỏi disk
    // ──────────────────────────────────────────────────────────────
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