#!/usr/bin/env bash
set -euo pipefail
adb install -r app-debug.apk
adb logcat -c
adb shell am start -W -n dev.capcap.mobile/.MainActivity
sleep 8
adb shell pidof dev.capcap.mobile
adb shell uiautomator dump /sdcard/capcap-window.xml
adb pull /sdcard/capcap-window.xml /tmp/capcap-window.xml
grep -F 'Capcut Free' /tmp/capcap-window.xml
# Verify all four navigation tabs are exposed in the accessibility tree.
for tab in 'Dự án' 'AI Studio' 'Xuất' 'Giọng đọc'; do
  grep -F "text=\"$tab\"" /tmp/capcap-window.xml
done
# Exercise activity recreation and verify the app remains responsive.
adb shell am force-stop dev.capcap.mobile
adb shell am start -W -n dev.capcap.mobile/.MainActivity
sleep 5
adb shell pidof dev.capcap.mobile
adb shell uiautomator dump /sdcard/capcap-window.xml
adb pull /sdcard/capcap-window.xml /tmp/capcap-window.xml
grep -F 'Capcut Free' /tmp/capcap-window.xml
adb logcat -d -s AndroidRuntime:E > /tmp/capcap-runtime.log
cat /tmp/capcap-runtime.log
if grep -F 'FATAL EXCEPTION' /tmp/capcap-runtime.log; then exit 1; fi
