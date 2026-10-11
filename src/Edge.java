// Ребро соединяет две вершины по их номерам в списке AppleModel.
public final class Edge {

    private final int startIndex;
    private final int endIndex;

    public Edge(int startIndex, int endIndex) {
        this.startIndex = startIndex;
        this.endIndex = endIndex;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public int getEndIndex() {
        return endIndex;
    }
}
