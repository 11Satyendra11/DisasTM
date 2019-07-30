package com.satyendra.opencv;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.regex.Pattern;


import com.nbsp.materialfilepicker.MaterialFilePicker;
import com.nbsp.materialfilepicker.ui.FilePickerActivity;
import com.satyendra.opencv.utils.Constants;
import com.satyendra.opencv.utils.ImagePicker;
import com.satyendra.opencv.utils.Utilities;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int PICK_IMAGE_ID = 303;

    private String fileLocation;



    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    Button uploadButton;

    String md5string;

    String firebasedata;

    String firematch;

    String bitmapsig;

    String bitmapsigmatch;

    String signature;

    Button aboutButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Dexter.withActivity(this)
                .withPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (!report.areAllPermissionsGranted()) {
                            Toast.makeText(MainActivity.this, "You need to grant all permission to use this app features", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {

                    }
                })
                .check();

        Button startButton = (Button) findViewById(R.id.start_button);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent cvIntent = new Intent(MainActivity.this, OpenCVCamera.class);
                startActivity(cvIntent);

            }
        });


        //uploadbutton
        uploadButton = (Button)findViewById(R.id.upload_button);
        uploadButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {


                new MaterialFilePicker()
                        .withActivity(MainActivity.this)
                        .withRequestCode(1000)
                        //.withFilter(Pattern.compile(".*\\.jpg$")) // Filtering files and directories by file name using regexp
                        .withFilterDirectories(true) // Set directories filterable (false by default)
                        .withHiddenFiles(true) // Show hidden files and folders
                        .start();

            }
        });

        //about button

        aboutButton = (Button)findViewById(R.id.about_Button);
        aboutButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view)
            {
                openDialog();
            }
        });
    }

    private void openDialog() {
        ExampleDialog exampleDialog = new ExampleDialog();
        exampleDialog.show(getSupportFragmentManager(),"example dialog");
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode,  Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


            String filePath = data.getStringExtra(FilePickerActivity.RESULT_FILE_PATH);
            // Do anything with file
            System.out.println(filePath);
            //Toast.makeText(getApplicationContext(),filePath,Toast.LENGTH_LONG).show();

                md5string = md5(filePath);
                //System.out.println("This is md5 string: " + md5string);

                bitmapsig =  bitmapimage(filePath);
                System.out.println("This is bitmap signature: " + bitmapsig);

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("data");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                showData(dataSnapshot);
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        }





    private void convertBitmapToImage(Bitmap bitmap, String filePath) throws IOException {
        if (!new File(Constants.SCAN_IMAGE_LOCATION).exists()) {
            new File(Constants.SCAN_IMAGE_LOCATION).mkdir();
        }
        File outFile = new File(filePath);
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(outFile));

        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, bufferedOutputStream);
        bufferedOutputStream.close();

        //**********


    }


    //File picker


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



    public  String md5(String filePath) {



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

        return checksum;
//        firebaseDatabase = FirebaseDatabase.getInstance();
//        databaseReference = firebaseDatabase.getReference("data");
//        databaseReference.push().setValue(checksum);

    }

    private void showData(DataSnapshot dataSnapshot) {
        for(DataSnapshot ds: dataSnapshot.getChildren())
        {

            firebasedata = ds.getValue().toString();
            //Toast.makeText(getApplicationContext(),firebasedata,Toast.LENGTH_LONG).show();
            //System.out.println("This is firebase string: "  + firebasedata);
            if(firebasedata.equals(md5string))
            {
              // Toast.makeText(getApplicationContext(),"Match",Toast.LENGTH_LONG).show();
                firematch = firebasedata;

            }
            if(firebasedata.equals(bitmapsig))
            {
               bitmapsigmatch = firebasedata;

            }


        }
        if(firematch.equals(md5string) && bitmapsigmatch.equals(bitmapsig))
        {
            Toast.makeText(getApplicationContext(),"Match",Toast.LENGTH_LONG).show();
        }else
        {
            Toast.makeText(getApplicationContext(),"Not a Match",Toast.LENGTH_LONG).show();
        }
    }

    public String bitmapimage(String mPictureFileName1)
    {
        try {
            FileInputStream in = new FileInputStream(mPictureFileName1);
            BufferedInputStream buf = new BufferedInputStream(in);
            byte[] bMapArray= new byte[buf.available()];
            buf.read(bMapArray);
            Bitmap bMap = BitmapFactory.decodeByteArray(bMapArray, 0, bMapArray.length,null);

            signature= getLbpSign(bMap);



           // System.out.println("The lbp signature  is : " + signature);
        }catch(Exception e)
        {

        }
        return signature;
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







