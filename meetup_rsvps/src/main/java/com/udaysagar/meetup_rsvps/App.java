package com.udaysagar.meetup_rsvps;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
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
		
		// Save the RDD in memory for future calculations
		// rsvpsYes.persist(StorageLevel.MEMORY_ONLY());
		
		/*
		 * Spark is useful when the input data coming is huge (in future).
		 */
		
		/*
		 * Trim the data and insert into the database
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
							insertRsvpsIntoDatabase(rsvpMap);							
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
	
	private static void insertRsvpsIntoDatabase(Map rsvpMap) {
		System.out.println("ready to insert records");
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
			System.out.println("class forname error");
		}
		try {
			dbConnection = (Connection) DriverManager
				    .getConnection(DB_CONNECTION, DB_USER, DB_PASSWORD);
		} catch (SQLException e) {
			System.out.println("db connectoin error");
			e.printStackTrace();
		}
		return dbConnection;
	}
}