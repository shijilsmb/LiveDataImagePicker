package com.mlsdev.rximagepicker;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.content.ClipData;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


import static android.app.Activity.RESULT_OK;

public class RxImagePicker extends Fragment {

    private static final int SELECT_PHOTO = 100;
    private static final int TAKE_PHOTO = 101;

    private static final String TAG = RxImagePicker.class.getSimpleName();
    private static Uri cameraPictureUrl;

    private MutableLiveData<Boolean> attachedSubject;
    private MutableLiveData<Uri> publishSubject;
    private MutableLiveData<List<Uri>> publishSubjectMultipleImages;
    //private MutableLiveData<Integer> canceledSubject;

    private boolean allowMultipleImages = false;
    private Sources imageSource;

    public static RxImagePicker with(FragmentManager fragmentManager) {
        RxImagePicker rxImagePickerFragment = (RxImagePicker) fragmentManager.findFragmentByTag(TAG);
        if (rxImagePickerFragment == null) {
            rxImagePickerFragment = new RxImagePicker();
            fragmentManager.beginTransaction()
                    .add(rxImagePickerFragment, TAG)
                    .commit();
        }
        return rxImagePickerFragment;
    }

    public LiveData<Uri> requestImage(final Sources source) {
    //public Observable<Uri> requestImage(final Sources source) {
        //publishSubject = PublishSubject.create();
        publishSubject = new MediatorLiveData<>();
        attachedSubject = new MutableLiveData<>();
        //canceledSubject = new MutableLiveData<>();
        allowMultipleImages = false;
        imageSource = source;
        requestPickImage();
        //return publishSubject.takeUntil(canceledSubject);
        return publishSubject;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public LiveData<List<Uri>> requestMultipleImages() {
        publishSubjectMultipleImages = new MutableLiveData<>();
        attachedSubject = new MutableLiveData<>();
        //canceledSubject = new MutableLiveData<>();
        imageSource = Sources.GALLERY;
        allowMultipleImages = true;
        requestPickImage();
        //return publishSubjectMultipleImages.takeUntil(canceledSubject);
        return publishSubjectMultipleImages;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        attachedSubject.setValue(true);
        //attachedSubject.onComplete();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        attachedSubject.setValue(true);
        //attachedSubject.onComplete();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            pickImage();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case SELECT_PHOTO:
                    handleGalleryResult(data);
                    break;
                case TAKE_PHOTO:
                    onImagePicked(cameraPictureUrl);
                    break;
            }
        } else {
           // canceledSubject.setValue(requestCode);
            onImagesPicked(null);
            onImagePicked(null);
        }
    }

    private void handleGalleryResult(Intent data) {
        if (allowMultipleImages) {
            ArrayList<Uri> imageUris = new ArrayList<>();
            ClipData clipData = data.getClipData();
            if (clipData != null) {
                for (int i = 0; i < clipData.getItemCount(); i++) {
                    imageUris.add(clipData.getItemAt(i).getUri());
                }
            } else {
                imageUris.add(data.getData());
            }
            onImagesPicked(imageUris);
        } else {
            onImagePicked(data.getData());
        }
    }

    private void requestPickImage() {
        if (!isAdded()) {
           /* attachedSubject.subscribe(new Consumer<Boolean>() {
                @Override
                public void accept(Boolean attached) throws Exception {
                    pickImage();
                }
            });*/
            attachedSubject.observe(this, new Observer<Boolean>() {
                @Override
                public void onChanged(@Nullable Boolean added) {

                    if (added != null && added) {
                        attachedSubject.removeObserver(this);
                        pickImage();
                    }
                }
            });


        } else {
            pickImage();
        }
    }

    private void pickImage() {
        if (!checkPermission()) {
            return;
        }

        int chooseCode = 0;
        Intent pictureChooseIntent = null;

        switch (imageSource) {
            case CAMERA:
                cameraPictureUrl = createImageUri();
                pictureChooseIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                pictureChooseIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraPictureUrl);
                chooseCode = TAKE_PHOTO;
                break;
            case GALLERY:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    pictureChooseIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    pictureChooseIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, allowMultipleImages);
                    pictureChooseIntent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                } else {
                    pictureChooseIntent = new Intent(Intent.ACTION_GET_CONTENT);
                }
                pictureChooseIntent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                pictureChooseIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                pictureChooseIntent.setType("image/*");
                chooseCode = SELECT_PHOTO;
                break;
        }

        startActivityForResult(pictureChooseIntent, chooseCode);
    }

    private boolean checkPermission() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
            }
            return false;
        } else {
            return true;
        }
    }

    private Uri createImageUri() {
        ContentResolver contentResolver = getActivity().getContentResolver();
        ContentValues cv = new ContentValues();
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        cv.put(MediaStore.Images.Media.TITLE, timeStamp);
        return contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv);
    }

    private void onImagesPicked(List<Uri> uris) {
        if (publishSubjectMultipleImages != null) {
            publishSubjectMultipleImages.setValue(uris);
           // publishSubjectMultipleImages.onComplete();
        }
    }

    private void onImagePicked(Uri uri) {
        if (publishSubject != null) {
            publishSubject.setValue(uri);
            //publishSubject.onComplete();
        }
    }

}
