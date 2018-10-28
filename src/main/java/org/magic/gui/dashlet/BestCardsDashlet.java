package org.magic.gui.dashlet;

import java.awt.BorderLayout;
import java.awt.Rectangle;
import java.io.IOException;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.table.TableRowSorter;

import org.jdesktop.swingx.JXTable;
import org.magic.api.beans.CardDominance;
import org.magic.api.beans.MTGFormat;
import org.magic.api.interfaces.MTGDashBoard;
import org.magic.api.interfaces.abstracts.AbstractJDashlet;
import org.magic.gui.abstracts.AbstractBuzyIndicatorComponent;
import org.magic.gui.models.CardDominanceTableModel;
import org.magic.services.MTGConstants;
import org.magic.services.MTGControler;
import org.magic.services.ThreadManager;
import org.magic.tools.UITools;

public class BestCardsDashlet extends AbstractJDashlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JXTable table;
	private CardDominanceTableModel models;
	private JComboBox<MTGFormat> cboFormat;
	private JComboBox<String> cboFilter;
	private AbstractBuzyIndicatorComponent lblLoading;

	
	@Override
	public Icon getIcon() {
		return MTGConstants.ICON_UP;
	}
	
	@Override
	public String getName() {
		return "Most Played cards";
	}

	@Override
	public void initGUI() {
		JPanel panneauHaut = new JPanel();
		getContentPane().add(panneauHaut, BorderLayout.NORTH);

		cboFormat = UITools.createCombobox(MTGFormat.values());
		
		panneauHaut.add(cboFormat);

		cboFilter = UITools.createCombobox(MTGControler.getInstance().getEnabled(MTGDashBoard.class).getDominanceFilters());
		panneauHaut.add(cboFilter);

		lblLoading = AbstractBuzyIndicatorComponent.createLabelComponent();
		panneauHaut.add(lblLoading);

		models = new CardDominanceTableModel();
		table = new JXTable(models);
		getContentPane().add(new JScrollPane(table), BorderLayout.CENTER);
		UITools.initCardToolTipTable(table, 0, null);

		cboFormat.addActionListener(ae -> init());

		cboFilter.addActionListener(ae -> init());

		if (getProperties().size() > 0) {
			Rectangle r = new Rectangle((int) Double.parseDouble(getString("x")),
					(int) Double.parseDouble(getString("y")), (int) Double.parseDouble(getString("w")),
					(int) Double.parseDouble(getString("h")));

			try {
				cboFormat.setSelectedItem(getProperty("FORMAT", "standard"));
				cboFilter.setSelectedItem(getProperty("FILTER", "all"));
			} catch (Exception e) {
				logger.error("can't get value", e);
			}
			setBounds(r);
		}
		setVisible(true);

	}

	@Override
	public void init() {
		ThreadManager.getInstance().execute(() -> {
			lblLoading.start();
			
			List<CardDominance> list;
			try {
				list = MTGControler.getInstance().getEnabled(MTGDashBoard.class).getBestCards((MTGFormat) cboFormat.getSelectedItem(), cboFilter.getSelectedItem().toString());
				models.init(list);
				models.fireTableDataChanged();
				table.packAll();
				table.setRowSorter(new TableRowSorter(models));
				setProperty("FORMAT", cboFormat.getSelectedItem().toString());
				setProperty("FILTER", cboFilter.getSelectedItem().toString());
			} catch (IOException e) {
				logger.error(e);
			}
			lblLoading.end();
			
			
		}, "init BestCardsDashlet");
	}

}
