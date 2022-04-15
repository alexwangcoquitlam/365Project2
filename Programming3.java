import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Programming3 {
    public static void main(String[] args) {
        if (args.length > 0) {
            System.out.println("Reading file from path: \033[0;34m" + args[0] + "\033[0;37m");
            try {
                Path newPath = Paths.get(args[0]);
                String fileString = Files.readString(newPath);
                String[] fileArr = fileString.split("[ \n]");

                fileArr[0] = fileArr[0].replaceAll("[^0-9.]", "");
                int N = Integer.parseInt(fileArr[0]);

                int[][] matrix = ReadMatrix(fileArr, N);
                System.gc();

                int[][] DCTMatrix = CalculateRowTransform(matrix, N);
                DCTMatrix = CalculateColumnTransform(DCTMatrix, N);
                System.out.println("\nThe row first transform result matrix is:");
                PrintMatrix(DCTMatrix);

                DCTMatrix = CalculateColumnTransform(matrix, N);
                DCTMatrix = CalculateRowTransform(DCTMatrix, N);
                System.out.println("\nThe column first transform result matrix is:");
                PrintMatrix(DCTMatrix);

            } catch (Exception ex) {
                System.out.println("File read error.");
                ex.printStackTrace();
            }
        } else {
            System.out.println("No input supplied.");
        }
        System.out.println("Exiting...");
    }

    private static int[][] ReadMatrix(String[] input, int N) {
        int[][] res = new int[N][N];
        int counter = 1;
        try {
            for (int i = 0; i < N; i++) {
                for (int j = 0; j < N; j++) {
                    input[counter] = input[counter].replaceAll("[^0-9.]", "");
                    res[i][j] = Integer.parseInt(input[counter]);
                    counter++;
                }
            }
        } catch (Exception ex) {
            System.out.println("Reading failed at iteration " + counter + ".");
            ex.printStackTrace();
        }
        return res;
    }

    private static int[][] CalculateRowTransform(int[][] input, int N) {
        int[][] result = new int[N][N];
        double coefficient = 2/Math.sqrt(N*N)*2;
        for (int u = 0; u < N; u++) {
            double temp = 0;
            double Cu;
            if (u == 0)
                Cu = (Math.sqrt(2)) / 2;
            else
                Cu = 1;
            for (int j = 0; j < N; j++) {
                for (int i = 0; i <= N-1; i++) {
                    temp += Math.cos(((2 * i + 1) * u * Math.PI) / (2*N)) * input[i][j];
                    
                }
                temp *= Cu*coefficient;

                result[u][j] = (int)Math.round(temp);

                temp = 0;
            }
        }
        return result;
    }

    private static int[][] CalculateColumnTransform(int[][] input, int N) {
        int[][] result = new int[N][N];
        double coefficient = 2/Math.sqrt(N*N)*2;
        for (int v = 0; v < N; v++) {
            double temp = 0;
            double Cv;
            if (v == 0)
                Cv = (Math.sqrt(2)) / 2;
            else
                Cv = 1;
            for (int u = 0; u < N; u++) {
                for (int j = 0; j < N; j++) {
                    temp += Math.cos(((2 * j + 1) * v * Math.PI) / (2*N)) * input[u][j];
                    
                }
                temp *= Cv*coefficient;

                result[u][v] = (int)Math.round(temp);

                temp = 0;
            }
        }

        return result;
    }

    private static void PrintMatrix(int[][] input) {
        int max = 0;
        boolean isNegative = false;

        for (int i = 0; i < input.length; i++) {
            for (int j = 0; j < input[0].length; j++) {
                if (Math.abs(input[i][j]) > max) {
                    max = Math.abs(input[i][j]);
                }
                if (input[i][j] < 0) {
                    isNegative = true;
                }
            }
        }
        String temp = Integer.toString(max);
        int maxWidth = temp.length();

        if (isNegative)
            maxWidth++;

        String format = "%" + maxWidth + "d";
        for (int i = 0; i < input.length; i++) {
            System.out.print("|");
            for (int j = 0; j < input[0].length; j++) {
                System.out.printf(format + " ", input[i][j]);
            }
            System.out.println("|");
        }

    }
}