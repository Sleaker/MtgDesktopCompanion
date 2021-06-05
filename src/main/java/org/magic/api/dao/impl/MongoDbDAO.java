package org.magic.api.dao.impl;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;
import static org.magic.tools.MTG.getPlugin;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Consumer;

import org.apache.commons.lang3.NotImplementedException;
import org.bson.Document;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.conversions.Bson;
import org.magic.api.beans.Contact;
import org.magic.api.beans.MagicCard;
import org.magic.api.beans.MagicCardAlert;
import org.magic.api.beans.MagicCardStock;
import org.magic.api.beans.MagicCollection;
import org.magic.api.beans.MagicEdition;
import org.magic.api.beans.MagicNews;
import org.magic.api.beans.OrderEntry;
import org.magic.api.beans.SealedStock;
import org.magic.api.beans.Transaction;
import org.magic.api.interfaces.MTGNewsProvider;
import org.magic.api.interfaces.abstracts.AbstractMagicDAO;
import org.magic.services.MTGConstants;
import org.magic.tools.Chrono;
import org.magic.tools.IDGenerator;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;

public class MongoDbDAO extends AbstractMagicDAO {

	private MongoDatabase db;
	private String colCards = "cards";
	private String colShops = "shops";
	private String colCollects = "collects";
	private String colStocks = "stocks";
	private String colAlerts = "alerts";
	private String colDecks = "decks";
	private String colNews = "news";
	private String colOrders = "orders";
	private String colSealed = "sealed";
	private String colContacts = "contacts";
	private String colTransactions = "transactions";
	
	private String dbIDField = "db_id";
	private String dbCardIDField = "card_id";
	private String dbEditionField = "edition";
	private String dbAlertField = "alertItem";
	private String dbOrdersField = "orderItem";
	private String dbNewsField = "newsItem";
	private String dbStockField = "stockItem";
	private String dbTransactionField = "transactionItem";
	private String dbStockSealedField = "stockSealedItem";
	private String dbColIDField = "collection.name";
	private String dbTypeNewsField = "typeNews";
	private MongoClient client;

	@Override
	public void initDefault() {
		setProperty(SERVERNAME, "localhost");
		setProperty(SERVERPORT, "27017");
		setProperty(DB_NAME, "mtgdesktopcompanion");
		setProperty(LOGIN, "login");
		setProperty(PASS, "");
		setProperty(DRIVER, "mongodb://");
		setProperty(PARAMETERS, "");

	}
		
	private <T> T deserialize(Object o, Class<T> classe) {
		
		if(o==null)
			return null;
		
		return serialiser.fromJson(String.valueOf(o.toString()), classe);

	}

	private String serialize(Object o) {
		return serialiser.toJson(o);
	}

	
	@Override
	public String getVersion() {
		return "3.12.8";
	
	}
	
	@Override
	public void deleteStock(SealedStock state) throws SQLException {
		Bson filter = new Document("stockSealedItem.id", state.getId());
		db.getCollection(colSealed).deleteOne(filter);
		notify(state);
		
	}
	
	@Override
	public List<SealedStock> listSeleadStocks() throws SQLException {
		List<SealedStock> stocks = new ArrayList<>();
		db.getCollection(colSealed, BasicDBObject.class).find().forEach((Consumer<BasicDBObject>) result -> stocks.add(deserialize(result.get(dbStockSealedField).toString(), SealedStock.class)));
		return stocks;
	}
	
	@Override
	public void saveOrUpdateStock(SealedStock state) throws SQLException {
		logger.debug("saving stock " + state);
		if (state.getId() == -1) {
			state.setId(Integer.parseInt(getNextSequence().toString()));
			var obj = new BasicDBObject();
			obj.put(dbStockSealedField, state);
			db.getCollection(colSealed, BasicDBObject.class).insertOne(BasicDBObject.parse(serialize(obj)));

		} else {
			Bson filter = new Document("stockSealedItem.id", state.getId());
			var obj = new BasicDBObject();
			obj.put(dbStockSealedField, state);
			logger.debug(filter);
			UpdateResult res = db.getCollection(colSealed, BasicDBObject.class).replaceOne(filter,BasicDBObject.parse(serialize(obj)));
			logger.trace(res);
		}
		
		notify(state);
		
	}
	
	
	@Override
	public STATUT getStatut() {
		return STATUT.BETA;
	}

	@Override
	public void unload() {
		if(client!=null)
			client.close();
	}
	
	public void init() throws SQLException {

		var pojoCodecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),fromProviders(PojoCodecProvider.builder().automatic(true).build()));
			

		
		var temp = new StringBuilder();
					  temp.append(getString(DRIVER)).append(getString(LOGIN)).append(":").append(getString(PASS));
					  temp.append("@").append(getString(SERVERNAME));
					  if(!getString(SERVERPORT).isEmpty())
						  temp.append(":").append(getString(SERVERPORT));
					  
					  temp.append("/");
					  if(!getString(PARAMETERS).isEmpty())
						  temp.append("?").append(getString(PARAMETERS));
					  
					  
			logger.debug(getName() + " connected to " + temp);		  
					  
			client = MongoClients.create(temp.toString());
		
			db = client.getDatabase(getString(DB_NAME)).withCodecRegistry(pojoCodecRegistry);
			createDB();
			logger.info("init " + getName() + " done");
		
	}

	public boolean createDB() {
		try {
			db.createCollection(colCards);
			db.createCollection(colShops);
			db.createCollection(colCollects);
			db.createCollection(colStocks);
			db.createCollection(colAlerts);
			db.createCollection(colDecks);
			db.createCollection(colNews);
			db.createCollection(colOrders);
			db.createCollection(colSealed);
			db.createCollection(colTransactions);
			db.createCollection(colContacts);
		} catch (Exception e) {
			logger.debug(e);
			return false;
		}
	
		for (String s : MTGConstants.getDefaultCollectionsNames())
		{
			try {
			saveCollection(s);
			}catch(Exception e)
			{
				//do nothing
			}
		}
	
		return true;

	}

	@Override
	public void saveCard(MagicCard mc, MagicCollection collection) throws SQLException {
		logger.debug("saveCard " + mc + " in " + collection);

		var obj = new BasicDBObject();
		obj.put(dbIDField, IDGenerator.generate(mc));
		obj.put("card", mc);
		obj.put(dbEditionField, mc.getCurrentSet().getId().toUpperCase());
		obj.put("collection", collection);
		String json = serialize(obj);

		db.getCollection(colCards, BasicDBObject.class).insertOne(BasicDBObject.parse(json));
	}

	@Override
	public void removeCard(MagicCard mc, MagicCollection collection) throws SQLException {
		logger.debug("removeCard " + mc + " from " + collection);

		DeleteResult dr = db.getCollection(colCards, BasicDBObject.class).deleteMany(Filters.and(Filters.eq(dbIDField,IDGenerator.generate(mc)),Filters.eq(dbColIDField,collection.getName())));
		logger.debug(dr.toString());

	}

	@Override
	public List<MagicCard> listCards() throws SQLException {
		logger.debug("list all cards");
		List<MagicCard> list = new ArrayList<>();
		db.getCollection(colCards, BasicDBObject.class).find().forEach((Consumer<BasicDBObject>) result -> list
				.add(deserialize(result.get("card").toString(), MagicCard.class)));
		return list;
	}

	@Override
	public Map<String, Integer> getCardsCountGlobal(MagicCollection c) throws SQLException {
		Map<String, Integer> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		List<Bson> aggr = Arrays.asList(Aggregates.match(Filters.eq(dbColIDField, c.getName())),
				Aggregates.group("$edition", Accumulators.sum("count", 1)));

		logger.trace(aggr.toString());

		db.getCollection(colCards, BasicDBObject.class).aggregate(aggr).forEach(
				(Consumer<BasicDBObject>) document -> map.put(document.getString("_id"), document.getInt("count")));
		return map;
	}


	
	@Override
	public List<MagicCard> listCardsFromCollection(MagicCollection collection) throws SQLException {
		return listCardsFromCollection(collection, null);
	}

	@Override
	public int getCardsCount(MagicCollection cols, MagicEdition me) throws SQLException {

		logger.debug("getCardsCount " + cols + " me:" + me);
		if (me != null) {
			return (int) db.getCollection(colCards, BasicDBObject.class).countDocuments(Filters.and(Filters.eq(dbColIDField,cols.getName()),Filters.eq(dbEditionField,me.getId().toUpperCase())));
		} else {
			return (int) db.getCollection(colCards, BasicDBObject.class)
					.countDocuments(new BasicDBObject(dbColIDField, cols.getName()));
		}

	}
	
	
	@Override
	public List<MagicCard> listCardsFromCollection(MagicCollection collection, MagicEdition me) throws SQLException {
	
		List<MagicCard> ret = new ArrayList<>();
		
		var b = Filters.eq(dbColIDField,collection.getName());
		
		
		if (me != null) {
			b = Filters.and(b,Filters.eq(dbEditionField,me.getId().toUpperCase()));
		}
		var c = new Chrono();
		c.start();
		db.getCollection(colCards, BasicDBObject.class).find(b).forEach((Consumer<BasicDBObject>) result -> ret.add(deserialize(result.get("card"), MagicCard.class)));
		logger.trace("list cards from " + collection + "/" + me + " :" + b+": done in "+c.stop()+"s.");
		
		return ret;
	}

	@Override
	public List<String> listEditionsIDFromCollection(MagicCollection collection) throws SQLException {
		List<String> ret = new ArrayList<>();
		var query = new BasicDBObject();
		query.put(dbColIDField, collection.getName());
		db.getCollection(colCards, BasicDBObject.class).distinct(dbEditionField, query, String.class).into(ret);
		return ret;
	}

	@Override
	public MagicCollection getCollection(String name) throws SQLException {
		MongoCollection<MagicCollection> collection = db.getCollection(colCollects, MagicCollection.class);
		return collection.find(Filters.eq("name", name)).first();
	}

	@Override
	public void saveCollection(MagicCollection c) throws SQLException {
		if(getCollection(c.getName())==null)
			db.getCollection(colCollects, MagicCollection.class).insertOne(c);
		else
			throw new SQLException(c.getName() + " already exists");
	}

	@Override
	public void removeCollection(MagicCollection c) throws SQLException {
		logger.debug("remove collection " + c);
		var query = new BasicDBObject();
		query.put(dbColIDField, c.getName());
		DeleteResult dr = db.getCollection(colCards).deleteMany(query);
		logger.trace(dr);
		db.getCollection(colCollects, MagicCollection.class).deleteOne(Filters.eq("name", c.getName()));
	}

	@Override
	public List<MagicCollection> listCollections() throws SQLException {
		MongoCollection<MagicCollection> collection = db.getCollection(colCollects, MagicCollection.class);
		List<MagicCollection> cols = new ArrayList<>();
		collection.find().into(cols);
		return cols;
	}

	@Override
	public void removeEdition(MagicEdition me, MagicCollection col) throws SQLException {
		logger.debug("delete " + me + " from " + col);

		var query = new BasicDBObject();
		query.put(dbColIDField, col.getName());
		query.put(dbEditionField, me.getId().toUpperCase());

		DeleteResult dr = db.getCollection(colCards).deleteMany(query);
		logger.debug(dr);
	}

	@Override
	public String getDBLocation() {
		return getString(SERVERNAME)+":"+getInt(SERVERPORT);
	}

	@Override
	public long getDBSize() {
		return db.runCommand(new BasicDBObject("dbstats", 1)).getLong("dataSize");
	}

	@Override
	public String getName() {
		return "MongoDB";
	}

	@Override
	public List<MagicCollection> listCollectionFromCards(MagicCard mc) throws SQLException {

		List<MagicCollection> ret = new ArrayList<>();
		var query = new BasicDBObject();
		query.put(dbIDField, IDGenerator.generate(mc));
		db.getCollection(colCards, BasicDBObject.class).distinct(dbColIDField, query, String.class)
				.forEach((Consumer<String>) result -> {
					try {
						logger.trace("found " + mc + " in " + result);
						ret.add(getCollection(result));
					} catch (SQLException e) {
						logger.error("Error", e);
					}

				});

		return ret;
	}

	@Override
	public List<MagicCardStock> listStocks(MagicCard mc, MagicCollection col,boolean editionStrict) throws SQLException {
		ArrayList<MagicCardStock> ret = new ArrayList<>();
		//TODO manage editionStrict value

		var filter = new BasicDBObject(dbCardIDField, IDGenerator.generate(mc));
		filter.put("stockItem.magicCollection.name", col.getName());
		logger.trace(filter);
		db.getCollection(colStocks, BasicDBObject.class).find(filter).forEach((Consumer<BasicDBObject>) result -> ret
				.add(deserialize(result.get(dbStockField).toString(), MagicCardStock.class)));

		return ret;
	}

	public List<MagicCardStock> listStocks() throws SQLException {
		List<MagicCardStock> stocks = new ArrayList<>();
		db.getCollection(colStocks, BasicDBObject.class).find().forEach((Consumer<BasicDBObject>) result -> stocks
				.add(deserialize(result.get(dbStockField).toString(), MagicCardStock.class)));
		return stocks;
	}

	@Override
	public void saveOrUpdateStock(MagicCardStock state) throws SQLException {
		logger.debug("saving stock " + state);
		state.setUpdate(false);
		if (state.getIdstock() == -1) {
			state.setIdstock(Integer.parseInt(getNextSequence().toString()));
			var obj = new BasicDBObject();
			obj.put(dbStockField, state);
			obj.put(dbCardIDField, IDGenerator.generate(state.getMagicCard()));
			db.getCollection(colStocks, BasicDBObject.class).insertOne(BasicDBObject.parse(serialize(obj)));

		} else {
			Bson filter = new Document("stockItem.idstock", state.getIdstock());
			var obj = new BasicDBObject();
			obj.put(dbStockField, state);
			obj.put(dbCardIDField, IDGenerator.generate(state.getMagicCard()));
			logger.debug(filter);
			UpdateResult res = db.getCollection(colStocks, BasicDBObject.class).replaceOne(filter,BasicDBObject.parse(serialize(obj)));
			logger.trace(res);
		}
		
		notify(state);
		
	}

	@Override
	public void saveAlert(MagicCardAlert alert) throws SQLException {
		logger.debug("saving alert " + alert);

		alert.setId(getNextSequence().toString());
		var obj = new BasicDBObject();
		obj.put(dbAlertField, alert);
		obj.put(dbIDField, IDGenerator.generate(alert.getCard()));
		db.getCollection(colAlerts, BasicDBObject.class).insertOne(BasicDBObject.parse(serialize(obj)));
		listAlerts.put(alert.getId(),alert);
	}

	@Override
	public void initAlerts() {
		db.getCollection(colAlerts, BasicDBObject.class).find().forEach((Consumer<BasicDBObject>) result ->{ 
				
				MagicCardAlert al = deserialize(result.get(dbAlertField).toString(), MagicCardAlert.class);
				listAlerts.put(al.getId(),al);
				});
	}


	@Override
	public void updateAlert(MagicCardAlert alert) throws SQLException {
		Bson filter = new Document("alertItem.id", alert.getId());
		var obj = new BasicDBObject();
		obj.put(dbAlertField, alert);
		obj.put(dbIDField, IDGenerator.generate(alert.getCard()));

		UpdateResult res = db.getCollection(colAlerts, BasicDBObject.class).replaceOne(filter,
				BasicDBObject.parse(serialize(obj)));
		logger.debug(res);

	}

	@Override
	public void deleteAlert(MagicCardAlert alert) throws SQLException {
		logger.debug("delete alert " + alert);
		Bson filter = new Document("alertItem.id", alert.getId());
		DeleteResult res = db.getCollection(colAlerts).deleteOne(filter);
		listAlerts.remove(alert.getId());
		logger.debug(res);
	}

	@Override
	public void deleteStock(List<MagicCardStock> state) throws SQLException {
		logger.debug("remove stocks : " + state.size() + " items");
		for (MagicCardStock s : state) {
			Bson filter = new Document("stockItem.idstock", s.getIdstock());
			db.getCollection(colStocks).deleteOne(filter);
			notify(s);
		}
	}

	@Override
	public void backup(File f) throws IOException {
		throw new NotImplementedException("Not yet implemented");
	}

	private void createCountersCollection(MongoCollection<Document> countersCollection) {
		var document = new Document();
		document.append("_id", "stock_increment");
		document.append("seq", 1);
		countersCollection.insertOne(document);
	}

	private Object getNextSequence() {
		MongoCollection<Document> countersCollection = db.getCollection("idSequences");
		if (countersCollection.countDocuments() == 0) {
			createCountersCollection(countersCollection);
		}
		var searchQuery = new Document("_id", "stock_increment");
		var increase = new Document("seq", 1);
		var updateQuery = new Document("$inc", increase);
		Document result = countersCollection.findOneAndUpdate(searchQuery, updateQuery);
		return result.get("seq");
	}

	@Override
	public List<MagicNews> listNews() {
		List<MagicNews> news = new ArrayList<>();
		db.getCollection(colNews, BasicDBObject.class).find().forEach((Consumer<BasicDBObject>) result ->{ 
			MagicNews mn = deserialize(result.get(dbNewsField).toString(), MagicNews.class);
			try{
				mn.setProvider(getPlugin(result.get(dbTypeNewsField).toString(),MTGNewsProvider.class));
			}catch(Exception e)
			{
				logger.error("error get typeNews provider "+result,e);
			}
			news.add(mn);
		});
			return news;
	}

	@Override
	public void deleteNews(MagicNews n) {
		Bson filter = new Document("newsItem.id", n.getId());
		db.getCollection(colNews).deleteOne(filter);
	}

	@Override
	public void saveOrUpdateNews(MagicNews state) {
	
		if (state.getId() == -1) {
			logger.debug("saving " + state);
			state.setId(Integer.parseInt(getNextSequence().toString()));
			var obj = new BasicDBObject();
			obj.put(dbNewsField, state);
			obj.put(dbTypeNewsField, state.getProvider().getName());
			db.getCollection(colNews, BasicDBObject.class).insertOne(BasicDBObject.parse(serialize(obj)));

		} else {
			logger.debug("update " + state);

			Bson filter = new Document("newsItem.id", state.getId());
			var obj = new BasicDBObject();
			obj.put(dbNewsField, state);
			obj.put(dbTypeNewsField, state.getProvider().getName());
			logger.debug(filter);
			UpdateResult res = db.getCollection(colNews, BasicDBObject.class).replaceOne(filter,BasicDBObject.parse(serialize(obj)));
			logger.debug(res);
		}

	}



	@Override
	public void saveOrUpdateOrderEntry(OrderEntry state) throws SQLException {
		logger.debug("saving " + state);
		state.setUpdated(false);
		if (state.getId() == -1) {
			state.setId(Integer.parseInt(getNextSequence().toString()));
			var obj = new BasicDBObject();
			obj.put(dbOrdersField, state);
			db.getCollection(colOrders, BasicDBObject.class).insertOne(BasicDBObject.parse(serialize(obj)));

		} else {
			Bson filter = new Document(dbOrdersField+".id", state.getId());
			var obj = new BasicDBObject();
			obj.put(dbOrdersField, state);
			logger.debug(filter);
			UpdateResult res = db.getCollection(colOrders, BasicDBObject.class).replaceOne(filter,BasicDBObject.parse(serialize(obj)));
			logger.trace(res);
		}
		
	}

	@Override
	public void deleteOrderEntry(List<OrderEntry> state) throws SQLException {
		logger.debug("remove " + state + " item(s) in orders");
		for (OrderEntry s : state) {
			Bson filter = new Document(dbOrdersField+".id", s.getId());
			DeleteResult res = db.getCollection(colOrders).deleteOne(filter);
			logger.debug(res.getDeletedCount() + " item deleted");
		}
	}

	@Override
	protected void initOrders() {
		db.getCollection(colOrders, BasicDBObject.class).find().forEach((Consumer<BasicDBObject>) result ->{
				OrderEntry o = deserialize(result.get(dbOrdersField).toString(), OrderEntry.class);
				listOrders.put(o.getId(),o);
			});
		
	}
	
	@Override
	public void executeQuery(String query) throws SQLException {
		throw new SQLException("Not implemented");
		
	}
	

	@Override
	public boolean equals(Object obj) {
		
		if(obj ==null)
			return false;
		
		return hashCode()==obj.hashCode();
	}
	
	@Override
	public int hashCode() {
		return getName().hashCode();
	}

	@Override
	public List<Transaction> listTransactions(Contact c) throws SQLException {
		List<Transaction> trans = new ArrayList<>();
		db.getCollection(colTransactions,BasicDBObject.class).find(Filters.eq("contact.id", c.getId())).forEach((Consumer<BasicDBObject>) result ->{
			Transaction o = deserialize(result.toString(), Transaction.class);
			trans.add(o);
		});
		
		return trans;
	}

	
	
	@Override
	public List<Transaction> listTransactions() throws SQLException {

		List<Transaction> trans = new ArrayList<>();
		db.getCollection(colTransactions, BasicDBObject.class).find().forEach((Consumer<BasicDBObject>) result ->{
			Transaction o = deserialize(result.toString(), Transaction.class);
			trans.add(o);
		});
		
		
		return trans;
	}


	@Override
	public List<Contact> listContacts() throws SQLException {
		MongoCollection<Contact> collection = db.getCollection(colContacts, Contact.class);
		List<Contact> cols = new ArrayList<>();
		collection.find().into(cols);
		return cols;
	}
	
	@Override
	public int saveOrUpdateTransaction(Transaction t) throws SQLException {
		if (t.getId() == -1) {
			t.setId(Integer.parseInt(getNextSequence().toString()));
			db.getCollection(colTransactions, BasicDBObject.class).insertOne(BasicDBObject.parse(serialize(t)));

		} else {
			UpdateResult res = db.getCollection(colTransactions, BasicDBObject.class).replaceOne(Filters.eq("id",t.getId()),BasicDBObject.parse(serialize(t)));
			logger.trace(res);
		}
		return t.getId();
	}



	@Override
	public void deleteTransaction(Transaction t) throws SQLException {
		logger.debug("remove " + t);
		Bson filter = new Document(dbTransactionField+".id", t.getId());
		db.getCollection(colTransactions).deleteOne(filter);
		
	}


	@Override
	public int saveOrUpdateContact(Contact c) throws SQLException {
		c.setPassword(IDGenerator.generateSha256(c.getPassword()));
		
		
		if(db.getCollection(colContacts, BasicDBObject.class).find(Filters.eq("email",c.getEmail())).first()!=null)
			throw new SQLException("Please choose another email");
		
		if (c.getId() == -1) {
			c.setId(Integer.parseInt(getNextSequence().toString()));
			
		db.getCollection(colContacts, BasicDBObject.class).insertOne(BasicDBObject.parse(serialize(c)));

		} else {
			UpdateResult res = db.getCollection(colContacts, BasicDBObject.class).replaceOne(Filters.eq("id",c.getId()),BasicDBObject.parse(serialize(c)));
			logger.debug(res);
		}
		return c.getId();
	}


	
	@Override
	public Transaction getTransaction(int id) throws SQLException {
		return deserialize(db.getCollection(colTransactions,BasicDBObject.class).find(Filters.eq("id", id)).first(),Transaction.class);
	}
	
	@Override
	public Contact getContactById(int id) throws SQLException {
		return deserialize(db.getCollection(colContacts).find(Filters.eq("id", id),BasicDBObject.class).first(),Contact.class);
	}


	@Override
	public Contact getContactByLogin(String email, String password) throws SQLException {
		return deserialize(db.getCollection(colContacts,BasicDBObject.class).find(Filters.and(Filters.eq("email", email),Filters.eq("password", IDGenerator.generateSha256(password)))).first(),Contact.class);
	}
	

}

