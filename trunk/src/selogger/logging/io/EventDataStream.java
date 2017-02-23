package selogger.logging.io;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.concurrent.atomic.AtomicInteger;

public class EventDataStream {
	
	public static final int MAX_EVENTS_PER_FILE = 10000000;
	public static final int BYTES_PER_EVENT = 16;
	
	private IFileNames files;
	private ObjectOutputStream out;
	private IErrorLogger err;
	private int count;

	private static final AtomicInteger nextThreadId = new AtomicInteger(0);
	private static ThreadLocal<Integer> threadId = new ThreadLocal<Integer>() {
		@Override
		protected Integer initialValue() {
			return nextThreadId.getAndIncrement();
		}
	};
	
	public EventDataStream(IFileNames target, IErrorLogger logger) {
		try {
			files = target;
			err = logger;
			out = new ObjectOutputStream(new FileOutputStream(target.getNextFile()));
			count = 0;
		} catch (IOException e) {
			err.record(e);
		}
	}
	
	public synchronized void write(int dataId, long value) {
		if (out != null) {
			try {
				if (count >= MAX_EVENTS_PER_FILE) {
					out.close();
					out = new ObjectOutputStream(new FileOutputStream(files.getNextFile()));
					count = 0;
				}
				System.err.println(dataId + "," + threadId.get() + "," + value);
				out.writeInt(dataId);
				out.writeInt(threadId.get());
				out.writeLong(value);
				count++;
			} catch (IOException e) {
				out = null;
				err.record(e);
			}
		}
	}
	
	public synchronized void close() {
		try {
			out.close();
			out = null;
		} catch (IOException e) {
			out = null;
			err.record(e);
		}
	}

}