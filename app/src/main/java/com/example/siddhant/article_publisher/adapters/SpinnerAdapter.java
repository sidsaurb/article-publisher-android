package com.example.siddhant.article_publisher.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.siddhant.article_publisher.Globals;
import com.example.siddhant.article_publisher.R;
import com.example.siddhant.article_publisher.networkClasses.Categories;

import java.util.ArrayList;

/**
 * Created by siddhant on 2/2/16.
 */
public class SpinnerAdapter extends ArrayAdapter<Categories> {

    private String tempValues = null;
    private final LayoutInflater inflater;

    public SpinnerAdapter(Context context, int textViewResourceId, ArrayList<Categories> objects) {
        super(context, textViewResourceId, objects);
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, parent);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getCollapsedView(position, parent);
    }

    private View getCustomView(int position, ViewGroup parent) {
        View row = inflater.inflate(R.layout.spinner_row, parent, false);
        tempValues = null;
        tempValues = getItem(position).name;
        TextView myCountryNameText = (TextView) row.findViewById(R.id.state);
        myCountryNameText.setTypeface(Globals.typeface);
        myCountryNameText.setText(tempValues);
        return row;
    }

    private View getCollapsedView(int position, ViewGroup parent) {
        View row = inflater.inflate(R.layout.spinner_row_collapsed, parent, false);
        tempValues = null;
        tempValues = getItem(position).name;
        TextView myCountryCodeText = (TextView) row.findViewById(R.id.state);
        myCountryCodeText.setTypeface(Globals.typeface);
        myCountryCodeText.setText(tempValues);
        return row;
    }
}
