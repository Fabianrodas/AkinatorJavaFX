package ec.edu.uees.akinatorproyecto;

import ec.edu.uees.opciones.AnimationManager;
import ec.edu.uees.opciones.MusicPlayer;
import ec.edu.uees.opciones.SFXPlayer;
import ec.edu.uees.opciones.ScreenManager;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;
import javafx.util.Duration;

public class OptionsController implements Initializable{
    @FXML private AnchorPane mainAnchor;
    private Stage stage;
    private double xOffset = 0;
    private double yOffset = 0;
    @FXML private Button minimizar, cerrar;
    @FXML private Label labelOptions;
    @FXML private ImageView imagenFondo;
    @FXML private MediaView fondoPergamino;
    @FXML private HBox hboxMusica, hboxSFX, hboxBrillo, hboxAnimaciones, hboxBotones;
    @FXML private Slider sliderMusica, sliderSFX;
    @FXML private ToggleButton toggleAnimaciones, togglePantalla;
    private MediaPlayer mediaPlayer;
    private Timeline optionsTimeline;

    @FXML
    private void switchToMenu() throws IOException {
        disposeMediaPlayer();
        stopOptionsTimeline();
        App.setRootSinAnimar("menu");
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        if(!ScreenManager.getInstance().isScreenLocked()) {
            mainAnchor.setOnMousePressed(e -> {
               xOffset = e.getSceneX();
               yOffset = e.getSceneY();
           });

           mainAnchor.setOnMouseDragged(e -> {
               stage = (Stage) cerrar.getScene().getWindow();
               if(!stage.isFullScreen()){
                   stage.setX(e.getScreenX() - xOffset);
                   stage.setY(e.getScreenY() - yOffset);
               }
           });   
        }
               
        if(AnimationManager.getInstance().areAnimationsEnabled()) {
            startAnimatedBackground(true);
        } else {
            showStaticBackground();
        }
        
        // OPCIÓN MUSICA
        sliderMusica.setValue(MusicPlayer.getMediaPlayer().getVolume());
        sliderMusica.valueProperty().addListener((observable, oldValue, newValue) -> {
            MusicPlayer.setVolume(newValue.doubleValue());
        });
        
        // OPCIÓN SFX
        sliderSFX.setValue(SFXPlayer.getVolume());
        sliderSFX.valueProperty().addListener((observable, oldValue, newValue) -> {
            SFXPlayer.setVolume(newValue.doubleValue());
        });
        
        // OPCIÓN LOCK SCREEN
        togglePantalla.setSelected(ScreenManager.getInstance().isScreenLocked());
        togglePantalla.selectedProperty().addListener((observable, oldValue, newValue) -> {
            ScreenManager.getInstance().setScreenLock(newValue);

            if (newValue) {
                // Bloquear la pantalla inmediatamente
                mainAnchor.setOnMousePressed(null);
                mainAnchor.setOnMouseDragged(null);
            } else {
                // Restaurar el movimiento de la pantalla
                mainAnchor.setOnMousePressed(e -> {
                    xOffset = e.getSceneX();
                    yOffset = e.getSceneY();
                });

                mainAnchor.setOnMouseDragged(e -> {
                    stage = (Stage) cerrar.getScene().getWindow();
                    if (!stage.isFullScreen()) {
                        stage.setX(e.getScreenX() - xOffset);
                        stage.setY(e.getScreenY() - yOffset);
                    }
                });
            }
        });
        
        
        //OPCIÓN ANIMACIONES
        toggleAnimaciones.setSelected(AnimationManager.getInstance().areAnimationsEnabled());
        toggleAnimaciones.selectedProperty().addListener((observable, oldValue, newValue) -> {
            AnimationManager.getInstance().setAnimationsEnabled(newValue);
            if(newValue) {
                startAnimatedBackground(false);
            } else {
                showStaticBackground();
            }
        });
        
    }
    
    private void animarNodo(Node nodo) {
        nodo.setVisible(true);
        nodo.setOpacity(0);
        FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.75), nodo);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();
    }

    private void startAnimatedBackground(boolean animateControls) {
        stopOptionsTimeline();
        disposeMediaPlayer();
        imagenFondo.setVisible(true);
        fondoPergamino.setVisible(false);

        if(animateControls) {
            hideOptionsControls();
            optionsTimeline = new Timeline(
                new KeyFrame(Duration.seconds(0.5), e -> animarNodo(labelOptions)),
                new KeyFrame(Duration.seconds(0.65), e -> animarNodo(hboxMusica)),
                new KeyFrame(Duration.seconds(0.7), e -> animarNodo(hboxSFX)),
                new KeyFrame(Duration.seconds(0.85), e -> animarNodo(hboxBrillo)),
                new KeyFrame(Duration.seconds(1.0), e -> animarNodo(hboxAnimaciones)),
                new KeyFrame(Duration.seconds(1.15), e -> animarNodo(hboxBotones))
            );
        } else {
            showOptionsControls();
        }

        try {
            URL videoResource = getClass().getResource("/imagenes/pergaminoVideo.mp4");
            if(videoResource == null) {
                showStaticBackground();
                return;
            }

            Media media = new Media(videoResource.toExternalForm());
            MediaPlayer player = new MediaPlayer(media);
            mediaPlayer = player;
            fondoPergamino.setMediaPlayer(player);
            media.setOnError(() -> {
                if(mediaPlayer == player) {
                    showStaticBackground();
                }
            });
            player.setOnError(() -> {
                if(mediaPlayer == player) {
                    showStaticBackground();
                }
            });
            fondoPergamino.setOnError(event -> {
                if(mediaPlayer == player) {
                    showStaticBackground();
                }
            });
            player.setOnReady(() -> {
                if(mediaPlayer != player || !AnimationManager.getInstance().areAnimationsEnabled()) {
                    return;
                }
                imagenFondo.setVisible(false);
                fondoPergamino.setVisible(true);
                player.play();
                if(animateControls && optionsTimeline != null) {
                    optionsTimeline.playFromStart();
                }
            });

            if(media.getError() != null || player.getError() != null) {
                showStaticBackground();
            }
        } catch(Exception e) {
            showStaticBackground();
        }
    }

    private void showStaticBackground() {
        stopOptionsTimeline();
        disposeMediaPlayer();
        imagenFondo.setVisible(true);
        fondoPergamino.setVisible(false);
        showOptionsControls();
    }

    private void hideOptionsControls() {
        labelOptions.setOpacity(1);
        hboxMusica.setOpacity(1);
        hboxSFX.setOpacity(1);
        hboxBrillo.setOpacity(1);
        hboxAnimaciones.setOpacity(1);
        hboxBotones.setOpacity(1);
        labelOptions.setVisible(false);
        hboxMusica.setVisible(false);
        hboxSFX.setVisible(false);
        hboxBrillo.setVisible(false);
        hboxAnimaciones.setVisible(false);
        hboxBotones.setVisible(false);
    }

    private void showOptionsControls() {
        labelOptions.setVisible(true);
        hboxMusica.setVisible(true);
        hboxSFX.setVisible(true);
        hboxBrillo.setVisible(true);
        hboxAnimaciones.setVisible(true);
        hboxBotones.setVisible(true);
        labelOptions.setOpacity(1);
        hboxMusica.setOpacity(1);
        hboxSFX.setOpacity(1);
        hboxBrillo.setOpacity(1);
        hboxAnimaciones.setOpacity(1);
        hboxBotones.setOpacity(1);
    }

    private void stopOptionsTimeline() {
        if(optionsTimeline != null) {
            optionsTimeline.stop();
            optionsTimeline = null;
        }
    }

    private void disposeMediaPlayer() {
        fondoPergamino.setMediaPlayer(null);
        fondoPergamino.setOnError(null);
        fondoPergamino.setVisible(false);
        if(mediaPlayer != null) {
            mediaPlayer.setOnReady(null);
            mediaPlayer.setOnError(null);
            mediaPlayer.stop();
            mediaPlayer.dispose();
            mediaPlayer = null;
        }
    }
    
     @FXML
    private void cerrarStage() {
        disposeMediaPlayer();
        stopOptionsTimeline();
        stage = (Stage) cerrar.getScene().getWindow();
        stage.close();
    }
    @FXML
    private void minimizarStage() {
        stage = (Stage) minimizar.getScene().getWindow();
        stage.setIconified(true);
    }
    
    @FXML
    private void resetearOpciones(){
        MusicPlayer.setVolume(1);
        SFXPlayer.setVolume(1);
        ScreenManager.getInstance().setScreenLock(false);
        AnimationManager.getInstance().setAnimationsEnabled(true);
        
        sliderMusica.setValue(1);
        sliderSFX.setValue(1);
        togglePantalla.setSelected(false);
        toggleAnimaciones.setSelected(true);
    }
}
