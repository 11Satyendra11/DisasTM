package com.satyendra.opencv;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.media.Image;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.Toast;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;



import org.opencv.android.JavaCameraView;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class OpenCameraView extends JavaCameraView implements Camera.PictureCallback {

    private static final String TAG = OpenCameraView.class.getSimpleName();

    private String mPictureFileName;

    public static int minWidthQuality = 400;

    private Context context;

    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;


    public OpenCameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }


    public List<String> getEffectList() {
        return mCamera.getParameters().getSupportedColorEffects();
    }


    public boolean isEffectSupported() {
        return (mCamera.getParameters().getColorEffect() != null);
    }


    public String getEffect() {
        return mCamera.getParameters().getColorEffect();
    }

    public void setEffect(String effect) {
        Camera.Parameters params = mCamera.getParameters();
        params.setColorEffect(effect);
        mCamera.setParameters(params);
    }


    public List<Camera.Size> getResolutionList() {
        return mCamera.getParameters().getSupportedPreviewSizes();
    }


    public void setResolution(Camera.Size resolution) {
        disconnectCamera();
        mMaxHeight = resolution.height;
        mMaxWidth = resolution.width;
        connectCamera(getWidth(), getHeight());
    }


    public Camera.Size getResolution() {
        return mCamera.getParameters().getPreviewSize();
    }


    public void takePicture(final String fileName) {
        Log.i(TAG, "Taking picture");
       System.out.println("Taking Picture"+ fileName);
        this.mPictureFileName = fileName;
        mCamera.setPreviewCallback(null);

        mCamera.takePicture(null, null, this);
    }


    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        Log.i(TAG, "Saving a bitmap to file");
        // The camera preview was automatically stopped. Start it again.
        mCamera.startPreview();
       mCamera.setPreviewCallback(this);


        //***********


         //*************
        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
        Uri uri = Uri.parse(mPictureFileName);

        Log.d(TAG, "selectedImage: " + uri);
        Bitmap bm = null;
        bm = rotate(bitmap, 90);

        // Write the image in a file (in jpeg format)
        try {
            FileOutputStream fos = new FileOutputStream(mPictureFileName);
            bm.compress(Bitmap.CompressFormat.JPEG, 80, fos);
            fos.close();

            mCamera.startPreview();
            mCamera.setPreviewCallback(this);



        } catch (java.io.IOException e) {
            Log.e("PictureDemo", "Exception in photoCallback", e);
        }
        bitmapimage(mPictureFileName);


        md5(mPictureFileName);
    }







    private static Bitmap rotate(Bitmap bm, int rotation) {
        if (rotation != 0) {
            Matrix matrix = new Matrix();
            matrix.postRotate(rotation);
            Bitmap bmOut = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);
            return bmOut;
        }
        return bm;
    }



    private static String getFileChecksum(MessageDigest digest, File file) throws IOException
    {
        //Get file input stream for reading the file content
        FileInputStream fis = new FileInputStream(file);

        //Create byte array to read data in chunks
        byte[] byteArray = new byte[1024];
        int bytesCount = 0;

        //Read file data and update in message digest
        while ((bytesCount = fis.read(byteArray)) != -1) {
            digest.update(byteArray, 0, bytesCount);
        };

        //close the stream; We don't need it now.
        fis.close();

        //Get the hash's bytes
        byte[] bytes = digest.digest();

        //This bytes[] has bytes in decimal format;
        //Convert it to hexadecimal format
        StringBuilder sb = new StringBuilder();
        for(int i=0; i< bytes.length ;i++)
        {
            sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
        }

        //return complete hash
        return sb.toString();
    }



    public  void md5(String filePath) {



        //Create checksum for this file
        File file = new File(filePath);

        //Use MD5 algorithm
        MessageDigest md5Digest = null;
        try {
            md5Digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }




        //Get the checksum
        String checksum =null;

       // System.out.println("Here the checksum is first : " + checksum);
        try {
            checksum = getFileChecksum(md5Digest, file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //System.out.println("here the check sum  is second:" + checksum);


        //see checksum
        Log.i(TAG,"The checksum value is " + checksum);
       System.out.println(" The final output checksum : " + checksum);


        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("data");
        databaseReference.push().setValue(checksum);

    }

    public void bitmapimage(String mPictureFileName1)
    {
        try {
            FileInputStream in = new FileInputStream(mPictureFileName1);
            BufferedInputStream buf = new BufferedInputStream(in);
            byte[] bMapArray= new byte[buf.available()];
            buf.read(bMapArray);
            Bitmap bMap = BitmapFactory.decodeByteArray(bMapArray, 0, bMapArray.length,null);

            String signature = getLbpSign(bMap);

            firebaseDatabase = FirebaseDatabase.getInstance();
            databaseReference = firebaseDatabase.getReference("data");
            databaseReference.push().setValue(signature);

            System.out.println("The lbp signature  is : " + signature);
        }catch(Exception e)
        {

        }

    }

    private String getLbpSign(Bitmap frame) {

        StringBuilder sb = new StringBuilder();
        int[][] imageArray = new int[frame.getWidth()][frame.getHeight()];
        int currentPixelValue, newPixelValue;

        for(int row=0; row < frame.getWidth(); row++){
            for(int col=0; col < frame.getHeight(); col++){

                imageArray[row][col]= frame.getPixel(row,col);
            }
        }

        for(int row=1; row<frame.getWidth()-1; row++){
            for(int col=1; col<frame.getHeight()-1; col++){
                currentPixelValue=imageArray[row][col];
                newPixelValue=0;
                if(imageArray[row-1][col-1]>currentPixelValue) newPixelValue=newPixelValue+1;
                if(imageArray[row-1][col]>currentPixelValue) newPixelValue=newPixelValue+2;
                if(imageArray[row-1][col+1]>currentPixelValue) newPixelValue=newPixelValue+4;
                if(imageArray[row][col+1]>currentPixelValue) newPixelValue=newPixelValue+8;
                if(imageArray[row+1][col+1]>currentPixelValue) newPixelValue=newPixelValue+16;
                if(imageArray[row+1][col]>currentPixelValue) newPixelValue=newPixelValue+32;
                if(imageArray[row+1][col-1]>currentPixelValue) newPixelValue=newPixelValue+64;
                if(imageArray[row][col-1]>currentPixelValue) newPixelValue=newPixelValue+128;
                sb.append(newPixelValue);
            }
        }

        sb.append(System.lineSeparator());
        return sb.toString();
    }
}
