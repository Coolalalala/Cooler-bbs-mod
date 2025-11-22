package mchorse.bbs_mod.utils;

import org.joml.Quaterniond;
import org.joml.Quaterniondc;
import org.joml.Vector3d;
import org.joml.Vector3i;

import java.util.Collection;

public class MathUtils
{
    public static final float PI = (float) Math.PI;

    public static float toRad(float degrees)
    {
        return degrees / 180F * PI;
    }

    public static float toDeg(float rad)
    {
        return rad / PI * 180F;
    }

    public static int clamp(int x, int min, int max)
    {
        return x < min ? min : (x > max ? max : x);
    }

    public static float clamp(float x, float min, float max)
    {
        return x < min ? min : (x > max ? max : x);
    }

    public static double clamp(double x, double min, double max)
    {
        return x < min ? min : (x > max ? max : x);
    }

    public static long clamp(long x, long min, long max)
    {
        return x < min ? min : (x > max ? max : x);
    }

    public static int cycler(int x, Collection collection)
    {
        return cycler(x, 0, collection.size() - 1);
    }

    public static int cycler(int x, int min, int max)
    {
        return x < min ? max : (x > max ? min : x);
    }

    public static float cycler(float x, float min, float max)
    {
        return x < min ? max : (x > max ? min : x);
    }

    public static double cycler(double x, double min, double max)
    {
        return x < min ? max : (x > max ? min : x);
    }

    public static int gridIndex(int x, int y, int size, int width)
    {
        x = x / size;
        y = y / size;

        return x + y * width / size;
    }

    public static int gridRows(int count, int size, int width)
    {
        double x = count * size / (double) width;

        return count <= 0 ? 1 : (int) Math.ceil(x);
    }

    /**
     * Converts given value to chunk coordinate (helps with negative values)
     */
    public static int toChunk(float x, int chunkSize)
    {
        return (int) ((x < 0 ? x - (chunkSize - 1) : x) / chunkSize);
    }

    /**
     * Converts given value to chunk coordinate (helps with negative values)
     */
    public static int toChunk(double x, int chunkSize)
    {
        return (int) ((x < 0 ? x - (chunkSize - 1) : x) / chunkSize);
    }

    /**
     * Converts given index into a 3D block coordinate
     */
    public static Vector3i toBlock(int i, int w, int h, Vector3i vector)
    {
        int c = i % (w * h);
        int z = i / (w * h);
        int y = c / w;
        int x = c % w;

        return vector.set(x, y, z);
    }

    /**
     * Normalize given angle (in degrees) to be in -180 to 180 number range
     */
    public static float normalizeDegrees(float angle)
    {
        return normalizeAngle(angle, 180);
    }

    /**
     * Normalize given angle (in radians) to be in -pi to pi number range
     */
    public static float normalizeRadians(float angle)
    {
        return normalizeAngle(angle, PI);
    }

    private static float normalizeAngle(float angle, float halfCircle)
    {
        if (Float.isNaN(angle))
        {
            angle = 0;
        }

        angle %= halfCircle * 2;

        if (angle > halfCircle)
        {
            return -halfCircle + (angle - halfCircle);
        }

        return halfCircle + (angle + halfCircle);
    }

    /**
     * Wrap/normalize given radian angle to 0..2PI.
     */
    public static float wrapToCircle(float rad)
    {
        float circle = PI * 2;

        if (rad >= 0)
        {
            return rad % circle;
        }

        float times = (float) Math.ceil(rad / -circle);

        return rad + circle * times;
    }

    static void quatLog(Quaterniond q, Vector3d out) {
        // q assumed unit: q = [x, y, z, w]
        double w = q.w;
        double sinTheta = Math.sqrt(q.x*q.x + q.y*q.y + q.z*q.z);
        if (sinTheta < 1e-8) {
            out.set(0f, 0f, 0f);
            return;
        }
        double theta = Math.acos(clamp(w, -1, 1));
        double k = theta / sinTheta;
        out.set(q.x * k, q.y * k, q.z * k);
    }

    static void quatExp(Vector3d v, Quaterniond out) {
        double theta = v.length();
        if (theta < 1e-8) {
            out.set(0f, 0f, 0f, 1f);
            return;
        }
        double s = Math.sin(theta) / theta;
        out.set(v.x * s, v.y * s, v.z * s, Math.cos(theta));
    }

    /**
     * Calculate the tangents of a squad spline.
     */
    public static void computeSquadControls(
            Quaterniond q0, Quaterniond q1,
            Quaterniond q2, Quaterniond q3,
            Quaterniond a1, Quaterniond b1) {

        Quaterniond identity = new Quaterniond(0, 0, 0, 1);
        Vector3d v = new Vector3d();
        Vector3d temp = new Vector3d();
        Quaterniond tmpQ = new Quaterniond();
        Quaterniond inv = new Quaterniond();

        // a1
        inv.set(q1).invert();          // q1^{-1}
        tmpQ.set(inv).mul(q0);            // q1^{-1} q0
        quatLog(tmpQ, v);

        tmpQ.set(inv).mul(q2);            // q1^{-1} q2
        quatLog(tmpQ, temp);

        v.add(temp).mul(-0.25f);          // -1/4 * (log(q1^{-1}q0)+log(q1^{-1}q2))
        quatExp(v, tmpQ);
        a1.set(q1).mul(tmpQ).normalize();

        // b1
        inv.set(q2).invert();          // q2^{-1}
        tmpQ.set(inv).mul(q1);            // q2^{-1} q1
        quatLog(tmpQ, v);

        tmpQ.set(inv).mul(q3);            // q2^{-1} q3
        quatLog(tmpQ, temp);

        v.add(temp).mul(-0.25f);
        quatExp(v, tmpQ);
        b1.set(q2).mul(tmpQ).normalize();
    }


    /**
     * Calculate the tangent of a squad spline segment.
     *
     * @param prev the previous segment
     * @param next the next segment
     * @return the tangent of the segment
     */
    public static Quaterniond simplifiedQuatTangent(Quaterniond prev, Quaterniond next) {
        Quaterniond tangent = new Quaterniond();

        // Log difference in tangent space
        Quaterniond diff = new Quaterniond().set(next).difference(prev);

        // Scale and convert back
        tangent.set(diff).mul(-0.25);
        return tangent.normalize();
    }

    /**
     * Same as JOML SLERP, but instead does not have the sign correction
     *
     * @param initial the initial quaternion
     * @param target the target quaternion
     * @param alpha the interpolation coefficient
     * @return interpolated quaternion
     */
    public static Quaterniond rawSlerp(Quaterniondc initial, Quaterniondc target, double alpha) {
        double cosom = org.joml.Math.fma(initial.x(), target.x(), org.joml.Math.fma(initial.y(), target.y(), org.joml.Math.fma(initial.z(), target.z(), initial.w() * target.w())));
        double absCosom = org.joml.Math.abs(cosom);
        double scale0, scale1;
        if (1.0 - absCosom > 1E-6) {
            double sinSqr = 1.0 - absCosom * absCosom;
            double sinom = org.joml.Math.invsqrt(sinSqr);
            double omega = org.joml.Math.atan2(sinSqr * sinom, absCosom);
            scale0 = org.joml.Math.sin((1.0 - alpha) * omega) * sinom;
            scale1 = org.joml.Math.sin(alpha * omega) * sinom;
        } else {
            scale0 = 1.0 - alpha;
            scale1 = alpha;
        }
        Quaterniond dest = new Quaterniond();
        dest.x = org.joml.Math.fma(scale0, initial.x(), scale1 * target.x());
        dest.y = org.joml.Math.fma(scale0, initial.y(), scale1 * target.y());
        dest.z = org.joml.Math.fma(scale0, initial.z(), scale1 * target.z());
        dest.w = org.joml.Math.fma(scale0, initial.w(), scale1 * target.w());
        return dest;
    }

    /**
     * Whether segments a and b are intersecting.
     *
     *     an          ax
     *     [ ---------- ]
     *               bn            bx
     *               [ ------------ ]
     *
     *               an            ax
     *               [ ------------ ]
     *     bn          bx
     *     [ ---------- ]
     */
    public static boolean isInside(double an, double ax, double bn, double bx)
    {
        return an < bx && bn < ax;
    }

    public static int remapIndex(int old, int from, int to)
    {
        if (from == to) return old;

        if (from < to)
        {
            /* Moving item down: [from+1..to] shift left by 1 */
            if (old == from) return to;
            if (old > from && old <= to) return old - 1;

            return old;
        }
        else
        {
            /* from > to: moving item up: [to..from-1] shift right by 1 */
            if (old == from) return to;
            if (old >= to && old < from) return old + 1;

            return old;
        }
    }
}