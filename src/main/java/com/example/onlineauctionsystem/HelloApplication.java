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

public class HelloApplication extends Application {

    @Override
    public void stop() {
        // Tự động đăng xuất khi đóng ứng dụng (giải phóng session trên server)
        try {
            com.example.onlineauctionsystem.model.Account current =
                    com.example.onlineauctionsystem.model.RemoteDataStorage.currentAccount;
            if (current != null) {
                AuctionClient.send(new AuctionMessage(AuctionMessage.Action.LOGOUT, current.getUsername()));
                System.out.println("[App] Đã gửi LOGOUT cho: " + current.getUsername());
            }
        } catch (Exception e) {
            System.err.println("[App] Lỗi khi gửi LOGOUT: " + e.getMessage());
        }
    }

    @Override
    public void start(Stage stage) {
        showModeDialog(stage);
    }

    // ── Hộp thoại chọn chế độ HOST / CLIENT ──────────────────────
    private void showModeDialog(Stage mainStage) {
        Stage dialog = new Stage();
        dialog.setTitle("Chọn chế độ khởi động");
        dialog.setResizable(false);

        Label title = new Label("Hệ thống Đấu giá Trực tuyến");
        title.setFont(Font.font("System", FontWeight.BOLD, 18));
        title.setStyle("-fx-text-fill: #2c3e50;");

        Label subtitle = new Label("Chọn vai trò của máy này:");
        subtitle.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 13px;");

        Button btnHost = new Button("  Chạy Server + Client  (Host)");
        btnHost.setPrefWidth(320);
        btnHost.setPrefHeight(55);
        btnHost.setStyle("-fx-background-color:#2980b9;-fx-text-fill:white;"
                + "-fx-font-size:14px;-fx-background-radius:8;-fx-cursor:hand;");

        Label hostDesc = new Label("Máy này làm server. Các máy khác kết nối vào IP của máy này.");
        hostDesc.setStyle("-fx-text-fill:#95a5a6;-fx-font-size:11px;");
        hostDesc.setWrapText(true);
        hostDesc.setMaxWidth(320);

        Button btnClient = new Button("  Chỉ chạy Client  (Kết nối đến Host)");
        btnClient.setPrefWidth(320);
        btnClient.setPrefHeight(55);
        btnClient.setStyle("-fx-background-color:#27ae60;-fx-text-fill:white;"
                + "-fx-font-size:14px;-fx-background-radius:8;-fx-cursor:hand;");

        Label clientDesc = new Label("Nhập IP của máy Host để kết nối vào hệ thống chung.");
        clientDesc.setStyle("-fx-text-fill:#95a5a6;-fx-font-size:11px;");
        clientDesc.setWrapText(true);
        clientDesc.setMaxWidth(320);

        Label ipLabel = new Label("IP máy này: " + getLocalIP());
        ipLabel.setStyle("-fx-text-fill:#e67e22;-fx-font-size:12px;-fx-font-weight:bold;");

        VBox layout = new VBox(12, title, subtitle,
                new Separator(), btnHost, hostDesc,
                new Separator(), btnClient, clientDesc,
                new Separator(), ipLabel);
        layout.setPadding(new Insets(25));
        layout.setAlignment(Pos.CENTER_LEFT);
        layout.setStyle("-fx-background-color:#f8f9fa;");
        layout.setPrefWidth(370);

        btnHost.setOnAction(e -> {
            dialog.close();
            System.setProperty("auction.host", "localhost");
            startEmbeddedServer();

            // FIX : waitForServer() gọi TCP blocking — không được chạy trên JavaFX UI Thread
            // vì sẽ làm đơ toàn bộ giao diện trong lúc chờ server khởi động (có thể 1-5 giây).
            // Giải pháp: hiện màn hình loading, chờ trên background thread, rồi mở UI chính
            // bằng Platform.runLater() khi server đã sẵn sàng.
            Label lblWaiting = new Label("Đang khởi động server, vui lòng chờ...");
            lblWaiting.setStyle("-fx-font-size:13px; -fx-text-fill:#e67e22;");
            javafx.scene.control.ProgressIndicator spinner = new javafx.scene.control.ProgressIndicator();
            spinner.setPrefSize(30, 30);
            VBox waitBox = new VBox(10, spinner, lblWaiting);
            waitBox.setAlignment(Pos.CENTER);
            waitBox.setPadding(new Insets(30));

            Stage waitStage = new Stage();
            waitStage.setTitle("Đang khởi động...");
            waitStage.setResizable(false);
            waitStage.setScene(new Scene(waitBox, 300, 120));
            waitStage.show();

            Thread t = new Thread(() -> {
                waitForServer("localhost");
                javafx.application.Platform.runLater(() -> {
                    waitStage.close();
                    launchMainUI(mainStage);
                });
            }, "server-wait-thread");
            t.setDaemon(true);
            t.start();
        });

        btnClient.setOnAction(e -> {
            dialog.close();
            showClientIPDialog(mainStage);
        });

        dialog.setScene(new Scene(layout));
        dialog.show();
    }

    // ── Hộp thoại nhập IP (chế độ CLIENT) ────────────────────────
    private void showClientIPDialog(Stage mainStage) {
        Stage dialog = new Stage();
        dialog.setTitle("Kết nối đến Host");
        dialog.setResizable(false);

        Label label = new Label("Nhập địa chỉ IP của máy Host:");
        label.setStyle("-fx-font-size:14px;-fx-font-weight:bold;");

        TextField txtIP = new TextField("192.168.1.");
        txtIP.setPrefWidth(260);
        txtIP.setStyle("-fx-font-size:14px;-fx-padding:8;");

        Label lblStatus = new Label("");
        lblStatus.setStyle("-fx-text-fill:red;-fx-font-size:12px;");
        lblStatus.setWrapText(true);

        Button btnConnect = new Button("Kết nối");
        btnConnect.setPrefWidth(120);
        btnConnect.setStyle("-fx-background-color:#27ae60;-fx-text-fill:white;"
                + "-fx-font-size:13px;-fx-background-radius:6;-fx-cursor:hand;");

        Button btnBack = new Button("Quay lại");
        btnBack.setStyle("-fx-font-size:13px;-fx-cursor:hand;");

        HBox buttons = new HBox(10, btnConnect, btnBack);
        buttons.setAlignment(Pos.CENTER);

        Label hint = new Label(" Xem IP của Host ở màn hình chọn chế độ trên máy Host");
        hint.setStyle("-fx-text-fill:#7f8c8d;-fx-font-size:11px;");
        hint.setWrapText(true);

        VBox layout = new VBox(12, label, txtIP, hint, lblStatus, buttons);
        layout.setPadding(new Insets(25));
        layout.setAlignment(Pos.CENTER_LEFT);
        layout.setPrefWidth(330);

        btnConnect.setOnAction(e -> {
            String ip = txtIP.getText().trim();
            if (ip.isEmpty()) { lblStatus.setText("Vui lòng nhập IP!"); return; }
            lblStatus.setStyle("-fx-text-fill:#e67e22;-fx-font-size:12px;");
            lblStatus.setText("Đang kết nối đến " + ip + ":5000 ...");
            btnConnect.setDisable(true);

            Thread t = new Thread(() -> {
                System.setProperty("auction.host", ip);
                boolean ok = pingServer(ip);
                javafx.application.Platform.runLater(() -> {
                    if (ok) {
                        dialog.close();
                        launchMainUI(mainStage);
                    } else {
                        lblStatus.setStyle("-fx-text-fill:red;-fx-font-size:12px;");
                        lblStatus.setText(" Không kết nối được đến " + ip + ":5000\n"
                                + "Kiểm tra lại IP và đảm bảo Host đang chạy.");
                        btnConnect.setDisable(false);
                    }
                });
            });
            t.setDaemon(true);
            t.start();
        });

        txtIP.setOnKeyPressed(ev -> {
            if (ev.getCode() == javafx.scene.input.KeyCode.ENTER) btnConnect.fire();
        });

        btnBack.setOnAction(e -> { dialog.close(); showModeDialog(mainStage); });

        dialog.setScene(new Scene(layout));
        dialog.show();
    }

    // ── Khởi động Server (chỉ dùng khi chế độ HOST) ──────────────
    private void startEmbeddedServer() {
        Thread t = new Thread(() -> {
            System.out.println("[App] Khởi động AuctionServer...");
            AuctionServer.main(new String[]{});
        }, "auction-server-main");
        t.setDaemon(true);
        t.start();
    }

    private void waitForServer(String host) {
        for (int i = 0; i < 10; i++) {
            if (pingServer(host)) {
                System.out.println("[App] Server sẵn sàng.");
                return;
            }
            try { Thread.sleep(500); } catch (InterruptedException ignored) {}
        }
        System.out.println("[App] Cảnh báo: Server chưa phản hồi.");
    }

    private boolean pingServer(String host) {
        try {
            System.setProperty("auction.host", host);
            AuctionMessage res = AuctionClient.send(
                    new AuctionMessage(AuctionMessage.Action.GET_ALL_PRODUCTS, null));
            return res.getAction() == AuctionMessage.Action.SUCCESS;
        } catch (Exception e) {
            return false;
        }
    }

    // ── Mở màn hình chính ─────────────────────────────────────────
    private void launchMainUI(Stage stage) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    HelloApplication.class.getResource("Dang_Nhap_BTL.fxml"));
            stage.setTitle("Hệ thống đấu giá trực tuyến");
            stage.setScene(new Scene(loader.load()));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ── Lấy IP LAN của máy ────────────────────────────────────────
    private String getLocalIP() {
        try {
            Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
            while (nets.hasMoreElements()) {
                NetworkInterface ni = nets.nextElement();
                if (ni.isLoopback() || !ni.isUp()) continue;
                Enumeration<InetAddress> addrs = ni.getInetAddresses();
                while (addrs.hasMoreElements()) {
                    String ip = addrs.nextElement().getHostAddress();
                    if (ip.contains(".") && !ip.startsWith("169.254")) return ip;
                }
            }
        } catch (Exception ignored) {}
        return "Không xác định";
    }

    public static void main(String[] args) { launch(); }
}