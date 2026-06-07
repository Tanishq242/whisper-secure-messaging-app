package org.chattingapp.mychatapp;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

import java.io.File;

public class imageViewerUI {
    private ImageView imageView;
    private static final int IMAGE_BOX_WIDTH = 450;
    private static final int IMAGE_BOX_HEIGHT = 500;
    private double currentRotation = 0;

    public BorderPane imageViewer() {
        BorderPane root = new BorderPane();
        root.setId("image-viewer");

        // Create ImageView with fixed dimensions
        imageView = new ImageView();
        imageView.setFitWidth(IMAGE_BOX_WIDTH);
        imageView.setFitHeight(IMAGE_BOX_HEIGHT);
        imageView.setPreserveRatio(true);  // Maintain aspect ratio
        imageView.setSmooth(true);  // Better image quality

        // Set a default placeholder style
        imageView.setStyle("-fx-border-color: #cccccc; -fx-border-width: 2;");

        // Center the image in the view
        BorderPane imageContainer = new BorderPane();
        imageContainer.setCenter(imageView);
        imageContainer.setPadding(new Insets(10));
        imageContainer.setStyle("-fx-background-color: #f0f0f0;");

        root.setCenter(imageContainer);

        // Create button panel
        HBox buttonPanel = new HBox(10);
        buttonPanel.setAlignment(Pos.CENTER);
        buttonPanel.setPadding(new Insets(10));

        Button openButton = new Button("Open Image");
        Button clearButton = new Button("Clear");
        Button rotateLeftButton = new Button("↺ Rotate Left");
        Button rotateRightButton = new Button("↻ Rotate Right");

        clearButton.setOnAction(e -> {
            imageView.setImage(null);
            currentRotation = 0;
            imageView.setRotate(0);
        });
        rotateLeftButton.setOnAction(e -> rotateImage(-90));
        rotateRightButton.setOnAction(e -> rotateImage(90));

        buttonPanel.getChildren().addAll(rotateLeftButton, rotateRightButton);
        root.setBottom(buttonPanel);

        return root;
    }

    public void openImage(File selectedFile) {
        if (selectedFile != null) {
            try {
                Image image = new Image(selectedFile.toURI().toString());

                // Check if image is landscape (width > height)
                if (image.getWidth() > image.getHeight()) {
                    // Rotate landscape images to portrait (90 degrees)
                    currentRotation = 90;
                    imageView.setRotate(90);
                } else {
                    // Keep portrait images as is
                    currentRotation = 0;
                    imageView.setRotate(0);
                }

                imageView.setImage(image);
            } catch (Exception e) {
                System.err.println("Error loading image: " + e.getMessage());
            }
        }
    }

    private void rotateImage(double angle) {
        if (imageView.getImage() != null) {
            currentRotation += angle;
            imageView.setRotate(currentRotation);
        }
    }
}
