package mchorse.bbs_mod.particles.components.shape;

import mchorse.bbs_mod.particles.emitter.Particle;
import mchorse.bbs_mod.particles.emitter.ParticleEmitter;
import net.minecraft.util.math.Box;

public class ParticleComponentShapeEntityAABB extends ParticleComponentShapeBase
{
    @Override
    public void apply(ParticleEmitter emitter, Particle particle)
    {
        float centerX = (float) this.offset[0].get();
        float centerY = (float) this.offset[1].get();
        float centerZ = (float) this.offset[2].get();

        float w = 0;
        float h = 0;
        float d = 0;

        if (emitter.target != null)
        {
            Box box = emitter.target.getBoundingBox();

            w = (float) (box.maxX - box.minX);
            h = (float) (box.maxY - box.minY);
            d = (float) (box.maxZ - box.minZ);
        }

        particle.position.x = centerX + ((float) Math.random() - 0.5F) * w;
        particle.position.y = centerY + ((float) Math.random() - 0.5F) * h;
        particle.position.z = centerZ + ((float) Math.random() - 0.5F) * d;

        if (this.surface)
        {
            int roll = (int) (Math.random() * 6 * 100) % 6;

            if (roll == 0) particle.position.x = centerX + w / 2F;
            else if (roll == 1) particle.position.x = centerX - w / 2F;
            else if (roll == 2) particle.position.y = centerY + h / 2F;
            else if (roll == 3) particle.position.y = centerY - h / 2F;
            else if (roll == 4) particle.position.z = centerZ + d / 2F;
            else if (roll == 5) particle.position.z = centerZ - d / 2F;
        }

        this.direction.applyDirection(particle, centerX, centerY, centerZ);
    }
}
