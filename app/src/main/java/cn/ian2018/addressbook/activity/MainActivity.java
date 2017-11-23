package cn.ian2018.addressbook.activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import cn.ian2018.addressbook.R;
import cn.ian2018.addressbook.db.MyDatabaseDao;
import cn.ian2018.addressbook.model.User;

public class MainActivity extends AppCompatActivity {

    private List<User> userList = new ArrayList<>();
    private ListView lv_content;
    private MyAdapter myAdapter;
    private MyDatabaseDao databaseDao;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        databaseDao = MyDatabaseDao.getInstance(getApplicationContext());

        initView();

        checkPermission();
    }

    private void initData() {
        // 判断是否是第一次打开应用
        SharedPreferences sharedPreferences = getSharedPreferences("config", Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sharedPreferences.edit();
        boolean first = sharedPreferences.getBoolean("first", true);
        if (first) {
            // 获取手机联系人
            getPhoneContent();
            edit.putBoolean("first", false).apply();
        } else {
            userList = databaseDao.getUsers();
            myAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        userList.clear();
        userList = databaseDao.getUsers();
        myAdapter.notifyDataSetChanged();
    }

    private void getPhoneContent() {
        showDialog();
        new Thread() {
            public void run() {
                // 获取内容解析器对象 调它的查询方法 几个参数分别为uri地址 查询的内容 根据什么去查 根据查的具体值 排序方式
                Cursor cursor = getContentResolver().query(Uri.parse("content://com.android.contacts/raw_contacts"), new String[]{"contact_id"}, null, null, null);
                // 循环游标 直至没有数据
                while (cursor.moveToNext()) {
                    String id = cursor.getString(0);
                    Cursor indexCursor = getContentResolver().query(Uri.parse("content://com.android.contacts/data"),
                            new String[]{"data1", "mimetype"},
                            "raw_contact_id=?", new String[]{id}, null);
                    User user = new User();
                    // 循环游标
                    while (indexCursor.moveToNext()) {
                        String data = indexCursor.getString(0);
                        String type = indexCursor.getString(1);
                        if ("vnd.android.cursor.item/name".equals(type)) {
                            String name = data;
                            if (name != null) {
                                user.setName(name);
                            }
                        } else if ("vnd.android.cursor.item/phone_v2".equals(type)) {
                            String phone = data;
                            if (phone != null) {
                                user.setPhone(phone);
                            }
                        }
                    }
                    indexCursor.close();
                    long userId = databaseDao.saveUser(user);
                    user.setId((int)userId);
                    userList.add(user);
                }
                cursor.close();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeDialog();
                        myAdapter.notifyDataSetChanged();
                    }
                });
            }
        }.start();
    }

    private void initView() {
        lv_content = (ListView) findViewById(R.id.lv_content);
        myAdapter = new MyAdapter();
        lv_content.setAdapter(myAdapter);

        lv_content.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // 跳转到修改页面
                Intent intent = new Intent(MainActivity.this, ContentInfoActivity.class);
                intent.putExtra("type",1);
                intent.putExtra("user",userList.get(position));
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            // 添加联系人
            case R.id.action_add:
                Intent intent = new Intent(this, ContentInfoActivity.class);
                intent.putExtra("type",0);
                startActivity(intent);
                return true;
            // 搜索
            case R.id.action_search:
                startActivity(new Intent(this,SearchActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // listview适配器
    class MyAdapter extends BaseAdapter {

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
                convertView = LayoutInflater.from(MainActivity.this).inflate(R.layout.item_content, parent, false);
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
            android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(MainActivity.this);
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
                        myAdapter.notifyDataSetChanged();
                        Toast.makeText(MainActivity.this,"删除成功",Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MainActivity.this,"删除失败",Toast.LENGTH_SHORT).show();
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

    // 检查权限
    private void checkPermission() {
        List<String> permissionList = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.CALL_PHONE);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.SEND_SMS);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.READ_CONTACTS);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.WRITE_CONTACTS);
        }

        if (!permissionList.isEmpty()) {
            String[] permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(this, permissions, 1);
        } else {
            initData();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0) {
                    for (int grantResult : grantResults) {
                        if (grantResult != PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(MainActivity.this, "请您同意所有权限再使用", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }
                    initData();
                } else {
                    Toast.makeText(MainActivity.this, "出了个小错误", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
        }
    }

    private void showDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
        }
        progressDialog.setMessage("加载中...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
    }

    private void closeDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }
}
