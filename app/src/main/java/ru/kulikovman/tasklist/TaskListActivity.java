package ru.kulikovman.tasklist;

import android.content.Context;
import android.os.Bundle;

import android.support.v7.widget.DividerItemDecoration;
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
import android.widget.LinearLayout;

import io.realm.OrderedRealmCollection;
import io.realm.Realm;
import io.realm.Sort;
import ru.kulikovman.tasklist.models.Task;
import ru.kulikovman.tasklist.models.TaskAdapter;

public class TaskListActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, TaskAdapter.OnItemClickListener {

    private Realm mRealm;
    private RecyclerView mRecyclerView;
    private TaskAdapter mAdapter;

    private Task mTask;
    private int mPosition = -1;

    private EditText mTaskField;
    private ImageButton mAddTask;

    private LinearLayout mTaskOptionsPanel;
    private ImageButton mSetDateButton, mSetPriorityButton, mSetGroupButton, mSetRepeatButton,
    mSetReminderButton, mDeleteButton;

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

        // Инициализируем базовые вью элементы
        mRecyclerView = (RecyclerView) findViewById(R.id.task_recycler_view);
        mTaskField = (EditText) findViewById(R.id.task_field);
        mAddTask = (ImageButton) findViewById(R.id.add_task_button);

        // Инициализируем вью элементы панели инструментов
        mTaskOptionsPanel = (LinearLayout) findViewById(R.id.task_options_panel);
        mSetDateButton = (ImageButton) findViewById(R.id.task_set_date_button);
        mSetPriorityButton = (ImageButton) findViewById(R.id.task_set_priority_button);
        mSetGroupButton = (ImageButton) findViewById(R.id.task_set_group_button);
        mSetRepeatButton = (ImageButton) findViewById(R.id.task_set_repeat_button);
        mSetReminderButton = (ImageButton) findViewById(R.id.task_set_reminder_button);
        mDeleteButton = (ImageButton) findViewById(R.id.task_delete_button);

        // Создаем и запускаем список
        setUpRecyclerView();

        // Нажатие по полю ввода
        mTaskField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                resetItemSelection();
            }
        });
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
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        // Слушатель для адаптера списка
        mAdapter.setOnItemClickListener(this);

        // Обработчик свайпов
        /*SwipeController swipeController = new SwipeController();
        ItemTouchHelper itemTouchhelper = new ItemTouchHelper(swipeController);
        itemTouchhelper.attachToRecyclerView(mRecyclerView);*/
    }

    private OrderedRealmCollection<Task> loadUnfinishedTasks() {
        return mRealm.where(Task.class)
                .equalTo(Task.DONE, false)
                .findAll()
                .sort(new String[]{Task.TARGET_DATE, Task.PRIORITY, Task.TITLE},
                        new Sort[]{Sort.ASCENDING, Sort.ASCENDING, Sort.ASCENDING});
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

    @Override
    public void onItemClick(View itemView, int position, Task task) {
        // Прячем клавиатуру
        hideKeyboard();

        // Определяем повторное нажатие по элементу
        if (position == mPosition) {
            resetItemSelection();
        } else {
            mTask = task;
            selectItem(position);
        }
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

            // Прячем клавиатуру и переходим к созданной задаче
            hideKeyboard();
            moveToItem(task.getId());
        }
    }

    private void moveToItem(long taskId) {
        // Получаем позицию и задачу
        int position = mAdapter.getPosition(taskId);
        mTask = mAdapter.getTaskById(taskId);

        // Скролим и выделяем
        mRecyclerView.scrollToPosition(position);
        selectItem(position);
    }

    public void deleteTask(View view) {
        mRealm.beginTransaction();
        mTask.deleteFromRealm();
        mRealm.commitTransaction();

        resetItemSelection();
    }

    private void hideKeyboard() {
        // Прячем клавиатуру
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }

        // Снимаем фокус с поля ввода
        mTaskField.clearFocus();
    }

    private void resetItemSelection() {
        mAdapter.resetSelection();
        mTaskOptionsPanel.setVisibility(View.INVISIBLE);
        mPosition = RecyclerView.NO_POSITION;
        mTask = null;
    }

    private void selectItem(int position) {
        mAdapter.selectItem(position);
        mPosition = position;

        // Показываем панель инструментов
        mTaskOptionsPanel.setVisibility(View.VISIBLE);

        // Настраиваем активность кнопок
        long date = mTask.getTargetDate();
        mSetRepeatButton.setEnabled(date != Long.MAX_VALUE);
        mSetReminderButton.setEnabled(date != Long.MAX_VALUE);
    }
}
