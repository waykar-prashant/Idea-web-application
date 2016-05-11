package hello;

import java.util.List;

import org.codehaus.jettison.json.JSONObject;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;

public class MongoDBConnection {
	public static MongoDBConnection databaseConnection = null;
	private static DB dbConn;

	private static void initializeDBConnection() {
		try {
			MongoClientURI uri = new MongoClientURI("mongodb://root:password@localhost:27017/idea");
			MongoClient mongoClient = new MongoClient(uri);
			dbConn = mongoClient.getDB("idea");
		} catch (Exception e) {
			System.out.println("Error in get database connection: " + e.getMessage());
		}
	}
	
	public static DB getDBConnection(){
		if(dbConn == null){
			initializeDBConnection();
		}
		return dbConn;
	}

	public static DBCollection getCollection(String collectionName) {
		DBCollection dbCollection = null;
		try {
			
			boolean collectionExists = getDBConnection().collectionExists(collectionName);
			if (collectionExists == false) {
				getDBConnection().createCollection(collectionName, null);
			}
			dbCollection = getDBConnection().getCollection(collectionName);
		} catch (Exception e) {
			System.out.println("Error in get database connection: " + e.getMessage());
		}
		return dbCollection;
	}
	
	public static JSONObject getActiveTemperature(){
		try {
			DBCursor cur = getCollection("active-temperature").find().limit(1).sort(new BasicDBObject("$natural", -1));
			while(cur.hasNext()){
				//System.out.println(cur.next());
				return new JSONObject(cur.next().toString());
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
		return null;
	}
	
	public static void main(String [] args){
		DB db = null;
		DBCollection dbCollection = null;

		try {
			MongoClientURI uri = new MongoClientURI("mongodb://root:password@localhost:27017/idea");
			MongoClient mongoClient = new MongoClient(uri);
			db = mongoClient.getDB("idea");
			//boolean collectionExists = db.collectionExists(collectionName);
			List<String> dbs = mongoClient.getDatabaseNames();
			for(String d : dbs){
				//System.out.println(d);
			}
			getActiveTemperature();
		} catch (Exception e) {
			System.out.println("Error in get database connection: " + e.getMessage());
		}
	}
}
