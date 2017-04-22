package edu.csulb.wifidirectdemo;


import static edu.csulb.wifidirectdemo.Constants.MSG_CONTENT;
import static edu.csulb.wifidirectdemo.Constants.MSG_SENDER;
import static edu.csulb.wifidirectdemo.Constants.MSG_TIME;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public class MessageDetails implements Parcelable {
	private final static String TAG = "PTP_MSG";
	
	public String mSender;
	public String mMsg;
	public String mTime;
	public static final String mDel = "^&^";
	
	private MessageDetails() {
	    this.mSender = null;
		this.mTime = null;
		this.mMsg = null;
	}
	
	public MessageDetails(String sender, String msg, String time){
		mTime = time;
		if( time == null ){
			Date now = new Date();
			//SimpleDateFormat timingFormat = new SimpleDateFormat("mm/dd hh:mm");
			//mTime = new SimpleDateFormat("dd/MM HH:mm").format(now);
			mTime = new SimpleDateFormat("h:mm a").format(now);
		} 
		mSender = sender;
		mMsg = msg;
	}
	
	public MessageDetails(Parcel in) {
        readFromParcel(in);
    }
	
	public String toString() {
		return mSender + mDel + mMsg + mDel + mTime;
	}
	
	
	public static JSONObject getAsJSONObject(MessageDetails msgrow) {
		JSONObject jsonobj = new JSONObject();
		try{
			jsonobj.put(MSG_SENDER, msgrow.mSender);
			jsonobj.put(MSG_TIME, msgrow.mTime);
			jsonobj.put(MSG_CONTENT, msgrow.mMsg);
		}catch(JSONException e){
			//Log.e(TAG, "getAsJSONObject : " + e.toString());
		}
		return jsonobj;
	}
	
	/**
	 * convert json object to message row.
	 */
	public static MessageDetails parseMesssageRow(JSONObject jsonobj) {
		MessageDetails row = null;
		if( jsonobj != null ){
			try{
				row = new MessageDetails(jsonobj.getString(MSG_SENDER), jsonobj.getString(MSG_CONTENT), jsonobj.getString(MSG_TIME));
			}catch(JSONException e){
				Log.d(TAG, "parseMessageRow: " + e.toString());
			}
		}
		return row;
	}
	
	/**
	 * convert a json string representation of messagerow into messageRow object.
	 */
	public static MessageDetails parseMessageRow(String jsonMsg){
		JSONObject jsonobj = JSONUtils.getJsonObject(jsonMsg);
		Log.d(TAG, "parseMessageRow : " + jsonobj.toString());
		return parseMesssageRow(jsonobj);
	}

	public static final Creator<MessageDetails> CREATOR = new Creator<MessageDetails>() {
        public MessageDetails createFromParcel(Parcel in) {
            return new MessageDetails(in);
        }
 
        public MessageDetails[] newArray(int size) {
            return new MessageDetails[size];
        }
    };
    
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(mSender);
		dest.writeString(mMsg);
		dest.writeString(mTime);
	}
	
	public void readFromParcel(Parcel in) {
		mSender = in.readString();
		mMsg = in.readString();
		mTime = in.readString();
    }
}
