package com.rapidbizapps.newapp.fragments;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.rapidbizapps.newapp.R;
import com.rapidbizapps.newapp.SelectMediaActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;



/**
 * A simple {@link Fragment} subclass.
 */
public class ChildFragment extends Fragment implements View.OnClickListener {
    static String BUNDLE_PATH = "path";
    private RecyclerView rvChild;
    private Toolbar toolbar;
    private View view;
    private Activity activity;
    private List<String> cList = new ArrayList<>();
    private ChildAdapter adapter;
    private TextView title;
    private ImageView ivSelected;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (Activity) context;
    }

    public ChildFragment() {
        // Required empty public constructor
    }

    public static ChildFragment instance(String path) {
        Bundle b = new Bundle();
        b.putString(BUNDLE_PATH, path);
        ChildFragment childFragment = new ChildFragment();
        childFragment.setArguments(b);
        return childFragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_chaild, container, false);
        initContols();
        return view;
    }

    private void initContols() {
        rvChild = (RecyclerView) view.findViewById(R.id.rv_child);
        rvChild.setLayoutManager(new GridLayoutManager(activity, 3));
        adapter = new ChildAdapter(activity);
        rvChild.setAdapter(adapter);
        toolbar = (Toolbar) view.findViewById(R.id.toolbar_child);
        title = (TextView) view.findViewById(R.id.title);
        ivSelected = (ImageView) view.findViewById(R.id.iv_selected_child);
        ivSelected.setOnClickListener(this);

        String[] str = getArguments().getString(BUNDLE_PATH).split("/");
        title.setText(str[str.length - 1]);

        adapter.setList(geFiles(new File(getArguments().getString(BUNDLE_PATH))));
    }

    @Override
    public void onClick(View v) {
        int i1 = v.getId();
        if (i1 == R.id.iv_selected_child) {
            if (adapter.geSelectedPaths().size() > 0) {
                Intent i = activity.getIntent();
                i.putStringArrayListExtra(SelectMediaActivity.INTENTFILES, (ArrayList<String>) adapter.geSelectedPaths());
                activity.setResult(Activity.RESULT_OK, i);
                activity.finish();
            }

        }
    }


    class ChildAdapter extends RecyclerView.Adapter<ChildAdapter.Holder> {
        private List<String> list = new ArrayList<>();
        private List<String> selectedList = new ArrayList<>();
        private Context mContext;

        ChildAdapter(Context context) {
            mContext = context;
        }

        @Override
        public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new Holder(LayoutInflater.from(mContext).inflate(R.layout.item_child, parent, false));
        }

        @Override
        public void onBindViewHolder(Holder holder, int position) {
            final String row = list.get(position);
            Glide.with(mContext)
                    .load(Uri.fromFile(new File(row)))
                    .into(holder.ivImg);
            if (selectedList.contains(String.valueOf(position))) {
                holder.ivSelected.setImageResource(R.mipmap.ic_check);
                holder.ivSelected.setBackgroundColor(Color.parseColor("#4c000000"));
            } else {
                holder.ivSelected.setImageBitmap(null);
                holder.ivSelected.setBackgroundColor(Color.TRANSPARENT);
            }
            holder.ivImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String i = String.valueOf(list.indexOf(row));
                    if (selectedList.contains(i)) {
                        selectedList.remove(i);
                        notifyDataSetChanged();
                    } else if (selectedList.size() < 3) {
                        selectedList.add(i);
                        notifyDataSetChanged();
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        public void setList(List<String> pList) {
            this.list = pList;
            notifyDataSetChanged();
        }

        public List<String> geSelectedPaths() {
            //it gives positions converting them to paths
            List<String> list = new ArrayList<>();
            for (String str : selectedList) {
                list.add(this.list.get(Integer.parseInt(str)));
            }
            return list;
        }

        class Holder extends RecyclerView.ViewHolder {
            ImageView ivImg, ivSelected;

            public Holder(View itemView) {
                super(itemView);
                ivImg = (ImageView) itemView.findViewById(R.id.iv_img_child);
                ivSelected = (ImageView) itemView.findViewById(R.id.iv_imgSlected_child);
            }
        }

    }


    public List<String> geFiles(File dir) {
        File listFile[] = dir.listFiles();
        if (listFile != null && listFile.length > 0) {
            for (File file : listFile) {

                if (file.getName().endsWith(".png")
                        || file.getName().endsWith(".jpg")
                        || file.getName().endsWith(".jpeg")
                        || file.getName().endsWith(".bmp")
                        || file.getName().endsWith(".webp")) {
                    cList.add(file.getAbsolutePath());
                }

            }
        }
        return cList;
    }


}
