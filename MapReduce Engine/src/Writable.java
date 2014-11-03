import java.io.ObjectInput;
import java.io.ObjectOutput;


public interface Writable {
	
	void write(ObjectOutput out);
	void read(ObjectInput in);
}
