package com.carlchenxq.zidong;

import android.app.Dialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends AppCompatActivity {


    /**
     * global varies
     */

    //init arrayList to save the information of the dishes
    private ArrayList<Menu> menuInfo = new ArrayList<>();
    private ArrayList<Drink> drinkInfo = new ArrayList<>();
    //the flags of current random item and the precious random item
    private int flag;
    private int preFlag = 0;
    private int drinkOrMenuFlag = 0;  //0.menu 1.drink
    private int cateFlag = 0; //0.menu 1.drink
    //the global Views
    private static Dialog bottomDialog = null;
    private EditText edit_name = null;
    private EditText edit_loc = null;
    private TextView nameTextView = null;
    private TextView locTextView = null;
    private TextView hintTextView = null;
    private TextView cateTextView = null;
    private int easterEgg = 0;
    private ConstraintLayout parent;
    private BottomNavigationView navView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);

        parent = findViewById(R.id.container);
        navView = findViewById(R.id.nav_view);


        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);
        initDB();
    }


    /**
     * 初始化ArrayList和DataBase
     */
    public void initDB() {
        DatabaseHelper helper = new DatabaseHelper(this);
        SQLiteDatabase db = helper.getWritableDatabase();

        Cursor cursor1 = db.query("menu", null, null, null, null, null, null);
        while (cursor1.moveToNext()) {
            //String _id = cursor.getString(cursor.getColumnIndex("_id"));
            String tempName = cursor1.getString(cursor1.getColumnIndex("name"));
            String tempLoc = cursor1.getString(cursor1.getColumnIndex("loc"));
            Menu tempMenu = new Menu();
            tempMenu.setName(tempName);
            tempMenu.setLoc(tempLoc);
            menuInfo.add(tempMenu);
        }

        Cursor cursor2 = db.query("drink", null, null, null, null, null, null);
        while (cursor2.moveToNext()) {
            //String _id = cursor.getString(cursor.getColumnIndex("_id"));
            String tempName = cursor2.getString(cursor2.getColumnIndex("name"));
            String tempLoc = cursor2.getString(cursor2.getColumnIndex("loc"));
            Drink tempDrink = new Drink();
            tempDrink.setName(tempName);
            tempDrink.setLoc(tempLoc);
            drinkInfo.add(tempDrink);
        }
    }

    /**
     * 完成添加按钮实现的操作
     */
    public void add(View view) {
        //初始化两个EditTExt对象
        edit_name = (EditText) findViewById(R.id.edit_name);
        edit_loc = (EditText) findViewById(R.id.edit_loc);
        showSnakeBar("添加成功，去看看吧！");
        if (edit_name.getText().length() == 0 || edit_loc.getText().length() == 0) {

            showSnakeBar("上面的信息得填完整哈");

            return;

        }


        //获取EditText控件中的值并存储到String对象中
        String tempName = edit_name.getText().toString().trim();
        String tempLoc = edit_loc.getText().toString().trim();

        if(cateFlag == 0) {
            //创建一个中间Menu对象，用来暂存获取到的值
            Menu tempMenu = new Menu();
            tempMenu.setName(tempName);
            tempMenu.setLoc(tempLoc);

            //将暂存的Menu对象添加到ArrayList中
            menuInfo.add(tempMenu);

            //将新添加的项写入数据库
            DatabaseHelper helper = new DatabaseHelper(this);
            SQLiteDatabase db = helper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("name", tempMenu.getName());
            values.put("loc", tempMenu.getLoc());
            db.insert("menu", null, values);
            cateFlag = 0;
        }

        if(cateFlag == 1) {
            //创建一个中间Drink对象，用来暂存获取到的值
            Drink tempDrink = new Drink();
            tempDrink.setName(tempName);
            tempDrink.setLoc(tempLoc);

            //将暂存的Menu对象添加到ArrayList中
            drinkInfo.add(tempDrink);

            //将新添加的项写入数据库
            DatabaseHelper helper = new DatabaseHelper(this);
            SQLiteDatabase db = helper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("name", tempDrink.getName());
            values.put("loc", tempDrink.getLoc());
            db.insert("drink", null, values);
            cateFlag = 0;
            cateTextView.setText("吃的");
        }

        //最后清空输入框内容
        edit_name.setText("");
        edit_loc.setText("");

    }

    /**
     * 显示SnackBar
     * @param text: 显示的内容
     */
    private void showSnakeBar(String text) {
        Snackbar.make(parent, text, Snackbar.LENGTH_SHORT)
                .setAnchorView(navView)
                .show();
    }

    /**
     * 完成随机选择按钮实现的操作
     */
    public void choose(View view) {

        nameTextView = (TextView) findViewById(R.id.text_dashboard_name);
        locTextView = (TextView) findViewById(R.id.text_dashboard_loc);
        hintTextView = (TextView) findViewById(R.id.test_text_view);
        int pressedBtn = view.getId();
        int arrayListSize = 0;

        if (pressedBtn == R.id.button_start_random) {
            arrayListSize = menuInfo.size();
            if (arrayListSize == 0) {

                showSnakeBar("TMD，烦死了，什么都没有");
                return;

            }

            random(arrayListSize);
            nameTextView.setText(menuInfo.get(flag).getName());
            locTextView.setText(menuInfo.get(flag).getLoc());
            drinkOrMenuFlag = 0;

        }

        if (pressedBtn == R.id.button_start_random_drink) {
            arrayListSize = drinkInfo.size();
            if (arrayListSize == 0) {

                showSnakeBar("TMD，烦死了，什么都没有");
                return;

            }
            random(arrayListSize);
            nameTextView.setText(drinkInfo.get(flag).getName());
            locTextView.setText(drinkInfo.get(flag).getLoc());
            drinkOrMenuFlag = 1;
        }

        //set a hint
        hintTextView.setText("不喜欢？点击删除！");
        preFlag = flag;

        showSnakeBar("提示：一次就好，冲多了就没意义了");

    }

    /**
     * method to random choose
     */
    public void random(int seed) {
        Random random = new Random();
        flag = random.nextInt(seed);
        if (flag == preFlag && seed != 1) {
            random(seed);
        }
    }


    /**
     * the method to awake the dialog
     */
    public void deleteOnClick(View view) {
        bottomDialog = new Dialog(this, R.style.BottomDialog);
        View contentView = LayoutInflater.from(this).inflate(R.layout.dialog_content_normal, null);
        bottomDialog.setContentView(contentView);
        ViewGroup.LayoutParams layoutParams = contentView.getLayoutParams();
        layoutParams.width = getResources().getDisplayMetrics().widthPixels;
        contentView.setLayoutParams(layoutParams);
        bottomDialog.getWindow().setGravity(Gravity.BOTTOM);
        bottomDialog.getWindow().setWindowAnimations(R.style.BottomDialog_Animation);
        bottomDialog.show();
    }

    /**
     * the method for category
     * */
    public void cateOnClick(View view) {
        bottomDialog = new Dialog(this, R.style.BottomDialog);
        View contentView = LayoutInflater.from(this).inflate(R.layout.dialog_category, null);
        bottomDialog.setContentView(contentView);
        ViewGroup.LayoutParams layoutParams = contentView.getLayoutParams();
        layoutParams.width = getResources().getDisplayMetrics().widthPixels;
        contentView.setLayoutParams(layoutParams);
        bottomDialog.getWindow().setGravity(Gravity.BOTTOM);
        bottomDialog.getWindow().setWindowAnimations(R.style.BottomDialog_Animation);
        bottomDialog.show();
    }

    /**
     * method to set category
     * */

    public void setCate(View view) {
        int pressedBtn = view.getId();
        cateTextView = (TextView) findViewById(R.id.text_category);
        if(pressedBtn == R.id.cate_menu) {
            cateFlag = 0;
            cateTextView.setText("吃的");
            bottomDialog.cancel();
        }
        if(pressedBtn == R.id.cate_drink) {
            cateFlag = 1;
            cateTextView.setText("喝的");
            bottomDialog.cancel();
        }
    }

    /**
     * the method to delete the selected item of the ArrayList
     */
    public void delete(View view) {

        DatabaseHelper helper = new DatabaseHelper(this);
        SQLiteDatabase db = helper.getWritableDatabase();

        if (drinkOrMenuFlag == 0) {
            Menu tempMenu = menuInfo.get(flag);
            String sql = "delete from menu where name='" + tempMenu.getName() + "'";
            menuInfo.remove(flag);
            db.execSQL(sql);
        }

        if (drinkOrMenuFlag == 1) {
            Drink tempDrink = drinkInfo.get(flag);
            String sql = "delete from drink where name='" + tempDrink.getName() + "'";
            drinkInfo.remove(flag);
            db.execSQL(sql);
        }

        nameTextView.setText("[已删除]");
        locTextView.setText(" ");
        hintTextView.setText("");
        bottomDialog.cancel();

        Snackbar.make(parent, "已删除", Snackbar.LENGTH_LONG)
                .setAnchorView(navView)
                .show();

    }

    /**
     * the method to cancel the dialog
     */

    public void cancelDialog(View view) {
        bottomDialog.cancel();
    }

    public void onClickEasterEgg(View view) {
        if(easterEgg < 10) {
            easterEgg++;
        }
        else {
            easterEgg = 0;
            showSnakeBar("突然出现！0x0");
        }
    }

}
