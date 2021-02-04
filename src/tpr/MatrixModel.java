package tpr;

import Jama.Matrix;
import javafx.util.Pair;

import java.util.HashSet;
import java.util.Set;

public class MatrixModel {
    private Set<Pair<Integer, Integer>> matPairs;
    private Matrix mat;
    private Integer size;

    public void setMat(Matrix newMat) {
        matPairs = MatrixMethods.getUpPairs(10);
        size = newMat.getRowDimension();
        for (int i = 0; i < newMat.getRowDimension(); i++) {
            for (int j = i + 1; j < newMat.getColumnDimension(); j++) {
                double val = newMat.get(i, j);

                if (val >= 1) {
                    updatePair(new Pair<>(i, j));
                    mat.set(i, j, val);
                    mat.set(j, i, 1 / val);
                } else {
                    updatePair(new Pair<>(j, i));
                    val = newMat.get(j, i);
                    mat.set(j, i, val);
                    mat.set(i, j, 1 / val);
                }
            }
        }
    }

    public MatrixModel(Matrix mat, Integer size) {
        this.mat = new Matrix(10, 10, 1);
        setMat(mat);
        this.size = size;
    }

    public void updatePair(Pair<Integer, Integer> pair) {
        matPairs.remove(new Pair<>(pair.getValue(), pair.getKey()));
        matPairs.add(pair);
    }

    public Set<Pair<Integer, Integer>> getMatPairs() {
        return matPairs;
    }

    public Matrix getMat() {
        return mat.getMatrix(0, size - 1, 0, size - 1);
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public Integer getSize() {
        return size;
    }
}
