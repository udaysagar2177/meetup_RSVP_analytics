package com.udaysagar.meetup_rsvps;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.VoidFunction;
import org.apache.spark.storage.StorageLevel;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;


public class App {
	public static void main(String[] args) throws Exception {
		Logger.getLogger("org").setLevel(Level.OFF);
		Logger.getLogger("akka").setLevel(Level.OFF);

		String masterURL = "local[4]";
		String appName = "Meetup RSVPs";
		String URL = "http://stream.meetup.com/2/rsvps";
		int durationInSeconds = 2;

		SparkConf conf = new SparkConf().setMaster(masterURL).setAppName(appName);
		JavaStreamingContext jssc = new JavaStreamingContext(conf, Durations.seconds(durationInSeconds));

		JavaDStream <Map> rsvps = jssc.receiverStream(new JavaCustomReceiver(URL));

		// Get the RSVP with value = yes
		JavaDStream <Map> rsvpsYes = rsvps.filter(new Function<Map, Boolean>(){
			public Boolean call(Map rsvp) {
				if (rsvp.get("response").toString().compareTo("yes") == 0){
					return true;
				}
				return false;
			}
		});
		/*
		 * Spark is useful when the input data coming is huge (in future).
		 */
		
		rsvpsYes.foreachRDD(
			new Function<JavaRDD<Map>, Void>() {
				public Void call(JavaRDD<Map> rsvpRDD) throws Exception {
					rsvpRDD.foreach(new VoidFunction<Map>(){
						public void call(Map rsvpMap) throws Exception {
							Connection connection = getConnection();
							if(connection == null){
								System.out.println("Unable to get DB connection");
								return;
							}
							//insertRsvpsIntoDatabase(connection, rsvpMap);							
							connection.close();
						}
					});
					return null;
				}
			}
		);
		
		jssc.start(); // Start the computation
		jssc.awaitTermination();
	}
	
	private static void insertRsvpsIntoDatabase(Connection connection, Map rsvpMap) {
		//System.out.println("ready to insert records");
		String groupTopics = getGroupTopics(rsvpMap);
		
		String insertEvent = "INSERT IGNORE INTO EventData"
				+ "(visibility, event_id, event_name, event_time, group_topics, group_city, group_country, group_name, group_lon, group_lat) "
				+ " VALUES (\""
				+ rsvpMap.get("visibility") + "\", \""
				+ ((Map) rsvpMap.get("event")).get("event_id") + "\", \""
				+ ((Map) rsvpMap.get("event")).get("event_name") + "\", "
				+ "FROM_UNIXTIME(" + ((Map) rsvpMap.get("event")).get("time") + "), \""
				+ groupTopics+ "\", \""
				+ ((Map) rsvpMap.get("group")).get("group_city") + "\", \""
				+ ((Map) rsvpMap.get("group")).get("group_country") + "\", \""
				+ ((Map) rsvpMap.get("group")).get("group_name") + "\", "
				+ ((Map) rsvpMap.get("group")).get("group_lon") + ", "
				+ ((Map) rsvpMap.get("group")).get("group_lat")
				+ ")";
		
		String insertRSVP  = "INSERT INTO RsvpData"
				+ "(rsvp_time, event_id)"
				+ " VALUES ("
				+ "FROM_UNIXTIME("+rsvpMap.get("mtime") + "), \""
				+ ((Map) rsvpMap.get("event")).get("event_id")
				+ "\")";
		
		try {
			
			// Insert Event Data
			PreparedStatement preparedStatement = connection.prepareStatement(insertEvent);
			preparedStatement.executeUpdate();
			
			// Insert RSVP Data
			preparedStatement = connection.prepareStatement(insertRSVP);
			preparedStatement.executeUpdate();
			
		} catch (SQLException e) {
			System.out.println("SQL Exception in Event or RSVP :");
			System.out.println(insertEvent);
			System.out.println(insertRSVP);
			e.printStackTrace();
		}
		

	}
	
	private static String getGroupTopics(Map<String, Object> rsvpMap) {
		StringBuffer sb = new StringBuffer();
		Map<String, Object> group = (Map) rsvpMap.get("group");
		ArrayList<Map<String, String>> groupTopics = (ArrayList) group.get("group_topics");
		//System.out.println(groupTopics);
		sb.append("");
		for(Map<String, String> groupTopic : groupTopics){
			sb.append(groupTopic.get("topic_name"));
			sb.append(", ");
		}
		return sb.toString();
	}

	private static Connection getConnection(){
		final String DB_DRIVER = "com.mysql.jdbc.Driver";
		final String DB_CONNECTION = "jdbc:mysql://52.23.29.98:3306/MeetupRsvps";
		final String DB_USER = "spark";
		final String DB_PASSWORD = "spark";
		Connection dbConnection = null;
		try {
			Class.forName(DB_DRIVER);
		} catch (ClassNotFoundException e) {
			System.out.println("Error with the Class.forName(DB_DRIVER)");
		}
		try {
			dbConnection = (Connection) DriverManager
				    .getConnection(DB_CONNECTION, DB_USER, DB_PASSWORD);
		} catch (SQLException e) {
			
			e.printStackTrace();
		}
		return dbConnection;
	}
}