package com.example.onlineauctionsystem.controller.seller;

import com.example.onlineauctionsystem.controller.BaseController;
import com.example.onlineauctionsystem.model.DataStorage;
import com.example.onlineauctionsystem.model.Product;
import com.example.onlineauctionsystem.utils.ProductImage;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import java.io.File;
import java.util.UUID;

public class AddProductController extends BaseController {

    @FXML private TextField txtName;
    @FXML private TextField txtInitialPrice;
    @FXML private TextField txtBidIncrement;
    @FXML private TextField txtDuration;
    @FXML private ImageView imgPreview;
    @FXML private Label lblImageName;
    @FXML private Label lblError;

    private String selectedImagePath = "";
    private Runnable onSuccessCallback;

    @Override
    public void initialize() { }

    public void setOnSuccessCallback(Runnable callback) {
        this.onSuccessCallback = callback;
    }

    @FXML
    private void onChooseImage() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Chọn ảnh sản phẩm");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Ảnh", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        File file = chooser.showOpenDialog(txtName.getScene().getWindow());
        if (file != null) loadImageFile(file);
    }

    @FXML
    private void onDragOver(DragEvent event) {
        if (event.getDragboard().hasFiles())
            event.acceptTransferModes(TransferMode.COPY);
        event.consume();
    }

    @FXML
    private void onDragDropped(DragEvent event) {
        Dragboard db = event.getDragboard();
        if (db.hasFiles()) {
            File file = db.getFiles().get(0);
            String name = file.getName().toLowerCase();
            if (name.endsWith(".png") || name.endsWith(".jpg")
                    || name.endsWith(".jpeg") || name.endsWith(".gif")) {
                loadImageFile(file);
            } else {
                lblError.setText("Chỉ hỗ trợ file ảnh png, jpg, jpeg, gif!");
            }
        }
        event.setDropCompleted(true);
        event.consume();
    }

    private void loadImageFile(File file) {
        String savedPath = ProductImage.save(file);
        if (savedPath == null) {
            lblError.setText("Không thể lưu ảnh, thử lại!");
            return;
        }
        selectedImagePath = savedPath;
        lblImageName.setText(file.getName());
        imgPreview.setImage(ProductImage.load(savedPath, 135, 135));
        lblError.setText("");
    }

    @FXML
    private void onSubmit() {
        lblError.setText("");
        String name     = txtName.getText().trim();
        String priceStr = txtInitialPrice.getText().trim().replace(".", "");
        String stepStr  = txtBidIncrement.getText().trim().replace(".", "");
        String durStr   = txtDuration.getText().trim();

        if (name.isEmpty() || priceStr.isEmpty() || stepStr.isEmpty() || durStr.isEmpty()) {
            lblError.setText("Vui lòng điền đầy đủ thông tin!");
            return;
        }

        double initialPrice, bidIncrement;
        long duration;
        try {
            initialPrice = Double.parseDouble(priceStr);
            bidIncrement = Double.parseDouble(stepStr);
            duration     = Long.parseLong(durStr);
        } catch (NumberFormatException e) {
            lblError.setText("Giá và thời gian phải là số!");
            return;
        }

        if (initialPrice <= 0 || bidIncrement <= 0 || duration <= 0) {
            lblError.setText("Giá và thời gian phải lớn hơn 0!");
            return;
        }

        Product p = new Product(
                UUID.randomUUID().toString(), name,
                initialPrice, bidIncrement, duration,
                DataStorage.currentAccount.getUsername(),
                selectedImagePath
        );
        p.setStatus("PENDING");

        if (DataStorage.addProduct(p)) {
            if (onSuccessCallback != null) onSuccessCallback.run();
            ((Stage) txtName.getScene().getWindow()).close();
        } else {
            lblError.setText("Đăng bán thất bại, vui lòng thử lại!");
        }
    }

    @FXML
    private void onCancel() {
        ((Stage) txtName.getScene().getWindow()).close();
    }
}
