package ec.edu.uees.akinatorproyecto;

import ec.edu.uees.opciones.ScreenManager;
import ec.edu.uees.opciones.AnimationManager;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;

public class CreditosController implements Initializable {

    private static final String FABIAN_GITHUB = "https://github.com/Fabianrodas";
    private static final String FABIAN_LINKEDIN = "https://www.linkedin.com/in/fabianrodas";
    private static final String PAULA_GITHUB = "https://github.com/paulabenalcazart/";
    private static final String PAULA_LINKEDIN = "https://www.linkedin.com/in/paula-benalc%C3%ADzar-944409329/";
    private static final String REPOSITORY = "https://github.com/Fabianrodas/AkinatorProyecto";

    @FXML private AnchorPane mainAnchor;
    @FXML private Button minimizar, cerrar;
    @FXML private Label creditTitle;
    @FXML private ImageView imagenFabian, imagenPaula;
    @FXML private Label inicialesFabian, inicialesPaula;

    private double xOffset;
    private double yOffset;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        if (!ScreenManager.getInstance().isScreenLocked()) {
            mainAnchor.setOnMousePressed(event -> {
                xOffset = event.getSceneX();
                yOffset = event.getSceneY();
            });
            mainAnchor.setOnMouseDragged(event -> {
                Stage stage = (Stage) cerrar.getScene().getWindow();
                if (!stage.isFullScreen()) {
                    stage.setX(event.getScreenX() - xOffset);
                    stage.setY(event.getScreenY() - yOffset);
                }
            });
        }

        loadAvatar("https://github.com/Fabianrodas.png?size=256", imagenFabian, inicialesFabian);
        loadAvatar("https://github.com/paulabenalcazart.png?size=256", imagenPaula, inicialesPaula);

        if (AnimationManager.getInstance().areAnimationsEnabled()) {
            creditTitle.sceneProperty().addListener((observable, oldScene, newScene) -> {
                if (newScene != null) {
                    iniciarBrillo();
                }
            });
        }
    }

    private void iniciarBrillo() {
        Platform.runLater(() -> {
            double labelWidth = creditTitle.getWidth();
            double labelHeight = creditTitle.getHeight();
            if (labelWidth == 0 || labelHeight == 0) {
                return;
            }

            Label tituloBrillo = new Label(creditTitle.getText());
            tituloBrillo.setFont(creditTitle.getFont());
            tituloBrillo.setStyle("-fx-text-fill: white;");
            tituloBrillo.setAlignment(creditTitle.getAlignment());
            tituloBrillo.setPrefSize(labelWidth, labelHeight);
            tituloBrillo.setLayoutX(creditTitle.getLayoutX());
            tituloBrillo.setLayoutY(creditTitle.getLayoutY());
            tituloBrillo.setMouseTransparent(true);

            Rectangle brillo = new Rectangle(labelWidth + 50, labelHeight);
            brillo.setFill(new LinearGradient(
                0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.TRANSPARENT),
                new Stop(0.5, Color.WHITE),
                new Stop(1, Color.TRANSPARENT)
            ));
            brillo.setOpacity(0.85);
            tituloBrillo.setClip(brillo);

            TranslateTransition transicion = new TranslateTransition(Duration.seconds(2), brillo);
            transicion.setFromX(-labelWidth - 50);
            transicion.setToX(labelWidth + 50);
            transicion.setCycleCount(TranslateTransition.INDEFINITE);
            transicion.setAutoReverse(false);
            transicion.play();

            Parent parentContainer = creditTitle.getParent();
            ((Pane) parentContainer).getChildren().add(tituloBrillo);
        });
    }

    private void loadAvatar(String url, ImageView imageView, Label fallback) {
        imageView.setClip(new Circle(43, 43, 41));
        imageView.setVisible(false);

        Image image = new Image(url, 86, 86, true, true, true);
        imageView.setImage(image);
        image.progressProperty().addListener((observable, oldValue, newValue) ->
            updateAvatarVisibility(image, imageView, fallback));
        image.errorProperty().addListener((observable, oldValue, hasError) ->
            updateAvatarVisibility(image, imageView, fallback));
        updateAvatarVisibility(image, imageView, fallback);
    }

    private void updateAvatarVisibility(Image image, ImageView imageView, Label fallback) {
        if (image.isError()) {
            imageView.setVisible(false);
            fallback.setVisible(true);
        } else if (image.getProgress() >= 1.0) {
            imageView.setVisible(true);
            fallback.setVisible(false);
        }
    }

    @FXML
    private void volverAlMenu() throws IOException {
        App.setRoot("menu");
    }

    @FXML private void abrirGithubFabian() { App.openExternalUrl(FABIAN_GITHUB); }
    @FXML private void abrirLinkedinFabian() { App.openExternalUrl(FABIAN_LINKEDIN); }
    @FXML private void abrirGithubPaula() { App.openExternalUrl(PAULA_GITHUB); }
    @FXML private void abrirLinkedinPaula() { App.openExternalUrl(PAULA_LINKEDIN); }
    @FXML private void abrirRepositorio() { App.openExternalUrl(REPOSITORY); }

    @FXML
    private void cerrarStage() {
        ((Stage) cerrar.getScene().getWindow()).close();
    }

    @FXML
    private void minimizarStage() {
        ((Stage) minimizar.getScene().getWindow()).setIconified(true);
    }
}
