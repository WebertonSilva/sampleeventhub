package br.com.ri.mysampleeventhub.processor;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

import org.springframework.stereotype.Service;

import com.microsoft.azure.eventhubs.ConnectionStringBuilder;
import com.microsoft.azure.eventprocessorhost.EventProcessorHost;
import com.microsoft.azure.eventprocessorhost.EventProcessorOptions;

import br.com.ri.mysampleeventhub.processor.Error.ErrorNotificationHandler;
import br.com.ri.mysampleeventhub.utils.EventHubParameters;

@Service
public class RunProcessor {	
	
	public EventHubParameters params = new EventHubParameters();
	
	public void run() throws InterruptedException, ExecutionException{
	 
		
		// SETUP SETUP SETUP SETUP
    	// Fill these strings in with the information of the Event Hub you wish to use. The consumer group
    	// can probably be left as-is. You will also need the connection string for an Azure Storage account,
    	// which is used to persist the lease and checkpoint data for this Event Hub. The Storage container name
    	// indicates where the blobs used to implement leases and checkpoints will be placed within the Storage
    	// account. All instances of EventProcessorHost which will be consuming from the same Event Hub and consumer
    	// group must use the same Azure Storage account and container name.
		
		
    	UUID hostNamePrefix = UUID.randomUUID();
    	
    	// To conveniently construct the Event Hub connection string from the raw information, use the ConnectionStringBuilder class.
    	ConnectionStringBuilder eventHubConnectionString = new ConnectionStringBuilder()
    			.setNamespaceName(params.getNamespaceName())
    			.setEventHubName(params.getEventHubName())
    			.setSasKeyName(params.getSasKeyName())
    			.setSasKey(params.getSasKey());
    	
		// Create the instance of EventProcessorHost using the most basic constructor. This constructor uses Azure Storage for
		// persisting partition leases and checkpoints. The host name, which identifies the instance of EventProcessorHost, must be unique.
		// You can use a plain UUID, or use the createHostName utility method which appends a UUID to a supplied string.
		EventProcessorHost host = EventProcessorHost.EventProcessorHostBuilder
				.newBuilder(EventProcessorHost.createHostName(hostNamePrefix.toString()), params.getConsumerGroupName())
				.useAzureStorageCheckpointLeaseManager(params.getStorageConnectionString(), params.getStorageContainerName(), null)
				.useEventHubConnectionString(eventHubConnectionString.toString(), params.getEventHubName())
				.build();
		
		// Registering an event processor class with an instance of EventProcessorHost starts event processing. The host instance
		// obtains leases on some partitions of the Event Hub, possibly stealing some from other host instances, in a way that
		// converges on an even distribution of partitions across all host instances. For each leased partition, the host instance
		// creates an instance of the provided event processor class, then receives events from that partition and passes them to
		// the event processor instance.
		//
		// There are two error notification systems in EventProcessorHost. Notification of errors tied to a particular partition,
		// such as a receiver failing, are delivered to the event processor instance for that partition via the onError method.
		// Notification of errors not tied to a particular partition, such as initialization failures, are delivered to a general
		// notification handler that is specified via an EventProcessorOptions object. You are not required to provide such a
		// notification handler, but if you don't, then you may not know that certain errors have occurred.
		System.out.println("Registering host named " + host.getHostName());
		EventProcessorOptions options = new EventProcessorOptions();
		options.setExceptionNotification(new ErrorNotificationHandler());
		options.setMaxBatchSize(1);
		
		host.registerEventProcessor(Processor.class, options)
		.whenComplete((unused, e) ->
		{
			// whenComplete passes the result of the previous stage through unchanged,
			// which makes it useful for logging a result without side effects.
			if (e != null)
				
			{
				System.out.println("Failure while registering: " + e.toString());
				if (e.getCause() != null)
				{
					System.out.println("Inner exception: " + e.getCause().toString());
				}
			} 
		})
		.thenAcceptAsync((unused) ->
		{
			// This stage will only execute if registerEventProcessor succeeded.
			// If it completed exceptionally, this stage will be skipped.
			
			try {
				Thread.sleep(10000l);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			System.out.println("\n\n\n\n\n ****************************** \n");
			System.out.println("\n **** Press enter to stop. **** \n");
			System.out.println("\n ****************************** \n\n\n\n\n");
			try 
			{
				System.in.read();
			}
			catch (Exception e)
			{
				System.out.println("Keyboard read failed: " + e.toString());
			}
		})
		.thenCompose((unused) ->
		{
			// This stage will only execute if registerEventProcessor succeeded.
			//
            // Processing of events continues until unregisterEventProcessor is called. Unregistering shuts down the
            // receivers on all currently owned leases, shuts down the instances of the event processor class, and
            // releases the leases for other instances of EventProcessorHost to claim.

			return host.unregisterEventProcessor();
		})
		.exceptionally((e) ->
		{
			System.out.println("Failure while unregistering: " + e.toString());
			if (e.getCause() != null)
			{
				System.out.println("Inner exception: " + e.getCause().toString());
			}
			return null;
		})
		.get(); // Wait for everything to finish before exiting main!
		
        System.out.println("End of sample");
	}

}
