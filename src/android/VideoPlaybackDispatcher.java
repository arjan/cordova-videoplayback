package nl.miraclethings.videoplayback;

import android.app.Activity;
import android.app.ProgressDialog;
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

    public void playVideo(String url) {
        final File cacheFile = getCacheFile(url);
        if (cacheFile.exists()) {
            openVideoPlayer(cacheFile);
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
                    if (e == null) {
                        openVideoPlayer(cacheFile);
                    } else {
                        e.printStackTrace();
                        Toast.makeText(activity, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }.execute();
        }
    }

    private void openVideoPlayer(File f) {
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

                    byte[] buff = new byte[1024 * 4];
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
