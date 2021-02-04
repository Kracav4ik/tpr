package tpr;

import java.util.*;

import Jama.EigenvalueDecomposition;
import Jama.Matrix;

import javafx.util.Pair;
import java.util.Set;

public class MatrixMethods {

    public static Set<Pair<Integer, Integer>> getUpPairs(Integer size) {
        Set<Pair<Integer, Integer>> res = new HashSet<>();

        for (int i = 0; i < size; i++) {
            for (int j = i + 1; j < size; j++) {
                res.add(new Pair<>(i, j));
            }
        }

        return res;
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

    public static Matrix perturbMatrix(MatrixModel input, double perturbPercent) {
        Random random = new Random();
        int size = input.getSize();

        Matrix A = new Matrix(size, size, 1);

        for (Pair<Integer, Integer> pair : input.getMatPairs()) {
            Integer k = pair.getKey();
            Integer v = pair.getValue();

            if (k >= size || v >= size) {
                continue;
            }

            A.set(k, v, (1 + perturbPercent) * input.getMat().get(k, v));
            A.set(v, k, 1 / A.get(k, v));
        }

        return A;
    }
}
