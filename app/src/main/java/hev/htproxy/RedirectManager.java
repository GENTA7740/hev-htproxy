/* RedirectManager.java
 * Heiher <r@hev.cc>
 */

package hev.htproxy;

import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import android.content.Context;

public class RedirectManager {
	private static final int TYPE_APPEND = 1;
	private static final int TYPE_CHECK = 2;
	private static final int TYPE_DELETE = 3;

	private static final String cmd_iptables = "iptables -t nat ";
	private static final String cmd_ip6tables = "ip6tables -t nat ";
	private static final String netd_dnsproxy_path = "/dev/socket/dnsproxyd";

	private static boolean isIpv6 = false;

	public static String[] generateCmds(int type, Context context) {
		List<String> cmds = new ArrayList<String>();
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

		if (type == TYPE_APPEND) {
			cmds.add(cmd_iptables + "-N HTPROXY");
			cmds.add(cmd_iptables + "-I OUTPUT -j HTPROXY");
			if (isIpv6) {
				cmds.add(cmd_ip6tables + "-N HTPROXY");
				cmds.add(cmd_ip6tables + "-I OUTPUT -j HTPROXY");
			}
		}
		if (isIpv6) {
			cmds.add(cmd_ip6tables + cmd_type + "HTPROXY -m owner --uid-owner " +
					 Integer.toString(android.os.Process.myUid()) + " -j RETURN");
			cmds.add(cmd_ip6tables + cmd_type + "HTPROXY -d ::1/128 -j RETURN");
			cmds.add(cmd_ip6tables + cmd_type + "HTPROXY -d fc00::/7 -j RETURN");
			cmds.add(cmd_ip6tables + cmd_type + "HTPROXY -d fe80::/10 -j RETURN");
		} else {
			cmds.add(cmd_iptables + cmd_type + "HTPROXY -m owner --uid-owner " +
					 Integer.toString(android.os.Process.myUid()) + " -j RETURN");
			cmds.add(cmd_iptables + cmd_type + "HTPROXY -d 0.0.0.0/8 -j RETURN");
			cmds.add(cmd_iptables + cmd_type + "HTPROXY -d 127.0.0.0/8 -j RETURN");
			cmds.add(cmd_iptables + cmd_type + "HTPROXY -d 169.254.0.0/16 -j RETURN");
			cmds.add(cmd_iptables + cmd_type + "HTPROXY -d 224.0.0.0/4 -j RETURN");
			cmds.add(cmd_iptables + cmd_type + "HTPROXY -d 240.0.0.0/4 -j RETURN");
		}
		Preferences prefs = new Preferences(context);
		Set<String> bypass_addresses = prefs.getBypassAddresses();
		for (String addr : bypass_addresses) {
			if (isIpv6 && addr.contains(":"))
			  cmds.add(cmd_ip6tables + cmd_type + "HTPROXY -d " + addr + " -j RETURN");
			else
			  cmds.add(cmd_iptables + cmd_type + "HTPROXY -d " + addr + " -j RETURN");
		}
		if (prefs.getGlobalProxy()) {
			cmds.add(cmd_iptables + cmd_type_dns + "HTPROXY -p udp --dport 53 "
					 + " -j REDIRECT --to-port " +
					 Integer.toString(prefs.getDNSFwdPort()));
			cmds.add(cmd_iptables + cmd_type + "HTPROXY -p tcp " +
					 " -j REDIRECT --to-port " +
					 Integer.toString(prefs.getTProxyPort()));
			if (isIpv6) {
				cmds.add(cmd_ip6tables + cmd_type_dns + "HTPROXY -p udp --dport 53 "
						 + " -j REDIRECT --to-port " +
						 Integer.toString(prefs.getDNSFwdPort()));
				cmds.add(cmd_ip6tables + cmd_type + "HTPROXY -p tcp " +
						 " -j REDIRECT --to-port " +
						 Integer.toString(prefs.getTProxyPort()));
			}
		} else {
			String addr = prefs.getProxyDns1Address();
			if (addr.contains(":") && isIpv6)
			  cmds.add(cmd_ip6tables + cmd_type_dns + "HTPROXY -d " +
					   addr + " -p udp --dport 53 "
					   + " -j REDIRECT --to-port " +
					   Integer.toString(prefs.getDNSFwdPort()));
			else
			  cmds.add(cmd_iptables + cmd_type_dns + "HTPROXY -d " +
					   addr + " -p udp --dport 53 "
					   + " -j REDIRECT --to-port " +
					   Integer.toString(prefs.getDNSFwdPort()));
			addr = prefs.getProxyDns2Address();
			if (addr.contains(":") && isIpv6)
			  cmds.add(cmd_ip6tables + cmd_type_dns + "HTPROXY -d " +
					   addr + " -p udp --dport 53 "
					   + " -j REDIRECT --to-port " +
					   Integer.toString(prefs.getDNSFwdPort()));
			else
			  cmds.add(cmd_iptables + cmd_type_dns + "HTPROXY -d " +
					   addr + " -p udp --dport 53 "
					   + " -j REDIRECT --to-port " +
					   Integer.toString(prefs.getDNSFwdPort()));
			Set<String> uids = prefs.getUIDs();
			for (String uid : uids) {
				String owner = "-m owner --uid-owner " + uid;
				cmds.add(cmd_iptables + cmd_type + "HTPROXY -p tcp " + owner +
						 " -j REDIRECT --to-port " +
						 Integer.toString(prefs.getTProxyPort()));
				cmds.add(cmd_iptables + cmd_type + "HTPROXY -p udp --dport 53 " +
						 owner + " -j REDIRECT --to-port " +
						 Integer.toString(prefs.getDNSFwdPort()));
				if (isIpv6) {
					cmds.add(cmd_ip6tables + cmd_type + "HTPROXY -p tcp " + owner +
							 " -j REDIRECT --to-port " +
							 Integer.toString(prefs.getTProxyPort()));
					cmds.add(cmd_ip6tables + cmd_type + "HTPROXY -p udp --dport 53 " +
							 owner + " -j REDIRECT --to-port " +
							 Integer.toString(prefs.getDNSFwdPort()));
				}
			}
		}
		if (!isIpv6) {
			cmds.add(cmd_iptables + cmd_type + "HTPROXY -d 10.0.0.0/8 -j RETURN");
			cmds.add(cmd_iptables + cmd_type + "HTPROXY -d 172.16.0.0/12 -j RETURN");
			cmds.add(cmd_iptables + cmd_type + "HTPROXY -d 192.168.0.0/16 -j RETURN");
		}
		if (type == TYPE_DELETE) {
			cmds.add(cmd_iptables + cmd_type + "OUTPUT -j HTPROXY");
			cmds.add(cmd_iptables + "-X HTPROXY");
			if (isIpv6) {
				cmds.add(cmd_ip6tables + cmd_type + "OUTPUT -j HTPROXY");
				cmds.add(cmd_ip6tables + "-X HTPROXY");
			}
		}

		String[] cmds_ = new String[cmds.size()];
		cmds.toArray(cmds_);

		return cmds_;
	}

	public static boolean isSupported() {
		if (0 != SuperRunner.runCmd(cmd_iptables + "-A OUTPUT -p tcp -j REDIRECT"))
		  return false;
		if (0 != SuperRunner.runCmd(cmd_iptables + "-D OUTPUT -p tcp -j REDIRECT"))
		  return false;

		if (0 == SuperRunner.runCmd(cmd_ip6tables + "-A OUTPUT -p tcp -j REDIRECT")) {
			isIpv6 = true;
			SuperRunner.runCmd(cmd_ip6tables + "-D OUTPUT -p tcp -j REDIRECT");
		}

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
