package com.ermys.echo;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.apache.log4j.PropertyConfigurator;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.util.concurrent.TimeUnit.*;

public class App 
{
    public static void main( String[] args ) throws Exception
    {
		PropertyConfigurator.configure(App.class.getResourceAsStream("/log.properties"));
		Injector injector = Guice.createInjector(new AppModule());

		ExecutorService es = Executors.newFixedThreadPool(1);

		es.submit(injector.getInstance(Server.class));

		es.awaitTermination(Long.MAX_VALUE, DAYS);

		es.shutdown();
	}
}
