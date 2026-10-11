import java.util.ArrayList;
import java.util.List;

public final class AppleModel {

    // 20 промежутков по высоте дают 21 кольцо; в каждом по 36 точек.
    private static final int LATITUDE_STEPS = 20;
    private static final int LONGITUDE_STEPS = 36;

    private final List<Point3D> vertices = new ArrayList<>();
    private final List<Edge> edges = new ArrayList<>();

    public AppleModel() {
        createBodyVertices();
        createBodyEdges();
        createStem();
        createLeaf();
    }

    public List<Point3D> getVertices() {
        return vertices;
    }

    public List<Edge> getEdges() {
        return edges;
    }

    private void createBodyVertices() {
        // Сначала строим кольцо, затем все точки на этом кольце.
        for (int latitude = 0;
             latitude <= LATITUDE_STEPS;
             latitude++) {

            // v меняется от -π/2 (низ) до +π/2 (верх).
            double v = -Math.PI / 2.0
                    + Math.PI * latitude / LATITUDE_STEPS;

            // cos(v) сужает яблоко к полюсам; скобка задаёт его силуэт.
            double radius = Math.cos(v)
                    * (1.75
                    + 0.18 * Math.cos(2.0 * v)
                    + 0.10 * Math.sin(v));

            // Высота кольца; sin(2v) немного меняет профиль яблока.
            double y = 1.35 * Math.sin(v)
                    + 0.22 * Math.sin(2.0 * v);

            // На каждом уровне обходим полный круг от 0 до 2π.
            for (int longitude = 0;
                 longitude < LONGITUDE_STEPS;
                 longitude++) {

                double u = 2.0 * Math.PI
                        * longitude / LONGITUDE_STEPS;

                // Обычная формула окружности в плоскости XZ.
                double x = radius * Math.cos(u);
                double z = radius * Math.sin(u);

                // Точки добавляются по порядку: кольцо за кольцом.
                vertices.add(new Point3D(x, y, z));
            }
        }
    }

    private void createBodyEdges() {
        // Соединяем соседние точки одного кольца.
        // Крайние кольца пропускаем: их радиус почти нулевой.
        for (int latitude = 1;
             latitude < LATITUDE_STEPS;
             latitude++) {

            for (int longitude = 0;
                 longitude < LONGITUDE_STEPS;
                 longitude++) {

                int current = index(latitude, longitude);
                // % замыкает круг: после точки 35 идёт точка 0.
                int nextLongitude =
                        (longitude + 1) % LONGITUDE_STEPS;
                int next = index(latitude, nextLongitude);

                edges.add(new Edge(current, next));
            }
        }

        // Соединяем точки с одинаковой долготой на соседних кольцах.
        for (int latitude = 0;
             latitude < LATITUDE_STEPS;
             latitude++) {

            for (int longitude = 0;
                 longitude < LONGITUDE_STEPS;
                 longitude++) {

                int current = index(latitude, longitude);
                int upper = index(latitude + 1, longitude);

                edges.add(new Edge(current, upper));
            }
        }
    }

    private int index(int latitude, int longitude) {
        return latitude * LONGITUDE_STEPS + longitude;
    }

    private void createStem() {
        // Следующие вершины будут идти после всех вершин тела яблока.
        int first = vertices.size();

        vertices.add(new Point3D(0.00, 1.34, 0.00));
        vertices.add(new Point3D(0.05, 1.68, 0.02));
        vertices.add(new Point3D(0.12, 1.98, 0.04));

        edges.add(new Edge(first, first + 1));
        edges.add(new Edge(first + 1, first + 2));
    }

    private void createLeaf() {
        // Номера вершин листа снова начинаются с конца общего списка.
        int first = vertices.size();

        vertices.add(new Point3D(0.08, 1.76, 0.03));
        vertices.add(new Point3D(0.45, 2.08, 0.08));
        vertices.add(new Point3D(1.05, 1.98, 0.15));
        vertices.add(new Point3D(0.58, 1.72, 0.08));

        // Контур листа.
        edges.add(new Edge(first, first + 1));
        edges.add(new Edge(first + 1, first + 2));
        edges.add(new Edge(first + 2, first + 3));
        edges.add(new Edge(first + 3, first));

        // Центральная линия листа.
        edges.add(new Edge(first, first + 2));
    }
}
