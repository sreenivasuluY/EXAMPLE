package com.rapidbizapps.newapp.fragments;


import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.rapidbizapps.newapp.GalleryActivity;
import com.rapidbizapps.newapp.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;



/**
 * A simple {@link Fragment} subclass.
 */
public class ParentFragment extends Fragment {
    private RecyclerView rvParent;
    private Toolbar toolbar;
    private View view;
    private Activity activity;
    private ParentAdapter adapter;
    private List<String> pList = new ArrayList<>();
    private List<String> totalImagesList = new ArrayList<>();

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (Activity) context;
    }

    public ParentFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_parent, container, false);
        initControls();
        return view;
    }

    private void initControls() {
        rvParent = (RecyclerView) view.findViewById(R.id.rv_parent);
        toolbar = (Toolbar) view.findViewById(R.id.toolbae_parent);
        toolbar.setTitle("Gallery");
        ((GalleryActivity) activity).setSupportActionBar(toolbar);
        rvParent.setLayoutManager(new GridLayoutManager(activity, 2));
        adapter = new ParentAdapter(activity, new ClickListener() {
            @Override
            public void click(String path) {
                ((GalleryActivity) activity).addFragment(ChildFragment.instance(path));
            }
        });
        rvParent.setAdapter(adapter);

        //get total ImageList
        totalImagesList = getFile(new File(Environment.getExternalStorageDirectory().toString()));
        Log.e("Size", String.valueOf(totalImagesList.size()));
        removeDuplicates();
        Log.e("Size", String.valueOf(pList.size()));
        adapter.setList(pList);
    }

    private void removeDuplicates() {
        TreeSet<String> ts = new TreeSet();
        ts.addAll(pList);
        pList.clear();
        pList.addAll(ts);
    }

    public String getFirsFile(File dir) {
        String str = "";
        File listFile[] = dir.listFiles();
        if (listFile != null && listFile.length > 0) {
            for (File file : listFile) {

                if (file.getName().endsWith(".png")
                        || file.getName().endsWith(".jpg")
                        || file.getName().endsWith(".jpeg")
                        || file.getName().endsWith(".bmp")
                        || file.getName().endsWith(".webp")) {
                    str = file.getAbsolutePath();
                    break;
                }

            }
        }
        return str;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            Toast.makeText(activity, "ffds", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(menuItem);
    }

    class ParentAdapter extends RecyclerView.Adapter<ParentAdapter.Holder> {
        private List<String> list = new ArrayList<>();
        private Context mContext;
        private ClickListener listener;

        ParentAdapter(Context context, ClickListener listener) {
            mContext = context;
            this.listener = listener;
        }

        @Override
        public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new Holder(LayoutInflater.from(mContext).inflate(R.layout.item_parent, parent, false));
        }

        @Override
        public void onBindViewHolder(Holder holder, int position) {
            final String row = list.get(position);
            String[] str = row.split("/");
            holder.tvName.setText(str[str.length - 1]);
            Glide.with(mContext)
                    .load(Uri.fromFile(new File(getFirsFile(new File(row)))))
                    .into(holder.ivImg);
            holder.ivImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.click(list.get(list.indexOf(row)));
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

        class Holder extends RecyclerView.ViewHolder {
            TextView tvName;
            ImageView ivImg;

            public Holder(View itemView) {
                super(itemView);
                ivImg = (ImageView) itemView.findViewById(R.id.iv_img_item_parent);
                tvName = (TextView) itemView.findViewById(R.id.tv_name_itme_parent);
            }
        }

    }

    interface ClickListener {
        void click(String path);
    }

    public List<String> getFile(File dir) {
        File listFile[] = dir.listFiles();
        if (listFile != null && listFile.length > 0) {
            for (File file : listFile) {
                if (file.isDirectory()) {
                    String[] str = file.getAbsolutePath().split("/");
                    if (!str[str.length - 1].startsWith("."))
                        getFile(file);
                } else {
                    if (file.getName().endsWith(".png")
                            || file.getName().endsWith(".jpg")
                            || file.getName().endsWith(".jpeg")
                            || file.getName().endsWith(".bmp")
                            || file.getName().endsWith(".webp")) {
                        if (!totalImagesList.contains(file.getAbsolutePath())) {
                            totalImagesList.add(file.getAbsolutePath());

                            String[] str = file.getAbsolutePath().split("/");
                            String imgCat = "";
                            for (int i = 0; i < str.length - 1; i++)
                                imgCat = imgCat + str[i] + "/";
                            pList.add(imgCat);
                        }
                    }
                }
            }
        }
        return totalImagesList;
    }


}
