/**
 * Copyright (C) 2010-14 pvmanager developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.javafxprobe;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import org.epics.graphene.InterpolationScheme;
import org.epics.graphene.LineGraph2DRenderer;
import org.epics.graphene.LineGraph2DRendererUpdate;
import org.epics.graphene.Point2DDataset;
import org.epics.graphene.Point2DDatasets;
import static org.epics.pvmanager.formula.ExpressionLanguage.formula;
import static org.epics.pvmanager.graphene.ExpressionLanguage.*;
import org.epics.pvmanager.graphene.LineGraph2DExpression;
import org.epics.pvmanager.graphene.ScatterGraph2DExpression;
import org.epics.pvmanager.sample.LineGraphDialog;
import org.epics.vtype.VNumberArray;

/**
 *
 * @author carcassi, sjdallst
 */
public class LineGraphApp extends BaseGraphApp<LineGraph2DRendererUpdate> {
    
    private InterpolationScheme interpolationScheme = InterpolationScheme.NEAREST_NEIGHBOR;
    private LineGraph2DRenderer renderer = new LineGraph2DRenderer(imagePanel.getWidth(), imagePanel.getHeight());

    public LineGraphApp() {
        imagePanel.setImage(new BufferedImage(imagePanel.getWidth(), imagePanel.getHeight(), BufferedImage.TYPE_INT_ARGB));
    }
    
    public InterpolationScheme getInterpolationScheme() {
        return interpolationScheme;
    }

    public void setInterpolationScheme(InterpolationScheme interpolationScheme) {
        this.interpolationScheme = interpolationScheme;
        if (graph != null) {
            graph.update(graph.newUpdate().interpolation(interpolationScheme));
        }
    }

    @Override
    protected LineGraph2DExpression createExpression(String dataFormula) {
        LineGraph2DExpression plot = lineGraphOf(formula(dataFormula),
                    null,
                    null,
                    null);
        plot.update(plot.newUpdate().interpolation(interpolationScheme));
        return plot;
    }

    @Override
    protected void openConfigurationDialog() {
//        LineGraphDialog dialog = new LineGraphDialog(new javax.swing.JFrame(), true, this);
//        dialog.setTitle("Configure...");
//        dialog.setLocationRelativeTo(this);
//        dialog.setVisible(true);
    }
    
    public static void main(String[] args) {
        main(LineGraphApp.class);
    }
    
    public byte[] render(VNumberArray array, int width, int height) {
        
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        Point2DDataset data = Point2DDatasets.lineData(array.getData());
        renderer.update(renderer.newUpdate().interpolation(InterpolationScheme.LINEAR)
                    .imageHeight(height).imageWidth(width));
        renderer.draw(image.createGraphics(), data);
        byte[] pixels = ((DataBufferByte)image.getRaster().getDataBuffer()).getData();
        return pixels;
    }
    
}
