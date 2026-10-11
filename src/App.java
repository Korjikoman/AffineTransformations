import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public final class App {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(App::createWindow);
    }

    private static void createWindow() {
        // Модель хранит 3D-точки и рёбра; панель превращает их в рисунок.
        AppleModel model = new AppleModel();
        ApplePanel panel = new ApplePanel(model);

        JFrame window = new JFrame("3D-проволочное яблоко");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setContentPane(panel);
        // Размер окна берётся из предпочтительного размера панели.
        window.pack();
        window.setLocationRelativeTo(null);
        window.setVisible(true);

        System.out.println("Вершин: " + model.getVertices().size());
        System.out.println("Рёбер: " + model.getEdges().size());
    }
}
