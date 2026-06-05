package com.example.onlineauctionsystem;

import com.example.onlineauctionsystem.network.AuctionMessage;
import com.example.onlineauctionsystem.network.AuctionClient;
import com.example.onlineauctionsystem.network.AuctionServer;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Entry point cho toàn bộ ứng dụng.
 *
 * Luồng khởi động:
 *   1. Khởi động AuctionServer trong thread nền (background)
 *   2. Chờ Server sẵn sàng (tối đa 5 giây)
 *   3. Mở cửa sổ JavaFX với màn hình Đăng nhập
 *
 * Khi bạn nhấn "Launch" / chạy Launcher.java, tất cả sẽ tự động:
 *   Server  → chạy nền (port 5000)
 *   Client  → ứng dụng JavaFX, mỗi thao tác gọi qua AuctionClient
 */
public class HelloApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        // Khởi động Server ngầm TRƯỚC khi hiển thị UI
        startEmbeddedServer();

        // Chờ server sẵn sàng (thử kết nối tối đa 10 lần, mỗi 500ms)
        waitForServer();

        // Mở màn hình Đăng nhập
        FXMLLoader fxmlLoader = new FXMLLoader(
                HelloApplication.class.getResource("Dang_Nhap_BTL.fxml")
        );
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Hệ thống đấu giá trực tuyến");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Khởi động AuctionServer trong một thread daemon riêng.
     * Daemon thread sẽ tự tắt khi ứng dụng JavaFX đóng.
     */
    private void startEmbeddedServer() {
        Thread serverThread = new Thread(() -> {
            System.out.println("[App] Khởi động AuctionServer tích hợp...");
            AuctionServer.main(new String[]{});
        }, "auction-server-main");
        serverThread.setDaemon(true);
        serverThread.start();
    }

    /**
     * Chờ server thực sự lắng nghe trước khi mở UI.
     * Thử ping server bằng GET_ALL_PRODUCTS tối đa 10 lần.
     */
    private void waitForServer() {
        int maxRetry = 10;
        for (int i = 0; i < maxRetry; i++) {
            AuctionMessage res = AuctionClient.send(
                    new AuctionMessage(AuctionMessage.Action.GET_ALL_PRODUCTS, null)
            );
            if (res.getAction() == AuctionMessage.Action.SUCCESS) {
                System.out.println("[App] Server sẵn sàng sau " + (i + 1) + " lần thử.");
                return;
            }
            try {
                System.out.println("[App] Chờ server... lần " + (i + 1));
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        System.out.println("[App] Cảnh báo: Server chưa phản hồi. Ứng dụng vẫn tiếp tục.");
    }

    public static void main(String[] args) {
        launch();
    }
}