package com.moor.im.options.mobileassistant.customer.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;

import com.moor.im.R;
import com.moor.im.options.mobileassistant.cdr.view.MP3PlayerView;

/**
 * Created by longwei on 2016/9/22.
 */

public class Mp3PlayDialog extends DialogFragment{
    private MP3PlayerView mycalldetail_mp3player;
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Bundle b = getArguments();
        String recordFile = b.getString("path");

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_mp3play, null);
        mycalldetail_mp3player = (MP3PlayerView) view.findViewById(R.id.mp3_player);

        Dialog dialog = new Dialog(getActivity(), R.style.dialog);
        dialog.setContentView(view);
        dialog.setCanceledOnTouchOutside(false);
        if(recordFile != null && !"".equals(recordFile)) {
            mycalldetail_mp3player.setUrlPath(recordFile);
        }else {
            dismiss();
        }
        return dialog;
    }

    @Override
    public void show(FragmentManager manager, String tag) {
        if(!this.isAdded()) {
            try {
                super.show(manager, tag);
            }catch (Exception e) {}
        }
    }

    @Override
    public void dismiss() {
        try {
            super.dismiss();
            mycalldetail_mp3player.stop();
        }catch (Exception e) {}

    }
}
