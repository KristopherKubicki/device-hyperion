/**
 *  Hyperion Controller
 *    SmartThings device to make your Hyperion LEDs act like a normal color changing bulb
 *
 * https://github.com/tvdzwan/hyperion
 *
 */

preferences {
	input("destIp", "text", title: "IP", description: "The device IP")
	input("destPort", "number", title: "Port", description: "The port you wish to connect (default: 19444)")
}
 

metadata {
	definition (name: "Hyperion Controller", namespace: "KristopherKubicki", 
    	author: "kristopher@acm.org") {
        capability "Actuator"
		capability "Switch" 
		capability "Switch Level"
		capability "Color Control"
		capability "Configuration"
		capability "Polling"
		capability "Refresh"

      	}

	tiles {
		standardTile("switch", "device.switch", width: 1, height: 1, canChangeIcon: true) {
			state "off", label: '${name}', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
			state "on", label: '${name}', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#79b821"
		}
		standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat") {
			state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
		}
		controlTile("rgbSelector", "device.color", "color",height: 3, width: 3, inactiveLabel: false) {
			state "color", action:"setColor"
		}
		controlTile("levelSliderControl", "device.level", "slider", height: 1, width: 2, inactiveLabel: false) {
			state "level", action:"switch level.setLevel"
		}


		main(["switch"])
		details(["switch", "levelSliderControl", "rgbSelector", "refresh"])
	}
}



def parse(String description) {
	log.debug "Parsing '${description}'"
    
 	def map = stringToMap(description)
    if(!map.body) { return }
	def localBody = new String(map.body.decodeBase64())

	log.debug "BODY: $localBody"
}

def setColor(value){
	log.trace "setColor($value)"
    
    
	def max = 0xfe
	value.hue = value.hue as Integer
    value.saturation = value.saturation as Integer	

	sendEvent(name: "hue", value: value.hue)
	sendEvent(name: "saturation", value: value.saturation)

	return hsvToHex(value.hue / 100,value.saturation / 100)
}

def setLevel(value) {

	if (value == 0) {
		sendEvent(name: "switch", value: "off")
	}
    if(value > 0 && device.currentValue("switch") == "off") { 
    	sendEvent(name: "switch", value: "on")
    }

	sendEvent(name: "level", value: value)
    value = value / 100
	request("{\"command\":\"transform\",\"transform\":{\"valueGain\": $value }}\r\n")
}


def on() {

	sendEvent(name: "switch", value: "on")
	request("{\"command\" : \"clear\", \"priority\" : 100 }\r\n")
}

def off() { 

	sendEvent(name: "switch", value: "off")
	request("{\"command\" : \"color\", \"priority\" : 100, \"color\" : [0,0,0]}\r\n")
}


def poll() { 
	status()
}

def status() {
    request('')
}

def request(body) { 

    def hosthex = convertIPtoHex(destIp)
    def porthex = convertPortToHex(destPort)
    device.deviceNetworkId = "$hosthex:$porthex" 

    
    log.debug "REQUEST : $body"
    
def hubAction = new physicalgraph.device.HubAction(body, physicalgraph.device.Protocol.LAN)

        
    hubAction
}


private String convertIPtoHex(ipAddress) { 
    String hex = ipAddress.tokenize( '.' ).collect {  String.format( '%02X', it.toInteger() ) }.join()
    return hex
}

private String convertPortToHex(port) {
	String hexport = port.toString().format( '%04X', port.toInteger() )
    return hexport
}

// Derived from java.awt.Color
def hsvToHex(float hue, float saturation) {

    int h = (int)(hue * 6);
    float f = hue * 6 - h;
    float p = (1 - saturation);
    float q = (1 - f * saturation);
    float t = (1 - (1 - f) * saturation);

    switch (h) {
      case 0: return rgbToString(1, t, p);
      case 1: return rgbToString(q, 1, p);
      case 2: return rgbToString(p, 1, t);
      case 3: return rgbToString(p, q, 1);
      case 4: return rgbToString(t, p, 1);
      case 5: return rgbToString(1, p, q);
    }
}

def rgbToString(float r, float g, float b) {
    def rs = (int)(r * 255)
    def gs = (int)(g * 255)
    def bs = (int)(b * 255)
   
   	sendEvent(name: "color", value: "#" + Integer.toHexString(rs).toUpperCase() + Integer.toHexString(gs).toUpperCase() + Integer.toHexString(bs).toUpperCase())
   	request("{\"command\" : \"color\", \"priority\" : 100, \"color\" : [$rs,$gs,$bs]}\r\n")
}
