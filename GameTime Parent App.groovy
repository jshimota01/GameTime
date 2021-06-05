/**
 *  GameTime
 *
 *  Copyright 2021 Justin Leonard
 *
 *  Licensed Virtual the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 *  Change History:
 *  v1.2.0 - Full Feature Beta
 *  v1.2.1 - Bug fixes
 *  v1.2.2 - Update scheduling if late night game; Time Formatting improvements
 */
import java.text.SimpleDateFormat
import groovy.transform.Field

definition(
    name: "GameTime",
    namespace: "lnjustin",
    author: "Justin Leonard",
    description: "GameTime Tracker for College and Professional Sports",
    category: "My Apps",
    oauth: [displayName: "GameTime", displayLink: ""],
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png"
)

@Field String checkMark = "https://raw.githubusercontent.com/lnjustin/App-Images/master/checkMark.svg"
@Field String xMark = "https://raw.githubusercontent.com/lnjustin/App-Images/master/xMark.svg"

preferences {
    page name: "mainPage", title: "", install: true, uninstall: true
}

def mainPage() {
    dynamicPage(name: "mainPage") {
       
            section (getInterface("header", " GameTime")) {
    	        installCheck()
		        if(state.appInstalled == 'COMPLETE'){
			        section(getInterface("header", " College Sports")) {
				        app(name: "anyOpenApp", appName: "GameTime College Instance", namespace: "lnjustin", title: "<b>Add a new GameTime instance for college sports</b>", multiple: true)
			        }
                    section(getInterface("header", " Professional Sports")) {
				        app(name: "anyOpenApp", appName: "GameTime Professional Instance", namespace: "lnjustin", title: "<b>Add a new GameTime instance for professional sports</b>", multiple: true)
			        } 
                    section("") { 
                        paragraph getInterface("note", txt="After installing or updating your team(s) above, be sure to click the DONE button below.")
                    }
                }
            }
            section (getInterface("header", " Tile Settings")) {
                input("showTeamName", "bool", title: "Show Team Name on Tile?", defaultValue: false, displayDuringSetup: false, required: false)
                input("showTeamRecord", "bool", title: "Show Team Record on Tile?", defaultValue: false, displayDuringSetup: false, required: false)
                input("showChannel", "bool", title: "Show TV Channel on Tile?", defaultValue: false, displayDuringSetup: false, required: false)
                input(name:"fontSize", type: "number", title: "Font Size (%)", required:true, submitOnChange:true, defaultValue:100)
                input("textColor", "text", title: "Text Color (Hex)", defaultValue: '#000000', displayDuringSetup: false, required: false)
                input name: "clearWhenInactive", type: "bool", title: "Clear Tile When Inactive?", defaultValue: false
                input name: "hoursInactive", type: "number", title: "Inactivity Threshold (In Hours)", defaultValue: 24
            }
			section (getInterface("header", " General Settings")) {
                input("debugOutput", "bool", title: "Enable debug logging?", defaultValue: true, displayDuringSetup: false, required: false)
		    }
            section("") {
                
                footer()
            }
    }
}

def getTextColor() {
    return (textColor) ? textColor : "#000000"
}

def getFontSize() {
    return fontSize != null ? fontSize : 100
}

def getInactivityThreshold() {
    return hoursInactive != null ? hoursInactive : 24
}

def getClearWhenInactive() {    
    return clearWhenInactive != null ? clearWhenInactive : false
}

def footer() {
    paragraph getInterface("line", "") + '<div style="display: block;margin-left: auto;margin-right: auto;text-align:center">&copy; 2020 Justin Leonard.<br>'
}
      
    
def installed() {
	initialize()
}

def updated() {
    unschedule()
	unsubscribe()
    state.clear()
	initialize()
}

def uninstalled() {
    deleteParentDevice()
	logDebug "Uninstalled app"
}

def initialize() {
    createParentDevice()
    childApps.each { child ->
        child.updated()                
    }
}

def updateLastGameResult(appID) {
    childApps.each { child ->
        if (child.id == appID) {
            child.updateRecord(true)                
        }
    }
}

def fullUpdate(appID) {
    childApps.each { child ->
        if (child.id == appID) {
            child.update()                
        }
    }
}

def installCheck(){
	state.appInstalled = app.getInstallationState() 
	if(state.appInstalled != 'COMPLETE'){
		section{paragraph "Please hit 'Done' to install '${app.label}' parent app "}
  	}
  	else{
    	log.info "Parent Installed OK"
  	}
}

def createParentDevice()
{
    def parent = getChildDevice("GameTimeParentDevice${app.id}")
    if (!parent) {
        String parentNetworkID = "GameTimeParentDevice${app.id}"
        parent = addChildDevice("lnjustin", "GameTime", parentNetworkID, [label:"GameTime", isComponent:true, name:"GameTime"])
        if (parent) {
            parent.updateSetting("parentID", app.id)
            logDebug("Created GameTime Parent Device")
        }
        else log.error "Error Creating GameTime Parent Device"
    }
}

def deleteParentDevice()
{
    deleteChildDevice("GameTimeParentDevice${app.id}")
}

def deleteChildDevice(appID) {
    def parent = getChildDevice("GameTimeParentDevice${app.id}")
    if (parent) {
        parent.deleteChild(appID)
    }
    else log.error "No Parent Device Found."
}

def createChildDevice(appID, name) {
    def parent = getChildDevice("GameTimeParentDevice${app.id}")
    if (parent) {
        parent.createChild(appID, name)
    }
    else log.error "No Parent Device Found."
}

def updateChildDevice(appID, data) {
    def parent = getChildDevice("GameTimeParentDevice${app.id}")
    if (parent) {
        parent.updateChildDevice(appID, data)
    }
    else log.error "No Parent Device Found."
}

def logDebug(msg) {
    if (settings?.debugOutput) {
		log.debug msg
	}
}

def countAPICall(league) {
    if (state.apiCallsThisMonth == null) state.apiCallsThisMonth = [:]
    if (state.apiCallsThisMonth[league] != null)  state.apiCallsThisMonth[league]++
    else if (state.apiCallsThisMonth[league] == null) state.apiCallsThisMonth[league] = 1
    if (state.apiCallsThisMonth[league] > 1000) log.warn "API Call Limit of 1000 per month exceeded for ${league}. Uncheck 'Clear Teams Data Between Updates' in the app to reduce the number of API calls."
}
    
def updateAPICallInfo(league) {
    Calendar cal = Calendar.getInstance()
    cal.setTimeZone(location.timeZone)
    cal.setTime(new Date())
    def dayOfMonth = cal.get(Calendar.DAY_OF_MONTH)
    def isMonthStart = dayOfMonth == 1 ? true : false    
    if (isMonthStart) {
        if (state.numMonthsInstalled == null) state.numMonthsInstalled = [:]
        if (state.numMonthsInstalled[league] == null) {
            state.numMonthsInstalled[league] = 0 // don't start average yet since only installed part of the month
            if (state.apiCallsThisMonth == null) state.apiCallsThisMonth = [:]
            state.apiCallsThisMonth[league] = 0
        }
        else {
            state.numMonthsInstalled[league]++
            if (state.avgAPICallsPerMonth == null) state.avgAPICallsPerMonth = [:]
            if (state.avgAPICallsPerMonth[league] != null) {
                state.avgAPICallsPerMonth[league] = state.avgAPICallsPerMonth[league] + ((state.apiCallsThisMonth[league] - state.avgAPICallsPerMonth[league]) / state.numMonthsInstalled[league])
            }
            else {
                state.avgAPICallsPerMonth[league] = state.apiCallsThisMonth[league]
            }           
            state.apiCallsThisMonth[league] = 0
        }
    }
}

def getInterface(type, txt="", link="") {
    switch(type) {
        case "line": 
            return "<hr style='background-color:#555555; height: 1px; border: 0;'></hr>"
            break
        case "header": 
            return "<div style='color:#ffffff;font-weight: bold;background-color:#555555;border: 1px solid;box-shadow: 2px 3px #A9A9A9'> ${txt}</div>"
            break
        case "error": 
            return "<div style='color:#ff0000;font-weight: bold;'>${txt}</div>"
            break
        case "note": 
            return "<div style='color:#333333;font-size: small;'>${txt}</div>"
            break
        case "subField":
            return "<div style='color:#000000;background-color:#ededed;'>${txt}</div>"
            break     
        case "subHeader": 
            return "<div style='color:#000000;font-weight: bold;background-color:#ededed;border: 1px solid;box-shadow: 2px 3px #A9A9A9'> ${txt}</div>"
            break
        case "subSection1Start": 
            return "<div style='color:#000000;background-color:#d4d4d4;border: 0px solid'>"
            break
        case "subSection2Start": 
            return "<div style='color:#000000;background-color:#e0e0e0;border: 0px solid'>"
            break
        case "subSectionEnd":
            return "</div>"
            break
        case "boldText":
            return "<b>${txt}</b>"
            break
        case "link":
            return '<a href="' + link + '" target="_blank" style="color:#51ade5">' + txt + '</a>'
            break
    }
} 


