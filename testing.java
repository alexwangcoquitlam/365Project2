import java.util.Arrays;

public class testing {
    public static void main(String[] args) {
        int [][] result = new int[8][8];
        for (int u = 0; u <= 7; u++) {
            for (int v = 0; v <= 7; v++) {
                double temp = 0;
                double Cu;
                double Cv;
                int[][] f = {
                        { 20, 20, 20, 20, 20, 20, 20, 20 },
                        { 20, 20, 20, 20, 20, 20, 20, 20 },
                        { 80, 80, 80, 80, 80, 80, 80, 80 },
                        { 80, 80, 80, 80, 80, 80, 80, 80 },
                        { 140, 140, 140, 140, 140, 140, 140, 140 },
                        { 140, 140, 140, 140, 140, 140, 140, 140 },
                        { 200, 200, 200, 200, 200, 200, 200, 200 },
                        { 200, 200, 200, 200, 200, 200, 200, 200 }
                };
                if (u == 0)
                    Cu = (Math.sqrt(2)) / 2;
                else
                    Cu = 1;
                if (v == 0)
                    Cv = (Math.sqrt(2)) / 2;
                else
                    Cv = 1;

                for (int i = 0; i <= 7; i++) {
                    for (int j = 0; j <= 7; j++) {
                        temp += Math.cos(((2 * i + 1) * u * Math.PI) / 16) * Math.cos(((2 * j + 1) * v * Math.PI) / 16)
                                * f[i][j];
                    }
                }
                temp *= (Cu * Cv) / 4;
                int res = (int) Math.round(temp);

                result[u][v] = res;
            }
        }
        System.out.println(Arrays.toString(result));
    }
}