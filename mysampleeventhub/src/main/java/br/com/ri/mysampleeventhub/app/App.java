/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package br.com.ri.mysampleeventhub.app;

import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;

import com.microsoft.azure.eventhubs.EventHubException;

import br.com.ri.mysampleeventhub.processor.Processor;
import br.com.ri.mysampleeventhub.processor.RunProcessor;
import br.com.ri.mysampleeventhub.send.SendMessage;

public class App {

	public static void main(String args[])
			throws InterruptedException, ExecutionException, EventHubException, IOException {

		System.out.println("----------------------------------------------------------");
		System.out.println("Press 1 and Enter to write in EventHub");
		System.out.println("Press 2 and Enter to read from EventHub");
		System.out.println("Press 3 and Enter to exit");
		System.out.println("----------------------------------------------------------");
		

		Scanner userInput = new Scanner(System.in);
		String input = userInput.nextLine();
		System.out.println("Processing input " + input);
		switch (input) {
		case "1":
			SendMessage sendMessage = new SendMessage();
			sendMessage.send();
			break;

		case "2":
			RunProcessor runProcesor = new RunProcessor();
			runProcesor.run();
			
			System.out.println("Message's list size : " + Processor.messages.size());
			Processor p = new Processor();
			p.saveBlob();
			
			break;

		case "3":
			break;

		default:
			break;
		}
		
		
	}
}
