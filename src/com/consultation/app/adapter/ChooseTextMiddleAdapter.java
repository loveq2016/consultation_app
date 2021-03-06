package com.consultation.app.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.consultation.app.R;

public class ChooseTextMiddleAdapter extends ArrayAdapter<String> {

	private Context mContext;
	private List<String> mListData;
	private String[] mArrayData;
	private int selectedPos = -1;
	private String selectedText = "";
	private int normalDrawbleId;
	private OnClickListener onClickListener;
	private OnItemClickListener mOnItemClickListener;

	public ChooseTextMiddleAdapter(Context context, List<String> listData, int nId) {
		super(context, R.string.no_data, listData);
		mContext = context;
		mListData = listData;
		normalDrawbleId = nId;
		init();
	}

	private void init() {
		onClickListener = new OnClickListener() {

			@Override
			public void onClick(View view) {
				selectedPos = (Integer) view.getTag();
				setSelectedPosition(selectedPos);
				if (mOnItemClickListener != null) {
					mOnItemClickListener.onItemClick(view, selectedPos);
				}
			}
		};
	}

	public ChooseTextMiddleAdapter(Context context, String[] arrayData, int nId) {
		super(context, R.string.no_data, arrayData);
		mContext = context;
		mArrayData = arrayData;
		normalDrawbleId = nId;
		init();
	}

	public void setSelectedPosition(int pos) {
		if (mListData != null && pos < mListData.size()) {
			selectedPos = pos;
			selectedText = mListData.get(pos);
			notifyDataSetChanged();
		} else if (mArrayData != null && pos < mArrayData.length) {
			selectedPos = pos;
			selectedText = mArrayData[pos];
			notifyDataSetChanged();
		}
	}

	public void setSelectedPositionNoNotify(int pos) {
		selectedPos = pos;
		if (mListData != null && pos < mListData.size()) {
			selectedText = mListData.get(pos);
		} else if (mArrayData != null && pos < mArrayData.length) {
			selectedText = mArrayData[pos];
		}
	}

	public int getSelectedPosition() {
		if (mArrayData != null && selectedPos < mArrayData.length) {
			return selectedPos;
		}
		if (mListData != null && selectedPos < mListData.size()) {
			return selectedPos;
		}

		return -1;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		TextView textView;
		ImageView imageView;;
		if (convertView == null) {
		    convertView = LayoutInflater.from(mContext).inflate(R.layout.choose_middle_item, null);
		}
		textView = (TextView)convertView.findViewById(R.id.choose_middle_text);
		textView.setTextSize(17);
		imageView = (ImageView)convertView.findViewById(R.id.choose_middle_imageView);
		String mString = "";
		if (mListData != null) {
			if (position < mListData.size()) {
				mString = mListData.get(position);
			}
		} else if (mArrayData != null) {
			if (position < mArrayData.length) {
				mString = mArrayData[position];
			}
		}
		textView.setText(mString);
		textView.setTextSize(17);
		if (selectedText != null && selectedText.equals(mString)) {
		    imageView.setVisibility(View.VISIBLE);
		    convertView.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.choose_item_selected));
		} else {
		    imageView.setVisibility(View.INVISIBLE);
		    convertView.setBackgroundDrawable(mContext.getResources().getDrawable(normalDrawbleId));
		}
		convertView.setOnClickListener(onClickListener);
		convertView.setTag(position);
		return convertView;
	}

	public void setOnItemClickListener(OnItemClickListener l) {
		mOnItemClickListener = l;
	}

	public interface OnItemClickListener {
		public void onItemClick(View view, int position);
	}

}
