package mchorse.bbs_mod.particles.components.motion;

import mchorse.bbs_mod.data.types.BaseType;
import mchorse.bbs_mod.data.types.MapType;
import mchorse.bbs_mod.math.molang.MolangException;
import mchorse.bbs_mod.math.molang.MolangParser;
import mchorse.bbs_mod.math.molang.expressions.MolangExpression;
import mchorse.bbs_mod.particles.ParticleUtils;
import mchorse.bbs_mod.particles.components.IComponentParticleUpdate;
import mchorse.bbs_mod.particles.components.ParticleComponentBase;
import mchorse.bbs_mod.particles.emitter.Particle;
import mchorse.bbs_mod.particles.emitter.ParticleEmitter;
import org.joml.Vector3f;

public class ParticleComponentMotionDynamic extends ParticleComponentMotion implements IComponentParticleUpdate
{
    public MolangExpression[] motionAcceleration = {MolangParser.ZERO, MolangParser.ZERO, MolangParser.ZERO};
    public MolangExpression motionDrag = MolangParser.ZERO;
    public MolangExpression rotationAcceleration = MolangParser.ZERO;
    public MolangExpression rotationDrag = MolangParser.ZERO;

    @Override
    protected void toData(MapType data)
    {
        data.put("linear_acceleration", ParticleUtils.vectorToList(this.motionAcceleration));

        if (!MolangExpression.isZero(this.motionDrag)) data.put("linear_drag_coefficient", this.motionDrag.toData());
        if (!MolangExpression.isZero(this.rotationAcceleration)) data.put("rotation_acceleration", this.rotationAcceleration.toData());
        if (!MolangExpression.isZero(this.rotationDrag)) data.put("rotation_drag_coefficient", this.rotationDrag.toData());
    }

    @Override
    public ParticleComponentBase fromData(BaseType data, MolangParser parser) throws MolangException
    {
        if (!data.isMap())
        {
            return super.fromData(data, parser);
        }

        MapType map = data.asMap();

        if (map.has("linear_acceleration"))
        {
            ParticleUtils.vectorFromList(map.getList("linear_acceleration"), this.motionAcceleration, parser);
        }

        if (map.has("linear_drag_coefficient")) this.motionDrag = parser.parseDataSilently(map.get("linear_drag_coefficient"));
        if (map.has("rotation_acceleration")) this.rotationAcceleration = parser.parseDataSilently(map.get("rotation_acceleration"));
        if (map.has("rotation_drag_coefficient")) this.rotationDrag = parser.parseDataSilently(map.get("rotation_drag_coefficient"));

        return super.fromData(map, parser);
    }

    @Override
    public void update(ParticleEmitter emitter, Particle particle)
    {
        float dt = 0.05F;
        float halfdt = 0.025F;
        float halfdt2 = 0.00125F;

        /* rotation */
        float rotationAcceleration = particle.rotationAcceleration * dt - particle.rotationDrag * particle.rotationVelocity;

        particle.rotationVelocity += rotationAcceleration * dt;
        particle.rotation = particle.initialRotation + particle.rotationVelocity * particle.age;

        /* Position */
        // Transform velocity into desired space
        Vector3f vecTemp = new Vector3f(particle.speed);
        if (particle.relativeVelocity)
        {
            if (particle.age == 0)
            {
                particle.matrix.transform(particle.speed);
            }
        }
        else if (particle.relativePosition || particle.relativeRotation)
        {
            particle.matrix.transform(vecTemp);
        }

        if (particle.age == 0)
        {
            vecTemp.mul(1F + particle.offset);
        }

        // if (!relativePosition && relativeRotation) vecTemp.mul(emitter.rotation);

        // Verlet: x(t+dt) = x(t) + v(t)*dt + 0.5*a(t)*dt²
        particle.position.x += vecTemp.x * dt + particle.acceleration.x * halfdt2;
        particle.position.y += vecTemp.y * dt + particle.acceleration.x * halfdt2;
        particle.position.z += vecTemp.z * dt + particle.acceleration.x * halfdt2;

        // Store previous acceleration
        Vector3f prevAccel = new Vector3f(particle.acceleration);
        // Calculate new acceleration
        particle.acceleration.x = (float) this.motionAcceleration[0].get();
        particle.acceleration.y = (float) this.motionAcceleration[1].get();
        particle.acceleration.z = (float) this.motionAcceleration[2].get();
        // Calculate drag
        vecTemp.set(particle.speed);
        vecTemp.mul(-(particle.drag + particle.dragFactor)); // drag factor means drag added after collision
        // Apply drag
        particle.acceleration.add(vecTemp);

        // Verlet: v(t+dt) = v(t) + 0.5*(a(t) + a(t+dt))*dt
        prevAccel.add(particle.acceleration);
        prevAccel.mul(halfdt);
        particle.speed.add(prevAccel);


        particle.drag = (float) this.motionDrag.get();

        particle.rotationAcceleration += (float) this.rotationAcceleration.get() * dt;
        particle.rotationDrag = (float) this.rotationDrag.get();
    }
}