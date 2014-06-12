/**
 * Copyright (C) 2010-14 pvmanager developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.javafxprobe;

import org.epics.graphene.InterpolationScheme;
import org.epics.graphene.LineGraph2DRendererUpdate;
import static org.epics.pvmanager.formula.ExpressionLanguage.formula;
import static org.epics.pvmanager.graphene.ExpressionLanguage.*;
import org.epics.pvmanager.graphene.LineGraph2DExpression;
import org.epics.pvmanager.graphene.ScatterGraph2DExpression;
import org.epics.pvmanager.sample.LineGraphDialog;

/**
 *
 * @author carcassi, sjdallst
 */
public class LineGraphApp extends BaseGraphApp<LineGraph2DRendererUpdate> {
    private InterpolationScheme interpolationScheme = InterpolationScheme.NEAREST_NEIGHBOR;

    public LineGraphApp() {
        dataFormulaField.setModel(new javax.swing.DefaultComboBoxModel<String>(
                new String[] { "sim://table",
                    "sim://gaussianWaveform",
                    "sim://sineWaveform",
                    "sim://triangleWaveform",
                    "=tableOf(column(\"X\", range(-5, 5)), column(\"Y\", 'sim://gaussianWaveform'))"}));
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
    
}
