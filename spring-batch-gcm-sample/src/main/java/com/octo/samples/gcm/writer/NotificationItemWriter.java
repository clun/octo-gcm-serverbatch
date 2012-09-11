package com.octo.samples.gcm.writer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.InitializingBean;

import com.google.android.gcm.server.Constants;
import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.Message.Builder;
import com.google.android.gcm.server.MulticastResult;
import com.google.android.gcm.server.Result;
import com.google.android.gcm.server.Sender;

/**
 * GCM Notification Writer.
 * 
 * @author octo
 */
public class NotificationItemWriter implements InitializingBean, ItemWriter < String > {
	
	/** Logger pour la classe. */
	private Logger logger = Logger.getLogger(getClass());
	
    /** Api Key to access GCM <should be externalised> */
    private String apiKey = "<YOUR_PERSONAL_API_KEY>";
    
	/** Message Sender. */
	private Sender sender;
	
	/** The message to sent. */
	private Message messageToSend;
	
	/** {@inheritDoc}  */
	public void afterPropertiesSet() throws Exception {
		sender = new Sender(apiKey);
		Builder mb = new Message.Builder();
		// Used to eventually group messages and override previous sendings
		mb.collapseKey(String.valueOf(System.currentTimeMillis()));
		// DATA
		mb.addData("title", "Here is my notification Title");
		mb.addData("message", "GCM is now available.");
		// Custom extra param...
		mb.addData("productCode", "503");
		
		// Initialize message
		messageToSend = mb.build();
	}

	/** {@inheritDoc}  */
	public void write(List<? extends String> items) throws Exception {
		
		List<String> devices = new ArrayList<String>(items);
		List<Result> results;
		try {
			// message with single adressee
		    if (devices.size() == 1) {
		       logger.info("Sending message to a single device");
		       // Retries are handled by spring-batch and no the sender
		       // But you can use : sender.send(messageToSend, devices.get(0), retryCount)
		       Result result = sender.sendNoRetry(messageToSend, devices.get(0));
		       results = Arrays.asList(result);
		    // muticasting
		    } else {
		       logger.info("Multicasting message to a " + items.size() + " devices.");
		       MulticastResult result = sender.sendNoRetry(messageToSend, devices);
		       results = result.getResults();
		    }
		    
		    // analyze the results
		    logger.debug("Checking GCM results : ");
		    for (int i = 0; i < devices.size(); i++) {
		        Result result = results.get(i);
		        if (result.getMessageId() != null) {
		          logger.info("  --> Succesfully sent message to device #" + i);
		          String canonicalRegId = result.getCanonicalRegistrationId();
		          // Some token should be updated in database
		          if (canonicalRegId != null) {
		        	devices.set(i, canonicalRegId);
		        	// < HERE DO CALL TO UPDATE TOKEN DATABASE >
		        	logger.info("The device " + i + " has updated its its");
		            }
		        } else {
		          String error = result.getErrorCodeName();
		          if (Constants.ERROR_NOT_REGISTERED.equals(error)) {
		            logger.info("App has been removed on device " + i + ", unregister");
		             // < HERE DO CALL TO UPDATE TOKEN DATABASE >
		          } else {
		            logger.error("  --> An error occured fo device " + i + " : " + error);
		            // HERE UPDATE REJECTED ITEMS.... 
		          }
		        }
		     }
		    
		} catch(Exception e) {
			logger.error("Fatal error ", e);
			throw e;
		}
	}
}

	
