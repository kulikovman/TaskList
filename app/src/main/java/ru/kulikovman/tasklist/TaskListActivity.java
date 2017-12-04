package ru.kulikovman.tasklist;

import android.content.Context;
import android.os.Bundle;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;

import io.realm.OrderedRealmCollection;
import io.realm.Realm;
import io.realm.Sort;
import ru.kulikovman.tasklist.models.Task;
import ru.kulikovman.tasklist.models.TaskAdapter;

public class TaskListActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private Realm mRealm;
    private RecyclerView mRecyclerView;
    private TaskAdapter mAdapter;

    private EditText mTaskField;
    private ImageButton mAddTask;
    private ImageButton mSetDateBtn, mSetPriorityBtn, mSetGroupBtn, mSetRepeatBtn, mSetReminderBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Подключаем базу данных
        Realm.init(this);
        mRealm = Realm.getDefaultInstance();

        // Инициализируем необходимые вью элементы
        mRecyclerView = (RecyclerView) findViewById(R.id.task_recycler_view);
        mTaskField = (EditText) findViewById(R.id.task_field);
        mAddTask = (ImageButton) findViewById(R.id.add_task_button);

        // Создаем и запускаем список
        setUpRecyclerView();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRecyclerView.setAdapter(null);
        mRealm.close();
    }

    private void setUpRecyclerView() {
        mAdapter = new TaskAdapter(this, loadUnfinishedTasks());
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setHasFixedSize(true);

        // Слушатель для адаптера списка
        //mAdapter.setOnItemClickListener(this);

        // Обработчик свайпов
        /*TouchHelperCallback touchHelperCallback = new TouchHelperCallback();
        ItemTouchHelper touchHelper = new ItemTouchHelper(touchHelperCallback);
        touchHelper.attachToRecyclerView(mRecyclerView);*/
    }

    private OrderedRealmCollection<Task> loadUnfinishedTasks() {
        return mRealm.where(Task.class)
                .equalTo(Task.DONE, false)
                .findAll()
                .sort(new String[]{Task.TARGET_DATE, Task.PRIORITY, Task.TITLE},
                        new Sort[] {Sort.ASCENDING, Sort.ASCENDING, Sort.ASCENDING});
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.task_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void addTask(View view) {
        String taskTitle = mTaskField.getText().toString().trim();

        if (taskTitle.length() > 0) {
            // Создаем задачу и добавляем в базу
            Task task = new Task(taskTitle);

            mRealm.beginTransaction();
            mRealm.insert(task);
            mRealm.commitTransaction();

            // Очищаем поле
            mTaskField.setText(null);

            // Прячем клавиатуру
            View v = this.getCurrentFocus();
            if (v != null) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }

            // Выделяем созданную задачу и перемещаемся к ней
            // ...
        }
    }
}
