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
    static final float MAX_POWER_UA = 250.f;        //Idd 250uA 25deg C
    static final float MAX_FREQ_HZ = 2.f;
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
    
    
    private float oldx = 0.f;
    private float oldy = 0.f;
    private float oldz = 0.f;
    
    private boolean IntState = false;
    private boolean accelerationState = false;
 
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
        
               
        defaultSetup();
        
       
        
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

 
    
    
    
    
     // Power mode Setup to do    
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
    @SuppressWarnings("ResourceType")
    public @Mode int getMode() throws IOException, IllegalStateException {
        if (mDevice == null) {
            throw new IllegalStateException("device not connected");
        }
       //return mDevice.readRegByte(REG_MODE);
       return 1;
    }

  

 
    public void defaultSetup() throws IOException, IllegalStateException {
      
           if (mDevice == null) {
            throw new IllegalStateException("device not connected");
            }
      
            //reset interrupt settings
            mDevice.writeRegByte(_1C_REG, (byte) 0x00);//poke only
            
            mDevice.writeRegByte(FILTER_REG, setFilter(0) );  
            mDevice.writeRegByte(RANGE_REG, setRange(0) );  
            
            //set up interrupts

            //Any Motion Detection:           
            //set up tapping x,y,z  en_data, en_orient, en_slopez, en_slopey, en_slopex  en_tt_x, en_tt_y, en_tt_
            mDevice.writeRegByte(_1A_REG, (byte) 0b00111111);//poke
           
            
            
            // 0x12  orient_ex   slope_filt bit 6   slope_th bit5,bit4,bit3,bit2   slope_dur bit1,bit0
            mDevice.writeRegByte(_12_REG, (byte) 0b01000111); //
             
            
            //Latching
            //mDevice.writeRegByte(_1C_REG, (byte) 0x00); //unlatched 
            //Latched or 
            //mDevice.writeRegByte(_1C_REG, (byte) 0b00010000); //latch time 0.25 secs 001
            //mDevice.writeRegByte(_1C_REG, (byte) 0b00100000); //latch time 0.5 secs 010
            //mDevice.writeRegByte(_1C_REG, (byte) 0b00110000); //latch time 1 secs 011
              mDevice.writeRegByte(_1C_REG, (byte) 0b01000000); //latch time 2 secs 100
            //mDevice.writeRegByte(_1C_REG, (byte) 0b01010000); //latch time 4 secs 101
            //mDevice.writeRegByte(_1C_REG, (byte) 0b01110000); //latch time 8 secs 110
            //mDevice.writeRegByte(_1C_REG, (byte) 0b01110000); //latch time 8 secs 111 permentaly latched            
           
            //bit 4 tip_en, bit 4 double tap0, single tap 1 default is 8 which is a single tap
            //oreient blocking  bit 2, 3
            //tt_samp bit1, bit2 number of samples on wake up 
             mDevice.writeRegByte(_14_REG, (byte) 0b00001100);//poke for double tapping
            
      
      
    }  
      
      
      
    
    public void ResetInteruptSettings()throws IOException, IllegalStateException {
        if (mDevice == null) {
            throw new IllegalStateException("device not connected");
        }
         mDevice.writeRegByte(_1C_REG, (byte) 0x00);//poke only 
    }
    
     public  int getChipID() throws IOException, IllegalStateException {
        if (mDevice == null) {
            throw new IllegalStateException("device not connected");
        }
        return mDevice.readRegByte(Chip_REG);
    } 
    
    public  int getChipREV() throws IOException, IllegalStateException {
        if (mDevice == null) {
            throw new IllegalStateException("device not connected");
        }
        return mDevice.readRegByte(REV_REG);
    } 
    
    
 
    public void setFilterAndBandwidth()throws IOException, IllegalStateException {
        if (mDevice == null) {
            throw new IllegalStateException("device not connected");
        }
         mDevice.writeRegByte(FILTER_REG, setFilter(0) ); 
    }
     
    
   public  int getFilterAndBandwidth() throws IOException, IllegalStateException {
        if (mDevice == null) {
            throw new IllegalStateException("device not connected");
        }
        return mDevice.readRegByte(FILTER_REG);
    } 
    
    
  public void setRangeMode()throws IOException, IllegalStateException {
        if (mDevice == null) {
            throw new IllegalStateException("device not connected");
        }
         mDevice.writeRegByte(RANGE_REG, setRange(0));   
    }
     
    
   public  int getRangeMode() throws IOException, IllegalStateException {
        if (mDevice == null) {
            throw new IllegalStateException("device not connected");
        }
        return mDevice.readRegByte(RANGE_REG);
    }     
    
    
   
  public void setTapDetection()throws IOException, IllegalStateException {
        if (mDevice == null) {
            throw new IllegalStateException("device not connected");
        }
         //set up tapping x,y,z  en_data, en_orient, en_slopez, en_slopey, en_slopex  en_tt_x, en_tt_y, en_tt_z
         mDevice.writeRegByte(_1A_REG, (byte) 0b00111111);
    }
     
    
   public  int getTapDetection() throws IOException, IllegalStateException {
        if (mDevice == null) {
            throw new IllegalStateException("device not connected");
        }
        return mDevice.readRegByte(_1A_REG);
    }      
   
   
    public void setOrientation()throws IOException, IllegalStateException {
        if (mDevice == null) {
            throw new IllegalStateException("device not connected");
        }
         // 0x12  orient_ex   slope_filt bit 6   slope_th bit5,bit4,bit3,bit2   slope_dur bit1,bit0
         mDevice.writeRegByte(_12_REG, (byte) 0b01000111);
    }
     
    
   public  int getOrientation() throws IOException, IllegalStateException {
        if (mDevice == null) {
            throw new IllegalStateException("device not connected");
        }
        return mDevice.readRegByte(_12_REG);
    }   
   
   
   
   
    public  int get_A_REG() throws IOException, IllegalStateException {
        if (mDevice == null) {
            throw new IllegalStateException("device not connected");
        }
        return mDevice.readRegByte(_A_REG);
    }    
   
    public  int get_C_REG() throws IOException, IllegalStateException {
        if (mDevice == null) {
            throw new IllegalStateException("device not connected");
        }
        return mDevice.readRegByte(_C_REG);
    }    
   
    public  int get_E_REG() throws IOException, IllegalStateException {
        if (mDevice == null) {
            throw new IllegalStateException("device not connected");
        }
        return mDevice.readRegByte(_E_REG);
    }    
   
   
   public  int get_16_REG() throws IOException, IllegalStateException {
        if (mDevice == null) {
            throw new IllegalStateException("device not connected");
        }
        return mDevice.readRegByte(_16_REG);
    }  
    
    
  public  int get_18_REG() throws IOException, IllegalStateException {
        if (mDevice == null) {
            throw new IllegalStateException("device not connected");
        }
        return mDevice.readRegByte(_18_REG);
    }  
   
    
    
  public  int get_1A_REG() throws IOException, IllegalStateException {
        if (mDevice == null) {
            throw new IllegalStateException("device not connected");
        }
        return mDevice.readRegByte(_1A_REG);
    }    
    
  public  int get_1C_REG() throws IOException, IllegalStateException {
        if (mDevice == null) {
            throw new IllegalStateException("device not connected");
        }
        return mDevice.readRegByte(_1C_REG);
    }    
    
   
   
   
   
  public void setLatching()throws IOException, IllegalStateException {
        if (mDevice == null) {
            throw new IllegalStateException("device not connected");
        }    
            //unlatched 
            // mDevice.writeRegByte(_1C_REG, (byte) 0x00);
            //Latched or 
            //mDevice.writeRegByte(_1C_REG, (byte) 0b00010000); //latch time 0.25 secs 001
            //mDevice.writeRegByte(_1C_REG, (byte) 0b00100000); //latch time 0.5 secs 010
            //mDevice.writeRegByte(_1C_REG, (byte) 0b00110000); //latch time 1 secs 011
              mDevice.writeRegByte(_1C_REG, (byte) 0b01000000); //latch time 2 secs 100
            //mDevice.writeRegByte(_1C_REG, (byte) 0b01010000); //latch time 4 secs 101
            //mDevice.writeRegByte(_1C_REG, (byte) 0b01110000); //latch time 8 secs 110
            //mDevice.writeRegByte(_1C_REG, (byte) 0b01110000); //latch time 8 secs 111 permentaly latched         
    }  
   
   
   public  int getLatching() throws IOException, IllegalStateException {
        if (mDevice == null) {
            throw new IllegalStateException("device not connected");
        }
        return mDevice.readRegByte(_1C_REG);
    }   
  
  
   public void setTapOption()throws IOException, IllegalStateException {
        if (mDevice == null) {
            throw new IllegalStateException("device not connected");
        }
         
         mDevice.writeRegByte(_14_REG, (byte) 0b00001100);//poke for double tapping
    }
     
    
   public  int getTapOption() throws IOException, IllegalStateException {
        if (mDevice == null) {
            throw new IllegalStateException("device not connected");
        }
        return mDevice.readRegByte(_14_REG);
    }   
   
   public boolean getAccelerationState(){
       return accelerationState;
   }
   
   public boolean getIntState(){
       return IntState;
   }
    
    
public float[] readSample() throws IOException, IllegalStateException {
        if (mDevice == null) {
            throw new IllegalStateException("device not connected");
        }
          
        
         
        
        
          int xdata = mDevice.readRegByte(X_REG);  
          int ydata = mDevice.readRegByte(Y_REG);  
          int zdata = mDevice.readRegByte(Z_REG);   
        
          float x = realData(xdata,0);
          float y = realData(ydata,0); 
          float z = realData(zdata,0);
          
          if ( Math.abs(x-oldx) > 0.07 || Math.abs(y-oldy) > 0.07 || Math.abs(z-oldz) > 0.07    ){            
            oldx = x;
            oldy = y;
            oldz = z;
            accelerationState = true;
            return new float[]{x,y,z};
          }else{          
           //return new float[]{0.f,0.f,0.f};
            accelerationState = false;
            return new float[]{oldx,oldy,oldz};
          }
       
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
    
  private byte setFilter(int mode){
      
      //Reg 0x20 default 0x00    
      //Filter Config   bit 2    bit 1   bit 0,   
      //bit6, bit5, bit4,bit 3
      //serial high bw bit 7 
      
      //mode 0 default 0x00
      byte F = 0x00;  //1kHz
     
      if (mode == 1)F = 0x01;// 500Hz
      if (mode == 2)F = 0x02;// 250Hz
      if (mode == 3)F = 0x03;// 125Hz
      if (mode == 4)F = 0x04;// 64Hz
      if (mode == 5)F = 0x05;// 32Hz
      return F;
        
    }
    
    
    
    
    private byte setRange(int mode){
      
      //Reg 0x22 default 0x00    
      //Range  bit 0, bit 1     
      //sbist (off,x,y,z) bit 2 and 3  self test
      //sbist sign bit 4  selftest
      byte R = 0x00;  //+/- 2g
     
      if (mode == 1)R = 0x01;// +/- 4g
      if (mode == 2)R = 0x02;// +/- 8g
      if (mode == 3)R = 0x03;// +/- 16g
      
      return R;
        
    }   
    
    
    
}
