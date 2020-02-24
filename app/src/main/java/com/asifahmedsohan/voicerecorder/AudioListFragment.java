package com.asifahmedsohan.voicerecorder;


import android.media.MediaPlayer;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.io.File;
import java.io.IOException;


/**
 * A simple {@link Fragment} subclass.
 */
public class AudioListFragment extends Fragment implements AudioListAdapter.onItemListClick{

    private static final String TAG = "AudioListFragment";

    private ConstraintLayout playerSheet;
    private BottomSheetBehavior bottomSheetBehavior;

    private RecyclerView audioList;

    private File [] allFiles;
    private File fileToPlay = null;

    private AudioListAdapter audioListAdapter;

    private MediaPlayer mediaPlayer = null;
    private boolean isPlaying = false;

    //UI elements
    private ImageButton playBtn;
    private TextView playerHeader;
    private TextView playerFileName;

    private SeekBar playerSeekbar;
    private Handler seekbarHandler;
    private Runnable updateSeekbar;

    public AudioListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_audio_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        playerSheet = view.findViewById(R.id.constraintLayout);
        bottomSheetBehavior = BottomSheetBehavior.from(playerSheet);
        audioList = view.findViewById(R.id.audio_list_view);

        playBtn = view.findViewById(R.id.player_play_btn);
        playerHeader = view.findViewById(R.id.player_header_title);
        playerFileName = view.findViewById(R.id.player_fileName);

        playerSeekbar = view.findViewById(R.id.player_seekbar);


        final String path = getActivity().getExternalFilesDir("/").getAbsolutePath();
        File directory = new File(path);
        allFiles = directory.listFiles();

        audioListAdapter = new AudioListAdapter(allFiles, this);

        audioList.setHasFixedSize(true);
        audioList.setLayoutManager(new LinearLayoutManager(getContext()));
        audioList.setAdapter(audioListAdapter);


        bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN){
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                //Nothing to do here.
            }
        });

        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPlaying){
                    pauseAudio();

                } else {
                    if (fileToPlay != null){
                        resumeAudio();
                    }

                }
            }
        });

        playerSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                pauseAudio();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int progress = seekBar.getProgress();
                mediaPlayer.seekTo(progress);
                resumeAudio();
            }
        });
    }

    @Override
    public void onClickListener(File file, int position) {
        Log.d(TAG, "onClickListener: " + file.getName());

        fileToPlay = file;

        if(isPlaying){
            stopAudio();
            isPlaying = false;
            playAudio(fileToPlay);

        } else {
            fileToPlay = file;
            playAudio(fileToPlay);

        }
    }

    private void pauseAudio(){
        mediaPlayer.pause();
        playBtn.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.player_play_btn, null));
        isPlaying = false;
        seekbarHandler.removeCallbacks(updateSeekbar);
    }

    private void resumeAudio(){
        mediaPlayer.start();
        playBtn.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.player_pause_btn, null));
        isPlaying = true;

        updateRunnable();
        seekbarHandler.postDelayed(updateSeekbar, 0);
    }

    private void stopAudio() {
        //stop the audio
        playBtn.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.player_play_btn, null));
        playerHeader.setText("stopped");
        isPlaying = false;
        mediaPlayer.stop();
        seekbarHandler.removeCallbacks(updateSeekbar);
    }

    private void playAudio(File fileToPlay) {

        mediaPlayer = new MediaPlayer();
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        try {
            mediaPlayer.setDataSource(fileToPlay.getAbsolutePath());
            mediaPlayer.prepare();
            mediaPlayer.start();

        } catch (IOException e) {
            e.printStackTrace();
        }

        playBtn.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.player_pause_btn, null));
        playerFileName.setText(fileToPlay.getName());
        playerHeader.setText("playing");

        //play the audio
        isPlaying = true;

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                stopAudio();
                playerHeader.setText("Finished");
            }
        });


        playerSeekbar.setMax(mediaPlayer.getDuration());

        seekbarHandler = new Handler();
        updateRunnable();
        seekbarHandler.postDelayed(updateSeekbar, 0);
    }

    private void updateRunnable() {
        updateSeekbar = new Runnable() {
            @Override
            public void run() {
                playerSeekbar.setProgress(mediaPlayer.getCurrentPosition());
                seekbarHandler.postDelayed(this, 500);

            }
        };

    }

    @Override
    public void onStop() {
        super.onStop();

        if (isPlaying){
            stopAudio();
        }
    }
}
