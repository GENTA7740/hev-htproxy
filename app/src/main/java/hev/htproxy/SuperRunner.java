/* SuperRunner.java
 * Heiher <r@hev.cc>
 */

package hev.htproxy;

import java.lang.Process;
import java.lang.ProcessBuilder;
import java.lang.InterruptedException;
import java.io.BufferedInputStream;
import java.io.IOException;

public class SuperRunner {
	public static int runCmd(String cmd, boolean careError) {
		ProcessBuilder pb = new ProcessBuilder();
		int exitValue = -1;

		try {
			pb.command("su", "-c", cmd);
			Process p = pb.start();
			p.waitFor();
			BufferedInputStream in = new BufferedInputStream(p.getErrorStream());
			exitValue = p.exitValue();
			if (careError && (0 < in.available()))
			  exitValue |= Integer.MIN_VALUE;
			p.destroy();
		} catch (IOException e) {
		} catch (InterruptedException e) {
		}

		return exitValue;
	}

	public static int runCmd(String cmd) {
		return runCmd(cmd, true);
	}
}
