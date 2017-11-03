package com.rapidbizapps.newapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import io.fotoapparat.Fotoapparat;
import io.fotoapparat.FotoapparatSwitcher;
import io.fotoapparat.error.CameraErrorCallback;
import io.fotoapparat.hardware.CameraException;
import io.fotoapparat.hardware.provider.CameraProviders;
import io.fotoapparat.parameter.LensPosition;
import io.fotoapparat.parameter.ScaleType;
import io.fotoapparat.parameter.update.UpdateRequest;
import io.fotoapparat.photo.BitmapPhoto;
import io.fotoapparat.preview.Frame;
import io.fotoapparat.preview.FrameProcessor;
import io.fotoapparat.result.PendingResult;
import io.fotoapparat.result.PhotoResult;
import io.fotoapparat.view.CameraView;

import static io.fotoapparat.log.Loggers.fileLogger;
import static io.fotoapparat.log.Loggers.logcat;
import static io.fotoapparat.log.Loggers.loggers;
import static io.fotoapparat.parameter.selector.AspectRatioSelectors.standardRatio;
import static io.fotoapparat.parameter.selector.FlashSelectors.autoFlash;
import static io.fotoapparat.parameter.selector.FlashSelectors.autoRedEye;
import static io.fotoapparat.parameter.selector.FlashSelectors.off;
import static io.fotoapparat.parameter.selector.FlashSelectors.torch;
import static io.fotoapparat.parameter.selector.FocusModeSelectors.autoFocus;
import static io.fotoapparat.parameter.selector.FocusModeSelectors.continuousFocus;
import static io.fotoapparat.parameter.selector.FocusModeSelectors.fixed;
import static io.fotoapparat.parameter.selector.LensPositionSelectors.lensPosition;
import static io.fotoapparat.parameter.selector.PreviewFpsRangeSelectors.rangeWithHighestFps;
import static io.fotoapparat.parameter.selector.Selectors.firstAvailable;
import static io.fotoapparat.parameter.selector.SensorSensitivitySelectors.highestSensorSensitivity;
import static io.fotoapparat.parameter.selector.SizeSelectors.biggestSize;
import static io.fotoapparat.result.transformer.SizeTransformers.scaled;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private final PermissionsDelegate permissionsDelegate = new PermissionsDelegate(this);
    private boolean hasCameraPermission;
    private CameraView cameraView;
    private FotoapparatSwitcher fotoapparatSwitcher;
    private Fotoapparat frontFotoapparat;
    private Fotoapparat backFotoapparat;
    private List<String> imgPathList = new ArrayList<>();
    private List<RecyclerModel> mainList = new ArrayList<>();
    private RecyclerAdapter adapter;
    private FloatingActionButton fabSelected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cameraView = (CameraView) findViewById(R.id.camera_view);
        hasCameraPermission = permissionsDelegate.hasCameraPermission();

        if (hasCameraPermission) {
            cameraView.setVisibility(View.VISIBLE);
        } else {
            permissionsDelegate.requestCameraPermission();
        }

        setupFotoapparat();

        takePictureOnClick();
        focusOnLongClick();
        switchCameraOnClick();
        toggleTorchOnSwitch();
        zoomSeekBar();
        setRecycler();

    }

    private void setupFotoapparat() {
        frontFotoapparat = createFotoapparat(LensPosition.FRONT);
        backFotoapparat = createFotoapparat(LensPosition.BACK);
        fotoapparatSwitcher = FotoapparatSwitcher.withDefault(backFotoapparat);
    }

    private void zoomSeekBar() {
        SeekBar seekBar = (SeekBar) findViewById(R.id.zoomSeekBar);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                fotoapparatSwitcher
                        .getCurrentFotoapparat()
                        .setZoom(progress / (float) seekBar.getMax());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Do nothing
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Do nothing
            }
        });
    }

    private void toggleTorchOnSwitch() {
        SwitchCompat torchSwitch = (SwitchCompat) findViewById(R.id.torchSwitch);

        torchSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                fotoapparatSwitcher
                        .getCurrentFotoapparat()
                        .updateParameters(
                                UpdateRequest.builder()
                                        .flash(
                                                isChecked
                                                        ? torch()
                                                        : off()
                                        )
                                        .build()
                        );
            }
        });
    }

    private void switchCameraOnClick() {
        View switchCameraButton = findViewById(R.id.switchCamera);
        switchCameraButton.setVisibility(
                canSwitchCameras()
                        ? View.VISIBLE
                        : View.GONE
        );
        switchCameraOnClick(switchCameraButton);
    }

    private void switchCameraOnClick(View view) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchCamera();
            }
        });
    }

    private void focusOnLongClick() {
        cameraView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                fotoapparatSwitcher.getCurrentFotoapparat().autoFocus();

                return true;
            }
        });
    }

    private void takePictureOnClick() {
        cameraView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePicture();
            }
        });
    }

    private boolean canSwitchCameras() {
        return frontFotoapparat.isAvailable() == backFotoapparat.isAvailable();
    }

    private Fotoapparat createFotoapparat(LensPosition position) {
        return Fotoapparat
                .with(this)
                .cameraProvider(CameraProviders.v1()) // change this to v2 to test Camera2 API
                .into(cameraView)
                .previewScaleType(ScaleType.CENTER_CROP)
                .photoSize(standardRatio(biggestSize()))
                .lensPosition(lensPosition(position))
                .focusMode(firstAvailable(
                        continuousFocus(),
                        autoFocus(),
                        fixed()
                ))
                .flash(firstAvailable(
                        autoRedEye(),
                        autoFlash(),
                        torch(),
                        off()
                ))
                .previewFpsRange(rangeWithHighestFps())
                .sensorSensitivity(highestSensorSensitivity())
                .frameProcessor(new SampleFrameProcessor())
                .logger(loggers(
                        logcat(),
                        fileLogger(this)
                ))
                .cameraErrorCallback(new CameraErrorCallback() {
                    @Override
                    public void onError(CameraException e) {
                        Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_LONG).show();
                    }
                })
                .build();
    }

    private void takePicture() {
        PhotoResult photoResult = fotoapparatSwitcher.getCurrentFotoapparat().takePicture();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH_mm_ss");
        String dateTime = sdf.format(Calendar.getInstance().getTime());
        final File file = new File(
                getExternalFilesDir("photos"),
                dateTime + ".jpg"
        );


        photoResult.saveToFile(file);

        photoResult
                .toBitmap(scaled(0.25f))
                .whenAvailable(new PendingResult.Callback<BitmapPhoto>() {
                    @Override
                    public void onResult(BitmapPhoto result) {
                        ImageView imageView = (ImageView) findViewById(R.id.result);

                        imageView.setImageBitmap(result.bitmap);
                        imageView.setRotation(-result.rotationDegrees);
                        mainList.add(0, new RecyclerModel(file.getAbsolutePath(), false));
                        adapter.setList(mainList);
                    }
                });
    }

    private void switchCamera() {
        if (fotoapparatSwitcher.getCurrentFotoapparat() == frontFotoapparat) {
            fotoapparatSwitcher.switchTo(backFotoapparat);
        } else {
            fotoapparatSwitcher.switchTo(frontFotoapparat);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (hasCameraPermission) {
            fotoapparatSwitcher.start();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (hasCameraPermission) {
            fotoapparatSwitcher.stop();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (permissionsDelegate.resultGranted(requestCode, permissions, grantResults)) {
            fotoapparatSwitcher.start();
            cameraView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onClick(View v) {
        int i1 = v.getId();
        if (i1 == R.id.fab_selected_main) {
            ArrayList<String> list = new ArrayList<>();
            for (RecyclerModel model : adapter.getSelectedList()) {
                list.add(model.getFilePath());
            }
            Intent i = getIntent();
            i.putStringArrayListExtra(SelectMediaActivity.INTENTFILES, list);
            setResult(Activity.RESULT_OK, i);
            finish();

        }
    }

    private class SampleFrameProcessor implements FrameProcessor {

        @Override
        public void processFrame(Frame frame) {
            // Perform frame processing, if needed
        }

    }


    private void setRecycler() {
        RecyclerView recycler = (RecyclerView) findViewById(R.id.recycler);
        fabSelected = (FloatingActionButton) findViewById(R.id.fab_selected_main);
        fabSelected.setOnClickListener(this);
        recycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        adapter = new RecyclerAdapter(MainActivity.this, new EnableFab() {
            @Override
            public void enableFab(boolean enable) {
                if (enable)
                    fabSelected.setVisibility(View.VISIBLE);
                else fabSelected.setVisibility(View.GONE);
            }
        });
        recycler.setAdapter(adapter);
        mainList = getFile(new File(Environment.getExternalStorageDirectory() + "/DCIM/Camera"));
        adapter.setList(mainList);
    }

    public List<RecyclerModel> getFile(File dir) {
        File listFile[] = dir.listFiles();
        if (listFile != null && listFile.length > 0) {
            for (File file : listFile) {
                if (file.isDirectory()) {
                    getFile(file);
                } else {
                    if (file.getName().endsWith(".png")
                            || file.getName().endsWith(".jpg")
                            || file.getName().endsWith(".jpeg")
                            || file.getName().endsWith(".bmp")
                            || file.getName().endsWith(".webp")) {
                        if (!imgPathList.contains(file.getAbsolutePath())) {
                            imgPathList.add(file.getAbsolutePath());
                        }
                    }
                }
            }
        }
        List<RecyclerModel> rvList = new ArrayList<>();
        for (String str : imgPathList) {
            RecyclerModel model = new RecyclerModel();
            model.setFilePath(str);
            model.setSelected(false);
            rvList.add(model);
        }

        return rvList;
    }

    class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.Holder> {
        Context context;
        List<RecyclerModel> list = new ArrayList<>();
        List<RecyclerModel> selectedList = new ArrayList<>();
        EnableFab enableFab;


        private RecyclerAdapter(Context context, EnableFab enableFab) {
            this.context = context;
            this.enableFab = enableFab;

        }

        public void setList(List<RecyclerModel> list) {
            this.list = list;
            notifyDataSetChanged();
        }

        @Override
        public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new Holder(LayoutInflater.from(context).inflate(R.layout.item, parent, false));
        }

        @Override
        public void onBindViewHolder(Holder holder, int position) {
            //Picasso.with(context).load(new File(list.get(position))).placeholder(R.mipmap.ic_launcher).error(R.mipmap.ic_launcher).resize(60,60).into(holder.iv_item);
            // Bitmap bmp = BitmapFactory.decodeFile(list.get(position));
            //   holder.iv_item.setImageBitmap(bmp);
            final RecyclerModel model = list.get(position);
            if (model.isSelected) {
                holder.ivSelected.setImageResource(R.mipmap.ic_check);
                holder.ivSelected.setBackgroundColor(Color.parseColor("#4c000000"));
            } else {
                holder.ivSelected.setImageBitmap(null);
                holder.ivSelected.setBackgroundColor(Color.TRANSPARENT);
            }

            File file = new File(model.getFilePath());
            Uri imageUri = Uri.fromFile(file);

            Glide.with(context)
                    .load(imageUri)
                    .into(holder.iv_item);

            holder.iv_item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    RecyclerModel dummy = list.get(list.indexOf(model));
                    if (selectedList.contains(dummy)) {
                        selectedList.remove(dummy);
                        list.get(list.indexOf(model)).setSelected(false);
                        notifyDataSetChanged();
                    } else if (selectedList.size() < 3) {
                        selectedList.add(dummy);
                        list.get(list.indexOf(model)).setSelected(true);
                        notifyDataSetChanged();
                    }
                    //enabling fabButton
                    if (selectedList.size() > 0)
                        enableFab.enableFab(true);
                    else
                        enableFab.enableFab(false);


                }
            });
        }

        private List<RecyclerModel> getSelectedList() {
            return selectedList;
        }

        @Override
        public int getItemCount() {
            return list.size();
        }


        class Holder extends RecyclerView.ViewHolder {
            ImageView iv_item, ivSelected;

            public Holder(View itemView) {
                super(itemView);
                iv_item = (ImageView) itemView.findViewById(R.id.img);
                ivSelected = (ImageView) itemView.findViewById(R.id.imgSeleected);
            }
        }
    }

    interface EnableFab {
        void enableFab(boolean enable);
    }

    private class RecyclerModel {
        String filePath = "";
        boolean isSelected = false;

        RecyclerModel() {

        }

        RecyclerModel(String filePath,
                      boolean isSelected) {
            this.filePath = filePath;
            this.isSelected = isSelected;
        }


        public String getFilePath() {
            return filePath;
        }

        public void setFilePath(String filePath) {
            this.filePath = filePath;
        }

        public void setSelected(boolean selected) {
            isSelected = selected;
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent i = getIntent();
        setResult(Activity.RESULT_CANCELED, i);
        finish();
    }
}
