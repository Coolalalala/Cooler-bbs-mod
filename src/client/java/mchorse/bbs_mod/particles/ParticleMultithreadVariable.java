package mchorse.bbs_mod.particles;

import mchorse.bbs_mod.math.Variable;

import java.util.Map;

public class ParticleMultithreadVariable extends Variable {
    private final ThreadLocal<Double> doubleValue = ThreadLocal.withInitial(super::doubleValue);

    public ParticleMultithreadVariable(String name, double value) {
        super(name, value);
    }

    public ParticleMultithreadVariable(String name, String value) {
        super(name, value);
    }

    public ParticleMultithreadVariable(Variable variable) {
        super(variable.getName(), variable.doubleValue());
    }

    public ParticleMultithreadVariable init() {
        this.doubleValue.set(super.doubleValue());
        return this;
    }

    @Override
    public double doubleValue() {
        return this.doubleValue.get();
    }

    @Override
    public void set(double value) {
        super.set(value);
        this.doubleValue.set(value);
    }
}
