package util.core;

/**
 * Reduce task defines the task type
 * 
 * @author zhiyiting
 *
 */
public class ReduceTask extends Task {

	private static final long serialVersionUID = -3199456777121838828L;

	private String output;

	public ReduceTask() {
		setType('R');
	}

	public String getOutput() {
		return output;
	}

	public void setOutput(String output) {
		this.output = output;
	}

}
