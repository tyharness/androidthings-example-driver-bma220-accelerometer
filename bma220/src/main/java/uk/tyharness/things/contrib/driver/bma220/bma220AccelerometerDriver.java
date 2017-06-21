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
    private static final float DRIVER_MAX_RANGE = bma220.MAX_RANGE_G * SensorManager.GRAVITY_EARTH;
    private static final float DRIVER_RESOLUTION = DRIVER_MAX_RANGE / 32.f; // 6bit signed
    private static final float DRIVER_POWER = bma220.MAX_POWER_UA / 1000.f;
    private static final int   DRIVER_MIN_DELAY_US = Math.round(1000000.f/bma220.MAX_FREQ_HZ);
    private static final int   DRIVER_MAX_DELAY_US = Math.round(1000000.f/bma220.MIN_FREQ_HZ);
    private static final int   DRIVER_VERSION = 1;
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
}