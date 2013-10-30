package com.ermys.echo;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.name.Names;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Properties;

/**
 * @author ANER0310
 * Date: 10/24/13
 */
public class AppModule extends AbstractModule {
	private static final Logger log = LoggerFactory.getLogger(AppModule.class);

	@Override
	protected void configure() {
		Properties props = new Properties();

		try {
			props.load(AppModule.class.getResourceAsStream("/system.properties"));

			Names.bindProperties(binder(), props);
		} catch (IOException e) {
			log.warn("Unable to load properties. Properties injection will not be able", e);
		}
	}

	@Provides
	@ReadBuffer
	protected ByteBuffer provideByteBuffer(){
		return ByteBuffer.allocate(2 * 1024);
	}
}