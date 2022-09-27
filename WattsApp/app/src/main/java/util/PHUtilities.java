package util;

/*
 * Copyright 2015-2018 Jeeva Kandasamy (jkandasa@gmail.com)
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.graphics.Color;

import java.util.ArrayList;
import java.util.List;

/**
 * Decompiled from Philips Hue SDK resources, to be able to run without the jar dependency.
 */
public class PHUtilities {
    private static List<PointF> colorPointsLivingColor = new ArrayList<>();
    private static List<PointF> colorPointsHueBulb = new ArrayList<>();
    private static List<PointF> colorPointsDefault = new ArrayList<>();
    private static final List<String> HUE_BULBS = new ArrayList<>();
    private static final List<String> LIVING_COLORS = new ArrayList<>();

    static {
        HUE_BULBS.add("LCT001");
        HUE_BULBS.add("LCT002");
        HUE_BULBS.add("LCT003");
        HUE_BULBS.add("LLM001");

        LIVING_COLORS.add("LLC001");
        LIVING_COLORS.add("LLC005");
        LIVING_COLORS.add("LLC006");
        LIVING_COLORS.add("LLC007");
        LIVING_COLORS.add("LLC010");
        LIVING_COLORS.add("LLC011");
        LIVING_COLORS.add("LLC012");
        LIVING_COLORS.add("LLC014");
        LIVING_COLORS.add("LLC013");
        LIVING_COLORS.add("LST001");

        colorPointsHueBulb.add(new PointF(0.674F, 0.322F));
        colorPointsHueBulb.add(new PointF(0.408F, 0.517F));
        colorPointsHueBulb.add(new PointF(0.168F, 0.041F));

        colorPointsLivingColor.add(new PointF(0.703F, 0.296F));
        colorPointsLivingColor.add(new PointF(0.214F, 0.709F));
        colorPointsLivingColor.add(new PointF(0.139F, 0.081F));

        colorPointsDefault.add(new PointF(1.0F, 0.0F));
        colorPointsDefault.add(new PointF(0.0F, 1.0F));
        colorPointsDefault.add(new PointF(0.0F, 0.0F));
    }

    public static String getHexFromXY(float[] points, String model) {
        int color = colorFromXY(points, model);
        return String.format("#%06X", (0xFFFFFF & color));
    }

    public static int colorFromXY(float[] points, String model) {
        if ((points == null) || (model == null)) {
            throw new IllegalArgumentException("Input parameter can't be null");
        }

        PointF xy = new PointF(points[0], points[1]);
        List<PointF> colorPoints = colorPointsForModel(model);
        boolean inReachOfLamps = checkPointInLampsReach(xy, colorPoints);
        if (!inReachOfLamps) {
            PointF pAB = getClosestPointToPoints((PointF) colorPoints.get(0), (PointF) colorPoints.get(1), xy);

            PointF pAC = getClosestPointToPoints((PointF) colorPoints.get(2), (PointF) colorPoints.get(0), xy);

            PointF pBC = getClosestPointToPoints((PointF) colorPoints.get(1), (PointF) colorPoints.get(2), xy);

            float dAB = getDistanceBetweenTwoPoints(xy, pAB);
            float dAC = getDistanceBetweenTwoPoints(xy, pAC);
            float dBC = getDistanceBetweenTwoPoints(xy, pBC);
            float lowest = dAB;
            PointF closestPoint = pAB;
            if (dAC < lowest) {
                lowest = dAC;
                closestPoint = pAC;
            }
            if (dBC < lowest) {
                lowest = dBC;
                closestPoint = pBC;
            }

            xy.x = closestPoint.x;
            xy.y = closestPoint.y;
        }
        float x = xy.x;
        float y = xy.y;
        float z = 1.0F - x - y;
        float y2 = 1.0F;
        float x2 = y2 / y * x;
        float z2 = y2 / y * z;

        float r = x2 * 1.656492F - y2 * 0.354851F - z2 * 0.255038F;
        float g = -x2 * 0.707196F + y2 * 1.655397F + z2 * 0.036152F;
        float b = x2 * 0.051713F - y2 * 0.121364F + z2 * 1.01153F;

        if ((r > b) && (r > g) && (r > 1.0F)) {
            g /= r;
            b /= r;
            r = 1.0F;
        } else if ((g > b) && (g > r) && (g > 1.0F)) {
            r /= g;
            b /= g;
            g = 1.0F;
        } else if ((b > r) && (b > g) && (b > 1.0F)) {
            r /= b;
            g /= b;
            b = 1.0F;
        }

        r = r <= 0.0031308F ? 12.92F * r : 1.055F * (float) Math.pow(r, 0.416666656732559D) - 0.055F;

        g = g <= 0.0031308F ? 12.92F * g : 1.055F * (float) Math.pow(g, 0.416666656732559D) - 0.055F;

        b = b <= 0.0031308F ? 12.92F * b : 1.055F * (float) Math.pow(b, 0.416666656732559D) - 0.055F;

        if ((r > b) && (r > g)) {
            if (r > 1.0F) {
                g /= r;
                b /= r;
                r = 1.0F;
            }
        } else if ((g > b) && (g > r)) {
            if (g > 1.0F) {
                r /= g;
                b /= g;
                g = 1.0F;
            }
        } else if ((b > r) && (b > g) && (b > 1.0F)) {
            r /= b;
            g /= b;
            b = 1.0F;
        }

        if (r < 0.0F) {
            r = 0.0F;
        }
        if (g < 0.0F) {
            g = 0.0F;
        }
        if (b < 0.0F) {
            b = 0.0F;
        }

        int r1 = (int) (r * 255.0F);
        int g1 = (int) (g * 255.0F);
        int b1 = (int) (b * 255.0F);

        return Color.rgb(r1, g1, b1);
    }

    public static float[] calculateXY(final int color, final String model) {
        float red = 1.0F;
        float green = 1.0F;
        float blue = 1.0F;

        red = Color.red(color) / 255.0F;
        green = Color.green(color) / 255.0F;
        blue = Color.blue(color) / 255.0F;

        final float r = (red > 0.04045F) ? (float) Math.pow((red + 0.055F) /
                1.055F, 2.400000095367432D) : red / 12.92F;
        final float g = (green > 0.04045F) ? (float) Math.pow((green + 0.055F) /
                1.055F, 2.400000095367432D) : green / 12.92F;
        final float b = (blue > 0.04045F) ? (float) Math.pow((blue + 0.055F) /
                1.055F, 2.400000095367432D) : blue / 12.92F;

        final float x = r * 0.649926F + g * 0.103455F + b * 0.197109F;
        final float y = r * 0.234327F + g * 0.743075F + b * 0.022598F;
        final float z = r * 0.0F + g * 0.053077F + b * 1.035763F;

        final float[] xy = new float[2];

        xy[0] = (x / (x + y + z));
        xy[1] = (y / (x + y + z));
        if (Float.isNaN(xy[0])) {
            xy[0] = 0.0F;
        }
        if (Float.isNaN(xy[1])) {
            xy[1] = 0.0F;
        }

        final PointF xyPoint = new PointF(xy[0], xy[1]);
        final List<PointF> colorPoints = colorPointsForModel(model);
        final boolean inReachOfLamps = checkPointInLampsReach(xyPoint, colorPoints);
        if (!(inReachOfLamps)) {
            final PointF pAB = getClosestPointToPoints(colorPoints.get(0),
                    colorPoints.get(1), xyPoint);
            final PointF pAC = getClosestPointToPoints(colorPoints.get(2),
                    colorPoints.get(0), xyPoint);
            final PointF pBC = getClosestPointToPoints(colorPoints.get(1),
                    colorPoints.get(2), xyPoint);

            final float dAB = getDistanceBetweenTwoPoints(xyPoint, pAB);
            final float dAC = getDistanceBetweenTwoPoints(xyPoint, pAC);
            final float dBC = getDistanceBetweenTwoPoints(xyPoint, pBC);

            float lowest = dAB;
            PointF closestPoint = pAB;
            if (dAC < lowest) {
                lowest = dAC;
                closestPoint = pAC;
            }
            if (dBC < lowest) {
                lowest = dBC;
                closestPoint = pBC;
            }

            xy[0] = closestPoint.x;
            xy[1] = closestPoint.y;
        }

        xy[0] = precision(xy[0]);
        xy[1] = precision(xy[1]);
        return xy;
    }

    public static float precision(final float d) {
        return (Math.round(10000.0F * d) / 10000.0F);
    }

    public static float[] calculateXYFromRGB(final int red, final int green, final int blue, final String model) {
        final int rgb = Color.rgb(red, green, blue);
        return calculateXY(rgb, model);
    }

    public static int daylight(double tcp) {
        double x, y, z, r, g, b;
        if (tcp < 4000.0) {
            throw new IllegalArgumentException();
        }
        else if (tcp <= 7000.0) {
            x = -4.6070E9 / (tcp * tcp * tcp) + 2.9678E6 / (tcp * tcp) + 0.09911E3 / tcp + 0.244063;
        }
        else if (tcp <= 25000.0) {
            x = -2.0064E9 / (tcp * tcp * tcp) + 1.9018E6 / (tcp * tcp) + 0.24748E3 / tcp + 0.237040;
        }
        else {
            throw new IllegalArgumentException();
        }
        y = -3.000 * (x * x) + 2.870 * x - 0.275;
        z = 1.0 - x - y;

        r = 3.2406 * x - 1.5372 * y - 0.4986 * z;
        g = -0.9689 * x + 1.8758 * y + 0.0415 * z;
        b = 0.0557 * x - 0.2040 * y + 1.0570 * z;

//		double maxRgb = Math.max(Math.max(r, g), b);
//		r /= maxRgb;
//		g /= maxRgb;
//		b /= maxRgb;
//		r = (r <= 0.0031308) ? 12.92 * r : 1.055 * Math.pow(r, 1.0 / 2.4) - 0.055;
//		g = (g <= 0.0031308) ? 12.92 * g : 1.055 * Math.pow(g, 1.0 / 2.4) - 0.055;
//		b = (b <= 0.0031308) ? 12.92 * b : 1.055 * Math.pow(b, 1.0 / 2.4) - 0.055;

//The commented out part above omits the branch in this situation.
//You can do the following

        double maxRgb = Math.max(Math.max(r, g), b);
        r = 1.055 * Math.pow(r / maxRgb, 1.0 / 2.4) - 0.055;
        g = 1.055 * Math.pow(g / maxRgb, 1.0 / 2.4) - 0.055;
        b = 1.055 * Math.pow(b / maxRgb, 1.0 / 2.4) - 0.055;

        int rr = (int) (255.0 * r + 0.5);
        int gg = (int) (255.0 * g + 0.5);
        int bb = (int) (255.0 * b + 0.5);
        return 0xFF000000 | (rr << 16) | (gg << 8) | bb;
    }

    /**
     * Convert color temperature in Kelvins to RGB color for AWT
     * @param temperature
     * @return ready to use color object
     */
    public static int getRGBFromK(int temperature) {
        // Used this: https://gist.github.com/paulkaplan/5184275 at the beginning
        // based on http://stackoverflow.com/questions/7229895/display-temperature-as-a-color-with-c
        // this answer: http://stackoverflow.com/a/24856307
        // (so, just interpretation of pseudocode in Java)

        double x = temperature / 1000.0;
        if (x > 40) {
            x = 40;
        }
        double red;
        double green;
        double blue;

        // R
        if (temperature < 6527) {
            red = 1;
        } else {
            double[] redpoly = {4.93596077e0, -1.29917429e0,
                    1.64810386e-01, -1.16449912e-02,
                    4.86540872e-04, -1.19453511e-05,
                    1.59255189e-07, -8.89357601e-10};
            red = poly(redpoly, x);

        }
        // G
        if (temperature < 850) {
            green = 0;
        } else if (temperature <= 6600) {
            double[] greenpoly = {-4.95931720e-01, 1.08442658e0,
                    -9.17444217e-01, 4.94501179e-01,
                    -1.48487675e-01, 2.49910386e-02,
                    -2.21528530e-03, 8.06118266e-05};
            green = poly(greenpoly, x);
        } else {
            double[] greenpoly = {3.06119745e0, -6.76337896e-01,
                    8.28276286e-02, -5.72828699e-03,
                    2.35931130e-04, -5.73391101e-06,
                    7.58711054e-08, -4.21266737e-10};

            green = poly(greenpoly, x);
        }
        // B
        if (temperature < 1900) {
            blue = 0;
        } else if (temperature < 6600) {
            double[] bluepoly = {4.93997706e-01, -8.59349314e-01,
                    5.45514949e-01, -1.81694167e-01,
                    4.16704799e-02, -6.01602324e-03,
                    4.80731598e-04, -1.61366693e-05};
            blue = poly(bluepoly, x);
        } else {
            blue = 1;
        }

        red = clamp(red, 0, 1);
        blue = clamp(blue, 0, 1);
        green = clamp(green, 0, 1);

        int r = (int)(red * 255.0);
        int g = (int)(green * 255.0);
        int b = (int)(blue * 255.0);
        return Color.rgb(r,g,b);
    }

    public static double poly(double[] coefficients, double x) {
        double result = coefficients[0];
        double xn = x;
        for (int i = 1; i < coefficients.length; i++) {
            result += xn * coefficients[i];
            xn *= x;

        }
        return result;
    }

    public static double clamp(double x, double min, double max) {
        if (x < min) {
            return min;
        }
        if (x > max) {
            return max;
        }
        return x;
    }

    private static boolean checkPointInLampsReach(final PointF point, final List<PointF> colorPoints) {
        if ((point == null) || (colorPoints == null)) {
            return false;
        }
        final PointF red = colorPoints.get(0);
        final PointF green = colorPoints.get(1);
        final PointF blue = colorPoints.get(2);
        final PointF v1 = new PointF(green.x - red.x, green.y - red.y);
        final PointF v2 = new PointF(blue.x - red.x, blue.y - red.y);
        final PointF q = new PointF(point.x - red.x, point.y - red.y);
        final float s = crossProduct(q, v2) / crossProduct(v1, v2);
        final float t = crossProduct(v1, q) / crossProduct(v1, v2);

        return ((s >= 0.0F) && (t >= 0.0F) && (s + t <= 1.0F));
    }

    private static float getDistanceBetweenTwoPoints(final PointF one, final PointF two) {
        final float dx = one.x - two.x;
        final float dy = one.y - two.y;
        final float dist = (float) Math.sqrt(dx * dx + dy * dy);

        return dist;
    }

    private static float crossProduct(final PointF point1, final PointF point2) {
        return (point1.x * point2.y - (point1.y * point2.x));
    }

    private static List<PointF> colorPointsForModel(String model) {
        if (model == null) {
            model = " ";
        }
        List<PointF> colorPoints;
        if (HUE_BULBS.contains(model)) {
            colorPoints = colorPointsHueBulb;
        } else {
            if (LIVING_COLORS.contains(model)) {
                colorPoints = colorPointsLivingColor;
            } else {
                colorPoints = colorPointsDefault;
            }
        }
        return colorPoints;
    }

    private static PointF getClosestPointToPoints(final PointF pointA, final PointF pointB, final PointF pointP) {
        if ((pointA == null) || (pointB == null) || (pointP == null)) {
            return null;
        }
        final PointF pointAP = new PointF(pointP.x - pointA.x, pointP.y - pointA.y);
        final PointF pointAB = new PointF(pointB.x - pointA.x, pointB.y - pointA.y);
        final float ab2 = pointAB.x * pointAB.x + pointAB.y * pointAB.y;
        final float apAb = pointAP.x * pointAB.x + pointAP.y * pointAB.y;
        float t = apAb / ab2;
        if (t < 0.0F) {
            t = 0.0F;
        } else if (t > 1.0F) {
            t = 1.0F;
        }
        final PointF newPoint = new PointF(pointA.x + pointAB.x * t, pointA.y +
                pointAB.y * t);
        return newPoint;
    }

}
