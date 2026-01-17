package mchorse.bbs_mod.particles.components.motion.integrator;

import mchorse.bbs_mod.math.molang.expressions.MolangExpression;
import mchorse.bbs_mod.particles.emitter.Particle;
import mchorse.bbs_mod.particles.emitter.ParticleEmitter;
import org.joml.Vector3d;
import org.joml.Vector3f;

import static mchorse.bbs_mod.particles.emitter.ParticleEmitter.dt;
import static mchorse.bbs_mod.particles.emitter.ParticleEmitter.halfdt;

public class ParticleRK4integrator {
    public static float sixthdt = dt/6;

    private static Vector3f acceleration(Particle particle, MolangExpression[] motionAcceleration, Vector3f velocity) {
        return new Vector3f(
            (float) motionAcceleration[0].get(),
            (float) motionAcceleration[1].get(),
            (float) motionAcceleration[2].get()
        ).sub(velocity.mul(particle.drag + particle.dragFactor));
    }

    private static void updateState(ParticleEmitter emitter, Vector3d pos, Vector3f vel, Vector3d vel_1, Vector3f accel_1, Vector3d tempd, Vector3f tempf, float step) {
        pos.get(tempd).add(vel_1.get(new Vector3d()).mul(step));
        vel.get(tempf).add(accel_1.get(new Vector3f()).mul(step));
        emitter.motionUpdate(tempd, tempf);
    }
    private static void updateStateParallel(Particle particle, Vector3d pos, Vector3f vel, Vector3d vel_1, Vector3f accel_1, Vector3d tempd, Vector3f tempf, float step) {
        pos.get(tempd).add(vel_1.get(new Vector3d()).mul(step));
        vel.get(tempf).add(accel_1.get(new Vector3f()).mul(step));
        particle.position.set(tempd);
        particle.speed.set(tempf);
    }

    public static void update(ParticleEmitter emitter, Particle particle, MolangExpression[] motionAcceleration) {
        // RK4 integration
        Vector3d[] kx = new Vector3d[4];
        Vector3f[] kv = new Vector3f[4];
        Vector3d position = new Vector3d(particle.position);
        Vector3f velocity = particle.getVelocity();
        Vector3d tempd = new Vector3d();
        Vector3f tempf = new Vector3f(velocity);

        // k1
        kx[0] = new Vector3d(velocity);
        kv[0] = acceleration(particle, motionAcceleration, tempf);
        if (!emitter.scheme.parallel) updateState(emitter, position, velocity, kx[0], kv[0], tempd, tempf, halfdt);
        else updateStateParallel(particle, position, velocity, kx[0], kv[0], tempd, tempf, dt);
        // k2
        kx[1] = new Vector3d(particle.getVelocity());
        kv[1] = acceleration(particle, motionAcceleration, tempf);
        if (!emitter.scheme.parallel) updateState(emitter, position, velocity, kx[1], kv[1], tempd, tempf, halfdt);
        else updateStateParallel(particle, position, velocity, kx[1], kv[1], tempd, tempf, dt);
        // k3
        kx[2] = new Vector3d(particle.getVelocity());
        kv[2] = acceleration(particle, motionAcceleration, tempf);
        if (!emitter.scheme.parallel) updateState(emitter, position, velocity, kx[2], kv[2], tempd, tempf, halfdt);
        else updateStateParallel(particle, position, velocity, kx[2], kv[2], tempd, tempf, dt);
        // k4
        kx[3] = new Vector3d(particle.getVelocity());
        kv[3] = acceleration(particle, motionAcceleration, tempf);
        if (!emitter.scheme.parallel) updateState(emitter, position, velocity, kx[3], kv[3], tempd, tempf, dt);
        else updateStateParallel(particle, position, velocity, kx[3], kv[3], tempd, tempf, dt);

        // x(t+dt) = x(t) + dt/6 * (k1 + 2k2 + 2k3 + k4)
        particle.position.set(position.add(
                kx[0].add(kx[1].mul(2)).add(kx[2].mul(2)).add(kx[3]).mul(sixthdt)
        ));
        particle.speed.set(velocity.add(
                kv[0].add(kv[1].mul(2)).add(kv[2].mul(2)).add(kv[3]).mul(sixthdt)
        ));
    }
}
