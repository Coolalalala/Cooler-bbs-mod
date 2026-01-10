package mchorse.bbs_mod.particles.components.motion.integrator;

import mchorse.bbs_mod.math.molang.expressions.MolangExpression;
import mchorse.bbs_mod.particles.emitter.Particle;
import mchorse.bbs_mod.particles.emitter.ParticleEmitter;
import org.joml.Vector3f;

import static mchorse.bbs_mod.particles.emitter.ParticleEmitter.dt;
import static mchorse.bbs_mod.particles.emitter.ParticleEmitter.halfdt;

public class ParticleVerletIntegrator {
    static private void halfStepVelocity(ParticleEmitter emitter, Particle particle, Vector3f temp) {
        temp.set(particle.acceleration);
        temp.mul(halfdt);
        particle.speed.add(temp);
        emitter.velocityUpdate(particle.speed);
    }
    static private void stepPosition(ParticleEmitter emitter, Particle particle, Vector3f temp, Vector3f velocity) {
        getVelocity(particle, velocity);
        temp.set(velocity);
        temp.mul(dt);
        particle.position.add(temp);
        emitter.positionUpdate(particle.position);
    }
    static private void stepAcceleration(Particle particle, Vector3f velocity, MolangExpression[] motionAcceleration) {
        // Calculate new acceleration
        particle.acceleration.x = (float) motionAcceleration[0].get();
        particle.acceleration.y = (float) motionAcceleration[1].get();
        particle.acceleration.z = (float) motionAcceleration[2].get();
        // Calculate drag
        velocity.mul(particle.drag + particle.dragFactor); // drag factor = drag added after collision
        // Apply drag
        particle.acceleration.sub(velocity);
    }
    static private void getVelocity(Particle particle, Vector3f velocity) {
        velocity.set(particle.speed);
        // Transform velocity into desired space
        if (particle.relativeVelocity)
        {
            if (particle.age == 0)
            {
                particle.matrix.transform(particle.speed);
            }
        }
        else if (particle.relativePosition || particle.relativeRotation)
        {
            particle.matrix.transform(velocity);
        }

        if (particle.age == 0)
        {
            velocity.mul(1F + particle.offset);
        }
    }

    public static void update(ParticleEmitter emitter, Particle particle, MolangExpression[] motionAcceleration) {
        // Velocity verlet integration
        Vector3f temp = new Vector3f();
        Vector3f velocity = new Vector3f();
        // v(t+dt/2) = v(t) + a(t)dt/2
        halfStepVelocity(emitter, particle, temp);
        // x(t+dt) = x(t) + v(t+dt/2)dt
        stepPosition(emitter, particle, temp, velocity);
        // calculate a(t+dt)
        stepAcceleration(particle, velocity, motionAcceleration);
        // v(t+dt) = v(t+dt/2) + a(t+dt)dt/2
        halfStepVelocity(emitter, particle, temp);
    }
}
