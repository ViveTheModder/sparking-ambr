package cmd;
//Value Handler class by ViveTheJoestar

public class ValueHandler {
	public static byte[] getByteArrayFromShort(short data, boolean bigEndian) {
		byte[] valBytes = new byte[2];
		int numBits = 0;
		if (bigEndian) {
			for (int i = 1; i >= 0; i--) {
				valBytes[i] = (byte) (data >> numBits);
				numBits += 8;
			}
		}
		else {
			for (int i = 0; i < 2; i++) {
				valBytes[i] = (byte) (data >> numBits);
				numBits += 8;
			}
		}
		return valBytes;
	}
	public static short getShort(short data, boolean bigEndian) {
		return bigEndian ? data : Short.reverseBytes(data);
	}
	public static short getShortFromByteArray(byte[] data, boolean bigEndian) {
		short output = 0;
		int numBits = 8;
		if (bigEndian) {
			for (int i = 0; i < 2; i++) {
				output |= ((data[i] & 0xFF) << numBits);
				numBits -= 8;
			}
		}
		else {
			for (int i = 1; i >= 0; i--) {
				output |= ((data[i] & 0xFF) << numBits);
				numBits -= 8;
			}
		}
		return output;
	}	
}