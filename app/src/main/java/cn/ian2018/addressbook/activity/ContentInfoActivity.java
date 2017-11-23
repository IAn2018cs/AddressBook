package cn.ian2018.addressbook.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import cn.ian2018.addressbook.R;
import cn.ian2018.addressbook.db.MyDatabaseDao;
import cn.ian2018.addressbook.model.User;

public class ContentInfoActivity extends AppCompatActivity {

    private int type;
    private EditText et_name;
    private EditText et_phone;
    private EditText et_qq;
    private EditText et_email;
    private EditText et_address;
    private Button bt_submit;
    private String oldName;
    private String oldPhone = "";
    private String oldQQ = "";
    private String oldEmail = "";
    private String oldAddress = "";
    private User mUser;
    private MyDatabaseDao databaseDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content_info);

        databaseDao = MyDatabaseDao.getInstance(getApplicationContext());
        // 获取页面类型
        type = getIntent().getIntExtra("type", -1);

        // 初始化控件
        initView();

        // 如果是修改活动  就将数据填充
        if (type == 1) {
            mUser = (User) getIntent().getSerializableExtra("user");
            initData(mUser);
        }

        // 按钮控制
        control();
    }

    private void initView() {
        et_name = (EditText) findViewById(R.id.et_name);
        et_phone = (EditText) findViewById(R.id.et_phone);
        et_qq = (EditText) findViewById(R.id.et_qq);
        et_email = (EditText) findViewById(R.id.et_email);
        et_address = (EditText) findViewById(R.id.et_address);

        bt_submit = (Button) findViewById(R.id.bt_submit);
    }

    private void initData(User user) {
        et_name.setText(user.getName());
        et_phone.setText(user.getPhone());
        et_qq.setText(user.getQQ());
        et_email.setText(user.getEmail());
        et_address.setText(user.getAddress());

        switch (type) {
            case 0:
                bt_submit.setText("添加联系人");
                break;
            case 1:
                bt_submit.setText("修改联系人");
                break;
        }

        oldName = user.getName();
        oldPhone = user.getPhone();
        oldQQ = user.getQQ();
        oldEmail = user.getEmail();
        oldAddress = user.getAddress();
    }

    private void control() {
        bt_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = et_name.getText().toString().trim();
                String phone = et_phone.getText().toString().trim();
                String qq = et_qq.getText().toString().trim();
                String email = et_email.getText().toString().trim();
                String address = et_address.getText().toString().trim();

                switch (type) {
                    // 添加联系人
                    case 0:
                        if (!name.equals("") && !phone.equals("") && !qq.equals("") && !email.equals("") && !address.equals("")) {
                            User user = new User(name, phone, qq, email, address);

                            long id = databaseDao.saveUser(user);

                            if (id > 0) {
                                Toast.makeText(getApplicationContext(), "添加成功", Toast.LENGTH_SHORT).show();
                                et_name.setText("");
                                et_phone.setText("");
                                et_qq.setText("");
                                et_email.setText("");
                                et_address.setText("");
                            } else {
                                Toast.makeText(getApplicationContext(), "添加失败", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), "请将信息填写完整", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    // 修改活动
                    case 1:
                        if (!name.equals(oldName) || !phone.equals(oldPhone) || !qq.equals(oldQQ) || !email.equals(oldEmail) || !address.equals(oldAddress)) {
                            User user = new User(name,phone,qq,email,address);
                            user.setId(mUser.getId());
                            boolean isChange = databaseDao.updateUser(user);
                            if (isChange) {
                                Toast.makeText(getApplicationContext(),"修改成功",Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getApplicationContext(),"修改失败",Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(getApplicationContext(),"您没有做任何修改",Toast.LENGTH_SHORT).show();
                        }
                        break;
                }
            }
        });
    }
}
