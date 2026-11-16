package cmd;
//Sparking! AMBR (AniMationBoneReplacer) by ViveTheJoestar
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import javax.swing.JProgressBar;
import gui.App;

public class Main {
	public static boolean gui = false;
	
	public static String[] writeSrcAnms(JProgressBar[] bar, File[] dirs, int[] anmCnt, int[] boneIds, String[] boneNames, boolean[] bigEndian) throws IOException {
		//First output is errors, second output is warnings
		String[] output = { "", "" };
		if (dirs[0].equals(dirs[1])) {
			output[0] += "Source and destination folders are identical!\n";
			if (!gui) {
				System.out.print(output[0]);
				System.exit(1);
			}
		}
		if (boneIds[1] < boneIds[0]) {
			output[0] += "Ending bone ID does NOT exceed the starting bone ID!\n";
			if (!gui) {
				System.out.print(output[0]);
				System.exit(2);
			}
		}
		int changedAnms = 0;
		boolean dirsAreFiles = dirs[0].isFile() && dirs[1].isFile();
		File[] srcFiles = dirsAreFiles ? new File[1] : dirs[0].listFiles((dir, name) -> (name.toLowerCase().endsWith(".anm")));
		File[] dstFiles = dirsAreFiles ? new File[1] : dirs[1].listFiles((dir, name) -> (name.toLowerCase().endsWith(".anm")));
		if (dirsAreFiles) {
			srcFiles[0] = dirs[0];
			dstFiles[0] = dirs[1];
		}
		if (srcFiles.length == 0 || dstFiles.length == 0) {
			output[0] += "No source and/or destination ANM files were found!\n";
			if (!gui) {
				System.out.print(output[0]);
				System.exit(3);
			}
		}
		else if (srcFiles.length != dstFiles.length) {
			output[0] += "The number of source & destination ANM files must match!\n";
			if (!gui) {
				System.out.print(output[0]);
				System.exit(4);
			}
		}
		if (gui && !output[0].equals("")) return output;
		Animation[] srcAnms = new Animation[srcFiles.length];
		Animation[] dstAnms = new Animation[dstFiles.length];
		if (!gui) {
			System.out.print("\nAnimation Transfer (Affected Bones: ");
			for (int i = boneIds[0]; i <= boneIds[1]; i++)
				System.out.print(boneNames[i] + " ");
			System.out.println(")");
		}
		int anmTotal = srcAnms.length;
		if (gui && bar != null) bar[0].setMaximum(anmTotal);
		for (int i = 0; i < srcAnms.length; i++) {
			srcAnms[i] = new Animation(srcFiles[i], bigEndian[0]);
			dstAnms[i] = new Animation(dstFiles[i], bigEndian[0]);
			if (srcAnms[i].isValidAnimation() && dstAnms[i].isValidAnimation()) {
				changedAnms++;
				if (!gui)
					System.out.println(srcAnms[i].getFileName() + " (src. ANM " + i + ") -> " + dstAnms[i].getFileName() + " (dest. ANM " + i + ")");
				else if (bar != null) bar[0].setValue(changedAnms);
				dstAnms[i].replaceBoneContents(srcAnms[i], boneIds[0], boneIds[1], boneNames);
				output[1] += dstAnms[i].getAnmError();
			}
			else {
				if (gui && bar != null) {
					anmTotal--;
					bar[0].setMaximum(anmTotal);
				}
			}
		}
		if (!gui) {
			String success = " ANM files have been changed in ";
			if (changedAnms == 1) success = success.replace("s have", " has");
			System.out.print("SUCCESS: " + changedAnms + success);
		}
		else anmCnt[0] = changedAnms;
		return output;
	}
	public static String getExceptionText(Exception e) {
		String exMsg = e.getClass().getSimpleName() + ": " + e.getMessage() + "\n";
		StackTraceElement[] stackTraceArr = e.getStackTrace();
		for (StackTraceElement ste: stackTraceArr) {
			String line = ste.toString().replace("[", "").replace("]", "");
			if (line.startsWith("gui") || line.startsWith("cmd")) exMsg += line + "\n";
		}
		return exMsg;
	}
	public static String[] getBoneNames(File csv, boolean numbered) throws IOException {
		int endOfHeader = 0, rowCnt = 0;
		RandomAccessFile raf = new RandomAccessFile(csv, "r");
		byte[] csvBytes = new byte[(int) raf.length()];
		raf.readFully(csvBytes);
		for (int pos = 0; pos < csvBytes.length; pos++) {
			if (csvBytes[pos] == 0x0A) {
				if (rowCnt == 0) endOfHeader = pos;
				rowCnt++;
			}
		}
		String[] rows = new String[rowCnt - 1];
		rowCnt = 0;
		int next = endOfHeader + 1, prev = endOfHeader + 1;
		for (int pos = endOfHeader + 1; pos < csvBytes.length; pos++) {
			if (csvBytes[pos] == 0x0A) {
				prev = next;
				next = pos;
				byte[] rowBytes = new byte[next - prev];
				next++;
				System.arraycopy(csvBytes, prev, rowBytes, 0, rowBytes.length);
				String tmpStr = new String(rowBytes);
				int commaIdx = tmpStr.indexOf(",");
				if (commaIdx > 0)
					rows[rowCnt] = numbered ? "(" + tmpStr.replace(",", ") ") : tmpStr.substring(commaIdx + 1);
				else rows[rowCnt] = new String(rowBytes);
				rowCnt++;
			}
		}
		raf.close();
		return rows;
	}
	public static void main(String[] args) {
		try {
			if (args.length >= 5) {
				boolean[] bigEndian = new boolean[1];
				if (args[4].equals("w")) bigEndian[0] = true;
				else if (args[4].equals("p"));
				else {
					System.out.println("ERROR: Invalid endian argument!");
					return;
				}
				File csv = new File("./csv/bone-ids.csv");
				if (csv.isFile()) {
					String[] boneNames = getBoneNames(csv, false);
					File[] dirs = new File[2];
					for (int i = 0; i < 2; i++)
						dirs[i] = new File(args[i]);
					if (dirs[0].exists() && dirs[1].exists()) {
						int[] anmCnt = new int[1], boneIds = new int[2];
						if (args[2].matches("\\d+") && args[3].matches("\\d+")) {
							for (int i = 0; i < 2; i++) {
								boneIds[i] = Integer.parseUnsignedInt(args[i + 2]);
								if (boneIds[i] > 67) boneIds[i] = 67;
							}
							long start = System.currentTimeMillis();
							writeSrcAnms(null, dirs, anmCnt, boneIds, boneNames, bigEndian);
							long end = System.currentTimeMillis();
							System.out.println((end - start) / 1000.0 + " s.");
						}
						else System.out.println("ERROR: Invalid bone IDs!");
					}
					else System.out.println("ERROR: Source and/or destination paths do NOT exist!");
				}
				else System.out.println("ERROR: bone-ids.csv is NOT present!");
			}
			else if (args.length > 0) {
				System.out.println("USAGE: java -jar \"path/to/src/anm/or/dir\" \"path/to/dst/anm/or/dir\" [bone-id-start] [bone-id-end] [endian]"); 
				System.out.println("Bone IDs range from 0 to 67. As for the endian argument, it must be set to either w or p.");
			}
			else {
				gui = true;
				App.launch();
			}
		}
		catch (IOException e) {
			System.out.println(getExceptionText(e));
		}
	}
}