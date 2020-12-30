package tpr;

import java.util.*;

import Jama.EigenvalueDecomposition;
import Jama.Matrix;

public class MatrixMethods {
    public static Matrix generation(int beg, int end, int size) {
        Random random = new Random();
        // Для записи сгенерированных чисел
        ArrayList<Integer> generated = new ArrayList<>();
        // Генерируем список случайных чисел
        for (int i = 0; i < ((size)*(size-1)/2); i++){
            generated.add(beg + random.nextInt(end-beg+1));
        }
        // Заполняем матрицу сгенерированными числами
        int listnum = 0;
        Matrix A = new Matrix(size, size, 1);
        for (int i = 0; i < size; i++) {
            for (int j = i + 1; j < size; j++) {
                A.set (i , j, generated.get(listnum));
                A.set (j , i, 1.d/generated.get(listnum));
                listnum++;
            }
        }
        return A;
    }
    // Выводим максимальный собственный вектор
    public static Matrix getMaxEigenVec(Matrix input) {
        EigenvalueDecomposition eig = input.eig();
        Matrix eigs = eig.getD();
        int maxIdx = 0;
        for (int i = 1; i < input.getRowDimension(); i++) {
            double eigVal = eigs.get(i, i);
            if (eigVal > eigs.get(maxIdx, maxIdx)) {
                maxIdx = i;
            }
        }
        Matrix result = eig.getV().getMatrix(0, input.getRowDimension() - 1, maxIdx, maxIdx);
        double sum = 0;
        for (int i = 0; i < result.getRowDimension(); ++i) {
            sum += result.get(i, 0);
        }
        return result.times(1.0/sum);
    }
    // Выводим максимальное собственное число
    public static double getMaxEigenVal(Matrix input) {
        Matrix eigs = input.eig().getD();
        double max = eigs.get(0, 0);
        for (int i = 1; i < input.getRowDimension(); i++) {
            double eig = eigs.get(i, i);
            if (eig > max) {
                max = eig;
            }
        }
        return max;
    }

    public static Matrix perturbMatrix(Matrix input, double perturbPercent) {
        Random random = new Random();
        int size = input.getRowDimension();

        Matrix A = new Matrix(size, size, 1);
        for (int i = 0; i < size; i++) {
            for (int j = i + 1; j < size; j++) {
                A.set(i, j, (1 + perturbPercent * (1 - 2*random.nextDouble())) * input.get(i, j));
            }
        }
        fixMatrix(A);
        return A;
    }

    public static void fixMatrix(Matrix input) {
        int size = input.getRowDimension();
        for (int i = 0; i < size; i++) {
            input.set(i, i, 1);
        }
        for (int i = 0; i < size; i++) {
            for (int j = i + 1; j < size; j++) {
                input.set(j, i, 1.d / input.get(i, j));
            }
        }
    }

    public static Matrix meAndSdMatrix(Matrix input) {
        EigenvalueDecomposition eig = input.eig();
        Matrix eigenvec = eig.getV();
        System.out.println("eig.getV() ");
        eigenvec.print(1, 3);
        System.out.println("eig.getD() ");
        eig.getD().print(1, 3);

        System.out.println("getImagEigenvalues");

        for (double ei : eig.getImagEigenvalues()) {
            System.out.print(" ");
            System.out.print(ei);
        }

        System.out.println("");
        System.out.println("getRealEigenvalues");

        for (double ei : eig.getRealEigenvalues()) {
            System.out.print(" ");
            System.out.print(ei);
        }

        Matrix res = new Matrix(eigenvec.getRowDimension(),2);
        //Складываем и потом поделим на количество
        for (int i = 0; i < eigenvec.getRowDimension(); i++) {
            for (int j = 0; j < eigenvec.getColumnDimension(); j++) {
                res.set(i, 0, res.get(i, 0) + eigenvec.get(i, j));
                res.set(i, 1, res.get(i, 1) + Math.pow(eigenvec.get(i, j), 2));
            }
        }
        res = res.times(1.d/input.getRowDimension());

        // Считаем СКО во втором столбце
        for (int i = 0; i < res.getColumnDimension();i++) {
            double m = res.get(i, 1) - Math.pow(res.get(i, 0), 2);
            m = Math.sqrt(m);
            res.set(i, 1, m);
        }
        return res;
    }
}
