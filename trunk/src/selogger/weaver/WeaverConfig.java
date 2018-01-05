package selogger.weaver;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;

public class WeaverConfig {

	private boolean stackMap = false;
	private boolean weaveExec = true;
	private boolean weaveMethodCall = true;
	private boolean weaveFieldAccess = true;
	private boolean weaveArray = true;
	private boolean weaveLabel = true;
	private boolean weaveMisc = true;
	private boolean weaveParameters = true;
	private boolean weaveLocalAccess = true;
	private boolean ignoreArrayInitializer = false;

	private static final String KEY_STACKMAP = "StackMap";
	public static final String KEY_RECORD_DEFAULT = "";
	public static final String KEY_RECORD_ALL = "ALL";
	private static final String KEY_RECORD = "Events";
	private static final String KEY_RECORD_SEPARATOR = ",";
	private static final String KEY_RECORD_EXEC = "EXEC";
	private static final String KEY_RECORD_CALL = "CALL";
	private static final String KEY_RECORD_FIELD = "FIELD";
	private static final String KEY_RECORD_ARRAY = "ARRAY";
	private static final String KEY_RECORD_MISC = "MISC";
	private static final String KEY_RECORD_LABEL = "LABEL";
	private static final String KEY_RECORD_PARAMETERS = "PARAM";
	private static final String KEY_RECORD_LOCAL = "LOCAL";
	
	/**
	 * Construct a configuration from string
	 * @param options
	 * @return true if at least one weaving option is enabled (except for parameter recording).
	 */
	public WeaverConfig(String options) {
		String opt = options.toUpperCase();
		if (opt.equals(KEY_RECORD_ALL)) {
			opt = KEY_RECORD_EXEC + KEY_RECORD_CALL + KEY_RECORD_FIELD + KEY_RECORD_ARRAY + KEY_RECORD_MISC + KEY_RECORD_PARAMETERS + KEY_RECORD_LABEL + KEY_RECORD_LOCAL;
		} else if (opt.equals(KEY_RECORD_DEFAULT)) {
			opt = KEY_RECORD_EXEC + KEY_RECORD_CALL + KEY_RECORD_FIELD + KEY_RECORD_ARRAY + KEY_RECORD_MISC + KEY_RECORD_PARAMETERS;
		}
		weaveExec = opt.contains(KEY_RECORD_EXEC);
		weaveMethodCall = opt.contains(KEY_RECORD_CALL);
		weaveFieldAccess = opt.contains(KEY_RECORD_FIELD);
		weaveArray = opt.contains(KEY_RECORD_ARRAY);
		weaveMisc = opt.contains(KEY_RECORD_MISC);
		weaveLabel = opt.contains(KEY_RECORD_LABEL);
		weaveParameters = opt.contains(KEY_RECORD_PARAMETERS);
		weaveLocalAccess = opt.contains(KEY_RECORD_LOCAL);
		ignoreArrayInitializer = false;
		stackMap = true; 
	}

	/**
	 * A copy constructor with a constraint.
	 * @param config
	 */
	public WeaverConfig(WeaverConfig parent, LogLevel level) {
		this.weaveExec = parent.weaveExec;
		this.weaveMethodCall = parent.weaveMethodCall;
		this.weaveFieldAccess = parent.weaveFieldAccess;
		this.weaveArray = parent.weaveArray;
		this.weaveMisc = parent.weaveMisc;
		this.weaveLabel = parent.weaveLabel;
		this.weaveParameters = parent.weaveParameters;
		this.weaveLocalAccess = parent.weaveLocalAccess;
		this.stackMap = parent.stackMap;
		this.ignoreArrayInitializer = parent.ignoreArrayInitializer;
		if (level == LogLevel.IgnoreArrayInitializer) {
			this.ignoreArrayInitializer = true;
		} else if (level == LogLevel.OnlyEntryExit) {
			this.weaveMethodCall = false;
			this.weaveFieldAccess = false;
			this.weaveArray = false;
			this.weaveMisc = false;
			this.weaveLabel = false;
			this.weaveParameters = false;
			this.weaveLocalAccess = false;
		}
	}
	
	public boolean isValid() {
		return weaveExec || weaveMethodCall || weaveFieldAccess || weaveArray || weaveMisc || weaveParameters || weaveLocalAccess || weaveLabel;
	}

	/**
	 * Generate a stack map for bytecode verification (JDK 1.7+).
	 * The option is enabled by default. 
	 */
	public void setStackMap(boolean value) {
		this.stackMap = value;
	}
	
	public boolean createStackMap() {
		return stackMap;
	}
	
	public boolean recordExecution() {
		return weaveExec;
	}
	
	public boolean recordFieldAccess() {
		return weaveFieldAccess;
	}
	
	public boolean recordMiscInstructions() {
		return weaveMisc;
	}
	
	public boolean recordMethodCall() {
		return weaveMethodCall;
	}
	
	public boolean recordArrayInstructions() {
		return weaveArray;
	}
	
	public boolean recordLabel() {
		return weaveLabel;
	}
	
	public boolean recordParameters() {
		return weaveParameters;
	}
	
	public boolean recordLocalAccess() {
		return weaveLocalAccess;
	}
	
	public boolean ignoreArrayInitializer() {
		return ignoreArrayInitializer;
	}
	

	/**
	 * Save the weaving configuration to a file.
	 * @param propertyFile
	 */
	public void save(File propertyFile) {
		ArrayList<String> events = new ArrayList<String>();
		if (weaveExec) events.add(KEY_RECORD_EXEC);
		if (weaveMethodCall) events.add(KEY_RECORD_CALL);
		if (weaveFieldAccess) events.add(KEY_RECORD_FIELD);
		if (weaveArray) events.add(KEY_RECORD_ARRAY);
		if (weaveMisc) events.add(KEY_RECORD_MISC);
		if (weaveLabel) events.add(KEY_RECORD_LABEL);
		if (weaveParameters) events.add(KEY_RECORD_PARAMETERS);
		if (weaveLocalAccess) events.add(KEY_RECORD_LOCAL);
		StringBuilder eventsString = new StringBuilder();
		for (int i=0; i<events.size(); ++i) {
			if (i>0) eventsString.append(KEY_RECORD_SEPARATOR);
			eventsString.append(events.get(i));
		}
		
		Properties prop = new Properties();
		prop.setProperty(KEY_RECORD, eventsString.toString());
		prop.setProperty(KEY_STACKMAP, Boolean.toString(stackMap));
		
		try {
			FileOutputStream out = new FileOutputStream(propertyFile);
			prop.storeToXML(out, "Generated: " + new Date().toString(), "UTF-8");
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}