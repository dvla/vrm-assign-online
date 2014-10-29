Configuring syslog on OSX
-------------------------

The `syslogd` daemon runs on most Unix distributions by default, including OSX.

The standard aproach for integrating with `syslog` from the JVM is to send log entries over UDP. For this, `syslogd` 
on OSX needs to be reconfigured to allow network connections.

1.  Convert the `syslog` config file into XML. This allows easy editing of the otherwise binary formatted config:

        sudo plutil -convert xml1 /System/Library/LaunchDaemons/com.apple.syslogd.plist
    
2.  Open the config file, as `sudo`, in a text editor of your choice:

        /System/Library/LaunchDaemons/com.apple.syslogd.plist
    
3.  Find the `dict` element that immediately follows `<key>Sockets</key>` and replace it with:
    
        <dict>
            <key>BSDSystemLogger</key>
            <dict>
                <key>SockServiceName</key>
                <string>syslog</string>
                <key>SockType</key>
                <string>dgram</string>
            </dict>
        </dict>
        
4.  Save the file.

5.  Convert the file back to binary:

        sudo plutil -convert binary1 /System/Library/LaunchDaemons/com.apple.syslogd.plist
    
6.  Restart `syslogd`:
    
        sudo launchctl unload /System/Library/LaunchDaemons/com.apple.syslogd.plist
        sudo launchctl load /System/Library/LaunchDaemons/com.apple.syslogd.plist
