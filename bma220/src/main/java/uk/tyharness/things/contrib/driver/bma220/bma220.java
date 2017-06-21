package uk.tyharness.things.contrib.driver.bma220;



import com.google.android.things.pio.I2cDevice;
import com.google.android.things.pio.PeripheralManagerService;

import java.io.IOException;



import android.support.annotation.IntDef;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


public class bma220 implements AutoCloseable {
    private static final String TAG = bma220.class.getSimpleName();
     /**
     * I2C slave address of the BMA220 0xA
     */
    public static final int I2C_ADDRESS = 0xA;
    static final float MAX_RANGE_G = 2.f;
    static final float MAX_POWER_UA = 250.f; // Idd 250uA 25deg C
    static final float MAX_FREQ_HZ = 5.f;
    static final float MIN_FREQ_HZ = 1.f;
    
    private static final  int Chip_REG      = 0x0;  //8 bit read only
    private static final  int REV_REG       = 0x2;  //8 bit read only  default  0x00
    private static final  int X_REG         = 0x4;  //6 bit read only
    private static final  int Y_REG         = 0x6;  //6 bit read only
    private static final  int Z_REG         = 0x8;  //6 bit read only
    private static final  int _A_REG        = 0xA;  //high_hy[1:0] high_dur[5:0] default  0x7F
    private static final  int _C_REG        = 0xC;  //low_th[3:0] high_th[3:0]   default  
    private static final  int _E_REG        = 0xE;  //low_hy[1:0] low_dur[5:0]   default
    private static final  int _10_REG       = 0x10; //
    private static final  int _12_REG       = 0x12; //
    private static final  int _14_REG       = 0x14; //
    private static final  int _16_REG       = 0x16; //
    private static final  int _18_REG       = 0x18; //
    private static final  int _1A_REG       = 0x1A; //
    private static final  int _1C_REG       = 0x1C; //reset interrupt write only
    private static final  int _1E_REG       = 0x1E; //    
    private static final  int FILTER_REG    = 0x20; //bit 0, bit 1, bit 2
    private static final  int RANGE_REG     = 0x22; //bit 0 and bit 1 
    //private static final  int _24_REG       = 0x24; //reserved
    //private static final  int _26_REG       = 0x26; //reserved
    //private static final  int _28_REG       = 0x28; //reserved
    //private static final  int _2A_REG       = 0x2A; //reserved
    //private static final  int _2C_REG       = 0x2C; //reserved
    //private static final  int _23_REG       = 0x2E; //3 bits
    //private static final  int _30_REG       = 0x30; //Suspend
    //private static final  int _32_REG       = 0x32; //Soft Reset  
    
 
    private I2cDevice mDevice;

    /**
     * Power mode.
     */
    
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({MODE_STANDBY, MODE_ACTIVE})
    public @interface Mode {}

    public static final int MODE_STANDBY = 0; // i2c on, output off, low power
    public static final int MODE_ACTIVE = 1;  // i2c on, output on

   
    
    
    
 
    public bma220(String bus) throws IOException {
        PeripheralManagerService pioService = new PeripheralManagerService();
        I2cDevice device = pioService.openI2cDevice(bus, I2C_ADDRESS);
        try {
            connect(device);
        } catch (IOException|RuntimeException e) {
            try {
                close();
            } catch (IOException|RuntimeException ignored) {
            }
            throw e;
        }
    }

    
    bma220(I2cDevice device) throws IOException {
        connect(device);
    }

    private void connect(I2cDevice device) throws IOException {
        if (mDevice != null) {
            throw new IllegalStateException("device already connected");
        }
        mDevice = device;
        //setSamplingRate(RATE_120HZ);
        
    }

    /**
     * Close the driver and the underlying device.
     */
    @Override
    public void close() throws IOException {
        if (mDevice != null) {
            try {
                mDevice.close();
            } finally {
                mDevice = null;
            }
        }
    }

    
    //Power mode
    public void setMode(@Mode int mode) throws IOException, IllegalStateException {
        if (mDevice == null) {
            throw new IllegalStateException("device not connected");
        }
       // mDevice.writeRegByte(REG_MODE, (byte) mode);
    }

    /**
     * Get current power mode.
     * @return
     * @throws IOException
     * @throws IllegalStateException
     */
    
    /*
    @SuppressWarnings("ResourceType")
    public @Mode int getMode() throws IOException, IllegalStateException {
        if (mDevice == null) {
            throw new IllegalStateException("device not connected");
        }
        return mDevice.readRegByte(REG_MODE);
    }

    /**
     * Set current sampling rate
     * @param rate
     * @throws IOException
     * @throws IllegalStateException
     */
    
    /*
    public void setSamplingRate(@SamplingRate int rate) throws IOException, IllegalStateException {
        if (mDevice == null) {
            throw new IllegalStateException("device not connected");
        }
        mDevice.writeRegByte(REG_SAMPLING_RATE, (byte) rate);
    }

    /**
     * Get current sampling rate.
     * @return
     * @throws IOException
     * @throws IllegalStateException
     */
    
    /*
    
    @SuppressWarnings("ResourceType")
    public @SamplingRate int getSamplingRate() throws IOException, IllegalStateException {
        if (mDevice == null) {
            throw new IllegalStateException("device not connected");
        }
        return mDevice.readRegByte(REG_SAMPLING_RATE);
    }
    */


    
/*    
    
 //Initalise the BMA220 set up the accelerometer.///////////////////////////////////////////////
            Log.i(TAG, "Set up the BMA220 in General Mode " );
            
            
            mDevice.writeRegByte(FILTER_REG, setFilter(0) );  
            Log.i(TAG, "_20_FILTER_REG written: "+ Integer.toBinaryString( mDevice.readRegByte(FILTER_REG) )  ); 
                         
            mDevice.writeRegByte(RANGE_REG, setRange(RangeMode) );  
            Log.i(TAG, "_22_Range REG Written : " + Integer.toBinaryString(mDevice.readRegByte(RANGE_REG)) );
            
            
            //set up interrupts

            //Any Motion Detection:           
            //set up tapping x,y,z  en_data, en_orient, en_slopez, en_slopey, en_slopex  en_tt_x, en_tt_y, en_tt_z
            mDevice.writeRegByte(_1A_REG, (byte) 0b00111111);//poke
            tapset = mDevice.readRegByte(_1A_REG);//peek
            Log.i(TAG, "_1A_REG write operation Enable Interrupt " + Integer.toHexString(tapset) );
            
            
            // 0x12  orient_ex   slope_filt bit 6   slope_th bit5,bit4,bit3,bit2   slope_dur bit1,bit0
            mDevice.writeRegByte(_12_REG, (byte) 0b01000111); //
            Log.i(TAG, "_12_REG write operation Enable Interrupt " + Integer.toBinaryString( mDevice.readRegByte(_12_REG) ) );
                       
          
            mDevice.writeRegByte(_1C_REG, setLatching(4)); //latch time 2 secs 100                    
            Log.i(TAG, "_1C_REG write int Latch mode " + Integer.toBinaryString( mDevice.readRegByte(_1C_REG) ) );
            
            // 0x14 default = 0x08
            //bit 4 tip_en, bit 4 double tap0, single tap 1 default is 8 which is a single tap
            //oreient blocking  bit 2, 3
            //tt_samp bit1, bit2 number of samples on wake up 
            mDevice.writeRegByte(_14_REG, (byte) 0b00001100);//poke for double tapping
            Log.i(TAG, "_14_REG write tap option " + Integer.toBinaryString( mDevice.readRegByte(_14_REG) ) );   
    
    
*/    
    
    
public float[] readSample() throws IOException, IllegalStateException {
        if (mDevice == null) {
            throw new IllegalStateException("device not connected");
        }
        
          
          int xdata = mDevice.readRegByte(X_REG);  
          int ydata = mDevice.readRegByte(Y_REG);  
          int zdata = mDevice.readRegByte(Z_REG);   
        
        
        return new float[]{
                realData(xdata,0),
                realData(ydata,0),
                realData(zdata,0)
        };
    }
    
    
 private float realData(int data, int mode){   
      //mode 0 by default
      float slope = 0.0625f;             //  1.94/31;    sensitivity   +/- 2g range  
      
      if (mode == 1)  slope = 0.125f;   // sensitivity   +/- 4g range  
      if (mode == 2)  slope = 0.25f;    // sensitivity   +/- 8g range  
      if (mode == 3)  slope = 0.5f;    //  sensitivity   +/- 16g range  
        
      float d = (float) (data >> 2); 
      
      float x = d*slope;            
      return x;      
    }     
    
    
    
    
    
}
