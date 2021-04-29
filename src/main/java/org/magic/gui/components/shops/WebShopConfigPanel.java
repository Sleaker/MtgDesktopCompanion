package org.magic.gui.components.shops;

import static org.magic.tools.MTG.getPlugin;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;

import org.magic.api.beans.WebShopConfig;
import org.magic.api.interfaces.MTGServer;
import org.magic.gui.components.ServerStatePanel;
import org.magic.services.MTGConstants;
import org.magic.services.MTGControler;

public class WebShopConfigPanel extends JPanel {
	
	
	private JTextField txtSiteTitle;
	private JTextField txtBannerTitle;
	private JTextArea txtBannerText;
	private JTextArea txtAbout;
	private JTextField txtURLSlides;
	private DefaultListModel<String> listModel;
	private JList listSlides;
	
	public WebShopConfigPanel() {
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{137, 86, 0};
		gridBagLayout.rowHeights = new int[]{31, 31, 58, 58, 30, 0, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		JButton btnDeleteLink = new JButton(MTGConstants.ICON_SMALL_DELETE);
		listModel = new DefaultListModel<>();
		
		for(String s : MTGControler.getInstance().getWebConfig().getLinks())
		{
			listModel.addElement(s);
		}
		
		
		
		var lblTitleSite = new JLabel("SITETITLE");
		GridBagConstraints gbclblTitleSite = new GridBagConstraints();
		gbclblTitleSite.insets = new Insets(0, 0, 5, 5);
		gbclblTitleSite.gridx = 0;
		gbclblTitleSite.gridy = 0;
		add(lblTitleSite, gbclblTitleSite);
		
		txtSiteTitle = new JTextField(MTGControler.getInstance().getWebConfig().getSiteTitle());
		GridBagConstraints gbctxtSiteTitle = new GridBagConstraints();
		gbctxtSiteTitle.insets = new Insets(0, 0, 5, 0);
		gbctxtSiteTitle.fill = GridBagConstraints.BOTH;
		gbctxtSiteTitle.gridx = 1;
		gbctxtSiteTitle.gridy = 0;
		add(txtSiteTitle, gbctxtSiteTitle);
		
		JLabel lblBannerTitle = new JLabel("BANNERTITLE");
		GridBagConstraints gbclblBannerTitle = new GridBagConstraints();
		gbclblBannerTitle.insets = new Insets(0, 0, 5, 5);
		gbclblBannerTitle.gridx = 0;
		gbclblBannerTitle.gridy = 1;
		add(lblBannerTitle, gbclblBannerTitle);
		
		txtBannerTitle = new JTextField(MTGControler.getInstance().getWebConfig().getBannerTitle());
		GridBagConstraints gbctxtBannerTitle = new GridBagConstraints();
		gbctxtBannerTitle.insets = new Insets(0, 0, 5, 0);
		gbctxtBannerTitle.fill = GridBagConstraints.BOTH;
		gbctxtBannerTitle.gridx = 1;
		gbctxtBannerTitle.gridy = 1;
		add(txtBannerTitle, gbctxtBannerTitle);
		txtBannerTitle.setColumns(10);
		
		JLabel lblBannerText = new JLabel("BANNERTEXT");
		GridBagConstraints gbclblBannerText = new GridBagConstraints();
		gbclblBannerText.insets = new Insets(0, 0, 5, 5);
		gbclblBannerText.gridx = 0;
		gbclblBannerText.gridy = 2;
		add(lblBannerText, gbclblBannerText);
		
		txtBannerText = new JTextArea(MTGControler.getInstance().getWebConfig().getBannerText());
		GridBagConstraints gbctxtBannerText = new GridBagConstraints();
		gbctxtBannerText.insets = new Insets(0, 0, 5, 0);
		gbctxtBannerText.fill = GridBagConstraints.BOTH;
		gbctxtBannerText.gridx = 1;
		gbctxtBannerText.gridy = 2;
		add(txtBannerText, gbctxtBannerText);
		
		JLabel lblAbout = new JLabel("ABOUT");
		GridBagConstraints gbclblAbout = new GridBagConstraints();
		gbclblAbout.insets = new Insets(0, 0, 5, 5);
		gbclblAbout.gridx = 0;
		gbclblAbout.gridy = 3;
		add(lblAbout, gbclblAbout);
		
		txtAbout = new JTextArea(MTGControler.getInstance().getWebConfig().getAboutText());
		GridBagConstraints gbctxtAbout = new GridBagConstraints();
		gbctxtAbout.insets = new Insets(0, 0, 5, 0);
		gbctxtAbout.fill = GridBagConstraints.BOTH;
		gbctxtAbout.gridx = 1;
		gbctxtAbout.gridy = 3;
		add(txtAbout, gbctxtAbout);
		
		JLabel lblSlides = new JLabel("SLIDES");
		GridBagConstraints gbclblSlides = new GridBagConstraints();
		gbclblSlides.insets = new Insets(0, 0, 5, 5);
		gbclblSlides.gridx = 0;
		gbclblSlides.gridy = 4;
		add(lblSlides, gbclblSlides);
		
		JPanel panelSlides = new JPanel();
		GridBagConstraints gbcpanelSlides = new GridBagConstraints();
		gbcpanelSlides.insets = new Insets(0, 0, 5, 0);
		gbcpanelSlides.fill = GridBagConstraints.BOTH;
		gbcpanelSlides.gridx = 1;
		gbcpanelSlides.gridy = 4;
		add(panelSlides, gbcpanelSlides);
		panelSlides.setLayout(new BorderLayout(0, 0));
		
		txtURLSlides = new JTextField();
		txtURLSlides.addActionListener((ActionEvent e)->{
				listModel.addElement(txtURLSlides.getText());
				txtURLSlides.setText("");
		});
		panelSlides.add(txtURLSlides, BorderLayout.NORTH);
		txtURLSlides.setColumns(10);
		
		
		listSlides = new JList<>(listModel);
		listSlides.addListSelectionListener((ListSelectionEvent e)->{
				btnDeleteLink.setEnabled(listSlides.getSelectedIndex()>-1);

		});

		panelSlides.add(new JScrollPane(listSlides), BorderLayout.CENTER);

		
		JPanel panel1 = new JPanel();
		panelSlides.add(panel1, BorderLayout.EAST);
		
		
		btnDeleteLink.addActionListener((ActionEvent e)->{
				listModel.removeElement(listSlides.getSelectedValue());
		});
		panel1.add(btnDeleteLink);
		
		JButton btnSave = new JButton("Save");
		GridBagConstraints gbcbtnSave = new GridBagConstraints();
		gbcbtnSave.insets = new Insets(0, 0, 5, 0);
		gbcbtnSave.gridwidth = 2;
		gbcbtnSave.gridx = 0;
		gbcbtnSave.gridy = 6;
		add(btnSave, gbcbtnSave);
		
		ServerStatePanel serverPanel = new ServerStatePanel(false,getPlugin("Shopping Server", MTGServer.class));
		GridBagConstraints gbcServer = new GridBagConstraints();
		gbcServer.gridwidth = 2;
		gbcServer.fill = GridBagConstraints.BOTH;
		gbcServer.gridx = 0;
		gbcServer.gridy = 6;
		add(serverPanel, gbcServer);
		
		
		
		btnSave.addActionListener(al->{
			
			WebShopConfig bean = MTGControler.getInstance().getWebConfig();
			
			bean.setAboutText(txtAbout.getText());
			bean.setBannerText(txtBannerText.getText());
			bean.setBannerTitle(txtBannerTitle.getText());
			bean.setSiteTitle(txtSiteTitle.getText());
			
			bean.getLinks().clear();
			
			Iterator<String> it = listModel.elements().asIterator();
			
			while(it.hasNext())
				bean.getLinks().add(it.next());
			
			MTGControler.getInstance().saveWebConfig(bean);
			
		});
		

	}

	public JTextField getTxtSiteTitle() {
		return txtSiteTitle;
	}
	public JTextField getTxtBannerTitle() {
		return txtBannerTitle;
	}
	public JTextArea getTextArea() {
		return txtBannerText;
	}
	public JTextArea getTextArea1() {
		return txtAbout;
	}
	public JList getListSlides() {
		return listSlides;
	}
}
