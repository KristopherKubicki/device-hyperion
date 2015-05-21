# Hyperion Controller Device
SmartThings implementation of tvdzwan's outstanding AmbiLight implementation, Hyperion.  Hyperion is a software stack that runs on a computer (usually a Raspberry Pi) behind your TV, adjusting LEDs to match the signal being processed on the computer -- usually a copy of the TV signal.  This gives a cool "behind" the TV effect that used to be reserved for a few high-end Philips TVs only. 

You can <a href='https://github.com/tvdzwan/hyperion'>read more about Hyperion on its GitHub page</a>. 

This device controller treats the Hyperion like a standard color-changing bulb for SmartThings. You have to enable the controller via the hyperion.config.json -- the default port is 19444.  Once you have the controller enabled, just add the IP address and Port to the device in your SmartThings preferences.

# Bugs 
* I have noticed the RGB Selector sometimes causes the device to crash.  This seems to happen if you select a color outside of what it can render.  I only know how to fix this by reinstalling the device.
* The Hyperion controller is a JSON server that does not respond with standard HTTP headers. Thus, SmartThings does not understand the return response from the device, but it can send it commands.  The net result is that if you change the Hyperion via a different app or restart the server, SmartThings will get out of sync with reality.  
