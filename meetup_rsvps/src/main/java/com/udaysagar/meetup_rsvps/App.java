package com.udaysagar.meetup_rsvps;

import java.util.Map;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.function.Function;
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
		int durationInSeconds = 5;

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
		 * Get the number of RSVP with value = yes
		 */
		JavaDStream <Long> rsvpsYesCount = rsvps.filter(new Function<Map, Boolean>(){
			public Boolean call(Map rsvp) {
				if (rsvp.get("response").toString().compareTo("yes") == 0){
					return true;
				}
				return false;
			}
		}).count();
		
		
		/*
		 * Get the number of RSVP with value = no
		 */
		JavaDStream <Long> rsvpsNoCount = rsvps.filter(new Function<Map, Boolean>(){
			public Boolean call(Map rsvp) {
				if (rsvp.get("response").toString().compareTo("no") == 0){
					return true;
				}
				return false;
			}
		}).count();
		
		rsvpsYes.print();
		
		jssc.start(); // Start the computation
		jssc.awaitTermination();
	}
}