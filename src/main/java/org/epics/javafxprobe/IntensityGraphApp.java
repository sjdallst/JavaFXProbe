/**
 * Copyright (C) 2010-14 pvmanager developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.javafxprobe;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import org.epics.graphene.Cell2DDataset;
import org.epics.graphene.GraphBuffer;
import org.epics.graphene.IntensityGraph2DRenderer;
import org.epics.graphene.InterpolationScheme;
import org.epics.graphene.LineGraph2DRenderer;
import org.epics.graphene.LineGraph2DRendererUpdate;
import org.epics.graphene.Point2DDataset;
import org.epics.graphene.Point2DDatasets;
import static org.epics.pvmanager.formula.ExpressionLanguage.formula;
import org.epics.pvmanager.graphene.DatasetConversions;
import static org.epics.pvmanager.graphene.ExpressionLanguage.*;
import org.epics.pvmanager.graphene.LineGraph2DExpression;
import org.epics.pvmanager.graphene.ScatterGraph2DExpression;
import org.epics.pvmanager.sample.LineGraphDialog;
import org.epics.vtype.VNumberArray;
import org.epics.graphene.IntensityGraph2DRenderer;
import org.epics.graphene.IntensityGraph2DRendererUpdate;
import org.epics.graphene.NumberColorMap;
import org.epics.graphene.NumberColorMaps;
import static org.epics.pvmanager.formula.ExpressionLanguage.formula;
import static org.epics.pvmanager.graphene.ExpressionLanguage.*;
import org.epics.pvmanager.graphene.Graph2DExpression;
import org.epics.pvmanager.graphene.IntensityGraph2DExpression;

/**
 *
 * @author carcassi, sjdallst
 */
public class IntensityGraphApp extends BaseGraphApp<LineGraph2DRendererUpdate> {
    
    private InterpolationScheme interpolationScheme = InterpolationScheme.NEAREST_NEIGHBOR;
    private IntensityGraph2DRenderer renderer = new IntensityGraph2DRenderer(imagePanel.getWidth(), imagePanel.getHeight());

    public IntensityGraphApp() {
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
        return null;
    }

    @Override
    protected void openConfigurationDialog() {
//        LineGraphDialog dialog = new LineGraphDialog(new javax.swing.JFrame(), true, this);
//        dialog.setTitle("Configure...");
//        dialog.setLocationRelativeTo(this);
//        dialog.setVisible(true);
    }
    
    public byte[] render(VNumberArray array, int width, int height) {
        
        GraphBuffer graphBuffer = new GraphBuffer(width, height);
        Cell2DDataset data = DatasetConversions.cell2DDatasetsFromVNumberArray(array);
        renderer.update(renderer.newUpdate().imageHeight(height).imageWidth(width));
        renderer.draw(graphBuffer, data);
        byte[] pixels = ((DataBufferByte)graphBuffer.getImage().getRaster().getDataBuffer()).getData();
        return pixels;
    }
    
}
