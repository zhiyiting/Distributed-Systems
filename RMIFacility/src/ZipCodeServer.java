public interface ZipCodeServer extends MyRemote
{
    public void initialise(ZipCodeList newlist);
    public String find(String city);
    public ZipCodeList findAll();
    public void printAll();
}