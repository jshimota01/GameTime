/**
 *  GameTime Child
 *
 *  Copyright\u00A9 2021 Justin Leonard
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
 *  Change History:
 *  v1.2.0 - Full Feature Beta
 *  v1.2.1 - Bug fixes
 *  v1.2.2 - Update scheduling if late night game; Time Formatting improvements
 *  v1.2.3 - Bug fixes
 *  v1.2.4 - Added option to hide game result spoilers
 *  v1.2.5 - Bug fixes
 *  v1.2.6 - Bug fixes
**/

metadata
{
    definition(name: "GameTime Child", namespace: "lnjustin", author: "Justin Leonard", importUrl: "")
    {
        capability "Actuator"
        capability "Switch"
        
        attribute "tile", "string" 
        
        attribute "gameTime", "string"
        attribute "gameTimeStr", "string"
        attribute "status", "string"     
        attribute "opponent", "string" 
        
    }
}

preferences
{
    section
    {
        input name: "parentID", type: "string", title: "Parent App ID"
        input name: "logEnable", type: "bool", title: "Enable debug logging", defaultValue: true
    }
}

def logDebug(msg) 
{
    if (logEnable)
    {
        log.debug(msg)
    }
}    

def on() {
    sendEvent(name: "switch", value: "on")
}

def off() {
    sendEvent(name: "switch", value: "off")
}

def updated()
{
    configure()
}

def parse(String description)
{
    logDebug(description)
}

def configure()
{    
    refresh()
}

def updateDevice(appID, data) {
    state.appID = appID
    sendEvent(name: "gameTime", value: data.game != null ? data.game.gameTime : "No Game Data")
    sendEvent(name: "gameTimeStr", value: data.game != null ? data.game.gameTimeStr : "No Game Data")
    sendEvent(name: "status", value: data.game != null ? data.game.status : "No Game Data")
    sendEvent(name: "opponent", value: data.game != null ? data.game.opponent.displayName : "No Game Data")
    
    sendEvent(name: "tile", value: data.tile)
    sendEvent(name: "switch", value: data.switchValue)
}

def getDeviceData() {
    def data = [gameTime: gameTime]    
}

def refresh()
{

}
