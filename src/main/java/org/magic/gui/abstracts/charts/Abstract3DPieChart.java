package org.magic.gui.abstracts.charts;

import org.jfree.chart3d.Chart3DFactory;
import org.jfree.chart3d.Colors;
import org.jfree.chart3d.Orientation;
import org.jfree.chart3d.data.PieDataset3D;
import org.jfree.chart3d.graphics2d.Anchor2D;
import org.jfree.chart3d.plot.PiePlot3D;
import org.jfree.chart3d.plot.Plot3D;
import org.jfree.chart3d.style.ChartStyle;
import org.jfree.chart3d.table.TextElement;

public abstract class Abstract3DPieChart<B> extends MTGUI3DChartComponent<B> {

	private static final long serialVersionUID = 1L;
	protected PiePlot3D plot;
	
	@Override
	public void refresh() {
		initPlot();
        plot.setDataset(getDataSet());
    	chartPanel.zoomToFit();
	}

	protected void initPlot()
	{
		plot.setSectionColors(Colors.createPastelColors());
	}

	@Override
	@SuppressWarnings({ "rawtypes"})
	protected void createNewChart() {
		chart= Chart3DFactory.createPieChart(
                getTitle(), 
                "", 
                getDataSet());
		
		plot = (PiePlot3D) chart.getPlot();
		
		
		if(!showLegend())
			chart.setLegendBuilder((Plot3D plotA, Anchor2D anchor, Orientation orientation, ChartStyle style)->new TextElement(""));
		
		if(showLabel())
			plot.setSectionLabelGenerator(( PieDataset3D dataset, Comparable<?> key)->key.toString());
		else
			plot.setSectionLabelGenerator(( PieDataset3D dataset, Comparable<?> key)->"");
		
		
		plot.setToolTipGenerator(( PieDataset3D dataset, Comparable<?> key)->key.toString() + " : " + dataset.getValue(key));
		
	}
	

	

}