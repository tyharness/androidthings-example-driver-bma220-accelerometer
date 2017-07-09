package uk.tyharness.things.contrib.driver.bma220;

import android.hardware.Sensor;
import android.hardware.SensorManager;

import com.google.android.things.userdriver.UserDriverManager;
import com.google.android.things.userdriver.UserSensor;
import com.google.android.things.userdriver.UserSensorDriver;
import com.google.android.things.userdriver.UserSensorReading;

import java.io.IOException;
import java.util.UUID;


public class bma220AccelerometerDriver implements AutoCloseable {
    private static final String TAG = bma220AccelerometerDriver.class.getSimpleName();
    private static final String DRIVER_NAME = "BMA220Accelerometer";
    private static final String DRIVER_VENDOR = "Bosch";
    private static final float  DRIVER_MAX_RANGE = bma220.MAX_RANGE_G * SensorManager.GRAVITY_EARTH;
    private static final float  DRIVER_RESOLUTION = DRIVER_MAX_RANGE / 32.f; // 6bit signed
    private static final float  DRIVER_POWER = bma220.MAX_POWER_UA / 1000.f;
    private static final int    DRIVER_MIN_DELAY_US = Math.round(1000000.f/bma220.MAX_FREQ_HZ);
    private static final int    DRIVER_MAX_DELAY_US = Math.round(1000000.f/bma220.MIN_FREQ_HZ);
    private static final int    DRIVER_VERSION = 1;
    private static final String DRIVER_REQUIRED_PERMISSION = "";
    private bma220 mDevice;
    private UserSensor mUserSensor;

   
    public bma220AccelerometerDriver(String bus) throws IOException {
        mDevice = new bma220(bus);
    }

    @Override
    public void close() throws IOException {
        unregister();
        if (mDevice != null) {
            try {
                mDevice.close();
            } finally {
                mDevice = null;
            }
        }
    }

    
    public void register() {
        if (mDevice == null) {
            throw new IllegalStateException("cannot register a closed driver");
        }
        if (mUserSensor == null) {
            mUserSensor = build(mDevice);
            UserDriverManager.getManager().registerSensor(mUserSensor);
        }
    }

    
    public void unregister() {
        if (mUserSensor != null) {
            UserDriverManager.getManager().unregisterSensor(mUserSensor);
            mUserSensor = null;
        }
    }

    static UserSensor build(final bma220 bma220a) {
        return new UserSensor.Builder()
                .setType(Sensor.TYPE_ACCELEROMETER)
                .setName(DRIVER_NAME)
                .setVendor(DRIVER_VENDOR)
                .setVersion(DRIVER_VERSION)
                .setMaxRange(DRIVER_MAX_RANGE)
                .setResolution(DRIVER_RESOLUTION)
                .setPower(DRIVER_POWER)
                .setMinDelay(DRIVER_MIN_DELAY_US)
                .setRequiredPermission(DRIVER_REQUIRED_PERMISSION)
                .setMaxDelay(DRIVER_MAX_DELAY_US)
                .setUuid(UUID.randomUUID())
                .setDriver(new UserSensorDriver() {
                    
                   @Override
                    public UserSensorReading read() throws IOException {
                        float[] sample = bma220a.readSample();
                        for (int i=0; i<sample.length; i++) {
                            sample[i] = sample[i] * SensorManager.GRAVITY_EARTH;
                        }
                        return new UserSensorReading(
                                sample,
                                SensorManager.SENSOR_STATUS_ACCURACY_HIGH); 
                    }

                    @Override
                    public void setEnabled(boolean enabled) throws IOException {
                        if (enabled) {
                            bma220a.setMode(bma220.MODE_ACTIVE);
                        } else {
                            bma220a.setMode(bma220.MODE_STANDBY);
                        }
                    }
                    
                   
                    
                })
                  
                .build();
    }
    
    
    
    //0x0
    public int _getChipID() throws IOException {
        return mDevice.getChipID();
    } 
    
    //0x2
    public int _getChipREV() throws IOException {
        return mDevice.getChipREV();
    } 
    
    
   //0xA
    public int _get_A_REG() throws IOException {
        return mDevice.get_A_REG();
    } 
    
    
    //0xC
    public int _get_C_REG() throws IOException {
        return mDevice.get_C_REG();
    } 
    
    //0xE
    public int _get_E_REG() throws IOException {
        return mDevice.get_E_REG();
    } 
    
    //0x10
    public int _get_10_REG() throws IOException {
        return mDevice.getChipREV();
    } 
    
   
  
    
    //0x12
    public int _getOrientation() throws IOException {
        return mDevice.getOrientation();
    } 
    
    
     //0x14
    public int _getTapDetection() throws IOException {
        return mDevice.getTapDetection();
    } 
    
   
     //0x16
    public int _get_16_REG() throws IOException {
        return mDevice.get_16_REG();
    } 
    
    
    //0x18
    public int _get_18_REG() throws IOException {
        return mDevice.get_18_REG();
    }  
    
    
     //0x1A
    public int _get_1A_REG() throws IOException {
        return mDevice.get_1A_REG();
    } 
    
 
    
    
    //0x1C
    public int _getLatching()  throws IOException {
        return mDevice.getLatching() ;
    } 
    
    
    //0x20
    public int _getFilterAndBandwidth() throws IOException {
        return mDevice.getFilterAndBandwidth();
    } 
    
    //0x22
    public int _getRangeMode() throws IOException {
        return mDevice.getRangeMode();
    } 
    
    public boolean _getAccelerationState() {
        return mDevice.getAccelerationState();
    } 
    
    public boolean _getIntState() {
        return mDevice.getIntState();
    } 
    
    
    
    
}
