package com.consultation.app.fragment;

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
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageLoader.ImageListener;
import com.android.volley.toolbox.Volley;
import com.consultation.app.R;
import com.consultation.app.activity.DepartmentActivity;
import com.consultation.app.activity.HospitalActivity;
import com.consultation.app.activity.SearchSpecialistActivity;
import com.consultation.app.activity.SpecialistInfoActivity;
import com.consultation.app.activity.TitleActivity;
import com.consultation.app.exception.ConsultationCallbackException;
import com.consultation.app.listener.ConsultationCallbackHandler;
import com.consultation.app.model.SpecialistTo;
import com.consultation.app.model.UserStatisticsTo;
import com.consultation.app.model.UserTo;
import com.consultation.app.service.OpenApiService;
import com.consultation.app.util.BitmapCache;
import com.consultation.app.util.CommonUtil;
import com.consultation.app.view.PullToRefreshLayout;
import com.consultation.app.view.PullToRefreshLayout.OnRefreshListener;
import com.consultation.app.view.PullableListView;
import com.consultation.app.view.PullableListView.OnLoadListener;

@SuppressLint("HandlerLeak")
public class SpecialistFragment extends Fragment implements OnLoadListener {

    private View specialistLayout;

    private TextView header_text, hospital_text, department_text, title_text;

    private LinearLayout hospital_layout, department_layout, title_layout;

    private PullableListView specialistListView;

    private List<SpecialistTo> specialistList=new ArrayList<SpecialistTo>();

    private MyAdapter myAdapter;

    private ViewHolder holder;

    private RequestQueue mQueue;

    private ImageLoader mImageLoader;

    private static Context mContext;

    private int page=1;

    private boolean hasMore=true;

    private ImageView searchBtn;

    private String hospital_id=null;

    private String department_id=null;

    private String title_id=null;

    private Handler handler=new Handler() {

        public void handleMessage(Message msg) {
            switch(msg.what) {
                case 0:
                    myAdapter.notifyDataSetChanged();
                    page=1;
                    ((PullToRefreshLayout)msg.obj).refreshFinish(PullToRefreshLayout.SUCCEED);
                    break;
                case 1:
                    if(hasMore) {
                        ((PullableListView)msg.obj).finishLoading();
                    } else {
                        specialistListView.setHasMoreData(false);
                    }
                    myAdapter.notifyDataSetChanged();
                    break;
                case 2:
                    specialistListView.setHasMoreData(true);
                    page=1;
                    ((PullToRefreshLayout)msg.obj).refreshFinish(PullToRefreshLayout.FAIL);
                    break;
                case 3:
                    page=1;
                    myAdapter.notifyDataSetChanged();
                    break;
            }
        };
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        specialistLayout=inflater.inflate(R.layout.specialist_layout, container, false);
        initData();
        initLayout();
        return specialistLayout;
    }

    public static SpecialistFragment getInstance(Context ctx) {
        mContext=ctx;
        return new SpecialistFragment();
    }

    private void initData() {
        mQueue=Volley.newRequestQueue(mContext);
        mImageLoader=new ImageLoader(mQueue, new BitmapCache());
        Map<String, String> parmas=new HashMap<String, String>();
        parmas.put("page", String.valueOf(page));
        parmas.put("rows", "10");
        CommonUtil.showLoadingDialog(mContext);
        OpenApiService.getInstance(mContext).getExpertList(mQueue, parmas, new Response.Listener<String>() {

            @Override
            public void onResponse(String arg0) {
                CommonUtil.closeLodingDialog();
                try {
                    JSONObject responses=new JSONObject(arg0);
                    if(responses.getInt("rtnCode") == 1) {
                        JSONArray infos=responses.getJSONArray("experts");
                        for(int i=0; i < infos.length(); i++) {
                            JSONObject info=infos.getJSONObject(i);
                            SpecialistTo specialistTo=new SpecialistTo();
                            specialistTo.setApprove_status(info.getString("approve_status"));
                            specialistTo.setDepart_name(info.getString("depart_name"));
                            specialistTo.setGoodat_fields(info.getString("goodat_fields"));
                            specialistTo.setHospital_name(info.getString("hospital_name"));
                            specialistTo.setId(info.getString("id"));
                            specialistTo.setTitle(info.getString("title"));
                            JSONObject userToJsonObject=info.getJSONObject("user");
                            UserTo userTo=
                                new UserTo(userToJsonObject.getString("real_name"), userToJsonObject.getString("sex"),
                                    userToJsonObject.getString("birth_year"), userToJsonObject.getString("tp"), userToJsonObject
                                        .getString("icon_url"));
                            JSONObject userStatisticsJsonObject=info.getJSONObject("userTj");
                            specialistTo.setUser(userTo);
                            UserStatisticsTo userStatistics=
                                new UserStatisticsTo(userStatisticsJsonObject.getInt("total_consult"), userStatisticsJsonObject.getInt("star_value"));
                            specialistTo.setUserTj(userStatistics);
                            specialistList.add(specialistTo);
                        }
                        if(infos.length() == 10) {
                            specialistListView.setHasMoreData(true);
                        } else {
                            specialistListView.setHasMoreData(false);
                        }
                    } else {
                        Toast.makeText(mContext, responses.getString("rtnMsg"), Toast.LENGTH_SHORT).show();
                    }
                } catch(JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError arg0) {
                CommonUtil.closeLodingDialog();
                Toast.makeText(mContext, "网络连接失败,请稍后重试", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initLayout() {
        header_text=(TextView)specialistLayout.findViewById(R.id.header_text);
        header_text.setText("专家库");
        header_text.setTextSize(20);

        hospital_text=(TextView)specialistLayout.findViewById(R.id.specialist_select_hospital_text);
        hospital_text.setTextSize(17);
        hospital_layout=(LinearLayout)specialistLayout.findViewById(R.id.specialist_select_hospital_layout);
        hospital_layout.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                HospitalActivity.setHandler(new ConsultationCallbackHandler() {

                    @Override
                    public void onSuccess(String rspContent, int statusCode) {
                        switch(statusCode) {
                            case 0:
                                hospital_text.setText(rspContent.split(",")[0]);
                                Map<String, String> parmas=new HashMap<String, String>();
                                parmas.put("hospital_id", rspContent.split(",")[1]);
                                hospital_id=rspContent.split(",")[1];
                                getData(parmas);
                                break;
                            case 2:
                                hospital_text.setText("选择医院");
                                hospital_id = null;
                                Map<String, String> parmas1=new HashMap<String, String>();
                                getData(parmas1);
                                break;

                            default:
                                break;
                        }
                            
                    }

                    @Override
                    public void onFailure(ConsultationCallbackException exp) {

                    }
                });
                startActivity(new Intent(mContext, HospitalActivity.class));
            }
        });

        department_text=(TextView)specialistLayout.findViewById(R.id.specialist_select_department_text);
        department_text.setTextSize(17);
        department_layout=(LinearLayout)specialistLayout.findViewById(R.id.specialist_select_department_layout);
        department_layout.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                DepartmentActivity.setHandler(new ConsultationCallbackHandler() {

                    @Override
                    public void onSuccess(String rspContent, int statusCode) {
                        switch(statusCode) {
                            case 0:
                                department_text.setText("选择专业");
                                department_id = null;
                                Map<String, String> parmas1=new HashMap<String, String>();
                                if(null != hospital_id) {
                                    parmas1.put("hospital_id", hospital_id);
                                }
                                getData(parmas1);
                                break;
                            case 1:
                                department_text.setText(rspContent.split(",")[0]);
                                Map<String, String> parmas2=new HashMap<String, String>();
                                if(null != hospital_id) {
                                    parmas2.put("hospital_id", hospital_id);
                                }
                                parmas2.put("depart_id", rspContent.split(",")[1]);
                                department_id=rspContent.split(",")[1];
                                getData(parmas2);
                                break;

                            default:
                                break;
                        }
                    }

                    @Override
                    public void onFailure(ConsultationCallbackException exp) {

                    }
                });
                startActivity(new Intent(mContext, DepartmentActivity.class));
            }
        });

        title_text=(TextView)specialistLayout.findViewById(R.id.specialist_select_title_text);
        title_text.setTextSize(17);
        title_layout=(LinearLayout)specialistLayout.findViewById(R.id.specialist_select_title_layout);
        title_layout.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                TitleActivity.setHandler(new ConsultationCallbackHandler() {

                    @Override
                    public void onSuccess(String rspContent, int statusCode) {
                        switch(statusCode) {
                            case 0:
                                title_text.setText("选择职称");
                                title_id = null;
                                Map<String, String> parmas1=new HashMap<String, String>();
                                if(null != hospital_id) {
                                    parmas1.put("hospital_id", hospital_id);
                                }
                                if(null != department_id) {
                                    parmas1.put("depart_id", department_id);
                                }
                                getData(parmas1);
                                break;
                            case 1:
                                title_text.setText(rspContent.split(",")[0]);
                                Map<String, String> parmas2=new HashMap<String, String>();
                                if(null != hospital_id) {
                                    parmas2.put("hospital_id", hospital_id);
                                }
                                if(null != department_id) {
                                    parmas2.put("depart_id", department_id);
                                }
                                parmas2.put("title_id", rspContent.split(",")[1]);
                                title_id=rspContent.split(",")[1];
                                getData(parmas2);
                                break;

                            default:
                                break;
                        }
                    }

                    @Override
                    public void onFailure(ConsultationCallbackException exp) {

                    }
                });
                startActivity(new Intent(mContext, TitleActivity.class));
            }
        });

        myAdapter=new MyAdapter();

        ((PullToRefreshLayout)specialistLayout.findViewById(R.id.specialist_info_refresh_view))
            .setOnRefreshListener(new OnRefreshListener() {

                @Override
                public void onRefresh(final PullToRefreshLayout pullToRefreshLayout) {
                    Map<String, String> parmas=new HashMap<String, String>();
                    parmas.put("page", "1");
                    parmas.put("rows", "10");
                    if(null != hospital_id) {
                        parmas.put("hospital_id", hospital_id);
                    }
                    if(null != department_id) {
                        parmas.put("depart_id", department_id);
                    }
                    if(null != title_id) {
                        parmas.put("title_id", title_id);
                    }
                    OpenApiService.getInstance(mContext).getExpertList(mQueue, parmas, new Response.Listener<String>() {

                        @Override
                        public void onResponse(String arg0) {
                            try {
                                JSONObject responses=new JSONObject(arg0);
                                if(responses.getInt("rtnCode") == 1) {
                                    JSONArray infos=responses.getJSONArray("experts");
                                    specialistList.clear();
                                    for(int i=0; i < infos.length(); i++) {
                                        JSONObject info=infos.getJSONObject(i);
                                        SpecialistTo specialistTo=new SpecialistTo();
                                        specialistTo.setApprove_status(info.getString("approve_status"));
                                        specialistTo.setDepart_name(info.getString("depart_name"));
                                        specialistTo.setGoodat_fields(info.getString("goodat_fields"));
                                        specialistTo.setHospital_name(info.getString("hospital_name"));
                                        specialistTo.setId(info.getString("id"));
                                        specialistTo.setTitle(info.getString("title"));
                                        JSONObject userToJsonObject=info.getJSONObject("user");
                                        UserTo userTo=
                                            new UserTo(userToJsonObject.getString("real_name"), userToJsonObject.getString("sex"),
                                                userToJsonObject.getString("birth_year"), userToJsonObject.getString("tp"),
                                                userToJsonObject.getString("icon_url"));
                                        JSONObject userStatisticsJsonObject=info.getJSONObject("userTj");
                                        specialistTo.setUser(userTo);
                                        UserStatisticsTo userStatistics=
                                            new UserStatisticsTo(userStatisticsJsonObject.getInt("total_consult"), userStatisticsJsonObject.getInt("star_value"));
                                        specialistTo.setUserTj(userStatistics);
                                        specialistList.add(specialistTo);
                                    }
                                    if(infos.length() == 10) {
                                        specialistListView.setHasMoreData(true);
                                    } else {
                                        specialistListView.setHasMoreData(false);
                                    }
                                    Message msg=handler.obtainMessage();
                                    msg.what=0;
                                    msg.obj=pullToRefreshLayout;
                                    handler.sendMessage(msg);
                                } else {
                                    Message msg=handler.obtainMessage();
                                    msg.what=2;
                                    msg.obj=pullToRefreshLayout;
                                    handler.sendMessage(msg);
                                    Toast.makeText(mContext, responses.getString("rtnMsg"), Toast.LENGTH_SHORT).show();
                                }
                            } catch(JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError arg0) {
                            Toast.makeText(mContext, "网络连接失败,请稍后重试", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });

        specialistListView=(PullableListView)specialistLayout.findViewById(R.id.specialist_info_listView);
        specialistListView.setAdapter(myAdapter);
        specialistListView.setOnLoadListener(this);
        specialistListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent=new Intent(mContext, SpecialistInfoActivity.class);
                intent.putExtra("id", specialistList.get(position).getId());
                intent.putExtra("name", specialistList.get(position).getUser().getUser_name());
                intent.putExtra("title", specialistList.get(position).getTitle());
                intent.putExtra("photoUrl", specialistList.get(position).getUser().getIcon_url());
                startActivity(intent);
            }
        });

        searchBtn=(ImageView)specialistLayout.findViewById(R.id.header_right_image);
        searchBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                startActivity(new Intent(mContext, SearchSpecialistActivity.class));
            }
        });
    }

    private class ViewHolder {

        ImageView photo;

        TextView name;

        TextView departmen;

        TextView hospital;

        TextView patients;

        TextView patientCount;

        TextView score;

        RatingBar scoreRatingBar;
    }

    private class MyAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return specialistList.size();
        }

        @Override
        public Object getItem(int location) {
            return specialistList.get(location);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(convertView == null) {
                holder=new ViewHolder();
                convertView=LayoutInflater.from(((Activity)mContext)).inflate(R.layout.specialist_info_item, null);
                holder.photo=(ImageView)convertView.findViewById(R.id.specialist_info_imageView);
                holder.name=(TextView)convertView.findViewById(R.id.specialist_info_name);
                holder.scoreRatingBar=(RatingBar)convertView.findViewById(R.id.specialist_info_score_ratingBar);
                holder.score=(TextView)convertView.findViewById(R.id.specialist_info_score);
                holder.departmen=(TextView)convertView.findViewById(R.id.specialist_info_department);
                holder.hospital=(TextView)convertView.findViewById(R.id.specialist_info_hospital);
                holder.patients=(TextView)convertView.findViewById(R.id.specialist_info_patient_text);
                holder.patientCount=(TextView)convertView.findViewById(R.id.specialist_info_patient_count_text);
                convertView.setTag(holder);
            } else {
                holder=(ViewHolder)convertView.getTag();
            }
            final String imgUrl=specialistList.get(position).getUser().getIcon_url();
            holder.photo.setTag(imgUrl);
            holder.photo.setImageResource(R.drawable.photo_expert);
            holder.name.setText(specialistList.get(position).getUser().getUser_name());
            holder.name.setTextSize(18);
            holder.score.setText((float)specialistList.get(position).getUserTj().getStar_value()/10 + "分");
            holder.score.setTextSize(16);
            holder.scoreRatingBar.setRating((float)specialistList.get(position).getUserTj().getStar_value()/10);
            holder.departmen.setText(specialistList.get(position).getDepart_name() + "|" + specialistList.get(position).getTitle());
            holder.departmen.setTextSize(16);
            holder.hospital.setText(specialistList.get(position).getHospital_name());
            holder.hospital.setTextSize(16);
            holder.patients.setTextSize(14);
            holder.patientCount.setText(specialistList.get(position).getUserTj().getTotal_consult() + "");
            holder.patientCount.setTextSize(16);
            if(!"null".equals(imgUrl) && !"".equals(imgUrl)) {
                ImageListener listener = ImageLoader.getImageListener(holder.photo, R.drawable.photo_expert, R.drawable.photo_expert);
                mImageLoader.get(imgUrl, listener);
            }
            return convertView;
        }
    }

    @Override
    public void onLoad(final PullableListView pullableListView) {
        Map<String, String> parmas=new HashMap<String, String>();
        page++;
        parmas.put("page", String.valueOf(page));
        parmas.put("rows", "10");
        if(null != hospital_id) {
            parmas.put("hospital_id", hospital_id);
        }
        if(null != department_id) {
            parmas.put("depart_id", department_id);
        }
        if(null != title_id) {
            parmas.put("title_id", title_id);
        }
        OpenApiService.getInstance(mContext).getExpertList(mQueue, parmas, new Response.Listener<String>() {

            @Override
            public void onResponse(String arg0) {
                try {
                    JSONObject responses=new JSONObject(arg0);
                    if(responses.getInt("rtnCode") == 1) {
                        JSONArray infos=responses.getJSONArray("experts");
                        for(int i=0; i < infos.length(); i++) {
                            JSONObject info=infos.getJSONObject(i);
                            SpecialistTo specialistTo=new SpecialistTo();
                            specialistTo.setApprove_status(info.getString("approve_status"));
                            specialistTo.setDepart_name(info.getString("depart_name"));
                            specialistTo.setGoodat_fields(info.getString("goodat_fields"));
                            specialistTo.setHospital_name(info.getString("hospital_name"));
                            specialistTo.setId(info.getString("id"));
                            specialistTo.setTitle(info.getString("title"));
                            JSONObject userToJsonObject=info.getJSONObject("user");
                            UserTo userTo=
                                new UserTo(userToJsonObject.getString("real_name"), userToJsonObject.getString("sex"),
                                    userToJsonObject.getString("birth_year"), userToJsonObject.getString("tp"), userToJsonObject
                                        .getString("icon_url"));
                            JSONObject userStatisticsJsonObject=info.getJSONObject("userTj");
                            specialistTo.setUser(userTo);
                            UserStatisticsTo userStatistics=
                                new UserStatisticsTo(userStatisticsJsonObject.getInt("total_consult"), 1);
                            specialistTo.setUserTj(userStatistics);
                            specialistList.add(specialistTo);
                        }
                        if(infos.length() == 10) {
                            hasMore=true;
                        } else {
                            hasMore=false;
                        }
                        Message msg=handler.obtainMessage();
                        msg.what=1;
                        msg.obj=pullableListView;
                        handler.sendMessage(msg);
                    } else {
                        Toast.makeText(mContext, responses.getString("rtnMsg"), Toast.LENGTH_SHORT).show();
                    }
                } catch(JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError arg0) {
                Toast.makeText(mContext, "网络连接失败,请稍后重试", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getData(Map<String, String> parmas) {
        parmas.put("page", String.valueOf(page));
        parmas.put("rows", "10");
        CommonUtil.showLoadingDialog(mContext);
        OpenApiService.getInstance(mContext).getExpertList(mQueue, parmas, new Response.Listener<String>() {

            @Override
            public void onResponse(String arg0) {
                CommonUtil.closeLodingDialog();
                try {
                    JSONObject responses=new JSONObject(arg0);
                    if(responses.getInt("rtnCode") == 1) {
                        JSONArray infos=responses.getJSONArray("experts");
                        specialistList.clear();
                        for(int i=0; i < infos.length(); i++) {
                            JSONObject info=infos.getJSONObject(i);
                            SpecialistTo specialistTo=new SpecialistTo();
                            specialistTo.setApprove_status(info.getString("approve_status"));
                            specialistTo.setDepart_name(info.getString("depart_name"));
                            specialistTo.setGoodat_fields(info.getString("goodat_fields"));
                            specialistTo.setHospital_name(info.getString("hospital_name"));
                            specialistTo.setId(info.getString("id"));
                            specialistTo.setTitle(info.getString("title"));
                            JSONObject userToJsonObject=info.getJSONObject("user");
                            UserTo userTo=
                                new UserTo(userToJsonObject.getString("real_name"), userToJsonObject.getString("sex"),
                                    userToJsonObject.getString("birth_year"), userToJsonObject.getString("tp"), userToJsonObject
                                        .getString("icon_url"));
                            JSONObject userStatisticsJsonObject=info.getJSONObject("userTj");
                            specialistTo.setUser(userTo);
                            UserStatisticsTo userStatistics=
                                new UserStatisticsTo(userStatisticsJsonObject.getInt("total_consult"), userStatisticsJsonObject.getInt("star_value"));
                            specialistTo.setUserTj(userStatistics);
                            specialistList.add(specialistTo);
                        }
                        if(infos.length() == 10) {
                            specialistListView.setHasMoreData(true);
                        } else {
                            specialistListView.setHasMoreData(false);
                        }
                        Message msg=handler.obtainMessage();
                        msg.what=3;
                        handler.sendMessage(msg);
                    } else {
                        Toast.makeText(mContext, responses.getString("rtnMsg"), Toast.LENGTH_SHORT).show();
                    }
                } catch(JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError arg0) {
                CommonUtil.closeLodingDialog();
                Toast.makeText(mContext, "网络连接失败,请稍后重试", Toast.LENGTH_SHORT).show();
            }
        });
    }
}