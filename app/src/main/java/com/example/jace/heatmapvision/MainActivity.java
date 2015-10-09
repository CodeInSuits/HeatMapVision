package com.example.jace.heatmapvision;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.chhavi.uploadingandviewimage.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class MainActivity extends Activity {

    ImageView viewImage;
    Bitmap bbitmap = null;
    Button b;
    Button heatVison;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        b=(Button)findViewById(R.id.buttonPanel);
        heatVison = (Button) findViewById(R.id.button);
        heatVison.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bitmap heatVision = bbitmap.copy(bbitmap.getConfig(), true);
                edgeDetection(heatVision,10);
                viewImage.setImageBitmap(heatVision);
            }
        });
        viewImage=(ImageView) findViewById(R.id.image);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds options to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    private void selectImage() {

        final CharSequence[] options = { "Take Photo", "Choose from Gallery","Cancel" };

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Add Photo!");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (options[item].equals("Take Photo"))
                {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    File f = new File(android.os.Environment.getExternalStorageDirectory(), "temp.jpg");
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
                    startActivityForResult(intent, 1);
                }
                else if (options[item].equals("Choose from Gallery"))
                {
                    Intent intent = new   Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent, 2);

                }
                else if (options[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == 1) {
                File f = new File(Environment.getExternalStorageDirectory().toString());
                for (File temp : f.listFiles()) {
                    if (temp.getName().equals("temp.jpg")) {
                        f = temp;
                        break;
                    }
                }
                try {
                    Bitmap bitmap;
                    BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();

                    bitmap = BitmapFactory.decodeFile(f.getAbsolutePath(),
                            bitmapOptions);

                    viewImage.setImageBitmap(bitmap);

                    String path = android.os.Environment
                            .getExternalStorageDirectory()
                            + File.separator
                            + "Phoenix" + File.separator + "default";
                    f.delete();
                    OutputStream outFile = null;
                    File file = new File(path, String.valueOf(System.currentTimeMillis()) + ".jpg");
                    try {
                        outFile = new FileOutputStream(file);
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outFile);
                        outFile.flush();
                        outFile.close();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (requestCode == 2) {

                Uri selectedImage = data.getData();
                String[] filePath = { MediaStore.Images.Media.DATA };
                Cursor c = getContentResolver().query(selectedImage, filePath, null, null, null);
                c.moveToFirst();
                int columnIndex = c.getColumnIndex(filePath[0]);
                String picturePath = c.getString(columnIndex);
                c.close();
                Bitmap thumbnail = (BitmapFactory.decodeFile(picturePath));
                bbitmap = thumbnail;
                //edgeDetection(thumbnail,10);
                Log.w("path of image from gallery......******************.........", picturePath + "");
                viewImage.setImageBitmap(thumbnail);

            }
        }
    }
    public void edgeDetection (Bitmap bitmap,double amount)
    {
        if(bitmap==null){
            return;
        }
        int topPixel;
        int bottomPixel;
        double topAverage = 0.0;
        double bottomAverage = 0.0;
        int endY = bitmap.getHeight()-1;
        for (int y = 0; y < endY; y++)
        {
            // loop through the x values from 0 to width
            for (int x = 0; x < bitmap.getWidth(); x++) { // get the top and bottom pixels
                int spot = bitmap.getPixel(x,y);
                int redValue = Color.red(spot);
                int blueValue = Color.blue(spot);
                int greenValue = Color.green(spot);
                boolean contrast = Math.abs(redValue-greenValue)>=130||Math.abs(redValue
                        -blueValue)>=130||Math.abs(blueValue-greenValue)>=130;
                boolean inRange = (x>=172&&x<=945)&&(y>=217&&y<=700);
                if(contrast&&inRange)
                {
                    //do nothing;
                }
                else{
                    bottomPixel = bitmap.getPixel(x,y+1);
                    int redValueBottom = Color.red(bottomPixel);
                    int blueValueBottom = Color.blue(bottomPixel);
                    int greenValueBottom = Color.green(bottomPixel);
                    // get the color averages for the two pixels
                    topAverage = (redValue + blueValue + greenValue) / 3.0;
                    bottomAverage = (redValueBottom + blueValueBottom + greenValueBottom) / 3.0; //check if the absolute value of the difference ? is less than the amount ?/
                    if (Math.abs(topAverage-bottomAverage) < amount)
                    {
                        bitmap.setPixel(x,y,Color.WHITE); // else set the color to black
                    }
                    else
                    {
                        bitmap.setPixel(x,y,Color.BLACK);
                    }
                }
            }
        }
    }
}
