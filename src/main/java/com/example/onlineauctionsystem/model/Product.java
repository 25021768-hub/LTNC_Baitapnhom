package com.example.onlineauctionsystem.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class Product implements Serializable {
    private String id;
    private String name;
    private double initialPrice;
    private double currentPrice;
    private double bidIncrement; // 🌟 MỚI: Bước giá (Số tiền chênh lệch tối thiểu)
    private String sellerName;
    private String imagePath;    // 🌟 MỚI: Đường dẫn ảnh sản phẩm (ví dụ: "images/laptop.png")
    private String highestBidder;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;
    private long durationHours;


    public Product(String id, String name, double initialPrice, double bidIncrement, long durationHours, String sellerName, String imagePath) {
        this.id = id;
        this.name = name;
        this.initialPrice = initialPrice;
        this.currentPrice = initialPrice;
        this.bidIncrement = bidIncrement;
        this.durationHours = durationHours;
        this.sellerName = sellerName;
        this.imagePath = imagePath;
        this.highestBidder = "None";
        this.status = "OPEN";
    }

    // --- LOGIC ĐẤU GIÁ NÂNG CẤP VỚI BƯỚC GIÁ ---
    public synchronized boolean placeBid(double newAmount, String bidderName) {
        LocalDateTime now = LocalDateTime.now();
        if (!status.equals("RUNNING")) return false;
        if (now.isBefore(startTime) || now.isAfter(endTime)) return false;


        double minimumRequiredPrice = currentPrice;
        if (!highestBidder.equals("None")) {
            minimumRequiredPrice = currentPrice + bidIncrement;
        }

        // Kiểm tra xem số tiền người dùng nhập vào có đạt mức tối thiểu không
        if (newAmount >= minimumRequiredPrice) {
            this.currentPrice = newAmount;
            this.highestBidder = bidderName;
            this.bidIncrement++;
            return true;
        }
        return false;
    }

    public void setEndTime(){

        this.endTime = startTime.plusHours(durationHours);
    }

    public void updateStatus() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime end = getEndTime();

        if (startTime == null || end == null) {
            if (!"RUNNING".equals(status) && !"FINISHED".equals(status)
                     && !"PAID".equals(status) && !"CANCELED".equals(status)) {
                status = "PENDING";
            }
            return;
        }

        if (now.isBefore(startTime)) {
            status = "PENDING";
        } else if (now.isAfter(startTime) && now.isBefore(end)) {
            status = "RUNNING";
        } else if (now.isAfter(end)) {
            // XỬ LÝ KHI VỪA HẾT GIỜ ĐẤU GIÁ
            if ("RUNNING".equals(status)) {
                if (highestBidder != null && !highestBidder.trim().isEmpty()) {
                    // TRƯỜNG HỢP 1: Có người đặt giá -> Chuyển sang ĐỢI XÁC NHẬN
                    status = "FINISHED";
                } else {
                    // TRƯỜNG HỢP 2: Không ai đặt giá -> Kết thúc thất bại luôn
                    status = "Cancel";
                }
            }
        }
    }

    public String getRemainingTime() {
        if (startTime == null || endTime == null) return "--:--:--";

        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(endTime)) return "Hết giờ";

        long totalSeconds = ChronoUnit.SECONDS.between(now, endTime);
        long h = totalSeconds / 3600;
        long m = (totalSeconds % 3600) / 60;
        long s = totalSeconds % 60;
        return String.format("%02d:%02d:%02d", h, m, s);
    }

    // --- CÁC HÀM GETTER / SETTER
    // 1. KHU VỰC GETTER
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getInitialPrice() {
        return initialPrice;
    }

    public double getCurrentPrice() {
        return currentPrice;
    }

    public double getBidIncrement() {
        return bidIncrement;
    } // 🌟 Mới

    public String getSellerName() {
        return sellerName;
    }

    public String getImagePath() {
        return imagePath;
    }       // 🌟 Mới

    public String getHighestBidder() {
        return highestBidder;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public String getStatus() {
        return status;
    }

    public long getDurationHours() {return  durationHours;}


    // 2. KHU VỰC SETTER (Chỉ tạo cho các thuộc tính ĐƯỢC PHÉP thay đổi)

    // Cho phép sửa thông tin hiển thị sản phẩm khi cần thiết
    public void setName(String name) {
        this.name = name;
    }

    // Giá hiện tại và Người giữ giá cao nhất sẽ thay đổi liên tục mỗi khi hàm placeBid() chạy thành công
    public void setCurrentPrice(double currentPrice) {
        this.currentPrice = currentPrice;
    }

    public void setHighestBidder(String highestBidder) {
        this.highestBidder = highestBidder;
    }

    // Trạng thái sẽ tự động thay đổi từ OPEN -> RUNNING -> FINISHED theo thời gian thực
    public void setStatus(String status) {
        this.status = status;
    }

    // Cho phép cập nhật lại bước giá và ảnh sản phẩm (Ví dụ: Khi Seller muốn đổi ảnh khác đẹp hơn)
    public void setBidIncrement(double bidIncrement) {
        this.bidIncrement = bidIncrement;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public void setDurationHours(long durationHours){this.durationHours = durationHours;}

    public void setStartTime(LocalDateTime startTime){this.startTime = startTime;}

    public void setEndTime(LocalDateTime endTime) {this.endTime = endTime;}
}