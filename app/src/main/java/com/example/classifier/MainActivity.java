package com.example.classifier;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class MainActivity extends AppCompatActivity {

    Bitmap bm;
    Button btn_pic;
    ImageView imageView;
    Interpreter tflite;
    Button inferButton;
    TextView outputNumber;


    private  static final int IMAGE_MEAN = 120;
    private  static  final float IMAGE_STD = 120.0f;
    private ByteBuffer imgData = null;
    private int DIM_IMG_SIZE_X = 200;
    private  int DIM_IMG_SIZE_Y = 200;
    private int DIM_PIXEL_SIZE =3;
    private int[] intValues;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        intValues = new int[DIM_IMG_SIZE_X*DIM_IMG_SIZE_Y];
        try{
            tflite = new Interpreter(loadModelFile());
        } catch (Exception ex){
            ex.printStackTrace();
        }

        btn_pic = (Button) findViewById(R.id.btn_capture);
        imageView = (ImageView)findViewById(R.id.image);
        inferButton = (Button)findViewById(R.id.inferButton);
        imgData = ByteBuffer.allocateDirect(3*DIM_IMG_SIZE_Y*DIM_IMG_SIZE_X*4);
        imgData.order(ByteOrder.nativeOrder());
        outputNumber = (TextView) findViewById(R.id.outputNumber);

        inferButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                outputNumber.setText("ye chal raha hai");

                Bitmap bit = getResizeBitmap(bm,200,200);
                convertBitmapToByteBuffer(bit);


                float[][] outputval;
                outputval = new float[1][1];

                tflite.run(imgData,outputval);
                if(outputval[0][0] > .8)
                {
                    outputNumber.setText("wet");
                }else
                {
                    outputNumber.setText("dry");
                }
                outputNumber.setText(outputNumber.getText()+"    "+Float.toString(outputval[0][0]));
                Toast toast=Toast.makeText(getApplicationContext(),"chala",Toast.LENGTH_SHORT);
                toast.setMargin(90,90);
                toast.show();


            }
        });


        btn_pic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent indent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(indent, 0);
                Toast toast=Toast.makeText(getApplicationContext(),"ek button chala",Toast.LENGTH_SHORT);
                toast.show();
                outputNumber.setText("hello ye lo");
            }
        });

    }

    protected  void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode,data);

        Bitmap bitmap = (Bitmap)data.getExtras().get("data");
        bm = bitmap;
        imageView.setImageBitmap(bitmap);

    }

    public Bitmap getResizeBitmap(Bitmap bm , int newWidth, int newHeight){
        int width = bm.getWidth();
        int height =bm.getHeight();
        float scaleWidth = ((float) newWidth)/width;
        float scaleheight = ((float) newHeight)/ height;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth,scaleheight);
        Bitmap resizedBitmap = Bitmap.createBitmap(bm,0,0,width,height,matrix,false);
        return resizedBitmap;
    }


    private MappedByteBuffer loadModelFile() throws IOException{
        AssetFileDescriptor fileDescriptor = this.getAssets().openFd("tf_lite_model_84%.tflite");
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY,startOffset,declaredLength);
    }

    private void convertBitmapToByteBuffer(Bitmap bitmap){
        if(imgData == null){
            return;
        }
        imgData.rewind();
        bitmap.getPixels(intValues,0,bitmap.getWidth(),0,0,bitmap.getWidth(),bitmap.getHeight());
        int pixel =0;
        for(int i =0 ; i < DIM_IMG_SIZE_X;++i){
            for ( int j=0;j<DIM_IMG_SIZE_Y;++j){
                final int val = intValues[pixel++];
                imgData.putFloat((((val>>16) & 0xFF)-IMAGE_MEAN)/IMAGE_STD);
                imgData.putFloat((((val>>8) & 0xFF)-IMAGE_MEAN)/IMAGE_STD);
                imgData.putFloat((((val) & 0xFF)-IMAGE_MEAN)/IMAGE_STD);
            }
        }

    }

}
