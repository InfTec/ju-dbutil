package ch.inftec.ju.testing;

/**
 * Helper class that takes a large amount of memory. Can be used to perform
 * memory leak testing.
 * @author Martin
 *
 */
public class HugeData {
	private HugeData[] data = new HugeData[1000 * 1000]; // Consumes around 4 MB
	
	@Override
	public String toString() {
		return "HugeData: " + this.data;
	}
}
