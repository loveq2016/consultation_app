package com.consultation.app.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageLoader.ImageListener;
import com.android.volley.toolbox.Volley;
import com.consultation.app.R;
import com.consultation.app.exception.ConsultationCallbackException;
import com.consultation.app.listener.ButtonListener;
import com.consultation.app.listener.ConsultationCallbackHandler;
import com.consultation.app.service.OpenApiService;
import com.consultation.app.util.BitmapCache;
import com.consultation.app.util.ClientUtil;
import com.consultation.app.util.CommonUtil;
import com.consultation.app.util.SharePreferencesEditor;

@SuppressLint("HandlerLeak")
public class CaseTestTxtActivity extends Activity implements OnLongClickListener {

    private TextView title_text, back_text;

    private LinearLayout back_layout;

    private LinearLayout rightLayout;

    private Context context;

    private int width;

    private int height;

    private TextView textAdd, tip;

    private Button saveBtn;

    private List<String> pathList=new ArrayList<String>();

    private List<String> idList=new ArrayList<String>();
    
    private List<String> bigPathList=new ArrayList<String>();

    private SharePreferencesEditor editor;

    private String caseId, imageString;

    private RequestQueue mQueue;

    private ImageLoader mImageLoader;

    private boolean isAdd=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_case_add_test_txt_layout);
        context=this;
        editor=new SharePreferencesEditor(context);
        mQueue=Volley.newRequestQueue(context);
        mImageLoader=new ImageLoader(mQueue, new BitmapCache());
        WindowManager wm=this.getWindowManager();
        width=wm.getDefaultDisplay().getWidth();
        height=wm.getDefaultDisplay().getHeight();
        caseId=getIntent().getStringExtra("caseId");
        imageString=getIntent().getStringExtra("imageString");
        initData();
        initView();
    }

    private void initView() {
        title_text=(TextView)findViewById(R.id.header_text);
        title_text.setText("检验");
        title_text.setTextSize(20);

        back_layout=(LinearLayout)findViewById(R.id.header_layout_lift);
        back_layout.setVisibility(View.VISIBLE);
        back_text=(TextView)findViewById(R.id.header_text_lift);
        back_text.setTextSize(18);

        back_layout.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {
                Intent intent=new Intent();
                Bundle bundle=new Bundle();
                bundle.putBoolean("isAdd", isAdd);
                intent.putExtras(bundle);
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        });
        rightLayout=(LinearLayout)findViewById(R.id.test_txt_layout);

        textAdd=(TextView)findViewById(R.id.test_txt_add_image_text);
        textAdd.setText(Html.fromHtml("<u>" + "添加" + "</u>"));
        textAdd.setGravity(Gravity.CENTER_VERTICAL);
        textAdd.setTextColor(Color.BLUE);
        textAdd.setTextSize(17);
        textAdd.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(CaseTestTxtActivity.this, AddPatientPicActivity.class), 0);
            }
        });

        tip=(TextView)findViewById(R.id.test_txt_image_tip);
        tip.setTextSize(14);

        saveBtn=(Button)findViewById(R.id.test_txt_image_btn_save);
        saveBtn.setTextSize(17);
        saveBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if(pathList.size() == 0) {
                    Toast.makeText(context, "请添加图片", Toast.LENGTH_LONG).show();
                    return;
                }
                List<String> temp=new ArrayList<String>();
                for(int i=0; i < pathList.size(); i++) {
                    if(!pathList.get(i).startsWith("http:")) {
                        temp.add(pathList.get(i));
                    }
                }
                File[] files=new File[temp.size()];
                for(int i=0; i < temp.size(); i++) {
                    File file=new File(temp.get(i));
                    files[i]=file;
                }
                Map<String, String> params=new HashMap<String, String>();
                params.put("case_id", caseId);
                params.put("accessToken", ClientUtil.getToken());
                params.put("uid", editor.get("uid", ""));
                CommonUtil.showLoadingDialog(context);
                OpenApiService.getInstance(context).getUploadFiles(ClientUtil.GET_UPLOAD_IMAGES_URL, context,
                    new ConsultationCallbackHandler() {

                        @Override
                        public void onSuccess(String rspContent, int statusCode) {
                            CommonUtil.closeLodingDialog();
                            Toast.makeText(context, "上传成功", Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onFailure(ConsultationCallbackException exp) {
                            CommonUtil.closeLodingDialog();
                            Toast.makeText(context, "上传失败", Toast.LENGTH_LONG).show();
                        }
                    }, files, params);
            }
        });
        saveBtn.setOnTouchListener(new ButtonListener().setImage(getResources().getDrawable(R.drawable.login_register_btn_shape),
            getResources().getDrawable(R.drawable.login_register_press_btn_shape)).getBtnTouchListener());
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            Intent intent=new Intent();
            Bundle bundle=new Bundle();
            bundle.putBoolean("isAdd", isAdd);
            intent.putExtras(bundle);
            setResult(Activity.RESULT_OK, intent);
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void showRightLayout() {
        rightLayout.removeAllViews();
        if(null != pathList && pathList.size() != 0) {
            LinearLayout rowsLayout=new LinearLayout(context);
            LinearLayout relativeLayout=new LinearLayout(context);
            for(int i=0; i < pathList.size(); i++) {
                if(i % 3 == 0) {
                    rowsLayout=createLinearLayout();
                    rightLayout.addView(rowsLayout);
                }
                relativeLayout=createImage(pathList.get(i), i, bigPathList.get(i));
                rowsLayout.addView(relativeLayout);
            }
        }
    }

    private LinearLayout createLinearLayout() {
        LinearLayout linearLayout=new LinearLayout(context);
        LayoutParams layoutParams=new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        layoutParams.gravity=Gravity.CENTER_VERTICAL;
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        linearLayout.setPadding(0, height / 100, 0, height / 100);
        linearLayout.setBackgroundColor(Color.WHITE);
        linearLayout.setLayoutParams(layoutParams);
        return linearLayout;
    }

    private LinearLayout createImage(String path, int id, final String bigPath) {
        LinearLayout relativeLayout=new LinearLayout(context);
        LayoutParams layoutParams=new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        layoutParams.gravity=Gravity.CENTER;
        layoutParams.leftMargin=width / 30;
        layoutParams.rightMargin=width / 30;
        relativeLayout.setLayoutParams(layoutParams);
        ImageView imageView=new ImageView(context);
        imageView.setId(id);
        imageView.setOnLongClickListener(this);
        imageView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // 展示大图片
                BigImageActivity.setViewData(bigPath);
                startActivity(new Intent(CaseTestTxtActivity.this, BigImageActivity.class));
            }
        });
        LayoutParams imageViewParams=new LayoutParams(width / 15 * 4, width / 15 * 4);
        imageView.setLayoutParams(imageViewParams);
        imageView.setScaleType(ScaleType.CENTER_CROP);
        if(!"null".equals(path) && !"".equals(path)) {
            if(path.startsWith("http:")) {
                imageView.setTag(path);
                imageView.setImageResource(R.anim.loading_anim_test);
                ImageListener listener=ImageLoader.getImageListener(imageView, 0, android.R.drawable.ic_menu_delete);
                mImageLoader.get(path, listener, 200, 200);
            } else {
                Bitmap bitmap=CommonUtil.readBitMap(200, path);
                imageView.setImageBitmap(bitmap);
            }
        }
        relativeLayout.addView(imageView);
        return relativeLayout;
    }

    private void initData() {
        if(null != imageString && !"".equals(imageString)) {
            try {
                JSONArray jsonArray=new JSONArray(imageString);
                for(int i=0; i < jsonArray.length(); i++) {
                    JSONObject imageFilesObject=jsonArray.getJSONObject(i);
                    pathList.add(imageFilesObject.getString("little_pic_url"));
                    idList.add(imageFilesObject.getString("id"));
                    bigPathList.add(imageFilesObject.getString("pic_url"));
                }
                showRightLayout();
            } catch(JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        switch(requestCode) {
            case 0:
                if(data != null) {
                    String photoPath=data.getStringExtra("bitmap");
                    if(!pathList.contains(photoPath)) {
                        pathList.add(photoPath);
                        bigPathList.add(photoPath);
                    }
                    showRightLayout();
                }
                break;
            case 1:
                if(resultCode == Activity.RESULT_OK) {
                    Map<String, String> parmas=new HashMap<String, String>();
                    parmas.put("id", idList.get(data.getIntExtra("index", 0)));
                    parmas.put("accessToken", ClientUtil.getToken());
                    parmas.put("uid", editor.get("uid", ""));
                    CommonUtil.showLoadingDialog(CaseTestTxtActivity.this);
                    OpenApiService.getInstance(CaseTestTxtActivity.this).getDeleteCaseImage(mQueue, parmas,
                        new Response.Listener<String>() {

                            @Override
                            public void onResponse(String arg0) {
                                CommonUtil.closeLodingDialog();
                                try {
                                    JSONObject responses=new JSONObject(arg0);
                                    if(responses.getInt("rtnCode") == 1) {
                                        pathList.remove(data.getIntExtra("index", 0));
                                        idList.remove(data.getIntExtra("index", 0));
                                        showRightLayout();
                                    } else if(responses.getInt("rtnCode") == 10004) {
                                        Toast.makeText(CaseTestTxtActivity.this, responses.getString("rtnMsg"), Toast.LENGTH_SHORT)
                                            .show();
                                        LoginActivity.setHandler(new ConsultationCallbackHandler() {

                                            @Override
                                            public void onSuccess(String rspContent, int statusCode) {
                                            }

                                            @Override
                                            public void onFailure(ConsultationCallbackException exp) {
                                            }
                                        });
                                        startActivity(new Intent(CaseTestTxtActivity.this, LoginActivity.class));
                                    } else {
                                        Toast.makeText(CaseTestTxtActivity.this, responses.getString("rtnMsg"), Toast.LENGTH_SHORT)
                                            .show();
                                    }
                                } catch(JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }, new Response.ErrorListener() {

                            @Override
                            public void onErrorResponse(VolleyError arg0) {
                                CommonUtil.closeLodingDialog();
                                Toast.makeText(CaseTestTxtActivity.this, "网络连接失败,请稍后重试", Toast.LENGTH_SHORT).show();
                            }
                        });
                }
                break;
            default:
                break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onLongClick(View v) {
        Intent intent=new Intent(CaseTestTxtActivity.this, DialogNewActivity.class);
        intent.putExtra("flag", 0);
        intent.putExtra("index", v.getId());
        intent.putExtra("titleText", "删除该图片?");
        startActivityForResult(intent, 1);
        return false;
    }
}
