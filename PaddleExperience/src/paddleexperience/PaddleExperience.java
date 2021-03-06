/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package paddleexperience;

// Java imports
import DBAcess.ClubDBAccess;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

// Javafx imports
import javafx.application.Application;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import paddleexperience.Dataclasses.Estat;
import paddleexperience.Dataclasses.Hora;

// Internal imports
import paddleexperience.Structures.Stoppable;
import paddleexperience.Structures.Cache;

/**
 *
 * @author joandzmn
 */
public class PaddleExperience extends Application {

    public static final int minHeight = 550;
    public static final int minWidth = 810;

    // Loader
    private static final HashMap<String, Parent> root = new HashMap<String, Parent>();
    private static final HashMap<String, Stoppable> controllers = new HashMap<String, Stoppable>();
    private static String back_root, prev_root;

    // Auxiliar
    static Class static_class;

    public static Stage last_window;

    @Override
    public void start(Stage stage) throws Exception {
        startAll();

        // Initial scene
        back_root = "FXMLPaddleExperience.fxml";
        prev_root = back_root;

        static_class = getClass();

        // Carrega tots els fxml en un diccionari per poder obrir-los més tard
        FXMLLoader loader;
        Parent initial_root = null;

        for (File file : new File("./src/paddleexperience/").listFiles()) {
            if (file.isFile() && file.getName().endsWith(".fxml")
                    && !root.containsKey(file.getName())) {

                System.out.println(file.getName());

                loader = new FXMLLoader(getClass().getResource(file.getName()));
                root.put(file.getName(), loader.load());
                controllers.put(file.getName(), loader.getController());

                if (file.getName().equals(back_root)) {
                    initial_root = root.get(file.getName());

                    controllers.get(file.getName()).refresh();
                }
            }
        }

        Rectangle2D screenBounds = Screen.getPrimary().getBounds();

        stage.setWidth(minWidth);//screenBounds.getWidth());
        stage.setHeight(minHeight);//screenBounds.getHeight());

        stage.setMinHeight(minHeight);
        stage.setMinWidth(minWidth);

        /*stage.setOnCloseRequest((WindowEvent event) -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Informacio");
            alert.setHeaderText("Guardant en la base de dades");
            alert.setContentText("L'aplicació està guardant els canvis a la base de dades,"
                    + " això pot tardar uns minuts");
            alert.show();
            Estat.club.saveDB();
        });*/
        stage.setTitle("Paddle Experience - International");
        stage.setScene(new Scene(initial_root));
        stage.show();
    }

    @Override
    public void stop() throws InterruptedException {
        if (controllers.get(back_root) == null) {
            return;
        }

        Stoppable controller = controllers.get(back_root);

        if (controller == null) {
            return;
        }

        controller.stop();

        Cache.stop();

        Estat.save();

        System.exit(0);
    }

    // Executar static functions abans d'iniciar el programa
    static void startAll() {
        Cache.start();
        Estat.start();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    // Canvia el root de l'escena en la finestra
    // on ha ocorregut l'Event event. Esta funció
    // NO ha de ser utilitzada com a manejador d'events
    public static void setParent(Event event, String sceneName) throws InterruptedException {
        ((Node) event.getSource()).getScene().setRoot(root.get(sceneName));

        controllers.get(back_root).stop();
        controllers.get(sceneName).refresh();

        prev_root = back_root;
        back_root = sceneName;
    }

    // Canvia el root de l'escena a l'anterior root
    // en la finestra on ha ocorregut l'Event event.
    // Esta funció NO ha de ser utilitzada com a manejador
    // d'events
    public static void back(Event event) throws InterruptedException {
        ((Node) event.getSource()).getScene().setRoot(root.get(prev_root));

        controllers.get(back_root).stop();
        controllers.get(prev_root).refresh();

        // Swap prev_root, back_root
        String aux = prev_root;
        prev_root = back_root;
        back_root = aux;
    }

    public static void stop(String sceneName) throws InterruptedException {
        controllers.get(sceneName).stop();
    }

    public static void refresh(String sceneName) {
        controllers.get(sceneName).refresh();
    }

    public static void window(String sceneName)
            throws IOException {
        window(sceneName, "", minWidth, minHeight, Modality.NONE);
    }

    public static void window(String sceneName, String title)
            throws IOException {
        window(sceneName, title, minWidth, minHeight, Modality.NONE);
    }

    public static void window(String sceneName, String title, double width, double height)
            throws IOException {
        window(sceneName, title, width, height, Modality.NONE);
    }

    // It will use 
    public static void window(String sceneName, String title, double width, double height,
            Modality mode) throws IOException {
        
        Parent new_window_root = root.get(sceneName);
        Stoppable new_window_controller = controllers.get(sceneName);
        
        boolean exists = controllers.containsKey(sceneName);

        if (!exists || new_window_root.getScene() != null) {
            System.out.println("### NEW");
            
            FXMLLoader loader = new FXMLLoader(static_class.getResource(sceneName));
            
            new_window_root = loader.load();
            new_window_controller = loader.getController();
                    
            if(!exists){ // crea el Parent i guarda'l per al seu ús global
                root.put(sceneName, new_window_root);
                controllers.put(sceneName, new_window_controller);
            }
        }

        Stage stage = new Stage();
        stage.setScene(new Scene(new_window_root));
        stage.setTitle(title);
        stage.setMinWidth(width);
        stage.setMinHeight(height);

        stage.setWidth(width);
        stage.setHeight(height);

        stage.initModality(mode);

        last_window = stage;
        
        final Stoppable controller_copy = new_window_controller;

        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(final WindowEvent event) {
                try {
                    controller_copy.stop();
                } catch (InterruptedException err) {
                }
            }
        });

        new_window_controller.refresh();

        stage.showAndWait();
    }

    // // GETTERS
    public static Parent getParent() throws IOException {
        if (controllers.containsKey(back_root)) {
            controllers.get(back_root).refresh();
        } else {
            FXMLLoader loader = new FXMLLoader(static_class.getResource(back_root));
            root.put(back_root, loader.load());
            controllers.put(back_root, loader.getController());
            //controllers.get(back_root).refresh();
        }

        return root.get(back_root);
    }

    public static Parent getParent(String sceneName) throws IOException {
        if (controllers.containsKey(sceneName)) {
            controllers.get(sceneName).refresh();
        } else {
            FXMLLoader loader = new FXMLLoader(static_class.getResource(sceneName));
            root.put(sceneName, loader.load());
            controllers.put(sceneName, loader.getController());
            //controllers.get(sceneName).refresh();
        }

        return root.get(sceneName);
    }

    public static Stoppable getController(String sceneName) throws IOException {
        if (controllers.containsKey(sceneName)) {
            controllers.get(sceneName).refresh();
        } else {
            FXMLLoader loader = new FXMLLoader(static_class.getResource(sceneName));
            root.put(sceneName, loader.load());
            controllers.put(sceneName, loader.getController());
            //controllers.get(sceneName).refresh();
        }

        return controllers.get(sceneName);
    }
}

