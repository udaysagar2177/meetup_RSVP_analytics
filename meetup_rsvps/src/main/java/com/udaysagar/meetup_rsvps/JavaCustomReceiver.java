package com.udaysagar.meetup_rsvps;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.URL;
import java.net.URLConnection;

import org.apache.spark.storage.StorageLevel;
import org.apache.spark.streaming.receiver.Receiver;

public class JavaCustomReceiver extends Receiver<String> {

	  String host = null;

	  public JavaCustomReceiver(String host_) {
	    super(StorageLevel.MEMORY_AND_DISK_2());
	    host = host_;
	  }

	  public void onStart() {
	    // Start the thread that receives data over a connection
	    new Thread()  {
	      @Override public void run() {
	        receive();
	      }
	    }.start();
	  }

	  public void onStop() {
	    // There is nothing much to do as the thread calling receive()
	    // is designed to stop by itself if isStopped() returns false
	  }

	  /** Create a socket connection and receive data until receiver is stopped */
	  private void receive() {
	   
	    String userInput = null;

	    try {
	      // connect to the server
	      URLConnection connection = new URL("http://stream.meetup.com/2/rsvps/").openConnection();;
	      connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
	      connection.connect();

	      BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

	      // Until stopped or connection broken continue reading
	      while (!isStopped() && (userInput = reader.readLine()) != null) {
	        System.out.println("Received data '" + userInput + "'");
	        store(userInput);
	      }
	      reader.close();
	      

	      // Restart in an attempt to connect again when server is active again
	      restart("Trying to connect again");
	    } catch(ConnectException ce) {
	      // restart if could not connect to server
	      restart("Could not connect", ce);
	    } catch(Throwable t) {
	      // restart if there is any other error
	      restart("Error receiving data", t);
	    }
	  }
	}
