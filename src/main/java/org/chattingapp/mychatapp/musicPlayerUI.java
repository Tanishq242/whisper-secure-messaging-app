package org.chattingapp.mychatapp;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import java.io.File;

public class musicPlayerUI {
    //    Music Player Variable
    private MediaPlayer mediaPlayer;
    private Label titleLabel;
    private Label timeLabel;
    private Slider timeSlider;
    private Slider volumeSlider;
    private Button playPauseButton;
    public boolean isPlaying = false;

    private static final int NUM_BARS = 5;
    private static final double BAR_WIDTH = 20;
    private static final double BAR_GAP = 15;
    private static final double MAX_HEIGHT = 200;
    private Rectangle[] bars = new Rectangle[NUM_BARS];

    public void togglePlayPause() {
        if (mediaPlayer != null) {
            if (isPlaying) {
                mediaPlayer.pause();
                playPauseButton.setText("▶");
                System.out.println("Song is paused");
                isPlaying = !isPlaying;
            } else {
                mediaPlayer.setAudioSpectrumNumBands(NUM_BARS);
                mediaPlayer.setAudioSpectrumInterval(0.03);
                mediaPlayer.setAudioSpectrumThreshold(-60);

                mediaPlayer.setAudioSpectrumListener((ts, dur, magnitudes, phases) -> {
                    Platform.runLater(() -> updateBars(magnitudes));
                });
                mediaPlayer.play();
                playPauseButton.setText("⏸");
                isPlaying = !isPlaying;
                System.out.println("Song is playing");
            }
        }
    }

    private String formatTime(Duration duration) {
        if (duration == null || duration.isUnknown()) {
            return "0:00";
        }

        int seconds = (int) duration.toSeconds();
        int minutes = seconds / 60;
        seconds = seconds % 60;

        return String.format("%d:%02d", minutes, seconds);
    }

    private void updateTimeLabel() {
        if (mediaPlayer != null) {
            Duration currentTime = mediaPlayer.getCurrentTime();
            Duration totalDuration = mediaPlayer.getTotalDuration();

            String current = formatTime(currentTime);
            String total = formatTime(totalDuration);

            timeLabel.setText(current + " / " + total);
        }
    }

    private void applySliderStyle(Slider slider, String fillColor) {
        Region track = (Region) slider.lookup(".track");
        Region thumb = (Region) slider.lookup(".thumb");

        if (thumb != null) {
            thumb.setStyle(
                    "-fx-background-color: " + fillColor + ";" +
                            "-fx-background-radius: 8;" +
                            "-fx-pref-width: 16;" +
                            "-fx-pref-height: 16;"
            );
        }

        updateSliderFill(slider, fillColor);
    }

    private void updateSliderFill(Slider slider, String fillColor) {
        Region track = (Region) slider.lookup(".track");
        if (track != null) {
            double percentage = (slider.getValue() - slider.getMin()) /
                    (slider.getMax() - slider.getMin()) * 100;

            track.setStyle(String.format(
                    "-fx-background-color: linear-gradient(to right, " +
                            "%s 0%%, %s %.2f%%, " +
                            "#cccccc %.2f%%, #cccccc 100%%);" +
                            "-fx-background-radius: 2;" +
                            "-fx-pref-height: 4;",
                    fillColor, fillColor, percentage, percentage
            ));
        }
    }

    public void loadMedia(File file) {
        // Dispose old player if exists
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
        }

        try {
            Media media = new Media(file.toURI().toString());
            mediaPlayer = new MediaPlayer(media);

            mediaPlayer.setOnReady(() -> {
                Duration totalDuration = media.getDuration();
                timeSlider.setMax(totalDuration.toSeconds());
                titleLabel.setText(file.getName());
                updateTimeLabel();
            });

            mediaPlayer.currentTimeProperty().addListener((obs, oldTime, newTime) -> {
                if (!timeSlider.isValueChanging()) {
                    timeSlider.setValue(newTime.toSeconds());
                }
                updateTimeLabel();
            });

            mediaPlayer.setOnEndOfMedia(() -> {
                isPlaying = false;
                playPauseButton.setText("▶");
                mediaPlayer.seek(Duration.ZERO);
            });

            mediaPlayer.setVolume(volumeSlider.getValue());

        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Cannot load media file");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    private void updateBars(float[] mag) {
        double beat = averageMagnitude(mag);   // detect beat-like movement

        for (int i = 0; i < NUM_BARS; i++) {
            double value = (mag[i] + 60) / 60.0;  // normalize 0–1
            if (value < 0) value = 0;

            // boost bar height on beat
            double height = (value * MAX_HEIGHT) + (beat * 40);

            if (height < 5) height = 5;
            if (height > MAX_HEIGHT) height = MAX_HEIGHT;

            bars[i].setHeight(height);
            bars[i].setTranslateY(MAX_HEIGHT - height);
        }
    }

    private double averageMagnitude(float[] m) {
        double sum = 0;
        for (float v : m) sum += (v + 60) / 60.0;
        return sum / m.length;
    }

    public VBox musicPlayer(String songName, Boolean isDark) {
        VBox vboxRight = new VBox(100);
        vboxRight.setPadding(new Insets(0, 0, 150, 0));
        vboxRight.setId("music-player");
        vboxRight.setStyle("-fx-background-color: white");
        vboxRight.getStyleClass().add("musicBox");

        titleLabel = new Label(songName);
        if (songName.length() > 55) titleLabel.setText("...");
        System.out.println("size of label text: "+songName.length());
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

//        Spectrum Bar
        Pane pane = new Pane();
        pane.setMaxWidth(165);
        pane.setPrefHeight(210);
//        pane.setBackground(Background.fill(Color.BLACK));

        for (int i = 0; i < NUM_BARS; i++) {
            Rectangle r = new Rectangle(BAR_WIDTH, 5, Color.web("#5d82fe"));
            r.setTranslateX(i * (BAR_WIDTH + BAR_GAP));
            r.setTranslateY(MAX_HEIGHT - 5);
            bars[i] = r;
            pane.getChildren().add(r);
        }

        // Time slider
        timeSlider = new Slider();
        timeSlider.setMin(0);
        timeSlider.setValue(0);
        timeSlider.setPrefWidth(400);
        timeSlider.setStyle(
                "-fx-control-inner-background: derive(-fx-base, -20%);" +
                        "-fx-track-color: linear-gradient(to right, #4CAF50 0%, #4CAF50 50%, derive(-fx-base, -20%) 50%, derive(-fx-base, -20%) 100%);"
        );

        // Time label
        timeLabel = new Label("0:00 / 0:00");

        // Control buttons
        playPauseButton = new Button("▶");
        playPauseButton.setPrefSize(50, 50);
        playPauseButton.setStyle("-fx-font-size: 20px;");

        Button stopButton = new Button("⏹");
        stopButton.setPrefSize(50, 50);
        stopButton.setStyle("-fx-font-size: 20px;");

        // Volume control
        Label volumeLabel = new Label("Volume:");
        volumeSlider = new Slider(0, 1, 0.5);
        volumeSlider.setPrefWidth(150);
        volumeSlider.setShowTickLabels(false);

        // Apply slider styling after scene is shown
        Platform.runLater(() -> {
            applySliderStyle(timeSlider, "#2196F3");
            applySliderStyle(volumeSlider, "#4CAF50");
        });

        // Update slider styles on value change
        timeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            updateSliderFill(timeSlider, "#2196F3");
        });

        volumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (mediaPlayer != null) {
                mediaPlayer.setVolume(newVal.doubleValue());
            }
            updateSliderFill(volumeSlider, "#4CAF50");
        });

//        Play Pause Button Logic
        playPauseButton.setOnAction(e -> togglePlayPause());

//        Stop Button Logic
        stopButton.setOnAction(e -> {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.seek(Duration.ZERO);
                isPlaying = false;
                playPauseButton.setText("▶");
            }
        });

        // Time slider interaction
        timeSlider.setOnMousePressed(e -> {
            if (mediaPlayer != null) {
                mediaPlayer.seek(Duration.seconds(timeSlider.getValue()));
            }
        });

        timeSlider.setOnMouseDragged(e -> {
            if (mediaPlayer != null) {
                mediaPlayer.seek(Duration.seconds(timeSlider.getValue()));
            }
        });

        // Volume slider interaction
        volumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (mediaPlayer != null) {
                mediaPlayer.setVolume(newVal.doubleValue());
            }
        });

        // Layout
        HBox controlBox = new HBox(10, playPauseButton, stopButton);
        controlBox.setAlignment(Pos.CENTER);

        HBox volumeBox = new HBox(10, volumeLabel, volumeSlider);
        volumeBox.setAlignment(Pos.CENTER);

        VBox timeBox = new VBox(5, timeSlider, timeLabel);
        timeBox.setAlignment(Pos.CENTER);

        if (isDark) {
            titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");
            timeLabel.setStyle("-fx-text-fill: white");
            volumeLabel.setStyle("-fx-text-fill: white");
        }

        VBox mainLayout = new VBox(20);
        mainLayout.setPadding(new Insets(20));
        mainLayout.setAlignment(Pos.CENTER);
        mainLayout.getChildren().addAll(pane, titleLabel, timeBox, controlBox, volumeBox);

        vboxRight.getChildren().addAll(mainLayout);

        return vboxRight;
    }

    public void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.dispose();
            isPlaying = !isPlaying;
        }
    }
}
