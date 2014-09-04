/**
 * @author zhiyiting
 *
 */
public interface MigratableProcess extends Runnable, java.io.Serializable {

	/**
	 * Given an object, it can start a new thread.
	 */
	void run();

	/**
	 * Called before the object is serialized. This method affords an
	 * opportunity for the process to enter a known safe state.
	 */
	void suspend();

	/**
	 * Print the class name of the process as well as the original set of
	 * arguments with which it was called.
	 * 
	 * @return process name and set of arguments
	 */
	@Override
	String toString();
}