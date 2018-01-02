package ru.kulikovman.tasklist;

import android.app.AlarmManager;
import android.app.PendingIntent;
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
import android.support.v4.content.res.ResourcesCompat;
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
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Map;

import io.realm.OrderedRealmCollection;
import io.realm.Realm;

import ru.kulikovman.tasklist.adapters.MenuAdapter;
import ru.kulikovman.tasklist.dialogs.DateDialog;
import ru.kulikovman.tasklist.dialogs.EditTaskDialog;
import ru.kulikovman.tasklist.dialogs.GroupDialog;
import ru.kulikovman.tasklist.dialogs.PriorityDialog;
import ru.kulikovman.tasklist.dialogs.RepeatDialog;
import ru.kulikovman.tasklist.models.Group;
import ru.kulikovman.tasklist.models.Task;
import ru.kulikovman.tasklist.adapters.TaskAdapter;

public class TaskListActivity extends AppCompatActivity implements TaskAdapter.OnItemClickListener,
        MenuAdapter.OnItemClickListener, CallbackDialogFragment.CallbackDialogListener {

    private Realm mRealm;
    public RecyclerView mTaskRecyclerView, mMenuRecyclerView;
    private TaskAdapter mTaskAdapter;
    private MenuAdapter mMenuAdapter;
    private String LOG = "log";

    private RealmHelper mRealmHelper;

    private Task mTask;
    private Group mGroup;
    private int mPosition = -1;

    private EditText mTaskField;
    private LinearLayout mTaskOptionsPanel;
    private ImageButton mSetRepeatButton, mSetReminderButton;
    private TextView mUnfinishedTasks, mIncomeTasks, mTodayTasks, mWeekTasks, mMonthTasks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_list);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Подключаем базу данных
        Realm.init(this);
        mRealm = Realm.getDefaultInstance();
        mRealmHelper = RealmHelper.get();

        // Боковое меню
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            @Override
            public void onDrawerStateChanged(int newState) {
                super.onDrawerStateChanged(newState);
                Log.d(LOG, "Запущен onDrawerStateChanged в onCreate / TaskListActivity");
                updateTaskCounters();
                mMenuAdapter.notifyDataSetChanged();
                hideKeyboard();
            }
        };
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        // Инициализируем вью бокового меню
        NavigationView navigationView = findViewById(R.id.nav_view);
        mMenuRecyclerView = navigationView.findViewById(R.id.menu_group_recycler_view);
        mUnfinishedTasks = navigationView.findViewById(R.id.menu_all_tasks_counter);
        mIncomeTasks = navigationView.findViewById(R.id.menu_tasks_income_counter);
        mTodayTasks = navigationView.findViewById(R.id.menu_tasks_today_counter);
        mWeekTasks = navigationView.findViewById(R.id.menu_tasks_week_counter);
        mMonthTasks = navigationView.findViewById(R.id.menu_tasks_month_counter);

        // Инициализируем базовые вью элементы
        mTaskRecyclerView = findViewById(R.id.task_recycler_view);
        mTaskField = findViewById(R.id.task_field);
        mTaskOptionsPanel = findViewById(R.id.task_options_panel);
        mSetRepeatButton = findViewById(R.id.task_set_repeat_button);
        mSetReminderButton = findViewById(R.id.task_set_reminder_button);

        // Создаем и запускаем списки
        setUpTaskRecyclerView(mRealmHelper.getWeekTasks(), true);
        setUpMenuRecyclerView();

        // Заголовок списка
        setTitle(R.string.list_title_week_tasks);

        // Напоминание
        initNotify();

        // Смена фокуса поля ввода
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
        mTaskRecyclerView.setAdapter(null);
        mRealmHelper = null;
        mRealm.close();
    }

    private void initNotify() {
        // Устанавливаем время напоминания
        //long currentTime = System.currentTimeMillis();

        Calendar notifyTime = DateHelper.getTodayCalendarWithoutTime();
        notifyTime.set(Calendar.HOUR_OF_DAY, 11);
        notifyTime.set(Calendar.MINUTE, 0);
        notifyTime.set(Calendar.SECOND, 0);

        // Перенос на день ,если время прошло
        /*if (notifyTime.getTimeInMillis() < currentTime) {
            notifyTime.add(Calendar.DAY_OF_YEAR, 1);
        }*/

        // Создаем напоминание
        Intent intent = new Intent(this, NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 10452, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, notifyTime.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);

        Log.d("log", "Время уведомления: " + notifyTime.getTime());
    }

    private void setUpTaskRecyclerView(OrderedRealmCollection<Task> list, boolean resetGroup) {
        // Сброс группы
        if (resetGroup) {
            mGroup = null;
        }

        mTaskAdapter = new TaskAdapter(this, list);
        mTaskRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mTaskRecyclerView.setAdapter(mTaskAdapter);
        mTaskRecyclerView.setHasFixedSize(true);
        mTaskAdapter.setOnItemClickListener(this);

        // Обработчик свайпов
        initSwipe();
    }

    private void setUpMenuRecyclerView() {
        mMenuAdapter = new MenuAdapter(this, mRealmHelper.getNotEmptyGroups());
        mMenuRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mMenuRecyclerView.setAdapter(mMenuAdapter);
        mMenuRecyclerView.setHasFixedSize(false);
        mMenuAdapter.setOnItemClickListener(this);
    }

    private void updateTaskCounters() {
        // Получаем количиство задач разных типов
        Map<String, Long> counters = mRealmHelper.getTaskCounters();

        // Устанавливаем значения в меню
        mUnfinishedTasks.setText(String.valueOf(counters.get("unfinishedTasks")));
        mIncomeTasks.setText(String.valueOf(counters.get("incomeTasks")));
        mTodayTasks.setText(String.valueOf(counters.get("todayTasks")));
        mWeekTasks.setText(String.valueOf(counters.get("weekTasks")));
        mMonthTasks.setText(String.valueOf(counters.get("monthTasks")));
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
                mTask = mTaskAdapter.getTaskByPosition(mPosition);

                // Открываем транзакцию
                mRealm.beginTransaction();

                // Понижаем счетчик в группе задачи
                if (mTask.getGroup() != null) {
                    mTask.getGroup().decreaseCountTask();
                    Log.d(LOG, "Понизили счетчик");
                }

                if (direction == ItemTouchHelper.RIGHT) {
                    // Удаляем задачу
                    mTask.deleteFromRealm();
                    Log.d(LOG, "Задача удалена");
                } else {
                    // Завершаем задачу
                    mTask.setCompletionDate(System.currentTimeMillis());
                    mTask.setDone(true);
                    Log.d(LOG, "Задача завершена");

                    // Если есть повтор, то создаем новую задачу
                    if (mTask.getRepeat() != null) {
                        // Копируем старую задачу
                        Task task = new Task(mTask.getTitle());
                        task.setPriority(mTask.getPriority());
                        task.setRepeat(mTask.getRepeat());
                        task.setReminder(mTask.getReminder());

                        // Делаем задачу управляемой
                        Task realmTask = mRealm.copyToRealm(task);

                        // Добавляем группу
                        if (mTask.getGroup() != null) {
                            realmTask.setGroup(mTask.getGroup());
                            mTask.getGroup().addTask(realmTask);
                        }

                        // Вычисляем и назначаем новую дату
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTimeInMillis(mTask.getTargetDate());

                        if (task.getRepeat().equals("day")) {
                            calendar.add(Calendar.DAY_OF_YEAR, 1);
                        } else if (task.getRepeat().equals("week")) {
                            calendar.add(Calendar.WEEK_OF_YEAR, 1);
                        } else if (task.getRepeat().equals("month")) {
                            calendar.add(Calendar.MONTH, 1);
                        } else if (task.getRepeat().equals("year")) {
                            calendar.add(Calendar.YEAR, 1);
                        }

                        realmTask.setTargetDate(calendar.getTimeInMillis());
                        Log.d(LOG, "Добавлена новая задача");
                    }
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

        // Присоединяем всю эту конструкцию к нашему mTaskRecyclerView
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(mTaskRecyclerView);
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

    // Код меню в экшен-баре (не удалять!)
    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.task_list, menu);
        return true;
    }*/

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
            // Создаем задачу
            Task task = new Task(taskTitle);

            // Открываем транзакцию
            mRealm.beginTransaction();

            // Делаем задачу управляемой и назначаем группу
            Task realmTask = mRealm.copyToRealm(task);

            if (mGroup != null) {
                realmTask.setGroup(mGroup);
                mGroup.addTask(realmTask);
            }

            // Закрываем транзакцию
            mRealm.commitTransaction();

            // Очищаем поле и ищем задачу в текущем списке
            mTaskField.setText(null);
            mTask = mTaskAdapter.getTaskById(task.getId());

            if (mTask != null) {
                // Получаем позицию и скролим к задаче
                mPosition = mTaskAdapter.getPosition(mTask.getId());
                mTaskRecyclerView.scrollToPosition(mPosition);
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
            mTaskAdapter.resetSelection();
            mTaskOptionsPanel.setVisibility(View.INVISIBLE);
            mPosition = RecyclerView.NO_POSITION;
            mTask = null;
        }
    }

    private void selectItem(int position) {
        mTaskAdapter.selectItem(position);
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

    public void menuItemClick(View view) {
        Log.d(LOG, "Запущен menuItemClick в TaskListActivity");

        int id = view.getId();

        // Действие при нажатии элемента бокового меню
        switch (id) {
            case R.id.menu_all_task:
                setUpTaskRecyclerView(mRealmHelper.getUnfinishedTasks(), true);
                setTitle(R.string.list_title_all_tasks);
                break;
            case R.id.menu_tasks_income:
                setUpTaskRecyclerView(mRealmHelper.getIncomeTasks(), true);
                setTitle(R.string.list_title_income_tasks);
                break;
            case R.id.menu_tasks_today:
                setUpTaskRecyclerView(mRealmHelper.getTodayTasks(), true);
                setTitle(R.string.list_title_today_tasks);
                break;
            case R.id.menu_tasks_week:
                setUpTaskRecyclerView(mRealmHelper.getWeekTasks(), true);
                setTitle(R.string.list_title_week_tasks);
                break;
            case R.id.menu_tasks_month:
                setUpTaskRecyclerView(mRealmHelper.getMonthTasks(), true);
                setTitle(R.string.list_title_month_tasks);
                break;
            case R.id.menu_tasks_finished:
                Intent finishedListActivity = new Intent(this, FinishedListActivity.class);
                startActivity(finishedListActivity);
                break;
            case R.id.menu_group:
                Intent groupListActivity = new Intent(this, GroupListActivity.class);
                startActivity(groupListActivity);
                break;
            /*case R.id.menu_review:
                // Переадресация на страницу приложения в маркете
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("market://details?id=ru.kulikovman.tasklist"));
                startActivity(intent);
                break;*/
        }

        // Закрываем меню и скрываем панель инструментов
        closeDrawer();
    }

    private void closeDrawer() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }

        // Скрываем панель инструментов
        mTaskOptionsPanel.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onDialogFinish(DialogFragment dialog) {
        Log.d(LOG, "Запущен onDialogFinish в TaskListActivity");

        // Сбрасываем выделение и позицию
        mTaskAdapter.resetSelection();
        mPosition = RecyclerView.NO_POSITION;

        // Получаем задачу или null
        mTask = mTaskAdapter.getTaskById(mTask.getId());

        if (mTask != null) {
            // Получаем позицию, скролим к задаче и выделяем ее
            mPosition = mTaskAdapter.getPosition(mTask.getId());
            mTaskRecyclerView.scrollToPosition(mPosition);
            selectItem(mPosition);
        } else {
            // Скрываем панель инструментов
            mTaskOptionsPanel.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onGroupMenuClick(Group group) {
        // Сохраняем группу
        mGroup = group;

        // Подключаем список задач из связанной группы
        setUpTaskRecyclerView(mRealmHelper.getTasksByGroup(group.getId()), false);

        // Название группы в заголовок
        setTitle(group.getName());

        // Закрываем меню и скрываем панель инструментов
        closeDrawer();
    }
}
