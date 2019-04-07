package com.example.fujitsu.argomediamicrophone.fragments.slides;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.fujitsu.argomediamicrophone.C;
import com.example.fujitsu.argomediamicrophone.R;
import com.example.fujitsu.argomediamicrophone.activityes.MainActivity;
import com.example.fujitsu.argomediamicrophone.util.PrefUtil;


public class ChooseRoleSlide extends Fragment implements View.OnClickListener {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.choose_role_slide_layout, null);
        v.findViewById(R.id.camera).setOnClickListener(this);
        v.findViewById(R.id.mic).setOnClickListener(this);
        return v;
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.mic) {
            PrefUtil.saveRole(C.MIC);
            startMainActivity(C.MIC);
        } else {
            PrefUtil.saveRole(C.CAMERA);
            startMainActivity(C.CAMERA);
        }
        PrefUtil.saveRegistered();
    }

    private void startMainActivity(final String role) {
        final Intent i = new Intent(getActivity(), MainActivity.class);
        i.putExtra(C.DATA, role);
        startActivity(i);
        getActivity().finish();
    }

}
