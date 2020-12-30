package tpr;

import Jama.Matrix;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.text.NumberFormatter;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.util.ArrayList;


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
    private JButton testStability;
    private JSpinner iterationCount;
    private JTextField lambdaOrig;
    private JTextField consistencyOrig;
    private JTextField lambdaModified;
    private JTextField consistencyModified;
    private JTextField testReport;
    private JButton randomizeMatrix;
    private Matrix mat;

    public MainWindow() {
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

        SpinnerNumberModel iterationCountModel = (SpinnerNumberModel)iterationCount.getModel();
        iterationCountModel.setValue(100);
        iterationCountModel.setMinimum(1);
        iterationCountModel.setMaximum(9999);

        lambdaOrig.setBorder(null);
        consistencyOrig.setBorder(null);
        lambdaModified.setBorder(null);
        consistencyModified.setBorder(null);
        testReport.setBorder(null);

        randomizeMatrix.addActionListener(e -> {
            calcResult(MatrixMethods.generation(1, 9, (Integer) dimension.getValue()));
            clickAll();
        });
        openMatrix.addActionListener(e -> {
            JFileChooser fileopen = new JFileChooser(".");
            FileNameExtensionFilter filter = new FileNameExtensionFilter("Матрица(*.mat)", "mat");
            fileopen.setAcceptAllFileFilterUsed(false);
            fileopen.setFileFilter(filter);
            int ret = fileopen.showOpenDialog(null);
            if (ret == JFileChooser.APPROVE_OPTION) {
                try (BufferedReader bf = Files.newBufferedReader(fileopen.getSelectedFile().toPath())) {
                    calcResult(Matrix.read(bf));
                    dimension.setValue(mat.getRowDimension());
                    clickAll();
                } catch (IOException er) {
                    System.err.format("IOException: %s%n", er);
                }
            }
        });
        saveMatrix.addActionListener(e -> {
            JFileChooser fileopen = new JFileChooser(".");
            FileNameExtensionFilter filter = new FileNameExtensionFilter("Матрица(*.mat)", "mat");
            fileopen.setAcceptAllFileFilterUsed(false);
            fileopen.setFileFilter(filter);
            int ret = fileopen.showSaveDialog(null);
            if (ret == JFileChooser.APPROVE_OPTION) {
                try {
                    PrintWriter writer = new PrintWriter(fileopen.getSelectedFile());
                    mat.print(writer, 1, 3);
                    writer.flush();
                } catch (IOException er) {
                    System.err.format("IOException: %s%n", er);
                }
            }
        });

        perturb.addActionListener(e -> {
            Matrix result = MatrixMethods.perturbMatrix(mat, (Double) perturbPercent.getValue() / 100);
            Matrix resultEigen = MatrixMethods.getMaxEigenVec(result);
            newEigVec.setModel(getTableModel(resultEigen, false));
            double resultMaxEigenVal = MatrixMethods.getMaxEigenVal(result);
            modifiedTable.setModel(getTableModel(result, false));
            lambdaModified.setText(String.format("%.3f", resultMaxEigenVal));
            consistencyModified.setText(String.format("%.3f", ((resultMaxEigenVal - result.getRowDimension()) / (result.getRowDimension() - 1))));
        });

        testStability.addActionListener(e -> {
            Integer size = (Integer)iterationCount.getValue();
            double avg = 0;
            double sd = 0;
            Matrix matMaxEigen = MatrixMethods.getMaxEigenVec(mat);
            for (int i = 0; i < size; i++) {
                Matrix result = MatrixMethods.perturbMatrix(mat, (Double) perturbPercent.getValue() / 100);
                Matrix resultEigen = MatrixMethods.getMaxEigenVec(result);
                double diff = (matMaxEigen.minus(resultEigen)).normF();
                sd += diff * diff / size;
                avg += diff / size;
            }
            sd -= avg*avg;

            testReport.setText(String.format("Средняя норма отклонения вектора приоритетов %.3f%%, дисперсия %.3f%%", avg * 100, sd * 100));
        });

        randomizeMatrix.doClick();
    }

    private void clickAll() {
        perturb.doClick();
        testStability.doClick();
    }

    private void calcResult(Matrix input) {
        mat = input;
        MatrixMethods.fixMatrix(mat);
        double matMaxEigenVal = MatrixMethods.getMaxEigenVal(mat);
        Matrix matEigen = MatrixMethods.getMaxEigenVec(mat);
        oldEigVec.setModel(getTableModel(matEigen, false));
        newEigVec.setModel(new DefaultTableModel());
        modifiedTable.setModel(new DefaultTableModel());
        origTable.setModel(getTableModel(mat, true));
        lambdaOrig.setText(String.format("%.3f", matMaxEigenVal));
        consistencyOrig.setText(String.format("%.3f", (matMaxEigenVal - mat.getRowDimension()) / (mat.getRowDimension() - 1)));
        lambdaModified.setText("");
        consistencyModified.setText("");
        testReport.setText("");
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

    private TableModel getTableModel(Matrix result, boolean editable) {
        return new AbstractTableModel() {
            public int getColumnCount() { return result.getColumnDimension(); }
            public int getRowCount() { return result.getRowDimension();}
            public Object getValueAt(int row, int col) { return result.get(row, col); }
            public Class<?> getColumnClass(int col) { return Double.class; }
            public boolean isCellEditable(int row, int col) { return editable && col > row; }
            public void setValueAt(Object aValue, int row, int col) {
                result.set(row, col, (Double)aValue);
                MatrixMethods.fixMatrix(result);
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
