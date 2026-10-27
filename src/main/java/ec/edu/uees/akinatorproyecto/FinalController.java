package ec.edu.uees.akinatorproyecto;

import ec.edu.uees.modelo.AlertaSingleton;
import ec.edu.uees.modelo.BT;
import ec.edu.uees.modelo.PersonajeSingleton;
import ec.edu.uees.modelo.ResultadoSingleton;
import ec.edu.uees.opciones.ScreenManager;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class FinalController implements Initializable{
    private static final String WIKIPEDIA_USER_AGENT = "AkinatorProyecto/1.0 (proyecto academico UEES)";
    private static final int WIKIPEDIA_TIMEOUT_MILLIS = 8000;

    private String searchText;
    @FXML private ImageView imagenResultado, akinatorFinalDefault, akinatorFinal1, akinatorFinal2, imagenAlerta;
    @FXML private AnchorPane mainAnchor;
    @FXML private StackPane stackBurbuja;
    private Stage stage;
    private double xOffset = 0;
    private double yOffset = 0;
    @FXML private Button cerrar, minimizar, btnHome, cerrarAlerta;
    @FXML private Label labelResultado, preguntaPerdida1, preguntaPerdida2,lblAkinatorFinal, alertatexto;
    @FXML private VBox vboxBotonesResultados;
    @FXML private TextField textfield1, textfield2;
    private String respuesta1;
    private BT<String> arbol = new BT<>();
    private JuegoController juego = new JuegoController();
    private ArrayList<String> arboltxt = juego.leerArchivo();
    private App app = new App();;
    
    @FXML
    private void switchToMenu() throws IOException {
        App.setRoot("menu");
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        arbol.armarPostOrder(arboltxt);
        
        searchText = PersonajeSingleton.getInstance().getPersonaje();
        if(labelResultado != null) {
            labelResultado.setText(searchText);
            loadWikipediaImage(searchText);
        }
        
        if(!ScreenManager.getInstance().isScreenLocked()) {
            mainAnchor.setOnMousePressed(e -> {
                xOffset = e.getSceneX();
                yOffset = e.getSceneY();
            });

            mainAnchor.setOnMouseDragged(e -> {
                if(cerrarAlerta == null) {
                    stage = (Stage) cerrar.getScene().getWindow();    
                } else {
                    stage = (Stage) cerrarAlerta.getScene().getWindow(); 
                }
                if(!stage.isFullScreen()){
                    stage.setX(e.getScreenX() - xOffset);
                    stage.setY(e.getScreenY() - yOffset);
                }
            });
        }
        
        if(preguntaPerdida2 != null) {
            preguntaPerdida2.setText("¿Cuál sería la pregunta que lo diferencia de "+searchText+"?");
        }
        
        if(labelResultado != null && ResultadoSingleton.getInstance().getResultado()) {
            akinatorFinal2.setVisible(true);
            akinatorFinalDefault.setVisible(false);
            vboxBotonesResultados.setVisible(false);
            btnHome.setVisible(true);
            lblAkinatorFinal.setText("¡Bravo! Me lo has puesto difícil.");
            stackBurbuja.setVisible(true);
            ResultadoSingleton.getInstance().setResultado(false);
        }
        
        if(alertatexto != null) {
            alertatexto.setText(AlertaSingleton.getInstance().getAlerta());
            GaussianBlur blur = new GaussianBlur(10);
            imagenAlerta.setEffect(blur);
        }
    }
    
    private String getWikipediaImage(String actorName) {
        HttpURLConnection conn = null;
        try {
            if(actorName == null || actorName.isBlank()) {
                return null;
            }

            String pageName = actorName.trim().replaceAll("\\s+", "_");
            String encodedPageName = URLEncoder.encode(pageName, StandardCharsets.UTF_8);
            URL url = new URL("https://es.wikipedia.org/api/rest_v1/page/summary/" + encodedPageName);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", WIKIPEDIA_USER_AGENT);
            conn.setRequestProperty("Accept", "application/json");
            conn.setConnectTimeout(WIKIPEDIA_TIMEOUT_MILLIS);
            conn.setReadTimeout(WIKIPEDIA_TIMEOUT_MILLIS);
            if(conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                return null;
            }

            String jsonResponse;
            try(InputStream response = conn.getInputStream()) {
                jsonResponse = new String(response.readAllBytes(), StandardCharsets.UTF_8);
            }
            return extractThumbnailSource(jsonResponse);
        } catch (IOException | RuntimeException e) {
            return null;
        } finally {
            if(conn != null) {
                conn.disconnect();
            }
        }
    }

    private void loadWikipediaImage(String actorName) {
        Thread imageLoader = new Thread(() -> {
            String url = getWikipediaImage(actorName);
            if(url == null) {
                return;
            }

            Image image = downloadWikipediaImage(url);
            if(image == null || image.isError()) {
                return;
            }

            try {
                Platform.runLater(() -> imagenResultado.setImage(image));
            } catch(IllegalStateException ignored) {
                // La aplicación puede haberse cerrado mientras terminaba la descarga.
            }
        }, "wikipedia-image-loader");
        imageLoader.setDaemon(true);
        imageLoader.start();
    }

    private Image downloadWikipediaImage(String imageUrl) {
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) new URL(imageUrl).openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", WIKIPEDIA_USER_AGENT);
            conn.setRequestProperty("Accept", "image/*");
            conn.setConnectTimeout(WIKIPEDIA_TIMEOUT_MILLIS);
            conn.setReadTimeout(WIKIPEDIA_TIMEOUT_MILLIS);
            if(conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                return null;
            }

            try(InputStream imageData = conn.getInputStream()) {
                Image image = new Image(imageData);
                return image.isError() ? null : image;
            }
        } catch (IOException | RuntimeException e) {
            return null;
        } finally {
            if(conn != null) {
                conn.disconnect();
            }
        }
    }

    private String extractThumbnailSource(String jsonResponse) {
        int thumbnailIndex = jsonResponse.indexOf("\"thumbnail\"");
        if(thumbnailIndex < 0) {
            return null;
        }
        int objectStart = jsonResponse.indexOf('{', thumbnailIndex);
        int objectEnd = objectStart < 0 ? -1 : jsonResponse.indexOf('}', objectStart);
        if(objectStart < 0 || objectEnd < 0) {
            return null;
        }

        String thumbnail = jsonResponse.substring(objectStart + 1, objectEnd);
        Matcher source = Pattern.compile("\\\"source\\\"\\s*:\\s*\\\"((?:\\\\.|[^\\\"\\\\])*)\\\"")
            .matcher(thumbnail);
        if(!source.find()) {
            return null;
        }

        return unescapeJsonString(source.group(1));
    }

    private String unescapeJsonString(String value) {
        StringBuilder decoded = new StringBuilder(value.length());
        for(int i = 0; i < value.length(); i++) {
            char current = value.charAt(i);
            if(current != '\\' || i + 1 >= value.length()) {
                decoded.append(current);
                continue;
            }

            char escaped = value.charAt(++i);
            switch(escaped) {
                case '"', '\\', '/' -> decoded.append(escaped);
                case 'b' -> decoded.append('\b');
                case 'f' -> decoded.append('\f');
                case 'n' -> decoded.append('\n');
                case 'r' -> decoded.append('\r');
                case 't' -> decoded.append('\t');
                case 'u' -> {
                    if(i + 4 < value.length()) {
                        try {
                            decoded.append((char) Integer.parseInt(value.substring(i + 1, i + 5), 16));
                            i += 4;
                        } catch(NumberFormatException e) {
                            decoded.append('u');
                        }
                    } else {
                        decoded.append('u');
                    }
                }
                default -> decoded.append(escaped);
            }
        }
        return decoded.toString();
    }
    
    @FXML
    private void cerrarStage() {
        if(cerrarAlerta == null) {
            stage = (Stage) cerrar.getScene().getWindow();    
        } else {
            stage = (Stage) cerrarAlerta.getScene().getWindow(); 
        }
        stage.close();
    }
    @FXML
    private void minimizarStage() {
        stage = (Stage) minimizar.getScene().getWindow();
        stage.setIconified(true);
    }
    
    @FXML
    private void btnResultadoSi() {
        akinatorFinal1.setVisible(true);
        akinatorFinalDefault.setVisible(false);
        vboxBotonesResultados.setVisible(false);
        btnHome.setVisible(true);
        stackBurbuja.setVisible(true);
    }
    
    @FXML
    private void btnResultadoNo() throws IOException {
        ResultadoSingleton.getInstance().setResultado(true);
        App.setRootSinAnimar("finalPerdido");
    }
    
    @FXML
    private void enviar() throws IOException {
        // VALIDACIONES
        if ((textfield1.isVisible() && textfield1.getText().isBlank()) || // Si los campos estan vacios
           (textfield2.isVisible() && textfield2.getText().isBlank())) {
            AlertaSingleton.getInstance().setAlerta("Debes escribir algo en el campo.");
            app.abrirAlerta();
            return;
        }
        if (textfield1.isVisible() && arbol.existeEnArbol(textfield1.getText())) { // Si el personaje ya existe
            AlertaSingleton.getInstance().setAlerta("Este personaje ya existe en el juego.");
            app.abrirAlerta();
            return;
        }
        if (textfield2.isVisible() && !textfield2.getText().startsWith("¿") && !textfield2.getText().endsWith("?")) {
            // Si no tiene formato de pregunta
            AlertaSingleton.getInstance().setAlerta("La pregunta debe incluir (¿) al inicio y (?) al final.");
            app.abrirAlerta();
            return;
        }
        if (textfield2.isVisible() && textfield2.getText().replace("¿", "").replace("?", "").isBlank()) {
            // Si la pregunta esta vacia
            AlertaSingleton.getInstance().setAlerta("La pregunta debe incluir al menos un caracter.");
            app.abrirAlerta();
            return;
        }
        if(textfield1.isVisible()) { // Llena campo 1
            respuesta1 = textfield1.getText();
            preguntaPerdida1.setVisible(false);
            textfield1.setVisible(false);
            preguntaPerdida2.setVisible(true);
            textfield2.setVisible(true);
        } else if(textfield2.isVisible()) { // Llena campo 2 y guarda respuesta
            arbol.add(textfield2.getText(), respuesta1, searchText);
            reescribirArchivo(arbol.desarmarPostOrder());
            PersonajeSingleton.getInstance().setPersonaje(respuesta1);
            App.setRootSinAnimar("final");
        }
    }
    
    private void reescribirArchivo(List<String> texto) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("arbolActores.txt", false))) {
            for (String linea : texto) {
                writer.write(linea);
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Ocurrió un error al escribir en el archivo: " + e.getMessage());
        }
    }
}
