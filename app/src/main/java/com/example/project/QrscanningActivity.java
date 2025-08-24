package com.example.project;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.media.Image;
import android.media.ImageReader;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;

import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class QrscanningActivity extends AppCompatActivity
{
    private SurfaceView surfaceView;
    private TextView textView;
    private CameraDevice cameraDevice;
    private CaptureRequest.Builder captureRequestBuilder;
    private ImageReader imageReader;
    private CameraCaptureSession cameraCaptureSession;
    private ImageView imageView;
    private Vibrator vibrator;

    private static final int REQUEST_IMAGE_SELECT = 101;
    private static final int REQUEST_CAMERA_PERMISSION = 102;

    private boolean isScanning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrscanning);

        surfaceView = findViewById(R.id.surface_view);
        textView = findViewById(R.id.text_view);
        imageView = findViewById(R.id.image_view1);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        // Button to upload a QR code from the gallery
        findViewById(R.id.button_upload_qr).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, REQUEST_IMAGE_SELECT);
        });

        // Button to scan a QR code using the camera
        findViewById(R.id.scan_qr).setOnClickListener(v -> {
            if (isScanning) {
                captureImage();
            } else {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
                } else {
                    initializeCamera();
                    isScanning = true;
                }
            }
        });
    }

    private void captureImage() {
        try {
            CaptureRequest.Builder captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureRequestBuilder.addTarget(imageReader.getSurface());

            cameraCaptureSession.capture(captureRequestBuilder.build(), new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);

                    String qrCode = decodeQRCodeFromImage(imageReader.acquireLatestImage());
                    if (qrCode != null) {
                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.post(() -> {
                            textView.setText(qrCode);
                            Toast.makeText(QrscanningActivity.this, "QR Code detected: " + qrCode, Toast.LENGTH_SHORT).show();
                            loadQRCodeImage(qrCode);
                            if (ContextCompat.checkSelfPermission(QrscanningActivity.this, Manifest.permission.VIBRATE) == PackageManager.PERMISSION_GRANTED) {
                                vibrator.vibrate(500); // Vibrate for 500ms
                            }
                        });
                    }
                }
            }, null);
        } catch (CameraAccessException e) {
            Log.e("QRCode", "Camera access exception", e);
        }
    }

    private void initializeCamera() {
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            String cameraId = cameraManager.getCameraIdList()[0];
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            cameraManager.openCamera(cameraId, new CameraDevice.StateCallback() {
                @Override
                public void onOpened(@NonNull CameraDevice camera) {
                    cameraDevice = camera;
                    createCameraPreview();
                }

                @Override
                public void onDisconnected(@NonNull CameraDevice camera) {
                    cameraDevice.close();
                }

                @Override
                public void onError(@NonNull CameraDevice camera, int error) {
                    cameraDevice.close();
                    cameraDevice = null;
                    Log.e("QRCode", "Camera error: " + error);
                }
            }, null);
        } catch (CameraAccessException e) {
            Log.e("QRCode", "Camera access exception", e);
        }
    }

    private void createCameraPreview() {
        Surface surface = surfaceView.getHolder().getSurface();
        try {
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(surface);

            imageReader = ImageReader.newInstance(surfaceView.getWidth(), surfaceView.getHeight(), ImageFormat.YUV_420_888, 2);
            imageReader.setOnImageAvailableListener(reader -> {
                Image image = reader.acquireLatestImage();
                if (image != null) {
                    String qrCode = decodeQRCodeFromImage(image);
                    if (qrCode != null) {
                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.post(() -> {
                            textView.setText(qrCode);
                            Toast.makeText(QrscanningActivity.this, "QR Code detected: " + qrCode, Toast.LENGTH_SHORT).show();
                            loadQRCodeImage(qrCode);
                            if (ContextCompat.checkSelfPermission(QrscanningActivity.this, Manifest.permission.VIBRATE) == PackageManager.PERMISSION_GRANTED) {
                                vibrator.vibrate(500); // Vibrate for 500ms
                            }
                        });
                    }

                    image.close();
                }
            }, null);

            cameraDevice.createCaptureSession(Arrays.asList(surface, imageReader.getSurface()), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    cameraCaptureSession = session;
                    try {
                        session.setRepeatingRequest(captureRequestBuilder.build(), null, null);
                    } catch (CameraAccessException e) {
                        Log.e("QRCode", "Capture session error", e);
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                    Log.e("QRCode", "Camera configuration failed");
                }
            }, null);
        } catch (CameraAccessException e) {
            Log.e("QRCode", "Camera access exception", e);
        }
    }

    private Bitmap convertImageToBitmap(Image image) {
        Image.Plane[] planes = image.getPlanes();
        ByteBuffer buffer = planes[0].getBuffer();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);

        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length, null);
    }

    private String decodeQRCodeFromImage(Image image) {
        try {
            ByteBuffer buffer = image.getPlanes()[0].getBuffer();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);

            int width = image.getWidth();
            int height = image.getHeight();

            PlanarYUVLuminanceSource source = new PlanarYUVLuminanceSource(bytes, width, height, 0, 0, width, height, false);
            BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(source));
            MultiFormatReader reader = new MultiFormatReader();
            Result result = reader.decode(binaryBitmap);
            if (result != null) {
                final String qrCode = result.getText();
                Log.d("QRCode", "QR Code detected: " + qrCode);

                runOnUiThread(() -> {
                    textView.setText(qrCode);
                    Toast.makeText(QrscanningActivity.this, "QR Code detected: " + qrCode, Toast.LENGTH_SHORT).show();
                    loadQRCodeImage(qrCode);
                    if (ContextCompat.checkSelfPermission(QrscanningActivity.this, Manifest.permission.VIBRATE) == PackageManager.PERMISSION_GRANTED) {
                        vibrator.vibrate(500); // Vibrate for 500ms
                    }
                });

                return qrCode;
            }
        } catch (NotFoundException e) {
            Log.e("QRCode", "QR Code not found", e);
        } finally {
            image.close();
        }

        return null;
    }

    private void loadQRCodeImage(String qrCode) {
        if (qrCode == null || qrCode.isEmpty()) {
            Toast.makeText(QrscanningActivity.this, "QR Code is empty or invalid", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference;

        if (qrCode.startsWith("gs://") || qrCode.startsWith("https://firebasestorage")) {
            storageReference = storage.getReferenceFromUrl(qrCode);
        } else if (qrCode.startsWith("http://") || qrCode.startsWith("https://")) {
            // If it's a standard URL, load the image directly from the web
            new LoadImageTask(imageView).execute(qrCode);
            return;
        } else {
            // If it's an identifier, construct the storage reference
            storageReference = storage.getReference().child("images/your_file_name.jpg");
        }

        // Fetch and display the image from Firebase Storage
        storageReference.getBytes(Long.MAX_VALUE).addOnSuccessListener(bytes -> {
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
            } else {
                Toast.makeText(QrscanningActivity.this, "Failed to load image", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(exception -> {
            Log.e("QRCode", "Error loading image from Firebase", exception);
            Toast.makeText(QrscanningActivity.this, "Error loading image", Toast.LENGTH_SHORT).show();
        });
    }

    // AsyncTask to load image from a URL
    private static class LoadImageTask extends AsyncTask<String, Void, Bitmap> {
        private ImageView imageView;

        LoadImageTask(ImageView imageView) {
            this.imageView = imageView;
        }

        @Override
        protected Bitmap doInBackground(String... urls) {
            String url = urls[0];
            try {
                return BitmapFactory.decodeStream(new URL(url).openStream());
            } catch (IOException e) {
                Log.e("QRCode", "Error loading image from URL", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
            } else {
                Toast.makeText(imageView.getContext(), "Failed to load image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_SELECT && resultCode == RESULT_OK && data != null) {
            Uri selectedImage = data.getData();
            if (selectedImage != null) {
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                    imageView.setImageBitmap(bitmap);

                    int[] intArray = new int[bitmap.getWidth() * bitmap.getHeight()];
                    bitmap.getPixels(intArray, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

                    RGBLuminanceSource source = new RGBLuminanceSource(bitmap.getWidth(), bitmap.getHeight(), intArray);
                    BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(source));
                    MultiFormatReader reader = new MultiFormatReader();
                    Result result = reader.decode(binaryBitmap);

                    if (result != null) {
                        String qrCode = result.getText();
                        textView.setText(qrCode);
                        Toast.makeText(QrscanningActivity.this, "QR Code detected: " + qrCode, Toast.LENGTH_SHORT).show();
                        loadQRCodeImage(qrCode);
                    }
                } catch (IOException | NotFoundException e) {
                    Log.e("QRCode", "Error decoding QR Code from image", e);
                    Toast.makeText(QrscanningActivity.this, "Failed to decode QR Code from image", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initializeCamera();
            } else {
                Toast.makeText(this, "Camera permission is required to scan QR codes", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private static String generateUniqueKey() {
        long timestamp = System.currentTimeMillis();
        int randomValue = (int) (Math.random() * 100000); // Adjust the range as needed
        return timestamp + "_" + randomValue;
    }

}
