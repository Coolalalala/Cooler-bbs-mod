package mchorse.bbs_mod.particles;

import mchorse.bbs_mod.math.Variable;
import mchorse.bbs_mod.particles.emitter.Particle;
import mchorse.bbs_mod.particles.emitter.ParticleEmitter;

public class ParticleVariable extends Variable {
    public ParticleVariable(String name, double value) {
        super(name, value);
    }

    public ParticleVariable(String name, String value) {
        super(name, value);
    }

    public ParticleVariable(String name) {
        super(name, getParticleVariableValue(name, ParticleEmitter.evaluationParticle.get()));
    }

    @Override
    public double doubleValue() {
        return getParticleVariableValue(this.getName(), ParticleEmitter.evaluationParticle.get());
    }

    private static double getParticleVariableValue(String name, Particle particle) {
        if (particle == null) {
            return 0.0;
        }
        name = name.replace("v.", "variable.");
        return switch (name) {
            case "variable.particle_index" -> particle.index;
            case "variable.particle_age" -> particle.getAge(0);
            case "variable.particle_lifetime" -> particle.lifetime / 20.0;
            case "variable.particle_random_1" -> particle.random1;
            case "variable.particle_random_2" -> particle.random2;
            case "variable.particle_random_3" -> particle.random3;
            case "variable.particle_random_4" -> particle.random4;
            case "variable.particle_x" -> particle.prevPosition.x;
            case "variable.particle_y" -> particle.prevPosition.y;
            case "variable.particle_z" -> particle.prevPosition.z;
            case "variable.particle_displacement" -> particle.position.length();
            case "variable.particle_vx" -> particle.speed.x;
            case "variable.particle_vy" -> particle.speed.y;
            case "variable.particle_vz" -> particle.speed.z;
            case "variable.particle_velocity" -> particle.speed.length();
            case "variable.particle_offset" -> particle.offset;
            case "variable.particle_collisions" -> particle.collisions;
            default -> 0.0;
        };
    }
}
