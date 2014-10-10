/* RedirectManager.java
 * Heiher <r@hev.cc>
 */

package hev.htproxy;

import java.util.Set;
import java.lang.Process;
import java.lang.ProcessBuilder;
import java.lang.InterruptedException;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import android.content.Context;

public class RedirectManager {
	private static final int TYPE_APPEND = 1;
	private static final int TYPE_CHECK = 2;
	private static final int TYPE_DELETE = 3;

	private static final String cmd_iptables = "iptables -t nat ";

	public static int runSuperCmd(String cmd) {
		ProcessBuilder pb = new ProcessBuilder();
		int exitValue = -1;

		try {
			pb.command("su", "-c", cmd);
			Process p = pb.start();
			p.waitFor();
			exitValue = p.exitValue();
			p.destroy();
		} catch (IOException e) {
		} catch (InterruptedException e) {
		}

		return exitValue;
	}

	public static String[] generateCmds(int type, Context context) {
		String cmd_type;
		switch (type) {
		case TYPE_APPEND:
			cmd_type = " -A ";
			break;
		case TYPE_CHECK:
			cmd_type = " -C ";
			break;
		case TYPE_DELETE:
		default:
			cmd_type = " -D ";
			break;
		}

		Preferences prefs = new Preferences(context);
		Set<String> bypass_addresses = prefs.getBypassAddresses();
		int i = 2, cmds_size = 4 + bypass_addresses.size();
		String[] cmds = new String[cmds_size];

		cmds[0] = cmd_iptables + cmd_type + "OUTPUT -d 127.0.0.1/32 -j RETURN";
		cmds[1] = cmd_iptables + cmd_type + "OUTPUT -d " + prefs.getServerAddress() + "/32 -j RETURN";
		for (String addr : bypass_addresses)
		  cmds[i++] = cmd_iptables + cmd_type + "OUTPUT -d " + addr + " -j RETURN";
		cmds[cmds_size-2] = cmd_iptables + cmd_type + "OUTPUT -p tcp -j REDIRECT --to-port " +
			Integer.toString(prefs.getTProxyPort());
		cmds[cmds_size-1] = cmd_iptables + cmd_type + "OUTPUT -p udp --dport 53 -j REDIRECT --to-port " +
			Integer.toString(prefs.getDNSFwdPort());

		return cmds;
	}

	public static boolean isSupported() {
		if (0 != runSuperCmd(cmd_iptables + "-I OUTPUT 1 -p tcp -j REDIRECT"))
		  return false;
		if (0 != runSuperCmd(cmd_iptables + "-D OUTPUT 1"))
		  return false;

		return true;
	}

	public static boolean isEnabled(Context context) {
		String[] cmds = generateCmds(TYPE_CHECK, context);
		for (String cmd : cmds) {
			if (0 != runSuperCmd(cmd))
			  return false;
		}

		return true;
	}

	public static boolean setEnabled(boolean enable, Context context) {
		boolean retValue = true;
		int type = enable ? TYPE_APPEND : TYPE_DELETE;
		String[] cmds = generateCmds(type, context);
		for (String cmd : cmds)
		  retValue = (0 == runSuperCmd(cmd)) ? true : false;

		return (enable) ? retValue : true;
	}
}
