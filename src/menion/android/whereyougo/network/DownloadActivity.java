package menion.android.whereyougo.network;

import java.io.*;
import java.net.*;
import java.util.ArrayList;

import org.apache.http.*;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import cz.matejcik.openwig.formats.CartridgeFile;
import locus.api.objects.extra.Location;
import locus.api.objects.extra.Waypoint;
import menion.android.whereyougo.R;
import menion.android.whereyougo.gui.activity.MainActivity;
import menion.android.whereyougo.gui.extension.activity.CustomActivity;
import menion.android.whereyougo.gui.utils.UtilsGUI;
import menion.android.whereyougo.openwig.WSaveFile;
import menion.android.whereyougo.openwig.WSeekableFile;
import menion.android.whereyougo.openwig.WUI;
import menion.android.whereyougo.preferences.PreferenceValues;
import menion.android.whereyougo.preferences.Preferences;
import menion.android.whereyougo.utils.FileSystem;
import menion.android.whereyougo.utils.Logger;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.Uri;
import android.opengl.Visibility;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.*;

public class DownloadActivity extends CustomActivity {
  private static final String TAG = "DownloadActivity";
  TextView urlTextView;
  Button buttonDownload;
  // Button buttonStart;
  DownloadTask downloadTask;
  String cguid;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Uri uri = getIntent().getData();
    try {
      cguid = uri.getQueryParameter("CGUID");
    } catch (Exception e) {
    }
    if (cguid == null) {
      finish();
      return;
    }

    setContentView(R.layout.activity_download);
    urlTextView = (TextView) findViewById(R.id.urlTextView);
    urlTextView.setText(cguid);
    File file = null;
    if ((file = findFile(cguid)) != null) {
      urlTextView.setText(urlTextView.getText() + "\n\n" + file.getName().replace(cguid + "_", "")
          + " already downloaded");
    }
    buttonDownload = (Button) findViewById(R.id.downloadButton);
    buttonDownload.setOnClickListener(new View.OnClickListener() {

      @Override
      public void onClick(View arg0) {
        if (downloadTask != null && downloadTask.getStatus() != Status.FINISHED) {
          downloadTask.cancel(true);
          downloadTask = null;
        } else {
          downloadTask = new DownloadTask(DownloadActivity.this);
          downloadTask.execute(cguid);
        }
      }
    });
    // buttonStart = (Button) findViewById(R.id.startButton);
    // buttonStart.setOnClickListener(new View.OnClickListener() {
    //
    // @Override
    // public void onClick(View arg0) {
    // File file = null;
    // if((file = findFile(cguid)) != null){
    // startCartridge(file);
    // }
    // }
    // });
    // if(findFile(cguid) == null){
    // buttonStart.setEnabled(false);
    // }
  }

  enum Task {
    PING, LOGIN, DOWNLOAD, DOWNLOAD_SINGLE, LOGOUT
  };
  enum State {
    WORKING, SUCCESS, FAIL
  };
  class DownloadTask extends AsyncTask<String, DownloadTask.Progress, Boolean> {

    static final String LOGIN = "https://www.wherigo.com/login/default.aspx";
    static final String DOWNLOAD = "http://www.wherigo.com/cartridge/download.aspx";
    ProgressDialog progressDialog;

    class Progress {
      public Progress(Task task, State state) {
        this.task = task;
        this.state = state;
      }

      public Progress(Task task, long completed, long total) {
        this.state = State.WORKING;
        this.task = task;
        this.total = total;
        this.completed = completed;
      }

      Task task;
      State state;
      long total;
      long completed;
    }

    public DownloadTask(final Context context) {
      progressDialog = new ProgressDialog(context);
      progressDialog.setMessage("");
      progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
      progressDialog.setIndeterminate(true);
      progressDialog.setCanceledOnTouchOutside(false);

      progressDialog.setCancelable(true);
      progressDialog.setOnCancelListener(new ProgressDialog.OnCancelListener() {

        @Override
        public void onCancel(DialogInterface arg0) {
          if (downloadTask != null && downloadTask.getStatus() != Status.FINISHED) {
            downloadTask.cancel(false);
            downloadTask = null;
            Log.i("down", "cancel");
            Toast.makeText(context, "cancelled", Toast.LENGTH_LONG).show();
          }
        }
      });
    }

    @Override
    protected void onPreExecute() {
      super.onPreExecute();
      progressDialog.show();
    }

    @Override
    protected Boolean doInBackground(String... arg0) {

      DefaultHttpClient httpClient = new DefaultHttpClient();
      BasicCookieStore cookieStore = new BasicCookieStore();
      HttpContext localContext = new BasicHttpContext();
      localContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);

      // login
      try {
        HttpGet httpGet = new HttpGet(LOGIN);
        publishProgress(new Progress(Task.PING, State.WORKING));
        if (status(httpClient.execute(httpGet, localContext)) != HttpStatus.SC_OK
            || cookieStore.getCookies().size() == 0) {
          publishProgress(new Progress(Task.PING, State.FAIL));
          return false;
        } else {
          publishProgress(new Progress(Task.PING, State.SUCCESS));
        }
        if (isCancelled())
          return false;

        String username =
            PreferenceValues.getPrefString(DownloadActivity.this,
                PreferenceValues.KEY_S_GC_USERNAME, PreferenceValues.DEFAULT_GC_USERNAME);
        String password =
            PreferenceValues.getPrefString(DownloadActivity.this,
                PreferenceValues.KEY_S_GC_PASSWORD, PreferenceValues.DEFAULT_GC_PASSWORD);
        HttpPost httpPost = new HttpPost(LOGIN);
        ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
        postParameters.add(new BasicNameValuePair("__EVENTTARGET", ""));
        postParameters.add(new BasicNameValuePair("__EVENTARGUMENT", ""));
        postParameters.add(new BasicNameValuePair(
            "ctl00$ContentPlaceHolder1$Login1$Login1$UserName", username));
        postParameters.add(new BasicNameValuePair(
            "ctl00$ContentPlaceHolder1$Login1$Login1$Password", password));
        postParameters.add(new BasicNameValuePair(
            "ctl00$ContentPlaceHolder1$Login1$Login1$LoginButton", "Sign In"));
        httpPost.setEntity(new UrlEncodedFormEntity(postParameters, "UTF-8"));
        publishProgress(new Progress(Task.LOGIN, State.WORKING));
        if (status(httpClient.execute(httpPost, localContext)) != HttpStatus.SC_OK
            || cookieStore.getCookies().size() <= 1) {
          publishProgress(new Progress(Task.LOGIN, State.FAIL));
          return false;
        } else {
          publishProgress(new Progress(Task.LOGIN, State.SUCCESS));
        }
        if (isCancelled())
          return false;
      } catch (Exception e) {
        publishProgress(new Progress(Task.PING, State.FAIL));
        return false;
      }

      // download
      for (int i = 0; i < arg0.length; i++) {
        String cguid = arg0[i];
        try {
          HttpPost httpPost = new HttpPost(DOWNLOAD + "?CGUID=" + cguid);
          ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
          postParameters.add(new BasicNameValuePair("__EVENTTARGET", ""));
          postParameters.add(new BasicNameValuePair("__EVENTARGUMENT", ""));
          postParameters.add(new BasicNameValuePair(
              "ctl00$ContentPlaceHolder1$EULAControl1$uxEulaAgree", "on"));
          postParameters.add(new BasicNameValuePair("ctl00$ContentPlaceHolder1$uxDeviceList", "4"));
          postParameters.add(new BasicNameValuePair("ctl00$ContentPlaceHolder1$btnDownload",
              "Download Now"));
          httpPost.setEntity(new UrlEncodedFormEntity(postParameters));
          publishProgress(new Progress(Task.DOWNLOAD, i, arg0.length));
          if (download(cguid, httpClient.execute(httpPost, localContext)) != HttpStatus.SC_OK) {
            publishProgress(new Progress(Task.DOWNLOAD, State.FAIL));
          } else {
            publishProgress(new Progress(Task.DOWNLOAD, State.SUCCESS));
          }
          if (isCancelled())
            return false;
        } catch (Exception e) {
          publishProgress(new Progress(Task.DOWNLOAD, State.FAIL));
          return false;
        }
      }

      // logout
      try {
        HttpPost httpPost = new HttpPost(LOGIN);
        ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
        postParameters.add(new BasicNameValuePair("__EVENTTARGET",
            "ctl00$ProfileWidget$LoginStatus1$ctl00"));
        postParameters.add(new BasicNameValuePair("__EVENTARGUMENT", ""));
        httpPost.setEntity(new UrlEncodedFormEntity(postParameters));
        publishProgress(new Progress(Task.LOGOUT, State.WORKING));
        if (status(httpClient.execute(httpPost, localContext)) != HttpStatus.SC_OK
            || cookieStore.getCookies().size() > 1) {
          publishProgress(new Progress(Task.LOGOUT, State.FAIL));
          return false;
        }
        publishProgress(new Progress(Task.LOGOUT, State.SUCCESS));
      } catch (Exception e) {
        publishProgress(new Progress(Task.LOGOUT, State.FAIL));
        return false;
      }
      return true;
    }

    @Override
    protected void onPostExecute(final Boolean result) {
      super.onPostExecute(result);
      if (result) {
        progressDialog.dismiss();
        MainActivity.refreshCartridges();
        // buttonStart.setEnabled(true);
      } else {
        progressDialog.setIndeterminate(false);
      }
      downloadTask = null;
    }

    @Override
    protected void onProgressUpdate(Progress... values) {
      super.onProgressUpdate(values);
      Progress progress = values[0];
      switch (progress.task) {
        case PING:
          progressDialog.setIndeterminate(true);
          if (progress.state == State.WORKING) {
            progressDialog.setMessage("connecting");
          } else if (progress.state == State.SUCCESS) {
            progressDialog.setMessage("connecting succesful");
          } else if (progress.state == State.FAIL) {
            progressDialog.setMessage("connecting failed");
          }
          break;
        case LOGIN:
          progressDialog.setIndeterminate(true);
          if (progress.state == State.WORKING) {
            progressDialog.setMessage("logging in");
          } else if (progress.state == State.SUCCESS) {
            progressDialog.setMessage("logging in succesful");
          } else if (progress.state == State.FAIL) {
            progressDialog.setMessage("logging in failed");
          }
          break;
        case LOGOUT:
          progressDialog.setIndeterminate(true);
          if (progress.state == State.WORKING) {
            progressDialog.setMessage("logging out");
          } else if (progress.state == State.SUCCESS) {
            progressDialog.setMessage("logging out succesful");
          } else if (progress.state == State.FAIL) {
            progressDialog.setMessage("logging out failed");
          }
          break;
        case DOWNLOAD:
          progressDialog.setIndeterminate(true);
          if (progress.state == State.WORKING) {
            progressDialog.setMessage("downloading");
          } else if (progress.state == State.SUCCESS) {
            progressDialog.setMessage("downloading succesful");
          } else if (progress.state == State.FAIL) {
            progressDialog.setMessage("downloading failed");
          }
          break;
        case DOWNLOAD_SINGLE:
          progressDialog.setIndeterminate(false);
          progressDialog.setMax((int) progress.total);
          progressDialog.setProgress((int) progress.completed);
          if (progress.state == State.WORKING) {
            progressDialog.setMessage("downloading");
          } else if (progress.state == State.SUCCESS) {
            progressDialog.setMessage("downloading succesful");
          } else if (progress.state == State.FAIL) {
            progressDialog.setMessage("downloading failed");
          }
          break;
      }
    }

    private int status(HttpResponse response) throws IllegalStateException, IOException {
      StatusLine statusLine = response.getStatusLine();
      int statusCode = statusLine.getStatusCode();
      if (statusCode == HttpStatus.SC_OK) {
        HttpEntity entity = response.getEntity();
        if (entity != null) {
          entity.consumeContent();
          return statusCode;
        }
      }
      return -1;
    }

    private int download(String filename, HttpResponse response) throws IllegalStateException,
        IOException {
      StatusLine statusLine = response.getStatusLine();
      int statusCode = statusLine.getStatusCode();
      if (statusCode == HttpStatus.SC_OK) {
        HttpEntity entity = response.getEntity();
        if (entity != null) {
          if (!"application/octet-stream".equals(entity.getContentType().getValue())) {
            entity.consumeContent();
            return -1;
          }
          long total = entity.getContentLength();
          String suggestedFilename =
              suggestedFilename(response.getFirstHeader("Content-Disposition"));
          String filePath =
              FileSystem.ROOT
                  + (suggestedFilename == null ? filename + ".gwc" : filename + "_"
                      + suggestedFilename);
          File file = new File(filePath);
          if (file.exists() && file.length() == total) {
            entity.consumeContent();
            publishProgress(new Progress(Task.DOWNLOAD_SINGLE, total, total));
            return HttpStatus.SC_OK;
          }
          long completed = 0;
          int length;
          byte[] buffer = new byte[1024];
          BufferedInputStream bis = new BufferedInputStream(entity.getContent());
          BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
          publishProgress(new Progress(Task.DOWNLOAD_SINGLE, completed, total));
          while ((length = bis.read(buffer)) > 0 && !isCancelled()) {
            bos.write(buffer, 0, length);
            completed += length;
            publishProgress(new Progress(Task.DOWNLOAD_SINGLE, completed, total));
          }
          bis.close();
          bos.close();
          entity.consumeContent();
          if (isCancelled()) {
            file.delete();
            return -1;
          }
          return statusCode;
        }
      }
      return -1;
    }

    private String suggestedFilename(Header header) {
      if (header != null) {
        HeaderElement[] helelms = header.getElements();
        if (helelms.length > 0) {
          HeaderElement helem = helelms[0];
          if (helem.getName().equalsIgnoreCase("attachment")) {
            NameValuePair nmv = helem.getParameterByName("filename");
            if (nmv != null) {
              return nmv.getValue();
            }
          }
        }
      }
      return null;
    }

  }

  @Override
  public void onDestroy() {
    super.onDestroy();

    if (downloadTask != null && downloadTask.getStatus() != Status.FINISHED) {
      downloadTask.cancel(true);
      downloadTask = null;
    }
  }

  private File findFile(String cguid) {
    File[] files = FileSystem.getFiles(FileSystem.ROOT, "gwc");
    for (File file : files) {
      // check whether cartridge is already present
      if (file.getName().startsWith(cguid)) {
        return file;
      }
    }
    return null;
  }

  // private void startCartridge(File file) {
  // try {
  // CartridgeFile cart = null;
  // try {
  // cart = CartridgeFile.read(new WSeekableFile(file), new WSaveFile(file));
  // if (cart != null) {
  // cart.filename = file.getAbsolutePath();
  // } else {
  // return;
  // }
  // } catch (Exception e) {
  // file.delete();
  // }
  // MainActivity.cartridgeFile = cart;
  // MainActivity.selectedFile = MainActivity.cartridgeFile.filename;
  //
  // if (MainActivity.cartridgeFile.getSavegame().exists()) {
  // UtilsGUI.showDialogQuestion(this, R.string.resume_previous_cartridge,
  // new DialogInterface.OnClickListener() {
  //
  // @Override
  // public void onClick(DialogInterface dialog, int btn) {
  // File file =
  // new File(MainActivity.getSelectedFile().substring(0,
  // MainActivity.getSelectedFile().length() - 3)
  // + "gwl");
  // FileOutputStream fos = null;
  // try {
  // if (!file.exists())
  // file.createNewFile();
  // fos = new FileOutputStream(file, true);
  // } catch (Exception e) {
  // Logger.e(TAG, "onResume() - create empty saveGame file", e);
  // }
  // MainActivity.restoreCartridge(fos);
  // }
  // }, new DialogInterface.OnClickListener() {
  //
  // @Override
  // public void onClick(DialogInterface dialog, int btn) {
  // MainActivity.wui.showScreen(WUI.SCREEN_CART_DETAIL, null);
  // try {
  // MainActivity.getSaveFile().delete();
  // } catch (Exception e) {
  // Logger.e(TAG, "onCreate() - deleteSyncFile", e);
  // }
  // }
  // }, null);
  // } else {
  // MainActivity.wui.showScreen(WUI.SCREEN_CART_DETAIL, null);
  // }
  // } catch (Exception e) {
  // Logger.e(TAG, "onCreate()", e);
  // }
  // }

}
