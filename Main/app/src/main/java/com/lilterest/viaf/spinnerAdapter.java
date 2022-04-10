package com.lilterest.viaf;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.lilterest.viaf.utils.imageFolder;

import java.util.List;

public class spinnerAdapter extends BaseAdapter {
    Context context;
    List<String> data;
    LayoutInflater inflater;
    List<imageFolder> imageView;

    public spinnerAdapter(Context context, List<String> data, List<imageFolder> imageView){
        this.context = context;
        this.data = data;
        this.imageView = imageView;

        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        if(data!=null) return data.size();
        else return 0;
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        if(convertView==null){
            convertView = inflater.inflate(R.layout.spinner_custom_dropdown, parent, false);
        }

        //데이터세팅
        String text = data.get(position);
        ((TextView)convertView.findViewById(R.id.spinner_text)).setText(text);
        imageFolder image = imageView.get(position);
        ImageView imageViewSpinner = (ImageView) convertView.findViewById(R.id.spinner_image);
        Glide.with(convertView)
                .load(image.getFirstPic())
                .apply(new RequestOptions().centerCrop())
                .into(imageViewSpinner);

        return convertView;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView==null) {
            convertView = inflater.inflate(R.layout.spinner_custom_dropdown, parent, false);
        }

        if(data!=null){
            //데이터세팅
            String text = data.get(position);
            ((TextView)convertView.findViewById(R.id.spinner_text)).setText(text);


        }

        return convertView;

    }
}
