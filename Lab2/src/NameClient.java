import java.io.IOException;
import java.util.ArrayList;

public class NameClient {
	public static void main(String[] args) throws IOException {
		String host = args[0];
		int port = Integer.parseInt(args[1]);
		String serviceName = args[2];

		ArrayList<String> serviceList = new ArrayList<String>();
		ArrayList<String> interfaceList = new ArrayList<String>();
		
		RMINaming naming = new RMINaming(host, port);
		NameServer ns = (NameServer) naming.lookup(serviceName);

		int cnt = 3;

		serviceList.add("test");
		serviceList.add("zip");
		serviceList.add("zipr");

		interfaceList.add("Test");
		interfaceList.add("ZipCodeList");
		interfaceList.add("ZipCodeRList");

		System.out.println("Add test...");
		for (int i = 0; i < cnt; i++) {
			RemoteObjectRef ror = new RemoteObjectRef(host, port,
					serviceList.get(i), interfaceList.get(i));
			ns = ns.add(serviceList.get(i), ror, ns);
		}
		System.out.println("Add test done!");
		System.out.println("Match test...");

		NameServer l = ns;
		int i = cnt - 1;
		while (l.next() != null) {
			System.out.println(serviceList.get(i));
			RemoteObjectRef ror = l.match(serviceList.get(i));
			if (ror != null) {
				System.out.println(ror.getHost() + " : " + ror.getPort() + "  "
						+ ror.getInterfaceName());
			} else {
				System.out.println("not found");
			}
		}

		System.out.println("Match test done!");
	}
}