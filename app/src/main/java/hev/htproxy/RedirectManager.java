/* RedirectManager.java
 * Heiher <r@hev.cc>
 */

package hev.htproxy;

import java.util.Set;
import android.content.Context;

public class RedirectManager {
	private static final int TYPE_APPEND = 1;
	private static final int TYPE_CHECK = 2;
	private static final int TYPE_DELETE = 3;

	private static final String cmd_iptables = "iptables -t nat ";
	private static final String netd_dnsproxy_path = "/dev/socket/dnsproxyd";

	public static String[] generateCmds(int type, Context context) {
		String cmd_type, cmd_type_dns;
		switch (type) {
		case TYPE_APPEND:
			cmd_type = " -A ";
			cmd_type_dns = " -I ";
			break;
		case TYPE_CHECK:
			cmd_type = " -C ";
			cmd_type_dns = cmd_type;
			break;
		case TYPE_DELETE:
		default:
			cmd_type = " -D ";
			cmd_type_dns = cmd_type;
			break;
		}

		Preferences prefs = new Preferences(context);
		Set<String> bypass_addresses = prefs.getBypassAddresses();
		int i = 0, cmds_size = 9 + bypass_addresses.size();
		if (prefs.getGlobalProxy()) {
			cmds_size += 2;
		} else {
			Set<String> uids = prefs.getUIDs();
			cmds_size += 2 + uids.size() * 2;
		}

		cmds_size += (type != TYPE_CHECK) ? 2 : 0;
		String[] cmds = new String[cmds_size];
		if (type == TYPE_APPEND) {
			cmds[i++] = cmd_iptables + "-N HTPROXY";
			cmds[i++] = cmd_iptables + "-I OUTPUT -j HTPROXY";
		}
		cmds[i++] = cmd_iptables + cmd_type + "HTPROXY -m owner --uid-owner " +
			Integer.toString(android.os.Process.myUid()) + " -j RETURN";
		cmds[i++] = cmd_iptables + cmd_type + "HTPROXY -d 0.0.0.0/8 -j RETURN";
		cmds[i++] = cmd_iptables + cmd_type + "HTPROXY -d 127.0.0.0/8 -j RETURN";
		cmds[i++] = cmd_iptables + cmd_type + "HTPROXY -d 169.254.0.0/16 -j RETURN";
		cmds[i++] = cmd_iptables + cmd_type + "HTPROXY -d 224.0.0.0/4 -j RETURN";
		cmds[i++] = cmd_iptables + cmd_type + "HTPROXY -d 240.0.0.0/4 -j RETURN";
		for (String addr : bypass_addresses)
		  cmds[i++] = cmd_iptables + cmd_type + "HTPROXY -d " + addr + " -j RETURN";
		if (prefs.getGlobalProxy()) {
			cmds[i++] = cmd_iptables + cmd_type_dns + "HTPROXY -p udp --dport 53 "
				+ " -j REDIRECT --to-port " +
				Integer.toString(prefs.getDNSFwdPort());
			cmds[i++] = cmd_iptables + cmd_type + "HTPROXY -p tcp " +
				" -j REDIRECT --to-port " +
				Integer.toString(prefs.getTProxyPort());
		} else {
			cmds[i++] = cmd_iptables + cmd_type_dns + "HTPROXY -d " +
				prefs.getProxyDns1Address() + "/32 -p udp --dport 53 "
				+ " -j REDIRECT --to-port " +
				Integer.toString(prefs.getDNSFwdPort());
			cmds[i++] = cmd_iptables + cmd_type_dns + "HTPROXY -d " +
				prefs.getProxyDns2Address() + "/32 -p udp --dport 53 "
				+ " -j REDIRECT --to-port " +
				Integer.toString(prefs.getDNSFwdPort());
			Set<String> uids = prefs.getUIDs();
			for (String uid : uids) {
				String owner = "-m owner --uid-owner " + uid;
				cmds[i++] = cmd_iptables + cmd_type + "HTPROXY -p tcp " + owner +
					" -j REDIRECT --to-port " +
					Integer.toString(prefs.getTProxyPort());
				cmds[i++] = cmd_iptables + cmd_type + "HTPROXY -p udp --dport 53 " +
					owner + " -j REDIRECT --to-port " +
					Integer.toString(prefs.getDNSFwdPort());
			}
		}
		cmds[i++] = cmd_iptables + cmd_type + "HTPROXY -d 10.0.0.0/8 -j RETURN";
		cmds[i++] = cmd_iptables + cmd_type + "HTPROXY -d 172.16.0.0/12 -j RETURN";
		cmds[i++] = cmd_iptables + cmd_type + "HTPROXY -d 192.168.0.0/16 -j RETURN";

		if (type == TYPE_DELETE) {
			cmds[i++] = cmd_iptables + cmd_type + "OUTPUT -j HTPROXY";
			cmds[i++] = cmd_iptables + "-X HTPROXY";
		}

		return cmds;
	}

	public static boolean isSupported() {
		if (0 != SuperRunner.runCmd(cmd_iptables + "-A OUTPUT -p tcp -j REDIRECT"))
		  return false;
		if (0 != SuperRunner.runCmd(cmd_iptables + "-D OUTPUT -p tcp -j REDIRECT"))
		  return false;

		return true;
	}

	public static boolean isEnabled(Context context) {
		String[] cmds = generateCmds(TYPE_CHECK, context);
		for (String cmd : cmds) {
			if (0 != SuperRunner.runCmd(cmd))
			  return false;
		}

		/* Ensure permissions */
		SuperRunner.runCmd("chown :inet " + netd_dnsproxy_path);
		SuperRunner.runCmd("chmod 0660 " + netd_dnsproxy_path);

		return true;
	}

	public static boolean setEnabled(boolean enable, Context context) {
		boolean retValue = true;
		int type = enable ? TYPE_APPEND : TYPE_DELETE;
		String[] cmds = generateCmds(type, context);
		for (String cmd : cmds) {
			if (0 != SuperRunner.runCmd(cmd)) {
				retValue = false;
				break;
			}
		}

		/* Enable failed, clear */
		if (enable && !retValue) {
			cmds = generateCmds(TYPE_DELETE, context);
			for (String cmd : cmds)
			  SuperRunner.runCmd(cmd);
		}

		/* DnsProxy */
		if (retValue) {
			if (enable) {
				Preferences prefs = new Preferences(context);

				if (SepolicyInjecter.inject(context) == 0) {
					SuperRunner.runCmd("mv " + netd_dnsproxy_path + " " +
							netd_dnsproxy_path + ".netd");
					SuperRunner.runCmd("ln -sf " + prefs.getDnsProxyPath() + " " +
							netd_dnsproxy_path);
					SuperRunner.runCmd("chown :inet " + netd_dnsproxy_path);
					SuperRunner.runCmd("chmod 0660 " + netd_dnsproxy_path);
				}
			} else {
				SuperRunner.runCmd("mv " + netd_dnsproxy_path + ".netd " +
						netd_dnsproxy_path);

				SepolicyInjecter.restore(context);
			}
		}

		return (enable) ? retValue : true;
	}
}
