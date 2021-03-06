/*
 * (c) COPYRIGHT 2009-2012 MOTOROLA INC.
 * MOTOROLA CONFIDENTIAL PROPRIETARY
 * MOTOROLA Advanced Technology and Software Operations
 *
 * REVISION HISTORY:
 * Author        Date       CR Number         Brief Description
 * ------------- ---------- ----------------- ------------------------------
 * e51141        2011/02/27 IKCTXTAW-201		   Initial version
 */

package edu.csulb.wifidirectdemo;


import android.util.Log;

import static edu.csulb.wifidirectdemo.Constants.MSG_SENDER;
import static edu.csulb.wifidirectdemo.Constants.MSG_SIZE;

import java.util.HashSet;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JSONUtils {
    public static final String TAG = "PTP_UtilsJSON";
    
    
    /**
     * convert json string to json object
     */
    public static JSONObject getJsonObject( String jsonstr ) {
    	JSONObject jsonobj = null;
    	try{
    		jsonobj = new JSONObject(jsonstr);
    	}catch(JSONException e){
    		Log.d(TAG, "getJsonObject : " + e.toString());
    	}
    	return jsonobj;
    }
    
    /**
     * convert json array string to jsonarray
     */
    public static JSONArray getJsonArray(String jsonstr) {
        JSONArray curjsons = null;
        if (jsonstr == null) {
            return null;
        }
        try {
            curjsons = new JSONArray(jsonstr);  // convert string back to json array
        } catch (JSONException e) {
        	Log.e(TAG, "getJSONArray:" + e.toString());
        }
        return curjsons;
    }
    
    /**
     * truncate the oldest json objects in the json array
     */
    public static JSONArray truncateJSONArray(JSONArray origarray, int offset){
    	int sz = origarray.length();
    	if( sz > MSG_SIZE){
    		JSONArray newarray = new JSONArray();
    		try{
    			for(int i=offset; i < sz; i++){
    				newarray.put(origarray.getJSONObject(i));
    			}
    		}catch(JSONException e){
    			Log.e(TAG, "truncateJSONArray :" + e.toString());
    		}
    		return newarray;
    	}else{
    		return origarray;  // no truncate, return the origarray.
    	}
    }
    
    /**
     * find whether a json array contains a json object with certain key.
     * the comparison is based on the string value of passed in key. If key is not provided, use the entire object's string value.
     * return true if found, false otherwise.
     * wifissid=[{"wifibssid":"00:14:6c:14:ec:fa","wifissid":"PInternet"},{...}, ... ]
     */
    public static int indexOfJSONObject(JSONArray jsonarray, JSONObject jsonobj, String key) {
        String objstr = null;
        if (key == null) {
            objstr = jsonobj.toString();
        } else {
            try {
                objstr = jsonobj.getString(key);
            } catch (JSONException e) {
                objstr = null;
                Log.e(TAG, "findJSONObject:  get key Exception: " + e.toString());
            }
        }

        // java is f* verbose...no expressive power!
        if (objstr != null) {
            objstr = objstr.trim();
            if (objstr.length() == 0) {
                return -1;
            }
        } else {
        	Log.d(TAG, "findJSONObject:  empty key string! no found. ");
            return -1;
        }

        int size = jsonarray.length();
        JSONObject entry = null;
        String entrystr = null;
        for (int i=0; i<size; i++) {
            try {
                entry = jsonarray.getJSONObject(i);
                if (key == null) {
                    entrystr = entry.toString();
                } else {
                    entrystr = entry.getString(key);
                }
                if (entrystr != null) {
                    entrystr = entrystr.trim();
                }

                if (objstr.equals(entrystr)) {
                	Log.d(TAG, "findJSONObject: match :" + objstr);
                    return i;   // return immediately
                }
            } catch (JSONException e) {
            	Log.e(TAG, "findJSONObject: getJSONObject Exception: " + e.toString());
                continue;
            }
        }
        return -1;
    }

    

    /**
     * merge the new json array into the existing json array.
     * @return existing json array with newjson array added
     */
    public static JSONArray mergeJsonArrays(JSONArray existingjsons, JSONArray newjsons, boolean updatess) {
        if (existingjsons == null)
            return newjsons;
        if (newjsons == null)
            return existingjsons;

        JSONObject newobj = null;
        for (int i=0; i<newjsons.length(); i++) {
            try {
                newobj = newjsons.getJSONObject(i);
                String sender = newobj.getString(MSG_SENDER);
                if(sender == null) {
                    continue;  // bad new json object, skip
                }
                int idx = indexOfJSONObject(existingjsons, newobj, MSG_SENDER);
                if(idx < 0) { // not found, insert newjson obj into json array
                    existingjsons.put(newobj);
                } else if(updatess) { // we need to update signal strength with the scan from discover
                    if(newobj.has(MSG_SENDER)) {
                        String newss = newobj.getString(MSG_SENDER);
                        JSONObject oldobj = existingjsons.getJSONObject(idx);
                        oldobj.put(MSG_SENDER, newss);
                        Log.d(TAG, "mergeJsonArrays: update ss: " + newss + " : " + oldobj.toString());
                    }
                }
            } catch (JSONException e) {
                Log.e(TAG, "mergeJSONArrays: getJSONObject Exception: " + e.toString());
                continue;
            }
        }
        return existingjsons;
    }

    /**
     * merge two jsonarray string and return one json array string
     * curstr is the current json array in string format, newstr is the to be merged json array in string format.
     */
    public static String mergeJsonArrayStrings(String curstr, String newstr) {
        JSONArray curjsons = null;
        JSONArray newjsons = null;

        Log.d(TAG, "mergeJSONArrays:" + curstr + " =+= " + newstr);

        // merge shortcut, if either one is null, return the other.
        if (curstr == null)
            return newstr;
        if (newstr == null)
            return curstr;

        try {
            curjsons = new JSONArray(curstr);  // convert string back to json array
            newjsons = new JSONArray(newstr);
        } catch (JSONException e) {
        	Log.e(TAG, "mergeJSONArrays:" + e.toString());
            return curstr;   // return the original curstr, no merge.
        }

        mergeJsonArrays(curjsons, newjsons, true);  // update ss using scanned ssid from discovery.

        return curjsons.toString();
    }

    /**
     * fuzzy match whether runtime cur wifi ssid jsonarray matches to static db wifi ssid jsonarray
     * match criteria : turn to positive if single match exist. can be more sophisticated.
     * wifissid=[{"wifibssid":"00:14:6c:14:ec:fa","wifissid":"PInternet"},{...}, ... ]
     * @param dbJsonStr  static db set
     * @param curJsonStr runtime current set
     * @return true if two array has common object, false otherwise.
     */
    @Deprecated
    public static boolean fuzzyMatchJsonArrays(String dbJsonStr, String curJsonStr, String key) {
    	Log.d(TAG, "fuzzyMatchJSONArrays : dbsdbjsonstret : " + dbJsonStr + " : curjsonstr :" +curJsonStr);
        if (dbJsonStr == null || curJsonStr == null) {
            return false;    // no match if either of them is null.
        }

        JSONArray dbjsons = null;
        JSONArray curjsons = null;
        try {
            dbjsons = new JSONArray(dbJsonStr);  // convert string back to json array
            curjsons = new JSONArray(curJsonStr);
        } catch (JSONException e) {
        	Log.e(TAG, "mergeJSONArrays:" + e.toString());
            return false;   // no merge if either is wrong
        }

        boolean match = false;
        JSONObject curobj = null;
        for (int i=0; i<curjsons.length(); i++) {
            try {
                curobj = curjsons.getJSONObject(i);
            } catch (JSONException e) {
            	Log.e(TAG, "mergeJSONArrays: getJSONObject Exception: " + e.toString());
                continue;  // skip this entry if can not construct object.
            }

            if(indexOfJSONObject(dbjsons, curobj, key) >= 0) {
                match = true;
                break;
            }
        }
        return match;
    }

    /**
     * get set of values from JSONArray with key, if key is null, get the string of each entire json object.
     * when you are using json, you are dealing with immutable string, no need Generic.
     * @return a set of values
     */
    public static Set<String> getValueSetFromJsonArray(JSONArray jsonarray, String key) {
        Set<String> valset = new HashSet<String>();
        if (jsonarray == null) {
            return valset;
        }

        JSONObject curobj = null;
        String valstr = null;
        for (int i=0; i<jsonarray.length(); i++) {
            try {
                curobj = jsonarray.getJSONObject(i);
                if (key == null) {
                    valstr = curobj.toString();
                } else {
                    valstr = curobj.getString(key);
                }
                valset.add(valstr);
                //LSAppLog.d(TAG, "getValueSetFromJSONArray: " + valstr);
            } catch (JSONException e) {
            	Log.e(TAG, "getValueSetFromJSONArray: Exception: " + e.toString());
                continue;  // skip this entry if can not construct object.
            }
        }
        return valset;
    }
       
    /**
     * given a json string, check whether the string set contains any value of the json string.
     */
    public static int intersectSetJsonArray(Set< ? extends String> set, String wrap, String jsonString) {
        int matchcnt = 0;
        if (jsonString != null && jsonString.length() > 0) {
            for (String s : set) {          
                String wrapstr = s;             
                if (wrap != null) {             
                    wrapstr = wrap + s + wrap;      
                }
                if (jsonString.indexOf(wrapstr) >= 0) {
                    Log.d(TAG, "intersectSetJsonArray: Match: " + wrapstr);
                    matchcnt++;
                }
            }
        }
        return matchcnt;
    }

}
