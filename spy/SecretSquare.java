package spy;

/**
 * 
 */
public class SecretSquare implements Secret {

	@Override
	public int transform(int i) {
		return i * i;
	}

}
