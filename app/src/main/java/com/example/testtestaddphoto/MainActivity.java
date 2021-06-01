package com.example.testtestaddphoto;

import android.Manifest;
import android.app.Activity;
import android.app.FragmentManager;
import android.content.ClipData;
import android.content.Intent;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;
import android.media.ExifInterface;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private Boolean isPermission = true;
    private static final int PICK_FROM_ALBUM = 1;
    private File photoFile;
    private String photoLocation;
    private String photoDate;
    showLocationFolderFragment fragment_showFolder = new showLocationFolderFragment();


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


//        transaction.replace(R.id.showLocationFragment, test);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.showLocationFragment, fragment_showFolder); // fragment삽입할 위치, fragment
//        transaction.commit();

//        View v = inflater.inflate(R.layout.fragment_show_location_folder, container, false);
//        GridView gridView = findViewById(R.id.list);
//        FolderAdapter adapter = new FolderAdapter();
//
//        File storageDir = new File(Environment.getExternalStorageDirectory() + "/addPhoto/");
//        if (!storageDir.exists()) storageDir.mkdirs();
//        File[] locationFoldersNames = storageDir.listFiles();
//        Log.d("folderName", locationFoldersNames[0].getName());
//        if(locationFoldersNames.length>0) {
//            for (int i = 0; i < locationFoldersNames.length; i++) {
//                adapter.addItem(new Folder(locationFoldersNames[i].getName()));
//            }
//            gridView.setAdapter(adapter);
//        }else{
//            Toast.makeText(this, "폴더없음!", Toast.LENGTH_LONG).show();
//        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String imagePath = null;
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

                    for (int i = 0; i < clipData.getItemCount(); i++) {
                        imagePath = getRealPathFromURI(clipData.getItemAt(i).getUri());
                        photoFile = new File(imagePath);
                        try {
                            checkDate();
                            createDirectory();
                            checkLocation();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                    transaction.detach(fragment_showFolder).attach(fragment_showFolder).commit();
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

    // 저장폴더 생성
    private void createDirectory() throws IOException {
        File storageDir = new File(Environment.getExternalStorageDirectory() + "/addPhoto/");
        if (!storageDir.exists()) storageDir.mkdirs(); // 폴더가 존재하지 않는다면 생성
//        File savedImageFile = new File(storageDir, photoFile.getName());
//        Log.d("photoFile", photoFile.getName());
//        Log.d("storage", storageDir.getAbsolutePath());
//        Log.d("savedImageFile", savedImageFile.getPath());
//        copy(photoFile, savedImageFile);
    }

    // 파일 복사
    private static void copy(File src, File dst) throws IOException {
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

    private void checkLocation() {
        String fileName = photoFile.getPath();
        Log.d("fileName", fileName);
        try {
            ExifInterface exif = new ExifInterface(fileName);
            showExif(exif);
            saveInLocation();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "위치가져오기오류!", Toast.LENGTH_LONG).show();
        }
    }

    private void showExif(ExifInterface exif) throws IOException {
        Geocoder geocoder = new Geocoder(this);
        List<Address> list = null;
        String latitude = getTagString(ExifInterface.TAG_GPS_LATITUDE, exif); // 위도
        latitude = latitude.substring(14); // GPSLatitude : null -> null
        String longitude = getTagString(ExifInterface.TAG_GPS_LONGITUDE, exif); // 경도
        longitude = longitude.substring(15); // GPSLongitude : null -> null
        Log.d("latitude", latitude);
        Log.d("longitude", longitude);
        if (latitude.contains("null") || longitude.contains("null")) {
            photoLocation = "noLocation";
        } else {
            double d1 = changeGPS(latitude);
            double d2 = changeGPS(longitude);
            try {
                list = geocoder.getFromLocation(d1, d2, 10);
            } catch (IOException e) {
                e.printStackTrace();
                list = geocoder.getFromLocation(d1, d2, 10);
                Toast.makeText(this, "위치변환오류!", Toast.LENGTH_LONG).show();
            }

            if (list != null) {
                if (list.size() == 0) {
                    Toast.makeText(this, "해당주소없음!", Toast.LENGTH_LONG).show();
                } else {
                    photoLocation = list.get(0).getLocality();
                    if (photoLocation == null) {
                        photoLocation = list.get(0).getAdminArea();
                        if (photoLocation == null) {
                            photoLocation = "noLocation";
                        }
                    }
                    Log.d("listAdminArea", list.get(0).getAdminArea());
                    Log.d("list", list.get(0).toString());
                    Log.d("listA", list.get(0).getAddressLine(0));
                    Toast.makeText(this, "주소있음!", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private double changeGPS(String gpsInfo) {
        String splitGPSInfo[] = gpsInfo.split(",");
//        Log.d("splitGPSInfo0", splitGPSInfo[0]);
//        Log.d("splitGPSInfo1", splitGPSInfo[1]);
//        Log.d("splitGPSInfo2", splitGPSInfo[2]);
        Double changeGPSInfo[] = new Double[3];
        for (int i = 0; i < splitGPSInfo.length; i++) {
            int idx = splitGPSInfo[i].indexOf("/");
            double num1 = Double.parseDouble(splitGPSInfo[i].substring(0, idx));
            double num2 = Double.parseDouble(splitGPSInfo[i].substring(idx + 1));
            changeGPSInfo[i] = num1 / num2;
        }
        double d1 = changeGPSInfo[0];
        double d2 = changeGPSInfo[1];
        double d3 = changeGPSInfo[2];
        double changedGPSInfo = d1 + d2 / 60 + d3 / 3600;
        return changedGPSInfo;
    }

    private String getTagString(String tag, ExifInterface exif) {
        return (tag + " : " + exif.getAttribute(tag) + "\n");
    }

    private void saveInLocation() throws IOException {
        File storageDir = new File(Environment.getExternalStorageDirectory() + "/addPhoto/" + photoLocation + "/");
        if (!storageDir.exists()) storageDir.mkdirs(); // 폴더가 존재하지 않는다면 생성
        File savedImageFile = new File(storageDir, photoFile.getName());

        Log.d("photoFile", photoFile.getName());
        Log.d("storage", storageDir.getAbsolutePath());
        Log.d("savedImageFile", savedImageFile.getPath());
        copy(photoFile, savedImageFile);
    }

    private void checkDate() throws IOException {
        String fileName = photoFile.getPath();
        Log.d("fileName", fileName);
        ExifInterface exif = new ExifInterface(fileName);
        photoDate = getTagString(ExifInterface.TAG_DATETIME, exif);
        Log.d("photoDate", photoDate);
    }


}