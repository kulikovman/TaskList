package ru.kulikovman.tasklist;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import android.support.v4.app.DialogFragment;
import android.support.v4.app.NavUtils;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
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
import ru.kulikovman.tasklist.dialogs.DateDialog;
import ru.kulikovman.tasklist.dialogs.EditTaskDialog;
import ru.kulikovman.tasklist.dialogs.GroupDialog;
import ru.kulikovman.tasklist.dialogs.PriorityDialog;
import ru.kulikovman.tasklist.dialogs.RepeatDialog;
import ru.kulikovman.tasklist.models.Group;
import ru.kulikovman.tasklist.models.Task;
import ru.kulikovman.tasklist.models.TaskAdapter;

public class TaskListActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, TaskAdapter.OnItemClickListener,
        CallbackDialogFragment.CallbackDialogListener {

    private Realm mRealm;
    public RecyclerView mRecyclerView;
    private TaskAdapter mAdapter;
    private String LOG = "log";

    private Task mTask;
    private int mPosition = -1;

    private EditText mTaskField;
    private LinearLayout mTaskOptionsPanel;
    private ImageButton mSetRepeatButton, mSetReminderButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_list);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Подключаем базу данных
        Realm.init(this);
        mRealm = Realm.getDefaultInstance();

        // Инициализируем вью элементы
        mRecyclerView = findViewById(R.id.task_recycler_view);
        mTaskField = findViewById(R.id.task_field);
        mTaskOptionsPanel = findViewById(R.id.task_options_panel);
        mSetRepeatButton = findViewById(R.id.task_set_repeat_button);
        mSetReminderButton = findViewById(R.id.task_set_reminder_button);

        // Создаем и запускаем список
        setUpRecyclerView();

        // Смена фокуса поля ввода
        mTaskField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                resetItemSelection();
            }
        });

        Log.d(LOG, "Завершен onCreate в TaskListActivity");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(LOG, "Запущен onResume в TaskListActivity");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(LOG, "Запущен onPause в TaskListActivity");
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
        initSwipe();
    }

    private OrderedRealmCollection<Task> loadUnfinishedTasks() {
        return mRealm.where(Task.class)
                .equalTo(Task.DONE, false)
                .findAll()
                .sort(new String[]{Task.TARGET_DATE, Task.PRIORITY, Task.TITLE},
                        new Sort[]{Sort.ASCENDING, Sort.ASCENDING, Sort.ASCENDING});
    }

    private void initSwipe() {
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
                if (viewHolder != null) {
                    // Получаем сдвигаемый элемент
                    View itemView = viewHolder.itemView;

                    // Снимаем выделение, если оно есть
                    if (itemView.isSelected()) {
                        itemView.setSelected(false);
                        mPosition = RecyclerView.NO_POSITION;
                    } else if (mPosition != -1) {
                        resetItemSelection();
                    }

                    // Скрываем панель инструментов
                    mTaskOptionsPanel.setVisibility(View.INVISIBLE);
                }

                super.onSelectedChanged(viewHolder, actionState);
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                // Получаем позицию и задачу
                mPosition = viewHolder.getAdapterPosition();
                mTask = mAdapter.getTaskByPosition(mPosition);

                // Открываем транзакцию
                mRealm.beginTransaction();

                // Понижаем счетчик задач в связанной группе
                Group group = mTask.getGroup();
                if (group != null) {
                    group.decreaseCountTask();
                }

                // Завершаем или удаляем
                if (direction == ItemTouchHelper.RIGHT) {
                    mTask.deleteFromRealm();
                    Log.d(LOG, "Задача удалена");
                } else {
                    mTask.setCompletionDate(System.currentTimeMillis());
                    mTask.setDone(true);
                    Log.d(LOG, "Задача завершена");
                }

                // Закрываем транзакцию
                mRealm.commitTransaction();

                // Обнуляем переменные
                mPosition = RecyclerView.NO_POSITION;
                mTask = null;
            }

            @Override
            public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                    View itemView = viewHolder.itemView;

                    float height = itemView.getBottom() - itemView.getTop();

                    Paint p = new Paint();
                    Bitmap icon;

                    if (dX > 0) {
                        // Цвет и размер фона
                        p.setColor(ResourcesCompat.getColor(getResources(), R.color.red, null));
                        RectF background = new RectF((float) itemView.getLeft(), (float) itemView.getTop(), dX, (float) itemView.getBottom());
                        c.drawRect(background, p);

                        // Иконка и ее расположение
                        Drawable d = getResources().getDrawable(R.drawable.ic_delete_white_24dp);
                        icon = drawableToBitmap(d);

                        int iconWidth = icon.getWidth();
                        int iconHeight = icon.getHeight();

                        float leftPosition = iconWidth;
                        float rightPosition = leftPosition + iconWidth;
                        float topPosition = itemView.getTop() + ((height - iconHeight) / 2);
                        float bottomPosition = topPosition + iconHeight;

                        RectF iconDest = new RectF(leftPosition, topPosition, rightPosition, bottomPosition);
                        c.drawBitmap(icon, null, iconDest, p);

                    } else {
                        // Цвет и размер фона
                        p.setColor(ResourcesCompat.getColor(getResources(), R.color.green, null));
                        RectF background = new RectF((float) itemView.getRight() + dX, (float) itemView.getTop(), (float) itemView.getRight(), (float) itemView.getBottom());
                        c.drawRect(background, p);

                        // Иконка и ее расположение
                        Drawable d = getResources().getDrawable(R.drawable.ic_done_white_24dp);
                        icon = drawableToBitmap(d);

                        int iconWidth = icon.getWidth();
                        int iconHeight = icon.getHeight();

                        float rightPosition = itemView.getRight() - iconWidth;
                        float leftPosition = rightPosition - iconWidth;
                        float topPosition = itemView.getTop() + ((height - iconHeight) / 2);
                        float bottomPosition = topPosition + iconHeight;

                        RectF iconDest = new RectF(leftPosition, topPosition, rightPosition, bottomPosition);
                        c.drawBitmap(icon, null, iconDest, p);
                    }

                    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                }
            }

            @Override
            public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                super.clearView(recyclerView, viewHolder);
            }

            public Bitmap drawableToBitmap(Drawable drawable) {
                if (drawable instanceof BitmapDrawable) {
                    return ((BitmapDrawable) drawable).getBitmap();
                }

                Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                drawable.draw(canvas);

                return bitmap;
            }
        };

        // Присоединяем всю эту конструкцию к нашему mRecyclerView
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(mRecyclerView);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
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

        if (id == R.id.nav_all_task) {
            /*NavUtils.navigateUpFromSameTask(this);
            return true;*/
        } else if (id == R.id.nav_group_list) {
            Intent intent = new Intent(this, GroupListActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_finished_task) {
            Intent intent = new Intent(this, FinishedListActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_fghdfhg) {

        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onItemClick(int position, Task task) {
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

            // Получаем задачу или null
            mTask = mAdapter.getTaskById(task.getId());

            if (mTask != null) {
                // Получаем позицию и скролим к задаче
                mPosition = mAdapter.getPosition(mTask.getId());
                mRecyclerView.scrollToPosition(mPosition);
            }
        }
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
        if (mPosition != RecyclerView.NO_POSITION) {
            mAdapter.resetSelection();
            mTaskOptionsPanel.setVisibility(View.INVISIBLE);
            mPosition = RecyclerView.NO_POSITION;
            mTask = null;
        }
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

    public void taskOptionsButtons(View view) {
        int id = view.getId();

        // Сохраняем id задачи для передачи в диалог
        Bundle args = new Bundle();
        args.putLong("taskId", mTask.getId());

        // Обрабатываем нажатие
        switch (id) {
            case R.id.task_edit_title_button:
                DialogFragment editTaskDialog = new EditTaskDialog();
                editTaskDialog.setArguments(args);
                editTaskDialog.show(getSupportFragmentManager(), "editTaskDialog");
                break;
            case R.id.task_set_date_button:
                DialogFragment dateDialog = new DateDialog();
                dateDialog.setArguments(args);
                dateDialog.show(getSupportFragmentManager(), "dateDialog");
                break;
            case R.id.task_set_priority_button:
                DialogFragment priorityDialog = new PriorityDialog();
                priorityDialog.setArguments(args);
                priorityDialog.show(getSupportFragmentManager(), "priorityDialog");
                break;
            case R.id.task_set_group_button:
                DialogFragment groupDialog = new GroupDialog();
                groupDialog.setArguments(args);
                groupDialog.show(getSupportFragmentManager(), "groupDialog");
                break;
            case R.id.task_set_repeat_button:
                DialogFragment repeatDialog = new RepeatDialog();
                repeatDialog.setArguments(args);
                repeatDialog.show(getSupportFragmentManager(), "repeatDialog");
                break;
            case R.id.task_set_reminder_button:
                mRealm.beginTransaction();
                boolean reminder = mTask.getReminder();
                mTask.setReminder(!reminder);
                mRealm.commitTransaction();
                break;
        }
    }

    @Override
    public void onDialogFinish(DialogFragment dialog) {
        Log.d("log", "Запущен onDialogFinish в TaskListActivity");

        // Сбрасываем выделение и позицию
        mAdapter.resetSelection();
        mPosition = RecyclerView.NO_POSITION;

        // Получаем задачу или null
        mTask = mAdapter.getTaskById(mTask.getId());

        if (mTask != null) {
            // Получаем позицию, скролим к задаче и выделяем ее
            mPosition = mAdapter.getPosition(mTask.getId());
            mRecyclerView.scrollToPosition(mPosition);
            selectItem(mPosition);
        } else {
            // Скрываем панель инструментов
            mTaskOptionsPanel.setVisibility(View.INVISIBLE);
        }
    }
}
