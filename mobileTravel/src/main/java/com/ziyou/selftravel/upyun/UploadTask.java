package com.ziyou.selftravel.upyun;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

//import com.lidroid.xutils.HttpUtils;
//import com.lidroid.xutils.exception.HttpException;
//import com.lidroid.xutils.http.RequestParams;
//import com.lidroid.xutils.http.ResponseInfo;
//import com.lidroid.xutils.http.callback.RequestCallBack;
//import com.lidroid.xutils.http.client.HttpRequest;
//import com.lidroid.xutils.http.client.multipart.HttpMultipartMode;
//import com.lidroid.xutils.http.client.multipart.MultipartEntity;
import com.lidroid.xutils.http.client.multipart.HttpMultipartMode;
import com.lidroid.xutils.http.client.multipart.content.ByteArrayBody;
import com.lidroid.xutils.http.client.multipart.content.StringBody;
import com.ziyou.selftravel.app.ServerAPI;
import com.ziyou.selftravel.model.QHToken;
import com.ziyou.selftravel.util.AppUtils;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.impl.client.DefaultHttpClient;


public class UploadTask extends AsyncTask<Object, String, String> {
	public interface UploadCallBack {
		public void onSuccess(String result);
		public void onFailed();
	};
	private UploadCallBack mCallBack;
	private Context mContext;
	private File file;
    List<File> imgFiles;
    int object_id;
    int ctype;
    int gender;
    int type;//0 评论，1更新用户信息 2，发布攻略。
    String nick_name;//
    String url;//
    String content;

	public UploadTask(Context mContext, List<File> imgFiles, String content,int object_id,int ctype,UploadCallBack mCallBack) {
		this.mContext = mContext;
		this.mCallBack = mCallBack;
        this.imgFiles = imgFiles;
        this.content = content;
        this.object_id = object_id;
        this.ctype = ctype;
        this.type = type;
	}
    public UploadTask(Context mContext, String url,int type ,List<File> imgFiles, String nick_name,int gender,UploadCallBack mCallBack) {
		this.mContext = mContext;
		this.mCallBack = mCallBack;
        this.imgFiles = imgFiles;
        this.content = content;
        this.object_id = object_id;
        this.ctype = ctype;
        this.type = type;
        this.url = url;
        this.nick_name = nick_name;
        this.gender = gender;
	}
	@Override
	protected String doInBackground(Object... params) {
		String string = null;
		publishProgress("图片上传中...");
		try {
            if(type ==0)
                string = Uploader.upload( mContext,imgFiles,content,object_id,ctype);
            else if(type ==1)
            {
//                String url = ServerAPI.User.buildUpdateUserInfoUrl();
                string = UserInfoUploader.upload(mContext,url,imgFiles,nick_name,gender);
            }
        }
        catch (Exception e) {
			e.printStackTrace();
		}
		return string;
	}

	@Override
	protected void onPostExecute(String result) {
		super.onPostExecute(result);
		publishProgress();
		if( null!=imgFiles){
            for(File file:imgFiles){
                if(file!=null && file.exists())
                file.delete();
            }
		}
		if (result != null) {
			mCallBack.onSuccess(result);
		} else {
			mCallBack.onFailed();
		}
	}

	@Override
	protected void onProgressUpdate(String... values) {
		super.onProgressUpdate(values);
		/*if (mPrgDlg == null) {
			mPrgDlg = new CustomProgressDialogForXF(mContext, "图片上传中...");
		}
		if (values == null || (values != null && values.length == 0)) {
			// 取消进度条显示
			mPrgDlg.dismiss();
		} else {
			mPrgDlg.show();
		}*/
	}

	@Override
	protected void onCancelled() {
		/*if (mPrgDlg != null) {
			mPrgDlg.dismiss();
		}*/
		if( file!=null && file.exists()){
			file.delete();
		}
	}

}
