package cn.ian2018.addressbook.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import cn.ian2018.addressbook.R;
import cn.ian2018.addressbook.db.MyDatabaseDao;
import cn.ian2018.addressbook.model.User;

public class SearchActivity extends AppCompatActivity {

    private EditText et_name;
    private ImageView iv_search;
    private MyDatabaseDao databaseDao;
    private MyAdapter mAdapter;
    private ListView lv_content;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        databaseDao = MyDatabaseDao.getInstance(this);

        initView();
    }

    private void initView() {
        et_name = (EditText) findViewById(R.id.et_name);
        iv_search = (ImageView) findViewById(R.id.iv_search);

        lv_content = (ListView) findViewById(R.id.lv_content);

        iv_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = et_name.getText().toString().trim();
                if (!name.equals("")) {
                    List<User> userList = databaseDao.getUsersForName(name);
                    if (userList.size() == 0) {
                        Toast.makeText(getApplicationContext(),"没有该联系人",Toast.LENGTH_SHORT).show();
                    } else {
                        setData(userList);
                    }
                } else {
                    Toast.makeText(getApplicationContext(),"请先输入姓名哦",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // 设置数据
    private void setData(final List<User> userList) {
        mAdapter = new MyAdapter(userList);
        lv_content.setAdapter(mAdapter);
        lv_content.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // 跳转到修改页面
                Intent intent = new Intent(getApplicationContext(), ContentInfoActivity.class);
                intent.putExtra("type",1);
                intent.putExtra("user",userList.get(position));
                startActivity(intent);
            }
        });
    }

    // listview适配器
    class MyAdapter extends BaseAdapter {
        private List<User> userList;

        public MyAdapter(List<User> userList){
            this.userList = userList;
        }

        @Override
        public int getCount() {
            return userList.size();
        }

        @Override
        public User getItem(int position) {
            return userList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder = null;
            if (convertView == null) {
                convertView = LayoutInflater.from(SearchActivity.this).inflate(R.layout.item_content, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
                viewHolder.tv_phone = (TextView) convertView.findViewById(R.id.tv_phone);
                viewHolder.iv_call = (ImageView) convertView.findViewById(R.id.iv_call);
                viewHolder.iv_message = (ImageView) convertView.findViewById(R.id.iv_message);
                viewHolder.iv_delete = (ImageView) convertView.findViewById(R.id.iv_delete);
                convertView.setTag(viewHolder);
            }
            viewHolder = (ViewHolder) convertView.getTag();
            viewHolder.tv_name.setText(getItem(position).getName());
            viewHolder.tv_phone.setText(getItem(position).getPhone());
            final User user = getItem(position);

            // 打电话
            viewHolder.iv_call.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 跳转到拨号界面
                    Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"+user.getPhone()));
                    startActivity(intent);
                }
            });

            // 发短信
            viewHolder.iv_message.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //定义一个Intent，让它能跳转到发短信的界面
                    Uri uri = Uri.parse("smsto:"+user.getPhone());
                    Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
                    //设置短信的内容
                    intent.putExtra("sms_body", "");
                    startActivity(intent);
                }
            });

            // 删除
            viewHolder.iv_delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showConfirmDialog(user);
                }
            });

            return convertView;
        }

        // 显示确认对话框
        protected void showConfirmDialog(final User user) {
            android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(SearchActivity.this);
            // 设置对话框左上角图标
            builder.setIcon(R.mipmap.ic_launcher2);
            // 设置不能取消
            builder.setCancelable(false);
            // 设置对话框标题
            builder.setTitle("删除联系人");
            // 设置对话框内容
            builder.setMessage("您确认删除该联系人？");
            // 设置积极的按钮
            builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // 删除联系人
                    boolean isDelete = databaseDao.deleteUser(user.getId());
                    if (isDelete) {
                        userList.remove(user);
                        mAdapter.notifyDataSetChanged();
                        Toast.makeText(getApplicationContext(),"删除成功",Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(),"删除失败",Toast.LENGTH_SHORT).show();
                    }
                    dialog.dismiss();
                }
            });
            // 设置消极的按钮
            builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            builder.show();
        }
    }

    static class ViewHolder{
        TextView tv_name;
        TextView tv_phone;
        ImageView iv_call;
        ImageView iv_message;
        ImageView iv_delete;
    }
}
