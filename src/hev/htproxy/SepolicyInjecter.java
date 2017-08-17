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

	static {
		System.loadLibrary("sepolicy-inject-jni");
	}

	public static int inject(Context context) {
		File sepolicy_file = new File(context.getFilesDir(), "sepolicy");
		String sepolicy_path = sepolicy_file.getAbsolutePath();
		int ret = 0;

		try {
			sepolicy_file.createNewFile();
		} catch (IOException e) {
			ret = -1;
		}

		if (ret == 0) {
			ret = SuperRunner.runCmd("dd if=" + SELINUX_FS_PATH +
					"policy of=" + sepolicy_path + " bs=1m", false);
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

		if (ret == 0) {
			ret = SuperRunner.runCmd("dd if=" + sepolicy_path + "_inject of=" +
					SELINUX_FS_PATH + "load bs=1m", false);
		}

		Toast.makeText(context, (ret == 0) ? "SELinux policy injected" :
				"Inject SELinux policy error!", Toast.LENGTH_SHORT).show();

		return ret;
	}

	public static void restore(Context context) {
		File sepolicy_file = new File(context.getFilesDir(), "sepolicy");
		String sepolicy_path = sepolicy_file.getAbsolutePath();

		int ret = SuperRunner.runCmd("dd if=" + sepolicy_path +
				" of=" + SELINUX_FS_PATH + "load bs=1m", false);

		Toast.makeText(context, (ret == 0) ? "SELinux policy restored" :
				"Restore SELinux policy error!", Toast.LENGTH_SHORT).show();
	}
}
