import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import java.util.ArrayList;
import java.util.List;

public final class ApplePanel extends JPanel {

    // Масштаб экрана: столько пикселей соответствует единице модели.
    private static final double PIXELS_PER_UNIT = 90.0;
    // Ось Z видна по диагонали: глубина влияет на экранные X и Y.
    private static final double DEPTH_X = 0.45;
    private static final double DEPTH_Y = 0.28;

    
    

    // Сколько единиц сцены и радиан приходится на пиксель движения мыши.
    private static final double ROTATION_SPEED = 0.01;
    // Условный радиус для вычисления угла качения.
    private static final double ROLL_RADIUS = 1.40;

    private static final Matrix PROJECTION_MATRIX =
        Matrix.obliqueProjection(DEPTH_X, DEPTH_Y);

    // Масштаб ограничен, чтобы яблоко не исчезало и не становилось огромным.
    private static final double MIN_ZOOM = 0.35;
    private static final double MAX_ZOOM = 3.0;
    private static final double ZOOM_STEP = 1.12;

    private final AppleModel model;
    
    // Текущее положение, угол в радианах и размер яблока.
    private double positionX = 0.0;
    private double positionY = 0.0;
    private double positionZ = 0.0;
    private double angleX = 0.0;
    private double angleY = 0.0;
    private double angleZ = 0.0;
    private double zoom = 1.0;

    private int previousMouseX;
    private int previousMouseY;
    private DragMode dragMode = DragMode.NONE;

    public ApplePanel(AppleModel model) {
        this.model = model;

        setPreferredSize(new Dimension(900, 700));
        setBackground(new Color(18, 23, 28));

        installMouseControl();
    }

    private void installMouseControl() {
        // Один обработчик следит за нажатием, движением и колесом мыши.
        MouseAdapter mouse = new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent event) {
                // Запоминаем точку начала, чтобы измерить следующее движение.
                previousMouseX = event.getX();
                previousMouseY = event.getY();

                if (SwingUtilities.isLeftMouseButton(event)
                        && event.isControlDown()) {
                    dragMode = DragMode.MOVE_Y;
                } else if (SwingUtilities.isLeftMouseButton(event)
                        && event.isAltDown()) {
                    dragMode = DragMode.MOVE_Z;
                } else if (SwingUtilities.isLeftMouseButton(event)
                        && event.isShiftDown()) {
                    dragMode = DragMode.ROTATE_Z;
                } else if (SwingUtilities.isLeftMouseButton(event)) {
                    dragMode = DragMode.MOVE_AND_ROLL;
                } else if (SwingUtilities.isRightMouseButton(event)) {
                    dragMode = DragMode.ROTATE_XY;
                }
            }

            @Override
            public void mouseDragged(MouseEvent event) {
                int deltaX = event.getX() - previousMouseX;
                int deltaY = event.getY() - previousMouseY;

                previousMouseX = event.getX();
                previousMouseY = event.getY();

                if (dragMode == DragMode.MOVE_AND_ROLL) {
                    moveAndRoll(deltaX);
                } else if (dragMode == DragMode.MOVE_Y) {
                    moveY(deltaY);
                } else if (dragMode == DragMode.MOVE_Z) {
                    moveZ(deltaY);
                } else if (dragMode == DragMode.ROTATE_XY) {
                    rotateXY(deltaX, deltaY);
                } else if (dragMode == DragMode.ROTATE_Z) {
                    rotateZ(deltaX);
                }

                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent event) {
                dragMode = DragMode.NONE;
            }

            @Override
            public void mouseWheelMoved(MouseWheelEvent event) {
                changeZoom(event.getPreciseWheelRotation());
                repaint();
            }
        };

        addMouseListener(mouse);
        addMouseMotionListener(mouse);
        addMouseWheelListener(mouse);
    }

    private void moveAndRoll(int deltaPixels) {
        
        double distance = deltaPixels / PIXELS_PER_UNIT;

        
        positionX += distance;

        
        angleZ -= distance / ROLL_RADIUS;
        angleZ = normalizeAngle(angleZ);
    }

    private void moveY(int deltaY) {
        positionY -= deltaY / PIXELS_PER_UNIT;
    }

    private void moveZ(int deltaY) {
        positionZ -= deltaY / PIXELS_PER_UNIT;
    }

    private void rotateXY(int deltaX, int deltaY) {
        angleX += deltaY * ROTATION_SPEED;
        angleY += deltaX * ROTATION_SPEED;

        angleX = normalizeAngle(angleX);
        angleY = normalizeAngle(angleY);
    }

    private void rotateZ(int deltaX) {
        angleZ -= deltaX * ROTATION_SPEED;
        angleZ = normalizeAngle(angleZ);
    }
    private void changeZoom(double wheelRotation) {
        // Каждый шаг колеса умножает масштаб на один и тот же коэффициент.
        zoom *= Math.pow(ZOOM_STEP, -wheelRotation);
        zoom = Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, zoom));
    }

    // Оставляет угол в диапазоне от 0 до 2π радиан (0–360°).
    private double normalizeAngle(double angle) {
        double fullTurn = 2.0 * Math.PI;

        angle %= fullTurn;

        if (angle < 0) {
            angle += fullTurn;
        }

        return angle;
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        // Сначала Swing очищает область старого рисунка.
        super.paintComponent(graphics);

        // Копия Graphics2D позволяет менять настройки только для этого кадра.
        Graphics2D g2 = (Graphics2D) graphics.create();

        // Убирает ступеньки на наклонных линиях.
        g2.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );

        // Линия толщиной 1.2 пикселя с круглыми концами и стыками.
        g2.setStroke(new BasicStroke(
                1.2f,
                BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND
        ));

        drawAxes(g2);
        drawApple(g2);
        drawOrigin(g2);
        drawInformation(g2);

        g2.dispose();
    }

    private void drawAxes(Graphics2D g2) {
        drawAxis(g2, new Point3D(-4, 0, 0), new Point3D(4, 0, 0),
                new Color(210, 95, 95), "X");
        drawAxis(g2, new Point3D(0, -3.2, 0), new Point3D(0, 3.2, 0),
                new Color(95, 170, 225), "Y");
        drawAxis(g2, new Point3D(0, 0, -2.4), new Point3D(0, 0, 2.4),
                new Color(225, 175, 95), "Z");
    }

    private void drawAxis(Graphics2D g2, Point3D from, Point3D to,
                          Color color, String label) {
        Point2D start = project(from);
        Point2D end = project(to);
        int endX = (int) Math.round(end.getX());
        int endY = (int) Math.round(end.getY());

        g2.setColor(color);
        g2.drawLine(
                (int) Math.round(start.getX()),
                (int) Math.round(start.getY()),
                endX,
                endY
        );
        g2.fillOval(endX - 3, endY - 3, 6, 6);
        g2.drawString(label, endX + 7, endY - 7);
    }

    private void drawOrigin(Graphics2D g2) {
        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;
        g2.setColor(new Color(235, 235, 235));
        g2.fillOval(centerX - 3, centerY - 3, 6, 6);
        g2.drawString("O", centerX + 8, centerY - 8);
    }

    private void drawApple(Graphics2D g2) {
    
        Matrix modelMatrix =
            Matrix.translation(positionX, positionY, positionZ)
                    .multiply(Matrix.rotationZ(angleZ))
                    .multiply(Matrix.rotationY(angleY))
                    .multiply(Matrix.rotationX(angleX))
                    .multiply(Matrix.scale(zoom));

        List<Point2D> projected = new ArrayList<>();

        for (Point3D vertex : model.getVertices()) {
            Point3D transformed = modelMatrix.transform(vertex);
            projected.add(project(transformed));
        }

        g2.setColor(new Color(75, 210, 110));

        // Ребро хранит два индекса: находим соответствующие экранные точки.
        for (Edge edge : model.getEdges()) {
            Point2D start = projected.get(edge.getStartIndex());
            Point2D end = projected.get(edge.getEndIndex());

            g2.drawLine(
                    (int) Math.round(start.getX()),
                    (int) Math.round(start.getY()),
                    (int) Math.round(end.getX()),
                    (int) Math.round(end.getY())
            );
        }
    }
    private Point2D project(Point3D point) {
        Matrix viewportMatrix = Matrix.viewport(
                getWidth(),
                getHeight(),
                PIXELS_PER_UNIT
        );

        Matrix projectionToScreen =
                viewportMatrix.multiply(PROJECTION_MATRIX);

        Point3D screenPoint = projectionToScreen.transform(point);

        return new Point2D(
                screenPoint.getX(),
                screenPoint.getY()
        );
    }

    private void drawInformation(Graphics2D g2) {
        g2.setColor(new Color(230, 235, 240));

        g2.drawString("ЛКМ: движение по X с качением", 18, 28);
        g2.drawString(
                "ПКМ или Shift + ЛКМ: вращение на месте",
                18,
                49
        );
        g2.drawString("Колесо: масштаб", 18, 70);

        g2.setColor(new Color(150, 165, 180));
        g2.drawString(
                String.format(
                        "X = %.2f   угол = %.1f°   масштаб = %.0f%%",
                        positionX,
                        Math.toDegrees(angleZ),
                        zoom * 100
                ),
                18,
                94
        );
    }
}
