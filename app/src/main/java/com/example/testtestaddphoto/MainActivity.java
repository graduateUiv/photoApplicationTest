package com.example.testtestaddphoto;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "addphoto";

    private Boolean isPermission = true;

    private static final int PICK_FROM_ALBUM = 1;

    private File photoFile;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tedPermission();

        findViewById(R.id.btnGallery).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 권한 허용에 동의하지 않았을 경우 토스트를 띄웁니다.
                if (isPermission) goToAlbum();
                else
                    Toast.makeText(view.getContext(), getResources().getString(R.string.permission_2), Toast.LENGTH_LONG).show();
            }
        });

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String imagePath = null;
        ImageView oneImage = null;
        ArrayList<Uri> imagesListURI = new ArrayList<>();
        if (resultCode != Activity.RESULT_OK) {
            Toast.makeText(this, "취소 되었습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (requestCode == PICK_FROM_ALBUM) {
            if (data == null) {
            } else {
                if (data.getClipData() == null) {
                    Toast.makeText(this, "다중선택이 불가한 기기입니다.", Toast.LENGTH_LONG).show();
                } else {
                    ClipData clipData = data.getClipData();
                    Log.i("clipData", String.valueOf(clipData.getItemCount())); // 사용자가 몇개 골랐는지
                    if (clipData.getItemCount() == 1) {
                        imagePath = getRealPathFromURI(clipData.getItemAt(0).getUri()); // 사진이 저장되어있는 경로
                        Log.i("imagePath", String.valueOf(imagePath));
                        photoFile = new File(imagePath);
                        Log.i("oneImageFilePath", String.valueOf(photoFile));
                        try {
                            saveInDirectory();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else if (clipData.getItemCount() > 1) {
                        for (int i = 0; i < clipData.getItemCount(); i++) {
                            imagePath = getRealPathFromURI(clipData.getItemAt(i).getUri());
                            photoFile = new File(imagePath);
//                            imagesListURI.add(clipData.getItemAt(i).getUri());
                            try {
                                saveInDirectory();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
    }

    // 사진 URI 얻어오는 함수 : 2016년부터 ? 변경
    private String getRealPathFromURI(Uri contentURI) {
        String result;
        Cursor cursor = getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) { // Source is Dropbox or other similar local file path
            result = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
    }

    // 앨범에서 사진 선택
    private void goToAlbum() {

        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
        intent.putExtra(intent.EXTRA_ALLOW_MULTIPLE, true); // 다중 선택 가능
        intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_FROM_ALBUM);
    }

    // 폴더 만들어서 사진 저장
    private void saveInDirectory() throws IOException {
        File storageDir = new File(Environment.getExternalStorageDirectory() + "/addPhoto/");
        if (!storageDir.exists()) storageDir.mkdirs();
        File savedImageFile = File.createTempFile(photoFile.getName(), ".jpg", storageDir);

        Log.d("photoFile", photoFile.getName());
        Log.d("storage", storageDir.getAbsolutePath());
        copy(photoFile, savedImageFile);
    }

    // 파일 복사
    public static void copy(File src, File dst) throws IOException {
        InputStream in = new FileInputStream(src);
        try {
            OutputStream out = new FileOutputStream(dst);
            try {

                // Transfer bytes from in to out
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            } finally {
                out.close();
            }
        } finally {
            in.close();
        }
    }

    // 권한 설정
    private void tedPermission() {

        PermissionListener permissionListener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                // 권한 요청 성공
                isPermission = true;

            }

            @Override
            public void onPermissionDenied(ArrayList<String> deniedPermissions) {
                // 권한 요청 실패
                isPermission = false;

            }
        };

        TedPermission.with(this)
                .setPermissionListener(permissionListener)
                .setRationaleMessage(getResources().getString(R.string.permission_2))
                .setDeniedMessage(getResources().getString(R.string.permission_1))
                .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
                .check();

    }

}