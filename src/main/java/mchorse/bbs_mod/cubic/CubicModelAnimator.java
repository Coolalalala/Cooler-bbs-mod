package mchorse.bbs_mod.cubic;

import mchorse.bbs_mod.cubic.data.animation.Animation;
import mchorse.bbs_mod.cubic.data.animation.AnimationPart;
import mchorse.bbs_mod.cubic.data.model.Model;
import mchorse.bbs_mod.cubic.data.model.ModelGroup;
import mchorse.bbs_mod.math.molang.expressions.MolangExpression;
import mchorse.bbs_mod.utils.interps.IInterp;
import mchorse.bbs_mod.utils.interps.Interpolation;
import mchorse.bbs_mod.utils.interps.Interpolations;
import mchorse.bbs_mod.utils.interps.Lerps;
import mchorse.bbs_mod.utils.keyframes.BezierUtils;
import mchorse.bbs_mod.utils.keyframes.KeyframeChannel;
import mchorse.bbs_mod.utils.keyframes.KeyframeSegment;
import mchorse.bbs_mod.utils.pose.Transform;
import org.joml.Quaterniond;
import org.joml.Vector3d;

public class CubicModelAnimator
{
    private static Vector3d p = new Vector3d();
    private static Vector3d s = new Vector3d();
    private static Vector3d r = new Vector3d();

    public static Vector3d interpolateList(Vector3d output, KeyframeChannel<MolangExpression> x, KeyframeChannel<MolangExpression> y, KeyframeChannel<MolangExpression> z, float frame, double defaultValue)
    {
        output.x = interpolateSegment(x.findSegment(frame), defaultValue);
        output.y = interpolateSegment(y.findSegment(frame), defaultValue);
        output.z = interpolateSegment(z.findSegment(frame), defaultValue);

        return output;
    }

    public static Vector3d interpolateQuaternion(Vector3d output, KeyframeChannel<MolangExpression> x, KeyframeChannel<MolangExpression> y, KeyframeChannel<MolangExpression> z, float frame, double defaultValue) {
        // Find segments for each axis at the given frame
        KeyframeSegment<MolangExpression> segX = x.findSegment(frame);
        KeyframeSegment<MolangExpression> segY = y.findSegment(frame);
        KeyframeSegment<MolangExpression> segZ = z.findSegment(frame);

        if (segX == null || segY == null || segZ == null) {
            return new Vector3d(defaultValue);
        }
        // Snatch rotations
        double startX = segX.a.getValue().get();
        double startY = segY.a.getValue().get();
        double startZ = segZ.a.getValue().get();

        double endX = segX.b.getValue().get();
        double endY = segY.b.getValue().get();
        double endZ = segZ.b.getValue().get();


        Interpolation interp = segX.b.getInterpolation();
        // Convert to quaternions
        Quaterniond startQuad = new Quaterniond().rotateXYZ(startX, startY, startZ);
        Quaterniond endQuad = new Quaterniond().rotateXYZ(endX, endY, endZ);

        // Interpolate rotations
        if (interp.getInterp() == Interpolations.BEZIER || interp.getInterp() == Interpolations.HERMITE || interp.getInterp() == Interpolations.CUBIC) {
            BezierUtils.getQuaternion(startQuad, endQuad,
                    segX.a.getTick(), segX.b.getTick(),
                    segX.a.rx, segX.a.ry,
                    segX.b.lx, segX.b.ly,
                    segX.x
            ).getEulerAnglesXYZ(output);
            // Return euler
            return output;
        }
        double factor = interp.interpolate(IInterp.context.set(0, 0, 1, 0, segX.x));
        startQuad.slerp(endQuad, factor);

        // Convert back to euler
        startQuad.getEulerAnglesXYZ(output);
        return output;
    }


    private static double interpolateSegment(KeyframeSegment<MolangExpression> segment, double defaultValue)
    {
        if (segment == null)
        {
            return defaultValue;
        }

        double start = segment.a.getValue().get();
        double destination = segment.b.getValue().get();

        if (segment.b.getInterpolation().getInterp() == Interpolations.BEZIER)
        {
            return BezierUtils.get(start, destination,
                segment.a.getTick(), segment.b.getTick(),
                segment.a.rx, segment.a.ry,
                segment.b.lx, segment.b.ly,
                segment.x
            );
        }

        double pre = segment.preA.getValue().get();
        double post = segment.postB.getValue().get();

        return segment.b.getInterpolation().interpolate(IInterp.context.set(pre, start, destination, post, segment.x));
    }

    public static void animate(Model model, Animation animation, float frame, float blend, boolean skipInitial)
    {
        for (ModelGroup group : model.topGroups)
        {
            animateGroup(group, animation, frame, blend, skipInitial);
        }
    }

    private static void animateGroup(ModelGroup group, Animation animation, float frame, float blend, boolean skipInitial)
    {
        boolean applied = false;

        AnimationPart part = animation.parts.get(group.id);

        if (part != null)
        {
            applyGroupAnimation(group, part, frame, blend);

            applied = true;
        }

        if (!applied && !skipInitial)
        {
            Transform initial = group.initial;
            Transform current = group.current;

            current.translate.lerp(initial.translate, blend);
            current.scale.lerp(initial.scale, blend);

            current.rotate.x = (float) Lerps.lerpYaw(current.rotate.x, initial.rotate.x, blend);
            current.rotate.y = (float) Lerps.lerpYaw(current.rotate.y, initial.rotate.y, blend);
            current.rotate.z = (float) Lerps.lerpYaw(current.rotate.z, initial.rotate.z, blend);
        }

        for (ModelGroup childGroup : group.children)
        {
            animateGroup(childGroup, animation, frame, blend, skipInitial);
        }
    }

    private static void applyGroupAnimation(ModelGroup group, AnimationPart animation, float frame, float blend)
    {
        // Interpolation
        Vector3d position = interpolateList(p, animation.x, animation.y, animation.z, frame, 0D);
        Vector3d scale = interpolateList(s, animation.sx, animation.sy, animation.sz, frame, 1D);
        Vector3d rotation = interpolateList(r, animation.rx, animation.ry, animation.rz, frame, 0D);
        // Vector3d rotation = interpolateQuaternion(r, animation.rx, animation.ry, animation.rz, frame, 0D);

        scale.sub(1, 1, 1);

        rotation.x *= -1;
        rotation.y *= -1;

        Transform initial = group.initial;
        Transform current = group.current;

        // Blend/hold between multiple poses?
        current.translate.x = Lerps.lerp(current.translate.x, (float) position.x + initial.translate.x, blend);
        current.translate.y = Lerps.lerp(current.translate.y, (float) position.y + initial.translate.y, blend);
        current.translate.z = Lerps.lerp(current.translate.z, (float) position.z + initial.translate.z, blend);

        current.scale.x = Lerps.lerp(current.scale.x, (float) scale.x + initial.scale.x, blend);
        current.scale.y = Lerps.lerp(current.scale.y, (float) scale.y + initial.scale.y, blend);
        current.scale.z = Lerps.lerp(current.scale.z, (float) scale.z + initial.scale.z, blend);

        current.rotate.x = (float) Lerps.lerpYaw(current.rotate.x, (float) rotation.x + initial.rotate.x, blend);
        current.rotate.y = (float) Lerps.lerpYaw(current.rotate.y, (float) rotation.y + initial.rotate.y, blend);
        current.rotate.z = (float) Lerps.lerpYaw(current.rotate.z, (float) rotation.z + initial.rotate.z, blend);
    }
}
