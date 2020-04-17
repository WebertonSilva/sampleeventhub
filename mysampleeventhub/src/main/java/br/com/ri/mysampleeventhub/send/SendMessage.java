package br.com.ri.mysampleeventhub.send;

import java.io.IOException;
import java.nio.charset.Charset;
import java.time.Instant;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.microsoft.azure.eventhubs.ConnectionStringBuilder;
import com.microsoft.azure.eventhubs.EventData;
import com.microsoft.azure.eventhubs.EventHubClient;
import com.microsoft.azure.eventhubs.EventHubException;

import br.com.ri.mysampleeventhub.utils.EventHubParameters;

public class SendMessage {
	
	public EventHubParameters params = new EventHubParameters();
	
	public void send() throws EventHubException, ExecutionException, InterruptedException, IOException {
		
		
        final ConnectionStringBuilder connStr = new ConnectionStringBuilder()
                .setNamespaceName(params.getNamespaceName())
                .setEventHubName(params.getEventHubName())
                .setSasKeyName(params.getSasKeyName())
                .setSasKey(params.getSasKey());


        final Gson gson = new GsonBuilder().create();

        // The Executor handles all asynchronous tasks and this is passed to the EventHubClient instance.
        // This enables the user to segregate their thread pool based on the work load.
        // This pool can then be shared across multiple EventHubClient instances.
        // The following sample uses a single thread executor, as there is only one EventHubClient instance,
        // handling different flavors of ingestion to Event Hubs here.
        
        
        //O executor manipula todas as tarefas ass�ncronas e isso � passado para a instancia do EventHubClient
        //Isso habilita o usuario a segregar seu pool de thread baseado na carga de trabalho
        //Esse pool can ent�o ser compartilhado atrav�s de multiplas instancias de EventHubClient
        //O seguinte exemplo usa um �nico thread executor, como existe apenas uma instancia de EventHubClient
        //manipulando diferentes tipos de insgest�o para o Hub de Eventos
           
        
        final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(4);

        // Each EventHubClient instance spins up a new TCP/SSL connection, which is expensive.
        // It is always a best practice to reuse these instances. The following sample shows this.
        
        // Cada intancia EventHubClient gera uma nova conex�o TCP / SSL, que � cara.
        // � sempre uma boa pratica reusar essas instancias. O seguinte exemplo mostra isso.
        final EventHubClient ehClient = EventHubClient.createFromConnectionStringSync(connStr.toString(), executorService);

        try {
            for (int i = 0; i < 10; i++) {

                String payload = "Message de Teste " + Integer.toString(i);
                //PayloadEvent payload = new PayloadEvent(i);
                byte[] payloadBytes = gson.toJson(payload).getBytes(Charset.defaultCharset());
                EventData sendEvent = EventData.create(payloadBytes);

                // Send - not tied to any partition
                // Event Hubs service will round-robin the events across all Event Hubs partitions.
                // This is the recommended & most reliable way to send to Event Hubs.
                
                // Enviar - n�o est� vinculado a nenhuma parti��o
                // O servi�o Hubs de Eventos far� o round-robin dos eventos em todas as parti��es dos Hubs de Eventos.
                // Essa � a maneira recomendada e mais confi�vel de enviar para os Hubs de Eventos.
                System.out.println(Instant.now() + ": Sending message " + i);
                ehClient.sendSync(sendEvent);
            }

            System.out.println(Instant.now() + ": Send Complete...");
            System.out.println("Press Enter to stop.");
            System.in.read();
        } finally {
            ehClient.closeSync();
            executorService.shutdown();
        }
	}

}
