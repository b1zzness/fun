package spy;

/**
 * 
 */
public class SecretDouble implements Secret {

	@Override
	public int transform(int i) {
		return 2 * i;
	}

}
