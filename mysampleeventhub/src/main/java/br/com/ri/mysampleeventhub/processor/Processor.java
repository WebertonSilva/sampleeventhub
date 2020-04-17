package br.com.ri.mysampleeventhub.processor;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.microsoft.azure.eventhubs.EventData;
import com.microsoft.azure.eventprocessorhost.CloseReason;
import com.microsoft.azure.eventprocessorhost.IEventProcessor;
import com.microsoft.azure.eventprocessorhost.PartitionContext;

import br.com.ri.mysampleeventhub.utils.EventHubParameters;


public class Processor implements IEventProcessor{
	
	
	private int checkpointBatchingCount = 0;
	public static List<String> messages = new ArrayList<>();
	public EventHubParameters params = new EventHubParameters();

	// OnOpen is called when a new event processor instance is created by the host. In a real implementation, this
	// is the place to do initialization so that events can be processed when they arrive, such as opening a database
	// connection.
	@Override
    public void onOpen(PartitionContext context) throws Exception
    {
    	System.out.println("SAMPLE: Partition " + context.getPartitionId() + " is opening");
    }

    // OnClose is called when an event processor instance is being shut down. The reason argument indicates whether the shut down
    // is because another host has stolen the lease for this partition or due to error or host shutdown. In a real implementation,
    // this is the place to do cleanup for resources that were opened in onOpen.
	@Override
    public void onClose(PartitionContext context, CloseReason reason) throws Exception
    {
        System.out.println("SAMPLE: Partition " + context.getPartitionId() + " is closing for reason " + reason.toString());
    }
	
	// onError is called when an error occurs in EventProcessorHost code that is tied to this partition, such as a receiver failure.
	// It is NOT called for exceptions thrown out of onOpen/onClose/onEvents. EventProcessorHost is responsible for recovering from
	// the error, if possible, or shutting the event processor down if not, in which case there will be a call to onClose. The
	// notification provided to onError is primarily informational.
	@Override
	public void onError(PartitionContext context, Throwable error)
	{
		System.out.println("SAMPLE: Partition " + context.getPartitionId() + " onError: " + error.toString());
	}

	// onEvents is called when events are received on this partition of the Event Hub. The maximum number of events in a batch
	// can be controlled via EventProcessorOptions. Also, if the "invoke processor after receive timeout" option is set to true,
	// this method will be called with null when a receive timeout occurs.
	@Override
    public void onEvents(PartitionContext context, Iterable<EventData> events) throws Exception
    {
        System.out.println("SAMPLE: Partition " + context.getPartitionId() + " got event batch");
        int eventCount = 0;
        for (EventData data : events)
        {
        	// It is important to have a try-catch around the processing of each event. Throwing out of onEvents deprives
        	// you of the chance to process any remaining events in the batch. 
        	try
        	{
        		System.out.println("Processing  " +  new String(data.getBytes(), "UTF8"));
        		
                System.out.println("SAMPLE (" + context.getPartitionId() + "," + data.getSystemProperties().getOffset() + "," +
                		data.getSystemProperties().getSequenceNumber() + "): " + new String(data.getBytes(), "UTF8"));
                eventCount++;
                
                // Checkpointing persists the current position in the event stream for this partition and means that the next
                // time any host opens an event processor on this event hub+consumer group+partition combination, it will start
                // receiving at the event after this one. Checkpointing is usually not a fast operation, so there is a tradeoff
                // between checkpointing frequently (to minimize the number of events that will be reprocessed after a crash, or
                // if the partition lease is stolen) and checkpointing infrequently (to reduce the impact on event processing
                // performance). Checkpointing every five events is an arbitrary choice for this sample.
                this.checkpointBatchingCount++;
                if ((checkpointBatchingCount % 5) == 0)
                {
                	System.out.println("SAMPLE: Partition " + context.getPartitionId() + " checkpointing at " +
               			data.getSystemProperties().getOffset() + "," + data.getSystemProperties().getSequenceNumber());
                	// Checkpoints are created asynchronously. It is important to wait for the result of checkpointing
                	// before exiting onEvents or before creating the next checkpoint, to detect errors and to ensure proper ordering.
                	context.checkpoint(data).get();
                }
                
                System.out.println("Adicionando mensagem a lista de mensagens...");
                this.messages.add( new String(data.getBytes(), "UTF8"));
                System.out.println("Tamanho lista : " + messages.size());
                
        	}
        	catch (Exception e)
        	{
        		System.out.println("Processing failed for an event: " + e.toString());
        	}
        	
        }
        System.out.println("SAMPLE: Partition " + context.getPartitionId() + " batch size was " + eventCount + " for host " + context.getOwner());
    }

	public void saveBlob() {
		// Create a BlobServiceClient object which will be used to create a container client
		BlobServiceClient blobServiceClient = new BlobServiceClientBuilder().connectionString(params.getStorageConnectionString()).buildClient();

		//Create a unique name for the container
		String containerName = "quickstartblobs" + java.util.UUID.randomUUID();

		// Create the container and return a container client object
		BlobContainerClient containerClient = blobServiceClient.createBlobContainer(containerName);
				
		// Create a local file in the ./data/ directory for uploading and downloading
		String localPath = params.getDirTmpBlobFile();
		String fileName = "quickstart" + java.util.UUID.randomUUID() + ".log";
		File localFile = new File(localPath + fileName);

		System.out.println("Recuperando lista de mensagens para salvar no Blob");
		System.out.println("Tamanho da lista : " + this.messages.size());
		try {
			// Write text to the file
			FileWriter writer = new FileWriter(localPath + fileName, true);
			this.messages.forEach(m -> {
				try {
					writer.write(m  + "\n");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			});			
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Get a reference to a blob
		BlobClient blobClient = containerClient.getBlobClient(fileName);

		System.out.println("\nUploading to Blob storage as blob:\n\t" + blobClient.getBlobUrl());

		// Upload the blob
		blobClient.uploadFromFile(localPath + fileName);
		
	}
}
