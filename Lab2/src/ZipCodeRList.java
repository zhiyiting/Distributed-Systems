import java.io.Serializable;

public interface ZipCodeRList extends MyRemote, Serializable {
	public String find(String city);

	public ZipCodeRList add(String city, String zipcode);

	public ZipCodeRList next();
}