package org.example.algos_fx;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.*;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.DrawMode;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
public class BaggageCompartment3D extends Application {
    // Параметры управления камерой
    private double anchorX, anchorY;
    private double anchorAngleX = 0;
    private double anchorAngleY = 0;
    private final Rotate rotateX = new Rotate(20, Rotate.X_AXIS);
    private final Rotate rotateY = new Rotate(-20, Rotate.Y_AXIS);
    private final Translate cameraTranslate = new Translate(0, 0, -500);
    // Основные группы сцены
    private final Group world = new Group();
    private final Group luggageGroup = new Group();
    private final List<Baggage> baggageList = new ArrayList<>();
    private final Group cameraHolder = new Group();
    // Параметры отсека и багажа
    private Box compartment = null;
    private int nextBaggageId = 1;
    private boolean alertShown = false;
    // Экземпляр алгоритма размещения
    private BaggagePlacementAlgorithm placementAlgorithm = null;
    // Старый код для автоматической сетки (не используется в этом примере)
    private final int itemsPerRow = 5;
    private final double spacingX = 10;
    private final double spacingZ = 10;
    // Таймлайн для обновления физики (например, 25 кадров в секунду)
    private Timeline physicsTimeline;
    @Override
    public void start(Stage primaryStage) {
        // Панель ввода параметров отсека
        Label compLengthLabel = new Label("Длина отсека:");
        TextField compLengthField = new TextField();
        compLengthField.setPrefWidth(60);
        Label compWidthLabel = new Label("Ширина отсека:");
        TextField compWidthField = new TextField();
        compWidthField.setPrefWidth(60);
        Label compHeightLabel = new Label("Высота отсека:");
        TextField compHeightField = new TextField();
        compHeightField.setPrefWidth(60);
        Button setCompartmentButton = new Button("Установить отсек");
        HBox compartmentPanel = new HBox(10, compLengthLabel, compLengthField,
                compWidthLabel, compWidthField,
                compHeightLabel, compHeightField, setCompartmentButton);
        compartmentPanel.setPadding(new Insets(10));
        compartmentPanel.setAlignment(Pos.CENTER);
        // Панель ввода параметров сумки
        Label lengthLabel = new Label("Длина сумки:");
        TextField lengthField = new TextField();
        lengthField.setPrefWidth(60);
        Label widthLabel = new Label("Ширина сумки:");
        TextField widthField = new TextField();
        widthField.setPrefWidth(60);
        Label heightLabel = new Label("Высота сумки:");
        TextField heightField = new TextField();
        heightField.setPrefWidth(60);
        CheckBox fragileCheck = new CheckBox("Хрупкая");
        Button addButton = new Button("Добавить сумку");
        Button resortButton = new Button("Перестроить приоритет");
        HBox baggagePanel = new HBox(10, lengthLabel, lengthField, widthLabel, widthField,
                heightLabel, heightField, fragileCheck, addButton, resortButton);
        baggagePanel.setPadding(new Insets(10));
        baggagePanel.setAlignment(Pos.CENTER);
        VBox topPanel = new VBox(10, compartmentPanel, baggagePanel);
        // Добавляем группу багажа и камеру в сцену
        world.getChildren().add(luggageGroup);
        PerspectiveCamera camera = new PerspectiveCamera(true);
        camera.setNearClip(0.1);
        camera.setFarClip(10000.0);
        camera.getTransforms().add(cameraTranslate);
        Group cameraGroup = new Group(camera);
        cameraGroup.getTransforms().addAll(rotateX, rotateY);
        cameraHolder.getChildren().add(cameraGroup);
        world.getChildren().add(cameraHolder);
        SubScene subScene = new SubScene(world, 800, 600, true, SceneAntialiasing.BALANCED);
        subScene.setFill(Color.SKYBLUE);
        subScene.setCamera(camera);
        initMouseControl(subScene);
        subScene.setOnScroll((ScrollEvent event) -> {
            cameraTranslate.setZ(cameraTranslate.getZ() + event.getDeltaY());
        });
        BorderPane rootPane = new BorderPane();
        rootPane.setTop(topPanel);
        rootPane.setCenter(subScene);
        // Устанавливаем отсек
        setCompartmentButton.setOnAction(e -> {
            try {
                double compLength = Double.parseDouble(compLengthField.getText());
                double compWidth  = Double.parseDouble(compWidthField.getText());
                double compHeight = Double.parseDouble(compHeightField.getText());
                if (compartment != null) {
                    world.getChildren().remove(compartment);
                }
                // Конструктор Box: ширина (X), высота (Y), глубина (Z)
                compartment = new Box(compLength, compHeight, compWidth);
                PhongMaterial compMaterial = new PhongMaterial(new Color(0.5, 0.5, 0.5, 0.2));
                compartment.setMaterial(compMaterial);
                compartment.setCullFace(CullFace.NONE);
                compartment.setDrawMode(DrawMode.LINE);
                compartment.setMouseTransparent(true);
                // Центрируем отсек в (0,0,0)
                compartment.setTranslateX(0);
                compartment.setTranslateY(0);
                compartment.setTranslateZ(0);
                world.getChildren().add(0, compartment);
                cameraHolder.translateXProperty().bind(compartment.translateXProperty());
                cameraHolder.translateYProperty().bind(compartment.translateYProperty());
                cameraHolder.translateZProperty().bind(compartment.translateZProperty());
                // Инициализация алгоритма размещения с margin = 10 и gridStep = 5
                placementAlgorithm = new BaggagePlacementAlgorithm(compLength, compHeight, compWidth, 10, 5);
                // Запускаем физическую симуляцию (если еще не запущена)
                if (physicsTimeline == null) {
                    physicsTimeline = new Timeline(new KeyFrame(Duration.millis(40), event -> updatePhysics()));
                    physicsTimeline.setCycleCount(Timeline.INDEFINITE);
                    physicsTimeline.play();
                }
            } catch (NumberFormatException ex) {
                System.out.println("Ошибка: введите корректные числовые значения для размеров отсека.");
            }
        });
        // Добавляем сумку с использованием алгоритма размещения
        addButton.setOnAction(e -> {
            try {
                double length = Double.parseDouble(lengthField.getText());
                double width  = Double.parseDouble(widthField.getText());
                double height = Double.parseDouble(heightField.getText());
                boolean fragile = fragileCheck.isSelected();
                Baggage bag = new Baggage(nextBaggageId++, length, width, height, fragile);
                if (placementAlgorithm == null) {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Сначала установите размеры отсека!");
                    alert.showAndWait();
                    return;
                }
                // Ищем оптимальное размещение
                BaggagePlacementAlgorithm.Placement placement = placementAlgorithm.findOptimalPlacement(bag);
                if (placement != null) {
                    bag.setPlacement(placement);
                    baggageList.add(bag);
                    luggageGroup.getChildren().add(bag);
                    placementAlgorithm.addPlacedBaggage(bag);
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Нет свободного места для размещения сумки");
                    alert.showAndWait();
                }
                // Очистка полей ввода
                lengthField.clear();
                widthField.clear();
                heightField.clear();
                fragileCheck.setSelected(false);
            } catch (NumberFormatException ex) {
                System.out.println("Ошибка: введите корректные числовые значения для размеров сумки.");
            }
        });
        // Перестроение багажа по приоритету – переупаковка ранее добавленных сумок
        resortButton.setOnAction(e -> {
            if (placementAlgorithm == null) return;
            // Сортировка сумок по приоритету и очистка текущих размещений
            baggageList.sort(Comparator.comparingDouble(Baggage::getPriority));
            placementAlgorithm.clearPlacements();
            // Для каждого багажа заново ищем оптимальное размещение
            for (Baggage bag : baggageList) {
                BaggagePlacementAlgorithm.Placement placement = placementAlgorithm.findOptimalPlacement(bag);
                if (placement != null) {
                    bag.setPlacement(placement);
                    placementAlgorithm.addPlacedBaggage(bag);
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR,
                            "Невозможно перестроить размещение для сумки ID " + bag.getId());
                    alert.showAndWait();
                }
            }
        });
        Scene scene = new Scene(rootPane, 800, 650, true);
        primaryStage.setTitle("3D Отсек Багажа с физикой и умным алгоритмом размещения");
        primaryStage.setScene(scene);
        primaryStage.show();
        // Периодическая проверка корректности размещения сумок (если вдруг они выходят за пределы отсека)
        Timeline checkTimeline = new Timeline(new KeyFrame(Duration.seconds(2), event -> checkBaggagePositions()));
        checkTimeline.setCycleCount(Timeline.INDEFINITE);
        checkTimeline.play();
    }
    // Проверка, что все сумки находятся внутри отсека
    private void checkBaggagePositions() {
        if (compartment == null) return;
        double margin = 10;
        double compMinX = -compartment.getWidth() / 2 + margin;
        double compMaxX = compartment.getWidth() / 2 - margin;
        double compMinY = -compartment.getHeight() / 2 + margin;
        double compMaxY = compartment.getHeight() / 2 - margin;
        double compMinZ = -compartment.getDepth() / 2 + margin;
        double compMaxZ = compartment.getDepth() / 2 - margin;
        boolean outside = false;
        for (Baggage bag : baggageList) {
            double bagMinX = bag.getTranslateX() - (bag.isRotated() ? bag.getWidth() : bag.getLength()) / 2;
            double bagMaxX = bag.getTranslateX() + (bag.isRotated() ? bag.getWidth() : bag.getLength()) / 2;
            double bagMinY = bag.getTranslateY() - bag.getHeight() / 2;
            double bagMaxY = bag.getTranslateY() + bag.getHeight() / 2;
            double bagMinZ = bag.getTranslateZ() - (bag.isRotated() ? bag.getLength() : bag.getWidth()) / 2;
            double bagMaxZ = bag.getTranslateZ() + (bag.isRotated() ? bag.getLength() : bag.getWidth()) / 2;
            if (bagMinX < compMinX || bagMaxX > compMaxX ||
                    bagMinY < compMinY || bagMaxY > compMaxY ||
                    bagMinZ < compMinZ || bagMaxZ > compMaxZ) {
                outside = true;
                break;
            }
        }
        if (outside && !alertShown) {
            alertShown = true;
            Alert alert = new Alert(Alert.AlertType.ERROR, "Ошибка: сумка(и) выходят за пределы отсека");
            alert.showAndWait();
            alertShown = false;
        }
    }
    // Управление камерой мышью
    private void initMouseControl(SubScene subScene) {
        subScene.setOnMousePressed((MouseEvent event) -> {
            anchorX = event.getSceneX();
            anchorY = event.getSceneY();
            anchorAngleX = rotateX.getAngle();
            anchorAngleY = rotateY.getAngle();
        });
        subScene.setOnMouseDragged((MouseEvent event) -> {
            rotateX.setAngle(anchorAngleX - (anchorY - event.getSceneY()) / 2);
            rotateY.setAngle(anchorAngleY + (anchorX - event.getSceneX()) / 2);
        });
    }
    //////////////////////////////////////////////////////////////////////////////////////////
    //  Класс BaggagePlacementAlgorithm – алгоритм оптимального размещения сумок
    //  Перебираем по Y (от пола) затем X и Z
    //////////////////////////////////////////////////////////////////////////////////////////
    public static class BaggagePlacementAlgorithm {
        private double compartmentWidth;
        private double compartmentHeight;
        private double compartmentDepth;
        private double margin;
        private double gridStep;
        private List<Baggage> placedBaggage;
        public BaggagePlacementAlgorithm(double compartmentWidth, double compartmentHeight, double compartmentDepth,
                                         double margin, double gridStep) {
            this.compartmentWidth = compartmentWidth;
            this.compartmentHeight = compartmentHeight;
            this.compartmentDepth = compartmentDepth;
            this.margin = margin;
            this.gridStep = gridStep;
            this.placedBaggage = new ArrayList<>();
        }
        // Метод для очистки размещений (при перестроении)
        public void clearPlacements() {
            placedBaggage.clear();
        }
        // Класс, описывающий размещение сумки
        public static class Placement {
            public double x;
            public double y;
            public double z;
            public boolean rotated; // Если true – сумка с поворотом (swap length и width)
            public Placement(double x, double y, double z, boolean rotated) {
                this.x = x;
                this.y = y;
                this.z = z;
                this.rotated = rotated;
            }
            @Override
            public String toString() {
                return "Placement: x=" + x + ", y=" + y + ", z=" + z + ", rotated=" + rotated;
            }
        }
        // Добавляем размещённую сумку для проверки пересечений
        public void addPlacedBaggage(Baggage bag) {
            placedBaggage.add(bag);
        }
        // Ищем оптимальное размещение: сначала без поворота, затем с (если возможно)
        public Placement findOptimalPlacement(Baggage bag) {
            Placement placement = findPlacementForOrientation(bag, false);
            if (placement != null) return placement;
            if (bag.canRotate()) {
                placement = findPlacementForOrientation(bag, true);
                if (placement != null) return placement;
            }
            return null;
        }
        /**
         * Перебираем Y (снизу), затем X и Z, чтобы найти допустимое место.
         */
        private Placement findPlacementForOrientation(Baggage bag, boolean rotated) {
            double length = rotated ? bag.getWidth() : bag.getLength();
            double width  = rotated ? bag.getLength() : bag.getWidth();
            double height = bag.getHeight();
            double minX = -compartmentWidth / 2 + margin + length / 2;
            double maxX = compartmentWidth / 2 - margin - length / 2;
            double minZ = -compartmentDepth / 2 + margin + width / 2;
            double maxZ = compartmentDepth / 2 - margin - width / 2;
            double minY = -compartmentHeight / 2 + margin + height / 2;
            double maxY = compartmentHeight / 2 - margin - height / 2;
            for (double y = minY; y <= maxY; y += gridStep) {
                for (double x = minX; x <= maxX; x += gridStep) {
                    for (double z = minZ; z <= maxZ; z += gridStep) {
                        if (!isColliding(x, y, z, length, height, width)) {
                            return new Placement(x, y, z, rotated);
                        }
                    }
                }
            }
            return null;
        }
        /**
         * Проверка коллизии (AABB) между новой сумкой и уже размещёнными.
         */
        private boolean isColliding(double x, double y, double z, double length, double height, double width) {
            double newMinX = x - length / 2;
            double newMaxX = x + length / 2;
            double newMinY = y - height / 2;
            double newMaxY = y + height / 2;
            double newMinZ = z - width / 2;
            double newMaxZ = z + width / 2;
            for (Baggage placed : placedBaggage) {
                double placedLength = placed.isRotated() ? placed.getWidth() : placed.getLength();
                double placedWidth  = placed.isRotated() ? placed.getLength() : placed.getWidth();
                double placedHeight = placed.getHeight();
                double px = placed.getX();
                double py = placed.getY();
                double pz = placed.getZ();
                double placedMinX = px - placedLength / 2;
                double placedMaxX = px + placedLength / 2;
                double placedMinY = py - placedHeight / 2;
                double placedMaxY = py + placedHeight / 2;
                double placedMinZ = pz - placedWidth / 2;
                double placedMaxZ = pz + placedWidth / 2;
                boolean overlapX = newMaxX > placedMinX && newMinX < placedMaxX;
                boolean overlapY = newMaxY > placedMinY && newMinY < placedMaxY;
                boolean overlapZ = newMaxZ > placedMinZ && newMinZ < placedMaxZ;
                if (overlapX && overlapY && overlapZ) return true;
            }
            return false;
        }
    }
    //////////////////////////////////////////////////////////////////////////////////////////
    //  Класс Baggage – сумка (багаж) в 3D-сцене с физикой и методами размещения
    //////////////////////////////////////////////////////////////////////////////////////////
    private class Baggage extends Group {
        private int id;
        private double length;
        private double width;
        private double height;
        private boolean fragile;
        private boolean rotated;  // Флаг: сумка размещена с поворотом
        private Box box;
        private Text label;
        // Параметр вертикальной скорости для физической симуляции
        public double vY = 0;
        public Baggage(int id, double length, double width, double height, boolean fragile) {
            this.id = id;
            this.length = length;
            this.width = width;
            this.height = height;
            this.fragile = fragile;
            this.rotated = false;
            box = new Box(length, height, width);
            PhongMaterial material = new PhongMaterial();
            material.setDiffuseColor(fragile ? Color.RED : Color.BLUE);
            box.setMaterial(material);
            label = new Text("ID: " + id);
            label.setFont(Font.font(14));
            label.setFill(Color.BLACK);
            label.setTranslateY(-height / 2 - 5);
            label.setTranslateX(-length / 4);
            getChildren().addAll(box, label);
        }
        public double getLength() {
            return length;
        }
        public double getWidth() {
            return width;
        }
        public double getHeight() {
            return height;
        }
        public boolean isFragile() {
            return fragile;
        }
        public boolean isRotated() {
            return rotated;
        }
        // Для алгоритма используем translateX/Y/Z как координаты центра
        public double getX() {
            return getTranslateX();
        }
        public double getY() {
            return getTranslateY();
        }
        public double getZ() {
            return getTranslateZ();
        }
        /**
         * Применяем найденное размещение: устанавливаем координаты и флаг поворота.
         * Если объект размещён с поворотом, изменяем визуальные размеры коробки.
         */
        public void setPlacement(BaggagePlacementAlgorithm.Placement placement) {
            setTranslateX(placement.x);
            setTranslateY(placement.y);
            setTranslateZ(placement.z);
            this.rotated = placement.rotated;
            if (rotated) {
                box.setWidth(length);
                box.setDepth(width);
            }
        }
        /**
         * Расчет приоритета – объём сумки (с поправкой для хрупкого багажа).
         */
        public double getPriority() {
            double volume = length * width * height;
            return fragile ? volume - 1000 : volume;
        }
        // Если длина и ширина различны, допускается поворот
        public boolean canRotate() {
            return length != width;
        }
    }
    /**
     * Метод обновления физики.
     * Для каждой сумки проверяем, поддерживается ли она полом или другой сумкой.
     * Если нет – к вертикальной скорости применяется гравитация, и объект опускается вниз.
     * Дополнительно проверяем, чтобы поддерживающая сумка имела достаточную опору
     * (если над маленькой сумкой стоит большая, объект падает).
     */
    private void updatePhysics() {
        if (compartment == null) return;
        // Координата пола отсека:
        double floorY = -compartment.getHeight() / 2 + 10; // margin = 10
        // Для каждого объекта:
        for (Baggage bag : baggageList) {
            boolean supported = false;
            // Проверка: касается ли пол?
            if (bag.getTranslateY() - bag.getHeight() / 2 <= floorY + 1) {
                supported = true;
                bag.setTranslateY(floorY + bag.getHeight() / 2);
                bag.vY = 0;
            } else {
                // Поиск среди других сумок, расположенных ниже
                for (Baggage other : baggageList) {
                    if (other == bag) continue;
                    double otherTop = other.getTranslateY() + other.getHeight() / 2;
                    // Если нижняя грань текущей сумки почти совпадает с верхней гранью другой
                    if (Math.abs((bag.getTranslateY() - bag.getHeight() / 2) - otherTop) < 3) {
                        // Проверяем, что проекция по X/Z полностью лежит внутри опоры другой сумки
                        double bMinX = bag.getTranslateX() - (bag.isRotated() ? bag.getWidth() : bag.getLength()) / 2;
                        double bMaxX = bag.getTranslateX() + (bag.isRotated() ? bag.getWidth() : bag.getLength()) / 2;
                        double bMinZ = bag.getTranslateZ() - (bag.isRotated() ? bag.getLength() : bag.getWidth()) / 2;
                        double bMaxZ = bag.getTranslateZ() + (bag.isRotated() ? bag.getLength() : bag.getWidth()) / 2;
                        double oMinX = other.getTranslateX() - (other.isRotated() ? other.getWidth() : other.getLength()) / 2;
                        double oMaxX = other.getTranslateX() + (other.isRotated() ? other.getWidth() : other.getLength()) / 2;
                        double oMinZ = other.getTranslateZ() - (other.isRotated() ? other.getLength() : other.getWidth()) / 2;
                        double oMaxZ = other.getTranslateZ() + (other.isRotated() ? other.getLength() : other.getWidth()) / 2;
                        if (bMinX >= oMinX && bMaxX <= oMaxX && bMinZ >= oMinZ && bMaxZ <= oMaxZ) {
                            supported = true;
                            // Приклеиваем сумку к верхней грани поддерживающей сумки
                            double newY = otherTop + bag.getHeight() / 2;
                            bag.setTranslateY(newY);
                            bag.vY = 0;
                            break;
                        }
                    }
                }
            }
            // Если не поддерживается – применяем гравитацию
            if (!supported) {
                bag.vY += -0.5;  // ускорение гравитации (подберите по вкусу)
                bag.setTranslateY(bag.getTranslateY() + bag.vY);
            }
        }
    }
    public static void main(String[] args) {
        launch(args);
    }
}
