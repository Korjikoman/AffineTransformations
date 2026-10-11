public final class Matrix {

    private final double[][] values;

    private Matrix(double[][] values) {
        this.values = values;
    }

    public static Matrix viewport(double width,double height,double pixelsPerUnit
    ) {
        return new Matrix(new double[][]{
                {pixelsPerUnit,              0, 0, width / 2.0},
                {0,             -pixelsPerUnit, 0, height / 2.0},
                {0,                          0, 1,           0},
                {0,                          0, 0,           1}
        });
    }

    // Последний столбец прибавляет x, y, z к координатам точки.
    public static Matrix translation(double x, double y, double z) {
        return new Matrix(new double[][]{
                {1, 0, 0, x},
                {0, 1, 0, y},
                {0, 0, 1, z},
                {0, 0, 0, 1}
        });
    }

    // Числа на диагонали умножают x, y, z на scale.
    public static Matrix scale(double scale) {
        return new Matrix(new double[][]{
                {scale, 0,     0,     0},
                {0,     scale, 0,     0},
                {0,     0,     scale, 0},
                {0,     0,     0,     1}
        });
    }


    // x' = x·cos - y·sin; y' = x·sin + y·cos.
    public static Matrix rotationZ(double angle) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);

        return new Matrix(new double[][]{
                {cos, -sin, 0, 0},
                {sin,  cos, 0, 0},
                {0,      0, 1, 0},
                {0,      0, 0, 1}
        });
    }

    public static Matrix obliqueProjection(double depthX, double depthY) {
    return new Matrix(new double[][]{
            {1, 0, depthX, 0},
            {0, 1, depthY, 0},
            {0, 0,      0, 0},
            {0, 0,      0, 1}
        });
    }

    public static Matrix rotationX(double angle) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);

        return new Matrix(new double[][]{
                {1,   0,    0, 0},
                {0, cos, -sin, 0},
                {0, sin,  cos, 0},
                {0,   0,    0, 1}
        });
    }

    public static Matrix rotationY(double angle) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);

        return new Matrix(new double[][]{
                { cos, 0, sin, 0},
                {   0, 1,   0, 0},
                {-sin, 0, cos, 0},
                {   0, 0,   0, 1}
        });
    }

    public Matrix multiply(Matrix other) {
        double[][] result = new double[4][4];

        for (int row = 0; row < 4; row++) {
            for (int column = 0; column < 4; column++) {
                for (int k = 0; k < 4; k++) {
                    result[row][column] +=
                            values[row][k]
                                    * other.values[k][column];
                }
            }
        }

        return new Matrix(result);
    }


    public Point3D transform(Point3D point) {
        double[] source = {
            point.getX(),
            point.getY(),
            point.getZ(),
                1
        };

        double[] result = new double[4];

        for (int row = 0; row < 4; row++) {
            for (int column = 0; column < 4; column++) {
                result[row] +=
                        values[row][column] * source[column];
            }
        }

        return new Point3D(result[0], result[1], result[2]);
    }

}
