package com.example.mynavigator.ui.data;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.mynavigator.R;

import java.util.ArrayList;
import java.util.List;

public class DataFragment extends Fragment {

    public List<Data> dataList ;
    ListView listview;
    SingerAdapter adapter;
    private DataViewModel dataViewModel;
    private int showCount = 10;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        dataViewModel =
                ViewModelProviders.of(this).get(DataViewModel.class);
        View root = inflater.inflate(R.layout.fragment_data, container, false);
        final TextView textView = root.findViewById(R.id.text_data);
        dataViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        initLoadDB();


        listview = (ListView)root.findViewById(R.id.data_container);
        adapter = new SingerAdapter();


        for(int i=1;i<dataList.size();i++){
            Data data = dataList.get(i);
            adapter.addItem(data);
        }
        listview.setAdapter(adapter);

        Button button = (Button)root.findViewById(R.id.data_roll_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCount += 10;
                listview.setAdapter(adapter);
            }
        });

        return root;
    }

    private void initLoadDB() {

        DataAdapter mDbHelper = new DataAdapter(getActivity().getApplicationContext());
        mDbHelper.createDatabase();
        mDbHelper.open();

        // db에 있는 값들을 model을 적용해서 넣는다.
        dataList = mDbHelper.getTableData();

        // db 닫기
        mDbHelper.close();

    }
    class SingerAdapter extends BaseAdapter {

        ArrayList<Data> items = new ArrayList<>();
        public void addItem(Data item){
            items.add(item);
        }
        @Override
        public int getCount() {
            return showCount;
        }

        @Override
        public Object getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ItemView view = null;
            if(convertView == null){
                view = new ItemView(getActivity().getApplicationContext());
            }
            else{
                view = (ItemView) convertView;
            }
            Data item = items.get(position);
            view.setTextview(item);
            return view;
        }

    }

}