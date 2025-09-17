package mchorse.bbs_mod.particles;

import mchorse.bbs_mod.math.Variable;
import mchorse.bbs_mod.particles.emitter.Particle;
import mchorse.bbs_mod.particles.emitter.ParticleEmitter;
import org.joml.Matrix3f;
import org.joml.Vector3d;
import org.joml.Vector3f;

public class ParticleVariable extends Variable {
    public static ParticleEmitter emitter;

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
        Vector3d prevPosition = new Vector3d(particle.prevPosition);
        if (emitter != null) {
            if (particle.age != 0 && !(particle.relativePosition && particle.relativeRotation)) {
                Vector3d lastGlobal = emitter.lastGlobal;
                prevPosition.sub(lastGlobal);
            }
            if (!particle.relativePosition && particle.relativeRotation) {
                Matrix3f inverseRotation = new Matrix3f(particle.matrix).invert();
                Vector3f tempVec = new Vector3f();
                tempVec.set(prevPosition);
                inverseRotation.transform(tempVec);
                prevPosition.set(tempVec);
            }
        }
        return switch (name) {
            case "variable.particle_index" -> particle.index;
            case "variable.particle_age" -> particle.getAge(0);
            case "variable.particle_lifetime" -> particle.lifetime / 20.0;
            case "variable.particle_random_1" -> particle.random1;
            case "variable.particle_random_2" -> particle.random2;
            case "variable.particle_random_3" -> particle.random3;
            case "variable.particle_random_4" -> particle.random4;
            case "variable.particle_x" -> prevPosition.x;
            case "variable.particle_y" -> prevPosition.y;
            case "variable.particle_z" -> prevPosition.z;
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
