# LiveDataImagePicker

An easy way to get image from Gallery or Camera with request runtime permission on Android M using LiveData

## Setup

To use this library your ` minSdkVersion` must be >= 16.

In your build.gradle :

```gradle
dependencies {
    compile 'com.mlsdev.rximagepicker:library:2.1.5'    
}
```

## Example

```java
RxImagePicker.with(context).requestImage(Sources.CAMERA).subscribe(new Consumer<Uri>() {
                    @Override
                    public void accept(@NonNull Uri uri) throws Exception {
                        //Get image by uri using one of image loading libraries. I use Glide in sample app.
                    }
                });
```

Request multiple images on Android Api level 18+ :

```java
RxImagePicker.with(getContext()).requestMultipleImages().subscribe(new Consumer<List<Uri>>() {
                    @Override
                    public void accept(@NonNull List<Uri> uris) throws Exception {
                        //Get images by uris.
                    }
                });
```

### Using converters

```java
RxImagePicker.with(context).requestImage(Sources.GALLERY)
    .flatMap(new Function<Uri, ObservableSource<Bitmap>>() {
                    @Override
                    public ObservableSource<Bitmap> apply(@NonNull Uri uri) throws Exception {
                        return RxImageConverters.uriToBitmap(getContext(), uri);
                    }
                }).subscribe(new Consumer<Bitmap>() {
                    @Override
                    public void accept(@NonNull Bitmap bitmap) throws Exception {
                        // Do something with Bitmap
                    }
                });
```

```java
RxImagePicker.with(context).requestImage(Sources.GALLERY)
    .flatMap(new Function<Uri, ObservableSource<File>>() {
                    @Override
                    public ObservableSource<File> apply(@NonNull Uri uri) throws Exception {
                        return RxImageConverters.uriToFile(getContext(), uri, new File("YOUR FILE"));
                    }
                }).subscribe(new Consumer<File>() {
                    @Override
                    public void accept(@NonNull File file) throws Exception {
                        // Do something with your file copy
                    }
                });
```

## About

LiveDataImagePicker is forked from [mlsdev]: https://github.com/MLSDev/RxImagePicker
