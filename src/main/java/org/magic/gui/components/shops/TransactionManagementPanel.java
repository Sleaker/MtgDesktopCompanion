package org.magic.gui.components.shops;

import static org.magic.tools.MTG.getEnabledPlugin;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.sql.SQLException;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.magic.api.beans.MTGNotification;
import org.magic.api.beans.MTGNotification.MESSAGE_TYPE;
import org.magic.api.beans.MagicCardStock;
import org.magic.api.beans.Transaction;
import org.magic.api.beans.Transaction.STAT;
import org.magic.api.exports.impl.WooCommerceExport;
import org.magic.api.interfaces.MTGCardsExport;
import org.magic.api.interfaces.MTGDao;
import org.magic.gui.abstracts.AbstractBuzyIndicatorComponent;
import org.magic.gui.abstracts.MTGUIComponent;
import org.magic.services.MTGConstants;
import org.magic.services.MTGControler;
import org.magic.services.TransactionService;
import org.magic.services.threads.ThreadManager;
import org.magic.services.workers.AbstractObservableWorker;
import org.magic.tools.MTG;

public class TransactionManagementPanel extends MTGUIComponent {
	
	private Transaction t;
	private JButton btnAcceptTransaction;
	private JButton btnSend;
	private JButton btnSave;
	private AbstractBuzyIndicatorComponent loader;
	private JButton btnWooCommerce;
	
	public void setTransaction(Transaction t)
	{
		this.t=t;
		btnAcceptTransaction.setEnabled(t!=null);
		btnSend.setEnabled(t!=null);
		btnSave.setEnabled(t!=null);
	}
	
	
	public TransactionManagementPanel() {
		setLayout(new BorderLayout(0, 0));
		loader = AbstractBuzyIndicatorComponent.createProgressComponent();
		btnAcceptTransaction = new JButton("Accept Transaction",MTGConstants.ICON_SMALL_CHECK);
		btnSend = new JButton("Mark as Sent",MTGConstants.ICON_TAB_DELIVERY);
		btnSave = new JButton("Save",MTGConstants.ICON_SMALL_SAVE);
		btnWooCommerce = new JButton("Send WooCommerce", new WooCommerceExport().getIcon());
		
		btnSend.setEnabled(false);
		btnSave.setEnabled(false);
		btnAcceptTransaction.setEnabled(false);
		
		
		var panelCenter = new JPanel();
		panelCenter.setLayout(new GridLayout(4,1));
		
		panelCenter.add(btnSave);
		panelCenter.add(btnAcceptTransaction);
		panelCenter.add(btnSend);
		panelCenter.add(btnWooCommerce);
		
		
		add(panelCenter, BorderLayout.NORTH);
		add(loader,BorderLayout.SOUTH);
			
		btnWooCommerce.addActionListener(e->{
			Map ret = ((WooCommerceExport)MTG.getPlugin("WooCommerce", MTGCardsExport.class)).sendOrder(t);
			logger.debug("Order created " + ret);			
		});
		
		
		btnSave.addActionListener(e->{
			try {
				TransactionService.saveTransaction(t,true);
			} catch (SQLException e1) {
				MTGControler.getInstance().notify(e1);
			}
		});
		
		
		btnSend.addActionListener(e->{
			
			String text = JOptionPane.showInputDialog(this, "Tracking number ?");
			t.setTransporterShippingCode(text);
			try {
				TransactionService.sendTransaction(t);
			} catch (SQLException e1) {
				MTGControler.getInstance().notify(e1);
			}
			
		});

		btnAcceptTransaction.addActionListener(e->{
			loader.start();
			var sw = new AbstractObservableWorker<Void, MagicCardStock, MTGDao>(loader,getEnabledPlugin(MTGDao.class),t.getItems().size()) 
			{
				
				boolean fullTransaction=true;
				StringBuilder temp = new StringBuilder();
				@Override
				protected Void doInBackground() throws Exception {
					
					
					for(MagicCardStock transactionItem : t.getItems())
					{
							MagicCardStock stock = plug.getStockById(transactionItem.getIdstock());
							if(transactionItem.getQte()>stock.getQte())
							{
								   temp.append("Not enough stock for " + transactionItem.getIdstock() +":" + transactionItem.getMagicCard() + " " + transactionItem.getQte() +" > " + stock.getQte()).append(System.lineSeparator());
								   t.setStatut(STAT.IN_PROGRESS);
								   fullTransaction=false;
							}
							else
							{
								   stock.setQte(stock.getQte()-transactionItem.getQte());
								   stock.setUpdate(true);
								   plug.saveOrUpdateStock(stock);
								   plug.saveOrUpdateOrderEntry(TransactionService.toOrder(t, transactionItem));
							}
					}
					
					return null;
				}
				@Override
				protected void notifyEnd() {
					
					if(fullTransaction)
					{
						t.setStatut(STAT.ACCEPTED);
						try {
							TransactionService.validateTransaction(t);
						} catch (SQLException e) {
							MTGControler.getInstance().notify(e);
						}
					}
					else
					{
						logger.debug(temp);
						  
						MTGControler.getInstance().notify(new MTGNotification("Error Update", temp.toString(),MESSAGE_TYPE.WARNING));
					}
				}
				
				
				
			};
			ThreadManager.getInstance().runInEdt(sw, "update stock for transactions");
	});
	
	}


	@Override
	public String getTitle() {
		return "Transaction management";
	}
}