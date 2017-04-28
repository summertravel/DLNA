package com.zx.zpush;

import org.teleal.cling.model.meta.Device;
import org.teleal.cling.model.types.UDN;

class DeviceDisplay { 
    Device device;
    UDN udn;
 
    public DeviceDisplay(Device device) {
        this.device = device;
        this.udn = device.getIdentity().getUdn();
    } 
 
    public Device getDevice() {
        return device; 
    } 
 
    @Override 
    public boolean equals(Object o) { 
        if (this == o) return true; 
        if (o == null || getClass() != o.getClass()) return false; 
        DeviceDisplay that = (DeviceDisplay) o; 
        return device.equals(that.device); 
    } 
 
    @Override 
    public int hashCode() { 
        return device.hashCode(); 
    } 
 
    @Override 
    public String toString() { 
    	String display;

		if (device.getDetails().getFriendlyName() != null)
			display = device.getDetails().getFriendlyName();
		else
			display = device.getDisplayString();
        // Display a little star while the device is being loaded 
//        return device.isFullyHydrated() ? device.getDisplayString() : device.getDisplayString() + " *";
		return device.isFullyHydrated() ? display : display + " *";
    } 
} 


