package nl.miraclethings.videoplayback;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.widget.Toast;

import com.squareup.okhttp.OkHttpClient;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class VideoPlaybackDispatcher {
    private final Activity activity;

    public VideoPlaybackDispatcher(Activity mainActivity) {
        this.activity = mainActivity;
    }

    interface VideoDownloadCallback {
        void onDownloadResult(String url, Exception error);
    }

    public void playVideo(final String url) {
        ensureDownloaded(url, new VideoDownloadCallback() {
            @Override
            public void onDownloadResult(String url, Exception error) {
                if (error == null) {
                    openVideoPlayer(url);
                }
            }
        });
    }

    public void ensureDownloaded(final String url, final VideoDownloadCallback callback) {

        final File cacheFile = getCacheFile(url);
        if (cacheFile.exists()) {
            callback.onDownloadResult(url, null);
        } else {

            new VideoDownloadTask(url, cacheFile){

                public ProgressDialog progressDialog;

                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                    progressDialog = new ProgressDialog(activity);
                    progressDialog.setTitle("Please wait...");
                    progressDialog.setMessage("Downloading the video");
                    progressDialog.setIndeterminate(false);
                    progressDialog.setCancelable(false);
                    progressDialog.setProgressNumberFormat(null);
                    progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                    progressDialog.show();
                }

                @Override
                protected void onProgressUpdate(Long... values) {
                    progressDialog.setMax(values[1].intValue());
                    progressDialog.setProgress(values[0].intValue());
                }

                @Override
                protected void onPostExecute(Exception e) {
                    progressDialog.dismiss();
                    callback.onDownloadResult(url, e);

                    if (e != null) {
                        e.printStackTrace();
                        new AlertDialog.Builder(activity)
                                .setTitle("Download failed")
                                .setMessage("An error occurred while downloading the video. Please make sure that you are connected to the internet.")
                                .setPositiveButton("Close", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                })
                                .show();
                    }
                }
            }.execute();
        }
    }

    private void openVideoPlayer(String url) {
        File f = getCacheFile(url);
        System.out.println("GO! " + f.getAbsolutePath());

        Intent intent = new Intent(activity, VideoPlaybackActivity.class);
        intent.setData(Uri.fromFile(f));
        activity.startActivity(intent);
    }

    private File getCacheFile(String url) {

        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }

        byte[] hashBytes = digest.digest(url.getBytes());
        String hash = convertByteArrayToHexString(hashBytes);

        String basename = String.format("vid_%s.mp4", hash);
        File dir = new File(activity.getCacheDir(), "mp4");
        dir.mkdirs();
        return new File(dir, basename);
    }

    private static String convertByteArrayToHexString(byte[] arrayBytes) {
        StringBuilder stringBuffer = new StringBuilder();
        for (byte arrayByte : arrayBytes) {
            stringBuffer.append(Integer.toString((arrayByte & 0xff) + 0x100, 16)
                    .substring(1));
        }
        return stringBuffer.toString();
    }

    private abstract class VideoDownloadTask extends AsyncTask<Void, Long, Exception> {
        private final File targetFile;
        private final String url;

        public VideoDownloadTask(String url, File targetFile) {
            this.url = url;
            this.targetFile = targetFile;
        }

        @Override
        protected Exception doInBackground(Void... params) {
            int tries = 20;

            while (tries-- > 0) {

                InputStream inputStream = null;
                BufferedOutputStream outputStream = null;

                System.out.println("Start download: " + this.url);

                OkHttpClient client = new OkHttpClient();

                try {

                    // Create request for remote resource.
                    HttpURLConnection connection = client.open(new URL(this.url));
                    inputStream = connection.getInputStream();

                    byte[] buff = new byte[1024 * 128];
                    long downloaded = 0;
                    long target = connection.getContentLength();

                    publishProgress(0L, target);
                    while (true) {
                        int readed = inputStream.read(buff);
                        if (readed == -1) {
                            break;
                        }

                        if (outputStream == null) {
                            outputStream = new BufferedOutputStream(new FileOutputStream(this.targetFile));
                        }

                        outputStream.write(buff, 0, readed);

                        //write buff
                        downloaded += readed;

                        publishProgress(downloaded, target);

                        if (isCancelled()) {
                            return new Exception("Download cancelled.");
                        }
                    }
                    return null;
                } catch (Exception ignore) {
                    return ignore;
                } finally {
                    try {
                        if (inputStream != null) {
                            inputStream.close();
                        }
                        if (outputStream != null) {
                            outputStream.close();
                        }
                    } catch (IOException e1) {
                        return e1;
                    }
                }
            }

            return new Exception("Too many retries");
        }

    }
}
