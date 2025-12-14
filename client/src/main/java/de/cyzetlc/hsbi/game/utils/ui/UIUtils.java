package de.cyzetlc.hsbi.game.utils.ui;

import de.cyzetlc.hsbi.game.audio.Sound;
import de.cyzetlc.hsbi.game.audio.SoundManager;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Die Klasse {@code UIUtils} bietet eine Sammlung statischer Dienstprogramm-Methoden zum Erstellen,
 * Zeichnen, Positionieren und Animieren gängiger JavaFX UI-Elemente wie Text, Schaltflächen (Buttons),
 * Rechtecke und Bildansichten (ImageViews) innerhalb eines {@code Pane}.
 * <p>
 * Sie beinhaltet komplexe Funktionen wie die reaktive Zentrierung und die Frame-basierte Hintergrundanimation.
 *
 *
 * @author Tom Coombs
 * @author Leonardo Parrino
 */
public class UIUtils {

    /**
     * Zentriert einen Text horizontal (und optional vertikal) innerhalb einer Pane.
     * <p>
     * Verwendet Listener, um die Zentrierung automatisch anzupassen, wenn sich die Größe
     * der {@code Pane} oder des {@code Text}-Knotens ändert.
     *
     * @param text Der zu zentrierende {@code Text}-Knoten.
     * @param parent Die übergeordnete {@code Pane}.
     * @param vertical {@code true}, um auch vertikal zu zentrieren.
     */
    public static void centerText(Text text, Pane parent, boolean vertical) {
        text.applyCss();

        Runnable doCenter = () -> {
            double textWidth = text.getLayoutBounds().getWidth();
            double textHeight = text.getLayoutBounds().getHeight();
            double baseline = text.getBaselineOffset();

            double x = (parent.getWidth() - textWidth) / 2.0;
            text.setLayoutX(x);

            if (vertical) {
                // Zentrierung berücksichtigt die Baseline für eine korrekte vertikale Mitte
                double y = (parent.getHeight() - textHeight) / 2.0 + baseline;
                text.setLayoutY(y);
            }
        };

        Platform.runLater(doCenter);

        ChangeListener<Number> parentSizeListener = (obs, oldV, newV) -> Platform.runLater(doCenter);
        parent.widthProperty().addListener(parentSizeListener);
        if (vertical) parent.heightProperty().addListener(parentSizeListener);

        // Reagiert auf Änderungen der Textgröße (z. B. durch CSS oder Schriftarten-Laden)
        text.layoutBoundsProperty().addListener((obs, oldB, newB) -> Platform.runLater(doCenter));
    }

    /**
     * Zentriert einen Button horizontal (und optional vertikal) innerhalb einer Pane.
     * <p>
     * Verwendet Listener, um die Zentrierung automatisch anzupassen, wenn sich die Größe
     * der {@code Pane} oder des {@code Button}-Knotens ändert.
     *
     * @param button Der zu zentrierende {@code Button}-Knoten.
     * @param parent Die übergeordnete {@code Pane}.
     * @param vertical {@code true}, um auch vertikal zu zentrieren.
     */
    public static void centerButton(Button button, Pane parent, boolean vertical) {
        button.applyCss();

        Runnable doCenter = () -> {
            double buttonWidth = button.getWidth();
            double buttonHeight = button.getHeight();

            double x = (parent.getWidth() - buttonWidth) / 2.0;
            button.setLayoutX(x);

            if (vertical) {
                double y = (parent.getHeight() - buttonHeight) / 2.0;
                button.setLayoutY(y);
            }
        };

        Platform.runLater(doCenter);

        ChangeListener<Number> listener = (obs, o, n) -> Platform.runLater(doCenter);
        parent.widthProperty().addListener(listener);
        if (vertical) parent.heightProperty().addListener(listener);

        button.widthProperty().addListener(listener);
        button.heightProperty().addListener(listener);
    }

    /**
     * Zeichnet einen einfachen Text in eine Pane an der angegebenen Position.
     *
     * @param parent Pane, in die der Text eingefügt wird.
     * @param label  Der anzuzeigende Textinhalt.
     * @param x      X-Position (LayoutX).
     * @param y      Y-Position (LayoutY, Baseline).
     * @return Der erstellte {@code Text}-Knoten.
     */
    public static Text drawText(Pane parent, String label, double x, double y) {
        Text text = new Text(label);
        text.setLayoutX(x);
        text.setLayoutY(y);
        parent.getChildren().add(text);
        return text;
    }

    /**
     * Zeichnet einen einfachen Text in eine Pane mit einer zugewiesenen CSS-ID.
     *
     * @param parent Pane, in die der Text eingefügt wird.
     * @param label  Der anzuzeigende Textinhalt.
     * @param x      X-Position.
     * @param y      Y-Position.
     * @param id     CSS-ID für das Styling.
     * @return Der erstellte {@code Text}-Knoten.
     */
    public static Text drawText(Pane parent, String label, double x, double y, String id) {
        Text text = new Text(label);
        text.setLayoutX(x);
        text.setLayoutY(y);
        text.setId(id);
        parent.getChildren().add(text);
        return text;
    }

    /**
     * Zeichnet einen Text und zentriert ihn automatisch in der übergeordneten Pane.
     *
     * @param parent Pane, in die der Text eingefügt wird.
     * @param label  Der anzuzeigende Textinhalt.
     * @param x      X-Position (wird durch Zentrierung überschrieben).
     * @param y      Y-Position (wird durch Zentrierung überschrieben, wenn {@code vertical} {@code true} ist).
     * @param vertical {@code true}, um auch vertikal zu zentrieren.
     * @return Der erstellte und zentrierte {@code Text}-Knoten.
     */
    public static Text drawCenteredText(Pane parent, String label, double x, double y, boolean vertical) {
        Text text = drawText(parent, label, x, y);
        centerText(text, parent, vertical);
        return text;
    }

    /**
     * Zeichnet einen Text mit CSS-ID und zentriert ihn automatisch.
     *
     * @param parent Pane, in die der Text eingefügt wird.
     * @param label  Der anzuzeigende Textinhalt.
     * @param x      X-Position.
     * @param y      Y-Position.
     * @param vertical {@code true}, um auch vertikal zu zentrieren.
     * @param id     CSS-ID für das Styling.
     * @return Der erstellte und zentrierte {@code Text}-Knoten.
     */
    public static Text drawCenteredText(Pane parent, String label, double x, double y, boolean vertical, String id) {
        Text text = drawText(parent, label, x, y, id);
        centerText(text, parent, vertical);
        return text;
    }

    /**
     * Zeichnet einen einfachen Button in eine Pane und registriert einen {@code onClick}-Handler.
     *
     * @param parent Pane, in die der Button eingefügt wird.
     * @param label  Der anzuzeigende Text auf dem Button.
     * @param x      X-Position.
     * @param y      Y-Position.
     * @param onClick Die Aktion, die beim Klicken ausgeführt wird.
     * @return Der erstellte {@code Button}-Knoten.
     */
    public static Button drawButton(Pane parent, String label, double x, double y, Runnable onClick) {
        Button button = new Button(label);
        button.setLayoutX(x);
        button.setLayoutY(y);
        button.setFocusTraversable(false);
        button.setOnAction(e -> {
            SoundManager.play(Sound.CLICK);
            onClick.run();
        });
        parent.getChildren().add(button);
        return button;
    }

    /**
     * Zeichnet einen einfachen Button mit CSS-ID und {@code onClick}-Handler.
     *
     * @param parent Pane, in die der Button eingefügt wird.
     * @param label  Der anzuzeigende Text auf dem Button.
     * @param x      X-Position.
     * @param y      Y-Position.
     * @param id     CSS-ID für das Styling.
     * @param onClick Die Aktion, die beim Klicken ausgeführt wird (kann {@code null} sein).
     * @return Der erstellte {@code Button}-Knoten.
     */
    public static Button drawButton(Pane parent, String label, double x, double y, String id, Runnable onClick) {
        Button button = new Button(label);
        button.setLayoutX(x);
        button.setLayoutY(y);
        button.setFocusTraversable(false);
        button.setId(id);
        button.setOnAction(e -> {
            SoundManager.play(Sound.CLICK);
            if (onClick != null) {
                onClick.run();
            }
        });
        parent.getChildren().add(button);
        return button;
    }

    /**
     * Zeichnet einen einfachen Button ohne spezifischen {@code onClick}-Handler.
     *
     * @param parent Pane, in die der Button eingefügt wird.
     * @param x      X-Position.
     * @param y      Y-Position.
     * @return Der erstellte {@code Button}-Knoten.
     */
    public static Button drawButton(Pane parent, String label, double x, double y) {
        return drawButton(parent, label, x, y, () -> {});
    }

    /**
     * Zeichnet einen einfachen Button mit CSS-ID, aber ohne spezifischen {@code onClick}-Handler.
     *
     * @param parent Pane, in die der Button eingefügt wird.
     * @param x      X-Position.
     * @param y      Y-Position.
     * @return Der erstellte {@code Button}-Knoten.
     */
    public static Button drawButton(Pane parent, String label, double x, double y, String id) {
        return drawButton(parent, label, x, y, id, () -> {});
    }

    /**
     * Zeichnet einen Button und zentriert ihn automatisch.
     *
     * @param parent Pane, in die der Button eingefügt wird.
     * @param x      X-Position.
     * @param y      Y-Position.
     * @param vertical {@code true}, um auch vertikal zu zentrieren.
     * @param onClick Die Aktion, die beim Klicken ausgeführt wird.
     * @return Der erstellte und zentrierte {@code Button}-Knoten.
     */
    public static Button drawCenteredButton(Pane parent, String label, double x, double y, boolean vertical, Runnable onClick) {
        Button button = drawButton(parent, label, x, y, onClick);
        centerButton(button, parent, vertical);
        return button;
    }

    /**
     * Zeichnet einen Button mit CSS-ID und zentriert ihn automatisch.
     *
     * @param parent Pane, in die der Button eingefügt wird.
     * @param x      X-Position.
     * @param y      Y-Position.
     * @param vertical {@code true}, um auch vertikal zu zentrieren.
     * @param id     CSS-ID für das Styling.
     * @param onClick Die Aktion, die beim Klicken ausgeführt wird.
     * @return Der erstellte und zentrierte {@code Button}-Knoten.
     */
    public static Button drawCenteredButton(Pane parent, String label, double x, double y, boolean vertical, String id, Runnable onClick) {
        Button button = drawButton(parent, label, x, y, id, onClick);
        centerButton(button, parent, vertical);
        return button;
    }

    /**
     * Zeichnet einen Button und zentriert ihn automatisch, ohne spezifischen {@code onClick}-Handler.
     *
     * @param parent Pane, in die der Button eingefügt wird.
     * @param x      X-Position.
     * @param y      Y-Position.
     * @param vertical {@code true}, um auch vertikal zu zentrieren.
     * @return Der erstellte und zentrierte {@code Button}-Knoten.
     */
    public static Button drawCenteredButton(Pane parent, String label, double x, double y, boolean vertical) {
        Button button = drawButton(parent, label, x, y);
        centerButton(button, parent, vertical);
        return button;
    }

    /**
     * Zeichnet ein einfaches Rechteck in eine Pane.
     *
     * @param parent Pane, in die das Rechteck eingefügt wird.
     * @param x      X-Position.
     * @param y      Y-Position.
     * @param width  Breite.
     * @param height Höhe.
     * @param color  Füllfarbe des Rechtecks.
     * @return Das erstellte {@code Rectangle}-Objekt.
     */
    public static Rectangle drawRect(Pane parent, double x, double y, double width, double height, Color color) {
        Rectangle rect = new Rectangle(x, y, width, height);
        rect.setFill(color);
        parent.getChildren().add(rect);
        return rect;
    }

    /**
     * Zeichnet eine Textur (Bild) in eine Pane.
     *
     * @param parent  Pane, in die das Bild eingefügt wird.
     * @param texture Pfad oder URL zur Bilddatei (z. B. "/assets/gui/button.png").
     * @param x       X-Position.
     * @param y       Y-Position.
     * @param width   Breite des Bildes (FitWidth).
     * @param height  Höhe des Bildes (FitHeight).
     * @return Das erstellte {@code ImageView}.
     */
    public static ImageView drawImage(Pane parent, String texture, double x, double y, double width, double height) {
        // Verwende getResourceAsStream für den sicheren Zugriff auf interne Ressourcen
        Image img = new Image(UIUtils.class.getResourceAsStream(texture));
        ImageView view = new ImageView(img);
        view.setX(x);
        view.setY(y);
        view.setFitWidth(width);
        view.setFitHeight(height);
        parent.getChildren().add(view);
        return view;
    }

    /**
     * Zeichnet einen animierten Hintergrund als einfache Frame-Schleife mit einer Standarddauer von 800 ms pro Frame.
     *
     * @param parent Pane, in die der Hintergrund eingefügt wird.
     * @param width Die Breite des Viewports.
     * @param height Die Höhe des Viewports.
     * @param framePaths Variable Liste der Ressourcenpfade für die Animations-Frames.
     * @return Der erstellte {@code ImageView}-Knoten, der die Animation hält.
     */
    public static ImageView drawAnimatedBackground(Pane parent, double width, double height, String... framePaths) {
        return drawAnimatedBackground(parent, width, height, Duration.millis(800), framePaths);
    }

    /**
     * Zeichnet einen Hintergrund-{@code ImageView}, der mithilfe einer angegebenen Frame-Dauer
     * zwischen mehreren Bild-Frames animiert wird. Enthält Fallback-Logik, falls die primären Pfade fehlschlagen.
     *
     *
     * @param parent Die {@code Pane}, in die der Hintergrund eingefügt wird.
     * @param width Die Breite des Viewports.
     * @param height Die Höhe des Viewports.
     * @param frameDuration Die Zeitdauer für jeden Frame in der Animation.
     * @param framePaths Variable Liste der Ressourcenpfade für die Animations-Frames.
     * @return Der erstellte {@code ImageView}-Knoten, der die Animation hält.
     */
    public static ImageView drawAnimatedBackground(Pane parent, double width, double height, Duration frameDuration, String... framePaths) {
        List<Image> frameList = new ArrayList<>();
        for (String path : framePaths) {
            InputStream stream = UIUtils.class.getResourceAsStream(path);
            if (stream != null) {
                frameList.add(new Image(stream));
            }
        }

        if (frameList.isEmpty()) {
            // Fallback-Logik zu älteren/generischen Pfaden
            String[] fallbackOld = {
                    "/assets/hud/BackgroundMainZustand1.png",
                    "/assets/hud/BackgroundMainZustand2.png",
                    "/assets/hud/BackgroundMainZustand3.png"
            };
            for (String path : fallbackOld) {
                InputStream stream = UIUtils.class.getResourceAsStream(path);
                if (stream != null) {
                    frameList.add(new Image(stream));
                }
            }
        }

        if (frameList.isEmpty()) {
            // Letzter Fallback zu einem einzelnen allgemeinen Hintergrund
            InputStream fallback = UIUtils.class.getResourceAsStream("/assets/hud/background.png");
            if (fallback != null) {
                frameList.add(new Image(fallback));
            }
        }

        if (frameList.isEmpty()) {
            // Letzter Notfall-Fallback (transparenter 1x1-Pixel)
            frameList.add(new WritableImage(1, 1));
        }

        Image[] frames = frameList.toArray(new Image[0]);

        ImageView view = new ImageView(frames[0]);
        view.setX(0);
        view.setY(0);
        view.setFitWidth(width);
        view.setFitHeight(height);
        view.setPreserveRatio(false);
        parent.getChildren().add(view);

        if (frames.length > 1) {
            int[] frameIndex = {0};
            // Definiert die Zeitleiste für die Animation
            Timeline loop = new Timeline(
                    new KeyFrame(frameDuration, e -> {
                        frameIndex[0] = (frameIndex[0] + 1) % frames.length;
                        view.setImage(frames[frameIndex[0]]);
                    })
            );
            loop.setCycleCount(Animation.INDEFINITE);
            loop.play();
            // Referenz auf die Timeline in den Eigenschaften speichern, um Garbage Collection zu verhindern
            view.getProperties().put("backgroundLoop", loop);
        }
        return view;
    }

    /**
     * Zeichnet einen {@code Slider} und zentriert ihn automatisch horizontal innerhalb der übergeordneten {@code Pane}.
     *
     * @param parent Die {@code Pane}, in die der Slider eingefügt wird.
     * @param min Der Minimalwert des Sliders.
     * @param max Der Maximalwert des Sliders.
     * @param value Der Startwert des Sliders.
     * @param y Die Y-Position des Sliders (wenn {@code verticalCenter} {@code false} ist).
     * @param verticalCenter {@code true}, um auch vertikal zu zentrieren; {@code false}, um das angegebene Y zu verwenden.
     * @return Der erstellte und zentrierte {@code Slider}-Knoten.
     */
    public static Slider drawCenteredSlider(Pane parent, double min, double max, double value, double y, boolean verticalCenter) {

        Slider slider = new Slider(min, max, value);
        slider.applyCss();
        slider.setPrefWidth(200); // Standardbreite festlegen
        parent.getChildren().add(slider);

        Runnable doCenter = () -> {
            double sliderWidth = slider.getWidth();
            double sliderHeight = slider.getHeight();

            double x = (parent.getWidth() - sliderWidth) / 2.0;
            slider.setLayoutX(x);

            if (verticalCenter) {
                double yCentered = (parent.getHeight() - sliderHeight) / 2.0;
                slider.setLayoutY(yCentered);
            } else {
                slider.setLayoutY(y);
            }
        };

        Platform.runLater(doCenter);

        ChangeListener<Number> parentSizeListener = (obs, o, n) -> Platform.runLater(doCenter);
        parent.widthProperty().addListener(parentSizeListener);
        if (verticalCenter) parent.heightProperty().addListener(parentSizeListener);

        slider.widthProperty().addListener((obs, o, n) -> Platform.runLater(doCenter));

        return slider;
    }

    /**
     * Berechnet und gibt die gerenderte Breite eines {@code Text}-Knotens nach Anwendung von CSS zurück.
     *
     * @param text Der {@code Text}-Knoten.
     * @return Die tatsächliche Breite der Text-Layout-Begrenzungen.
     */
    public static double getTextWidth(Text text) {
        text.applyCss();
        return text.getLayoutBounds().getWidth();
    }
}