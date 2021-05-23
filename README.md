# HevHTProxy

A Socks5 transparent proxy for Android.

**Features**
* Redirect TCP connections.
* Redirect UDP packets. (UDP over TCP see [server](https://gitlab.com/hev/hev-socks5-server))
* IPv4/IPv6. (dual stack)

Tip: This project contains closed-source submodule for traffic encryption.

## How to Build

```bash
git clone --recursive https://gitlab.com/hev/hev-htproxy
cd hev-htproxy
gradle assembleDebug
```

## Authors
* **Heiher** - https://hev.cc

## License
LGPL
