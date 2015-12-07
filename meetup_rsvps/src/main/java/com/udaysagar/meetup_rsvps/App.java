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
							Connection connection = DatabaseHandler.getConnection();
							if(connection == null){
								System.out.println("Unable to get DB connection");
								return;
							}
							DatabaseHandler.insertRsvpsIntoDatabase(connection, rsvpMap);							
							//connection.close();
						}
					});
					return null;
				}
			}
		);
		
		//DatabaseHandler.closeConnection();
		jssc.start(); // Start the computation
		jssc.awaitTermination();
	}	
}