package com.example.seongjun.biocube;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link UserCubeFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link UserCubeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UserCubeFragment extends Fragment {
    private OnFragmentInteractionListener mListener;

//    Button.OnClickListener registCubeBtn = new Button.OnClickListener(){
//        @Override
//        public void onClick(View v) {
//            Intent intent;
//            intent = new Intent(v.getContext(), CubeRegister.class);
//            startActivity(intent);
//        }
//    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        getView().findViewById(R.id.btn_registCube).setOnClickListener(registCubeBtn);
    }

    public UserCubeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment UserCubeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static UserCubeFragment newInstance() {
        UserCubeFragment fragment = new UserCubeFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_cube, container, false);

        /* Toolbar 설정 */
        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar_user_cube);
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
        ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);

        Button registCube = (Button) view.findViewById(R.id.btn_cube_register);

        registCube.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            Intent intent;
                intent = new Intent(v.getContext(), CubeRegister.class);
                startActivity(intent);
            }
        });
        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
