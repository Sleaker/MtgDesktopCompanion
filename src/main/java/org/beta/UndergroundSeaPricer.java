package org.beta;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.nodes.Document;
import org.magic.api.beans.MagicCard;
import org.magic.api.beans.MagicEdition;
import org.magic.api.beans.MagicPrice;
import org.magic.api.interfaces.abstracts.AbstractMagicPricesProvider;
import org.magic.tools.URLTools;
import org.mozilla.javascript.Parser;
import org.mozilla.javascript.ast.AstNode;
import org.python.icu.util.Calendar;
import org.mozilla.javascript.Token;
public class UndergroundSeaPricer extends AbstractMagicPricesProvider {

	private static final String BASE_URL="http://www.usmtgproxy.com/";
	
	public static void main(String[] args) throws IOException {
		new UndergroundSeaPricer().getLocalePrice(null, null);
	}
	
	
	@Override
	public String getName() {
		return "UnderGroundSea";
	}

	@Override
	protected List<MagicPrice> getLocalePrice(MagicEdition me, MagicCard card) throws IOException {
		String month = String.format("%02d",Calendar.getInstance().get(Calendar.MONTH)-1);
		int year = Calendar.getInstance().get(Calendar.YEAR);
		String url = BASE_URL + "wp-content/uploads/"+year+"/"+month+"/proxycardslist.html";
		
		
		Document d = URLTools.extractHtml(url);
		AstNode root = new Parser().parse(d.select("script").get(1).html(), "", 1);
		root.visit(visitedNode -> {
			
		
			switch (visitedNode.getType()) 
			{
				case Token.NAME:System.out.println(visitedNode.getString());break;
				case Token.STRING:break;
				case Token.NUMBER:break;
				case Token.OBJECTLIT:break;
				case Token.ARRAYLIT:break;
			}
			return true;
		});
		
		return new ArrayList<>();
	}

}