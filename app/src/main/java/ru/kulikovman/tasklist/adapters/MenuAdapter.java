package ru.kulikovman.tasklist.adapters;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;
import ru.kulikovman.tasklist.R;
import ru.kulikovman.tasklist.models.Group;

public class MenuAdapter extends RealmRecyclerViewAdapter<Group, MenuAdapter.GroupHolder> {
    private OrderedRealmCollection<Group> mResults;
    private Context mContext;
    private Group mGroup;

    private OnItemClickListener mOnItemClickListener;

    public MenuAdapter(Context context, OrderedRealmCollection<Group> results) {
        super(results, true);
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
        View item = inflater.inflate(R.layout.item_group_menu, parent, false);
        return new GroupHolder(item);
    }

    @Override
    public void onBindViewHolder(GroupHolder holder, int position) {
        // Привязка данных задачи к макету
        holder.bindGroup(mResults.get(position));
    }

    public class GroupHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView mGroupName, mGroupCounter;
        private ImageView mGroupColor;

        public GroupHolder(View view) {
            super(view);

            // Инициализируем вью элемента списка
            mGroupName = view.findViewById(R.id.menu_group_name);
            mGroupCounter = view.findViewById(R.id.menu_group_counter);
            mGroupColor = view.findViewById(R.id.menu_group_color);

            // Слушатель нажатий по элементу
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            // Получаем группу
            mGroup = mResults.get(getLayoutPosition());

            // Код для проброса слушателя
            if (mOnItemClickListener != null) {
                mOnItemClickListener.onGroupMenuClick(mGroup.getId());
            }
        }

        void bindGroup(Group group) {
            // Делаем состояние айтема по умолчанию
            defaultStateItem();

            // Устанавливаем название группы и количество задач
            mGroupName.setText(group.getName());
            mGroupCounter.setText(String.valueOf(group.getCountTask()));

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
            mGroupCounter.setText(null);

            // Цвет ярлычка по умолчанию
            mGroupColor.setBackgroundResource(R.color.gray_2);
        }
    }

    // Интерфейс для проброса слушателя наружу
    public interface OnItemClickListener {
        void onGroupMenuClick(long groupId);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }
}
