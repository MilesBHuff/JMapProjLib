/*
Copyright 2006 Jerry Huxtable

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */

/*
 * This file was semi-automatically converted from the public-domain USGS PROJ source.
 *
 * Bernhard Jenny, 23 September 2010: change super class to CylindricalProjection.
 */
package com.jhlabs.map.proj;

import com.jhlabs.map.MapMath;
import java.awt.geom.Point2D;
import java.time.Year;

public class CassiniProjection extends CylindricalProjection {

    private double m0;
    private double n;
    private double t;
    private double a1;
    private double c;
    private double r;
    private double dd;
    private double d2;
    private double a2;
    private double tn;
    private double[] en;
    private final static double EPS10 = 1e-10;
    private final static double C1 = .16666666666666666666;
    private final static double C2 = .00833333333333333333;
    private final static double C3 = .04166666666666666666;
    private final static double C4 = .33333333333333333333;
    private final static double C5 = .06666666666666666666;

    public CassiniProjection() {
        projectionLatitude = Math.toRadians(0);
        projectionLongitude = Math.toRadians(0);
        minLongitude = Math.toRadians(-90);
        maxLongitude = Math.toRadians(90);
        initialize();
    }

    @Override
    public Point2D.Double project(double lplam, double lpphi, Point2D.Double xy) {
        if (spherical) {
            xy.x = Math.asin(Math.cos(lpphi) * Math.sin(lplam));
            xy.y = Math.atan2(Math.tan(lpphi), Math.cos(lplam)) - projectionLatitude;
        } else {
            xy.y = MapMath.mlfn(lpphi, n = Math.sin(lpphi), c = Math.cos(lpphi), en);
            n = 1. / Math.sqrt(1. - es * n * n);
            tn = Math.tan(lpphi);
            t = tn * tn;
            a1 = lplam * c;
            c *= es * c / (1 - es);
            a2 = a1 * a1;
            xy.x = n * a1 * (1. - a2 * t
                    * (C1 - (8. - t + 8. * c) * a2 * C2));
            xy.y -= m0 - n * tn * a2
                    * (.5 + (5. - t + 6. * c) * a2 * C3);
        }
        return xy;
    }

    @Override
    public Point2D.Double projectInverse(double xyx, double xyy, Point2D.Double out) {
        if (spherical) {
            out.y = Math.asin(Math.sin(dd = xyy + projectionLatitude) * Math.cos(xyx));
            out.x = Math.atan2(Math.tan(xyx), Math.cos(dd));
        } else {
            double ph1;

            ph1 = MapMath.inv_mlfn(m0 + xyy, es, en);
            tn = Math.tan(ph1);
            t = tn * tn;
            n = Math.sin(ph1);
            r = 1. / (1. - es * n * n);
            n = Math.sqrt(r);
            r *= (1. - es) * n;
            dd = xyx / n;
            d2 = dd * dd;
            out.y = ph1 - (n * tn / r) * d2
                    * (.5 - (1. + 3. * t) * d2 * C3);
            out.x = dd * (1. + t * d2
                    * (-C4 + (1. + 3. * t) * d2 * C5)) / Math.cos(ph1);
        }
        return out;
    }

    @Override
    public void initialize() {
        super.initialize();
        if (!spherical) {
            if ((en = MapMath.enfn(es)) == null) {
                throw new IllegalArgumentException();
            }
            m0 = MapMath.mlfn(projectionLatitude, Math.sin(projectionLatitude), Math.cos(projectionLatitude), en);
        }
    }

    @Override
    public boolean hasInverse() {
        return true;
    }

    /**
     * Returns the ESPG code for this projection.
     */
    public int getEPSGCode() {
        return 9806;
    }

    @Override
    public String toString() {
        return "Cassini";
    }
    
    @Override
    public Year getYear() {
        return Year.of(1745);
    }
    
    @Override
    public String getAuthor() {
        return "César François Cassini de Thury (1714-1784)";
    }
    
    @Override
    public String getDescription() {
        return super.getDescription() + "\nTransverse aspect of Plate Carrée.";
    }
    
}
