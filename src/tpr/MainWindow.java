package tpr;

import Jama.Matrix;
import javafx.util.Pair;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.stream.Stream;


public class MainWindow {
    private JPanel root;
    private JTable origTable;
    private JTable modifiedTable;
    private JButton saveMatrix;
    private JSpinner dimension;
    private JButton openMatrix;
    private JTable oldEigVec;
    private JTable newEigVec;
    private JButton perturb;
    private JSpinner perturbPercent;
    private JTextField lambdaOrig;
    private JTextField consistencyOrig;
    private JButton savePerturbMatrix;
    private MatrixModel mat;
    private MatrixModel matMin;
    private MatrixModel matMax;

    public MainWindow() {
        mat = new MatrixModel(new Matrix(10, 10, 1), 3);
        matMin = new MatrixModel(new Matrix(10, 10, 1), 3);
        matMax = new MatrixModel(new Matrix(10, 10, 1), 3);

        origTable.setDefaultRenderer(String.class, new DefaultTableCellRenderer() {
            final Color backgroundColor = getBackground();

            public Component getTableCellRendererComponent(
                    JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, column);

                if (isSelected) {
                    return c;
                }
                if (mat.getMatPairs().contains(new Pair<>(row, column))) {
                    c.setBackground(Color.green.darker());
                } else {
                    c.setBackground(backgroundColor);
                }
                return c;
            }
        });

        modifiedTable.setDefaultRenderer(String.class, new DefaultTableCellRenderer() {
            final Color backgroundColor = getBackground();

            public Component getTableCellRendererComponent(
                    JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, column);

                if (isSelected) {
                    return c;
                }
                if (mat.getMatPairs().contains(new Pair<>(row, column))) {
                    c.setBackground(Color.green.darker());
                } else {
                    c.setBackground(backgroundColor);
                }
                return c;
            }
        });

        SpinnerNumberModel dimensionModel = (SpinnerNumberModel)dimension.getModel();
        dimensionModel.setValue(3);
        dimensionModel.setMinimum(2);
        dimensionModel.setMaximum(10);

        SpinnerNumberModel perturbPercentModel = (SpinnerNumberModel)perturbPercent.getModel();
        NumberFormatter formatter = (NumberFormatter)((JSpinner.NumberEditor)perturbPercent.getEditor()).getTextField().getFormatter();
        formatter.setFormat(new DecimalFormat("#0.0'%'"));
        formatter.setValueClass(Double.class);
        perturbPercentModel.setValue(10.0);
        perturbPercentModel.setMinimum(0.1);
        perturbPercentModel.setMaximum(90.0);
        perturbPercentModel.setStepSize(0.1);

        lambdaOrig.setBorder(null);
        consistencyOrig.setBorder(null);

        dimension.addChangeListener(e -> {
            mat.setSize((Integer)dimension.getValue());
            matMin.setSize((Integer)dimension.getValue());
            matMax.setSize((Integer)dimension.getValue());
            calcResult(mat.getMat());
            clickAll();
        });

        openMatrix.addActionListener(e -> {
            JFileChooser fileopen = new JFileChooser(".");
            fileopen.setAcceptAllFileFilterUsed(false);
            int ret = fileopen.showOpenDialog(null);
            if (ret == JFileChooser.APPROVE_OPTION) {
                try (BufferedReader bf = Files.newBufferedReader(fileopen.getSelectedFile().toPath())) {
                    calcResult(Matrix.read(bf));
                    dimension.setValue(mat.getSize());
                    clickAll();
                } catch (IOException er) {
                    System.err.format("IOException: %s%n", er);
                }
            }
        });
        saveMatrix.addActionListener(e -> {
            JFileChooser fileopen = new JFileChooser(".");
            fileopen.setAcceptAllFileFilterUsed(false);
            int ret = fileopen.showSaveDialog(null);
            if (ret == JFileChooser.APPROVE_OPTION) {
                try {
                    String selectedFile = fileopen.getSelectedFile().getPath();
                    PrintWriter writer = new PrintWriter(selectedFile);
                    mat.getMat().print(writer, 1, 3);
                    writer.flush();
                } catch (IOException er) {
                    System.err.format("IOException: %s%n", er);
                }
            }
        });

        savePerturbMatrix.addActionListener(e -> {
            JFileChooser fileopen = new JFileChooser(".");
            fileopen.setAcceptAllFileFilterUsed(false);
            int ret = fileopen.showSaveDialog(null);
            if (ret == JFileChooser.APPROVE_OPTION) {
                try {
                    String selectedFile = fileopen.getSelectedFile().getPath();
                    PrintWriter writer = new PrintWriter(selectedFile);
                    int count = matMin.getSize();
                    StringBuilder result = new StringBuilder();
                    for (int i = 0; i < count; ++i) {
                        if (i != 0) result.append("\n");
                        for (int j = 0; j < count; j++) {
                            if (j != 0) result.append("\t");
                            double min = matMin.getMat().get(i, j);
                            double max = matMax.getMat().get(i, j);
                            if (min == max) {
                                result.append(String.format("%.3f", min));
                            } else {
                                result.append(String.format("%.3f-%.3f", min, max));
                            }
                        }
                    }
                    writer.write(result.toString());
                    writer.flush();
                } catch (IOException er) {
                    System.err.format("IOException: %s%n", er);
                }
            }
        });

        perturb.addActionListener(e -> {
            double perturb = (Double) perturbPercent.getValue() / 100;
            matMax.setMat(MatrixMethods.perturbMatrix(mat, perturb));
            matMin.setMat(MatrixMethods.perturbMatrix(mat, -perturb));
            Matrix matEigenMin = MatrixMethods.getMaxEigenVec(matMin.getMat());
            Matrix matEigenMax = MatrixMethods.getMaxEigenVec(matMax.getMat());
            newEigVec.setModel(getTableModel(matEigenMin, matEigenMax, false));
            modifiedTable.setModel(getTableModel(matMin.getMat(), matMax.getMat(), false));
        });
        calcResult(mat.getMat());
        clickAll();
    }

    private void clickAll() {
        perturb.doClick();
    }

    private void calcResult(Matrix input) {
        mat.setMat(input);
        double matMaxEigenVal = MatrixMethods.getMaxEigenVal(mat.getMat());
        Matrix matEigen = MatrixMethods.getMaxEigenVec(mat.getMat());
        oldEigVec.setModel(getTableModel(matEigen, matEigen, false));
        newEigVec.setModel(new DefaultTableModel());
        origTable.setModel(getTableModel(mat.getMat(), mat.getMat(), true));
        lambdaOrig.setText(String.format("%.3f", matMaxEigenVal));
        consistencyOrig.setText(String.format("%.3f", (matMaxEigenVal - mat.getSize()) / (mat.getSize() - 1)));
    }

    private static void setLocaleConstants() {
        UIManager.put("FileChooser.saveButtonText", "Сохранить");
        UIManager.put("FileChooser.saveButtonToolTipText", "Сохранить");
        UIManager.put("FileChooser.openButtonText", "Открыть");
        UIManager.put("FileChooser.openButtonToolTipText", "Открыть матрицу");
        UIManager.put("FileChooser.directoryOpenButtonText", "Открыть папку");
        UIManager.put("FileChooser.directoryOpenButtonToolTipText", "Открыть папку");
        UIManager.put("FileChooser.cancelButtonText", "Отмена");
        UIManager.put("FileChooser.cancelButtonToolTipText", "Отмена");

        UIManager.put("FileChooser.lookInLabelText", "Папка");
        UIManager.put("FileChooser.saveInLabelText", "Папка");
        UIManager.put("FileChooser.fileNameLabelText", "Имя файла");
        UIManager.put("FileChooser.filesOfTypeLabelText", "Тип файлов");

        UIManager.put("FileChooser.upFolderToolTipText", "На один уровень вверх");
        UIManager.put("FileChooser.newFolderToolTipText", "Создание новой папки");
        UIManager.put("FileChooser.listViewButtonToolTipText", "Список");
        UIManager.put("FileChooser.detailsViewButtonToolTipText", "Таблица");
        UIManager.put("FileChooser.fileNameHeaderText", "Имя");
        UIManager.put("FileChooser.fileSizeHeaderText", "Размер");
        UIManager.put("FileChooser.fileTypeHeaderText", "Тип");
        UIManager.put("FileChooser.fileDateHeaderText", "Изменен");
        UIManager.put("FileChooser.fileAttrHeaderText", "Атрибуты");

        UIManager.put("FileChooser.acceptAllFileFilterText", "Все файлы");
        UIManager.put("FileChooser.readOnly", true);
        UIManager.put("FileChooser.viewMenuButtonToolTipText", "Вид");
        UIManager.put("FileChooser.listViewActionLabelText", "Имя");
        UIManager.put("FileChooser.detailsViewActionLabelText", "Имя и детали");
        UIManager.put("FileChooser.openDialogTitleText", "Открыть матрицу");
        UIManager.put("FileChooser.refreshActionLabelText", "Обновить");
        UIManager.put("FileChooser.viewMenuLabelText", "Вид");
    }

    private TableModel getTableModel(Matrix resultMin, Matrix resultMax, boolean editable) {
        return new AbstractTableModel() {
            public int getColumnCount() { return resultMin.getColumnDimension(); }
            public int getRowCount() { return resultMin.getRowDimension();}
            public Object getValueAt(int row, int col) {
                double min = resultMin.get(row, col);
                double max = resultMax.get(row, col);

                if (min == max) {
                    return String.format("%.3f", min);
                } else {
                    return String.format("%.3f-%.3f", min, max);
                }
            }
            public Class<?> getColumnClass(int col) { return String.class; }
            public boolean isCellEditable(int row, int col) { return editable && row != col; }
            public void setValueAt(Object aValue, int row, int col) {
                double value = Double.parseDouble(aValue.toString().replace(",", "."));
                if (value <= 0) return;
                resultMin.set(row, col, value);
                resultMin.set(col, row, 1 / value);
                calcResult(resultMin);
                clickAll();
                fireTableCellUpdated(row, col);
                fireTableCellUpdated(col, row);
            }
        };
    }

    public static void main(String[] args) {
        setLocaleConstants();
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        JFrame frame = new JFrame("Чувствительность вектора приоритетов");
        frame.setContentPane(new MainWindow().root);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}
