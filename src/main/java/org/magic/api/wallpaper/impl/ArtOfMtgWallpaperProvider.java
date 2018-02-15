package org.magic.api.wallpaper.impl;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.magic.api.beans.MagicCard;
import org.magic.api.beans.MagicEdition;
import org.magic.api.beans.Wallpaper;
import org.magic.api.interfaces.MTGCardsProvider.STATUT;
import org.magic.api.interfaces.MTGWallpaperProvider;
import org.magic.api.interfaces.abstracts.AbstractWallpaperProvider;

public class ArtOfMtgWallpaperProvider extends  AbstractWallpaperProvider {

	String url="http://www.artofmtg.com";
	String userAgent="Mozilla/5.0 (X11; U; Linux x86_64; en-US; rv:1.9.2.13) Gecko/20101206 Ubuntu/10.10 (maverick) Firefox/3.6.13";

	
	public static void main(String[] args) {
		new ArtOfMtgWallpaperProvider().search("liliana");
	}
	
	@Override
	public List<Wallpaper> search(String search) {
		List<Wallpaper> list = new ArrayList<>();
		try {
			
			Document d = Jsoup.connect(url+"/?s="+search)
					  .userAgent(userAgent)
					  .get();
			
			for(Element e : d.select("article.result"))
			{
				Wallpaper w = new Wallpaper();
				w.setName(e.select("h2 a").text());
				w.setUrl(new URL(e.select("a img").attr("src")));
				w.setFormat(FilenameUtils.getExtension(w.getUrl().toString()));
				list.add(w);
			}
			return list;
		}
		catch (IOException e) {
			logger.error(e);
			return list;
		}
		
		
	}
	
	@Override
	public List<Wallpaper> search(MagicEdition ed) {
		List<Wallpaper> list = new ArrayList<>();
		try {
			
			Document d = Jsoup.connect(url+"/set/"+ed.getSet().toLowerCase().replaceAll(" ", "-"))
					  .userAgent(userAgent)
					  .get();
			
			for(Element e : d.select("div.elastic-portfolio-item img"))
			{
				Wallpaper w = new Wallpaper();
				w.setName(e.attr("title"));
				w.setUrl(new URL(e.attr("src")));
				w.setFormat(FilenameUtils.getExtension(w.getUrl().toString()));
				list.add(w);
			}
			return list;
		}
		catch (IOException e) {
			logger.error(e);
			return list;
		}
		
	}

	@Override
	public List<Wallpaper> search(MagicCard card) {
		return search(card.getName());
	}

	@Override
	public String getName() {
		return "Art of Mtg";
	}

	@Override
	public STATUT getStatut() {
		return STATUT.DEV;
	}
	
	

}
