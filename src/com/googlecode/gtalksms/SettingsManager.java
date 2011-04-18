package com.googlecode.gtalksms;

import java.util.Locale;
import java.util.Map;

import org.jivesoftware.smack.util.StringUtils;

import android.app.backup.BackupManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.provider.Settings;
import android.util.Log;

import com.googlecode.gtalksms.tools.Tools;
/**
 * 
 * @author GTalkSMS Team
 * 
 * In order to work flawlessly with the BackupAgent
 * ALL settings in SettingsManager have to be of the same type
 * as within the SharedPreferences back-end AND they need to have
 * the same name
 *
 */
public class SettingsManager {
    public static final String[] xmppConnectionSettings = { "serverHost", "serviceName", "serverPort", 
                                                            "login", "password", "useDifferentAccount",
                                                            "xmppSecurityMode", "manuallySpecifyServerSettings",
                                                            "useCompression"};
    
    public static final int XMPPSecurityDisabled = 1;
    public static final int XMPPSecurityRequired = 2;
    public static final int XMPPSecurityOptional = 3;
    
    // XMPP connection
    public String serverHost;
    public String serviceName;
    public int serverPort;
    
    public String login;
    public String password;
    public String notifiedAddress;
    public boolean useDifferentAccount;
    public String roomPassword;
    public String mucServer;
    public boolean useCompression;
    public String xmppSecurityMode;
    public int xmppSecurityModeInt;
    public boolean manuallySpecifyServerSettings;
    
    public static boolean connectionSettingsObsolete;
    
    // notifications
    public boolean notifyApplicationConnection;
    public boolean formatResponses;
    public boolean showStatusIcon;
    
    // geo location
    public boolean useGoogleMapUrl;
    public boolean useOpenStreetMapUrl;

    // ring
    public String ringtone = null;

    // battery
    public boolean notifyBatteryInStatus;
    public boolean notifyBattery;
    public int batteryNotificationIntervalInt;
    public String batteryNotificationInterval;

    // sms
    public int smsNumber;
    public boolean showSentSms;
    public boolean notifySmsSent;
    public boolean notifySmsDelivered;
    public boolean notifySmsSentDelivered;
    public boolean notifyIncomingCalls;
    public boolean notifySmsInChatRooms;
    public boolean notifySmsInSameConversation;
    public boolean notifyInMuc;
    public boolean smsReplySeparate;
    
    // calls
    public int callLogsNumber;
    
    // locale
    public Locale locale;
    
    // app settings
    public boolean api8orGreater;
    public boolean api9orGreater;
    public boolean debugLog;
    public boolean connectOnMainscreenShow;
    
    private static SettingsManager settingsManager = null;
    
    private SharedPreferences _sharedPreferences;
    private Context _context;
    private OnSharedPreferenceChangeListener _changeListener = new OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
			if (debugLog) {
				Log.i(Tools.LOG_TAG, "Preferences updated: key=" + key);
			}
            importPreferences();
            OnPreferencesUpdated(key);
        }
    };
    
    private SettingsManager(Context context) {
        _context = context;
        _sharedPreferences = _context.getSharedPreferences("GTalkSMS", 0);
        _sharedPreferences.registerOnSharedPreferenceChangeListener(_changeListener);
        
        importPreferences();
    }
    
    public static SettingsManager getSettingsManager(Context context) {
        if (settingsManager == null) {
            settingsManager = new SettingsManager(context);           
        } 
        return settingsManager;        
    }
    
    public void Destroy() {
        _sharedPreferences.unregisterOnSharedPreferenceChangeListener(_changeListener);
    }
    
    public SharedPreferences.Editor getEditor() {
    	return _sharedPreferences.edit();
    }
    
    public Map<String, ?> getAllSharedPreferences() {
        return _sharedPreferences.getAll();
    }
    
    public boolean SharedPreferencesContains(String key) {
    	return _sharedPreferences.contains(key);
    }

    public void OnPreferencesUpdated(String key) {
    	if(api8orGreater) {
    		BackupManager.dataChanged(_context.getPackageName());
    	}
    	for (String s : xmppConnectionSettings)
    	    if (s.equals(key))
    	        connectionSettingsObsolete = true;
    	if (key.equals("locale")) {
            Tools.setLocale(this, _context);
    	}
    }
    
    /** imports the preferences */
	private void importPreferences() {
        serverHost = _sharedPreferences.getString("serverHost", "");
        serverPort = _sharedPreferences.getInt("serverPort", 0);
        
        notifiedAddress = _sharedPreferences.getString("notifiedAddress", "");
        
        useDifferentAccount = _sharedPreferences.getBoolean("useDifferentAccount", false);
        if (useDifferentAccount) {
            login = _sharedPreferences.getString("login", "");
        } else{
            login = notifiedAddress;
        }
        
        manuallySpecifyServerSettings = _sharedPreferences.getBoolean("manuallySpecifyServerSettings", true);
        if (manuallySpecifyServerSettings) {
            serviceName = _sharedPreferences.getString("serviceName", "");
        } else {
            serviceName = StringUtils.parseServer(login);
        }
        
        password =  _sharedPreferences.getString("password", "");
        xmppSecurityMode = _sharedPreferences.getString("xmppSecurityMode", "opt");
        if(xmppSecurityMode.equals("req")) {
            xmppSecurityModeInt = XMPPSecurityRequired;
        } else if (xmppSecurityMode.equals("dis")) {
            xmppSecurityModeInt = XMPPSecurityDisabled;
        } else {
            xmppSecurityModeInt = XMPPSecurityOptional;
        }
        useCompression = _sharedPreferences.getBoolean("useCompression", false);
        
        useGoogleMapUrl = _sharedPreferences.getBoolean("useGoogleMapUrl", true);
        useOpenStreetMapUrl = _sharedPreferences.getBoolean("useOpenStreetMapUrl", false);
        
        showStatusIcon = _sharedPreferences.getBoolean("showStatusIcon", true);
        
        notifyApplicationConnection = _sharedPreferences.getBoolean("notifyApplicationConnection", true);
        notifyBattery = _sharedPreferences.getBoolean("notifyBattery", true);
        notifyBatteryInStatus = _sharedPreferences.getBoolean("notifyBatteryInStatus", true);
        batteryNotificationInterval = _sharedPreferences.getString("batteryNotificationInterval", "10");
        batteryNotificationIntervalInt = Integer.parseInt(batteryNotificationInterval);
        notifySmsSent = _sharedPreferences.getBoolean("notifySmsSent", true);
        notifySmsDelivered = _sharedPreferences.getBoolean("notifySmsDelivered", true);
        notifySmsSentDelivered = notifySmsSent || notifySmsDelivered;
        ringtone = _sharedPreferences.getString("ringtone", Settings.System.DEFAULT_RINGTONE_URI.toString());
        showSentSms = _sharedPreferences.getBoolean("showSentSms", false);
        smsNumber = _sharedPreferences.getInt("smsNumber", 5);
        callLogsNumber = _sharedPreferences.getInt("callLogsNumber", 10);
        formatResponses = _sharedPreferences.getBoolean("formatResponses", false);
        notifyIncomingCalls = _sharedPreferences.getBoolean("notifyIncomingCalls", false);

        String localeStr = _sharedPreferences.getString("locale", "default");
        if (localeStr.equals("default")) {
            locale = Locale.getDefault();
        } else {
            locale = new Locale(localeStr);
        }
        
        roomPassword = _sharedPreferences.getString("roomPassword", "gtalksms");
        mucServer = _sharedPreferences.getString("mucServer", "conference.jwchat.org");
        String notificationIncomingSmsType = _sharedPreferences.getString("notificationIncomingSmsType", "same");
        
        if (notificationIncomingSmsType.equals("both")) {
            notifySmsInChatRooms = true;
            notifySmsInSameConversation = true;
        } else if (notificationIncomingSmsType.equals("no")) {
            notifySmsInChatRooms = false;
            notifySmsInSameConversation = false;
        } else if (notificationIncomingSmsType.equals("separate")) {
            notifySmsInChatRooms = true;
            notifySmsInSameConversation = false;
        } else {
            notifySmsInSameConversation = true;
            notifySmsInChatRooms = false;
        }
        
        try {
        	Class.forName("android.app.backup.BackupAgent");
        	api8orGreater = true;
        } catch (Exception e) {
        	api8orGreater = false;
        }
        
        api9orGreater = false;
                
        notifyInMuc = _sharedPreferences.getBoolean("notifyInMuc", false); 
        smsReplySeparate = _sharedPreferences.getBoolean("smsReplySeparate", false);
        
        connectOnMainscreenShow = _sharedPreferences.getBoolean("connectOnMainscreenShow", false);
        debugLog = _sharedPreferences.getBoolean("debugLog", false); 
    }
}
