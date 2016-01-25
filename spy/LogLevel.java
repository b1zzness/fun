package spy;

/**
 *
 */
public enum LogLevel {
	None, Normal, Verbose;
	
	/**
	 * @param level
	 * @return
	 */
	public boolean is(LogLevel level){
		return this == level;
	}
}
