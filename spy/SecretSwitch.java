package spy;

/**
 * 
 */
public class SecretSwitch implements Secret {
	private final int threshhold = 10000000;
	private final SecretDouble d = new SecretDouble();
	private final SecretSquare s = new SecretSquare();

	@Override
	public int transform(int i) {
		return i > threshhold ? s.transform(i) : d.transform(i);
	}

}
