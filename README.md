# HevHTProxy

A Socks5 transparent proxy for Android. (require root access)

## How to Build

```bash
git clone git://github.com/heiher/hev-htproxy
cd hev-htproxy
git submodule init
git submodule update
git submodule foreach git submodule init
git submodule foreach git submodule update
nkd-build

android update project -n hev.htproxy -p . -t 1 # android-21+
ant debug
```

## Authors
* **Heiher** - https://hev.cc

## License
LGPL

