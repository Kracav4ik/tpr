package tpr;

public class RangeArithmetic {
    private double min;
    private double max;

    public double getMin() {
        return min;
    }

    public void setMin(double min) {
        this.min = min;
    }

    public double getMax() {
        return max;
    }

    public void setMax(double max) {
        this.max = max;
    }

    public RangeArithmetic(double min, double max) {
        this.min = min;
        this.max = max;
    }

    public RangeArithmetic plus(RangeArithmetic other) {
        min += other.min;
        max += other.max;
        return this;
    }

    public RangeArithmetic minus(RangeArithmetic other) {
        return plus(other.invert());
    }

    public RangeArithmetic invert() {
        double t = min;
        min = -max;
        max = -t;
        return this;
    }

    public RangeArithmetic mul(RangeArithmetic other) {
        double oldMin = Math.min(Math.min(min * other.min, min * other.max), Math.min(max * other.min, max * other.max));
        max = Math.max(Math.max(min * other.min, min * other.max), Math.max(max * other.min, max * other.max));
        min = oldMin;
        return this;
    }

    public RangeArithmetic divide(RangeArithmetic other) {
        min = Math.min(Math.min(min / other.min, min / other.max), Math.min(max / other.min, max / other.max));
        max = Math.max(Math.max(min / other.min, min / other.max), Math.max(max / other.min, max / other.max));
        return this;
    }

    public RangeArithmetic invertPow(double pow) {
        double pow1 = Math.pow(min, 1 / pow);
        double pow2 = Math.pow(max, 1 / pow);
        min = Math.min(pow1, pow2);
        max = Math.max(pow1, pow2);
        return this;
    }
}
