package org.magic.tools;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.magic.api.beans.MagicCard;
import org.magic.api.beans.MagicEdition;

public class IDGenerator {

	static final Logger logger = LogManager.getLogger(IDGenerator.class.getName());

	public static String generate(MagicCard mc)
	{
		return generate(mc,mc.getEditions().get(0));
	}
	
	public static String generate(MagicCard mc, MagicEdition ed)
	{
		//String id = ed.getId()+mc.getName()+mc.getImageName(); --> mtgjson
		
		String id = String.valueOf((mc.getName()+ed+ed.getNumber()+ed.getMultiverse_id()));
		id = DigestUtils.sha1Hex(id);
		
		logger.trace("Generate ID for " + String.valueOf((mc.getName()+"|"+ed+"|"+ed.getNumber()+"|"+ed.getMultiverse_id()))+"="+id);
		
		return id;
	}
}
