package org.magic.gui.models;

import java.util.Date;
import java.util.List;

import org.magic.api.beans.Transaction;
import org.magic.gui.abstracts.GenericTableModel;

public class TransactionsModel extends GenericTableModel<Transaction> {

	
	public TransactionsModel() {
		columns = new String[] { "ID","DATEPROPOSITION","CONTACT","ITEMS","TOTAL","STATUT" };
	}
	
	
	
	@Override
	public Object getValueAt(int row, int column) {
		
		Transaction it = items.get(row);
		
		switch (column) 
		{
			case 0 : return it;
			case 1 : return it.getDateProposition();
			case 2 : return it.getContact();
			case 3 : return it.getItems().size();
			case 4 : return it.total();
			case 5 : return it.getStatut();
			default : return 0;
		}
	}
	
	
	@Override
	public Class<?> getColumnClass(int columnIndex) {
		if(columnIndex==1)
			return Date.class;
		
		if(columnIndex==4)
			return Double.class;
		
		return super.getColumnClass(columnIndex);
	}
	
}
