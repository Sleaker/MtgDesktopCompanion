package org.magic.api.dashboard.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.magic.api.beans.CardDominance;
import org.magic.api.beans.CardShake;
import org.magic.api.beans.EditionsShakers;
import org.magic.api.beans.HistoryPrice;
import org.magic.api.beans.MagicCard;
import org.magic.api.beans.MagicEdition;
import org.magic.api.beans.MagicFormat.FORMATS;
import org.magic.api.beans.Packaging;
import org.magic.api.interfaces.abstracts.AbstractDashBoard;
import org.api.mtgstock.modele.CardSet;
import org.api.mtgstock.modele.FullPrint;
import org.api.mtgstock.modele.Interest;
import org.api.mtgstock.modele.Played;
import org.api.mtgstock.modele.Print;
import org.api.mtgstock.modele.SearchResult;
import org.api.mtgstock.modele.SetPricesAnalysis;
import org.api.mtgstock.services.AnalyticsService;
import org.api.mtgstock.services.CardsService;
import org.api.mtgstock.services.InterestsService;
import org.api.mtgstock.services.PriceService;
import org.api.mtgstock.tools.MTGStockConstants;
import org.api.mtgstock.tools.MTGStockConstants.CATEGORY;
import org.api.mtgstock.tools.MTGStockConstants.FORMAT;
import org.api.mtgstock.tools.MTGStockConstants.PRICES;

public class MTGStockDashBoard extends AbstractDashBoard {

	private static final String GET_FOIL = "GET_FOIL";
	private static final String AVERAGE_MARKET = "AVERAGE_MARKET";
	private CardsService cardService;
	private InterestsService interestService;
	private PriceService pricesService;
	private AnalyticsService analyticService;
	
	@Override
	public String getVersion() {
		return MTGStockConstants.VERSION;
	}
	
	@Override
	public STATUT getStatut() {
		return STATUT.BETA;
	}
	
	public MTGStockDashBoard() {
		cardService = new CardsService();
		interestService = new InterestsService();
		pricesService = new PriceService();
		analyticService = new AnalyticsService();
	}
	

	@Override
	public List<CardDominance> getBestCards(FORMATS f, String filter) throws IOException {
		List<CardDominance> ret = new ArrayList<>();
		
		int i=1;
		for(Played p : analyticService.getMostPlayedCard(FORMAT.valueOf(f.name()))) {
			CardDominance cd = new CardDominance();
			cd.setCardName(p.getName());
			cd.setPlayers(p.getQuantity());
			cd.setPosition(i++);			
			ret.add(cd);
		}
		return ret;
	}

	@Override
	protected HistoryPrice<Packaging> getOnlinePricesVariation(Packaging packaging) throws IOException {
		
		
		
		HistoryPrice<Packaging> ret = new HistoryPrice<>(packaging);
		CardSet cs = cardService.getSetByCode(packaging.getEdition().getId());
		SetPricesAnalysis sp = pricesService.getSetPricesAnalysis(cs);
		
		PRICES p = PRICES.AVG;
		
		if(getBoolean(GET_FOIL))
			p = PRICES.FOIL;
		
		
		if(sp!=null)
		{
			ret.setFoil(false);
			ret.setCurrency(getCurrency());
			switch(packaging.getType())
			{
				case BOOSTER : sp.getPrices().get(p).entrySet().forEach(e->ret.getVariations().put(e.getKey(),e.getValue()));break;
				case BOX : if(sp.getBooster()!=null)
							{ 
								ret.getVariations().put(new Date(), sp.getBooster().getNum() * sp.getBooster().getAvg().get(0).getValue());
							}
							break;
				
				case BANNER:break;
				case BUNDLE:break;
				case CONSTRUCTPACK:break;
				case PRERELEASEPACK:break;
				case STARTER:break;
				default:break;
			}
			
		}
		
		
		return ret;
	}

	@Override
	protected List<CardShake> getOnlineShakerFor(FORMATS f) throws IOException {
		List<CardShake> ret = new ArrayList<>();
		
		FORMAT mtgstockformat = null;
		
		if(f!=null)
			mtgstockformat = FORMAT.valueOf(f.name());
		
		
		
		CATEGORY c=CATEGORY.valueOf(getString(AVERAGE_MARKET).toUpperCase());
		
		
		logger.debug("Parsing shakers for " + f +" "+ c);
		
		List<Interest> st;
		
		if(getBoolean(GET_FOIL))
			st = interestService.getInterestFor(c,mtgstockformat);
		else
			st = interestService.getInterestFor(c,false,mtgstockformat);
		
		
		
		st.forEach(i->{
			
			CardShake cs = initFromPrint(i.getPrint());
						cs.setDateUpdate(i.getDate());
						cs.setPrice(i.getPricePresent());
						cs.setPercentDayChange(i.getPercentage());
						cs.setPriceDayChange(i.getPriceDayChange());
						cs.setFoil(i.isFoil());
						
						
			ret.add(cs);
		});
		
		
		
		return ret;
	}
	
	
	@Override
	protected EditionsShakers getOnlineShakesForEdition(MagicEdition ed) throws IOException {
	
		EditionsShakers es = new EditionsShakers();
						es.setProviderName(getName());
						es.setDate(new Date());
						es.setEdition(ed);
		
		PRICES c = PRICES.valueOf(getString(AVERAGE_MARKET).toUpperCase());
						
						
		logger.debug("Parsing shakers for " + ed + " " + c);
		cardService.getPrintsBySetCode(ed.getId()).forEach(p->{
					CardShake cs = initFromPrint(p);
					cs.setEd(ed.getId());
					cs.init(p.getLatestPrices().get(c), p.getLastWeekPreviousPrice(), p.getLastWeekPrice());
					es.getShakes().add(cs);
			});
		return es;
	}

	@Override
	protected HistoryPrice<MagicCard> getOnlinePricesVariation(MagicCard mc, MagicEdition ed, boolean foil)throws IOException {
		HistoryPrice<MagicCard> hp = new HistoryPrice<>(mc);
		if(mc==null)
		{
			logger.error("couldn't calculate edition only");
			return hp;
		}
		
		if(ed==null)
			ed=mc.getCurrentSet();
		
		
		Integer id = mc.getMtgstocksId();

		if(id==null)
		{
			SearchResult rs = cardService.getBestResult(mc.getName());
			FullPrint fp = cardService.getCard(rs);
			CardSet set = cardService.getSetByCode(ed.getId());
			Print fpSet = fp.getPrintForSetId(set.getId());
			
			logger.debug("found " + fpSet +" " + fpSet.getSetName());
			id = fpSet.getId();
		}
		
		PRICES p = PRICES.AVG;
		
		if(foil || ed.isFoilOnly())
			p = PRICES.FOIL;
		
	
		pricesService.getPricesFor(id,p).forEach(e->{
			
			hp.setCurrency(getCurrency());
			hp.setFoil(foil);
			hp.getVariations().put(e.getKey(), e.getValue());
			hp.setSerieName(hp.toString());
		});
		
		
		return hp;
		
	}

	private CardShake initFromPrint(Print p)
	{
		CardShake cs = new CardShake();
				cs.setCurrency(getCurrency());
				cs.setName(p.getCleanName());
				cs.setFoil(p.isFoil());
				cs.setLink(p.getWebPage());
				cs.setBorderless(p.isBorderless());
				cs.setShowcase(p.isShowcase());
				cs.setExtendedArt(p.isExtendedArt());
				cs.setEd(cardService.getSetById(p.getSetId()).getAbbrevation());
		return cs;
	}
	

	@Override
	public Date getUpdatedDate() {
		return new Date();
	}

	@Override
	public String getName() {
		return "MTGStocks";
	}


	@Override
	public void initDefault() {
		setProperty("LOGIN", "login@mail.com");
		setProperty("PASS", "changeme");
		setProperty(AVERAGE_MARKET, "average"); // average // market
		setProperty(GET_FOIL,"false");
	}
	

}
