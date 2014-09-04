import java.io.Serializable;

/**
 * 
 */

/**
 * @author zhiyiting
 *
 */
public interface MigratableProcess extends Runnable, Serializable{
	
	/**
	 * Given an object, it can start a new thread.
	 */
	void run();
	
	/**
	 * Called before the object is serialized.
	 * This method affords an opportunity for the process to enter a known safe state.
	 */
	void suspend();
	@Override
	String toString();
}