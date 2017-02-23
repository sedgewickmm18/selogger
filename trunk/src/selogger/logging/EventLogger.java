package selogger.logging;

import java.io.File;
import java.io.IOException;

import selogger.Config;
import selogger.logging.io.EventDataStream;
import selogger.logging.io.FileNameGenerator;
import selogger.logging.io.IErrorLogger;

public class EventLogger {

	public static final String FILENAME_TYPEID = "LOG$Types.txt";
	public static final String FILENAME_THREADID = "LOG$Threads.txt";

	static EventLogger INSTANCE = new EventLogger();
	
	private File outputDir;
	private IErrorLogger errorLogger; 
	private EventDataStream stream;
	
	private TypeIdMap typeToId;
	private ObjectIdFile objectIdMap;
	
	private EventLogger() {
		try {
			final Config config = new Config();
			outputDir = config.getOutputDir();
			if (config.getErrorLogFile() != null) {
				errorLogger = new TextErrorLogger(new File(config.getErrorLogFile()));
			} else {
				errorLogger = new TextErrorLogger();
			}
			errorLogger.record(config.getConfigLoadError());
			
			try {
				stream = new EventDataStream(new FileNameGenerator(outputDir), errorLogger);
				typeToId = new TypeIdMap();
				objectIdMap = new ObjectIdFile(config, typeToId);
			} catch (IOException e) {
				errorLogger.record("We cannot record runtime information: " + e.getLocalizedMessage());
				errorLogger.record(e);
			}
	
			Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
				@Override
				public void run() {
					stream.close();
					objectIdMap.close();
					typeToId.save(new File(config.getOutputDir(), FILENAME_TYPEID));
					errorLogger.close();
				}
			}));
		} catch (Throwable e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	public void recordEvent(int dataId, Object value) {
		stream.write(dataId, objectIdMap.getId(value));
	}

	public void recordEvent(int dataId, int value) {
		stream.write(dataId, value);
	}

	public void recordEvent(int dataId, long value) {
		stream.write(dataId, value);
	}

	public void recordEvent(int dataId, byte value) {
		stream.write(dataId, value);
	}

	public void recordEvent(int dataId, short value) {
		stream.write(dataId, value);
	}

	public void recordEvent(int dataId, char value) {
		stream.write(dataId, value);
	}

	public void recordEvent(int dataId, boolean value) {
		stream.write(dataId, value ? 1: 0);
	}

	public void recordEvent(int dataId, double value) {
		stream.write(dataId, Double.doubleToRawLongBits(value));
	}

	public void recordEvent(int dataId, float value) {
		stream.write(dataId, Float.floatToRawIntBits(value));
	}

}