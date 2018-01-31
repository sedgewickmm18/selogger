package selogger.logging;

import java.io.IOException;

import selogger.Config;

public class ObjectIdFile extends ObjectIdMap {

	private StringFileListStream objectIdList;
	private String lineSeparator;
	private TypeIdMap typeToId;
	private SequentialFileName filenames;
	private StringFileListStream exceptionList;
	
	private StringContentFile stringContentList;

	public static final long ID_NOT_FOUND = -1;
	
	public static long cacheHit = 0;
	public static long cacheMiss = 0;

	public ObjectIdFile(Config config, TypeIdMap typeToId) throws IOException {
		super(16 * 1024 * 1024);
		this.typeToId = typeToId;
		lineSeparator = config.getLineSeparator();
		
		filenames = new SequentialFileName(config.getOutputDir(), "LOG$ObjectTypes", ".txt", 2);
		objectIdList = new StringFileListStream(filenames, 10000000, 100 * 1024 * 1024, false);

		exceptionList = new StringFileListStream(new SequentialFileName(config.getOutputDir(), "LOG$Exceptions", ".txt", 2), 1000000, 100 * 1024 * 1024, false);
		
		stringContentList = new StringContentFile(config);
	}
	
	@Override
	protected void onNewObject(Object o) {
		typeToId.getTypeIdString(o.getClass());
	}
	
	@Override
	protected void onNewObjectId(Object o, long id) {
		String typeId = typeToId.getTypeIdString(o.getClass());
		objectIdList.write(Long.toString(id));
		objectIdList.write(",");
		objectIdList.write(typeId);
		objectIdList.write(lineSeparator);
		
		if (o instanceof String) {
			stringContentList.write(id, (String)o);
		} else if (o instanceof Throwable) {
			try {
				Throwable t = (Throwable)o;
				long causeId = getId(t.getCause());
				Throwable[] suppressed = t.getSuppressed();
				long[] suppressedId = new long[suppressed.length];
				for (int i=0; i<suppressedId.length; ++i) {
					suppressedId[i] = getId(suppressed[i]); 
				}
				
				exceptionList.write(Long.toString(id));
				exceptionList.write(",M,");
				exceptionList.write(t.getMessage());
				exceptionList.write("\n");
				exceptionList.write(Long.toString(id));
				exceptionList.write(",CS,");
				exceptionList.write(Long.toString(causeId));
				for (int i=0; i<suppressedId.length; ++i) {
					exceptionList.write(",");
					exceptionList.write(Long.toString(suppressedId[i]));
				}
				exceptionList.write("\n");

				StackTraceElement[] trace = t.getStackTrace();
				for (int i=0; i<trace.length; ++i) {
					exceptionList.write(Long.toString(id));
					exceptionList.write(",S,");
					StackTraceElement e = trace[i];
					exceptionList.write(e.isNativeMethod() ? "T," : "F,");
					exceptionList.write(e.getClassName());
					exceptionList.write(",");
					exceptionList.write(e.getMethodName());
					exceptionList.write(",");
					exceptionList.write(e.getFileName());
					exceptionList.write(",");
					exceptionList.write(Integer.toString(e.getLineNumber()));
					exceptionList.write("\n");
				}
				
			} catch (Throwable e) {
				// ignore all exceptions
			}
		}
	}
	
	public void close() {
		objectIdList.close();
		exceptionList.close();
		stringContentList.close();
	}
	
}
