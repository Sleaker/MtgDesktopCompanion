package org.magic.gui.models;

import org.magic.api.beans.CardShake;
import org.magic.gui.abstracts.GenericTableModel;

public class CardsShakerTableModel extends GenericTableModel<CardShake> {

	private static final long serialVersionUID = 1L;
	

	public CardsShakerTableModel() {
		columns = new String[] { "CARD",
				"EDITION",
				"PRICE",
				"DAILY",
				"PC_DAILY" };
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		switch (columnIndex) {
		case 0:
			return CardShake.class;
		case 1:
			return String.class;
		case 2:
			return Double.class;
		case 4:
			return Double.class;
		default:
			return super.getColumnClass(columnIndex);
		}
	}


	@Override
	public Object getValueAt(int row, int column) {
		try {

			CardShake mp = items.get(row);
			switch (column) {
			case 0:
				return mp;
			case 1:
				return mp.getEd();
			case 2:
				return mp.getPrice();
			case 3:
				return mp.getPriceDayChange();
			case 4:
				return mp.getPercentDayChange();
			case 5:
				return mp.getPriceDayChange();
			default:
				return 0;
			}
		} catch (IndexOutOfBoundsException ioob) {
			logger.error(ioob);
			return null;
		}
	}

}
