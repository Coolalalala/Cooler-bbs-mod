package mchorse.bbs_mod.particles.emitter;

import com.google.common.util.concurrent.AtomicDouble;
import com.mojang.blaze3d.systems.RenderSystem;
import mchorse.bbs_mod.BBSModClient;
import mchorse.bbs_mod.camera.Camera;
import mchorse.bbs_mod.graphics.texture.Texture;
import mchorse.bbs_mod.math.IExpression;
import mchorse.bbs_mod.math.Variable;
import mchorse.bbs_mod.particles.ParticleScheme;
import mchorse.bbs_mod.particles.ParticleVariable;
import mchorse.bbs_mod.particles.components.IComponentEmitterInitialize;
import mchorse.bbs_mod.particles.components.IComponentEmitterUpdate;
import mchorse.bbs_mod.particles.components.IComponentParticleInitialize;
import mchorse.bbs_mod.particles.components.IComponentParticleRender;
import mchorse.bbs_mod.particles.components.IComponentParticleUpdate;
import mchorse.bbs_mod.particles.components.motion.ParticleComponentMotion;
import mchorse.bbs_mod.resources.Link;
import mchorse.bbs_mod.utils.MathUtils;
import mchorse.bbs_mod.utils.interps.Lerps;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;
import org.joml.*;

import java.lang.Math;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ParticleEmitter
{
    public static float dt = 0.05F;
    public static float halfdt = dt / 2;

    public ParticleScheme scheme;
    public List<Particle> particles = new ArrayList<>();
    protected HashMap<Integer, Particle> particleIndexMap = new HashMap<>();
    public Map<String, IExpression> variables = new HashMap<>();
    public Map<String, ArrayList<AtomicDouble>> listVariables = new HashMap<>();

    public Link texture;
    public LivingEntity target;
    public World world;
    public boolean lit;

    public boolean running = true;
    private Particle uiParticle;

    /* Intermediate values */
    public Vector3d lastGlobal = new Vector3d();
    public Matrix3f rotation = new Matrix3f();
    public Vector3f velocity = new Vector3f();

    /* Runtime properties */
    public float spawnRemainder;
    public int index;
    public int age;
    public int lifetime;
    public boolean playing = true;
    public boolean paused;

    public float random1 = (float) Math.random();
    public float random2 = (float) Math.random();
    public float random3 = (float) Math.random();
    public float random4 = (float) Math.random();

    /* Camera properties */
    public float cYaw;
    public float cPitch;

    public double cX;
    public double cY;
    public double cZ;

    public float user1;
    public float user2;
    public float user3;
    public float user4;
    public float user5;
    public float user6;

    /* Cached variable references to avoid hash look-ups */
    private Variable varInitX;
    private Variable varInitY;
    private Variable varInitZ;
    private Variable varIndex;
    private Variable varAge;
    private Variable varLifetime;
    private Variable varRandom1;
    private Variable varRandom2;
    private Variable varRandom3;
    private Variable varRandom4;
    private Variable varPositionX;
    private Variable varPositionY;
    private Variable varPositionZ;
    private Variable varDisplacement;
    private Variable varVelocityX;
    private Variable varVelocityY;
    private Variable varVelocityZ;
    private Variable varVelocity;
    private Variable varOffset;
    private Variable varCollisions;
    private Variable varSubsteps;

    private Variable varEmitterAge;
    private Variable varEmitterLifetime;
    private Variable varEmitterRandom1;
    private Variable varEmitterRandom2;
    private Variable varEmitterRandom3;
    private Variable varEmitterRandom4;
    private Variable varEmitterUser1;
    private Variable varEmitterUser2;
    private Variable varEmitterUser3;
    private Variable varEmitterUser4;
    private Variable varEmitterUser5;
    private Variable varEmitterUser6;
    private Variable varEmitterPositionX;
    private Variable varEmitterPositionY;
    private Variable varEmitterPositionZ;
    private Variable varEmitterDisplacement;
    private Variable varEmitterRotationX;
    private Variable varEmitterRotationY;
    private Variable varEmitterRotationZ;
    private Variable varEmitterParticleCount;
    private Variable varEmitterVelocityX;
    private Variable varEmitterVelocityY;
    private Variable varEmitterVelocityZ;
    private Variable varEmitterVelocity;

    public double getAge()
    {
        return this.getAge(0);
    }

    public double getAge(float transition)
    {
        return !this.paused ? (this.age + transition) / 20.0 : this.age / 20.0;
    }

    public void setTarget(LivingEntity target)
    {
        this.target = target;
        this.world = target == null ? null : target.getWorld();
    }

    public void setWorld(World world)
    {
        this.world = world;
    }

    public void setScheme(ParticleScheme scheme)
    {
        this.scheme = scheme;

        if (this.scheme == null)
        {
            return;
        }

        this.lit = true;
        this.stop();
        this.start();

        this.setupVariables();
        this.setEmitterVariables(0);

        for (IComponentEmitterInitialize component : this.scheme.emitterInitializes)
        {
            component.apply(this);
        }
    }

    public void setUserVariables(float a, float b, float c, float d, float e, float f)
    {
        this.user1 = a;
        this.user2 = b;
        this.user3 = c;
        this.user4 = d;
        this.user5 = e;
        this.user6 = f;
    }

    /* Variable related code */
    public void transformPositions(Particle particle, Vector3d prevPosition, Vector3d position) {
        if (particle.age != 0 && !(particle.relativePosition && particle.relativeRotation)) {
            prevPosition.sub(this.lastGlobal);
            position.sub(this.lastGlobal);
        }
        if (!particle.relativePosition && particle.relativeRotation) {
            Matrix3f inverseRotation = new Matrix3f(particle.matrix).invert();
            Vector3f tempVec = new Vector3f();
            tempVec.set(prevPosition);
            inverseRotation.transform(tempVec);
            prevPosition.set(tempVec);
            tempVec.set(position);
            inverseRotation.transform(tempVec);
            position.set(tempVec);
        }
    }

    public void transformPosition(Particle particle, Vector3d position) {
        if (particle.age != 0 && !(particle.relativePosition && particle.relativeRotation)) {
            position.sub(this.lastGlobal);
        }
        if (!particle.relativePosition && particle.relativeRotation) {
            Matrix3f inverseRotation = new Matrix3f(particle.matrix).invert();
            Vector3f tempVec = new Vector3f();
            tempVec.set(position);
            inverseRotation.transform(tempVec);
            position.set(tempVec);
        }
    }

    public void setupVariables()
    {
        this.varInitX = this.scheme.parser.variables.get("variable.particle_init_x");
        this.varInitY = this.scheme.parser.variables.get("variable.particle_init_y");
        this.varInitZ = this.scheme.parser.variables.get("variable.particle_init_z");
        this.varIndex = this.scheme.parser.variables.get("variable.particle_index");
        this.varAge = this.scheme.parser.variables.get("variable.particle_age");
        this.varLifetime = this.scheme.parser.variables.get("variable.particle_lifetime");
        this.varRandom1 = this.scheme.parser.variables.get("variable.particle_random_1");
        this.varRandom2 = this.scheme.parser.variables.get("variable.particle_random_2");
        this.varRandom3 = this.scheme.parser.variables.get("variable.particle_random_3");
        this.varRandom4 = this.scheme.parser.variables.get("variable.particle_random_4");
        this.varPositionX = this.scheme.parser.variables.get("variable.particle_x");
        this.varPositionY = this.scheme.parser.variables.get("variable.particle_y");
        this.varPositionZ = this.scheme.parser.variables.get("variable.particle_z");
        this.varDisplacement = this.scheme.parser.variables.get("variable.particle_displacement");
        this.varVelocityX = this.scheme.parser.variables.get("variable.particle_vx");
        this.varVelocityY = this.scheme.parser.variables.get("variable.particle_vy");
        this.varVelocityZ = this.scheme.parser.variables.get("variable.particle_vz");
        this.varVelocity = this.scheme.parser.variables.get("variable.particle_velocity");
        this.varOffset = this.scheme.parser.variables.get("variable.particle_offset");
        this.varCollisions = this.scheme.parser.variables.get("variable.particle_collisions");
        this.varSubsteps = this.scheme.parser.variables.get("variable.particle_substeps");

        this.varEmitterAge = this.scheme.parser.variables.get("variable.emitter_age");
        this.varEmitterLifetime = this.scheme.parser.variables.get("variable.emitter_lifetime");
        this.varEmitterRandom1 = this.scheme.parser.variables.get("variable.emitter_random_1");
        this.varEmitterRandom2 = this.scheme.parser.variables.get("variable.emitter_random_2");
        this.varEmitterRandom3 = this.scheme.parser.variables.get("variable.emitter_random_3");
        this.varEmitterRandom4 = this.scheme.parser.variables.get("variable.emitter_random_4");
        this.varEmitterUser1 = this.scheme.parser.variables.get("variable.emitter_user_1");
        this.varEmitterUser2 = this.scheme.parser.variables.get("variable.emitter_user_2");
        this.varEmitterUser3 = this.scheme.parser.variables.get("variable.emitter_user_3");
        this.varEmitterUser4 = this.scheme.parser.variables.get("variable.emitter_user_4");
        this.varEmitterUser5 = this.scheme.parser.variables.get("variable.emitter_user_5");
        this.varEmitterUser6 = this.scheme.parser.variables.get("variable.emitter_user_6");
        this.varEmitterPositionX = this.scheme.parser.variables.get("variable.emitter_x");
        this.varEmitterPositionY = this.scheme.parser.variables.get("variable.emitter_y");
        this.varEmitterPositionZ = this.scheme.parser.variables.get("variable.emitter_z");
        this.varEmitterDisplacement = this.scheme.parser.variables.get("variable.emitter_displacement");
        this.varEmitterRotationX = this.scheme.parser.variables.get("variable.emitter_rx");
        this.varEmitterRotationY = this.scheme.parser.variables.get("variable.emitter_ry");
        this.varEmitterRotationZ = this.scheme.parser.variables.get("variable.emitter_rz");
        this.varEmitterParticleCount = this.scheme.parser.variables.get("variable.emitter_pcount");
        this.varEmitterVelocityX = this.scheme.parser.variables.get("variable.emitter_vx");
        this.varEmitterVelocityY = this.scheme.parser.variables.get("variable.emitter_vy");
        this.varEmitterVelocityZ = this.scheme.parser.variables.get("variable.emitter_vz");
        this.varEmitterVelocity = this.scheme.parser.variables.get("variable.emitter_velocity");
    }

    public void setParticleVariables(Particle particle, float transition)
    {
        this.scheme.particle = particle; // Which particle is currently processing

        // General variables
        if (this.varIndex != null) this.varIndex.set(particle.index);
        if (this.varAge != null) this.varAge.set(particle.getAge(transition));
        if (this.varLifetime != null) this.varLifetime.set(particle.lifetime / 20.0);
        if (this.varRandom1 != null) this.varRandom1.set(particle.random1);
        if (this.varRandom2 != null) this.varRandom2.set(particle.random2);
        if (this.varRandom3 != null) this.varRandom3.set(particle.random3);
        if (this.varRandom4 != null) this.varRandom4.set(particle.random4);

        // Transform
        Vector3d prevPosition = new Vector3d(particle.prevPosition);
        Vector3d position = new Vector3d(particle.position);
        this.transformPositions(particle, prevPosition, position);
        // Movements
        if (this.varInitX != null) this.varInitX.set(particle.initialPosition.x);
        if (this.varInitY != null) this.varInitY.set(particle.initialPosition.y);
        if (this.varInitZ != null) this.varInitZ.set(particle.initialPosition.z);
        if (this.varPositionX != null) this.varPositionX.set(Lerps.lerp(prevPosition.x, position.x, transition));
        if (this.varPositionY != null) this.varPositionY.set(Lerps.lerp(prevPosition.y, position.y, transition));
        if (this.varPositionZ != null) this.varPositionZ.set(Lerps.lerp(prevPosition.z, position.z, transition));
        if (this.varDisplacement != null) this.varDisplacement.set(prevPosition.length());
        if (this.varVelocityX != null) this.varVelocityX.set(particle.speed.x);
        if (this.varVelocityY != null) this.varVelocityY.set(particle.speed.y);
        if (this.varVelocityZ != null) this.varVelocityZ.set(particle.speed.z);
        if (this.varVelocity != null) this.varVelocity.set(particle.speed.length());
        if (this.varOffset != null) this.varOffset.set(particle.offset);
        if (this.varSubsteps != null) this.varSubsteps.set(particle.substeps);

        // Collisions
        if (this.varCollisions != null) this.varCollisions.set(particle.collisions);

        this.scheme.updateCurves();
    }

    public void motionUpdate(Vector3d pos, Vector3f vel) {
        positionUpdate(pos);
        velocityUpdate(vel);
    }
    public void positionUpdate(Vector3d pos) {
        if (this.scheme.parallel) return;
        pos = new Vector3d(pos); // Copy to avoid modifying the original input
        this.transformPosition(this.scheme.particle, pos);
        if (this.varPositionX != null) this.varPositionX.set(pos.x);
        if (this.varPositionY != null) this.varPositionY.set(pos.y);
        if (this.varPositionZ != null) this.varPositionZ.set(pos.z);
        if (this.varDisplacement != null) this.varDisplacement.set(pos.length());
    }
    public void velocityUpdate(Vector3f vel) {
        if (this.varVelocityX != null) this.varVelocityX.set(vel.x);
        if (this.varVelocityY != null) this.varVelocityY.set(vel.y);
        if (this.varVelocityZ != null) this.varVelocityZ.set(vel.z);
        if (this.varVelocity != null) this.varVelocity.set(vel.length());
    }

    public void setEmitterVariables(float transition)
    {
        this.scheme.emitter = this;

        if (this.varEmitterAge != null) this.varEmitterAge.set(this.getAge(transition));
        if (this.varEmitterLifetime != null) this.varEmitterLifetime.set(this.lifetime / 20.0);
        if (this.varEmitterRandom1 != null) this.varEmitterRandom1.set(this.random1);
        if (this.varEmitterRandom2 != null) this.varEmitterRandom2.set(this.random2);
        if (this.varEmitterRandom3 != null) this.varEmitterRandom3.set(this.random3);
        if (this.varEmitterRandom4 != null) this.varEmitterRandom4.set(this.random4);
        if (this.varEmitterUser1 != null) this.varEmitterUser1.set(this.user1);
        if (this.varEmitterUser2 != null) this.varEmitterUser2.set(this.user2);
        if (this.varEmitterUser3 != null) this.varEmitterUser3.set(this.user3);
        if (this.varEmitterUser4 != null) this.varEmitterUser4.set(this.user4);
        if (this.varEmitterUser5 != null) this.varEmitterUser5.set(this.user5);
        if (this.varEmitterUser6 != null) this.varEmitterUser6.set(this.user6);

        // Position
        if (this.varEmitterPositionX != null) this.varEmitterPositionX.set(this.lastGlobal.x);
        if (this.varEmitterPositionY != null) this.varEmitterPositionY.set(this.lastGlobal.y);
        if (this.varEmitterPositionZ != null) this.varEmitterPositionZ.set(this.lastGlobal.z);
        if (this.varEmitterDisplacement != null) this.varEmitterDisplacement.set(this.lastGlobal.length());
        Vector3f rot = this.rotation.getEulerAnglesZYX(new Vector3f());
        if (this.varEmitterRotationX != null) this.varEmitterRotationX.set(rot.x);
        if (this.varEmitterRotationY != null) this.varEmitterRotationY.set(rot.y);
        if (this.varEmitterRotationZ != null) this.varEmitterRotationZ.set(rot.z);
        if (this.varEmitterParticleCount != null) this.varEmitterParticleCount.set(this.particles.size());
        if (this.varEmitterVelocityX != null) this.varEmitterVelocityX.set(this.velocity.x);
        if (this.varEmitterVelocityY != null) this.varEmitterVelocityY.set(this.velocity.y);
        if (this.varEmitterVelocityZ != null) this.varEmitterVelocityZ.set(this.velocity.z);
        if (this.varEmitterVelocity != null) this.varEmitterVelocity.set(this.velocity.length());

        this.scheme.updateCurves();
    }

    public void parseVariables(Map<String, String> variables)
    {
        for (Map.Entry<String, String> entry : variables.entrySet())
        {
            this.parseVariable(entry.getKey(), entry.getValue());
        }
    }

    public void parseVariable(String name, String expression)
    {
        try
        {
            this.variables.put(name, this.scheme.parser.parse(expression));
        }
        catch (Exception e)
        {}
    }

    public void replaceVariables()
    {
        if (this.variables == null)
        {
            return;
        }

        for (Map.Entry<String, IExpression> entry : this.variables.entrySet())
        {
            Variable var = this.scheme.parser.variables.get(entry.getKey());

            if (var != null)
            {
                var.set(entry.getValue().get().doubleValue());
            }
        }
    }

    public void start()
    {
        if (this.playing)
        {
            return;
        }

        this.spawnRemainder = 0F;
        this.index = 0;
        this.age = 0;
        this.playing = true;
    }

    public void stop()
    {
        if (!this.playing)
        {
            return;
        }

        this.playing = false;

        this.random1 = (float) Math.random();
        this.random2 = (float) Math.random();
        this.random3 = (float) Math.random();
        this.random4 = (float) Math.random();
    }

    /**
     * Update this current emitter
     */
    public void update()
    {
        if (this.scheme == null)
        {
            return;
        }

        this.setEmitterVariables(0);

        for (IComponentEmitterUpdate component : this.scheme.emitterUpdates)
        {
            component.update(this);
        }

        this.setEmitterVariables(0);
        if (this.scheme.parallel) {
            ParticleVariable.emitter = this;
            this.updateParticlesParallel();
        } else {
            this.updateParticles();
        }

        if (!this.paused)
        {
            this.age += 1;
        }
    }

    /**
     * Update all particles
     */
    private void updateParticles()
    {
        Iterator<Particle> it = this.particles.iterator();

        while (it.hasNext())
        {
            Particle particle = it.next();

            this.updateParticle(particle);

            if (particle.isDead())
            {
                it.remove();
            }
        }
    }

    private void updateParticlesParallel() {
        this.particles = this.particles.parallelStream()
                .filter(particle -> {
                    this.updateParticleParallel(particle);
                    return !particle.isDead();
                })
                .collect(Collectors.toList());
    }

    /**
     * Update a single particle
     */
    private void updateParticle(Particle particle)
    {
        particle.update(this);
        this.setParticleVariables(particle, 0);

        this.updateComponents(particle);
    }

    public static final ThreadLocal<Particle> evaluationParticle = new ThreadLocal<>();
    public void updateParticleParallel(Particle particle) {
        particle.update(this);

        evaluationParticle.set(particle);
        scheme.updateCurves();
        this.updateComponents(particle);
        evaluationParticle.remove();
    }

    public void updateComponents(Particle particle) {
        for (IComponentParticleUpdate component : this.scheme.particleUpdates) {
            if (component instanceof ParticleComponentMotion) {
                // Process substeps for motion integration
                dt = 0.05F * this.scheme.timeScale / particle.substeps;
                halfdt = dt / 2;
                for (int i = 1; i < particle.substeps; i++) component.update(this, particle);
            }
            component.update(this, particle);
        }
    }

    public Particle getParticleByIndex(int index)
    {
        return this.particleIndexMap.getOrDefault(index, null);
    }

    /**
     * Spawn a particle
     */
    public void spawnParticle(float offset)
    {
        if (!this.running)
        {
            return;
        }

        this.particles.add(this.createParticle(offset));
    }

    /**
     * Create a new particle
     */
    private Particle createParticle(float offset)
    {
        Particle particle = new Particle(this.index, offset);

        this.index += 1;

        this.setParticleVariables(particle, offset);
        particle.setupMatrix(this);

        for (IComponentParticleInitialize component : this.scheme.particleInitializes)
        {
            component.apply(this, particle);
        }

        if (!particle.relativeRotation)
        {
            Vector3f vec = new Vector3f().set(particle.position);

            particle.matrix.transform(vec);
            particle.position.x = vec.x;
            particle.position.y = vec.y;
            particle.position.z = vec.z;
        }

        if (!(particle.relativePosition && particle.relativeRotation))
        {
            particle.position.add(this.lastGlobal);
            particle.initialPosition.add(this.lastGlobal);
        }

        particle.prevPosition.set(particle.position);
        particle.rotation = particle.initialRotation;
        particle.prevRotation = particle.rotation;

        this.particleIndexMap.put(index, particle);
        return particle;
    }

    /**
     * Render the particle on screen
     */
    public void renderUI(MatrixStack stack, float transition)
    {
        if (this.scheme == null)
        {
            return;
        }

        List<IComponentParticleRender> list = this.scheme.getComponents(IComponentParticleRender.class);

        if (!list.isEmpty())
        {
            this.bindTexture();

            if (this.uiParticle == null || this.uiParticle.isDead())
            {
                this.uiParticle = this.createParticle(0F);
            }

            this.rotation.identity();
            this.uiParticle.update(this);
            this.setEmitterVariables(transition);
            this.setParticleVariables(this.uiParticle, transition);

            Matrix4f matrix = stack.peek().getPositionMatrix();
            BufferBuilder builder = Tessellator.getInstance().getBuffer();

            builder.begin(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION_TEXTURE_COLOR);

            for (IComponentParticleRender render : list)
            {
                render.renderUI(this.uiParticle, builder, matrix, transition);
            }

            RenderSystem.setShader(GameRenderer::getPositionTexColorProgram);
            RenderSystem.disableCull();
            BufferRenderer.drawWithGlobalProgram(builder.end());
            RenderSystem.enableCull();
        }
    }

    /**
     * Render all the particles in this particle emitter
     */
    public void render(VertexFormat format, Supplier<ShaderProgram> program, MatrixStack stack, int overlay, float transition)
    {
        if (this.scheme == null)
        {
            return;
        }

        List<IComponentParticleRender> renders = this.scheme.particleRender;

        for (IComponentParticleRender component : renders)
        {
            component.preRender(this, transition);
        }

        if (!this.particles.isEmpty())
        {
            Matrix4f matrix = stack.peek().getPositionMatrix();
            BufferBuilder builder = Tessellator.getInstance().getBuffer();

            this.bindTexture();
            builder.begin(VertexFormat.DrawMode.TRIANGLES, format);

            for (Particle particle : this.particles)
            {
                this.setEmitterVariables(transition);
                if (this.scheme.parallel) {
                    evaluationParticle.set(particle);
                } else {
                    this.setParticleVariables(particle, transition);
                }

                for (IComponentParticleRender component : renders)
                {
                    component.render(this, format, particle, builder, matrix, overlay, transition);
                }
            }

            RenderSystem.setShader(program);
            // RenderSystem.disableBlend();
            RenderSystem.disableCull();
            BufferRenderer.drawWithGlobalProgram(builder.end());
            RenderSystem.enableCull();
        }

        for (IComponentParticleRender component : renders)
        {
            component.postRender(this, transition);
        }
    }

    private void bindTexture()
    {
        Texture texture = BBSModClient.getTextures().getTexture(this.texture == null ? this.scheme.texture : this.texture);

        BBSModClient.getTextures().bindTexture(texture);
    }

    public void setupCameraProperties(Camera camera)
    {
        this.cYaw = 180 - MathUtils.toDeg(camera.rotation.y);
        this.cPitch = MathUtils.toDeg(camera.rotation.x);
        this.cX = camera.position.x;
        this.cY = camera.position.y;
        this.cZ = camera.position.z;
    }
}