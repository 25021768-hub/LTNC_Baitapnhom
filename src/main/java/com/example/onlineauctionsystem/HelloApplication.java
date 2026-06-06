package com.example.onlineauctionsystem;

import com.example.onlineauctionsystem.network.AuctionMessage;
import com.example.onlineauctionsystem.network.AuctionClient;
import com.example.onlineauctionsystem.network.AuctionServer;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

/**
 * HelloApplication – khởi động ứng dụng với hộp thoại chọn chế độ.
 *
 * Hai chế độ:
 *   HOST   → Máy này khởi động Server + mở UI (1 người làm host)
 *   CLIENT → Máy này chỉ mở UI, kết nối vào IP của Host (những người còn lại)
 */
public class HelloApplication extends Application {

    @Override
    public void start(Stage stage) {
        showModeDialog(stage);
    }

    // ──────────────────────────────────────────────────────────────
    //  HỘP THOẠI CHỌN CHẾ ĐỘ
    // ──────────────────────────────────────────────────────────────
    private void showModeDialog(Stage mainStage) {
        Stage dialog = new Stage();
        dialog.setTitle("Chọn chế độ khởi động");
        dialog.setResizable(false);

        // Tiêu đề
        Label title = new Label("Hệ thống Đấu giá Trực tuyến");
        title.setFont(Font.font("System", FontWeight.BOLD, 18));
        title.setStyle("-fx-text-fill: #2c3e50;");

        Label subtitle = new Label("Chọn vai trò của máy này trong mạng LAN:");
        subtitle.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 13px;");

        // Nút HOST
        Button btnHost = new Button("🖥  Chạy Server + Client (Host)");
        btnHost.setPrefWidth(320);
        btnHost.setPrefHeight(55);
        btnHost.setStyle(
                "-fx-background-color: #2980b9; -fx-text-fill: white; " +
                        "-fx-font-size: 14px; -fx-background-radius: 8; -fx-cursor: hand;"
        );
        Label hostDesc = new Label("Máy này làm server. Các máy khác kết nối vào IP của máy này.");
        hostDesc.setStyle("-fx-text-fill: #95a5a6; -fx-font-size: 11px;");
        hostDesc.setWrapText(true);
        hostDesc.setMaxWidth(320);

        // Nút CLIENT
        Button btnClient = new Button("💻  Chỉ chạy Client (Kết nối đến Host)");
        btnClient.setPrefWidth(320);
        btnClient.setPrefHeight(55);
        btnClient.setStyle(
                "-fx-background-color: #27ae60; -fx-text-fill: white; " +
                        "-fx-font-size: 14px; -fx-background-radius: 8; -fx-cursor: hand;"
        );
        Label clientDesc = new Label("Nhập IP của máy Host để kết nối vào hệ thống chung.");
        clientDesc.setStyle("-fx-text-fill: #95a5a6; -fx-font-size: 11px;");
        clientDesc.setWrapText(true);
        clientDesc.setMaxWidth(320);

        // IP máy hiện tại
        Label ipLabel = new Label("IP máy này: " + getLocalIP());
        ipLabel.setStyle("-fx-text-fill: #e67e22; -fx-font-size: 12px; -fx-font-weight: bold;");

        VBox layout = new VBox(12,
                title, subtitle,
                new Separator(),
                btnHost, hostDesc,
                new Separator(),
                btnClient, clientDesc,
                new Separator(),
                ipLabel
        );
        layout.setPadding(new Insets(25));
        layout.setAlignment(Pos.CENTER_LEFT);
        layout.setStyle("-fx-background-color: #f8f9fa;");
        layout.setPrefWidth(370);

        // ── Xử lý HOST ──
        btnHost.setOnAction(e -> {
            dialog.close();
            // Server chạy trên localhost
            System.setProperty("auction.host", "localhost");
            startEmbeddedServer();
            waitForServer("localhost");
            launchMainUI(mainStage);
        });

        // ── Xử lý CLIENT ──
        btnClient.setOnAction(e -> {
            dialog.close();
            showClientIPDialog(mainStage);
        });

        dialog.setScene(new Scene(layout));
        dialog.show();
    }

    // ──────────────────────────────────────────────────────────────
    //  HỘP THOẠI NHẬP IP (chế độ CLIENT)
    // ──────────────────────────────────────────────────────────────
    private void showClientIPDialog(Stage mainStage) {
        Stage dialog = new Stage();
        dialog.setTitle("Kết nối đến Host");
        dialog.setResizable(false);

        Label label = new Label("Nhập địa chỉ IP của máy Host:");
        label.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        TextField txtIP = new TextField("192.168.1.");
        txtIP.setPrefWidth(260);
        txtIP.setStyle("-fx-font-size: 14px; -fx-padding: 8;");

        Label lblStatus = new Label("");
        lblStatus.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
        lblStatus.setWrapText(true);

        Button btnConnect = new Button("Kết nối");
        btnConnect.setPrefWidth(120);
        btnConnect.setStyle(
                "-fx-background-color: #27ae60; -fx-text-fill: white; " +
                        "-fx-font-size: 13px; -fx-background-radius: 6; -fx-cursor: hand;"
        );

        Button btnCancel = new Button("Quay lại");
        btnCancel.setStyle("-fx-font-size: 13px; -fx-cursor: hand;");

        HBox buttons = new HBox(10, btnConnect, btnCancel);
        buttons.setAlignment(Pos.CENTER);

        Label hint = new Label("💡 Xem IP của Host ở màn hình chọn chế độ trên máy Host");
        hint.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 11px;");
        hint.setWrapText(true);

        VBox layout = new VBox(12, label, txtIP, hint, lblStatus, buttons);
        layout.setPadding(new Insets(25));
        layout.setAlignment(Pos.CENTER_LEFT);
        layout.setPrefWidth(330);

        btnConnect.setOnAction(e -> {
            String ip = txtIP.getText().trim();
            if (ip.isEmpty()) {
                lblStatus.setText("Vui lòng nhập IP!");
                return;
            }
            lblStatus.setStyle("-fx-text-fill: #e67e22; -fx-font-size: 12px;");
            lblStatus.setText("Đang kết nối đến " + ip + ":5000 ...");
            btnConnect.setDisable(true);

            // Thử kết nối trong thread riêng để không block UI
            Thread t = new Thread(() -> {
                System.setProperty("auction.host", ip);
                boolean ok = waitForServerOnce(ip);
                javafx.application.Platform.runLater(() -> {
                    if (ok) {
                        dialog.close();
                        launchMainUI(mainStage);
                    } else {
                        lblStatus.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
                        lblStatus.setText("❌ Không kết nối được đến " + ip + ":5000\n" +
                                "Hãy kiểm tra lại IP và đảm bảo Host đã chạy.");
                        btnConnect.setDisable(false);
                    }
                });
            });
            t.setDaemon(true);
            t.start();
        });

        // Nhấn Enter cũng kết nối
        txtIP.setOnKeyPressed(ev -> {
            if (ev.getCode() == javafx.scene.input.KeyCode.ENTER) {
                btnConnect.fire();
            }
        });

        btnCancel.setOnAction(e -> {
            dialog.close();
            showModeDialog(mainStage);
        });

        dialog.setScene(new Scene(layout));
        dialog.show();
    }

    // ──────────────────────────────────────────────────────────────
    //  KHỞI ĐỘNG SERVER (chỉ dùng khi chế độ HOST)
    // ──────────────────────────────────────────────────────────────
    private void startEmbeddedServer() {
        Thread serverThread = new Thread(() -> {
            System.out.println("[App] Khởi động AuctionServer...");
            AuctionServer.main(new String[]{});
        }, "auction-server-main");
        serverThread.setDaemon(true);
        serverThread.start();
    }

    private void waitForServer(String host) {
        for (int i = 0; i < 10; i++) {
            if (waitForServerOnce(host)) {
                System.out.println("[App] Server sẵn sàng.");
                return;
            }
            try { Thread.sleep(500); } catch (InterruptedException ignored) {}
        }
        System.out.println("[App] Cảnh báo: Server chưa phản hồi.");
    }

    private boolean waitForServerOnce(String host) {
        try {
            System.setProperty("auction.host", host);
            AuctionMessage res = AuctionClient.send(
                    new AuctionMessage(AuctionMessage.Action.GET_ALL_PRODUCTS, null)
            );
            return res.getAction() == AuctionMessage.Action.SUCCESS;
        } catch (Exception e) {
            return false;
        }
    }

    // ──────────────────────────────────────────────────────────────
    //  MỞ MÀN HÌNH CHÍNH
    // ──────────────────────────────────────────────────────────────
    private void launchMainUI(Stage stage) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    HelloApplication.class.getResource("Dang_Nhap_BTL.fxml")
            );
            Scene scene = new Scene(loader.load());
            stage.setTitle("Hệ thống đấu giá trực tuyến");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ──────────────────────────────────────────────────────────────
    //  LẤY IP LAN CỦA MÁY
    // ──────────────────────────────────────────────────────────────
    private String getLocalIP() {
        try {
            Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
            while (nets.hasMoreElements()) {
                NetworkInterface ni = nets.nextElement();
                if (ni.isLoopback() || !ni.isUp()) continue;
                Enumeration<InetAddress> addrs = ni.getInetAddresses();
                while (addrs.hasMoreElements()) {
                    InetAddress addr = addrs.nextElement();
                    String ip = addr.getHostAddress();
                    // Lấy IPv4, bỏ IPv6
                    if (ip.contains(".") && !ip.startsWith("169.254")) {
                        return ip;
                    }
                }
            }
        } catch (Exception ignored) {}
        return "Không xác định được";
    }

    public static void main(String[] args) {
        launch();
    }
}