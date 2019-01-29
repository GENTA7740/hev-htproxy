/* SepolicyInjecter.java
 * Heiher <r@hev.cc>
 */

package hev.htproxy;

import java.io.File;
import java.io.IOException;
import android.os.Build;
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

	private static int runSeInjectCmd(String src, String dst, String scontext, String tcontext) {
		String args[] = new String[13];

		args[0] = "sepolicy-inject";
		args[1] = "-s";
		args[2] = scontext;
		args[3] = "-t";
		args[4] = tcontext;
		args[5] = "-c";
		args[6] = "unix_stream_socket";
		args[7] = "-p";
		args[8] = "connectto";
		args[9] = "-P";
		args[10] = src;
		args[11] = "-o";
		args[12] = dst;

		return SepolicyInject(args);
	}

	private static int runSeInjectCmd(String src, String dst, String scontext) {
		String args[] = new String[7];

		args[0] = "sepolicy-inject";
		args[1] = "-Z";
		args[2] = "untrusted_app";
		args[3] = "-P";
		args[4] = src;
		args[5] = "-o";
		args[6] = dst;

		return SepolicyInject(args);
	}

	private static int makeInject(File sepolicy_file) {
		String sepolicy_path = sepolicy_file.getAbsolutePath();
		String sepolicy_inject_path = sepolicy_path + "_inject";

		try {
			sepolicy_file.createNewFile();
		} catch (IOException e) {
			return -1;
		}

		if (runDdCmd(SELINUX_FS_PATH + "policy", sepolicy_path, "8m") != 0) {
			return -2;
		}

		int uid = android.os.Process.myUid();
		if (SuperRunner.runCmd("chown " + uid + ":" + uid + " " + sepolicy_path) != 0) {
			return -3;
		}

		/* allow netdomain untrusted_app:unix_stream_socket connectto; */
		if (runSeInjectCmd(sepolicy_path, sepolicy_inject_path,
					"netdomain", "untrusted_app") != 0) {
			return -4;
		}

		if (Build.VERSION.SDK_INT >= 26) {
			/* allow netdomain untrusted_app_all:unix_stream_socket connectto; */
			if (runSeInjectCmd(sepolicy_inject_path, sepolicy_inject_path,
						"netdomain", "untrusted_app_all") != 0) {
				return -5;
			}

			/* allow untrusted_app_all untrusted_app_all:unix_stream_socket connectto; */
			if (runSeInjectCmd(sepolicy_inject_path, sepolicy_inject_path,
						"untrusted_app_all", "untrusted_app_all") != 0) {
				return -6;
			}

			/* FIXME: permissive untrusted_app; */
			if (runSeInjectCmd(sepolicy_inject_path, sepolicy_inject_path,
						"untrusted_app") != 0) {
				return -7;
			}
		}

		return 0;
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
