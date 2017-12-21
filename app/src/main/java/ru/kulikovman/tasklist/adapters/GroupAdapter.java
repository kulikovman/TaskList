package ru.kulikovman.tasklist.adapters;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;
import ru.kulikovman.tasklist.R;
import ru.kulikovman.tasklist.models.Group;

public class GroupAdapter extends RealmRecyclerViewAdapter<Group, GroupAdapter.GroupHolder> {
    private OrderedRealmCollection<Group> mResults;
    private Context mContext;

    private OnItemClickListener mOnItemClickListener;

    private int mSelectedPosition = -1;
    private Group mGroup;

    public GroupAdapter(Context context, OrderedRealmCollection<Group> results) {
        super(results, true);
        // Only set this if the model class has a primary key that is also a integer or long.
        // In that case, {@code getItemId(int)} must also be overridden to return the key.
        setHasStableIds(true);

        mResults = results;
        mContext = context;
    }

    @Override
    public long getItemId(int index) {
        //noinspection ConstantConditions
        return getItem(index).getId();
    }

    @Override
    public GroupHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View item = inflater.inflate(R.layout.item_group, parent, false);
        return new GroupHolder(item);
    }

    @Override
    public void onBindViewHolder(GroupHolder holder, int position) {
        // Привязка данных задачи к макету
        holder.bindGroup(mResults.get(position));

        // Выделяет или снимает выделение с элемента
        holder.itemView.setSelected(mSelectedPosition == position);
    }

    public void selectItem(int position) {
        resetSelection();
        mSelectedPosition = position;
        notifyItemChanged(mSelectedPosition);
    }

    public void resetSelection() {
        int oldPosition = mSelectedPosition;
        mSelectedPosition = RecyclerView.NO_POSITION;
        notifyItemChanged(oldPosition);
    }

    public int getPosition(long groupId) {
        // Важно искать объект по id
        for (Group group : mResults) {
            if (group.getId() == groupId) {
                return mResults.indexOf(group);
            }
        }
        return 0;
    }

    public Group getGroupById(long groupId) {
        for (Group group : mResults) {
            if (group.getId() == groupId) {
                return group;
            }
        }
        return null;
    }

    public Group getGroupByPosition(int position) {
        return mResults.get(position);
    }

    public class GroupHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView mGroupName, mTaskCounter;
        private ImageView mGroupColor;

        public GroupHolder(View view) {
            super(view);

            // Инициализируем вью элемента списка
            mGroupName = view.findViewById(R.id.item_group_name);
            mGroupColor = view.findViewById(R.id.item_group_color);
            mTaskCounter = view.findViewById(R.id.item_group_counter);

            // Слушатель нажатий по элементу
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            // Получаем группу
            mGroup = mResults.get(getLayoutPosition());

            // Код для проброса слушателя
            if (mOnItemClickListener != null) {
                mOnItemClickListener.onItemClick(getLayoutPosition(), mGroup);
            }
        }

        // Назначаем содержимое для текущего элемента списка
        void bindGroup(Group group) {
            // Делаем состояние айтема по умолчанию
            defaultStateItem();

            // Устанавливаем название группы
            mGroupName.setText(group.getName());

            // Количество задач в группе
            int countTask = group.getCountTask();
            if (countTask > 0) {
                mTaskCounter.setText(String.valueOf(countTask));
            }

            // Получаем цвет и закрашиваем ярлычок
            String color = group.getColor();

            if (color != null) {
                // Получаем id цвета из его названия
                int colorId = mContext.getResources().getIdentifier(color, "color", mContext.getPackageName());
                mGroupColor.setBackgroundResource(colorId);
            }
        }

        private void defaultStateItem() {
            // Обнуляем все поля
            mGroupName.setText(null);
            mTaskCounter.setText(null);

            // Цвет ярлычка по умолчанию
            mGroupColor.setBackgroundResource(R.color.gray_2);
        }
    }

    // Интерфейс для проброса слушателя наружу
    public interface OnItemClickListener {
        void onItemClick(int position, Group group);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }
}
