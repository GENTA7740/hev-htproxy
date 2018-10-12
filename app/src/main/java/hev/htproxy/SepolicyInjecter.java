/* SepolicyInjecter.java
 * Heiher <r@hev.cc>
 */

package hev.htproxy;

import java.io.File;
import java.io.IOException;
import android.content.Context;
import android.widget.Toast;

public class SepolicyInjecter {
	private static native int SepolicyInject(String[] args);

	private static final String SELINUX_FS_PATH = "/sys/fs/selinux/";
	private static boolean isInject = false;

	static {
		System.loadLibrary("sepolicy-inject-jni");
	}

	private static int runDdCmd(String src, String dst, String bs) {
		return SuperRunner.runCmd("dd if=" + src + " of=" + dst + " bs=" + bs, false);
	}

	private static int makeInject(File sepolicy_file) {
		int ret = 0;
		String sepolicy_path = sepolicy_file.getAbsolutePath();

		try {
			sepolicy_file.createNewFile();
		} catch (IOException e) {
			ret = -1;
		}

		if (ret == 0) {
			ret = runDdCmd(SELINUX_FS_PATH + "policy", sepolicy_path, "8m");
		}

		if (ret == 0) {
			int uid = android.os.Process.myUid();
			ret = SuperRunner.runCmd("chown " + uid + ":" + uid + " " + sepolicy_path);
		}

		if (ret == 0) {
			String args[] = new String[13];

			args[0] = "sepolicy-inject";
			args[1] = "-s";
			args[2] = "netdomain";
			args[3] = "-t";
			args[4] = "untrusted_app";
			args[5] = "-c";
			args[6] = "unix_stream_socket";
			args[7] = "-p";
			args[8] = "connectto";
			args[9] = "-P";
			args[10] = sepolicy_path;
			args[11] = "-o";
			args[12] = sepolicy_path + "_inject";

			ret = SepolicyInject(args);
		}

		return ret;
	}

	public static int inject(Context context) {
		File sepolicy_file = new File(context.getFilesDir(), "sepolicy");
		String sepolicy_path = sepolicy_file.getAbsolutePath();
		int ret = 0;

		if (!isInject) {
			ret = makeInject(sepolicy_file);
			isInject = true;
		}

		if (ret == 0) {
			ret = runDdCmd(sepolicy_path + "_inject", SELINUX_FS_PATH + "load", "8m");
		}

		Toast.makeText(context, (ret == 0) ? "SELinux policy injected" :
				"Inject SELinux policy error!", Toast.LENGTH_SHORT).show();

		return ret;
	}

	public static void restore(Context context) {
		File sepolicy_file = new File(context.getFilesDir(), "sepolicy");
		String sepolicy_path = sepolicy_file.getAbsolutePath();

		int ret = runDdCmd(sepolicy_path, SELINUX_FS_PATH + "load", "8m");

		Toast.makeText(context, (ret == 0) ? "SELinux policy restored" :
				"Restore SELinux policy error!", Toast.LENGTH_SHORT).show();
	}
}
