package com.example.fujitsu.argomediamicrophone.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.fujitsu.argomediamicrophone.R;
import com.example.fujitsu.argomediamicrophone.Util;
import com.example.fujitsu.argomediamicrophone.activityes.CameraActivity;
import com.example.fujitsu.argomediamicrophone.session.Session;


public class CameraRecordFragment extends Fragment implements View.OnClickListener {

    public CameraRecordFragment() {
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.camera_record_fragment_layout, container, false);
        v.findViewById(R.id.record).setOnClickListener(this);
        return v;
    }

    @Override
    public void onClick(View v) {
        if (!Session.current().isConnected()) {
            Util.toast(getString(R.string.CONNECTION_IS_NOT_EXISTING), getActivity());
            return;
        }
        final Intent i = new Intent(getActivity().getApplicationContext(), CameraActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
    }
}
