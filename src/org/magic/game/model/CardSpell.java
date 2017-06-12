package org.magic.game.model;

import java.awt.event.ActionEvent;

import org.magic.game.gui.components.DisplayableCard;

public class CardSpell extends AbstractSpell{

	DisplayableCard c;
	
	public CardSpell(String name, String description,DisplayableCard card) {
		super(name,description);
		this.c=card;
	}
	
	@Override
	public void actionPerformed(ActionEvent paramActionEvent) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getCost() {
		return c.getMagicCard().getCost();
	}

	@Override
	public boolean isStackable() {
		return !c.getMagicCard().getTypes().contains("Land");
	}

	
}
