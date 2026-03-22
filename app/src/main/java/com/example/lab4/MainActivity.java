package com.example.lab4;

import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private Button btnChooseFile, btnStart, btnPause, btnStop;
    private VideoView videoView;
    private ImageView imageView;
    private TextView tvTitle, tvArtist, tvAlbum, tvYear;
    private MediaPlayer mediaPlayer;
    private Uri currentUri;
    private boolean isVideo = false;
    private static final int PICK_FILE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnChooseFile = findViewById(R.id.btnChooseFile);
        btnStart = findViewById(R.id.btnStart);
        btnPause = findViewById(R.id.btnPause);
        btnStop = findViewById(R.id.btnStop);

        videoView = findViewById(R.id.videoView);
        imageView = findViewById(R.id.imageView);
        tvTitle = findViewById(R.id.tvTitle);
        tvArtist = findViewById(R.id.tvArtist);
        tvAlbum = findViewById(R.id.tvAlbum);
        tvYear = findViewById(R.id.tvYear);

        btnChooseFile.setOnClickListener(v -> chooseFile());
        btnStart.setOnClickListener(v -> startMedia());
        btnPause.setOnClickListener(v -> pauseMedia());
        btnStop.setOnClickListener(v -> stopMedia());
    }

    private void chooseFile() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"audio/*", "video/*"});
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivityForResult(intent, PICK_FILE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_FILE && resultCode == RESULT_OK && data != null) {
            currentUri = data.getData();
            if (currentUri == null) return;

            String type = getContentResolver().getType(currentUri);
            if (type != null && type.startsWith("video")) {
                isVideo = true;
                imageView.setVisibility(ImageView.GONE);
                videoView.setVisibility(VideoView.VISIBLE);
                videoView.setVideoURI(currentUri);

                tvTitle.setText("");
                tvArtist.setText("");
                tvAlbum.setText("");
                tvYear.setText("");

            } else {
                isVideo = false;
                videoView.setVisibility(VideoView.GONE);
                imageView.setVisibility(ImageView.VISIBLE);

                try {
                    if (mediaPlayer != null) {
                        mediaPlayer.release();
                        mediaPlayer = null;
                    }
                    mediaPlayer = new MediaPlayer();
                    mediaPlayer.setDataSource(this, currentUri);
                    mediaPlayer.prepare();

                    MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                    mmr.setDataSource(this, currentUri);

                    String title = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
                    String artist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
                    String album = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
                    String year = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_YEAR);
                    byte[] artBytes = mmr.getEmbeddedPicture();

                    tvTitle.setText(title != null ? title : "Невідомо");
                    tvArtist.setText(artist != null ? artist : "Невідомо");
                    tvAlbum.setText(album != null ? album : "Невідомо");
                    tvYear.setText(year != null ? year : "Невідомо");

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void startMedia() {
        if (isVideo) {
            if (videoView != null) videoView.start();
        } else {
            if (mediaPlayer != null) mediaPlayer.start();
        }
    }

    private void pauseMedia() {
        if (isVideo) {
            if (videoView != null && videoView.isPlaying()) videoView.pause();
        } else {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) mediaPlayer.pause();
        }
    }

    private void stopMedia() {
        if (isVideo) {
            if (videoView != null) videoView.stopPlayback();
        } else {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                try {
                    mediaPlayer.prepare();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}