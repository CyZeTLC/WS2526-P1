package de.cyzetlc.hsbi.game.utils.ui;

import de.cyzetlc.hsbi.game.Game;
import de.cyzetlc.hsbi.game.audio.Sound;
import de.cyzetlc.hsbi.game.audio.SoundManager;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

public class UIUtils {

    /**
     * Zentriert einen Text horizontal (und optional vertikal) innerhalb einer Pane.
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
                double y = (parent.getHeight() - textHeight) / 2.0 + baseline;
                text.setLayoutY(y);
            }
        };

        Platform.runLater(doCenter);

        ChangeListener<Number> parentSizeListener = (obs, oldV, newV) -> Platform.runLater(doCenter);
        parent.widthProperty().addListener(parentSizeListener);
        if (vertical) parent.heightProperty().addListener(parentSizeListener);

        text.layoutBoundsProperty().addListener((obs, oldB, newB) -> Platform.runLater(doCenter));
    }

    /**
     * Zentriert einen Button horizontal (und optional vertikal) innerhalb einer Pane.
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
     * Zeichnet einen einfachen Button in eine Pane.
     *
     * @param parent Pane, in die der Button eingefügt wird
     * @param x      X-Position
     * @param y      Y-Position
     * @return der erstellte Button
     */
    public static Text drawText(Pane parent, String label, double x, double y) {
        Text text = new Text(label);
        text.setLayoutX(x);
        text.setLayoutY(y);
        parent.getChildren().add(text);
        return text;
    }

    /**
     * Zeichnet einen einfachen Button in eine Pane.
     *
     * @param parent Pane, in die der Button eingefügt wird
     * @param x      X-Position
     * @param y      Y-Position
     * @param id     CSS-ID
     * @return der erstellte Button
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
     * Zeichnet einen einfachen Button in eine Pane.
     *
     * @param parent Pane, in die der Button eingefügt wird
     * @param x      X-Position
     * @param y      Y-Position
     * @return der erstellte Button
     */
    public static Text drawCenteredText(Pane parent, String label, double x, double y, boolean vertical) {
        Text text = drawText(parent, label, x, y);
        centerText(text, parent, vertical);
        return text;
    }

    /**
     * Zeichnet einen einfachen Button in eine Pane.
     *
     * @param parent Pane, in die der Button eingefügt wird
     * @param x      X-Position
     * @param y      Y-Position
     * @return der erstellte Button
     */
    public static Text drawCenteredText(Pane parent, String label, double x, double y, boolean vertical, String id) {
        Text text = drawText(parent, label, x, y, id);
        centerText(text, parent, vertical);
        return text;
    }

    /**
     * Zeichnet einen einfachen Button in eine Pane.
     *
     * @param parent Pane, in die der Button eingefügt wird
     * @param x      X-Position
     * @param y      Y-Position
     * @return der erstellte Button
     */
    public static Button drawButton(Pane parent, String label, double x, double y, Runnable onClick) {
        Button button = new Button(label);
        button.setLayoutX(x);
        button.setLayoutY(y);
        button.setOnAction(e -> {
            SoundManager.play(Sound.CLICK);
            onClick.run();
        });
        parent.getChildren().add(button);
        return button;
    }

    /**
     * Zeichnet einen einfachen Button in eine Pane.
     *
     * @param parent Pane, in die der Button eingefügt wird
     * @param x      X-Position
     * @param y      Y-Position
     * @return der erstellte Button
     */
    public static Button drawButton(Pane parent, String label, double x, double y) {
        return drawButton(parent, label, x, y, () -> {});
    }

    /**
     * Zeichnet einen einfachen Button in eine Pane.
     *
     * @param parent Pane, in die der Button eingefügt wird
     * @param x      X-Position
     * @param y      Y-Position
     * @return der erstellte Button
     */
    public static Button drawCenteredButton(Pane parent, String label, double x, double y, boolean vertical, Runnable onClick) {
        Button button = drawButton(parent, label, x, y, onClick);
        centerButton(button, parent, vertical);
        return button;
    }

    /**
     * Zeichnet einen einfachen Button in eine Pane.
     *
     * @param parent Pane, in die der Button eingefügt wird
     * @param x      X-Position
     * @param y      Y-Position
     * @return der erstellte Button
     */
    public static Button drawCenteredButton(Pane parent, String label, double x, double y, boolean vertical) {
        Button button = drawButton(parent, label, x, y);
        centerButton(button, parent, vertical);
        return button;
    }

    /**
     * Zeichnet ein einfaches Rechteck in eine Pane.
     *
     * @param parent Pane, in die das Rechteck eingefügt wird
     * @param x      X-Position
     * @param y      Y-Position
     * @param width  Breite
     * @param height Höhe
     * @param color  Farbe des Rechtecks
     * @return das erstellte Rechteck
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
     * @param parent  Pane, in die das Bild eingefügt wird
     * @param texture Pfad oder URL zur Bilddatei (z. B. "/assets/gui/button.png")
     * @param x       X-Position
     * @param y       Y-Position
     * @param width   Breite des Bildes
     * @param height  Höhe des Bildes
     * @return das erstellte ImageView
     */
    public static ImageView drawImage(Pane parent, String texture, double x, double y, double width, double height) {
        Image img = new Image(UIUtils.class.getResourceAsStream(texture));
        ImageView view = new ImageView(img);
        view.setX(x);
        view.setY(y);
        view.setFitWidth(width);
        view.setFitHeight(height);
        parent.getChildren().add(view);
        return view;
    }
}
