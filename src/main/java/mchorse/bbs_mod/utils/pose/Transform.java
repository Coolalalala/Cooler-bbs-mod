package mchorse.bbs_mod.utils.pose;

import mchorse.bbs_mod.data.DataStorageUtils;
import mchorse.bbs_mod.data.IMapSerializable;
import mchorse.bbs_mod.data.types.MapType;
import mchorse.bbs_mod.utils.MathUtils;
import mchorse.bbs_mod.utils.interps.IInterp;
import mchorse.bbs_mod.utils.interps.Lerps;
import mchorse.bbs_mod.utils.joml.Matrices;
import org.joml.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.lang.Math.abs;

public class Transform implements IMapSerializable
{
    private static final Vector3f DEFAULT_SCALE = new Vector3f(1F, 1F, 1F);

    public static final Transform DEFAULT = new Transform();
    private static final Logger log = LoggerFactory.getLogger(Transform.class);

    public final Vector3f translate = new Vector3f();
    public final Vector3f scale = new Vector3f(DEFAULT_SCALE);
    public final Vector3f rotate = new Vector3f();
    public final Vector3f rotate2 = new Vector3f();

    public void lerp(Transform transform, float a)
    {
        this.translate.lerp(transform.translate, a);
        this.scale.lerp(transform.scale, a);
        this.rotate.lerp(transform.rotate, a);
        this.rotate2.lerp(transform.rotate2, a);
    }

    public void lerp(Transform preA, Transform a, Transform b, Transform postB, IInterp interp, float x)
    {
        this.lerp(this.translate, preA.translate, a.translate, b.translate, postB.translate, interp, x);
        this.lerp(this.scale, preA.scale, a.scale, b.scale, postB.scale, interp, x);
        this.lerp(this.rotate, preA.rotate, a.rotate, b.rotate, postB.rotate, interp, x);
        this.lerp(this.rotate2, preA.rotate2, a.rotate2, b.rotate2, postB.rotate2, interp, x);
    }

    /* Quaternion interpolation */
    private void interpolateQuat(Vector3f output, Vector3f preA, Vector3f a, Vector3f b, Vector3f postB, IInterp interp, float x) {
        // Convert to quaternions (minecraft uses left-handed system!!)
        Quaterniond startQuad = new Quaterniond().rotateXYZ(-a.x, a.y, a.z).normalize();
        Quaterniond endQuad = new Quaterniond().rotateXYZ(-b.x, b.y, b.z).normalize();
        double startRoll = startQuad.w;
        double endRoll = endQuad.w;

        Vector3d rotation = new Vector3d();
        Vector3d vector = new Vector3d();
        if (interp.getKey().equals("hermite") || interp.getKey().equals("cubic")) {
            Quaterniond preQuad = new Quaterniond().rotateXYZ(-preA.x, preA.y, preA.z);
            Quaterniond postQuad = new Quaterniond().rotateXYZ(-postB.x, postB.y, postB.z);
            double preRoll = preQuad.w;
            double postRoll = postQuad.w;
            // Interpolate and extract angles
            Lerps.squad(preQuad, startQuad, endQuad, postQuad, x);
        } else if (interp.getKey().equals("bezier")) {
            Quaterniond preQuad = new Quaterniond().rotateXYZ(-preA.x, preA.y, preA.z);
            Quaterniond postQuad = new Quaterniond().rotateXYZ(-postB.x, postB.y, postB.z);
            double preRoll = preQuad.w;
            double postRoll = postQuad.w;
            // Interpolate and extract angles
            Lerps.bezierQuat(preQuad, startQuad, endQuad, postQuad, x).getEulerAnglesXYZ(rotation);
        }
        else {
            // Interpolate
            double factor = interp.interpolate(IInterp.context.set(0, 0, 1, 1, x));
            Quaterniond result = new Quaterniond();
            startQuad.nlerpIterative(endQuad, factor, 0.005, result);
            // Extract Euler angles
            result.getEulerAnglesXYZ(rotation);
        }

        // Return the rotation
        output.set(-(float) rotation.x, (float) rotation.y, (float) rotation.z);
    }

    private void lerpQuat(Vector3f target, Vector3f preA, Vector3f a, Vector3f b, Vector3f postB, IInterp interp, float x) {
        // Convert to quaternions (minecraft uses left-handed system!!)
        Quaterniond startQuad = new Quaterniond().rotateXYZ(-a.x, a.y, a.z);
        Quaterniond endQuad = new Quaterniond().rotateXYZ(-b.x, b.y, b.z);
        Quaterniond preQuad = new Quaterniond().rotateXYZ(preA.x, preA.y, preA.z);
        Quaterniond postQuad = new Quaterniond().rotateXYZ(postB.x, postB.y, postB.z);
        Quaterniond result = new Quaterniond();

        result.x = interp.interpolate(IInterp.context.set(preQuad.x, startQuad.x, endQuad.x, postQuad.x, x));
        result.y = interp.interpolate(IInterp.context.set(preQuad.y, startQuad.y, endQuad.y, postQuad.y, x));
        result.z = interp.interpolate(IInterp.context.set(preQuad.z, startQuad.z, endQuad.z, postQuad.z, x));
        result.w = interp.interpolate(IInterp.context.set(preQuad.w, startQuad.w, endQuad.w, postQuad.w, x));

        // Conver back to Euler
        Vector3d rotation = new Vector3d();
        result.getEulerAnglesXYZ(rotation);
        target.set(-(float) rotation.x, (float) rotation.y, (float) rotation.z);
    }

    private void lerp(Vector3f target, Vector3f preA, Vector3f a, Vector3f b, Vector3f postB, IInterp interp, float x)
    {
        target.x = (float) interp.interpolate(IInterp.context.set(preA.x, a.x, b.x, postB.x, x));
        target.y = (float) interp.interpolate(IInterp.context.set(preA.y, a.y, b.y, postB.y, x));
        target.z = (float) interp.interpolate(IInterp.context.set(preA.z, a.z, b.z, postB.z, x));
    }

    public void identity()
    {
        this.translate.set(0, 0, 0);
        this.scale.set(1, 1, 1);
        this.rotate.set(0, 0, 0);
        this.rotate2.set(0, 0, 0);
    }

    public Matrix3f createRotationMatrix()
    {
        Matrix3f matrix = new Matrix3f();

        matrix.rotateZ(this.rotate.z);
        matrix.rotateY(this.rotate.y);
        matrix.rotateX(this.rotate.x);
        matrix.rotateZ(this.rotate2.z);
        matrix.rotateY(this.rotate2.y);
        matrix.rotateX(this.rotate2.x);

        return matrix;
    }

    public Matrix4f createMatrix()
    {
        return this.setupMatrix(Matrices.TEMP_4F.identity());
    }

    public Matrix4f setupMatrix(Matrix4f matrix)
    {
        matrix.translate(this.translate);
        matrix.rotateZ(this.rotate.z);
        matrix.rotateY(this.rotate.y);
        matrix.rotateX(this.rotate.x);
        matrix.rotateZ(this.rotate2.z);
        matrix.rotateY(this.rotate2.y);
        matrix.rotateX(this.rotate2.x);
        matrix.scale(this.scale);

        return matrix;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (super.equals(obj))
        {
            return true;
        }

        if (obj instanceof Transform)
        {
            Transform transform = (Transform) obj;

            return this.translate.equals(transform.translate)
                && this.scale.equals(transform.scale)
                && this.rotate.equals(transform.rotate)
                && this.rotate2.equals(transform.rotate2);
        }

        return false;
    }

    public Transform copy()
    {
        Transform transform = new Transform();

        transform.copy(this);

        return transform;
    }

    public void copy(Transform transform)
    {
        this.translate.set(transform.translate);
        this.scale.set(transform.scale);
        this.rotate.set(transform.rotate);
        this.rotate2.set(transform.rotate2);
    }

    public boolean isDefault()
    {
        return this.equals(DEFAULT);
    }

    public void toRad()
    {
        this.rotate.x = MathUtils.toRad(this.rotate.x);
        this.rotate.y = MathUtils.toRad(this.rotate.y);
        this.rotate.z = MathUtils.toRad(this.rotate.z);
        this.rotate2.x = MathUtils.toRad(this.rotate2.x);
        this.rotate2.y = MathUtils.toRad(this.rotate2.y);
        this.rotate2.z = MathUtils.toRad(this.rotate2.z);
    }

    @Override
    public void toData(MapType data)
    {
        if (!this.isDefault())
        {
            data.put("t", DataStorageUtils.vector3fToData(this.translate));
            data.put("s", DataStorageUtils.vector3fToData(this.scale));
            data.put("r", DataStorageUtils.vector3fToData(this.rotate));
            data.put("r2", DataStorageUtils.vector3fToData(this.rotate2));
        }
    }

    @Override
    public void fromData(MapType data)
    {
        this.identity();

        this.translate.set(DataStorageUtils.vector3fFromData(data.getList("t")));
        this.scale.set(DataStorageUtils.vector3fFromData(data.getList("s"), DEFAULT_SCALE));
        this.rotate.set(DataStorageUtils.vector3fFromData(data.getList("r")));
        this.rotate2.set(DataStorageUtils.vector3fFromData(data.getList("r2")));
    }
}