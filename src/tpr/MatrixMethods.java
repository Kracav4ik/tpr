package tpr;

import java.util.*;

import Jama.EigenvalueDecomposition;
import Jama.Matrix;

import javafx.util.Pair;
import java.util.Set;
import java.util.ArrayList;

public class MatrixMethods {

    public static ArrayList<RangeArithmetic> getRangeEigen(Matrix inputMin, Matrix inputMax) {
        ArrayList<RangeArithmetic> result = new ArrayList<>();
        for (int i = 0; i < inputMin.getRowDimension(); i++) {
            RangeArithmetic toAdd = new RangeArithmetic(1, 1);
            for (int j = 0; j < inputMin.getColumnDimension(); j++) {
                toAdd.mul(new RangeArithmetic(inputMin.get(i, j), inputMax.get(i, j)));
            }
            result.add(toAdd.invertPow(inputMin.getRowDimension()));
        }
        RangeArithmetic norm = new RangeArithmetic(0, 0);
        for (RangeArithmetic el : result) {
            norm.plus(el);
        }
        for (int k = 0; k < inputMin.getRowDimension(); k++) {
            result.set(k, result.get(k).divide(norm));
        }

        return result;
    }
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

    public static Matrix perturbMatrix(MatrixModel input, double perturbPercent, boolean isMin) {
        int size = input.getSize();

        Matrix A = new Matrix(size, size, 1);

        for (Pair<Integer, Integer> pair : input.getMatPairs()) {
            Integer k = pair.getKey();
            Integer v = pair.getValue();

            if (k >= size || v >= size) {
                continue;
            }

            double v1 = (1 + perturbPercent) * input.getMat().get(k, v);
            double v2 = (1 - perturbPercent) * input.getMat().get(k, v);
            double v3 = 1 / v1;
            double v4 = 1 / v2;
            if (isMin) {
                A.set(k, v, Math.min(v1, v2));
                A.set(v, k, Math.min(v3, v4));
            } else {
                A.set(k, v, Math.max(v1, v2));
                A.set(v, k, Math.max(v3, v4));
            }
        }

        return A;
    }
}
