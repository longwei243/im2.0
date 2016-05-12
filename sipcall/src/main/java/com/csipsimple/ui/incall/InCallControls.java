/**
 * Copyright (C) 2010-2012 Regis Montoya (aka r3gis - www.r3gis.fr)
 * This file is part of CSipSimple.
 *
 *  CSipSimple is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  If you own a pjsip commercial license you can also redistribute it
 *  and/or modify it under the terms of the GNU Lesser General Public License
 *  as an android library.
 *
 *  CSipSimple is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with CSipSimple.  If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * This file contains relicensed code from Apache copyright of 
 * Copyright (C) 2008 The Android Open Source Project
 */

package com.csipsimple.ui.incall;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.csipsimple.R;
import com.csipsimple.api.MediaState;
import com.csipsimple.api.SipCallSession;
import com.csipsimple.utils.Log;

/**
 * Manages in call controls not relative to a particular call such as media route
 */
public class InCallControls extends FrameLayout implements View.OnClickListener {

	private static final String THIS_FILE = "InCallControls";
	IOnCallActionTrigger onTriggerListener;
	
	private MediaState lastMediaState;
	private SipCallSession currentCall;
    private ImageView incall_controls_btn_speaker,incall_controls_btn_mute,incall_controls_btn_setting, incall_controls_btn_dialpan;
    private boolean isSpeakerChecked = false;
    private boolean isMuteChecked = false;

	public InCallControls(Context context) {
        this(context, null, 0);
    }
	
	public InCallControls(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	
    public InCallControls(Context context, AttributeSet attrs, int style) {
        super(context, attrs, style);
        LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		LinearLayout view = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.layout_incall_controls, null);
        incall_controls_btn_speaker = (ImageView) view.findViewById(R.id.incall_controls_btn_speaker);
        incall_controls_btn_mute = (ImageView) view.findViewById(R.id.incall_controls_btn_mute);
        incall_controls_btn_setting = (ImageView) view.findViewById(R.id.incall_controls_btn_setting);
		incall_controls_btn_dialpan = (ImageView) view.findViewById(R.id.incall_controls_btn_dialpan);

        incall_controls_btn_speaker.setOnClickListener(this);
        incall_controls_btn_mute.setOnClickListener(this);
        incall_controls_btn_setting.setOnClickListener(this);
		incall_controls_btn_dialpan.setOnClickListener(this);

        this.addView(view, lp);
    }
    
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		// Finalize object style
		setEnabledMediaButtons(false);
	}

	
	
	private boolean callOngoing = false;
	public void setEnabledMediaButtons(boolean isInCall) {
        callOngoing = isInCall;
        setMediaState(lastMediaState);
	}
	
	public void setCallState(SipCallSession callInfo) {
		currentCall = callInfo;
		
		if(currentCall == null) {
			setVisibility(GONE);
			return;
		}
		
		int state = currentCall.getCallState();
		Log.d(THIS_FILE, "Mode is : "+state);
		switch (state) {
		case SipCallSession.InvState.INCOMING:
		    setVisibility(GONE);
			break;
		case SipCallSession.InvState.CALLING:
		case SipCallSession.InvState.CONNECTING:
		    setVisibility(VISIBLE);
			setEnabledMediaButtons(true);
			break;
		case SipCallSession.InvState.CONFIRMED:
		    setVisibility(VISIBLE);
			setEnabledMediaButtons(true);
			break;
		case SipCallSession.InvState.NULL:
		case SipCallSession.InvState.DISCONNECTED:
		    setVisibility(GONE);
			break;
		case SipCallSession.InvState.EARLY:
		default:
			if (currentCall.isIncoming()) {
			    setVisibility(GONE);
			} else {
			    setVisibility(VISIBLE);
				setEnabledMediaButtons(true);
			}
			break;
		}
		
	}
	
	/**
	 * Registers a callback to be invoked when the user triggers an event.
	 * 
	 * @param listener
	 *            the OnTriggerListener to attach to this view
	 */
	public void setOnTriggerListener(IOnCallActionTrigger listener) {
		onTriggerListener = listener;
	}

	private void dispatchTriggerEvent(int whichHandle) {
		if (onTriggerListener != null) {
			onTriggerListener.onTrigger(whichHandle, currentCall);
		}
	}
	
	public void setMediaState(MediaState mediaState) {
		lastMediaState = mediaState;

        // Update menu
		// BT
		boolean enabled, checked;

        // Mic
        if(lastMediaState == null) {
            enabled = callOngoing;
            checked = false;
        }else {
            enabled = callOngoing && lastMediaState.canMicrophoneMute;
            checked = lastMediaState.isMicrophoneMute;
        }


        // Speaker
        Log.d(THIS_FILE, ">> Speaker " + lastMediaState);
        if(lastMediaState == null) {
            enabled = callOngoing;
            checked = false;
        }else {
            Log.d(THIS_FILE, ">> Speaker " + lastMediaState.isSpeakerphoneOn);
            enabled = callOngoing && lastMediaState.canSpeakerphoneOn;
            checked = lastMediaState.isSpeakerphoneOn;
        }


	}


    @Override
    public void onClick(View view) {
		if(view.getId() == R.id.incall_controls_btn_speaker) {
			if (isSpeakerChecked) {
				isSpeakerChecked = false;
				incall_controls_btn_speaker.setImageResource(R.drawable.sip_call_icon_speaker_off);
				dispatchTriggerEvent(IOnCallActionTrigger.SPEAKER_OFF);
			} else {
				isSpeakerChecked = true;
				incall_controls_btn_speaker.setImageResource(R.drawable.sip_call_icon_speaker_on);
				dispatchTriggerEvent(IOnCallActionTrigger.SPEAKER_ON);
			}
		}else if(view.getId() == R.id.incall_controls_btn_mute) {
			if(isMuteChecked) {
				isMuteChecked = false;
				incall_controls_btn_mute.setImageResource(R.drawable.sip_call_icon_mute_off);
				dispatchTriggerEvent(IOnCallActionTrigger.MUTE_OFF);
			}else {
				isMuteChecked = true;
				incall_controls_btn_mute.setImageResource(R.drawable.sip_call_icon_mute_on);

				dispatchTriggerEvent(IOnCallActionTrigger.MUTE_ON);
			}
		}else if(view.getId() == R.id.incall_controls_btn_setting) {
			dispatchTriggerEvent(IOnCallActionTrigger.MEDIA_SETTINGS);
		}else if(view.getId() == R.id.incall_controls_btn_dialpan) {
			dispatchTriggerEvent(IOnCallActionTrigger.DTMF_DISPLAY);
		}
    }
}
