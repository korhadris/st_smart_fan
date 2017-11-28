/**
 *  Smart HVAC Fan
 *
 *  Copyright 2017 Josh M
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
definition(
    name: "Smart HVAC Fan",
    namespace: "korhadris",
    author: "Josh M",
    description: "Turns on fan for circulation when there is a difference between temperature sensors and thermostat.",
    category: "Green Living",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
	section("Temperature diffference to turn on fan") {
    	input "max_delta", "number", required: true, title: "Max temp difference"
        input "hysteresis", "number", required: true, title: "Hysteresis"
	}
    section("Thermostat") {
        input "thermostat", "capability.thermostat", required: true, title: "Thermostat (main temperature and fan control)"
    }
    section("Temperature sensors to use") {
    	// input "temp1", "capability.temperatureMeasurement", required: true, title: "Additional Sensor"
    	input "temps", "capability.temperatureMeasurement", required: false, title: "Additional Sensors", multiple: true
    }
}

def installed() {
	log.debug "Installed with settings: ${settings}"
	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"
	unsubscribe()
	initialize()
}

def initialize() {
  	subscribe(thermostat, "temperature", temperatureChangeHandler)
    temps.each() {
      subscribe(it, "temperature", temperatureChangeHandler)
    }
    checkTemperatures()
}

def temperatureChangeHandler(evt) {
    // log.debug "Temperature change: ${evt}"
    checkTemperatures()
}

def checkTemperatures() {
    def therm = thermostat.currentValue("temperature")
    def max_diff = 0//temp_diff.abs()
    temps.each() {
        def temp_diff = it.currentValue("temperature") - therm
        temp_diff = temp_diff.abs()
        log.debug "Temp diff: ${temp_diff}"
        if (temp_diff > max_diff) {
            max_diff = temp_diff;
            log.debug "Max diff: ${max_diff}"
        }
    }

    if (max_diff > max_delta) {
        log.debug "Turn on fan"
        thermostat.fanOn()
    } else if (max_diff < (max_delta - hysteresis)) {
        log.debug "Turn off fan"
        thermostat.fanAuto()
    } else {
        log.debug "Leave fan alone"
    }
}
