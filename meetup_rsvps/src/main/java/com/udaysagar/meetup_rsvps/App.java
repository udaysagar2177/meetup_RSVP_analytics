package com.udaysagar.meetup_rsvps;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.spark.SparkConf;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;

public class App 
{
	public static void main(String[] args) throws Exception {
		Logger.getLogger("org").setLevel(Level.OFF);
		Logger.getLogger("akka").setLevel(Level.OFF);
	    
		SparkConf conf = new SparkConf().setMaster("local[4]").setAppName("Meetup RSVPs");
	    JavaStreamingContext jssc = new JavaStreamingContext(conf, Durations.seconds(5));
		
	    JavaDStream<String> customReceiverStream = jssc.receiverStream(new JavaCustomReceiver("http://stream.meetup.com/2/rsvps"));
	    
	    customReceiverStream.print();
	    
	    jssc.start();             // Start the computation
	    jssc.awaitTermination();
	}
}